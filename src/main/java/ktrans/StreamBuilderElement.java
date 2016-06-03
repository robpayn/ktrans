package ktrans;

import org.w3c.dom.Element;

import statemachine.io.xml.ElementHelper;

public class StreamBuilderElement extends ElementHelper {

   public StreamBuilderElement(Element element) 
   {
      super(element);
   }

   public long getNumCells() 
   {
      return Long.valueOf(getElement().getAttribute("numCells"));
   }

   public double getLength() 
   {
      return Double.valueOf(getElement().getAttribute("length"));
   }

   public double getWidth() 
   {
      return Double.valueOf(getElement().getAttribute("width"));
   }

   public double getDepth() 
   {
      return Double.valueOf(getElement().getAttribute("depth"));
   }

   public Double getUpstreamConc() 
   {
      Element element = getFirstChildElement("upstreamconc");
      return Double.valueOf(element.getAttribute("value"));
   }

   public double getFlow() 
   {
      Element element = getFirstChildElement("flow");
      return Double.valueOf(element.getAttribute("value"));
   }

   public double getDispersion() 
   {
      Element element = getFirstChildElement("dispersion");
      return Double.valueOf(element.getAttribute("value"));
   }

}
