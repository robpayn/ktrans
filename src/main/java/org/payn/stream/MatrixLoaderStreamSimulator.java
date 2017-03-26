package org.payn.stream;

import java.io.File;
import java.util.HashMap;

import org.payn.chsm.ModelLoaderXML;
import org.payn.chsm.ReporterFactoryXML;
import org.payn.chsm.Resource;
import org.payn.chsm.io.logger.LoggerSystemOut;
import org.payn.chsm.io.reporters.ReporterBehaviorFactoryXML;
import org.payn.chsm.io.xmltools.ElementReporter;
import org.payn.chsm.io.xmltools.ElementResource;
import org.payn.chsm.processors.ControllerHolon;
import org.payn.neoch.HolonMatrix;
import org.payn.neoch.MatrixBuilder;
import org.payn.neoch.MatrixBuilderXML;
import org.payn.neoch.io.reporters.ReporterXMLSerialFactoryXML;
import org.payn.neoch.processors.ControllerNEOCHRKTwo;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.solute.ResourceSoluteOTIS;
import org.payn.resources.water.ResourceWater;
import org.payn.stream.reporter.ReporterTASCCFactoryXML;

/**
 * Matrix loader implementing some default configuration
 * 
 * @author robpayn
 *
 */
public class MatrixLoaderStreamSimulator extends ModelLoaderXML {
   
   /**
    * Serial reporter name
    */
   private static final String REPORTER_SERIAL = "serial";
   
   /**
    * Behavior reporter name
    */
   private static final String REPORTER_BEHAVIOR = "behavior";

   /**
    * Behavior reporter name
    */
   private static final String REPORTER_TASCC = "tascc";

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
            argMap,
            new MatrixLoaderStreamSimulator()
            );
      HolonMatrix matrix = builder.buildModel();
      matrix.getController().initializeController();
      return matrix;
   }

   @Override
   protected void loadLoggers() throws Exception {
      loggerList.add(LoggerSystemOut.class);
      super.loadLoggers();
   }   

   @Override
   protected MatrixBuilder loadBuilder() throws Exception 
   {
      MatrixBuilder builder = null;
      try
      {
         builder = (MatrixBuilder)super.loadBuilder();
      }
      catch (Exception e)
      {
         throw new Exception(String.format(
               "Designated builder %s is not a matrix builder", 
               builder.getClass().getCanonicalName()
               ));
      }
      if (builder == null)
      {
         builder = new MatrixBuilderXML();
      }
      return builder;
   }
   
   @Override
   protected ControllerHolon loadController() throws Exception 
   {
      ControllerHolon controller = super.loadController();
      if (controller == null)
      {
         controller = new ControllerNEOCHRKTwo();
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
            case "oxygen":
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
            case REPORTER_SERIAL:
               factory = new ReporterXMLSerialFactoryXML();
               break;
            case REPORTER_BEHAVIOR:
               factory = new ReporterBehaviorFactoryXML();
               break;
            case REPORTER_TASCC:
               factory = new ReporterTASCCFactoryXML();
               break;
         }
      }
      return factory;
   }

}
