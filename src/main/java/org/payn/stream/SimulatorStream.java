package org.payn.stream;

import java.io.File;

import org.payn.simulation.OutputProcessorFactory;
import org.payn.simulation.OutputProcessorFactoryAbstract;
import org.payn.simulation.neoch.SimulatorNEOCH;
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
    * @param loader 
    *       loader to use to create the matrix
    * @throws Exception 
    */
   public SimulatorStream(
         File workingDir, 
         String[] args, 
         MatrixLoaderStreamSimulator loader
         ) throws Exception
   {
      super(workingDir, args, loader);
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
                  new MetaInputXMLMetabolism(workingDir, configPath, "streamsimulator"), 
                  getSimulator()
                  );
         }

         @Override
         public void addHyperUptakeBuilderInputProcessor(File workingDir,
               String configPath) throws Exception 
         {
            new InputProcessorXMLHyperUptake(
                  new MetaInputXMLHyperUptake(workingDir, configPath, "streamsimulator"), 
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

}
