package ktrans;

import java.io.File;

import org.w3c.dom.Element;

import currencies.solute.CurrencySolute;
import currencies.solute.boundary.BehaviorSoluteActiveMM;
import currencies.solute.boundary.BehaviorSoluteBoundInject;
import edu.montana.cerg.simmanager.interfaces.IMetaInput;
import statemachine.io.xml.ElementHelper;
import statemachine.io.xml.XMLDocument;

/**
 * Characterizes XML metainput for the stream solute simulator
 * 
 * @author v78h241
 *
 */
public class StreamBuilderMetaInputXML extends ElementHelper implements IMetaInput {

   /**
    * XML document with configuration information
    */
   private XMLDocument document;
   
   /**
    * Getter
    * 
    * @return
    *       XML document
    */
   public XMLDocument getXMLDocument()
   {
      return document;
   }
   
   /**
    * Working directory for the model
    */
   private File workingDir;

   /**
    * Getter
    * 
    * @return
    *       File for working directory
    */
   public File getWorkingDir()
   {
      return workingDir;
   }
   
   /**
    * Constructor based on XML input
    * 
    * @param element
    *       element with information about the metainput
    * @param document
    *       document containing the element
    * @param workingDir
    *       working directory for the model
    */
   public StreamBuilderMetaInputXML(Element element, XMLDocument document, File workingDir) 
   {
      super(element);
      this.document = document;
      this.workingDir = workingDir;
   }

   /**
    * Get the number of cells
    * 
    * @return
    *       number of cells
    */
   public Long getNumCells() 
   {
      return Long.valueOf(getElement().getAttribute("numCells"));
   }

   /**
    * Get the total length of the stream
    * 
    * @return
    *       length of stream
    */
   public Double getLength() 
   {
      return Double.valueOf(getElement().getAttribute("length"));
   }

   /**
    * Get the width of the stream
    * 
    * @return
    *       width of stream
    */
   public Double getWidth() 
   {
      return Double.valueOf(getElement().getAttribute("width"));
   }

   /**
    * Get the depth of the stream
    * 
    * @return
    *       depth of stream
    */
   public Double getDepth() 
   {
      return Double.valueOf(getElement().getAttribute("depth"));
   }

   /**
    * Get the volumetric water flow
    * 
    * @return
    *       water flow
    */
   public Double getFlow() 
   {
      Element element = getFirstChildElement("flow");
      return Double.valueOf(element.getAttribute("flow"));
   }

   /**
    * Get the dispersion coefficient
    * 
    * @return
    *       dispersion coefficient
    */
   public Double getDispersion() 
   {
      Element element = getFirstChildElement("flow");
      return Double.valueOf(element.getAttribute("dispersion"));
   }

   /**
    * Get the injection total mass
    * 
    * @return
    *       injection mass
    */
   public Double getInjectMass() 
   {
      Element element = getFirstChildElement("inject");
      return Double.valueOf(element.getAttribute(BehaviorSoluteBoundInject.REQ_STATE_MASS));
   }

   /**
    * Get the injection duration (in intervals)
    * 
    * @return
    *       injection duration
    */
   public Long getInjectDuration() 
   {
      Element element = getFirstChildElement("inject");
      return Long.valueOf(element.getAttribute(BehaviorSoluteBoundInject.REQ_STATE_DURATION));
   }

   /**
    * Get the start interval for the injection
    * 
    * @return
    *       injection start interval
    */
   public Long getInjectStartInterval() 
   {
      Element element = getFirstChildElement("inject");
      return Long.valueOf(element.getAttribute(BehaviorSoluteBoundInject.REQ_STATE_START));
   }

   /**
    * Get the maximum uptake
    * 
    * @return
    *       maximum uptake
    */
   public Double getUMax() 
   {
      Element element = getFirstChildElement("active");
      return Double.valueOf(element.getAttribute(BehaviorSoluteActiveMM.REQ_STATE_UMAX));
   }

   /**
    * Get the half saturation concentration
    * 
    * @return
    *       half saturation concentration
    */
   public Double getHalfSat() 
   {
      Element element = getFirstChildElement("active");
      return Double.valueOf(element.getAttribute(BehaviorSoluteActiveMM.REQ_STATE_HALFSAT));
   }

   /**
    * Get the background concentration
    * 
    * @return
    *       background concentration
    */
   public Double getBkgConc() 
   {
      Element element = getFirstChildElement("active");
      return Double.valueOf(element.getAttribute("bkg" + CurrencySolute.NAME_SOLUTE_CONC));
   }

   /**
    * Checks if is to be an injected boundary
    * 
    * @return
    *       true if injected, false otherwise
    */
   public boolean isInject() 
   {
      return hasElement("inject");
   }

   /**
    * Get the concentration boundary path
    * 
    * @return
    *       path
    */
   public String getConcBoundFile() 
   {
      Element element = getFirstChildElement("concbound");
      return element.getAttribute("path");
   }

   /**
    * Get the type of interpolation
    * 
    * @return
    *       interpolation type
    */
   public String getInterpolationType() 
   {
      Element element = getFirstChildElement("concbound");
      return element.getAttribute("type");
   }

   /**
    * Get the delimiter
    * 
    * @return
    *       delimiter
    */
   public String getDelimiter() 
   {
      Element element = getFirstChildElement("concbound");
      return element.getAttribute("delimiter");
   }

}
