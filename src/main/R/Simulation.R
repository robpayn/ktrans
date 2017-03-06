require(XML);

# CLASS Simulation ####

Simulation <- function(
   configFile, conserveFile, activeFile
) {
   simulation <- new.env();
   class(simulation) <- c("Simulation", class(simulation));
   
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
   createBlankPlot(xlim, ylim, xlab, ylab);
   for (column in columns)
   {
      lines(
         x = simulation$conserveSolute$Time,
         y = simulation$conserveSolute[[column]],
         ...
      )
   }
}

plotActive <- function(
   simulation,
   device,
   width,
   height,
   ratio,
   columns,
   xlim,
   ylim,
   xlab,
   ylab,
   col,
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
   col = "red",
   window = c(-1, -1),
   ...
   ) 
{
   createDevice(device, width, height);
   createBlankPlot(xlim, ylim, xlab, ylab);
   for (column in columns)
   {
      lines(
         x = simulation$conserveSolute$Time,
         y = (simulation$conserveSolute[[column]] - simulation$conservebkg) 
            * ratio,
         ...
      )
   }
   for (column in columns)
   {
      lines(
         x = simulation$activeSolute$Time,
         y = simulation$activeSolute[[column]] - simulation$activebkg,
         col = col,
         ...
      )
   }
   abline(v = window, lty = "dashed");
}

# CLASS SimulationLagrange ####

SimulationLagrange <- function(
   configFile, 
   conserveFile, 
   activeFile, 
   particleDir,
   analysisWindow
   ) 
{
   simulation <- Simulation(configFile, conserveFile, activeFile);
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
   arrivalTime <- particleTable[length(particleTable[,1]),]$time;
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

plotPaths <- function(
   simulation,
   device,
   width,
   height,
   columns,
   xlim,
   ylim,
   xlab,
   ylab,
   ...
   )
{
   UseMethod("plotPaths", simulation);
}

plotPaths.SimulationLagrange <- function(
   simulation,
   device = "default",
   width = 8,
   height = 6,
   columns = 3:length(simulation$conserveSolute),
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
   createBlankPlot(xlim, ylim, xlab, ylab);
   for (column in columns)
   {
      lines(
         x = simulation$conserveSolute$Time,
         y = simulation$conserveSolute[[column]],
         ...
      )
   }
   lastConc <- 0;
   lastCount <- 0;
   for (i in 1:length(simulation$paths))
   {
      conc <- simulation$paths[[i]]$conserveOTIS[length(
         simulation$paths[[i]]$conserveOTIS
         )];
      if (conc > lastConc)
      {
         lines(
            x = simulation$paths[[i]]$time,
            y = simulation$paths[[i]]$conserveOTIS
         );
         lastConc <- conc;
      }
      else
      {
         lastCount <- i;
         break;
      }
   }

   createDevice(device, width, height);
   createBlankPlot(xlim, ylim, xlab, ylab);
   for (column in columns)
   {
      lines(
         x = simulation$conserveSolute$Time,
         y = simulation$conserveSolute[[column]],
         ...
      )
   }
   for (i in lastCount:length(simulation$paths))
   {
      lines(
         x = simulation$paths[[i]]$time,
         y = simulation$paths[[i]]$conserveOTIS
      );
   }
}