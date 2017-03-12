package org.payn.ktrans;

import java.io.File;

import org.payn.chsm.io.xmltools.ElementHelper;
import org.payn.neoch.io.xmltools.ElementXMLInputMatrix;
import org.payn.neoch.io.xmltools.XMLDocumentMatrixConfig;
import org.payn.resources.solute.ResourceSolute;
import org.payn.simulation.metainputs.MetaInputXML;
import org.w3c.dom.Element;

/**
 * Characterizes XML meta-input for the stream simulator
 * 
 * @author v78h241
 *
 */
public class StreamBuilderMetaInputXML extends MetaInputXML<XMLDocumentMatrixConfig> {

   /**
    * XML input element from the NEO settings
    */
   private ElementXMLInputMatrix xmlInputElement;

   /**
    * Constructor based on XML input
    * 
    * @param workingDir
    *       working directory for the configuration
    * @param configPath
    *       path to the configuration file from the working directory
    * @param elementName
    *       name of the xml element with the configuration
    * @throws Exception 
    */
   public StreamBuilderMetaInputXML(File workingDir, String configPath, String elementName) 
         throws Exception 
   {
      super(workingDir, configPath, elementName);
      this.xmlInputElement = 
            ((XMLDocumentMatrixConfig)document).getBuilderElement().getXMLInputElement(workingDir);
   }

   /**
    * Get the number of cells
    * 
    * @return
    *       number of cells
    */
   public Long getNumCells() 
   {
      return Long.valueOf(helper.getAttribute("numCells"));
   }

   /**
    * Get the total length of the stream
    * 
    * @return
    *       length of stream
    */
   public Double getLength() 
   {
      return Double.valueOf(helper.getAttribute("length"));
   }

   /**
    * Get the width of the stream
    * 
    * @return
    *       width of stream
    */
   public Double getWidth() 
   {
      return Double.valueOf(helper.getAttribute("width"));
   }

   /**
    * Get the depth of the stream
    * 
    * @return
    *       depth of stream
    */
   public Double getDepth() 
   {
      return Double.valueOf(helper.getAttribute("depth"));
   }

   /**
    * Get the volumetric water flow
    * 
    * @return
    *       water flow
    */
   public Double getFlow() 
   {
      Element element = helper.getFirstChildElement("flow");
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
      Element element = helper.getFirstChildElement("flow");
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
      ElementHelper element = helper.getFirstChildElementHelper("inject");
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
            ResourceSolute.NAME_INJECT_MASS
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
            ResourceSolute.NAME_INJECT_DURATION
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
            ResourceSolute.NAME_INJECT_START
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
            ResourceSolute.NAME_INJECT_MASS
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
            ResourceSolute.NAME_INJECT_DURATION
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
            ResourceSolute.NAME_INJECT_START
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
      Element element = helper.getFirstChildElement("active");
      return Double.valueOf(element.getAttribute(ResourceSolute.NAME_UPTAKE_MAX));
   }

   /**
    * Get the half saturation concentration
    * 
    * @return
    *       half saturation concentration
    */
   public Double getHalfSat() 
   {
      Element element = helper.getFirstChildElement("active");
      return Double.valueOf(element.getAttribute(ResourceSolute.NAME_CONC_HALF_SAT));
   }

   /**
    * Get the background concentration
    * 
    * @return
    *       background concentration
    */
   public Double getActiveBkgConc() 
   {
      Element element = helper.getFirstChildElement("active");
      return Double.valueOf(element.getAttribute("bkg" + ResourceSolute.NAME_SOLUTE_CONC));
   }

   /**
    * Checks if is to be an injected boundary
    * 
    * @return
    *       true if injected, false otherwise
    */
   public boolean isInject() 
   {
      return helper.hasElement("inject");
   }

   /**
    * Get the concentration boundary path
    * 
    * @return
    *       path
    */
   public String getConcBoundFile() 
   {
      Element element = helper.getFirstChildElement("concbound");
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
      Element element = helper.getFirstChildElement("concbound");
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
      Element element = helper.getFirstChildElement("concbound");
      return element.getAttribute("delimiter");
   }

   /**
    * Get the background concentration of the conservative tracer
    * @return
    *       background concentration
    */
   public Double getConsBkgConc() 
   {
      Element element = helper.getFirstChildElement("conservative");
      return Double.valueOf(element.getAttribute("bkg" + ResourceSolute.NAME_SOLUTE_CONC));
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

   /**
    * Determines if a particle tracker is configured
    * 
    * @return
    *       true if particle tracker is configured, false otherwise
    */
   public boolean isParticle() 
   {
      return helper.hasElement("particle");
   }

   @Override
   protected XMLDocumentMatrixConfig createDocument(File metaInputFile) throws Exception 
   {
      return new XMLDocumentMatrixConfig(metaInputFile);
   }

}
