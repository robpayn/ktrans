package org.payn.stream;

import java.io.File;
import java.util.HashMap;

import org.payn.chsm.io.xmltools.ElementHelper;
import org.payn.simulation.neoch.MetaInputXMLNEOCH;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Abstract meta input for a NEOCH stream model
 * 
 * @author robpayn
 *
 */
public abstract class MetaInputXMLStream extends MetaInputXMLNEOCH {
   
   /**
    * XML element for solute configuration information
    * 
    * @author robpayn
    *
    */
   protected class ElementSolute extends ElementHelper {

      /**
       * Element with information about interpolation
       */
      private ElementHelper elementInterp;
      
      /**
       * Element with information for injection boundary
       */
      private ElementHelper elementInject;

      /**
       * Element with information about a hyperbolic function
       */
      private ElementHelper elementHyperbolic;

      /**
       * Element for the upstream boundary configuration
       */
      private ElementHelper elementUpstreamBound;

      /**
       * XML element with information about air-water gas exchange
       */
      private ElementHelper elementAWExchange;

      /**
       * XML element helper with information about photosynthesis effects
       * on oxygen
       */
      private ElementHelper elementPhotosynthesis;

      /**
       * XML element helper with information about respiration effects
       * on oxygen
       */
      private ElementHelper elementRespiration;

      /**
       * Construct a new instance for the provided solute name
       * based on information in the provided
       * element helper
       * 
       * @param element
       */
      public ElementSolute(Element element) 
      {
         super(element);
      }

      /**
       * Get the interpolation element
       * 
       * @return
       *        interpolation element
       */
      private ElementHelper getElementInterp() 
      {
         if (elementInterp == null)
         {
            elementInterp = getFirstChildElementHelper("interpolate");
         }
         return elementInterp;
      }
      
      /**
       * Get the injection element
       * 
       * @return
       *        reference to the injection element, or null if
       *        there is no injection element
       */
      private ElementHelper getElementInject() 
      {
         if (elementInject == null)
         {
            elementInject = getFirstChildElementHelper("inject");
         }
         return elementInject;
      }

      /**
       * Get the element with information about a hyperbolic function
       * 
       * @return
       *        hyperbolic element
       */
      private ElementHelper getHyperbolicElement() 
      {
         if (elementHyperbolic == null)
         {
            elementHyperbolic = getFirstChildElementHelper("hyperbolic");
         }
         return elementHyperbolic;
      }

      /**
       * Get the XML element with information about air-water gas exchange
       * 
       * @return
       *        air water gas exchange element
       */
      private ElementHelper getElementAWExchange() 
      {
         if (elementAWExchange == null)
         {
            elementAWExchange = getFirstChildElementHelper("awexchange");
         }
         return elementAWExchange;
      }

      /**
       * Get the element helper with information about photosynthesis
       * 
       * @return
       *        element helper
       */
      private ElementHelper getElementPhotosynthesis() 
      {
         if (elementPhotosynthesis == null)
         {
            elementPhotosynthesis = getFirstChildElementHelper("photosynthesis");
         }
         return elementPhotosynthesis;
      }

      /**
       * Get the element helper with information about respiration
       * 
       * @return
       *        respiration element
       */
      private ElementHelper getElementRespiration() 
      {
         if (elementRespiration == null)
         {
            elementRespiration = getFirstChildElementHelper("respiration");
         }
         return elementRespiration;
      }

      /**
       * Get the background concentration attribute
       * 
       * @return
       *        Double object
       */
      public Double getAttributeBkgConc() 
      {
         return getAttributeDouble("bkgConc");
      }

      /**
       * Get the path to the interpolation file
       * 
       * @return
       *        String object
       */
      public String getAttributeConcBoundFile() 
      {
         ElementHelper helper = getElementInterp();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeString("path");
         }
      }

