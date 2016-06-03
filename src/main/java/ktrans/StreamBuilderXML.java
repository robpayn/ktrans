package ktrans;

import java.io.File;

import org.w3c.dom.Element;

import currencies.solute.CurrencySolute;
import neolite.behaviors.BehaviorMatrix;
import neolite.io.xml.DocumentBoundary;
import neolite.io.xml.DocumentCell;
import neolite.io.xml.ElementBehaviorMatrix;
import neolite.io.xml.ElementBoundary;
import neolite.io.xml.ElementHolonMatrix;
import statemachine.io.xml.XMLDocument;

public class StreamBuilderXML extends StreamBuilder {

   private XMLDocument document;
   
   private StreamBuilderElement element;

   private File workingDir;

   public StreamBuilderXML(File configFile, File workingDir) throws Exception
   {
      document = new XMLDocument(configFile);
      element = new StreamBuilderElement((Element)document.getRootElement().getElementsByTagName("streambuilder").item(0));
      this.workingDir = workingDir;
   }
   
   @Override
   public void build() throws Exception 
   {
      System.out.println("Building the stream matrix files...");
      long numCells = element.getNumCells();
      Double boundaryLength = new Double(element.getLength() / (double)numCells);
      Double boundaryArea = new Double(element.getWidth() * element.getDepth());
      Double storageVolume = boundaryLength * boundaryArea;
      Double upstreamConc = new Double(element.getUpstreamConc());
      Double flow = new Double(-element.getFlow());
      Double disp = new Double(element.getDispersion());
      
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
      BehaviorMatrix behaviorFlow = currency.getBehavior("BehaviorSoluteFlow");
      BehaviorMatrix behaviorStorage = currency.getBehavior("BehaviorSoluteStorage");
      BehaviorMatrix behaviorBound = currency.getBehavior("BehaviorSoluteFlowBound");
      
      for (int i = 1; i < numCells; i++)
      {
         cellName = String.format("cell%0" + numCellsDigits.toString() + "d", i);
         elementCell = documentCell.createCellElement(cellName);
         elementBehavior = elementCell.createBehaviorElement(behaviorStorage);
         elementBehavior.createInitValueElement("WaterStorage", storageVolume.toString(), null);
         if (i == 1)
         {
            elementBehavior.createInitValueElement("SoluteConc", upstreamConc.toString(), null);
         }
         else
         {
            elementBehavior.createInitValueElement("SoluteConc", "0", null);
         }
         
         boundaryName = cellName + String.format("_%0" + numCellsDigits.toString() + "d", i + 1);
         elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
         elementBehavior = elementBoundary.createBehaviorElement(behaviorFlow);
         elementBehavior.createInitValueElement("Length", boundaryLength.toString(), null);
         elementBehavior.createInitValueElement("AreaXSect", boundaryArea.toString(), null);
         elementBehavior.createInitValueElement("WaterFlow", flow.toString(), null);
         elementBehavior.createInitValueElement("DispCoeff", disp.toString(), null);
         
         cellName = String.format("cell%0" + numCellsDigits.toString() + "d", i + 1);
         boundaryName = cellName + String.format("_%0" + numCellsDigits.toString() + "d", i);
         elementBoundary = elementBoundary.createAdjacentElement(boundaryName, cellName);
      }
      cellName = String.format("cell%0" + numCellsDigits.toString() + "d", numCells);
      elementCell = documentCell.createCellElement(cellName);
      elementBehavior = elementCell.createBehaviorElement(behaviorStorage);
      elementBehavior.createInitValueElement("WaterStorage", storageVolume.toString(), null);
      elementBehavior.createInitValueElement("SoluteConc", "0", null);
      
      boundaryName = cellName + String.format("_ext", numCells);
      elementBoundary = documentBoundary.createBoundaryElement(boundaryName, cellName);
      elementBehavior = elementBoundary.createBehaviorElement(behaviorBound);
      elementBehavior.createInitValueElement("WaterFlow", flow.toString(), null);
      
      documentCell.write(new File(workingDir.getAbsolutePath() + File.separator + "input"));
      documentBoundary.write(new File(workingDir.getAbsolutePath() + File.separator + "input"));
   }

}
