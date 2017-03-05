package org.payn.stream;

import java.io.File;
import org.payn.neoch.HolonMatrix;
import org.payn.simulation.OutputProcessorFactory;
import org.payn.simulation.OutputProcessorFactoryAbstract;
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
    * Entry point for stream simulations
    * 
    * @param args
    *       command line arguments
    */
   public static void main(String[] args)
   {
      try 
      {
         File workingDir = new File(System.getProperty("user.dir"));
         SimulatorStream simulator = new SimulatorStream(args, workingDir);
         
         // Check for configuration file in file system
         if (!simulator.argMap.containsKey("config"))
         {
            throw new Exception(
                  "Must provide an argument for configuration file relative to working directory " +
                        "(e.g. 'config=./config/config.xml')"
                  );
         }

         simulator.getInputProcessorFactory().addMetabolismBuilderInputProcessor(
               workingDir, simulator.argMap.get("config")
               );
         simulator.execute();
      } 
      catch (Exception e) 
      {
         e.printStackTrace();
      }
   }

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
