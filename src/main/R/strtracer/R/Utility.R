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

createBlankPlot <- function(
   x = 1,
   y = 1,
   type = "n",
   xlim, 
   ylim, 
   xlab, 
   ylab, 
   ...
   )
{
   plot(
      x = x,
      y = y,
      xlim = xlim,
      ylim = ylim,
      type = type,
      xlab = xlab,
      ylab = ylab,
      ...
      );
}
