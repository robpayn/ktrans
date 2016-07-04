package ktrans;

import java.io.File;

import chsm.io.file.interpolate.ProcessorInterpolateSnapshotTable;
import currencies.solute.CurrencySolute;
import currencies.solute.boundary.BehaviorSoluteActiveMM;
import currencies.solute.boundary.BehaviorSoluteBoundInject;
import currencies.solute.boundary.BehaviorSoluteFlow;
import currencies.solute.boundary.BehaviorSoluteFlowBound;
import currencies.solute.cell.BehaviorSoluteStorage;
import edu.montana.cerg.simmanager.InputProcessor;
import neoch.behaviors.BehaviorMatrix;
import neoch.io.xml.DocumentBoundary;
import neoch.io.xml.DocumentCell;
import neoch.io.xml.ElementBehaviorMatrix;
import neoch.io.xml.ElementBoundary;
import neoch.io.xml.ElementHolonMatrix;

/**
 * Input processor for building NEO input for a simple stream solute model
 * 
 * @author v78h241
 *
 */
public class StreamBuilderNEOInputProcessorXML extends InputProcessor<StreamBuilderMetaInputXML,StreamSimulatorNEO> {

   /**
    * Constructor 
    * 
    * @param metaInput
    *       meta input processed by this processor
    * @param sim
    *       simulator associated with this processor
    */
   public StreamBuilderNEOInputProcessorXML(StreamBuilderMetaInputXML metaInput, StreamSimulatorNEO sim) 
   {
      super(metaInput, sim);
   }

