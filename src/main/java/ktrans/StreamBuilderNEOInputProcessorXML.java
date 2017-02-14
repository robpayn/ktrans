package ktrans;

import java.io.File;

import org.payn.chsm.Behavior;
import org.payn.chsm.Resource;
import org.payn.chsm.io.file.interpolate.ProcessorInterpolateSnapshotTable;
import org.payn.neoch.io.xmltools.DocumentBoundary;
import org.payn.neoch.io.xmltools.DocumentCell;
import org.payn.neoch.io.xmltools.ElementBehaviorMatrix;
import org.payn.neoch.io.xmltools.ElementBoundary;
import org.payn.neoch.io.xmltools.ElementHolonMatrix;
import org.payn.resources.particle.ResourceParticle;
import org.payn.resources.particle.cell.BehaviorConcTrackerVel;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.solute.boundary.BehaviorSoluteActiveMM;
import org.payn.resources.solute.boundary.BehaviorSoluteBoundInject;
import org.payn.resources.solute.boundary.BehaviorSoluteFlow;
import org.payn.resources.solute.boundary.BehaviorSoluteFlowBound;
import org.payn.resources.solute.cell.BehaviorSoluteStorage;
import org.payn.simulation.InputProcessor;

/**
 * Input processor for building NEO input for a simple stream solute model
 * 
 * @author v78h241
 *
 */
public class StreamBuilderNEOInputProcessorXML extends InputProcessor<StreamBuilderMetaInputXML,StreamSimulatorNEO> {

   private String particleBehaviorName;
   private Resource particleResource;

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
         
         // Set up the resources
         ResourceSolute consResource = new ResourceSolute();
         consResource.initialize("cons");
         
         ResourceSolute actResource = new ResourceSolute();
         actResource.initialize("active");

         particleResource = null;
         if (metaInput.isParticle())
         {
            particleResource = new ResourceParticle();
            particleResource.initialize("particle");
         }

         // Set up the behaviors
         Behavior consBehaviorFlow = consResource.getBehavior(ResourceSolute.BEHAVIOR_FLOW);
         Behavior consBehaviorStorage = consResource.getBehavior(ResourceSolute.BEHAVIOR_STORAGE);

         Behavior actBehaviorFlow = actResource.getBehavior(ResourceSolute.BEHAVIOR_FLOW);
         Behavior actBehaviorStorage = actResource.getBehavior(ResourceSolute.BEHAVIOR_STORAGE);
         Behavior actBehaviorUptake = actResource.getBehavior(ResourceSolute.BEHAVIOR_ACTIVEMM);

         particleBehaviorName = null;
         if (metaInput.isParticle())
         {
            particleBehaviorName = metaInput.getParticleBehaviorName();
         }

         // Upstream boundaries
         cellName = String.format("cell%0" + numCellsDigits.toString() + "d", 1);
         
