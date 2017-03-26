package org.payn.stream.otis;

import java.io.File;
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

   /**
    * Determine if an inject element exists in the names solute
    * element
    * 
    * @param soluteName 
    *       name of the solute tag to check for inject tag
    * 
    * @return
    *       true if element exists, false otherwise
    */
   public boolean isUpstreamInject(String soluteName) 
   {
      ElementSolute helper = getElementSolute(soluteName);
      if (helper == null)
      {
         return false;
      }
      else
      {
         return helper.isUpstreamInject();
      }
   }

}
