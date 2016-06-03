package ktrans;

import java.io.File;
import java.util.HashMap;

import neolite.Matrix;
import neolite.io.MatrixBuilder;
import neolite.processors.ControllerNEO;

public class NEOModel {
   
   public static void main(String[] args)
   {
      try 
      {
         HashMap<String,String> argMap = MatrixBuilder.createArgMap(args);
         File workingDir = new File(System.getProperty("user.dir"));
         
         // Check for configuration file in file system
         if (!argMap.containsKey("config"))
         {
            throw new Exception(
                  "Must provide an argument for configuration file relative to working directory " +
                        "(e.g. 'config=./config/config.xml')"
                  );
         }
         File configFile = new File(
               workingDir.getAbsolutePath() + argMap.get("config")
               );
         if (!configFile.exists() || configFile.isDirectory()) 
         {
            throw new Exception(String.format(
                  "%s is an invalid configuration file.", 
                  configFile.getAbsolutePath()
                  ));
         }
         
         StreamBuilder streamBuilder = new StreamBuilderXML(configFile, workingDir);
         streamBuilder.build();
         
         Matrix matrix = MatrixBuilder.createMatrix(
               argMap, 
               new File(System.getProperty("user.dir"))
               );
         ControllerNEO controller = matrix.getController();
         controller.initializeController();
         controller.executeController();
      } 
      catch (Exception e) 
      {
         e.printStackTrace();
      }
   }

}
