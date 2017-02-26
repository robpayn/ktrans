package org.payn.stream;

import java.io.File;
import java.util.HashMap;

import org.payn.ktrans.StreamSimulatorMatrixLoader;
import org.payn.neoch.HolonMatrix;
import org.payn.neoch.MatrixLoader;
import org.payn.simulation.OutputProcessorFactory;
import org.payn.simulation.OutputProcessorFactoryAbstract;

/**
 * Implementation of a simulator for stream simulations
 * 
 * @author robpayn
 *
 */
public class StreamSimulator extends NEOCHSimulator {
   
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
         HashMap<String,String>argMap = MatrixLoader.createArgMap(args);
         File workingDir = new File(System.getProperty("user.dir"));
         
         StreamSimulator simulator = new StreamSimulator(argMap, workingDir);
         
         // Check for configuration file in file system
         if (!argMap.containsKey("config"))
         {
            throw new Exception(
                  "Must provide an argument for configuration file relative to working directory " +
                        "(e.g. 'config=./config/config.xml')"
                  );
         }

         simulator.getInputProcessorFactory().addMetabolismBuilderInputProcessor(
               workingDir, argMap.get("config")
               );
         simulator.execute();
      } 
      catch (Exception e) 
      {
         e.printStackTrace();
      }
   }

   /**
    * Map of command line arguments
    * (assumes "&ltkey&gt=&ltvalue&gt" form for each argument)
    */
   private HashMap<String, String> argMap;
   
   /**
    * Working directory for simulation
    */
   private File workingDir;

   /**
    * Construct a new instance based on the provided argument map and working directory
    * @param argMap
    * @param workingDir
    */
   public StreamSimulator(HashMap<String, String> argMap, File workingDir) 
   {
      this.argMap = argMap;
      this.workingDir = workingDir;
   }

   @Override
   protected InputProcessorFactoryStreamSimulator createInputProcessorFactory() 
   {
      return new InputProcessorFactoryStreamSimulator(this) {
         
         @Override
         public void addMetabolismBuilderInputProcessor(File workingDir, String configPath)
               throws Exception 
         {
            new MetabolismBuilderInputProcessorXML(
                  new MetabolismMetaInputXML(workingDir, configPath, "streambuilder"), 
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
      return StreamSimulatorMatrixLoader.initializeStreamSimulator(
            argMap, 
            workingDir
            );
   }

}
