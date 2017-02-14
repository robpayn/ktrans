package ktrans;

import java.util.ArrayList;

import org.payn.simulation.OutputProcessorFactory;
import org.payn.simulation.interfaces.IOutputProcessor;
import org.payn.simulation.metaoutputs.DoubleMetaOutput;
import org.w3c.dom.Element;

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
