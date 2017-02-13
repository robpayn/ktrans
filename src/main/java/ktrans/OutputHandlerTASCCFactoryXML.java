package ktrans;

import org.payn.chsm.io.OutputHandlerFactoryXML;
import org.payn.chsm.io.file.OutputHandlerIntervalFactoryXML;
import org.payn.chsm.io.xml.ElementHelper;

public class OutputHandlerTASCCFactoryXML extends OutputHandlerFactoryXML<OutputHandlerTASCC> {

   @Override
   protected void init() 
   {
      new OutputHandlerIntervalFactoryXML(outputHandler, config).init();
      ElementHelper particleElement = config.getFirstChildElementHelper("particle");
      
      outputHandler.initializeOutputHandlerTASCC();
      outputHandler.addResources(particleElement.getAttribute("resource"));
      outputHandler.setReleaseCell(particleElement.getAttribute("releaseCell"));
      outputHandler.setEndCell(particleElement.getAttribute("endCell"));
      outputHandler.setReleaseIteration(Long.valueOf(particleElement.getAttribute("releaseIteration")));
      outputHandler.setVelocityFile(particleElement.getAttribute("velocityFile"));
   }

   @Override
   protected OutputHandlerTASCC newOutputHandler() throws Exception 
   {
      return new OutputHandlerTASCC();
   }

}
