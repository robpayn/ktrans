package ktrans;

import java.util.ArrayList;

import org.w3c.dom.Element;

import edu.montana.cerg.simmanager.OutputProcessorFactory;
import edu.montana.cerg.simmanager.interfaces.IOutputProcessor;
import edu.montana.cerg.simmanager.metaoutputs.DoubleMetaOutput;

/**
 * Generic output processor factory for the stream solute simulator
 * 
 * @author v78h241
 *
 */
public class StreamSimulatorNEOOutputProcessorFactory extends OutputProcessorFactory {

   @Override
   public IOutputProcessor createFunctionOutputProc(DoubleMetaOutput output, ArrayList<Double> independentVals,
         Element element) 
   {
      throw new UnsupportedOperationException();
   }

}
