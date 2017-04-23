require(XML);

# CLASS Experiment ####

Experiment <- function(
   discharge,
   streamLength,
   streamWidth,
   streamDepth,
   xSectionArea = streamWidth * streamDepth,
   streamVel = discharge / (streamWidth * streamDepth),
   travelTime = streamLength / 
      (discharge / (streamWidth * streamDepth)),
   conserveSolute,
   activeSolute,
   conserveBkg,
   activeBkg,
   injectRatio = 1
   )
{
   experiment <- new.env();
   
   experiment$discharge <- discharge;
   experiment$streamLength <- streamLength;
   experiment$streamWidth <- streamWidth;
   experiment$streamDepth <- streamDepth;
   experiment$xSectionArea <- xSectionArea;
   experiment$streamVel <- streamVel;
   experiment$travelTime <- travelTime;
   experiment$conserveSolute <- conserveSolute;
   experiment$activeSolute <- activeSolute;
   experiment$conserveBkg <- conserveBkg;
   experiment$activeBkg <- activeBkg;
   experiment$injectRatio <- injectRatio;
   
   class(experiment) <- c("Experiment", class(experiment));
   return(experiment);
}

plotConservative <- function(experiment, ...)
{
   UseMethod("plotConservative", experiment);
}

plotConservative.Experiment <- function(
   experiment,
   device = "default",
   width = 8,
   height = 6,
   columns = 3:length(experiment$conserveSolute),
   xfactor = 1,
   yfactor = 1,
   xlim = c(
      min(experiment$conserveSolute$Time),
      max(experiment$conserveSolute$Time)
      ),
   ylim = c(
      0,
      max(
         if (backgroundCorrect) (experiment$conserveSolute[,columns] - experiment$conserveBkg)
         else experiment$conserveSolute[,columns]
         )
      ),
   xlab = "Time",
   ylab = "Concentration",
   ratio = 1,
   backgroundCorrect = TRUE,
   ...
   ) 
{
   createDevice(device, width, height);
   par(...);
   createBlankPlot(
      xlim = xlim * xfactor, 
      ylim = ylim * yfactor * ratio, 
      xlab = xlab, 
      ylab = ylab
      );
   for (column in columns)
   {
      lines(
         x = experiment$conserveSolute$Time * xfactor,
         y = if (backgroundCorrect) 
               ((experiment$conserveSolute[[column]] - experiment$conserveBkg) * yfactor * ratio)
            else (experiment$conserveSolute[[column]] * yfactor * ratio)
      )
   }
}

plotActive <- function(experiment, ...)
{
   UseMethod("plotActive", experiment);
}

plotActive.Experiment <- function(
   experiment,
   columns = 3:length(experiment$conserveSolute),
   xfactor = 1,
   yfactor = 1,
   ratio = experiment$injectRatio,
   activeColor = "red",
   window = NULL,
   ...
   ) 
{
   plotConservative.Experiment(
      experiment = experiment,
      columns = columns,
      xfactor = xfactor,
      yfactor = yfactor,
      ratio = ratio,
      ...
      );
   for (column in columns)
   {
      lines(
         x = experiment$activeSolute$Time * xfactor,
         y = (experiment$activeSolute[[column]] - experiment$activeBkg) 
            * yfactor,
         col = activeColor
      )
   }
   if (!is.null(window))
   {
      abline(v = window * xfactor, lty = "dashed", col = "red");
   }
}

# CLASS ExperimentSlug ####

ExperimentSlug <- function(
   experiment = Experiment(injectRatio = injectRatio, ...),
   releaseTime,
   conserveMass,
   activeMass,
   injectRatio = activeMass / conserveMass,
   ...
   )
{
   experiment$releaseTime <- releaseTime;
   experiment$conserveMass <- conserveMass;
   experiment$activeMass <- activeMass;
   experiment$injectRatio <- injectRatio;

   class(experiment) <- c("ExperimentSlug", class(experiment));
   return(experiment);   
}

plotConservative.ExperimentSlug <- function(
   experiment,
   xfactor = 1,
   ...
   ) 
{
   plotConservative.Experiment(
      experiment = experiment, 
      xfactor = xfactor, 
      ...
      );
   abline(v = experiment$releaseTime * xfactor, lty = "dashed", col = "green");
}

