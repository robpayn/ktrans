package org.payn.stream;

import java.io.File;

import org.payn.neoch.io.xmltools.DocumentBoundary;
import org.payn.neoch.io.xmltools.DocumentCell;
import org.payn.simulation.InputProcessorAbstract;

/**
 * Abstract implementation of an input processor that creates a
 * NEOCH model from meta input
 * 
 * @author robpayn
 *
 * @param <MIT>
 *      meta input type
 * @param <ST> 
 *      simulator type
 */
public abstract class NEOCHBuilderInputProcessorXML<MIT extends NEOCHMetaInputXML, ST extends NEOCHSimulator> 
      extends InputProcessorAbstract<MIT, ST> {

   /**
    * Cell XML document
    */
   protected DocumentCell documentCell;
   
   /**
    * Boundary XML document
    */
   protected DocumentBoundary documentBoundary;

   /**
    * Construct a new instance with the given meta input and simulator
    * 
    * @param metaInput
    * @param sim
    */
   public NEOCHBuilderInputProcessorXML(MIT metaInput, ST sim) 
   {
      super(metaInput, sim);
   }

   @Override
   public void execute() throws Exception 
   {
      if (!metaInput.isActive())
      {
         System.out.println("Builder is inactive, attempting to run existing model...");
      }
      else
      {
         System.out.println("Building the NEOCH files...");

         // Set up cell XML document
         File cellFile = metaInput.getCellFile();
         documentCell = new DocumentCell(cellFile.getName());
         
         // Set up boundary XML document
         File boundaryFile = metaInput.getBoundaryFile();
         documentBoundary = new DocumentBoundary(boundaryFile.getName());
         
         configureResources();
         
         configureModel();
         
         // Write the model input files
         documentCell.write(cellFile.getParentFile());
         documentBoundary.write(boundaryFile.getParentFile());
      }
      simulator.initializeModel();
   }

   /**
    * Configure the model
    * 
    * @throws Exception
    *       if error in model configuration
    */
   protected abstract void configureModel() throws Exception;
   
   /**
    * Configure the resources used in the model
    * 
    * @throws Exception
    *       if error in loading resources
    */
   protected abstract void configureResources() throws Exception;

}
