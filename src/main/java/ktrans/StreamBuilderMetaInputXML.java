package ktrans;

import java.io.File;

import org.w3c.dom.Element;

import currencies.solute.boundary.inject.BehaviorSoluteInject;
import edu.montana.cerg.simmanager.interfaces.IMetaInput;
import statemachine.io.xml.ElementHelper;
import statemachine.io.xml.XMLDocument;

public class StreamBuilderMetaInputXML extends ElementHelper implements IMetaInput {

   private XMLDocument document;
   
   public XMLDocument getXMLDocument()
   {
      return document;
   }
   
   private File workingDir;

   public File getWorkingDir()
   {
      return workingDir;
   }
   
   public StreamBuilderMetaInputXML(Element element, XMLDocument document, File workingDir) 
   {
      super(element);
      this.document = document;
      this.workingDir = workingDir;
   }

   public Long getNumCells() 
   {
      return Long.valueOf(getElement().getAttribute("numCells"));
   }

   public Double getLength() 
   {
      return Double.valueOf(getElement().getAttribute("length"));
   }

   public Double getWidth() 
   {
      return Double.valueOf(getElement().getAttribute("width"));
   }

   public Double getDepth() 
   {
      return Double.valueOf(getElement().getAttribute("depth"));
   }

   public Double getUpstreamConc() 
   {
      Element element = getFirstChildElement("upstreamconc");
      return Double.valueOf(element.getAttribute("value"));
   }

   public Double getFlow() 
   {
      Element element = getFirstChildElement("flow");
      return Double.valueOf(element.getAttribute("value"));
   }

   public Double getDispersion() 
   {
      Element element = getFirstChildElement("dispersion");
      return Double.valueOf(element.getAttribute("value"));
   }

   public Double getInjectMass() 
   {
      Element element = getFirstChildElement("inject");
      return Double.valueOf(element.getAttribute(BehaviorSoluteInject.REQ_STATE_MASS));
   }

   public Long getInjectDuration() 
   {
      Element element = getFirstChildElement("inject");
      return Long.valueOf(element.getAttribute(BehaviorSoluteInject.REQ_STATE_DURATION));
   }

   public Long getInjectStartTime() 
   {
      Element element = getFirstChildElement("inject");
      return Long.valueOf(element.getAttribute(BehaviorSoluteInject.REQ_STATE_START));
   }

}
