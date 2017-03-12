require(XML);

# CLASS Simulation ####

Simulation <- function(
   configFile, 
   conserveFile, 
   activeFile,
   streamLength,
   injectRatio = 1
   ) 
{
   simulation <- new.env();
   class(simulation) <- c("Simulation", class(simulation));
   
   simulation$config <- xmlInternalTreeParse(configFile);
   
   simulation$timeStep <- as.numeric(xmlValue(
      getNodeSet(simulation$config, "/document/holon/behavior/initval")[[1]]
      ));
   outputNode <- getNodeSet(simulation$config, "/document/reporters/reporter")[[1]];
   simulation$outputInterval <- as.numeric(
      xmlGetAttr(
         getNodeSet(outputNode, "interval")[[1]], 
         "interval"
         )
      );
   simulation$outputTimeStep <- 
      simulation$timeStep * simulation$outputInterval;

   conserveSoluteNode <- getNodeSet(
      simulation$config, 
      "/document/streambuilder/solute"
      )[[1]];
   simulation$conservebkg <- as.numeric(
      xmlGetAttr(conserveSoluteNode,  "bkgConc")
      );

   activeSoluteNode <- getNodeSet(
      simulation$config, 
      "/document/streambuilder/solute"
      )[[2]];
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
   simulation$activebkg <- as.numeric(
      xmlGetAttr(activeSoluteNode,  "bkgConc")
      );

   simulation$discharge <- as.numeric(
      xmlGetAttr(
         getNodeSet(simulation$config, "/document/streambuilder/flow")[[1]], 
         "initialFlow")
      );
   simulation$streamLength <- streamLength;
   simulation$streamwidth <- as.numeric(
      xmlGetAttr(
         getNodeSet(
            simulation$config, 
            "/document/streambuilder/channelgeometry/activechannel"
            )[[1]], 
         "averageWidth")
      );
   simulation$streamdepth <- as.numeric(
      xmlGetAttr(
         getNodeSet(simulation$config, "/document/streambuilder/flow")[[1]], 
         "initialDepth"
         )
      );

   simulation$xSectionArea <- 
      simulation$streamwidth * simulation$streamdepth;
   simulation$streamvel <- 
      simulation$discharge / simulation$xSectionArea;
   simulation$travelTime <- 
      simulation$streamLength / simulation$streamvel;
   simulation$injectRatio <- injectRatio;

   simulation$conserveSolute <- read.table(
      file = conserveFile,
      header = TRUE,
      stringsAsFactors = FALSE
      );
   simulation$activeSolute <- read.table(
      file = activeFile,
      header = TRUE,
      stringsAsFactors = FALSE
      );

   return(simulation);
}

plotConservative <- function(
   simulation,
   device,
   width,
   height,
   columns,
   xfactor,
   yfactor,
   xlim,
   ylim,
   xlab,
   ylab,
   ratio,
   ...
   )
{
   UseMethod("plotConservative", simulation);
}

plotConservative.Simulation <- function(
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
   ratio = 1,
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
         x = simulation$conserveSolute$Time * xfactor,
         y = simulation$conserveSolute[[column]] * yfactor * ratio
      )
   }
}

plotActive <- function(
   simulation,
   columns,
   xfactor,
   yfactor,
   xlim,
   ylim,
   ratio,
   activeColor,
   window,
   ...
   )
{
   UseMethod("plotActive", simulation);
}

plotActive.Simulation <- function(
   simulation,
   columns = 3:length(simulation$conserveSolute),
   xfactor = 1,
   yfactor = 1,
   ratio = simulation$injectRatio,
   activeColor = "red",
   window = NULL,
   ...
   ) 
{
   plotConservative.Simulation(
      simulation = simulation,
      columns = columns,
      xfactor = xfactor,
      yfactor = yfactor,
      ratio = ratio,
      ...
      );
   for (column in columns)
   {
      lines(
         x = simulation$activeSolute$Time * xfactor,
         y = (simulation$activeSolute[[column]] - simulation$activebkg) 
            * yfactor,
         col = activeColor
      )
   }
   if (!is.null(window))
   {
      abline(v = window * xfactor, lty = "dashed", col = "red");
   }
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
   class(simulation) <- c("SimulationSlug", class(simulation));
   
   simulation$releaseTime <- releaseTime;
   
   conserveSoluteNode <- getNodeSet(
      simulation$config, 
      "/document/streambuilder/solute"
      )[[1]];
   simulation$conserveMass <- as.numeric(
      xmlGetAttr(
         getNodeSet(conserveSoluteNode, "inject")[[1]], 
         "soluteMass"
         )
      );
      
   activeSoluteNode <- getNodeSet(
      simulation$config, 
      "/document/streambuilder/solute"
      )[[2]];
   simulation$activeMass <- as.numeric(
      xmlGetAttr(
         getNodeSet(activeSoluteNode, "inject")[[1]], 
         "soluteMass"
         )
      );

   if (is.null(injectRatio))
   {
      simulation$injectRatio <-
         simulation$activeMass / simulation$conserveMass;
   }
   else
   {
      simulation$injectRatio <- injectRatio;
   }

   return(simulation);
}

plotConservative.SimulationSlug <- function(
   simulation,
   xfactor = 1,
   ...
   ) 
{
   plotConservative.Simulation(
      simulation = simulation, 
      xfactor = xfactor, 
      ...
      );
   abline(v = simulation$releaseTime * xfactor, lty = "dashed", col = "green");
}

plotActive.SimulationSlug <- function(
   simulation,
   columns = 3:length(simulation$conserveSolute),
   xfactor = 1,
   yfactor = 1,
   ratio = simulation$injectRatio,
   activeColor = "red",
   window = NULL,
   ...
   ) 
{
   plotActive.Simulation(
      simulation = simulation,
      xfactor = xfactor,
      ratio = ratio,
      ...
      );
   abline(v = simulation$releaseTime * xfactor, lty = "dashed", col = "green");
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
   class(simulation) <- c("SimulationLagrange", class(simulation));
   
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
   
   return(simulation);
}

plotPathsConservative <- function(
   simulation,
   device,
   width,
   height,
   columns,
   xfactor,
   yfactor,
   xlim,
   ylim,
   xlab,
   ylab,
   pathPlotWindow,
   pathPlotInterval,
   pathPlotSequence,
   logy,
   backgroundCorrect,
   activeNR,
   ...
   )
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
   abline(v = simulation$releaseTime * xfactor, lty = "dashed", col = "green");
   for (column in columns)
   {
      time = simulation$conserveSolute$Time;
      conc = simulation$conserveSolute[[column]];
      if (backgroundCorrect || activeNR)
      {
         conc = conc - simulation$conservebkg;
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
      conc = simulation$paths[[i]]$conserveOTIS;
      if (backgroundCorrect || activeNR)
      {
         conc = conc - simulation$conservebkg;
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

plotPathsActive <- function(
   simulation,
   columns,
   xfactor,
   yfactor,
   ylim,
   pathPlotWindow,
   pathPlotInterval,
   pathPlotSequence,
   logy,
   activeColor,
   ...
   )
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
      max((simulation$conserveSolute[,columns] - simulation$conservebkg) *
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
      conc = simulation$activeSolute[[column]] - simulation$activebkg;
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
         y = (simulation$paths[[i]]$activeOTIS - simulation$activebkg) 
            * yfactor,
         col = activeColor
      );
   }
}