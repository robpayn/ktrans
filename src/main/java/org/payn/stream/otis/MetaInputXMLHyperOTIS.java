package org.payn.stream.otis;

import java.io.File;
import java.util.HashMap;

import org.payn.chsm.io.xmltools.ElementHelper;
import org.payn.stream.MetaInputXMLStream;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Meta input for building a stream solute transport model based 
 * on direct calcualtions of concentration in a one-dimensional
 * construct
 * 
 * @author robpayn
 *
 */
public class MetaInputXMLHyperOTIS extends MetaInputXMLStream {
   
   /**
    * XML element for solute configuration information
    * 
    * @author robpayn
    *
    */
   private class ElementSolute extends ElementHelper {

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

   }
   
   /**
    * Hash map for looking up the elements for each simulated solute
    */
   HashMap<String, ElementSolute> elementSoluteMap;

   /**
    * Construct a new instance of the meta-input
    * 
    * @param workingDir
    * @param path
    * @param elementName
    * @throws Exception
    */
   public MetaInputXMLHyperOTIS(File workingDir, String path,
         String elementName) throws Exception 
   {
      super(workingDir, path, elementName);
   }

   /**
    * Get the element for the provided solute name
    * 
    * @param soluteName
    * @return
    */
   private ElementSolute getElementSolute(String soluteName) 
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
    * Get the background concentration attribute
    * 
    * @param soluteName 
    * 
    * @return
    *        Double object
    */
   public Double getAttributeBkgConc(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeBkgConc();
      }
   }

   /**
    * Get the path to the interpolation file
    * 
    * @param soluteName 
    * 
    * @return
    *        String object
    */
   public String getAttributeConcBoundFile(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeConcBoundFile();
      }
   }

   /**
    * Get the type of interpolation
    * 
    * @param soluteName 
    * 
    * @return
    *        String object
    */
   public String getAttributeInterpolationType(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeInterpolationType();
      }
   }

   /**
    * Get the delimiter attribute
    * 
    * @param soluteName 
    * 
    * @return
    *        String object
    */
   public String getAttributeDelimiter(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeDelimiter();
      }
   }

   /**
    * Get the injection mass attribute
    * 
    * @param soluteName 
    * 
    * @return
    *        Double object
    */
   public Double getAttributeInjectMass(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeInjectMass();
      }
   }

   /**
    * Get the injection duration attribute (number of iterations)
    * 
    * @param soluteName 
    * 
    * @return
    *        Long object
    */
   public Long getAttributeInjectDuration(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeInjectDuration();
      }
   }

   /**
    * Get the injection start iteration attribute
    * 
    * @param soluteName 
    * 
    * @return
    *        Long object
    */
   public Long getAttributeInjectStartInterval(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeInjectStartInterval();
      }
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
    * Get the maximum uptake parameter of a hyperbolic function
    * 
    * @param soluteName 
    * 
    * @return
    *        Double object
    */
   public Double getAttributeUptakeMax(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeUptakeMax();
      }
   }

   /**
    * Get the attribute for the half saturation concentration
    * in the hyperbolic function
    * 
    * @param soluteName 
    * 
    * @return
    *        Double object
    */
   public Double getAttributeConcHalfSat(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return null;
      }
      else
      {
         return helper.getAttributeConcHalfSat();
      }
   }

}
