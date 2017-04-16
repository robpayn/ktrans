package org.payn.stream;

import java.io.File;

import org.payn.chsm.Behavior;
import org.payn.chsm.Resource;
import org.payn.chsm.io.xmltools.ElementBehavior;
import org.payn.chsm.resources.time.BehaviorTime;
import org.payn.chsm.resources.time.ResourceTime;
import org.payn.neoch.io.xmltools.DocumentHolonMatrix;
import org.payn.simulation.InputProcessorAbstract;

/**
 * Abstract implementation of an input processor that creates a
 * NEOCH model from meta input
 * 
 * @author robpayn
 *
 * @param <MIT>
 *      meta input type
 * @param <ST> 
 *      simulator type
 */
public abstract class InputProcessorXMLNEOCHBuilder<MIT extends MetaInputXMLNEOCH, ST extends SimulatorNEOCH> 
      extends InputProcessorAbstract<MIT, ST> {

   /**
    * Holon XML document
    */
   protected DocumentHolonMatrix documentHolon;

   /**
    * Construct a new instance with the given meta input and simulator
    * 
    * @param metaInput
    * @param sim
    */
   public InputProcessorXMLNEOCHBuilder(MIT metaInput, ST sim) 
   {
      super(metaInput, sim);
   }

   @Override
   public void execute() throws Exception 
   {
      if (!metaInput.isActive())
      {
         System.out.println("Builder is inactive, attempting to run existing model...");
      }
      else
      {
         System.out.println("Building the NEOCH files...");

         File holonFile = metaInput.getHolonFile();
         documentHolon = new DocumentHolonMatrix(holonFile.getName());
         
         Resource resourceTime = new ResourceTime();
         resourceTime.initialize("time");
         Behavior behaviorTime = resourceTime.getBehavior(
               ResourceTime.BEHAVIOR_TIME
               );
         ElementBehavior elementTime =
               documentHolon.getRootHolonElement().createBehaviorElement(behaviorTime);
         elementTime.createInitValueElement(
               BehaviorTime.ITERATION_INTERVAL,
               metaInput.getAttributeTimeInterval().toString(), 
               null
               );
         elementTime.createInitValueElement(
               BehaviorTime.LAST_ITERATION,
               metaInput.getAttributeLastIteration().toString(), 
               null
               );
         
         configureResources();
         
         configureModel();
         
         // Write the model input files
         holonFile.getParentFile().mkdirs();
         documentHolon.write(holonFile.getParentFile());
      }
      simulator.initializeModel();
   }

   /**
    * Configure the model
    * 
    * @throws Exception
    *       if error in model configuration
    */
   protected abstract void configureModel() throws Exception;
   
   /**
    * Configure the resources used in the model
    * 
    * @throws Exception
    *       if error in loading resources
    */
   protected abstract void configureResources() throws Exception;

}
