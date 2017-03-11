package org.payn.stream;

import java.io.File;
import java.util.HashMap;

import org.payn.chsm.Resource;
import org.payn.chsm.io.ReporterFactoryXML;
import org.payn.chsm.io.file.ReporterBehaviorFactoryXML;
import org.payn.chsm.io.logger.LoggerSystemOut;
import org.payn.chsm.io.xml.ElementReporter;
import org.payn.chsm.io.xml.ElementResource;
import org.payn.chsm.processors.ControllerHolon;
import org.payn.ktrans.OutputHandlerTASCCFactoryXML;
import org.payn.neoch.HolonMatrix;
import org.payn.neoch.MatrixBuilder;
import org.payn.neoch.MatrixBuilderXML;
import org.payn.neoch.MatrixLoaderXML;
import org.payn.neoch.io.OutputHandlerXMLSerialFactoryXML;
import org.payn.neoch.processors.ControllerNEORKTwo;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.solute.ResourceSoluteOTIS;
import org.payn.resources.water.ResourceWater;

/**
 * Matrix loader implementing some default configuration
 * 
 * @author robpayn
 *
 */
public class MatrixLoaderStreamSimulator extends MatrixLoaderXML {
   
   /**
    * Serial output handler name
    */
   private static final String OUTPUT_HANDLER_SERIAL = "serial";
   
   /**
    * Behavior output handler name
    */
   private static final String OUTPUT_HANDLER_BEHAVIOR = "behavior";

   /**
    * Behavior output handler name
    */
   private static final String OUTPUT_HANDLER_TASCC = "tascc";

   /**
    * Load and build the matrix
    * 
    * @param argMap
    * @param workingDir
    * @return
    *       matrix object
    * @throws Exception
    */
   public static HolonMatrix initializeStreamSimulator(HashMap<String, String> argMap,
         File workingDir) throws Exception 
   {
      MatrixBuilder builder = MatrixBuilder.loadBuilder(
            workingDir,
            new MatrixLoaderStreamSimulator(),
            argMap
            );
      HolonMatrix matrix = builder.createModel();
      matrix.getController().initializeController();
      return matrix;
   }

   @Override
   protected void loadLoggers() throws Exception {
      loggerList.add(LoggerSystemOut.class);
      super.loadLoggers();
   }   

   @Override
   protected MatrixBuilder createBuilder() throws Exception 
   {
      MatrixBuilder builder = super.createBuilder();
      if (builder == null)
      {
         builder = new MatrixBuilderXML();
      }
      return builder;
   }
   
   @Override
   protected ControllerHolon getController() throws Exception 
   {
      ControllerHolon controller = super.getController();
      if (controller == null)
      {
         controller = new ControllerNEORKTwo();
      }
      return controller;
   }
   
   @Override
   protected Resource getResource(ElementResource resourceElem) throws Exception 
   {
      Resource resource = super.getResource(resourceElem);
      if (resource == null)
      {
         switch(resourceElem.getName())
         {
            case "water":
               resource = new ResourceWater();
               break;
            case "cons":
            case "active":
               resource = new ResourceSolute();
               break;
            case "conserveOTIS":
            case "activeOTIS":
               resource = new ResourceSoluteOTIS();
               break;
         }
      }
      return resource;
   }
   
   @Override
   protected ReporterFactoryXML<?> getReporterFactory(ElementReporter outputElem) throws Exception 
   {
      ReporterFactoryXML<?> factory = super.getReporterFactory(outputElem);
      if (factory == null)
      {
         switch(outputElem.getName())
         {
            case OUTPUT_HANDLER_SERIAL:
               factory = new OutputHandlerXMLSerialFactoryXML();
               break;
            case OUTPUT_HANDLER_BEHAVIOR:
               factory = new ReporterBehaviorFactoryXML();
               break;
            case OUTPUT_HANDLER_TASCC:
               factory = new OutputHandlerTASCCFactoryXML();
               break;
         }
      }
      return factory;
   }

}
