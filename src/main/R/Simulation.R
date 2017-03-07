require(XML);

# CLASS Simulation ####

Simulation <- function(
   configFile, 
   conserveFile, 
   activeFile, 
   releaseTime
   ) 
{
   simulation <- new.env();
   class(simulation) <- c("Simulation", class(simulation));
   
   simulation$releaseTime <- releaseTime;
   
   config <- xmlInternalTreeParse(configFile);
   
   simulation$timeStep <- as.numeric(xmlValue(
      getNodeSet(config, "/document/holon/behavior/initval")[[1]]
      ));
   outputNode <- getNodeSet(config, "/document/outputters/output")[[1]];
   simulation$outputInterval <- as.numeric(
      xmlGetAttr(
         getNodeSet(outputNode, "interval")[[1]], 
         "interval"
         )
      );
   simulation$outputTimeStep <- 
      simulation$timeStep * simulation$outputInterval;

   conserveSoluteNode <- getNodeSet(
      config, 
      "/document/streambuilder/solute"
      )[[1]];
   simulation$conservebkg <- as.numeric(
      xmlGetAttr(conserveSoluteNode,  "bkgConc")
      );
   simulation$conserveMass <- as.numeric(
      xmlGetAttr(
         getNodeSet(conserveSoluteNode, "inject")[[1]], 
         "soluteMass"
         )
      );

   activeSoluteNode <- getNodeSet(
      config, 
      "/document/streambuilder/solute"
      )[[2]];
   simulation$activeMass <- as.numeric(
      xmlGetAttr(
         getNodeSet(activeSoluteNode, "inject")[[1]], 
         "soluteMass"
         )
      );
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
         getNodeSet(config, "/document/streambuilder/flow")[[1]], 
         "initialFlow")
      );
   simulation$streamlength <- as.numeric(
      xmlGetAttr(
         getNodeSet(
            config, 
            "/document/streambuilder/channelgeometry"
            )[[1]], 
         "length")
      );
   simulation$streamwidth <- as.numeric(
      xmlGetAttr(
         getNodeSet(
            config, 
            "/document/streambuilder/channelgeometry/activechannel"
            )[[1]], 
         "averageWidth")
      );
   simulation$streamdepth <- as.numeric(
      xmlGetAttr(
         getNodeSet(config, "/document/streambuilder/flow")[[1]], 
         "initialDepth"
         )
      );
   
   
   simulation$xSectionArea <- 
      simulation$streamwidth * simulation$streamdepth;
   simulation$streamvel <- 
      simulation$discharge / simulation$xSectionArea;
   simulation$travelTime <- 
      simulation$reachLength / simulation$streamvel;
   simulation$injectRatio <-
      simulation$activeMass / simulation$conserveMass;

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
   ...
   ) 
{
   createDevice(device, width, height);
   par(...);
   createBlankPlot(
      xlim = xlim * xfactor, 
      ylim = ylim * yfactor, 
      xlab = xlab, 
      ylab = ylab
      );
   for (column in columns)
   {
      lines(
         x = simulation$conserveSolute$Time * xfactor,
         y = simulation$conserveSolute[[column]] * yfactor
      )
   }
   abline(v = simulation$releaseTime * xfactor, lty = "dashed", col = "green");
}

plotActive <- function(
   simulation,
   device,
   width,
   height,
   ratio,
   columns,
   xfactor,
   yfactor,
   xlim,
   ylim,
   xlab,
   ylab,
   activeColor,
   window,
   ...
   )
{
   UseMethod("plotActive", simulation);
}

plotActive.Simulation <- function(
   simulation,
   device = "default",
   width = 8,
   height = 6,
   ratio = simulation$injectRatio,
   columns = 3:length(simulation$conserveSolute),
   xfactor = 1,
   yfactor = 1,
   xlim = c(
      min(simulation$conserveSolute$Time),
      max(simulation$conserveSolute$Time)
      ),
   ylim = c(
      0,
      max(simulation$conserveSolute[,columns]) * ratio
      ),
   xlab = "Time",
   ylab = "Concentration",
   activeColor = "red",
   window = NULL,
   ...
   ) 
{
   createDevice(device, width, height);
   par(...);
   createBlankPlot(
      xlim = xlim * xfactor, 
      ylim = ylim * yfactor,
      xlab = xlab, 
      ylab = ylab
      );
   for (column in columns)
   {
      lines(
         x = simulation$conserveSolute$Time * xfactor,
         y = (simulation$conserveSolute[[column]] - simulation$conservebkg) 
            * ratio * yfactor
      )
   }
   for (column in columns)
   {
      lines(
         x = simulation$activeSolute$Time * xfactor,
         y = (simulation$activeSolute[[column]] - simulation$activebkg) 
            * yfactor,
         col = activeColor,
         ...
      )
   }
   abline(v = simulation$releaseTime * xfactor, lty = "dashed", col = "green");
   if (!is.null(window))
   {
      abline(v = window * xfactor, lty = "dashed", col = "red");
   }
}

# CLASS SimulationLagrange ####

SimulationLagrange <- function(
   configFile, 
   conserveFile, 
   activeFile, 
   particleDir,
   analysisWindow,
   releaseTime, 
   pathTimeWindow = analysisWindow[2]
   ) 
{
   simulation <- Simulation(configFile, conserveFile, activeFile, releaseTime);
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