package ktrans;

import java.io.File;

import currencies.solute.CurrencySolute;
import currencies.solute.boundary.flow.BehaviorSoluteFlow;
import currencies.solute.boundary.flowbound.BehaviorSoluteFlowBound;
import currencies.solute.boundary.inject.BehaviorSoluteInject;
import currencies.solute.cell.storage.BehaviorSoluteStorage;
import currencies.solute.cell.storage.SoluteConc;
import edu.montana.cerg.simmanager.InputProcessor;
import neolite.behaviors.BehaviorMatrix;
import neolite.io.xml.DocumentBoundary;
import neolite.io.xml.DocumentCell;
import neolite.io.xml.ElementBehaviorMatrix;
import neolite.io.xml.ElementBoundary;
import neolite.io.xml.ElementHolonMatrix;

public class StreamBuilderNEOInputProcessorXML extends InputProcessor<StreamBuilderMetaInputXML,StreamSimulatorNEO> {

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
         Long numCells = metaInput.getNumCells();
         Double boundaryLength = new Double(metaInput.getLength() / (double)numCells);
         Double boundaryArea = metaInput.getWidth() * metaInput.getDepth();
         Double storageVolume = boundaryLength * boundaryArea;
         Double flow = -metaInput.getFlow();
         Double disp = metaInput.getDispersion();
         
         DocumentCell documentCell = new DocumentCell();
         DocumentBoundary documentBoundary = new DocumentBoundary();
         
         Integer numCellsDigits = new Integer(1 + (int)Math.log10(numCells));
         String cellName = "";
         String boundaryName = "";
         ElementHolonMatrix elementCell = null;
         ElementBoundary elementBoundary = null;
         ElementBehaviorMatrix elementBehavior = null;
         CurrencySolute currency = new CurrencySolute();
         currency.setName("solute");
         BehaviorMatrix behaviorFlow = currency.getBehavior(BehaviorSoluteFlow.class.getSimpleName());
         BehaviorMatrix behaviorStorage = currency.getBehavior(BehaviorSoluteStorage.class.getSimpleName());
         
         cellName = String.format("cell%0" + numCellsDigits.toString() + "d", 1);
         boundaryName = "ext_" + cellName;
         elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
         elementBehavior = elementBoundary.createBehaviorElement(
               currency.getBehavior(BehaviorSoluteInject.class.getSimpleName())
               );
         elementBehavior.createInitValueElement(
               BehaviorSoluteInject.REQ_STATE_MASS, 
               metaInput.getInjectMass().toString(), 
               null
               );
         elementBehavior.createInitValueElement(
               BehaviorSoluteInject.REQ_STATE_DURATION, 
               metaInput.getInjectDuration().toString(), 
               null
               );
         elementBehavior.createInitValueElement(
               BehaviorSoluteInject.REQ_STATE_START, 
               metaInput.getInjectStartTime().toString(), 
               null
               );
          
         for (int i = 1; i < numCells; i++)
         {
            cellName = String.format("cell%0" + numCellsDigits.toString() + "d", i);
            elementCell = documentCell.createCellElement(cellName);
            elementBehavior = elementCell.createBehaviorElement(behaviorStorage);
            elementBehavior.createInitValueElement(BehaviorSoluteStorage.REQ_STATE_VOLUME, storageVolume.toString(), null);
            elementBehavior.createInitValueElement(SoluteConc.class.getSimpleName(), "0", null);
            
            boundaryName = cellName + String.format("_%0" + numCellsDigits.toString() + "d", i + 1);
            elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
            elementBehavior = elementBoundary.createBehaviorElement(behaviorFlow);
            elementBehavior.createInitValueElement(BehaviorSoluteFlow.REQ_STATE_LENGTH, boundaryLength.toString(), null);
            elementBehavior.createInitValueElement(BehaviorSoluteFlow.REQ_STATE_AREA_XSECT, boundaryArea.toString(), null);
            elementBehavior.createInitValueElement(BehaviorSoluteFlow.REQ_STATE_FLOW, flow.toString(), null);
            elementBehavior.createInitValueElement(BehaviorSoluteFlow.REQ_STATE_DISP, disp.toString(), null);
            
            cellName = String.format("cell%0" + numCellsDigits.toString() + "d", i + 1);
            boundaryName = cellName + String.format("_%0" + numCellsDigits.toString() + "d", i);
            elementBoundary = elementBoundary.createAdjacentElement(boundaryName, cellName);
         }
         cellName = String.format("cell%0" + numCellsDigits.toString() + "d", numCells);
         elementCell = documentCell.createCellElement(cellName);
         elementBehavior = elementCell.createBehaviorElement(behaviorStorage);
         elementBehavior.createInitValueElement(BehaviorSoluteStorage.REQ_STATE_VOLUME, storageVolume.toString(), null);
         elementBehavior.createInitValueElement(SoluteConc.class.getSimpleName(), "0", null);
         
         boundaryName = cellName + String.format("_ext", numCells);
         elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
         elementBehavior = elementBoundary.createBehaviorElement(
               currency.getBehavior(BehaviorSoluteFlowBound.class.getSimpleName())
               );
         elementBehavior.createInitValueElement(BehaviorSoluteFlowBound.REQ_STATE_FLOW, flow.toString(), null);
         
         documentCell.write(new File(metaInput.getWorkingDir().getAbsolutePath() + File.separator + "input"));
         documentBoundary.write(new File(metaInput.getWorkingDir().getAbsolutePath() + File.separator + "input"));
      }
      sim.loadMatrix();
   }
}