   @Override
   public void execute() throws Exception 
   {
      if (!metaInput.isActive())
      {
         System.out.println("Builder is inactive, proceeding to run existing model...");
      }
      else
      {
         System.out.println("Building the stream matrix files...");
         
         // Geometry
         Long numCells = metaInput.getNumCells();
         Double length = new Double(metaInput.getLength() / (double)numCells);
         Double width = metaInput.getWidth();
         Double boundaryArea = width * metaInput.getDepth();
         Double planarea = length * width;
         Double storageVolume = length * boundaryArea;
         
         // Flow
         Double flow = -metaInput.getFlow();
         Double disp = metaInput.getDispersion();
         
         // Conservative solute
         Double consBkgConc = metaInput.getConsBkgConc();
         
         // Active solute
         Double activeBkgConc = metaInput.getActiveBkgConc();
         Double uMax = metaInput.getUMax();
         Double halfSat = metaInput.getHalfSat();
         
         // Cell and Boundary input files
         File cellFile = metaInput.getCellFile();
         DocumentCell documentCell = new DocumentCell(cellFile.getName());
         File boundaryFile = metaInput.getBoundaryFile();
         DocumentBoundary documentBoundary = new DocumentBoundary(boundaryFile.getName());
         
         Integer numCellsDigits = new Integer(1 + (int)Math.log10(numCells));
         String cellName = "";
         String boundaryName = "";
         ElementHolonMatrix elementCell = null;
         ElementBoundary elementBoundary = null;
         ElementBehaviorMatrix elementBehavior = null;
         
         // Set up the currencies
         CurrencySolute consCurrency = new CurrencySolute();
         consCurrency.initialize("cons");
         
         CurrencySolute actCurrency = new CurrencySolute();
         actCurrency.initialize("active");
         
         // Set up the behaviors
         BehaviorMatrix consBehaviorFlow = consCurrency.getBehavior(CurrencySolute.BEHAVIOR_FLOW);
         BehaviorMatrix consBehaviorStorage = consCurrency.getBehavior(CurrencySolute.BEHAVIOR_STORAGE);

         BehaviorMatrix actBehaviorFlow = actCurrency.getBehavior(CurrencySolute.BEHAVIOR_FLOW);
         BehaviorMatrix actBehaviorStorage = actCurrency.getBehavior(CurrencySolute.BEHAVIOR_STORAGE);
         BehaviorMatrix actBehaviorUptake = actCurrency.getBehavior(CurrencySolute.BEHAVIOR_ACTIVEMM);
         
         // Upstream boundaries
         cellName = String.format("cell%0" + numCellsDigits.toString() + "d", 1);
         
         if (metaInput.isInject())
         {
            boundaryName = "ext_" + cellName;
            
            // Conservative tracer
            elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
            elementBehavior = elementBoundary.createBehaviorElement(
                  consCurrency.getBehavior(CurrencySolute.BEHAVIOR_CONCBOUND_INJECT)
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_PATH, 
                  metaInput.getConcBoundFile(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_TYPE, 
                  metaInput.getInterpolationType(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_DELIMITER, 
                  metaInput.getDelimiter(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  BehaviorSoluteFlowBound.REQ_STATE_FLOW, 
                  new Double(-flow).toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  BehaviorSoluteFlow.REQ_STATE_DISP, 
                  metaInput.getDispersion().toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  BehaviorSoluteFlow.REQ_STATE_LENGTH, 
                  length.toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  BehaviorSoluteFlow.REQ_STATE_AREA_XSECT, 
                  boundaryArea.toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + BehaviorSoluteBoundInject.REQ_STATE_MASS, 
                  metaInput.getConservativeInjectMass().toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + BehaviorSoluteBoundInject.REQ_STATE_DURATION, 
                  metaInput.getConservativeInjectDuration().toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + BehaviorSoluteBoundInject.REQ_STATE_START, 
                  metaInput.getConservativeInjectStartInterval().toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + CurrencySolute.NAME_SOLUTE_CONC, 
                  consBkgConc.toString(), 
                  null
                  );
            
            // Active Tracer
            elementBehavior = elementBoundary.createBehaviorElement(
                  actCurrency.getBehavior(CurrencySolute.BEHAVIOR_CONCBOUND_INJECT)
                  );
            elementBehavior.createInitValueElement(
                  actCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_PATH, 
                  metaInput.getConcBoundFile(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  actCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_TYPE, 
                  metaInput.getInterpolationType(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  actCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_DELIMITER, 
                  metaInput.getDelimiter(), 
                  null
                  );
            if (metaInput.isActivInjUnique())
            {
               elementBehavior.createInitValueElement(
                     actCurrency.getName() + BehaviorSoluteBoundInject.REQ_STATE_MASS, 
                     metaInput.getActiveInjectMass().toString(), 
                     null
                     );
               elementBehavior.createInitValueElement(
                     actCurrency.getName() + BehaviorSoluteBoundInject.REQ_STATE_DURATION, 
                     metaInput.getActiveInjectDuration().toString(), 
                     null
                     );
               elementBehavior.createInitValueElement(
                     actCurrency.getName() + BehaviorSoluteBoundInject.REQ_STATE_START, 
                     metaInput.getActiveInjectStartInterval().toString(), 
                     null
                     );
            }
            else
            {
               elementBehavior.createInitValueElement(
                     actCurrency.getName() + BehaviorSoluteBoundInject.REQ_STATE_MASS, 
                     metaInput.getConservativeInjectMass().toString(), 
                     null
                     );
               elementBehavior.createInitValueElement(
                     actCurrency.getName() + BehaviorSoluteBoundInject.REQ_STATE_DURATION, 
                     metaInput.getConservativeInjectDuration().toString(), 
                     null
                     );
               elementBehavior.createInitValueElement(
                     actCurrency.getName() + BehaviorSoluteBoundInject.REQ_STATE_START, 
                     metaInput.getConservativeInjectStartInterval().toString(), 
                     null
                     );
            }
            elementBehavior.createInitValueElement(
                  actCurrency.getName() + CurrencySolute.NAME_SOLUTE_CONC, 
                  activeBkgConc.toString(), 
                  null);
         }
         else
         {
            boundaryName = "ext_" + cellName;
            
            elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
            elementBehavior = elementBoundary.createBehaviorElement(
                  consCurrency.getBehavior(CurrencySolute.BEHAVIOR_CONCBOUND)
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_PATH, 
                  metaInput.getConcBoundFile(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_TYPE, 
                  metaInput.getInterpolationType(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_DELIMITER, 
                  metaInput.getDelimiter(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  BehaviorSoluteFlowBound.REQ_STATE_FLOW, 
                  new Double(-flow).toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  BehaviorSoluteFlow.REQ_STATE_DISP, 
                  metaInput.getDispersion().toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  BehaviorSoluteFlow.REQ_STATE_LENGTH, 
                  length.toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  BehaviorSoluteFlow.REQ_STATE_AREA_XSECT, 
                  boundaryArea.toString(), 
                  null
                  );
            
            elementBehavior = elementBoundary.createBehaviorElement(
                  actCurrency.getBehavior(CurrencySolute.BEHAVIOR_CONCBOUND)
                  );
            elementBehavior.createInitValueElement(
                  actCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_PATH, 
                  metaInput.getConcBoundFile(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  actCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_TYPE, 
                  metaInput.getInterpolationType(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  actCurrency.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_DELIMITER, 
                  metaInput.getDelimiter(), 
                  null
                  );
         }
         
         // Cycle through cells
         for (int i = 1; i < numCells; i++)
         {
            // Cell
            cellName = String.format("cell%0" + numCellsDigits.toString() + "d", i);
            elementCell = documentCell.createCellElement(cellName);
            
            // Cell conservative tracer
            elementBehavior = elementCell.createBehaviorElement(consBehaviorStorage);
            elementBehavior.createInitValueElement(
                  BehaviorSoluteStorage.REQ_STATE_VOLUME, 
                  storageVolume.toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consCurrency.getName() + CurrencySolute.NAME_SOLUTE_CONC, 
                  consBkgConc.toString(), 
                  null
                  );
            
            // Cell active tracer
            elementBehavior = elementCell.createBehaviorElement(actBehaviorStorage);
            elementBehavior.createInitValueElement(
                  actCurrency.getName() + CurrencySolute.NAME_SOLUTE_CONC, 
                  activeBkgConc.toString(), 
                  null
                  );
            
            // Downstream boundary 
            boundaryName = cellName + String.format("_%0" + numCellsDigits.toString() + "d", i + 1);
            elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
            
            // Downstream boundary conservative tracer
            elementBehavior = elementBoundary.createBehaviorElement(consBehaviorFlow);
            elementBehavior.createInitValueElement(BehaviorSoluteFlow.REQ_STATE_LENGTH, length.toString(), null);
            elementBehavior.createInitValueElement(BehaviorSoluteFlow.REQ_STATE_AREA_XSECT, boundaryArea.toString(), null);
            elementBehavior.createInitValueElement(BehaviorSoluteFlow.REQ_STATE_FLOW, flow.toString(), null);
            elementBehavior.createInitValueElement(BehaviorSoluteFlow.REQ_STATE_DISP, disp.toString(), null);
            
            // Downstream boundary active tracer
            elementBehavior = elementBoundary.createBehaviorElement(actBehaviorFlow);
            
            // Downstream boundary create cell and boundary name for adjacent boundary
            cellName = String.format("cell%0" + numCellsDigits.toString() + "d", i + 1);
            boundaryName = cellName + String.format("_%0" + numCellsDigits.toString() + "d", i);
            elementBoundary = elementBoundary.createAdjacentElement(boundaryName, cellName);
            
            // Uptake boundary (external boundary for active tracer only)
            cellName = String.format("cell%0" + numCellsDigits.toString() + "d", i);
            boundaryName = cellName + String.format("_uptake", numCells);
            elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
            elementBehavior = elementBoundary.createBehaviorElement(actBehaviorUptake);
            elementBehavior.createInitValueElement(BehaviorSoluteActiveMM.REQ_STATE_UMAX, uMax.toString(), null);
            elementBehavior.createInitValueElement(BehaviorSoluteActiveMM.REQ_STATE_HALFSAT, halfSat.toString(), null);
            elementBehavior.createInitValueElement(BehaviorSoluteActiveMM.REQ_STATE_PLANAREA, planarea.toString(), null);
            elementBehavior.createInitValueElement(BehaviorSoluteActiveMM.REQ_STATE_BKG_CONC, activeBkgConc.toString(), null);
         }
         // Last cell
         cellName = String.format("cell%0" + numCellsDigits.toString() + "d", numCells);
         elementCell = documentCell.createCellElement(cellName);
         
         // Last cell conservative tracer
         elementBehavior = elementCell.createBehaviorElement(consBehaviorStorage);
         elementBehavior.createInitValueElement(BehaviorSoluteStorage.REQ_STATE_VOLUME, storageVolume.toString(), null);
         elementBehavior.createInitValueElement(
               consCurrency.getName() + CurrencySolute.NAME_SOLUTE_CONC, 
               consBkgConc.toString(),
               "null"
               );
         
         // Last cell active tracer
         elementBehavior = elementCell.createBehaviorElement(actBehaviorStorage);
         elementBehavior.createInitValueElement(actCurrency.getName() + CurrencySolute.NAME_SOLUTE_CONC, activeBkgConc.toString(), null);
         
         // Uptake boundary for last cell
         boundaryName = cellName + String.format("_uptake", numCells);
         elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
         elementBehavior = elementBoundary.createBehaviorElement(actBehaviorUptake);
         elementBehavior.createInitValueElement(BehaviorSoluteActiveMM.REQ_STATE_UMAX, uMax.toString(), null);
         elementBehavior.createInitValueElement(BehaviorSoluteActiveMM.REQ_STATE_HALFSAT, halfSat.toString(), null);
         elementBehavior.createInitValueElement(BehaviorSoluteActiveMM.REQ_STATE_PLANAREA, planarea.toString(), null);
         elementBehavior.createInitValueElement(BehaviorSoluteActiveMM.REQ_STATE_BKG_CONC, activeBkgConc.toString(), null);

         // Downstream boundary
         boundaryName = cellName + String.format("_ext", numCells);
         elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
         
         // Downstream boundary conservative tracer
         elementBehavior = elementBoundary.createBehaviorElement(
               consCurrency.getBehavior(CurrencySolute.BEHAVIOR_FLOWBOUND)
               );
         elementBehavior.createInitValueElement(BehaviorSoluteFlowBound.REQ_STATE_FLOW, flow.toString(), null);
         elementBehavior.createInitValueElement(
               consCurrency.getName() + CurrencySolute.NAME_SOLUTE_CONC, 
               consBkgConc.toString(), 
               null
               );
         
         // Downstream boundary active tracer
         elementBehavior = elementBoundary.createBehaviorElement(
               actCurrency.getBehavior(CurrencySolute.BEHAVIOR_FLOWBOUND)
               );
         elementBehavior.createInitValueElement(
               actCurrency.getName() + CurrencySolute.NAME_SOLUTE_CONC, 
               activeBkgConc.toString(), 
               null
               );
         
         // Write the cell and boundary XML files
         documentCell.write(cellFile.getParentFile());
         documentBoundary.write(boundaryFile.getParentFile());
      }
      sim.loadMatrix();
   }
}
