require(XML);

# GLOBAL ####

mm <- function(umax, halfsat, conc)
{
  return((umax * conc) / (halfsat + conc));
}

mmnet <- function(umax, halfsat, concadd, concbkg)
{
  return(mm(umax, halfsat, (concadd + concbkg)) - mm(umax, halfsat, concbkg));
}

# CLASS Analysis ####

# Constructor 
Analysis <- function(rootDir, configFile, locations, legend, colors, types, 
                     location, reachLength, timeStep, injectateRatio)
{
   analysis <- new.env();
   class(analysis) <- c("Analysis", class(analysis));
   
   analysis$rootDir <- rootDir;
   analysis$configFile <- configFile;
   analysis$locations <- locations;
   analysis$legend <- legend;
   analysis$colors <- colors; 
   analysis$types <- types;
   analysis$location <- location;
   analysis$reachLength <- reachLength;
   analysis$timeStep <- timeStep;
   analysis$injectateRatio <- injectateRatio;
   
   return(analysis);
}

loadStreamData <- function(analysis)
{
   UseMethod("loadStreamData", analysis);
}

loadStreamData.Analysis <- function(analysis)
{
   config <- xmlInternalTreeParse(analysis$configFile);
                                  
   analysis$umax <- as.numeric(
      xmlGetAttr(getNodeSet(config, "/document/streambuilder/active")[[1]], "UMax")
      );
   analysis$halfsat <- as.numeric(
      xmlGetAttr(getNodeSet(config, "/document/streambuilder/active")[[1]], "HalfSatConc")
      );
   analysis$discharge <- as.numeric(
      xmlGetAttr(getNodeSet(config, "/document/streambuilder/flow")[[1]], "flow")
      );
   analysis$streamwidth <- as.numeric(
      xmlGetAttr(getNodeSet(config, "/document/streambuilder")[[1]], "width")
      );
   analysis$streamdepth <- as.numeric(
      xmlGetAttr(getNodeSet(config, "/document/streambuilder")[[1]], "depth")
      );
   analysis$streamvel <- analysis$discharge / (analysis$streamwidth * analysis$streamdepth);
   analysis$travelTime <- analysis$reachLength / analysis$streamvel;
   
   analysis$solstore <- read.table(
      file = sprintf("%s/behaviors/cons_storage.txt", analysis$rootDir), 
      header = TRUE,
      stringsAsFactors = FALSE
      );
   analysis$solact <- read.table(
      file = sprintf("%s/behaviors/active_storage.txt", analysis$rootDir), 
      header = TRUE,
      stringsAsFactors = FALSE
      );
   analysis$actbkg <- analysis$solact[
      1, sprintf("matrix.cell%s.activeConc", analysis$locations[1])
      ];
   analysis$consbkg <- analysis$solstore[
      1, sprintf("matrix.cell%s.consConc", analysis$locations[1])
      ];
   analysis$uambactual <- mm(
      umax = analysis$umax, halfsat = analysis$halfsat, conc = analysis$actbkg
   );
   analysis$swambactual <- (analysis$discharge * analysis$actbkg) / 
      (analysis$streamwidth * analysis$uambactual);
}

setup <- function(analysis, width, height, plot, filename)
{
   UseMethod("setup", analysis);
}

setup.Analysis <- function(analysis, width = 12, height = 10, 
                           plot = "off", filename = "analysis_summary")
{
   loadStreamData(analysis);
   openDevice(analysis, width, height, plot, filename);
   if (plot != "off")
   {
      plotSummary(analysis, bkgCorrect = FALSE);
   }
   if (plot == "pdf" || plot == "metafile_clip")
   {
      dev.off();
   }

   openDevice(analysis, width, height, plot, paste(filename, "bc", sep = "_"));
   if (plot != "off")
   {
      plotSummary(analysis, bkgCorrect = TRUE);
   }
   if (plot == "pdf" || plot == "metafile_clip")
   {
      dev.off();
   }

   createMetrics(analysis);
}

