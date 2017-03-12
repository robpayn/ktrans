package org.payn.ktrans;

import java.io.File;
import org.payn.neoch.HolonMatrix;
import org.payn.simulation.InputProcessorFactory;
import org.payn.simulation.OutputProcessorFactory;
import org.payn.simulation.OutputProcessorFactoryAbstract;
import org.payn.simulation.SimulatorAbstract;
import org.payn.stream.MatrixLoaderStreamSimulator;

/**
 * A solute transport stream simulator using the NEO framework
 * 
 * @author v78h241
 *
 */
public class StreamSimulatorNEO extends SimulatorAbstract {
   
   /**
    * Entry point
    * 
    * @param args
    *       array of command line arguments
    */
   public static void main(String[] args)
   {
      try 
      {
         File workingDir = new File(System.getProperty("user.dir"));
         StreamSimulatorNEO simulator = new StreamSimulatorNEO(args, workingDir);
         
         // Check for configuration file in file system
         if (!simulator.argMap.containsKey("config"))
         {
            throw new Exception(
                  "Must provide an argument for configuration file relative to working directory " +
                        "(e.g. 'config=./config/config.xml')"
                  );
         }

         simulator.getInputProcessorFactory().addBuilderInputProcessor(workingDir, simulator.argMap.get("config"));
         simulator.execute();
      } 
      catch (Exception e) 
      {
         e.printStackTrace();
      }
   }

   /**
    * NEO matrix
    */
   private HolonMatrix matrix;
   
   /**
    * Constructor based on an argument map and working directory
    * 
    * @param args
    *       array of command line arguments
    * @param workingDir
    *       working directory
    * @throws Exception 
    */
   public StreamSimulatorNEO(String[] args, File workingDir) throws Exception
   {
      super(args, workingDir);
   }

   /**
    * Load the matrix
    * 
    * @throws Exception
    *       if error in loading the matrix
    */
   public void initializeModel() throws Exception 
   {
      matrix = MatrixLoaderStreamSimulator.initializeStreamSimulator(
            argMap, 
            workingDir
            );
   }

   @Override
   protected void runModel() throws Exception 
   {
      matrix.getController().executeController();
   }

   @Override
   protected InputProcessorFactory createInputProcessorFactory() 
   {
      return new InputProcessorFactoryStreamSimulator(this) {
         
         @Override
         public void addBuilderInputProcessor(File workingDir, String configPath) throws Exception 
         {
            new StreamBuilderNEOInputProcessorXML(
                  new StreamBuilderMetaInputXML(workingDir, configPath, "streambuilder"), 
                  getSimulator()
                  );
         }
         
      };
   }

   @Override
   protected OutputProcessorFactory createOutputProcessorFactory() 
   {
      return new OutputProcessorFactoryAbstract(this) {
      };
   }

   @Override
   public InputProcessorFactoryStreamSimulator getInputProcessorFactory()
   {
       return (InputProcessorFactoryStreamSimulator)inputProcessorFactory;
   }

}
