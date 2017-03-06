hyperbolic <- function(umax, halfsat, conc)
{
  return((umax * conc) / (halfsat + conc));
}

hyperbolicnet <- function(umax, halfsat, concadd, concbkg)
{
  return(hyperbolic(umax, halfsat, (concadd + concbkg)) - hyperbolic(umax, halfsat, concbkg));
}

createDevice <- function(deviceName, width, height) 
{
   switch(
      deviceName, 
      windows = windows(width = width, height = height),
      message("Using active graphics device.")
      );
}

createBlankPlot <- function(xlim, ylim, xlab, ylab, ...)
{
   plot(
      x = 0,
      y = 0,
      xlim = xlim,
      ylim = ylim,
      type = "n",
      xlab = xlab,
      ylab = ylab,
      ...
      );
}