         if (metaInput.isInject())
         {
            boundaryName = "ext_" + cellName;
            
            // Conservative tracer
            elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
            elementBehavior = elementBoundary.createBehaviorElement(
                  consResource.getBehavior(ResourceSolute.BEHAVIOR_CONCBOUND_INJECT)
                  );
            elementBehavior.createInitValueElement(
                  consResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_PATH, 
                  metaInput.getConcBoundFile(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_TYPE, 
                  metaInput.getInterpolationType(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_DELIMITER, 
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
                  consResource.getName() + BehaviorSoluteBoundInject.REQ_STATE_MASS, 
                  metaInput.getConservativeInjectMass().toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consResource.getName() + BehaviorSoluteBoundInject.REQ_STATE_DURATION, 
                  metaInput.getConservativeInjectDuration().toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consResource.getName() + BehaviorSoluteBoundInject.REQ_STATE_START, 
                  metaInput.getConservativeInjectStartInterval().toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consResource.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
                  consBkgConc.toString(), 
                  null
                  );
            
            // Active Tracer
            elementBehavior = elementBoundary.createBehaviorElement(
                  actResource.getBehavior(ResourceSolute.BEHAVIOR_CONCBOUND_INJECT)
                  );
            elementBehavior.createInitValueElement(
                  actResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_PATH, 
                  metaInput.getConcBoundFile(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  actResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_TYPE, 
                  metaInput.getInterpolationType(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  actResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_DELIMITER, 
                  metaInput.getDelimiter(), 
                  null
                  );
            if (metaInput.isActivInjUnique())
            {
               elementBehavior.createInitValueElement(
                     actResource.getName() + BehaviorSoluteBoundInject.REQ_STATE_MASS, 
                     metaInput.getActiveInjectMass().toString(), 
                     null
                     );
               elementBehavior.createInitValueElement(
                     actResource.getName() + BehaviorSoluteBoundInject.REQ_STATE_DURATION, 
                     metaInput.getActiveInjectDuration().toString(), 
                     null
                     );
               elementBehavior.createInitValueElement(
                     actResource.getName() + BehaviorSoluteBoundInject.REQ_STATE_START, 
                     metaInput.getActiveInjectStartInterval().toString(), 
                     null
                     );
            }
            else
            {
               elementBehavior.createInitValueElement(
                     actResource.getName() + BehaviorSoluteBoundInject.REQ_STATE_MASS, 
                     metaInput.getConservativeInjectMass().toString(), 
                     null
                     );
               elementBehavior.createInitValueElement(
                     actResource.getName() + BehaviorSoluteBoundInject.REQ_STATE_DURATION, 
                     metaInput.getConservativeInjectDuration().toString(), 
                     null
                     );
               elementBehavior.createInitValueElement(
                     actResource.getName() + BehaviorSoluteBoundInject.REQ_STATE_START, 
                     metaInput.getConservativeInjectStartInterval().toString(), 
                     null
                     );
            }
            elementBehavior.createInitValueElement(
                  actResource.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
                  activeBkgConc.toString(), 
                  null);
         }
         else
         {
            boundaryName = "ext_" + cellName;
            
            elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
            elementBehavior = elementBoundary.createBehaviorElement(
                  consResource.getBehavior(ResourceSolute.BEHAVIOR_CONCBOUND)
                  );
            elementBehavior.createInitValueElement(
                  consResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_PATH, 
                  metaInput.getConcBoundFile(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_TYPE, 
                  metaInput.getInterpolationType(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  consResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_DELIMITER, 
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
                  actResource.getBehavior(ResourceSolute.BEHAVIOR_CONCBOUND)
                  );
            elementBehavior.createInitValueElement(
                  actResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_PATH, 
                  metaInput.getConcBoundFile(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  actResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_TYPE, 
                  metaInput.getInterpolationType(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  actResource.getName() + ProcessorInterpolateSnapshotTable.REQ_STATE_DELIMITER, 
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
                  consResource.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
                  consBkgConc.toString(), 
                  null
                  );
            
            // Cell active tracer
            elementBehavior = elementCell.createBehaviorElement(actBehaviorStorage);
            elementBehavior.createInitValueElement(
                  actResource.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
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
               consResource.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
               consBkgConc.toString(),
               "null"
               );
         
         // Last cell active tracer
         elementBehavior = elementCell.createBehaviorElement(actBehaviorStorage);
         elementBehavior.createInitValueElement(actResource.getName() + ResourceSolute.NAME_SOLUTE_CONC, activeBkgConc.toString(), null);
         
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
               consResource.getBehavior(ResourceSolute.BEHAVIOR_FLOWBOUND)
               );
         elementBehavior.createInitValueElement(BehaviorSoluteFlowBound.REQ_STATE_FLOW, flow.toString(), null);
         elementBehavior.createInitValueElement(
               consResource.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
               consBkgConc.toString(), 
               null
               );
         
         // Downstream boundary active tracer
         elementBehavior = elementBoundary.createBehaviorElement(
               actResource.getBehavior(ResourceSolute.BEHAVIOR_FLOWBOUND)
               );
         elementBehavior.createInitValueElement(
               actResource.getName() + ResourceSolute.NAME_SOLUTE_CONC, 
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
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerVel.REQ_STATE_RESOURCE, 
            metaInput.getParticleResource(), 
            null
            );
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerVel.REQ_STATE_INTERVAL_RELEASE, 
            metaInput.getParticleReleaseInterval(), 
            null
            );
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerVel.REQ_STATE_INTERVAL_RECORD, 
            metaInput.getParticleRecordInterval(), 
            null
            );
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerVel.REQ_STATE_RELEASE_NAME, 
            String.format("cell%0" + numCellsDigits.toString() + "d", releaseCellNum), 
            null
            );
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerVel.REQ_STATE_END_NAME, 
            String.format("cell%0" + numCellsDigits.toString() + "d", endCellNum), 
            null
            );   
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerVel.REQ_STATE_VEL_FILE, 
            metaInput.getParticleVelocityFile(), 
            null
            );   
      elementBehaviorParticle.createInitValueElement(
            BehaviorConcTrackerVel.REQ_STATE_OUTPUT_LOC, 
            metaInput.getParticleOutputLocation(), 
            null
            );   
   }
}
