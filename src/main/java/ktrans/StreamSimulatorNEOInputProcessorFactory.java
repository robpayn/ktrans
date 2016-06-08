package ktrans;

import java.io.File;

import org.w3c.dom.Element;

import edu.montana.cerg.simmanager.InputProcessorFactory;
import edu.montana.cerg.simmanager.metainputs.DoubleMetaInput;
import statemachine.io.xml.XMLDocument;

/**
 * Generic input processor factory for the stream solute simulator
 * 
 * @author v78h241
 *
 */
public class StreamSimulatorNEOInputProcessorFactory extends InputProcessorFactory {

   @Override
   public void addDoubleToParamProc(DoubleMetaInput input, Element element) 
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void addBuilderInputProcessor(File configFile, File workingDir) throws Exception 
   {
      XMLDocument doc = new XMLDocument(configFile);
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

}
