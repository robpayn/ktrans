package org.payn.stream;

import java.io.File;

import org.payn.chsm.io.xml.ElementHelper;
import org.w3c.dom.Element;

/**
 * Abstract meta input for a NEOCH stream model
 * 
 * @author robpayn
 *
 */
public abstract class MetaInputXMLStream extends MetaInputXMLNEOCH {
   
   /**
    * An element helper for the geometry element
    * 
    * @author v78h241
    *
    */
   private class ElementGeometry extends ElementHelper {

      /**
       * Elevation element
       */
      private ElementHelper elementElevation;
      
      /**
       * Active channel element
       */
      private ElementHelper elementActiveChannel;

      /**
       * Construct a new instance associated with the provided element
       * 
       * @param helper 
       *        the element helper containing the geometry element
       */
      public ElementGeometry(ElementHelper helper) 
      {
         super(helper.getFirstChildElement("channelgeometry"));
      }

      /**
       * Get the length attribute value
       * 
       * @return
       *        length value
       */
      public String getAttributeStreamLength() 
      {
         return getAttribute("length");
      }
      
      /**
       * Get the elevation element
       * 
       * @return
       *       Elevation element reference
       */
      private ElementHelper getElementElevation() 
      {
         if (elementElevation == null)
         {
            elementElevation = helper.getFirstChildElementHelper("elevation");
         }
         return elementElevation;
      }

      /**
       * Get the element for active channel configuration
       * 
       * @return
       *        active channel element
       */
      private ElementHelper getElementActiveChannel() 
      {
         if (elementActiveChannel == null)
         {
            elementActiveChannel = helper.getFirstChildElementHelper("activechannel");
         }
         return elementActiveChannel;
      }

      /**
       * Get the datum attribute value
       * 
       * @return
       *        datum value
       */
      public String getAttributeElevationDatum() 
      {
         return getElementElevation().getAttribute("datum");
      }

      /**
       * Get the bed slope attribute value
       * 
       * @return
       *        bed slope attribute value
       */
      public String getAttributeBedSlope() 
      {
         return getElementElevation().getAttribute("bedSlope");
      }

      /**
       * Get the value for the depth attribute
       * 
       * @return
       *        value for the depth attribute
       */
      public String getAttributeActiveDepth() 
      {
         return getElementActiveChannel().getAttribute("depth");
      }

      /**
       * Get the value for the average width attribute
       * 
       * @return
       *        average width value
       */
      public String getAttributeAverageWidth() 
      {
         return getElementActiveChannel().getAttribute("averageWidth");
      }

   }

   private class ElementFlow extends ElementHelper {

      private ElementHelper elementFricton;
      private ElementHelper elementUpstreamBound;

      public ElementFlow(ElementHelper helper) 
      {
         super(helper.getFirstChildElement("flow"));
      }

      public String getAttributeInitialDepth() 
      {
         return getAttribute("initialDepth");
      }
      
      public String getAttributeInitialFlow() 
      {
         return getAttribute("initialFlow");
      }

      private ElementHelper getElementFriction() 
      {
         if (elementFricton == null)
         {
            elementFricton = helper.getFirstChildElementHelper("friction");
         }
         return elementFricton;
      }

      private ElementHelper getElementUpstreamBound() 
      {
         if (elementUpstreamBound == null)
         {
            elementUpstreamBound = helper.getFirstChildElementHelper("upstreambound");
         }
         return elementUpstreamBound;
      }

      public String getAttributeWieleInt() 
      {
         return getElementFriction().getAttribute("wieleInt");
      }

      public String getAttributeWieleSlope() 
      {
         return getElementFriction().getAttribute("wieleSlope");
      }

      public String getAttributeUpstreamFlowPath() 
      {
         return getElementUpstreamBound().getAttribute("upstreamPath");
      }

      public String getAttributeUpstreamFlowDelimeter() 
      {
         return getElementUpstreamBound().getAttribute("upstreamDelimiter");
      }

      public String getAttributeUpstreamInterpType() 
      {
         return getElementUpstreamBound().getAttribute("upstreamInterpType");
      }

   }
   
   /**
    * Element helper for the model structure
    */
   private ElementHelper elementStructure;

