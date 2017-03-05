package org.payn.stream.otis;

import java.io.File;
import java.util.HashMap;

import org.payn.chsm.io.xml.ElementHelper;
import org.payn.stream.MetaInputXMLStream;

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
       * Construct a new instance for the provided solute name
       * based on information in the provided
       * element helper
       * 
       * @param helper
       * @param soluteName
       */
      public ElementSolute(ElementHelper helper, String soluteName) 
      {
         super(helper.getFirstChildElement(soluteName));
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
            elementInterp = helper.getFirstChildElementHelper("interpolate");
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
            elementInject = helper.getFirstChildElementHelper("inject");
         }
         return elementInject;
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
      elementSoluteMap = new HashMap<String, ElementSolute>();
   }

   /**
    * Get the element for the provided solute name
    * 
    * @param soluteName
    * @return
    */
   private ElementSolute getElementSolute(String soluteName) 
   {
      ElementSolute elementSolute = elementSoluteMap.get(soluteName);
      if (elementSolute == null)
      {
         elementSolute = new ElementSolute(helper, soluteName);
         elementSoluteMap.put(soluteName, elementSolute);
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

}
