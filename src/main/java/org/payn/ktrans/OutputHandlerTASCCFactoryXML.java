package org.payn.ktrans;

import org.payn.chsm.io.ReporterFactoryXML;
import org.payn.chsm.io.file.ReporterIntervalFactoryXML;
import org.payn.chsm.io.xml.ElementHelper;

public class OutputHandlerTASCCFactoryXML extends ReporterFactoryXML<ReporterTASCC> {

   @Override
   protected void init() 
   {
      new ReporterIntervalFactoryXML(reporter, config).init();
      ElementHelper particleElement = config.getFirstChildElementHelper("particle");
      
      reporter.initializeOutputHandlerTASCC();
      reporter.addResources(particleElement.getAttribute("resource"));
      reporter.setReleaseCell(particleElement.getAttribute("releaseCell"));
      reporter.setEndCell(particleElement.getAttribute("endCell"));
      reporter.setReleaseIteration(Long.valueOf(particleElement.getAttribute("releaseIteration")));
      reporter.setVelocityFile(particleElement.getAttribute("velocityFile"));
   }

   @Override
   protected ReporterTASCC newReporter() throws Exception 
   {
      return new ReporterTASCC();
   }

}