openDevice <- function(analysis, width, height, plot, filename)
{
   UseMethod("openDevice", analysis);
}

openDevice <- function(analysis, width, height, plot, filename)
{
   if (plot == "pdf")
   {
      pdf(
         file = sprintf("%s/%s.pdf", analysis$rootDir, filename),
         width = width,
         height = height
      );
   }
   else if (plot == "windows")
   {
      windows(
         width = width,
         height = height
      );
   }
   else if (plot == "metafile_clip")
   {
      win.metafile(
         width = width,
         height = height
      );
   }
}

saveAnalysis <- function(analysis)
{
   UseMethod("saveAnalysis", analysis);
}

saveAnalysis.Analysis <- function(analysis)
{
   save(analysis, file = sprintf(
      "%s/%s_setup.RData", 
      analysis$rootDir,
      class(analysis)[1]
      ));
}

runAnalysis <- function(analysis)
{
   UseMethod("runAnalysis", analysis);
}

runAnalysis.Analysis <- function(analysis)
{
   actbkg <- analysis$actbkg;

   # Regression of added solute uptake length vs. concentration
   lmresults <- lm(
     sw ~ cefftot,
     data = analysis$metrics
   );
   intercept <- as.numeric(lmresults$coefficients["(Intercept)"]);
   slope <- as.numeric(lmresults$coefficients["cefftot"]);
   swambest <- intercept;
   halfsatest <- (intercept + slope * actbkg) / slope - actbkg;
   slopeActual <- (analysis$discharge * (analysis$halfsat + actbkg)) / 
      (analysis$streamwidth * analysis$umax * analysis$halfsat);
   analysis$swEstimates <- list(
      intercept = intercept,
      slope = slope,
      swamb = swambest,
      uamb = (analysis$discharge * actbkg) / 
         (analysis$streamwidth * swambest),
      halfsat = halfsatest,
      umax = (analysis$discharge * (halfsatest + actbkg)) / 
         (analysis$streamwidth * slope * halfsatest),
      interceptActual = ((analysis$discharge * (analysis$halfsat + actbkg)^2) / 
         (analysis$streamwidth * analysis$umax * analysis$halfsat)) + 
         slopeActual * -actbkg,
      slopeActual = slopeActual
   );

   nlsresults <- nls(
     u ~ mmnet(umax = umaxp, halfsat = halfsatp, concadd = ceff, concbkg = actbkg), 
     data = analysis$metrics,
     start = list(umaxp = analysis$umax, halfsatp = analysis$halfsat)
   );
   umaxest = summary(nlsresults)$coefficients["umaxp","Estimate"];
   halfsatest = summary(nlsresults)$coefficients["halfsatp","Estimate"];
   xvalues <- seq(-actbkg, max(analysis$metrics$ceff), length.out=50);
   analysis$uEstimates <- list(
      umax = umaxest,
      halfsat = halfsatest,
      uamb = mm(umax = umaxest, halfsat = halfsatest, conc = actbkg),
      xvalues = xvalues,
      actualModel = mmnet(
         umax = analysis$umax, 
         halfsat = analysis$halfsat, 
         concadd = xvalues,
         concbkg = actbkg
      )
   );

   ineff <- 1 / analysis$metrics$vf;
   lmresults <- lm(
      ineff ~ cefftot,
      data = analysis$metrics
   );
   intercept = as.numeric(lmresults$coefficients["(Intercept)"]);
   slope = as.numeric(lmresults$coefficients["cefftot"]);
   vfambest = 1 / intercept;
   analysis$vfEstimates = list(
      intercept = intercept,
      slope = slope,
      vfamb = vfambest,
      uamb = vfambest * actbkg
   );

}

createMetrics <- function(analysis)
{
   UseMethod("createMetrics", analysis);
}