      /**
       * Get the type of interpolation
       * 
       * @return
       *        String object
       */
      public String getAttributeInterpolationType() 
      {
         ElementHelper helper = getElementInterp();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeString("type");
         }
      }

      /**
       * Get the delimiter attribute
       * 
       * @return
       *        String object
       */
      public String getAttributeDelimiter() 
      {
         ElementHelper helper = getElementInterp();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeString("delimiter");
         }
      }

      /**
       * Get the injection mass attribute
       * 
       * @return
       *        Double object
       */
      public Double getAttributeInjectMass() 
      {
         ElementHelper helper = getElementInject();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("soluteMass");
         }
      }

      /**
       * Get the injection duration attribute (number of iterations)
       * 
       * @return
       *        Long object
       */
      public Long getAttributeInjectDuration() 
      {
         ElementHelper helper = getElementInject();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeLong("durationIterations");
         }
      }

      /**
       * Get the injection start iteration attribute
       * 
       * @return
       *        Long object
       */
      public Long getAttributeInjectStartInterval() 
      {
         ElementHelper helper = getElementInject();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeLong("startIteration");
         }
      }

      /**
       * Get the maximum uptake parameter of a hyperbolic function
       * 
       * @return
       *        Double object
       */
      public Double getAttributeUptakeMax() 
      {
         ElementHelper helper = getHyperbolicElement();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("uptakeMax");
         }
      }

      /**
       * Get the attribute for the half saturation concentration
       * in the hyperbolic function
       * 
       * @return
       *        Double object
       */
      public Double getAttributeConcHalfSat() 
      {
         ElementHelper helper = getHyperbolicElement();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("concHalfSat");
         }
      }

      /**
       * Determine if an inject element exists
       * 
       * @return
       *        true if element exists, false otherwise
       */
      public boolean isUpstreamInject() 
      {
         return !(getElementInject() == null);
      }

      /**
       * Get the attribute for the path to the upstream interpolation file
       * 
       * @return
       *        path
       */
      public String getAttributeUpstreamPath() 
      {
         return getElementUpstreamBound().getAttribute("upstreamPath");
      }

      /**
       * Get the element with the upstream bound configuration
       * 
       * @return
       *        element helper
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
       * Get the element with the upstream interpolation type
       * 
       * @return
       *        interpolation type
       */
      public String getAttributeUpstreamInterpType() 
      {
         return getElementUpstreamBound().getAttribute("upstreamInterpType");
      }

      /**
       * Get the attribute with the delimiter for the upstream interpolation file
       * 
       * @return
       *        delimiter
       */
      public String getAttributeUpstreamDelimiter() 
      {
         return getElementUpstreamBound().getAttribute("upstreamDelimiter");
      }

      /**
       * Get the attribute with the initial concentration
       * 
       * @return
       *        initial concentration
       */
      public Double getAttributeInitialConc() 
      {
         return getAttributeDouble("initialConc");
      }

      /**
       * Get the attribute with the air-water gas exchange velocity
       * at a Schmidt number of 600
       * 
       * @return
       *        air-water gas exchange velocity
       */
      public Double getAttributeK600() 
      {
         return getElementAWExchange().getAttributeDouble("k600");
      }

      /**
       * Get the attribute for the interpolation type for PAR
       * 
       * @return
       *        interpolation type
       */
      public String getAttributePARType() 
      {
         return getElementPhotosynthesis().getAttributeString("parType");
      }

      /**
       * Get the attribute for the delimiter for the interpolation table for PAR
       * 
       * @return
       *        delimiter
       */
      public String getAttributePARDelimiter() 
      {
         return getElementPhotosynthesis().getAttributeString("parDelimiter");
      }

      /**
       * Get the attribute for the path to the interpolation file for PAR
       * 
       * @return
       *        path to interpolation file
       */
      public String getAttributePARPath() 
      {
         return getElementPhotosynthesis().getAttributeString("parPath");
      }

      /**
       * Get the attribute for the primary production to PAR ratio
       * 
       * @return
       *        ratio of primary production to PAR
       */
      public Double getAttributePToPARRatio() 
      {
         return getElementPhotosynthesis().getAttributeDouble("pToPARRatio");
      }

      /**
       * Get the attribute for the rate of respiration effect
       * 
       * @param soluteName
       *       name of the solute
       * @return
       *       respiration rate
       */
      public Double getAttributeRespiration() 
      {
         return getElementRespiration().getAttributeDouble("respiration");
      }

   }
   
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
       * Get the element with information about dispersion
       * 
       * @return
       *    dispersion element
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
       * Get the exponent for velocity in the Chezey model
       * 
       * @return
       *        exponent for velocity
       */
      public Double getAttributeChezeyExpVel() 
      {
         ElementHelper helper = getElementFriction();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("chezeyExponentVelocity");
         }
      }

      /**
       * Get the exponent for radius in the Chezey model
       * 
       * @return
       *        exponent for radius
       */
      public Double getAttributeChezeyExpRad() 
      {
         ElementHelper helper = getElementFriction();
         if (helper == null)
         {
            return null;
         }
         else
         {
            return helper.getAttributeDouble("chezeyExponentRadius");
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
    * Hash map for looking up the elements for each simulated solute
    */
   HashMap<String, ElementSolute> elementSoluteMap;

   /**
    * Element helper for the temperature element
    */
   private ElementHelper elementTemperature;

   /**
    * Element helper for the atmosphere element
    */
   private ElementHelper elementAtmosphere;

   /**
    * Element helper for the initial conditions element
    */
   private ElementHelper elementInitialConditions;

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
    *       model structure element
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
    * Get the XML element for with information about the atmosphere
    * 
    * @return
    *       atmosphere element
    */
   private ElementHelper getElementAtmosphere() 
   {
      if (elementAtmosphere == null)
      {
         elementAtmosphere = helper.getFirstChildElementHelper("atmosphere");
      }
      return elementAtmosphere;
   }

   /**
    * Get the geometry element
    * 
    * @return
    *       geometry element helper object
    */
   private ElementGeometry getElementGeometry()
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
    * Get the element helper for the temperature element
    * 
    * @return
    *       temperature element
    */
   private ElementHelper getElementTemperature() 
   {
      if (elementTemperature == null)
      {
         elementTemperature = helper.getFirstChildElementHelper("temperature");
      }
      return elementTemperature;
   }
   
   /**
    * Get the element helper for the initial conditions element
    * 
    * @return
    *       intial conditions element helper
    */
   private ElementHelper getElementInitialConditions()
   {
      if (elementInitialConditions == null)
      {
         elementInitialConditions = helper.getFirstChildElementHelper("initialConditions");
      }
      return elementInitialConditions;
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
    * Determine if Chezey exponents are configured
    * 
    * @return
    *       true if configured, false otherwise
    */
   public boolean isChezyExpConfigured() 
   {
      return (getAttributeChezeyExpVel() != null) 
            && (getAttributeChezeyExpRad() != null);
   }

   /**
    * Get the exponent for velocity in the Chezey model
    * 
    * @return
    *        exponent for velocity
    */
   public Double getAttributeChezeyExpVel() 
   {
      ElementFlow helper = getElementFlow();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeChezeyExpVel();
      }
   }

   /**
    * Get the exponent for radius in the Chezey model
    * 
    * @return
    *        exponent for radius
    */
   public Double getAttributeChezeyExpRad() 
   {
      ElementFlow helper = getElementFlow();
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeChezeyExpRad();
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
   public String getAttributeUpstreamFlowInterpType()
   {
      return getElementFlow().getAttributeUpstreamInterpType();
   }

   /**
    * Get the path to the interpolation file for upstream concentration
    * 
    * @param soluteName
    * @return
    *       path to file
    */
   public String getAttributeUpstreamConcPath(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributeUpstreamPath();
   }
   
   /**
    * Get the interpolation type for the file with upstream concentration
    * 
    * @param soluteName
    * @return
    *       interpolation type
    */
   public String getAttributeUpstreamConcInterpType(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributeUpstreamInterpType();
   }

   /**
    * Get the column delimiter for the upstream concentration file
    * 
    * @param soluteName
    * @return
    *       delimiter
    */
   public String getAttributeUpstreamConcDelimiter(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributeUpstreamDelimiter();
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
      return getElementInitialConditions().isActive();
   }
   
   /**
    * Get the path to the initial condition file for cells
    * 
    * @return
    *       path
    */
   public String getAttributeInitialConditionPathCell()
   {
      return getElementInitialConditions().getAttribute("cellPath");
   }

   /**
    * Get the delimiter for the initial condition file for cells
    * 
    * @return
    *       delimiter
    */
   public String getAttributeInitialConditionDelimiterCell()
   {
      return getElementInitialConditions().getAttribute("cellDelimiter");
   }

   /**
    * Get the path to the initial condition file for bounds
    * 
    * @return
    *       path
    */
   public String getAttributeInitialConditionPathBound()
   {
      return getElementInitialConditions().getAttribute("boundPath");
   }

   /**
    * Get the delimiter for the initial condition file for bounds
    * 
    * @return
    *       delimiter
    */
   public String getAttributeInitialConditionDelimiterBound()
   {
      return getElementInitialConditions().getAttribute("boundDelimiter");
   }
   
   /**
    * Determine if the provided solute is configured
    * 
    * @param soluteName
    * @return
    *       true if configured, false otherwise
    */
   public boolean isSoluteConfigured(String soluteName) 
   {
      ElementSolute elementSolute = getElementSolute(soluteName);
      if (elementSolute == null)
      {
         return false;
      }
      else
      {
         return elementSolute.isActive();
      }
   }

   /**
    * Get the element for the provided solute name
    * 
    * @param soluteName
    * @return
    *       solute element
    */
   protected ElementSolute getElementSolute(String soluteName) 
   {
      if (elementSoluteMap == null)
      {
         elementSoluteMap = new HashMap<String, ElementSolute>();
         NodeList nodes = helper.getElement().getElementsByTagName("solute");
         for (int nodeCount = 0; nodeCount < nodes.getLength(); nodeCount++)
         {
            ElementSolute elementSolute = 
                  new ElementSolute((Element)nodes.item(nodeCount));
            elementSoluteMap.put(elementSolute.getName(), elementSolute);
         }
      }
      ElementSolute elementSolute = elementSoluteMap.get(soluteName);
      if (elementSolute == null)
      {
         return null;
      }
      return elementSolute;
   }

   /**
    * Get the attribute with the initial concentration
    * 
    * @param soluteName
    * @return
    *       initial concentration
    */
   public Double getAttributeInitialConc(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributeInitialConc();
   }

   /**
    * Get the attribute for the path to the interpolation file
    * for upstream temperature
    * 
    * @return
    *       path
    */
   public String getAttributeUpstreamTempPath() 
   {
      return getElementTemperature().getAttributeString("upstreamPath");
   }

   /**
    * Get the attribute for the path to the interpolation file
    * for downstream temperature
    * 
    * @return
    *       path
    */
   public String getAttributeDownstreamTempPath() 
   {
      return getElementTemperature().getAttributeString("downstreamPath");
   }

   /**
    * Get the interpolation type for temperature interpolation
    * 
    * @return
    *       interpolation type
    */
   public String getAttributeUpstreamTempType() 
   {
      return getElementTemperature().getAttributeString("interpType");
   }

   /**
    * Get the column delimiter for temperature interpolation files
    * 
    * @return
    *       delimiter
    */
   public String getAttributeUpstreamTempDelimiter() 
   {
      return getElementTemperature().getAttributeString("delimiter");
   }

   /**
    * Get the attribute with the air-water gas exchange velocity
    * at a Schmidt number of 600
    * 
    * @param soluteName 
    *       name of the solute
    * @return
    *       air-water gas exchange velocity
    */
   public Double getAttributeK600(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributeK600();
   }

   /**
    * Get the attribute with the atmospheric pressure
    * 
    * @return
    *       atmospheric pressure
    */
   public Double getAttributeAirPressure() 
   {
      return getElementAtmosphere().getAttributeDouble("airPressure");
   }

   /**
    * Get the attribute for the interpolation type for PAR
    * 
    * @param soluteName 
    *       name of the solute
    * @return
    *       interpolation type
    */
   public String getAttributePARType(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributePARType();
   }

   /**
    * Get the attribute for the delimiter for the interpolation table for PAR
    * 
    * @param soluteName 
    *       name of the solute
    * @return
    *        delimiter
    */
   public String getAttributePARDelimiter(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributePARDelimiter();
   }

   /**
    * Get the attribute for the path to the interpolation file for PAR
    * 
    * @param soluteName 
    *       name of the solute
    * @return
    *        path to interpolation file
    */
   public String getAttributePARPath(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributePARPath();
   }

   /**
    * Get the attribute for the primary production to PAR ratio
    * 
    * @param soluteName 
    *       name of the solute
    * @return
    *       ratio of primary production to PAR
    */
   public Double getAttributePToPARRatio(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributePToPARRatio();
   }

   /**
    * Get the attribute for the rate of respiration effect
    * 
    * @param soluteName
    *       name of the solute
    * @return
    *       respiration rate
    */
   public Double getAttributeRespiration(String soluteName) 
   {
      return getElementSolute(soluteName).getAttributeRespiration();
   }

}
