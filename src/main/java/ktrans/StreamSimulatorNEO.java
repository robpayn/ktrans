package ktrans;

import java.io.File;
import java.util.HashMap;

import edu.montana.cerg.simmanager.Simulator;
import edu.montana.cerg.simmanager.interfaces.IInputProcessorFactory;
import edu.montana.cerg.simmanager.interfaces.IOutputProcessorFactory;
import neolite.HolonMatrix;
import neolite.MatrixBuilder;

/**
 * A solute transport stream simulator using the NEO framework
 * 
 * @author v78h241
 *
 */
public class StreamSimulatorNEO extends Simulator {
   
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
         HashMap<String,String>argMap = MatrixBuilder.createArgMap(args);
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
         File configFile = new File(workingDir.getAbsolutePath() + argMap.get("config"));
         if (!configFile.exists() || configFile.isDirectory()) 
         {
            throw new Exception(String.format(
                  "%s is an invalid configuration file.", 
                  configFile.getAbsolutePath()
                  ));
         }

         simulator.getInputProcessorFactory().addBuilderInputProcessor(configFile, workingDir);
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
      matrix = MatrixBuilder.createMatrix(
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
   protected IInputProcessorFactory createInputProcessorFactory() 
   {
      return new StreamSimulatorNEOInputProcessorFactory();
   }

   @Override
   protected IOutputProcessorFactory createOutputProcessorFactory() 
   {
      return new StreamSimulatorNEOOutputProcessorFactory();
   }

   /**
    * Overrides implementation in Simulator to provide specific type
    */
   @Override
   public StreamSimulatorNEOInputProcessorFactory getInputProcessorFactory()
   {
      return (StreamSimulatorNEOInputProcessorFactory)inputProcessorFactory;
   }

   /**
    * Overrides implementation in Simulator to provide specific type
    */
   @Override
   public StreamSimulatorNEOOutputProcessorFactory getOutputProcessorFactory()
   {
      return (StreamSimulatorNEOOutputProcessorFactory)outputProcessorFactory;
   }

}