createMetrics.Analysis <- function(analysis)
{
   analysis$metrics$ceff <- (analysis$metrics$actbc * analysis$metrics$consbc) ^ 0.5;
   analysis$metrics$cefftot <- analysis$actbkg + analysis$metrics$ceff;
   analysis$metrics$sw <- analysis$streamvel / analysis$metrics$k;
   analysis$metrics$vf <- analysis$metrics$k * analysis$streamdepth;
   analysis$metrics$u <- analysis$metrics$vf * analysis$metrics$ceff;
}

plotSummary <- function(analysis, xlim, ylim, ...)
{
   UseMethod("plotSummary", analysis);
}

plotSummary.Analysis <- function(
   analysis, 
   xlim = c(
      0, 
      max(analysis$solstore$Time)
   ),
   ylim = c(
      0,
      max(analysis$solstore[,sprintf(
         "matrix.cell%s.consConc",
         analysis$locations[1]
      )])
   ),
   ...
)
{
   comparisonPlot(
      analysis,
      xlim = xlim, 
      ylim = ylim,
      rightMargin = 12,
      ...
   );
   marginValues(
      analysis,
      ypos = 0.6, 
      yposinterval = -0.03, 
      outer = FALSE,
      line = 0.5,
      ylim = ylim
   );
}

comparisonPlot <- function(analysis, xlim, ylim, rightMargin, legendXFraction,
                        startTime, stopTime, bkgCorrect)
{
   UseMethod("comparisonPlot", analysis);
}

comparisonPlot.Analysis <- function(analysis, xlim, ylim, rightMargin, legendXFraction = 1.05,
                                 startTime = NULL, stopTime = NULL, bkgCorrect = FALSE)
{
   par(mar=c(5,5,4,rightMargin), xpd = TRUE);
   plot(
      x = 0, 
      xlab = "Time (sec)",
      ylab = expression(paste(
         "Concentration (g  ",
         m^-3,
         ")"
      )),
      xlim = xlim,
      ylim = ylim,
      type = "n"
   );
   if (!is.null(startTime) && !is.null(stopTime))
   {
      rect(
         xleft = startTime,
         ybottom = ylim[1],
         xright = stopTime,
         ytop = ylim[2],
         col = "lightgray",
         border = FALSE
      );
   }
   
   for (i in 1:length(analysis$locations))
   {
      y <- analysis$solstore[,sprintf("matrix.cell%s.consConc",analysis$locations[i])];
      if (bkgCorrect) 
      {
         y <- y - analysis$consbkg;
      }
      lines(
         x = analysis$solstore$Time, 
         y = y, 
         col = analysis$colors[i],
         lty = analysis$types["cons"]
      );
      y <- analysis$solact[,sprintf("matrix.cell%s.activeConc",analysis$locations[i])]
      if (bkgCorrect)
      {
         y = y - analysis$actbkg;
      }
      lines(
         x = analysis$solstore$Time,
         y = y,
         col = analysis$colors[i],
         lty = analysis$types["active"]
      );
   }
   
   legend(
      x = xlim[2] * legendXFraction,
      y = max(
         analysis$solstore[,sprintf("matrix.cell%s.consConc",analysis$locations[1])]
      ),
      bty = "n",
      legend = c(paste(analysis$legend,"Conservative"),paste(analysis$legend,"Active")),
      col = analysis$colors,
      lty = c(
         rep(analysis$types["cons"], length(analysis$legend)),
         rep(analysis$types["active"], length(analysis$legend))
      )
   );
}

marginValues <- function(analysis, ypos, yposinterval, outer, line, cex, ylim)
{
   UseMethod("marginValues", analysis);
}

