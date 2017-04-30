package org.payn.stream.uptake;

import java.util.ArrayList;
import java.util.Iterator;

import org.payn.chsm.io.xmltools.ElementResource;
import org.payn.chsm.resources.Resource;
import org.payn.resources.solute.concentration.ResourceSoluteConcentration;
import org.payn.stream.MatrixLoaderStreamSimulator;

/**
 * A specific matrix loader for a simualtor for stream solute uptake
 * 
 * @author robpayn
 *
 */
public class MatrixLoaderUptakeSimulator extends MatrixLoaderStreamSimulator {

   @Override
   protected ArrayList<Resource> loadResources() throws Exception 
   {
      Iterator<ElementResource> resourceIter = documentConfig.getResourceIterator();
      if (resourceIter == null)
      {
         ArrayList<Resource> list = new ArrayList<Resource>();         
         Resource resource = new ResourceSoluteConcentration();
         resource.initialize("conserve");
         list.add(resource);
         resource = new ResourceSoluteConcentration();
         resource.initialize("active");
         list.add(resource);
         return list;
      }
      else
      {
         return super.loadResources();
      }
   }

}
