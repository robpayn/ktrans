package org.payn.stream;

import java.io.File;

import org.payn.simulation.InputProcessorFactoryAbstract;

/**
 * Abstract implementation of a input processor factory for a stream simulator
 * 
 * @author robpayn
 *
 */
public abstract class InputProcessorFactoryStreamSimulator extends InputProcessorFactoryAbstract {

   /**
    * Construct a new instance for the provdied simulator
    * 
    * @param simulator
    *       simulator object
    */
   public InputProcessorFactoryStreamSimulator(StreamSimulator simulator) 
   {
      super(simulator);
   }

   @Override
   public StreamSimulator getSimulator() 
   {
      return (StreamSimulator)simulator;
   }

   /**
    * Add a stream builder input processor
    * 
    * @param workingDir
    * @param configPath
    * @throws Exception
    */
   public abstract void addMetabolismBuilderInputProcessor(File workingDir, String configPath)
         throws Exception;

}
