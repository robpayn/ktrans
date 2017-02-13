#' Constructs a ktrans parameter set object
#'
#' @return the parameter set object
#' @examples
#' model <- KtransParam()
#' @export
KtransParam <- function()
{
   param <- list();
   
   class(param) <- c("KtransParam", class(param));
   return(param);
}

#' Interface, reads a parameter set from an XML file
#'
#' @return the appropriate function for the class
#' @export
readXML <- function(ktransParam, file)
{
   UseMethod("readXML", ktransParam);
}

#' Reads a paremeter set from an XML file
#'
#' @return nothing
#' @import XML
#' @export
readXML.KtransParam <- function(ktransParam, file)
{
   tree = xmlTreeParse(file);
   
}