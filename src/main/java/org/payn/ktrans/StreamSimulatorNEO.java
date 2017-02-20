package org.payn.ktrans;

import java.io.File;
import java.util.HashMap;

import org.payn.neoch.HolonMatrix;
import org.payn.neoch.MatrixLoader;
import org.payn.simulation.InputProcessorFactory;
import org.payn.simulation.OutputProcessorFactory;
import org.payn.simulation.OutputProcessorFactoryAbstract;
import org.payn.simulation.SimulatorAbstract;

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
         HashMap<String,String>argMap = MatrixLoader.createArgMap(args);
         File workingDir = new File(System.getProperty("user.dir"));
         
         StreamSimulatorNEO simulator = new StreamSimulatorNEO(argMap, workingDir);
         
         // Check for configuration file in file system
         if (!argMap.containsKey("config"))
         {
            throw new Exception(
                  "Must provide an argument for configuration file relative to working directory " +
                        "(e.g. 'config=./config/config.xml')"
                  );
         }

         simulator.getInputProcessorFactory().addBuilderInputProcessor(workingDir, argMap.get("config"));
         simulator.execute();
      } 
      catch (Exception e) 
      {
         e.printStackTrace();
      }
   }

   /**
    * Command line arguments (java) for simulator
    */
   private HashMap<String,String> argMap;
   
   /**
    * Working directory for the simulator
    */
   private File workingDir;

   /**
    * NEO matrix
    */
   private HolonMatrix matrix;
   
   /**
    * Constructor based on an argument map and working directory
    * 
    * @param argMap
    *       map of command line arguments ("=" delimiter expected for key/value pairs
    * @param workingDir
    *       working directory
    */
   public StreamSimulatorNEO(HashMap<String,String> argMap, File workingDir)
   {
      this.argMap = argMap;
      this.workingDir = workingDir;
   }

   /**
    * Load the matrix
    * 
    * @throws Exception
    *       if error in loading the matrix
    */
   public void loadMatrix() throws Exception 
   {
      matrix = StreamSimulatorMatrixLoader.loadStreamSimulatorModel(
            argMap, 
            workingDir
            );
      matrix.getController().initializeController();
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
