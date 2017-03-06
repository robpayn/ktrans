HyperbolicAnalysis <- function(length)
{
   analysis <- new.env();
   class(analysis) <- c("HyperbolicAnalysis", class(analysis));

   analysis$metrics <- data.frame(
      time = numeric(length = length),
      conserve = numeric(length = length),
      active = numeric(length = length),
      conservebc = numeric(length = length),
      activebc = numeric(length = length),
      ceffinject = numeric(length = length),
      cefftot = numeric(length = length),
      activenr = numeric(length = length),
      k = numeric(length = length),
      sw = numeric(length = length),
      vf = numeric(length = length),
      u = numeric(length = length),
      stringsAsFactors = FALSE
      );
   return(analysis);   
}

HyperbolicAnalysisTASCC <- function(
   simulation,
   analysisWindow,
   releaseTime,
   conserveColumn = length(simulation$conserveSolute),
   activeColumn = length(simulation$activeSolute)
   )
{
   startIndex = trunc(analysisWindow[1] / simulation$outputTimeStep) + 1;
   endIndex = trunc(analysisWindow[2] / simulation$outputTimeStep);
   length <- (endIndex - startIndex) + 1;
   
   analysis <- HyperbolicAnalysis(length);
   
   analysis$simulation <- simulation;
   
   analysis$umaxactual <- simulation$umax;
   analysis$halfsatactual <- simulation$halfsat;
   analysis$uambactual <- hyperbolic(
      umax = simulation$umax, 
      halfsat = simulation$halfsat, 
      conc = simulation$activebkg
      );
   analysis$vfambactual <- 
      analysis$uambactual / simulation$activebkg;
   analysis$kambactual <- 
      simulation$streamdepth / analysis$vfambactual;
   analysis$swambactual <- 
      (simulation$discharge * simulation$activebkg) /
      (simulation$streamwidth * analysis$uambactual);

   analysis$releaseTime <- releaseTime;
   analysis$metricsOriginal <- data.frame(
      swo = numeric(length = length),
      vfo = numeric(length = length),
      uo = numeric(length = length),
      stringsAsFactors = FALSE
      );
   
   analysis$metrics$time <- 
      simulation$conserveSolute$Time[startIndex:endIndex];
   analysis$metrics$conserve <- simulation$conserveSolute[
      startIndex:endIndex,
      conserveColumn
      ];
   analysis$metrics$active <- simulation$activeSolute[
      startIndex:endIndex,
      activeColumn
      ];
   analysis$metrics$conservebc <- 
      analysis$metrics$conserve - simulation$conservebkg;
   analysis$metrics$activebc <- 
      analysis$metrics$active - simulation$activebkg;
   analysis$metrics$k <- 
      ( log(simulation$injectRatio) 
         - log(analysis$metrics$activebc / analysis$metrics$conservebc) ) /
         (analysis$metrics$time - analysis$releaseTime);
   
   calcMetrics(analysis);
  
   return(analysis);
}

calcMetrics <- function(analysis)
{
   UseMethod("calcMetrics", analysis);
}

calcMetrics.HyperbolicAnalysis <- function(analysis)
{
   analysis$metrics$activenr <- 
      analysis$metrics$conservebc * simulation$injectRatio;
   analysis$metrics$ceffinject <- 
      sqrt(analysis$metrics$activebc * analysis$metrics$activenr);
   analysis$metrics$cefftot <- simulation$activebkg + analysis$metrics$ceffinject;
   analysis$metrics$sw <- simulation$streamvel / analysis$metrics$k;
   analysis$metrics$vf <- analysis$metrics$k * simulation$streamdepth;
   analysis$metrics$u <- analysis$metrics$vf * analysis$metrics$ceffinject;
}

run <- function(analysis)
{
   UseMethod("run", analysis);
}

