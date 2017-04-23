package org.payn.stream;

import java.io.File;
import org.payn.neoch.HolonMatrix;
import org.payn.simulation.OutputProcessorFactory;
import org.payn.simulation.OutputProcessorFactoryAbstract;
import org.payn.stream.metabolism.InputProcessorXMLMetabolismBuilder;
import org.payn.stream.metabolism.MetaInputXMLMetabolism;
import org.payn.stream.uptake.InputProcessorXMLHyperUptake;
import org.payn.stream.uptake.MetaInputXMLHyperUptake;

/**
 * Implementation of a simulator for stream simulations
 * 
 * @author robpayn
 *
 */
public class SimulatorStream extends SimulatorNEOCH {
   
   /**
    * Construct a new instance based on the provided command line arguments and working directory
    * 
    * @param args
    * @param workingDir
    * @throws Exception 
    */
   public SimulatorStream(File workingDir, String[] args) throws Exception
   {
      super(workingDir, args);
   }

   @Override
   protected InputProcessorFactoryStreamSimulator createInputProcessorFactory() 
   {
      return new InputProcessorFactoryStreamSimulator(this) {
         
         @Override
         public void addMetabolismBuilderInputProcessor(File workingDir, String configPath)
               throws Exception 
         {
            new InputProcessorXMLMetabolismBuilder(
                  new MetaInputXMLMetabolism(workingDir, configPath, "stream"), 
                  getSimulator()
                  );
         }

         @Override
         public void addHyperUptakeBuilderInputProcessor(File workingDir,
               String configPath) throws Exception 
         {
            new InputProcessorXMLHyperUptake(
                  new MetaInputXMLHyperUptake(workingDir, configPath, "stream"), 
                  getSimulator()
                  );
         }
         
      };
   }

   @Override
   public InputProcessorFactoryStreamSimulator getInputProcessorFactory()
   {
       return (InputProcessorFactoryStreamSimulator)inputProcessorFactory;
   }

   @Override
   protected OutputProcessorFactory createOutputProcessorFactory() 
   {
      return new OutputProcessorFactoryAbstract(this) {
      };
   }

   @Override
   public HolonMatrix createMatrix() throws Exception 
   {
      return MatrixLoaderStreamSimulator.initializeStreamSimulator(
            argMap, 
            workingDir
            );
   }

}
