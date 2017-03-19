package org.payn.stream;

import java.io.File;

import org.payn.chsm.io.xmltools.ElementHelper;

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
      public Double getAttributeStreamLength() 
      {
         return getAttributeDouble("length");
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
            elementElevation = getFirstChildElementHelper("elevation");
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
            elementActiveChannel = getFirstChildElementHelper("activechannel");
         }
         return elementActiveChannel;
      }

      /**
       * Get the datum attribute value
       * 
       * @return
       *        datum value
       */
      public Double getAttributeElevationDatum() 
      {
         ElementHelper helper = getElementElevation();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("datum");
         }
      }

      /**
       * Get the bed slope attribute value
       * 
       * @return
       *        bed slope attribute value
       */
      public Double getAttributeBedSlope() 
      {
         ElementHelper helper = getElementElevation();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("bedSlope");
         }
      }

      /**
       * Get the value for the depth attribute
       * 
       * @return
       *        value for the depth attribute
       */
      public Double getAttributeActiveDepth() 
      {
         ElementHelper helper = getElementActiveChannel();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("depth");
         }
      }

      /**
       * Get the value for the average width attribute
       * 
       * @return
       *        average width value
       */
      public Double getAttributeAverageWidth() 
      {
         ElementHelper helper = getElementActiveChannel();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("averageWidth");
         }
      }

   }

   /**
    * Element for flow configuration data
    * 
    * @author v78h241
    *
    */
   private class ElementFlow extends ElementHelper {

      /**
       * Element for friction
       */
      private ElementHelper elementFricton;
      
      /**
       * Element for upstream boundary conditions
       */
      private ElementHelper elementUpstreamBound;

      /**
       * Element with information about dispersion
       */
      private ElementHelper elementDispersion;

      /**
       * Construct a flow element based on the flow element
       * in the provided helper
       * 
       * @param helper
       */
      public ElementFlow(ElementHelper helper) 
      {
         super(helper.getFirstChildElement("flow"));
      }

      /**
       * Get the initial depth attribute
       * 
       * @return
       *    value of initial depth
       */
      public Double getAttributeInitialDepth() 
      {
         return getAttributeDouble("initialDepth");
      }
      
      /**
       * Get the initial flow attribute
       * 
       * @return
       *        value of initial flow
       */
      public Double getAttributeInitialFlow() 
      {
         return getAttributeDouble("initialFlow");
      }

      /**
       * Get the friction element
       * 
       * @return
       *        friction element
       */
      private ElementHelper getElementFriction() 
      {
         if (elementFricton == null)
         {
            elementFricton = getFirstChildElementHelper("friction");
         }
         return elementFricton;
      }

      /**
       * Get the upstream boundary element
       * 
       * @return
       *        upstream boundary element
       */
      private ElementHelper getElementUpstreamBound() 
      {
         if (elementUpstreamBound == null)
         {
            elementUpstreamBound = getFirstChildElementHelper("upstreambound");
         }
         return elementUpstreamBound;
      }

      /**
       * Get the element with information about dipsersion
       * 
       * @return
       */
      private ElementHelper getElementDispersion() 
      {
         if (elementDispersion == null)
         {
            elementDispersion = getFirstChildElementHelper("dispersion");
         }
         return elementDispersion;
      }

      /**
       * Get the Chezey coefficient friction attribute
       * 
       * @return
       *        Chezey coefficient
       */
      public Double getAttributeChezey() 
      {
         ElementHelper helper = getElementFriction();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("chezey");
         }
      }

      /**
       * Get the Wiele model intercept attribute
       * 
       * @return
       *        value of Wiele model intercept
       */
      public Double getAttributeWieleInt() 
      {
         ElementHelper helper = getElementFriction();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("wieleInt");
         }
      }

      /**
       * Get the Wiele model slope attribute
       * 
       * @return
       *        value of the Wiele model slope
       */
      public Double getAttributeWieleSlope() 
      {
         ElementHelper helper = getElementFriction();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("wieleSlope");
         }
      }

      /**
       * Get the flow path attribute
       * 
       * @return
       *        value of the flow path
       */
      public String getAttributeUpstreamFlowPath() 
      {
         return getElementUpstreamBound().getAttribute("upstreamPath");
      }

      /**
       * Get the delimiter attribute
       * 
       * @return
       *        value of the delimiter
       */
      public String getAttributeUpstreamFlowDelimeter() 
      {
         return getElementUpstreamBound().getAttribute("upstreamDelimiter");
      }

      /**
       * Get the interpolation type attribute
       * 
       * @return
       *        value of the interpolation type
       */
      public String getAttributeUpstreamInterpType() 
      {
         return getElementUpstreamBound().getAttribute("upstreamInterpType");
      }

      /**
       * Get the dispersion coefficient attribute
       * 
       * @return
       *        Double object
       */
      public Double getAttributeDispersionCoefficient() 
      {
         ElementHelper helper = getElementDispersion();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("coefficient");
         }
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
      return getElementModelStructure().getAttributeLong("numCells");
   }
   
   /**
    * Get the length of the stream
    * 
    * @return
    *       stream length
    */
   public Double getAttributeStreamLength()
   {
      return getElementGeometry().getAttributeStreamLength();
   }

   /**
    * Get the length of the stream
    * 
    * @return
    *       stream length
    */
   public Double getAttributeBedSlope()
   {
      ElementGeometry helper = getElementGeometry();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeBedSlope();
      }
   }

   /**
    * Get the elevation datum
    * 
    * @return
    *       stream length
    */
   public Double getAttributeElevationDatum()
   {
      ElementGeometry helper = getElementGeometry();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeElevationDatum();
      }
   }

   /**
    * Get the active channel depth
    * 
    * @return
    *       active channel depth
    */
   public Double getAttributeActiveDepth()
   {
      ElementGeometry helper = getElementGeometry();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeActiveDepth();
      }
   }

   /**
    * Get the initial channel depth
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeInitialDepth()
   {
      ElementFlow helper = getElementFlow();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeInitialDepth();
      }
   }

   /**
    * Get the average width of the active channel
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeAverageWidth()
   {
      ElementGeometry helper = getElementGeometry();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeAverageWidth();
      }
   }

   /**
    * Get the dispersion coefficient attribute
    * 
    * @return
    *       Double object
    */
   public Double getAttributeDispersionCoefficient()
   {
      ElementFlow helper = getElementFlow();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeDispersionCoefficient();
      }
   }

   /**
    * Get the Chezey coefficient friction attribute
    * 
    * @return
    *        Chezey coefficient
    */
   public Double getAttributeChezey() 
   {
      ElementFlow helper = getElementFlow();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeChezey();
      }
   }

   /**
    * Determine if the Wiele model of friction vs. depth
    * is configured
    * 
    * @return
    *       true if configured, false otherwise
    */
   public boolean isWieleConfigured() 
   {
      return (getAttributeWieleInt() != null) 
            && (getAttributeWieleSlope() != null);
   }

   /**
    * Get the intercept of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeWieleInt()
   {
      ElementFlow helper = getElementFlow();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeWieleInt();
      }
   }

   /**
    * Get the slope of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeWieleSlope()
   {
      ElementFlow helper = getElementFlow();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeWieleSlope();
      }
   }

   /**
    * Get the slope of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeInitialFlow()
   {
      ElementFlow helper = getElementFlow();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeInitialFlow();
      }
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
      return getElementModelStructure().getAttributeString("cellName");
   }
   
   /**
    * Get the name for cells
    * 
    * @return
    *       cell name as a string
    */
   public String getAttributeBoundaryName()
   {
      return getElementModelStructure().getAttributeString("boundaryName");
   }
   
   /**
    * Is the initial conditions flag set?
    * 
    * @return
    *       true if initial conditions flag is set, false otherwise
    */
   public boolean isInitialConditions()
   {
      String value = helper.getAttribute("initialConditions");
      if (value.equals(""))
      {
         return false;
      }
      else
      {
         return Boolean.valueOf(helper.getAttribute("initialConditions"));
      }
   }
   
   /**
    * Get the path to the initial condition file for cells
    * 
    * @return
    *       path
    */
   public String getAttributeInitialConditionPathCell()
   {
      return helper.getAttribute("cellPath");
   }

   /**
    * Get the delimiter for the initial condition file for cells
    * 
    * @return
    *       delimiter
    */
   public String getAttributeInitialConditionDelimiterCell()
   {
      return helper.getAttribute("cellDelimiter");
   }

   /**
    * Get the path to the initial condition file for bounds
    * 
    * @return
    *       path
    */
   public String getAttributeInitialConditionPathBound()
   {
      return helper.getAttribute("boundPath");
   }

   /**
    * Get the delimiter for the initial condition file for bounds
    * 
    * @return
    *       delimiter
    */
   public String getAttributeInitialConditionDelimiterBound()
   {
      return helper.getAttribute("boundDelimiter");
   }
   
}