plotActive.ExperimentSlug <- function(
   experiment,
   xfactor = 1,
   ratio = experiment$injectRatio,
   releaseTimeCol = "black",
   releaseTimeLty = "dashed",
   ...
   ) 
{
   plotActive.Experiment(
      experiment = experiment,
      xfactor = xfactor,
      ratio = ratio,
      ...
      );
   abline(
      v = experiment$releaseTime * xfactor, 
      lty = releaseTimeLty, 
      col = releaseTimeCol
      );
}

# CLASS Simulation ####

Simulation <- function(
   configFile, 
   holonFile,
   conserveFile, 
   activeFile,
   ...
   ) 
{
   config <- xmlInternalTreeParse(configFile);

   conserveSoluteNode <- getNodeSet(
      config, 
      "/document/stream/solute"
      )[[1]];
   conserveBkg <- as.numeric(
      xmlGetAttr(conserveSoluteNode,  "bkgConc")
      );
   activeSoluteNode <- getNodeSet(
      config, 
      "/document/stream/solute"
      )[[2]];
   activeBkg <- as.numeric(
      xmlGetAttr(activeSoluteNode,  "bkgConc")
      );
   discharge <- as.numeric(
      xmlGetAttr(
         getNodeSet(config, "/document/stream/flow")[[1]], 
         "initialFlow")
      );
   streamWidth <- as.numeric(
      xmlGetAttr(
         getNodeSet(
            config, 
            "/document/stream/channelgeometry/activechannel"
            )[[1]], 
         "averageWidth")
      );
   streamDepth <- as.numeric(
      xmlGetAttr(
         getNodeSet(config, "/document/stream/flow")[[1]], 
         "initialDepth"
         )
      );
   conserveSolute <- read.table(
      file = conserveFile,
      header = TRUE,
      stringsAsFactors = FALSE
      );
   activeSolute <- read.table(
      file = activeFile,
      header = TRUE,
      stringsAsFactors = FALSE
      );

   simulation <- Experiment(
      discharge = discharge,
      streamWidth = streamWidth,
      streamDepth = streamDepth,
      conserveSolute = conserveSolute,
      activeSolute = activeSolute,
      conserveBkg = conserveBkg,
      activeBkg = activeBkg,
      ...
      );
   
   simulation$config <- config;
   
   # Get the model timing characteristics
   simulation$timeStep <- as.numeric(
      xmlGetAttr(
         getNodeSet(simulation$config, "/document/time")[[1]], 
         "timeInterval")[[1]]
      );
   outputNode <- getNodeSet(simulation$config, "/document/reporters/reporter")[[1]];
   simulation$outputInterval <- as.numeric(
      xmlGetAttr(
         getNodeSet(outputNode, "interval")[[1]], 
         "interval"
         )
      );
   simulation$outputTimeStep <- 
      simulation$timeStep * simulation$outputInterval;

   # Get the actual umax and halfsat parameters used in the model
   simulation$umax <- as.numeric(
      xmlGetAttr(
         getNodeSet(activeSoluteNode, "hyperbolic")[[1]], 
         "uptakeMax"
         )
      );
   simulation$halfsat <- as.numeric(
      xmlGetAttr(
         getNodeSet(activeSoluteNode, "hyperbolic")[[1]],
         "concHalfSat"
         )
      );
   
   # Calculate the actual uptake characteristics of the model
   simulation$uambactual <- hyperbolic(
      umax = simulation$umax, 
      halfsat = simulation$halfsat, 
      conc = simulation$activeBkg
      );

   simulation$vfambactual <- 
      simulation$uambactual / simulation$activeBkg;
   vfSlopeActual <- (simulation$discharge * (simulation$halfsat + simulation$activeBkg)) /
      (simulation$streamWidth * simulation$umax * simulation$halfsat);
   simulation$vfInterceptActual = (1 / (simulation$streamDepth * simulation$streamVel)) *
      (((simulation$discharge * (simulation$halfsat + simulation$activeBkg)^2) /
      (simulation$streamWidth * simulation$umax * simulation$halfsat)) +
      vfSlopeActual * -simulation$activeBkg);
   simulation$vfSlopeActual = (1 / 
      (simulation$streamDepth * simulation$streamVel) ) *
      vfSlopeActual;

   simulation$kambactual <- 
      simulation$streamDepth / simulation$vfambactual;

   simulation$swambactual <- 
      (simulation$discharge * simulation$activeBkg) /
      (simulation$streamWidth * simulation$uambactual);
   simulation$swSlopeActual <- (simulation$discharge * (simulation$halfsat + simulation$activeBkg)) /
      (simulation$streamWidth * simulation$umax * simulation$halfsat);
   simulation$swInterceptActual = ((simulation$discharge * (simulation$halfsat + simulation$activeBkg)^2) /
      (simulation$streamWidth * simulation$umax * simulation$halfsat)) +
      simulation$swSlopeActual * -simulation$activeBkg;

   class(simulation) <- c("Simulation", class(simulation));
   return(simulation);
}

