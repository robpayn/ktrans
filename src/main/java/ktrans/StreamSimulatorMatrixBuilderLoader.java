package ktrans;

import chsm.Behavior;
import chsm.behaviors.time.BehaviorTime;
import chsm.io.OutputHandlerFactoryXML;
import chsm.io.file.OutputHandlerBehaviorFactoryXML;
import chsm.io.logger.LoggerSystemOut;
import chsm.io.xml.ElementBehavior;
import chsm.io.xml.ElementOutput;
import chsm.processors.ControllerHolon;
import currencies.solute.CurrencySolute;
import neoch.Currency;
import neoch.MatrixBuilder;
import neoch.io.MatrixLoaderXML;
import neoch.io.MatrixBuilderXML;
import neoch.io.OutputHandlerXMLSerialFactoryXML;
import neoch.io.xmltools.ElementCurrency;
import neoch.processors.ControllerNEORKTwo;

/**
 * Matrix loader implementing some default configuration
 * 
 * @author robpayn
 *
 */
public class StreamSimulatorMatrixBuilderLoader extends MatrixLoaderXML {
   
   /**
    * Serial output handler name
    */
   private static final String OUTPUT_HANDLER_SERIAL = "serial";
   
   /**
    * Behavior output handler name
    */
   private static final String OUTPUT_HANDLER_BEHAVIOR = "behavior";

   /**
    * Global behavior for time
    */
   private static final String GLOBAL_BEHAVIOR_TIME = "BehaviorTime";

   @Override
   protected void initializeLoggers() throws Exception {
      loggerList.add(LoggerSystemOut.class);
      super.initializeLoggers();
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
   protected Currency getCurrency(ElementCurrency currencyElem) throws Exception 
   {
      Currency currency = super.getCurrency(currencyElem);
      if (currency == null)
      {
         currency = new CurrencySolute();
      }
      return currency;
   }
   
   @Override
   protected OutputHandlerFactoryXML<?> getOutputHandlerFactory(ElementOutput outputElem) throws Exception 
   {
      OutputHandlerFactoryXML<?> factory = super.getOutputHandlerFactory(outputElem);
      if (factory == null)
      {
         switch(outputElem.getName())
         {
            case OUTPUT_HANDLER_SERIAL:
               factory = new OutputHandlerXMLSerialFactoryXML();
               break;
            case OUTPUT_HANDLER_BEHAVIOR:
               factory = new OutputHandlerBehaviorFactoryXML();
               break;
         }
      }
      return factory;
   }

   @Override
   protected Behavior getBehavior(ElementBehavior behaviorElem) throws Exception 
   {
      Behavior behavior = super.getBehavior(behaviorElem);
      if (behavior == null && behaviorElem.getName().matches(GLOBAL_BEHAVIOR_TIME))
      {
            return new BehaviorTime();
      }
      return behavior;
   }
   
}
