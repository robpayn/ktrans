package org.payn.ktrans;

import java.io.File;
import java.util.HashMap;

import org.payn.chsm.ReporterFactoryXML;
import org.payn.chsm.io.reporters.ReporterIntervalFactoryXML;
import org.payn.chsm.io.xmltools.ElementHelper;

/**
 * Factory for configuring a TASCC lagrangian reporter
 * 
 * @author robpayn
 *
 */
public class ReporterTASCCFactoryXML extends ReporterFactoryXML<ReporterTASCC> {

   @Override
   protected void init() throws Exception 
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
   protected ReporterTASCC newReporter(File workingDir, HashMap<String, String> argMap) throws Exception 
   {
      return new ReporterTASCC(workingDir, argMap);
   }

}
