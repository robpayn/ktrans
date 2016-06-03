package ktrans;

import java.io.File;

import neolite.Matrix;
import neolite.io.MatrixBuilder;
import neolite.processors.ControllerNEO;

public class NEOModel {
   
   public static void main(String[] args)
   {
      try 
      {
         Matrix matrix = MatrixBuilder.createMatrix(
               args, 
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
