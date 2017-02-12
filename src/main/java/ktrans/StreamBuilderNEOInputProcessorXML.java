package ktrans;

import java.io.File;

import org.payn.chsm.Behavior;
import org.payn.chsm.Resource;
import org.payn.chsm.io.file.interpolate.ProcessorInterpolateSnapshotTable;
import org.payn.resources.particle.ResourceParticle;
import org.payn.resources.particle.cell.BehaviorConcTracker;
import org.payn.resources.particle.cell.BehaviorConcTrackerAlt;
import org.payn.resources.particle.cell.BehaviorConcTrackerLagrange;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.solute.boundary.BehaviorSoluteActiveMM;
import org.payn.resources.solute.boundary.BehaviorSoluteBoundInject;
import org.payn.resources.solute.boundary.BehaviorSoluteFlow;
import org.payn.resources.solute.boundary.BehaviorSoluteFlowBound;
import org.payn.resources.solute.cell.BehaviorSoluteStorage;

import edu.montana.cerg.simmanager.InputProcessor;
import neoch.behaviors.BehaviorMatrix;
import neoch.io.xmltools.DocumentBoundary;
import neoch.io.xmltools.DocumentCell;
import neoch.io.xmltools.ElementBehaviorMatrix;
import neoch.io.xmltools.ElementBoundary;
import neoch.io.xmltools.ElementHolonMatrix;

/**
 * Input processor for building NEO input for a simple stream solute model
 * 
 * @author v78h241
 *
 */
public class StreamBuilderNEOInputProcessorXML extends InputProcessor<StreamBuilderMetaInputXML,StreamSimulatorNEO> {

   private String particleBehaviorName;
   private Resource particleResource;
   private boolean isParticleAlt;

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
         ResourceSolute consCurrency = new ResourceSolute();
         consCurrency.initialize("cons");
         
         ResourceSolute actCurrency = new ResourceSolute();
         actCurrency.initialize("active");

         particleResource = null;
         if (metaInput.isParticle())
         {
            particleResource = new ResourceParticle();
            particleResource.initialize("particle");
         }

         // Set up the behaviors
         Behavior consBehaviorFlow = consCurrency.getBehavior(ResourceSolute.BEHAVIOR_FLOW);
         Behavior consBehaviorStorage = consCurrency.getBehavior(ResourceSolute.BEHAVIOR_STORAGE);

         Behavior actBehaviorFlow = actCurrency.getBehavior(ResourceSolute.BEHAVIOR_FLOW);
         Behavior actBehaviorStorage = actCurrency.getBehavior(ResourceSolute.BEHAVIOR_STORAGE);
         Behavior actBehaviorUptake = actCurrency.getBehavior(ResourceSolute.BEHAVIOR_ACTIVEMM);

         isParticleAlt = false;
         particleBehaviorName = null;
         Behavior behaviorParticleStorage = null;
         Behavior behaviorParticleMove = null;
         if (metaInput.isParticle())
         {
            particleBehaviorName = metaInput.getParticleBehaviorName();
            isParticleAlt = particleBehaviorName.equals(ResourceParticle.BEHAVIOR_CONC_TRACKER_ALT);
            if (isParticleAlt)
            {
               behaviorParticleStorage = particleResource.getBehavior(ResourceParticle.BEHAVIOR_PARTICLE_STORAGE);
               behaviorParticleMove = particleResource.getBehavior(ResourceParticle.BEHAVIOR_PARTICLE_MOVEMENT);
            }
         }

         // Upstream boundaries
         cellName = String.format("cell%0" + numCellsDigits.toString() + "d", 1);
         