run.HyperbolicAnalysis <- function(analysis)
{
   actbkg <- simulation$activebkg;

   # Regression of added solute uptake length vs. concentration
   lmresults <- lm(
     sw ~ cefftot,
     data = analysis$metrics
   );
   intercept <- as.numeric(lmresults$coefficients["(Intercept)"]);
   slope <- as.numeric(lmresults$coefficients["cefftot"]);
   swambest <- intercept;
   halfsatest <- (intercept + slope * actbkg) / slope - actbkg;
   slopeActual <- (analysis$simulation$discharge * (analysis$halfsatactual + actbkg)) /
      (analysis$simulation$streamwidth * analysis$umaxactual * analysis$halfsatactual);
   analysis$swEstimates <- list(
      intercept = intercept,
      slope = slope,
      swamb = swambest,
      uamb = (analysis$simulation$discharge * actbkg) /
         (analysis$simulation$streamwidth * swambest),
      halfsat = halfsatest,
      umax = (analysis$simulation$discharge * (halfsatest + actbkg)) /
         (analysis$simulation$streamwidth * slope * halfsatest),
      interceptActual = ((analysis$simulation$discharge * (analysis$halfsatactual + actbkg)^2) /
         (analysis$simulation$streamwidth * analysis$umaxactual * analysis$halfsatactual)) +
         slopeActual * -actbkg,
      slopeActual = slopeActual
   );

   # Nonlinear regression of hyperbolic function for uptake vs. concentration
   nlsresults <- nls(
     u ~ hyperbolicnet(umax = umaxp, halfsat = halfsatp, concadd = ceffinject, concbkg = actbkg),
     data = analysis$metrics,
     start = list(umaxp = analysis$umaxactual, halfsatp = analysis$halfsatactual)
      );
   umaxest = summary(nlsresults)$coefficients["umaxp","Estimate"];
   halfsatest = summary(nlsresults)$coefficients["halfsatp","Estimate"];
   analysis$uEstimates <- list(
      umax = umaxest,
      halfsat = halfsatest,
      uamb = hyperbolic(umax = umaxest, halfsat = halfsatest, conc = actbkg)
      );

   # Liner regression of 1/vf vs. concentration
   ineff <- 1 / analysis$metrics$vf;
   lmresults <- lm(
      ineff ~ cefftot,
      data = analysis$metrics
   );
   intercept = as.numeric(lmresults$coefficients["(Intercept)"]);
   slope = as.numeric(lmresults$coefficients["cefftot"]);
   vfambest = 1 / intercept;
   slopeActual <- (analysis$simulation$discharge * (analysis$halfsatactual + actbkg)) /
      (analysis$simulation$streamwidth * analysis$umaxactual * analysis$halfsatactual);
   analysis$vfEstimates = list(
      intercept = intercept,
      slope = slope,
      vfamb = vfambest,
      uamb = vfambest * actbkg,
      interceptActual = (1 / (analysis$simulation$streamdepth * analysis$simulation$streamvel)) *
         (((analysis$simulation$discharge * (analysis$halfsatactual + actbkg)^2) /
         (analysis$simulation$streamwidth * analysis$umaxactual * analysis$halfsatactual)) +
         slopeActual * -actbkg),
      slopeActual = (1 / (analysis$simulation$streamdepth * analysis$simulation$streamvel)) * slopeActual
   );
}

plot.HyperbolicAnalysis <- function(
   analysis,
   device = "default",
   width = 8,
   height = 6,   
   xlim = c(
      0,
      max(analysis$metrics$cefftot)
      ),
   ylim = c(
      0,
      max(analysis$metrics$u)
      ),
   xlab = "Concentration",
   ylab = "Net uptake",
   ...
   )
{
   createDevice(device, width, height);
   createBlankPlot(xlim, ylim, xlab, ylab);
   points(
      x = analysis$metrics$cefftot, 
      y = analysis$metrics$u,
      ...
      );
}

plotUptakeEstimate <- function(
   analysis, 
   device,
   xlim,
   ylim,
   xlab,
   ylab,
   ...
   )
{
   UseMethod("plotUptakeEstimate", analysis);
}

plotUptakeEstimate.HyperbolicAnalysis <- function(
   analysis, 
   device = "default",
   width = 8,
   height = 6,   
   xlim = c(
      0,
      max(analysis$metrics$cefftot)
      ),
   ylim = c(
      0,
      max(
         analysis$metrics$u + analysis$uEstimates$uamb,
         hyperbolicnet(
            analysis$umaxactual, 
            analysis$halfsatactual, 
            max(analysis$metrics$ceffinject), 
            analysis$simulation$activebkg
            ) + analysis$uambactual
         )
      ),
   xlab = "Concentration",
   ylab = "Uptake",
   ...
   )
{
   createDevice(device, width, height);
   createBlankPlot(xlim, ylim, xlab, ylab);
   points(
      x = analysis$metrics$cefftot, 
      y = analysis$metrics$u + analysis$uEstimates$uamb,
      ...
      );
   xvals <- seq(
      from = 0, 
      max(analysis$metrics$cefftot),
      length.out = 30
      );
   lines(
      x = xvals,
      y = hyperbolicnet(
         analysis$uEstimates$umax, 
         analysis$uEstimates$halfsat, 
         xvals, 
         analysis$simulation$activebkg
         ) + analysis$uEstimates$uamb,
      ...
      );
   lines(
      x = xvals,
      y = hyperbolicnet(
         analysis$umaxactual, 
         analysis$halfsatactual, 
         xvals, 
         analysis$simulation$activebkg
         ) + analysis$uambactual,
      lty = "dashed",
      ...
      );
}