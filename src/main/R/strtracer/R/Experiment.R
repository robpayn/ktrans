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
   releaseTimeCol = "black",
   releaseTimeLty = "dashed",
   ...
   ) 
{
   plotConservative.Experiment(
      experiment = experiment, 
      xfactor = xfactor, 
      ...
      );
   abline(
      v = experiment$releaseTime * xfactor, 
      lty = releaseTimeLty, 
      col = releaseTimeCol
   );
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
