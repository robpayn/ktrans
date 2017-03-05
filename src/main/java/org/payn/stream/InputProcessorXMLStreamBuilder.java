package org.payn.stream;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.payn.neoch.io.xmltools.ElementBoundary;
import org.payn.neoch.io.xmltools.ElementHolonMatrix;

/**
 * Abstract input processor for building NEOCH models from meta input
 * 
 * @author robpayn
 *
 * @param <MIT>
 */
public abstract class InputProcessorXMLStreamBuilder<MIT extends MetaInputXMLStream>
   extends InputProcessorXMLNEOCHBuilder<MIT, SimulatorStream> {

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
   protected Double cellLength;

   /**
    * Bed slope
    */
   protected Double bedSlope;

   /**
    * Elevation datum
    */
   protected Double elevationDatum;

   /**
    * Active channel depth
    */
   protected Double activeDepth;

   /**
    * Initial water depth
    */
   protected Double initialDepth;

   /**
    * Average width of the active channel
    */
   protected Double averageWidth;

   /**
    * Intercept of the Wiele friction vs. depth relationship
    */
   protected Double wieleInt;

   /**
    * Slope of the Wiele friction vs. depth relationship
    */
   protected Double wieleSlope;

   /**
    * Initial stream flow
    */
   protected Double initialFlow;

   /**
    * Number of digits in cell count
    */
   protected Integer numCellsDigits;

   /**
    * Root string for cell names
    */
   protected String cellNameRoot;

   /**
    * Root string for boundary names
    */
   protected String boundaryNameRoot;

   /**
    * Initial conditions flag
    */
   protected boolean isInitialConditions;

   /**
    * Map of initial conditions for cells
    */
   protected LinkedHashMap<String, HashMap<String, Double>> initialConditionsCellMap;

   /**
    * Map of initial conditions for boundaries
    */
   protected LinkedHashMap<String, HashMap<String, Double>> initialConditionsBoundMap;

   /**
    * Dispersion coefficient
    */
   protected Double dispersionCoeff;

   /**
    * Construct a new instance with the given meta input and simulator
    * 
    * @param metaInput
    * @param sim
    */
   public InputProcessorXMLStreamBuilder(MIT metaInput, SimulatorStream sim) 
   {
      super(metaInput, sim);
   }

   @Override
   public void configureModel() throws Exception 
   {
      System.out.println("Building the stream matrix...");
      

      isInitialConditions = metaInput.isInitialConditions();

      // Stream structure
      numCells = metaInput.getAttributeNumCells();
      if (numCells != null)
      {
         numCellsDigits = new Integer(1 + (int)Math.log10(numCells));
      }
      cellNameRoot = metaInput.getAttributeCellName();
      boundaryNameRoot = metaInput.getAttributeBoundaryName();

      // Geometry
      streamLength = metaInput.getAttributeStreamLength();
      if (streamLength != null && numCells != null)
      {
         cellLength = streamLength / numCells;
      }
      elevationDatum = metaInput.getAttributeElevationDatum();
      bedSlope = metaInput.getAttributeBedSlope();
      activeDepth = metaInput.getAttributeActiveDepth();
      averageWidth = metaInput.getAttributeAverageWidth();
      
      // Flow
      initialDepth = metaInput.getAttributeInitialDepth();
      initialFlow = metaInput.getAttributeInitialFlow();
      dispersionCoeff = metaInput.getAttributeDispersionCoefficient();
      wieleInt = metaInput.getAttributeWieleInt();
      wieleSlope = metaInput.getAttributeWieleSlope();
      
      configureStreamLoop();
      
      String cellName;
      String boundaryName;
      ElementHolonMatrix elementCell;
      ElementBoundary elementBoundary;
      ElementBoundary elementBoundaryAdj;

      // Create the first cell
      cellName = String.format(
            "%s%0" + numCellsDigits.toString() + "d", 
            cellNameRoot,
            1
            );
      elementCell = documentCell.createCellElement(cellName);
      configureStreamCell(elementCell, 1);

      // Create upstream boundary
      boundaryName = String.format(
            "%sext_%0" + numCellsDigits.toString() + "d", 
            boundaryNameRoot,
            1
            );
      elementBoundary = documentBoundary.createBoundaryElement(
            boundaryName, cellName);
      configureUpstreamBoundary(elementBoundary, 1);

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
      
      // Create downstream boundary
      cellName = String.format(
            "%s%0" + numCellsDigits.toString() + "d", 
            cellNameRoot,
            numCells
            );
      boundaryName = String.format(
            "%s%0" + numCellsDigits.toString() + "d_ext", 
            boundaryNameRoot,
            numCells
            );
      elementBoundary = documentBoundary.createBoundaryElement(
            boundaryName, cellName);
      configureDownstreamBoundary(elementBoundary, numCells);
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

   /**
    * Configure the upstream boundary
    * 
    * @param elementBoundary
    *       XML element for the boundary
    * @param indexFirstCell
    *       index of first cell
    * @throws Exception
    *       if error in configuring boundary       
    */
   protected abstract void configureUpstreamBoundary(ElementBoundary elementBoundary,
         int indexFirstCell) throws Exception;

   /**
    * Configure the downstream boundary
    * 
    * @param elementBoundary
    *       XML element for the boundary
    * @param indexLastCell
    *       index of the last cell
    * @throws Exception 
    */
   protected abstract void configureDownstreamBoundary(ElementBoundary elementBoundary,
         long indexLastCell) throws Exception;

}