   /**
    * Element for channel geometry configuration
    */
   private ElementGeometry elementGeometry;
   
   /**
    * Element for the flow configuration
    */
   private ElementFlow elementFlow;

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
   public MetaInputXMLStream(File workingDir, String path,
         String elementName) throws Exception 
   {
      super(workingDir, path, elementName);
   }
   
   /**
    * Get the model structure element
    * 
    * @return
    */
   private ElementHelper getElementModelStructure() 
   {
      if (elementStructure == null)
      {
         elementStructure = helper.getFirstChildElementHelper("modelstructure");
      }
      return elementStructure;
   }
   /**
    * Get the geometry element
    * 
    * @return
    *       geometry element helper object
    */
   public ElementGeometry getElementGeometry()
   {
      if (elementGeometry == null)
      {
         elementGeometry = new ElementGeometry(helper);
      }
      return elementGeometry;
   }

   /**
    * Get the flow element
    * 
    * @return
    *       flow element helper object
    */
   private ElementFlow getElementFlow() 
   {
      if (elementFlow == null)
      {
         elementFlow = new ElementFlow(helper);
      }
      return elementFlow;
   }

  /**
    * Get the number of cells from the meta input
    * 
    * @return
    *       number of cells
    */
   public Long getAttributeNumCells() 
   {
      return Long.valueOf(getElementModelStructure().getAttribute("numCells"));
   }
   
   /**
    * Get the length of the stream
    * 
    * @return
    *       stream length
    */
   public Double getAttributeStreamLength()
   {
      return Double.valueOf(getElementGeometry().getAttributeStreamLength());
   }

   /**
    * Get the length of the stream
    * 
    * @return
    *       stream length
    */
   public Double getAttributeBedSlope()
   {
      return Double.valueOf(getElementGeometry().getAttributeBedSlope());
   }

   /**
    * Get the elevation datum
    * 
    * @return
    *       stream length
    */
   public Double getAttributeElevationDatum()
   {
      return Double.valueOf(getElementGeometry().getAttributeElevationDatum());
   }

   /**
    * Get the active channel depth
    * 
    * @return
    *       active channel depth
    */
   public Double getAttributeActiveDepth()
   {
      return Double.valueOf(getElementGeometry().getAttributeActiveDepth());
   }

   /**
    * Get the initial channel depth
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeInitialDepth()
   {
      return Double.valueOf(getElementFlow().getAttributeInitialDepth());
   }

   /**
    * Get the average width of the active channel
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeAverageWidth()
   {
      return Double.valueOf(getElementGeometry().getAttributeAverageWidth());
   }

   /**
    * Get the intercept of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeWieleInt()
   {
      return Double.valueOf(getElementFlow().getAttributeWieleInt());
   }

   /**
    * Get the slope of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeWieleSlope()
   {
      return Double.valueOf(getElementFlow().getAttributeWieleSlope());
   }

   /**
    * Get the slope of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeInitialFlow()
   {
      return Double.valueOf(getElementFlow().getAttributeInitialFlow());
   }
  
   /**
    * Get the delimiter for the upstream flow file
    * 
    * @return
    *       delimiter character
    */
   public String getAttributeUpstreamFlowDelimiter()
   {
      return getElementFlow().getAttributeUpstreamFlowDelimeter();
   }
   
   /**
    * Get the path to the interpolation file for upstream boundary
    * 
    * @return
    *       relative path to working directory
    */
   public String getAttributeUpstreamFlowPath()
   {
      return getElementFlow().getAttributeUpstreamFlowPath();
   }
   
   /**
    * Get the interpolation type for upstream boundary
    * 
    * @return
    *       interpolation type
    */
   public String getAttributeUpstreamInterpType()
   {
      return getElementFlow().getAttributeUpstreamInterpType();
   }

   /**
    * Get the name for cells
    * 
    * @return
    *       cell name as a string
    */
   public String getAttributeCellName()
   {
      return getElementModelStructure().getAttribute("cellName");
   }
   
   /**
    * Get the name for cells
    * 
    * @return
    *       cell name as a string
    */
   public String getAttributeBoundaryName()
   {
      return getElementModelStructure().getAttribute("boundaryName");
   }
   
}