marginValues.Analysis <- function(analysis, ypos, yposinterval, outer, 
                                  line = 0, cex = 0.7, ylim = NULL)
{
   if (!outer)
   {
      ypos = ypos * (ylim[2] - ylim[1]);
      yposinterval = yposinterval * (ylim[2] - ylim[1]);
   }
   mtexttemp <- function(text, ypos)
   {
      mtext(
         outer = outer,
         side = 4,
         las = 1,
         at = ypos,
         cex = cex,
         text = text,
         line = line
      );
   }
   
   mtexttemp(
      text = bquote(paste(
         "Injectate Concentration Ratio = ",
         .(formatC(analysis$injectateRatio, format = "e", digits = 2))
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;
   
   mtexttemp(
      text = bquote(paste(
         "Stream Q = ",
         .(formatC(analysis$discharge, format = "e", digits = 2)),
         ~ m^3 ~ sec^-1
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;
   
   mtexttemp(
      text = bquote(paste(
         "Stream width = ",
         .(formatC(analysis$streamwidth, format = "e", digits = 2)),
         ~ m
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;

   mtexttemp(
      text = bquote(paste(
         "Stream depth = ",
         .(formatC(analysis$streamdepth, format = "e", digits = 2)),
         ~ m
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;

   mtexttemp(
      text = bquote(paste(
         "Stream average v = ",
         .(formatC(analysis$streamvel, format = "e", digits = 2)),
         ~ m ~ sec^-1
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;

   mtexttemp(
      text = bquote(paste(
         "Active ", C[amb], " = ",
         .(formatC(analysis$actbkg, format = "e", digits = 2)),
         ~ g ~ m^-3
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;

   mtexttemp(
      text = bquote(paste(
         "Actual ambient ", S[w], " = ",
         .(formatC(analysis$swambactual, format = "e", digits = 2)),
         ~ m 
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;

   mtexttemp(
      text = bquote(paste(
         "Actual ambient ", U[tot], " = ",
         .(formatC(analysis$uambactual, format = "e", digits = 2)),
         ~ g ~ m^-2 ~ sec^-1 
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;
   
   mtexttemp(
      text = bquote(paste(
         "Actual ", U[max], " = ",
         .(formatC(analysis$umax, format = "e", digits = 2)),
         ~ g ~ m^-2 ~ sec^-1 
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;
   
   mtexttemp(
      text = bquote(paste(
         "Actual ", C[half], " = ",
         .(formatC(analysis$halfsat, format = "e", digits = 2)),
         ~ g ~ m^-3 
         )),
      ypos = ypos
      );
   ypos <- ypos + yposinterval;
}

plotAnalysis <- function(analysis, width, height, plot, filename)
{
   UseMethod("plotAnalysis", analysis);
}

plotAnalysis.Analysis <- function(analysis, width = 10, height = 12, 
                          plot = "off", filename = "analysis")
{
   runAnalysis.Analysis(analysis);
   if (plot == "pdf")
   {
      pdf(
         file = sprintf("%s/%s.pdf", analysis$rootDir, filename),
         width = width,
         height = height
      );
   }
   else if (plot == "windows")
   {
      windows(width = width, height = height);
   }
   else if (plot == "metafile_clip")
   {
      win.metafile(width = width, height = height);
   }
   if (plot != "off")
   {
      plot(analysis);
   }
   if (plot == "pdf" || plot == "metafile_clip")
   {
      dev.off();
   }
}

plot.Analysis <- function(analysis)
{
   initializePlot(analysis); 
   plotSw(analysis);
   plotU(analysis);
   plotVf(analysis);
   mtext(
      text = expression(paste(
         "Concentration (g  ",
         m^-3,
         ")"
         )),
      outer = TRUE,
      side = 1,
      line = 2,
      cex = 0.8
   )
   marginValues(analysis, ypos = 0.6, yposinterval = -0.02, outer = TRUE);
}

initializePlot <- function(analysis)
{
   UseMethod("initializePlot", analysis);
}

plotSw <- function(analysis, xlim, ylim, xfrac, yfrac)
{
   UseMethod("plotSw", analysis);
}

plotSw.Analysis <- function(
   analysis, 
   xlim = c(
      0, 
      max(analysis$metrics$cefftot)
   ), 
   ylim = c(
      -0.1 * max(analysis$metrics$sw), 
      max(analysis$metrics$sw)
   ),
   xfrac = 0.35, 
   yfrac = 0.2
)
{
   plot(
      x = analysis$metrics$cefftot, 
      y = analysis$metrics$sw,
      xlim = xlim,
      ylim = ylim,
      xlab = "",
      ylab = bquote(paste(
         S["w,add"],
         " (m)"
         ))
      );
   lines(
      x = xlim,
      y = analysis$swEstimates$intercept +
         analysis$swEstimates$slope * xlim
      );
   lines(
      x = xlim, 
      y = analysis$swEstimates$interceptActual +
         analysis$swEstimates$slopeActual * xlim,
      lty = "dashed"
      );
   
   yinterval = 0.08;
   
   text(
     x = xlim[2] * xfrac,
     y = ylim[2] * yfrac,
     adj = c(0,0),
     labels = bquote(
       paste(
         "Estimated ambient ",
         S[w],
         " = ",
         .(formatC(
           analysis$swEstimates$swamb, 
           format = "e", 
           digits = 2)
         ),
         ~ m
         )
       )
     );
   yfrac = yfrac - yinterval;
   
   text(
     x = xlim[2] * xfrac,
     y = ylim[2] * yfrac,
     adj = c(0,0),
     labels = bquote(
       paste(
         "Estimated ambient ",
         U[tot],
         " = ",
         .(formatC(
           analysis$swEstimates$uamb, 
           format = "e", 
           digits = 2)
         ),
         ~ g ~ m^-2 ~ sec^-1
         )
       )
     );
   yfrac = yfrac - yinterval;
   
   text(
     x = xlim[2] * xfrac,
     y = ylim[2] * yfrac,
     adj = c(0,0),
     labels = bquote(
       paste(
         "Estimated ",
         U[max],
         " = ",
         .(formatC(
           analysis$swEstimates$umax, 
           format = "e", 
           digits = 2)
         ),
         ~ g ~ m^-2 ~ sec^-1
         )
       )
     );
   yfrac = yfrac - yinterval;
   
   text(
     x = xlim[2] * xfrac,
     y = ylim[2] * yfrac,
     adj = c(0,0),
     labels = bquote(
       paste(
         "Estimated ",
         C[half],
         " = ",
         .(formatC(
           analysis$swEstimates$halfsat, 
           format = "e", 
           digits = 2)
         ),
         ~ g ~ m^-3
         )
       )
     );
   yfrac = yfrac - yinterval;
}

plotU <- function(analysis, xlim, ylim, xfrac, yfrac, concRanges)
{
   UseMethod("plotU", analysis);
}

plotU.Analysis <- function(
   analysis, 
   xlim = c(
      0, 
      analysis$actbkg + max(analysis$metrics$cons)
   ), 
   ylim = c(
      0, 
      max(
         analysis$metrics$u + analysis$uEstimates$uamb,
         analysis$uEstimates$actualModel + analysis$uambactual
      )
   ),
   xfrac = 0.35, 
   yfrac = 0.2,
   concRanges = FALSE
)
{
   plot(
      x = 0,
      xlab = "",
      ylab = bquote(paste(
         U[tot],
         " (g  ",
         m^-2,
         " ",
         sec^-1,
         ")"
      )),
      xlim = xlim,
      ylim = ylim,
      type = "n"
   );
   if (concRanges)
   {
      for (index in 1:length(analysis$metrics$cons))
      {
         rect(
            xleft = analysis$metrics$act[index],
            ybottom = ylim[1],
            xright = analysis$metrics$cons[index] + analysis$actbkg,
            ytop = ylim[2],
            border = FALSE,
            col = "gray88"
         );
      }
   }
   points(
      x = analysis$metrics$cefftot, 
      y = analysis$metrics$u + analysis$uEstimates$uamb
   );
   lines(
      analysis$uEstimates$xvalues + analysis$actbkg, 
      analysis$uEstimates$uamb + mmnet(
         umax = analysis$uEstimates$umax, 
         halfsat = analysis$uEstimates$halfsat, 
         concadd = analysis$uEstimates$xvalues, 
         concbkg = analysis$actbkg
         )
   );
   lines(
      analysis$uEstimates$xvalues + analysis$actbkg, 
      analysis$uambactual + analysis$uEstimates$actualModel,
      lty = "dashed"
   );
   legend(
      x = xlim[2] * 0.03,
      y = ylim[2],
      legend = c("Estimated kinetic model", "Actual kinetic model"),
      lty = c("solid","dashed"),
      bty = "n"
   );

   yinterval = 0.07;
   
   text(
      x = xlim[2] * xfrac,
      y = ylim[2] * yfrac,
      adj = c(0,0),
      labels = bquote(
         paste(
            "Estimated ambient ",
            U[tot],
            " = ",
            .(formatC(
               analysis$uEstimates$uamb, 
               format = "e", 
               digits = 2)
               ),
            ~ g ~ m^-2 ~ sec^-1
            )
         )
      );
   yfrac = yfrac - yinterval;
   
   text(
      x = xlim[2] * xfrac,
      y = ylim[2] * yfrac,
      adj = c(0,0),
      labels = bquote(
         paste(
            "Estimated ",
            U[max],
            " = ",
            .(formatC(
               analysis$uEstimates$umax, 
               format = "e", 
               digits = 2
               )),
            ~ g ~ m^-2 ~ sec^-1
            )
         )
      );
   yfrac = yfrac - yinterval;
   
   text(
      x = xlim[2] * xfrac,
      y = ylim[2] * yfrac,
      adj = c(0,0),
      labels = bquote(
         paste(
            "Estimated ",
            C[half],
            " = ",
            .(formatC(
               analysis$uEstimates$halfsat, 
               format = "e", 
               digits = 2
               )),
            ~ g ~ m^-3
            )
         )
      );
   yfrac = yfrac - yinterval;
   
}

plotVf <- function(analysis, xlim, ylim, xfrac, yfrac)
{
   UseMethod("plotVf", analysis);
}

plotVf.Analysis <- function(
   analysis, 
   xlim = c(0, max(analysis$metrics$cefftot)), 
   ylim = c(0, max(1 / analysis$metrics$vf)),
   xfrac = 0.35, 
   yfrac = 0.1
)
{
   plot(
      analysis$metrics$cefftot, 
      1 / analysis$metrics$vf,
      xlim = xlim,
      ylim = ylim,
      xlab = "",
      ylab = bquote(paste(
         v[f]^-1,
         " (sec  ",
         m^-1,
         ")"
      ))
   );
   lines(
      xlim,
      analysis$vfEstimates$intercept + 
         analysis$vfEstimates$slope * xlim
   );
   
   yinterval = 0.07;
   
   text(
      x = xlim[2] * xfrac,
      y = ylim[2] * yfrac,
      adj = c(0,0),
      labels = bquote(
         paste(
            "Estimated ambient ",
            v[f],
            " = ",
            .(formatC(
               analysis$vfEstimates$vfamb, 
               format = "e", 
               digits = 2)
            ),
            ~ m ~ sec^-1
         )
      )
   );
   yfrac = yfrac - yinterval;
   
   text(
      x = xlim[2] * xfrac,
      y = ylim[2] * yfrac,
      adj = c(0,0),
      labels = bquote(
         paste(
            "Estimated ambient ",
            U[tot],
            " = ",
            .(formatC(
               analysis$vfEstimates$uamb, 
               format = "e", 
               digits = 2)
            ),
            ~ g ~ m^-2 ~ sec^-1
         )
      )
   );
   yfrac = yfrac - yinterval;
}

# CLASS TasccAnalysis ####

# Constructor 
TasccAnalysis <- function(..., startTime, stopTime)
{
   analysis <- Analysis(...);
   class(analysis) <- c("TasccAnalysis", class(analysis));

   analysis$startTime = startTime;
   analysis$stopTime = stopTime;
   
   return(analysis);
}

plotSummary.TasccAnalysis <- function(
   analysis,
   startTime = analysis$startTime,
   stopTime = analysis$stopTime,
   ...
)
{
   plotSummary.Analysis(
      analysis, 
      startTime = startTime, 
      stopTime = stopTime,
      ...
   );
}

createMetrics.TasccAnalysis <- function(analysis)
{
   startIndex = trunc(analysis$startTime / analysis$timeStep) + 1;
   stopIndex = trunc(analysis$stopTime / analysis$timeStep) + 1;
   length <- (stopIndex - startIndex) + 1;
   analysis$metrics <- data.frame(
      time = numeric(length = length),
      cons = numeric(length = length),
      act = numeric(length = length),
      consbc = numeric(length = length),
      actbc = numeric(length = length),
      ceff = numeric(length = length),
      cefftot = numeric(length = length),
      k = numeric(length = length),
      sw = numeric(length = length),
      vf = numeric(length = length),
      u = numeric(length = length),
      swo = numeric(length = length),
      vfo = numeric(length = length),
      uo = numeric(length = length),
      stringsAsFactors = FALSE
   );
   analysis$metrics$time <- analysis$solstore$Time[startIndex:stopIndex];
   analysis$metrics$cons <- analysis$solstore[
      startIndex:stopIndex,
      sprintf("matrix.cell%s.consConc", analysis$location)
      ];
   analysis$metrics$act <- analysis$solact[
      startIndex:stopIndex,
      sprintf("matrix.cell%s.activeConc", analysis$location)
      ];
   analysis$metrics$consbc <- analysis$metrics$cons - analysis$consbkg;
   analysis$metrics$actbc <- analysis$metrics$act - analysis$actbkg;
   analysis$metrics$k <- (log(analysis$injectateRatio) - log(analysis$metrics$actbc / analysis$metrics$consbc)) / 
      (analysis$metrics$time - 8);
   
   createMetrics.Analysis(analysis);

   analysis$metrics$swo <- analysis$reachLength / 
      (log(analysis$injectateRatio) - log(analysis$metrics$actbc / analysis$metrics$consbc));
   analysis$metrics$uo <- (analysis$discharge * analysis$metrics$ceff) / 
      (analysis$streamwidth * analysis$metrics$swo);
   analysis$metrics$vfo <- analysis$metrics$uo / analysis$metrics$ceff;
}

initializePlot.TasccAnalysis <- function(analysis)
{
   par(mfrow = c(3,2), mar=c(1.5,5,1,2), oma=c(4,1,2,17));
}

plotAnalysis.TasccAnalysis <- function(analysis, width = 15, height = 12, ...)
{
   plotAnalysis.Analysis(analysis, width = width, height = height, ...);
}

plot.TasccAnalysis <- function(analysis)
{
   plot.Analysis(analysis);
   mtext(
      text = "Based on uniform travel time",
      side = 3,
      outer = TRUE,
      adj = 0.22,
      cex = 0.8
   );
   mtext(
      text = "Based on variable travel times",
      side = 3,
      outer = TRUE,
      adj = 0.83,
      cex = 0.8
   );
}

plotSw.TasccAnalysis <- function(
   analysis, 
   xlim = c(0, max(analysis$metrics$cefftot)), 
   ylim = c(
      -0.1 * max(analysis$metrics$sw, analysis$metrics$swo), 
      max(analysis$metrics$sw, analysis$metrics$swo)
   ),
   ...
)
{
   plot(
      x = analysis$metrics$cefftot,
      y = analysis$metrics$swo,
      xlim = xlim,
      ylim = ylim,
      xlab = "",
      ylab = bquote(paste(
         S["w,add"],
         " (m)"
      ))
   );
   lines(
      x = xlim, 
      y = analysis$swEstimates$interceptActual + 
         analysis$swEstimates$slopeActual * xlim,
      lty = "dashed"
      );
   plotSw.Analysis(analysis, xlim, ylim, ...);
}

plotU.TasccAnalysis <- function(
   analysis, 
   xlim = c(0, analysis$actbkg + max(analysis$metrics$cons)),
   ylim = c(0, max(
      analysis$metrics$u + analysis$uEstimates$uamb,
      analysis$metrics$uo + analysis$uEstimates$uamb,
      analysis$uEstimates$actualModel + analysis$uambactual
   )),
   ...
)
{
   plot(
      x = analysis$metrics$cefftot, 
      y = analysis$metrics$uo + analysis$uEstimates$uamb,
      xlab = "",
      ylab = bquote(paste(
         U[tot],
         " (g  ",
         m^-2,
         " ",
         sec^-1,
         ")"
      )),
      xlim = xlim,
      ylim = ylim,
      type = "p"
   );
   lines(
      x = analysis$uEstimates$xvalues + analysis$actbkg, 
      y = analysis$uambactual + analysis$uEstimates$actualModel,
      lty = "dashed"
   );
   plotU.Analysis(analysis, xlim, ylim, ...);
}

plotVf.TasccAnalysis <- function(
   analysis, 
   xlim = c(0, max(analysis$metrics$cefftot)), 
   ylim = c(0, max(
      1 / analysis$metrics$vf,
      1 / analysis$metrics$vfo
   )),
   ...
)
{
   plot(
      analysis$metrics$cefftot, 
      1 / analysis$metrics$vfo,
      xlim = xlim,
      ylim = ylim,
      xlab = "",
      ylab = bquote(paste(
         v[f]^-1,
         " (sec  ",
         m^-1,
         ")"
      ))
   );
   plotVf.Analysis(analysis, xlim, ylim, ...)
}

# CLASS MultilevelAnalysis ####

# Constructor 
MultilevelAnalysis <- function(..., times)
{
   analysis <- Analysis(...);
   class(analysis) <- c("MultilevelAnalysis", class(analysis));
   
   analysis$times <- times;
   analysis$indeces <- analysis$times / analysis$timeStep + 1
   
   return(analysis);
}

plotSummary.MultilevelAnalysis <- function(
   analysis,
   ylim = c(
      0,
      max(analysis$solstore[,sprintf(
         "matrix.cell%s.consConc",
         analysis$locations[1]
      )])
   ),
   ...
)
{
   plotSummary.Analysis(
      analysis,
      ylim = ylim,
      ...
   );
   for (time in analysis$times)
   {
      ylimbuffer = 0.04 * (ylim[2]-ylim[1]);
      lines(
         x = rep(time,2),
         y = c(ylim[1]-ylimbuffer, ylim[2]+ylimbuffer),
         lty="dotted"
      );
   }
}

createMetrics.MultilevelAnalysis <- function(analysis)
{
   length <- length(analysis$indeces);
   analysis$metrics <- data.frame(
      time = numeric(length = length),
      cons = numeric(length = length),
      act = numeric(length = length),
      actbc = numeric(length = length),
      ceff = numeric(length = length),
      cefftot = numeric(length = length),
      k = numeric(length = length),
      sw = numeric(length = length),
      vf = numeric(length = length),
      u = numeric(length = length),
      stringsAsFactors = FALSE
   );

   analysis$metrics$time <- analysis$time;
   analysis$metrics$cons <- analysis$solstore[
      analysis$indeces,
      sprintf("matrix.cell%s.consConc", analysis$location)
      ];
   analysis$metrics$act <- analysis$solact[
      analysis$indeces,
      sprintf("matrix.cell%s.activeConc", analysis$location)
      ];
   analysis$metrics$actbc <- analysis$metrics$act - analysis$actbkg;
   analysis$metrics$k <- (log(analysis$injectateRatio) - log(analysis$metrics$actbc / analysis$metrics$consbc)) / 
      analysis$travelTime;
   
   createMetrics.Analysis(analysis);
}

initializePlot.MultilevelAnalysis <- function(analysis)
{
   par(mfcol = c(3,1), mar=c(1.5,5,1,2), oma=c(4,1,1,17));
}

plotU.MultilevelAnalysis <- function(
   analysis, 
   concRanges = TRUE, 
   ...)
{
   plotU.Analysis(analysis, concRanges = concRanges, ...);
}
