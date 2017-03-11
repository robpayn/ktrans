package org.payn.stream;

import java.io.File;
import org.payn.neoch.HolonMatrix;
import org.payn.simulation.OutputProcessorFactory;
import org.payn.simulation.OutputProcessorFactoryAbstract;
import org.payn.stream.metabolism.InputProcessorXMLMetabolismBuilder;
import org.payn.stream.metabolism.MetaInputXMLMetabolism;
import org.payn.stream.otis.InputProcessorXMLHyperOTIS;
import org.payn.stream.otis.MetaInputXMLHyperOTIS;

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
   public SimulatorStream(String[] args, File workingDir) throws Exception
   {
      super(args, workingDir);
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
                  new MetaInputXMLMetabolism(workingDir, configPath, "streambuilder"), 
                  getSimulator()
                  );
         }

         @Override
         public void addHyperOTISBuilderInputProcessor(File workingDir,
               String configPath) throws Exception 
         {
            new InputProcessorXMLHyperOTIS(
                  new MetaInputXMLHyperOTIS(workingDir, configPath, "streambuilder"), 
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