         if (metaInput.isInject())
         {
            boundaryName = "ext_" + cellName;
            
            // Conservative tracer
            elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
            elementBehavior = elementBoundary.createBehaviorElement(
                  consCurrency.getBehavior(ResourceSolute.BEHAVIOR_CONCBOUND_INJECT)
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
                  consCurrency.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
                  consBkgConc.toString(), 
                  null
                  );
            
            // Active Tracer
            elementBehavior = elementBoundary.createBehaviorElement(
                  actCurrency.getBehavior(ResourceSolute.BEHAVIOR_CONCBOUND_INJECT)
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
                  actCurrency.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
                  activeBkgConc.toString(), 
                  null);
         }
         else
         {
            boundaryName = "ext_" + cellName;
            
            elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
            elementBehavior = elementBoundary.createBehaviorElement(
                  consCurrency.getBehavior(ResourceSolute.BEHAVIOR_CONCBOUND)
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
                  actCurrency.getBehavior(ResourceSolute.BEHAVIOR_CONCBOUND)
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
                  consCurrency.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
                  consBkgConc.toString(), 
                  null
                  );
            
            // Cell active tracer
            elementBehavior = elementCell.createBehaviorElement(actBehaviorStorage);
            elementBehavior.createInitValueElement(
                  actCurrency.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
                  activeBkgConc.toString(), 
                  null
                  );
            
            // Cell particle
            if (isParticleAlt)
            {
               elementCell.createBehaviorElement(behaviorParticleStorage);
            }
            
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
            
            // Downstream boundary particle
            if (isParticleAlt)
            {
               elementBoundary.createBehaviorElement(behaviorParticleMove);
            }
            
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
               consCurrency.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
               consBkgConc.toString(),
               "null"
               );
         
         // Last cell active tracer
         elementBehavior = elementCell.createBehaviorElement(actBehaviorStorage);
         elementBehavior.createInitValueElement(actCurrency.getName() + ResourceSolute.NAME_SOLUTE_CONC, activeBkgConc.toString(), null);
         
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
               consCurrency.getBehavior(ResourceSolute.BEHAVIOR_FLOWBOUND)
               );
         elementBehavior.createInitValueElement(BehaviorSoluteFlowBound.REQ_STATE_FLOW, flow.toString(), null);
         elementBehavior.createInitValueElement(
               consCurrency.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
               consBkgConc.toString(), 
               null
               );
         
         // Downstream boundary active tracer
         elementBehavior = elementBoundary.createBehaviorElement(
               actCurrency.getBehavior(ResourceSolute.BEHAVIOR_FLOWBOUND)
               );
         elementBehavior.createInitValueElement(
               actCurrency.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
               activeBkgConc.toString(), 
               null
               );
         
         if (metaInput.isParticle())
         {
            configureParticle(documentCell, numCellsDigits);
         }
         
         // Write the cell and boundary XML files
         documentCell.write(cellFile.getParentFile());
         documentBoundary.write(boundaryFile.getParentFile());
      }
      
      sim.loadMatrix();
   }

   /**
    * Configure the particle trackers
    * 
    * @param documentCell
    * @param numCellsDigits
    * @throws Exception
    */
   private void configureParticle(DocumentCell documentCell, Integer numCellsDigits) throws Exception 
   {
      long releaseCellNum = metaInput.getParticleReleaseCell();
      long endCellNum = metaInput.getParticleEndCell();
      
      String cellName = "cellParticle";
      ElementHolonMatrix elementCell = documentCell.createCellElement(cellName);
      ElementBehaviorMatrix elementBehaviorParticle = elementCell.createBehaviorElement(
            particleResource.getBehavior(particleBehaviorName)
            );
      if (isParticleAlt || particleBehaviorName.equals(ResourceParticle.BEHAVIOR_CONC_TRACKER_VEL))
      {
         elementBehaviorParticle.createInitValueElement(
               BehaviorConcTrackerAlt.REQ_STATE_RELEASE_COUNT, 
               metaInput.getParticleCount(), 
               null
               );
      }
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTracker.REQ_STATE_CURRENCY, 
            metaInput.getParticleCurrency(), 
            null
            );
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTracker.REQ_STATE_INTERVAL_RELEASE, 
            metaInput.getParticleReleaseInterval(), 
            null
            );
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerLagrange.REQ_STATE_INTERVAL_RECORD, 
            metaInput.getParticleRecordInterval(), 
            null
            );
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerLagrange.REQ_STATE_RELEASE_NAME, 
            String.format("cell%0" + numCellsDigits.toString() + "d", releaseCellNum), 
            null
            );
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerLagrange.REQ_STATE_END_NAME, 
            String.format("cell%0" + numCellsDigits.toString() + "d", endCellNum), 
            null
            );   
   }
}
