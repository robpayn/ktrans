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
   public InputProcessorFactoryStreamSimulator(SimulatorStream simulator) 
   {
      super(simulator);
   }

   @Override
   public SimulatorStream getSimulator() 
   {
      return (SimulatorStream)simulator;
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

   /**
    * Add a stream builder input processor
    * 
    * @param workingDir
    * @param configPath
    * @throws Exception
    */
   public abstract void addHyperOTISBuilderInputProcessor(File workingDir, String configPath)
         throws Exception;

}