# CLASS SimulationSlug ####

SimulationSlug <- function(
   configFile, 
   conserveFile, 
   activeFile, 
   streamLength,
   injectRatio = NULL,
   releaseTime
   )
{
   simulation <- Simulation(
      configFile = configFile, 
      conserveFile = conserveFile, 
      activeFile = activeFile,
      streamLength = streamLength
      );

   conserveSoluteNode <- getNodeSet(
      simulation$config, 
      "/document/stream/solute"
      )[[1]];
   conserveMass <- as.numeric(
      xmlGetAttr(
         getNodeSet(conserveSoluteNode, "inject")[[1]], 
         "soluteMass"
         )
      );
      
   activeSoluteNode <- getNodeSet(
      simulation$config, 
      "/document/stream/solute"
      )[[2]];
   activeMass <- as.numeric(
      xmlGetAttr(
         getNodeSet(activeSoluteNode, "inject")[[1]], 
         "soluteMass"
         )
      );

   if (is.null(injectRatio))
   {
      injectRatio <-
         activeMass / conserveMass;
   }

   simulation <- ExperimentSlug(
      experiment = simulation, 
      releaseTime = releaseTime,
      conserveMass = conserveMass,
      activeMass = activeMass,
      injectRatio = injectRatio
      );
   
   class(simulation) <- c("SimulationSlug", class(simulation));
   return(simulation);
}

# CLASS SimulationLagrange ####

SimulationLagrange <- function(
   configFile, 
   conserveFile, 
   activeFile, 
   streamLength,
   particleDir,
   analysisWindow,
   releaseTime, 
   pathTimeWindow = analysisWindow[2]
   ) 
{
   simulation <- SimulationSlug(
      configFile = configFile, 
      conserveFile = conserveFile, 
      activeFile = activeFile,
      streamLength = streamLength,
      releaseTime = releaseTime
      );
   
   times <- seq(
      from = analysisWindow[1], 
      to = analysisWindow[2], 
      by = simulation$outputTimeStep
      );
   simulation$paths <- vector("list", length(times));

   fileName <- paste(
      particleDir,
      sprintf("/particle_%06d.txt", 0),
      sep = ""
      );
   particleTable <- read.table(file=fileName, header=TRUE);
   arrivalTime <- particleTable$time[length(particleTable$time)];
   pathStartTime <- arrivalTime - pathTimeWindow;
   particleTable <- particleTable[(particleTable$time > pathStartTime),];
   simulation$paths[[1]] <- particleTable;
   
   for (metricsCount in 1:length(times)) 
   {
      metricTime <- times[metricsCount];
      count <- 1;
      continue <- TRUE;
      while(continue) 
      {
         fileName <- paste(
            particleDir,
            sprintf("/particle_%06d.txt", count),
            sep = ""
            );
         nextParticleTable <- 
            read.table(file=fileName, header=TRUE, sep=" ");
         nextArrivalTime <- 
            nextParticleTable[length(nextParticleTable[,1]),]$time;
         nextPathStartTime <- 
            nextArrivalTime - pathTimeWindow;
         nextParticleTable <- 
            nextParticleTable[(nextParticleTable$time > nextPathStartTime),];

         if (nextArrivalTime > metricTime) {
            continue <- FALSE;
            if (nextArrivalTime - metricTime < metricTime - arrivalTime) {
               simulation$paths[[metricsCount]] <- nextParticleTable;
               count <- count - 1;
            }
         } else {
            particleTable <- nextParticleTable;
            arrivalTime <- nextArrivalTime;
            simulation$paths[[metricsCount]] <- particleTable;
         }

         count = count + 1;
      }
   }
   
   class(simulation) <- c("SimulationLagrange", class(simulation));
   return(simulation);
}

