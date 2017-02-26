package org.payn.stream;

import java.io.File;

import org.payn.chsm.io.xml.ElementHelper;

/**
 * Abstract meta input for a NEOCH stream model
 * 
 * @author robpayn
 *
 */
public abstract class StreamMetaInputXML extends NEOCHMetaInputXML {

   /**
    * Element for channel geometry configuration
    */
   protected ElementHelper geometryElement;
   
   /**
    * Element for the flow configuration
    */
   private ElementHelper flowElement;

   /**
    * Construct a new instance that uses the provided working directory,
    * path to the configuration file, and the name of the XML element
    * with the configuration information
    * 
    * @param workingDir
    * @param path
    * @param elementName
    * @throws Exception
    */
   public StreamMetaInputXML(File workingDir, String path,
         String elementName) throws Exception 
   {
      super(workingDir, path, elementName);
   }
   
   /**
    * Get the geometry element
    * 
    * @return
    *       geometry element helper object
    */
   public ElementHelper getGeometryElement()
   {
      if (geometryElement == null)
      {
         geometryElement = helper.getFirstChildElementHelper("channelgeometry");
      }
      return geometryElement;
   }

   /**
    * Get the flow element
    * 
    * @return
    *       flow element helper object
    */
   private ElementHelper getFlowElement() 
   {
      if (flowElement == null)
      {
         flowElement = helper.getFirstChildElementHelper("flow");
      }
      return flowElement;
   }

  /**
    * Get the number of cells from the meta input
    * 
    * @return
    *       number of cells
    */
   public Long getNumCells() 
   {
      return Long.valueOf(helper.getAttribute("numCells"));
   }
   
   /**
    * Get the length of the stream
    * 
    * @return
    *       stream length
    */
   public Double getStreamLength()
   {
      return Double.valueOf(getGeometryElement().getAttribute("length"));
   }

   /**
    * Get the length of the stream
    * 
    * @return
    *       stream length
    */
   public Double getBedSlope()
   {
      return Double.valueOf(getGeometryElement().getAttribute("bedSlope"));
   }

   /**
    * Get the elevation datum
    * 
    * @return
    *       stream length
    */
   public Double getElevationDatum()
   {
      return Double.valueOf(getGeometryElement().getAttribute("elevationDatum"));
   }

   /**
    * Get the active channel depth
    * 
    * @return
    *       active channel depth
    */
   public Double getActiveDepth()
   {
      return Double.valueOf(getGeometryElement().getAttribute("activeDepth"));
   }

   /**
    * Get the initial channel depth
    * 
    * @return
    *       initial channel depth
    */
   public Double getInitialDepth()
   {
      return Double.valueOf(getGeometryElement().getAttribute("initialDepth"));
   }

   /**
    * Get the average width of the active channel
    * 
    * @return
    *       initial channel depth
    */
   public Double getAverageWidth()
   {
      return Double.valueOf(getGeometryElement().getAttribute("averageWidth"));
   }

   /**
    * Get the intercept of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getWieleInt()
   {
      return Double.valueOf(getFlowElement().getAttribute("wieleInt"));
   }

   /**
    * Get the slope of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getWieleSlope()
   {
      return Double.valueOf(getFlowElement().getAttribute("wieleSlope"));
   }

}
