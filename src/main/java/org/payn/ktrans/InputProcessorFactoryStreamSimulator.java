package org.payn.ktrans;

import java.io.File;

import org.payn.simulation.InputProcessorFactoryAbstract;

/**
 * Abstract implementation of an input processor factory for a stream simulator
 * 
 * @author robpayn
 *
 */
public abstract class InputProcessorFactoryStreamSimulator extends InputProcessorFactoryAbstract {

   /**
    * Construct a new instance controlled by the provided stream simulator
    * 
    * @param streamSimulatorNEO
    *       simulator
    */
   public InputProcessorFactoryStreamSimulator(
         StreamSimulatorNEO streamSimulatorNEO) 
   {
      super(streamSimulatorNEO);
   }

   @Override
   public StreamSimulatorNEO getSimulator() 
   {
      return (StreamSimulatorNEO)simulator;
   }

   /**
    * Add a stream builder input processor
    * 
    * @param workingDir
    * @param configPath
    * @throws Exception
    */
   public abstract void addBuilderInputProcessor(File workingDir, String configPath)
         throws Exception;

}
