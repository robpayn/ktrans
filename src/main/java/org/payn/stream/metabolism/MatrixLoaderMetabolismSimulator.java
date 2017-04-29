package org.payn.stream.metabolism;

import java.util.ArrayList;
import java.util.Iterator;

import org.payn.chsm.io.xmltools.ElementResource;
import org.payn.chsm.resources.Resource;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.water.ResourceWater;
import org.payn.stream.MatrixLoaderStreamSimulator;

/**
 * A simplified matrix loader specifically for stream metabolism models
 * 
 * @author robpayn
 *
 */
public class MatrixLoaderMetabolismSimulator extends MatrixLoaderStreamSimulator {

   @Override
   protected ArrayList<Resource> loadResources() throws Exception 
   {
      Iterator<ElementResource> resourceIter = documentConfig.getResourceIterator();
      if (resourceIter == null)
      {
         ArrayList<Resource> list = new ArrayList<Resource>();         
         Resource resource = new ResourceWater();
         resource.initialize("water");
         list.add(resource);
         resource = new ResourceSolute();
         resource.initialize("oxygen");
         list.add(resource);
         return list;
      }
      else
      {
         return super.loadResources();
      }
   }
      
}
