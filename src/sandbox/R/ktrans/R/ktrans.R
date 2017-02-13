#' Constructs a ktrans model object
#'
#' @param parentEnv the parent environment for the model
#' @return the model object
#' @examples
#' model <- ktrans()
#' @import rJava
#' @export
KtransModel <- function(defaultParam, parentEnv = .GlobalEnv)
{
   model <- new.env(parent = parentEnv);
   model$param <- defaultParam;
   
   class(model) <- c("KtransModel", class(model));
   return(model);
}