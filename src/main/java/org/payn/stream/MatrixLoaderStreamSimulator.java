package org.payn.stream;

import java.io.File;
import java.util.HashMap;

import org.payn.chsm.io.ModelBuilder;
import org.payn.chsm.io.ModelLoader;
import org.payn.chsm.io.ModelLoaderXML;
import org.payn.chsm.io.logger.LoggerSystemOut;
import org.payn.chsm.io.reporters.ReporterBehaviorFactoryXML;
import org.payn.chsm.io.reporters.ReporterFactoryXML;
import org.payn.chsm.io.xmltools.ElementReporter;
import org.payn.chsm.io.xmltools.ElementResource;
import org.payn.chsm.processors.ControllerHolon;
import org.payn.chsm.processors.finitedifference.ControllerRungeKuttaTwo;
import org.payn.chsm.resources.Resource;
import org.payn.neoch.HolonMatrix;
import org.payn.neoch.io.MatrixBuilderXML;
import org.payn.neoch.io.reporters.ReporterXMLSerialFactoryXML;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.solute.concentration.ResourceSoluteConcentration;
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
    * @param workingDir
    * @param argMap
    * @param loader 
    * @return
    *       matrix object
    * @throws Exception
    */
   public static HolonMatrix initializeStreamSimulator(
         File workingDir, 
         HashMap<String, String> argMap,
         MatrixLoaderStreamSimulator loader
         ) throws Exception 
   {
      ModelBuilder builder = ModelLoader.loadBuilder(
            workingDir,
            argMap,
            loader
            );
      HolonMatrix matrix = (HolonMatrix)builder.buildModel();
      matrix.getController().initializeController();
      return matrix;
   }

   @Override
   protected void loadLoggers() throws Exception {
      loggerList.add(LoggerSystemOut.class);
      super.loadLoggers();
   }   

   @Override
   protected ModelBuilder loadBuilder() throws Exception 
   {
      ModelBuilder builder = null;
      builder = super.loadBuilder();
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
         controller = new ControllerRungeKuttaTwo();
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
            case "oxygen":
               resource = new ResourceSolute();
               break;
            case "conserve":
            case "active":
               resource = new ResourceSoluteConcentration();
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
