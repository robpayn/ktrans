package ktrans;

import java.io.File;

import org.w3c.dom.Element;

import chsm.io.xml.ElementHelper;
import currencies.solute.CurrencySolute;
import currencies.solute.boundary.BehaviorSoluteActiveMM;
import currencies.solute.boundary.BehaviorSoluteBoundInject;
import edu.montana.cerg.simmanager.interfaces.IMetaInput;
import neoch.io.xml.ElementXMLInput;
import neoch.io.xml.XMLDocumentConfig;

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
   private XMLDocumentConfig document;
   
   /**
    * Getter
    * 
    * @return
    *       XML document
    */
   public XMLDocumentConfig getXMLDocument()
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
    * XML input element from the NEO settings
    */
   private ElementXMLInput xmlInputElement;

   
   /**
    * Constructor based on XML input
    * 
    * @param element
    *       element with information about the metainput
    * @param document
    *       document containing the element
    * @param workingDir
    *       working directory for the model
    * @throws Exception 
    */
   public StreamBuilderMetaInputXML(Element element, XMLDocumentConfig document, File workingDir) throws Exception 
   {
      super(element);
      this.document = document;
      this.workingDir = workingDir;
      this.xmlInputElement = document.getBuilderElement().getXMLInputElement(workingDir);
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
    * Get an attribute for a specific solute in the inject
    * 
    * @param tracerTag
    *       tag for the attribute containing the value
    * @param attribute
    *       attribute containing the value
    * @return
    *       string value of attribute
    */
   public String getInjectAttribute(String tracerTag, String attribute)
   {
      ElementHelper element = getFirstChildElementHelper("inject");
      Element elementCons = element.getFirstChildElement(tracerTag);
      return elementCons.getAttribute(attribute);
   }
   
   /**
    * Get the injection total mass
    * 
    * @return
    *       injection mass
    */
   public Double getConservativeInjectMass() 
   {
      return Double.valueOf(getInjectAttribute(
            "conservativeinj", 
            BehaviorSoluteBoundInject.REQ_STATE_MASS
            ));
   }
   
   /**
    * Get the injection duration (in intervals)
    * 
    * @return
    *       injection duration
    */
   public Long getConservativeInjectDuration() 
   {
      return Long.valueOf(getInjectAttribute(
            "conservativeinj", 
            BehaviorSoluteBoundInject.REQ_STATE_DURATION
            ));
   }

   /**
    * Get the start interval for the injection
    * 
    * @return
    *       injection start interval
    */
   public Long getConservativeInjectStartInterval() 
   {
      return Long.valueOf(getInjectAttribute(
            "conservativeinj", 
            BehaviorSoluteBoundInject.REQ_STATE_START
            ));
   }

   /**
    * Get the injection total mass
    * 
    * @return
    *       injection mass
    */
   public Double getActiveInjectMass() 
   {
      return Double.valueOf(getInjectAttribute(
            "activeinj", 
            BehaviorSoluteBoundInject.REQ_STATE_MASS
            ));
   }
   
   /**
    * Get the injection duration (in intervals)
    * 
    * @return
    *       injection duration
    */
   public Long getActiveInjectDuration() 
   {
      return Long.valueOf(getInjectAttribute(
            "activeinj", 
            BehaviorSoluteBoundInject.REQ_STATE_DURATION
            ));
   }

   /**
    * Get the start interval for the injection
    * 
    * @return
    *       injection start interval
    */
   public Long getActiveInjectStartInterval() 
   {
      return Long.valueOf(getInjectAttribute(
            "activeinj", 
            BehaviorSoluteBoundInject.REQ_STATE_START
            ));
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
   public Double getActiveBkgConc() 
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

   /**
    * Get the background concentration of the conservative tracer
    * @return
    *       background concentration
    */
   public Double getConsBkgConc() 
   {
      Element element = getFirstChildElement("conservative");
      return Double.valueOf(element.getAttribute("bkg" + CurrencySolute.NAME_SOLUTE_CONC));
   }

   /**
    * Determine if the active injection should have unique settings
    * 
    * @return
    *       true if unique, false otherwise
    */
   public boolean isActivInjUnique() 
   {
      return Boolean.valueOf(getInjectAttribute("activeinj", "unique"));
   }

   /**
    * Get the cell file from the NEO settings
    * 
    * @return
    *       cell file
    * @throws Exception
    */
   public File getCellFile() throws Exception 
   {
      return xmlInputElement.getCellFile();
   }

   /**
    * Get the boundary file from the NEO settings
    * 
    * @return
    *       boundary file
    * @throws Exception
    */
   public File getBoundaryFile() throws Exception 
   {
      return xmlInputElement.getBoundaryFile();
   }

}
