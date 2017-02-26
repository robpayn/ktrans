package org.payn.stream;

import org.payn.neoch.io.xmltools.ElementBoundary;
import org.payn.neoch.io.xmltools.ElementHolonMatrix;

/**
 * Abstract input processor for building NEOCH models from meta input
 * 
 * @author robpayn
 *
 * @param <MIT>
 */
public abstract class StreamBuilderInputProcessorXML<MIT extends StreamMetaInputXML>
   extends NEOCHBuilderInputProcessorXML<MIT, StreamSimulator> {

   /**
    * Number of cells
    */
   protected Long numCells;
   
   /**
    * Length of the stream
    */
   protected Double streamLength;

   /**
    * Length of a cell
    */
   protected double cellLength;

   /**
    * Bed slope
    */
   protected double bedSlope;

   /**
    * Elevation datum
    */
   protected double elevationDatum;

   /**
    * Active channel depth
    */
   protected double activeDepth;

   /**
    * Initial water depth
    */
   protected double initialDepth;

   /**
    * Average width of the active channel
    */
   protected double averageWidth;

   /**
    * Intercept of the Wiele friction vs. depth relationship
    */
   protected double wieleInt;

   /**
    * Slope of the Wiele friction vs. depth relationship
    */
   protected double wieleSlope;

   /**
    * Construct a new instance with the given meta input and simulator
    * 
    * @param metaInput
    * @param sim
    */
   public StreamBuilderInputProcessorXML(MIT metaInput, StreamSimulator sim) 
   {
      super(metaInput, sim);
   }

   @Override
   public void configureModel() throws Exception 
   {
      System.out.println("Building the stream matrix...");

      // Geometry
      numCells = metaInput.getNumCells();
      Integer numCellsDigits = new Integer(1 + (int)Math.log10(numCells));
      String cellNameRoot = metaInput.getCellName();
      String boundaryNameRoot = metaInput.getBoundaryName();
      
      String cellName;
      String boundaryName;
      ElementHolonMatrix elementCell;
      ElementBoundary elementBoundary;
      ElementBoundary elementBoundaryAdj;
      
      streamLength = metaInput.getStreamLength();
      cellLength = streamLength / numCells;
      bedSlope = metaInput.getBedSlope();
      elevationDatum = metaInput.getElevationDatum();
      activeDepth = metaInput.getActiveDepth();
      initialDepth = metaInput.getInitialDepth();
      averageWidth = metaInput.getAverageWidth();
      
      wieleInt = metaInput.getWieleInt();
      wieleSlope = metaInput.getWieleSlope();
      
      configureStreamLoop();
      
      // Create the first cell
      cellName = String.format(
            "%s%0" + numCellsDigits.toString() + "d", 
            cellNameRoot,
            1
            );
      elementCell = documentCell.createCellElement(cellName);
      configureStreamCell(elementCell, 1);

      // Cycle through cells
      for (int i = 2; i <= numCells; i++)
      {
         // Create cell
         cellName = String.format(
               "%s%0" + numCellsDigits.toString() + "d", 
               cellNameRoot,
               i
               );
         elementCell = documentCell.createCellElement(cellName);
         configureStreamCell(elementCell, i);

         // Create upstream boundary
         boundaryName = String.format(
               "%s%0" + numCellsDigits.toString() + "d_%0" + numCellsDigits.toString() + "d", 
               boundaryNameRoot,
               i,
               i - 1
               );
         elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
         
         // Create adjacent boundary
         cellName = String.format(
               "%s%0" + numCellsDigits.toString() + "d", 
               cellNameRoot,
               i - 1
               );
         boundaryName = String.format(
               "%s%0" + numCellsDigits.toString() + "d_%0" + numCellsDigits.toString() + "d", 
               boundaryNameRoot,
               i - 1,
               i
               );
         elementBoundaryAdj = elementBoundary.createAdjacentElement(boundaryName, cellName);
         configureStreamBoundary(elementBoundary, elementBoundaryAdj, i);
      }
   }

   /**
    * Configure the behaviors for the stream
    * 
    * @throws Exception 
    *       if error in creating the behaviors
    */
   protected abstract void configureStreamLoop() throws Exception;

   /**
    * Set up a stream cell
    * 
    * @param elementCell
    *       cell element to configure
    * @param index
    *       index number of cell         
    */
   protected abstract void configureStreamCell(ElementHolonMatrix elementCell, long index);

   /**
    * Set up a stream boundary
    * 
    * @param elementBoundary
    *       boundary element to configure
    * @param index
    *       index number of cell
    */
   protected abstract void configureStreamBoundary(ElementBoundary elementBoundary, 
         ElementBoundary elementBoundaryAdj, int index);

}