plotPathsConservative <- function(simulation, ...)
{
   UseMethod("plotPathsConservative", simulation);
}

plotPathsConservative.SimulationLagrange <- function(
   simulation,
   device = "default",
   width = 8,
   height = 6,
   columns = 3:length(simulation$conserveSolute),
   xfactor = 1,
   yfactor = 1,
   xlim = c(
      min(simulation$conserveSolute$Time),
      max(simulation$conserveSolute$Time)
      ),
   ylim = c(
      0,
      max(simulation$conserveSolute[,columns])
      ),
   xlab = "Time",
   ylab = "Concentration",
   pathPlotWindow = c(
      1,
      length(simulation$paths)
      ),
   pathPlotInterval = 1,
   pathPlotSequence = seq(
      from = pathPlotWindow[1], 
      to = pathPlotWindow[2], 
      by = pathPlotInterval
      ),
   logy = FALSE,
   backgroundCorrect = FALSE,
   activeNR = FALSE,
   releaseTimeCol = "black",
   releaseTimeLty = "dashed",
   ...
   )
{
   createDevice(device, width, height);
   par(...);
   if (logy)
   {
      log = "y";
      if (ylim[1] <= 0)
      {
         ylim[1] <- ylim[2] / 100;
      }
   }
   else
   {
      log = "";
   }
   createBlankPlot(
      xlim = xlim * xfactor, 
      ylim = ylim * yfactor, 
      xlab = xlab, 
      ylab = ylab,
      log = log
      );
   abline(
      v = simulation$releaseTime * xfactor, 
      lty = releaseTimeLty, 
      col = releaseTimeCol
      );
   for (column in columns)
   {
      time = simulation$conserveSolute$Time;
      conc = simulation$conserveSolute[[column]];
      if (backgroundCorrect || activeNR)
      {
         conc = conc - simulation$conserveBkg;
      }
      if (activeNR)
      {
         conc = conc * simulation$injectRatio;
      }
      if (logy)
      {
         time = time[conc > 0];
         conc = conc[conc > 0];
      }
      lines(
         x = time * xfactor,
         y = conc * yfactor
      )
   }
   for (i in pathPlotSequence)
   {
      conc = simulation$paths[[i]]$conserve;
      if (backgroundCorrect || activeNR)
      {
         conc = conc - simulation$conserveBkg;
      }
      if (activeNR)
      {
         conc = conc * simulation$injectRatio;
      }
      lines(
         x = simulation$paths[[i]]$time * xfactor,
         y = conc * yfactor
      );
   }
}

plotPathsActive <- function(simulation, ...)
{
   UseMethod("plotPathsActive", simulation);
}

plotPathsActive.SimulationLagrange <- function(
   simulation,
   columns = 3:length(simulation$conserveSolute),
   xfactor = 1,
   yfactor = 1,
   ylim = c(
      0,
      max((simulation$conserveSolute[,columns] - simulation$conserveBkg) *
         simulation$injectRatio)
      ), 
   pathPlotWindow = c(
      1,
      length(simulation$paths)
      ),
   pathPlotInterval = 1,
   pathPlotSequence = seq(
      from = pathPlotWindow[1], 
      to = pathPlotWindow[2], 
      by = pathPlotInterval
      ),
   logy = FALSE,
   activeColor = "red",
   ...
   )
{
   plotPathsConservative(
      simulation, 
      columns = columns,
      xfactor = xfactor,
      yfactor = yfactor,
      ylim = ylim,
      pathPlotWindow = pathPlotWindow,
      pathPlotInterval= pathPlotInterval,
      pathPlotSequence = pathPlotSequence,
      logy = logy,
      activeNR = TRUE,
      ...
      );
   for (column in columns)
   {
      time = simulation$activeSolute$Time;
      conc = simulation$activeSolute[[column]] - simulation$activeBkg;
      if (logy)
      {
         time = time[conc > 0];
         conc = conc[conc > 0];
      }
      lines(
         x = time * xfactor,
         y = conc * yfactor,
         col = activeColor
      )
   }
   for (i in pathPlotSequence)
   {
      lines(
         x = simulation$paths[[i]]$time * xfactor,
         y = (simulation$paths[[i]]$active - simulation$activeBkg) 
            * yfactor,
         col = activeColor
      );
   }
}