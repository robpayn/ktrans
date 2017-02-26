package org.payn.stream;

import org.payn.neoch.HolonMatrix;
import org.payn.simulation.SimulatorAbstract;

/**
 * Simulator using a NEOCH model
 * 
 * @author robpayn
 *
 */
public abstract class SimulatorNEOCH extends SimulatorAbstract {
   
   /**
    * NEOCH model matrix
    */
   protected HolonMatrix matrix;

   /**
    * Initialize the model
    * 
    * @throws Exception
    */
   public void initializeModel() throws Exception
   {
      matrix = createMatrix();
   }

   @Override
   protected void runModel() throws Exception 
   {
      matrix.getController().executeController();
   }
   
   /**
    * Create the matrix for the model
    * 
    * @return
    *       matrix object
    * @throws Exception
    *       if error in matrix creation
    */
   protected abstract HolonMatrix createMatrix() throws Exception;

}
