package org.payn.ktrans.otis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.payn.ktrans.StreamBuilderMetaInputXML;
import org.payn.neoch.HolonMatrix;
import org.payn.neoch.MatrixBuilder;
import org.payn.neoch.io.xmltools.XMLDocumentConfig;
import org.payn.simulation.InputProcessorFactory;
import org.payn.simulation.InputProcessorFactoryAbstract;
import org.payn.simulation.OutputProcessor;
import org.payn.simulation.OutputProcessorFactory;
import org.payn.simulation.OutputProcessorFactoryAbstract;
import org.payn.simulation.SimulatorAbstract;
import org.payn.simulation.metainputs.DoubleMetaInput;
import org.payn.simulation.metaoutputs.DoubleMetaOutput;
import org.payn.stream.MatrixLoaderStreamSimulator;
import org.w3c.dom.Element;

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
            workingDir,
            new MatrixLoaderStreamSimulator()
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
      return new InputProcessorFactoryAbstract() {

         @Override
         public void addDoubleToParamProc(DoubleMetaInput input,
               Element element) 
         {
            throw new UnsupportedOperationException();
         }

         @Override
         public void addBuilderInputProcessor(File configFile, File workingDir)
               throws Exception 
         {
            XMLDocumentConfig doc = new XMLDocumentConfig(configFile);
            StreamBuilderMetaInputXML metaInput = new StreamBuilderMetaInputXML(
                  (Element)doc.getRootElementHelper().getFirstChildElement("streambuilder"),
                  doc,
                  workingDir
                  );
            new StreamBuilderNEOInputProcessorXML(metaInput, getSimulator());
         }
         
         @Override
         public StreamSimulatorNEO getSimulator() 
         {
            return (StreamSimulatorNEO)simulator;
         }

      };
   }

   @Override
   protected OutputProcessorFactory createOutputProcessorFactory() 
   {
      return new OutputProcessorFactoryAbstract() {
         
         @Override
         public OutputProcessor createFunctionOutputProc(DoubleMetaOutput output, ArrayList<Double> independentVals,
               Element element) 
         {
            throw new UnsupportedOperationException();
         }
         
      };
   }

}
