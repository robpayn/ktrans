package org.payn.stream.otis;

import java.io.File;

import org.payn.chsm.Behavior;
import org.payn.chsm.io.file.interpolate.InterpolatorSnapshotTable;
import org.payn.neoch.io.xmltools.ElementBehaviorMatrix;
import org.payn.neoch.io.xmltools.ElementBoundary;
import org.payn.neoch.io.xmltools.ElementHolonMatrix;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.solute.ResourceSoluteOTIS;
import org.payn.stream.InputProcessorXMLStreamBuilder;
import org.payn.stream.SimulatorStream;

/**
 * Input processer for building a stream model for one-dimensional transport
 * based on direct concentration calculations.
 * 
 * @author robpayn
 *
 */
public class InputProcessorXMLHyperOTIS extends InputProcessorXMLStreamBuilder<MetaInputXMLHyperOTIS>{

   /**
    * Entry point for stream simulations
    * 
    * @param args
    *       command line arguments
    */
   public static void main(String[] args)
   {
      try 
      {
         File workingDir = new File(System.getProperty("user.dir"));
         SimulatorStream simulator = new SimulatorStream(args, workingDir);
         
         // Check for configuration file in file system
         if (!simulator.getArgMap().containsKey("config"))
         {
            throw new Exception(
                  "Must provide an argument for configuration file relative to working directory " +
                        "(e.g. 'config=./config/config.xml')"
                  );
         }

         simulator.getInputProcessorFactory().addHyperOTISBuilderInputProcessor(
               workingDir, simulator.getArgMap().get("config")
               );
         simulator.execute();
      } 
      catch (Exception e) 
      {
         e.printStackTrace();
      }
   }

   /**
    * Solute resource for a conservative tracer
    */
   private ResourceSoluteOTIS conserveResourceOTIS;
   
   /**
    * Storage behavior for the conservative tracer
    */
   private Behavior conserveBehaviorStorage;
   
   /**
    * Background concentration of conservative tracer
    */
   private Double conserveBkgConc;
   
   /**
    * Flow behavior for conservative tracer
    */
   private Behavior conserveBehaviorFlow;
   
   /**
    * Cross-sectional area of the flow
    */
   private Double xSectionalArea;

   /**
    * Construct a new instance based on the provided meta input
    * 
    * @param metaInput
    * @param simulator
    */
   public InputProcessorXMLHyperOTIS(MetaInputXMLHyperOTIS metaInput,
         SimulatorStream simulator) 
   {
      super(metaInput, simulator);
   }

   @Override
   protected void configureStreamLoop() throws Exception 
   {
      conserveBehaviorStorage = conserveResourceOTIS.getBehavior(
            ResourceSolute.BEHAVIOR_STORAGE);
      conserveBkgConc = metaInput.getAttributeBkgConc("conservative");

      conserveBehaviorFlow = conserveResourceOTIS.getBehavior(
            ResourceSolute.BEHAVIOR_FLOW);
      
      xSectionalArea = averageWidth * initialDepth;
   }

   @Override
   protected void configureStreamCell(ElementHolonMatrix elementCell,
         long index) 
   {
      ElementBehaviorMatrix elementBehavior = 
            elementCell.createBehaviorElement(conserveBehaviorStorage);
      elementBehavior.createInitValueElement(
            conserveBehaviorStorage.getAbstractStateName(ResourceSolute.NAME_SOLUTE_CONC), 
            conserveBkgConc.toString(), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceSolute.NAME_WATER_FLOW, 
            initialFlow.toString(), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceSolute.NAME_AREA_XSECT, 
            xSectionalArea.toString(), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceSolute.NAME_LENGTH, 
            cellLength.toString(), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceSolute.NAME_DISPERSION_COEFF, 
            dispersionCoeff.toString(), 
            null
            );
   }

   @Override
   protected void configureStreamBoundary(ElementBoundary elementBoundary,
         ElementBoundary elementBoundaryAdj, int index) 
   {
      ElementBehaviorMatrix elementBehavior = 
            elementBoundary.createBehaviorElement(conserveBehaviorFlow);
      elementBehavior.createInitValueElement(
            ResourceSolute.NAME_WATER_FLOW, 
            initialFlow.toString(), 
            null
            );
      elementBehavior = 
            elementBoundary.getAdjacentBoundElement().createBehaviorElement(conserveBehaviorFlow);
      elementBehavior.createInitValueElement(
            ResourceSolute.NAME_WATER_FLOW,
            Double.toString(-initialFlow), 
            null
            );
   }

   @Override
   protected void configureUpstreamBoundary(ElementBoundary elementBoundary,
         int indexFirstCell) throws Exception 
   {
      Behavior behavior = conserveResourceOTIS.getBehavior(
            ResourceSolute.BEHAVIOR_CONCBOUND_INJECT);
      ElementBehaviorMatrix elementBehavior = 
            elementBoundary.createBehaviorElement(behavior);
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(InterpolatorSnapshotTable.REQ_STATE_PATH), 
            metaInput.getAttributeConcBoundFile("conservative"), 
            null
            );
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(InterpolatorSnapshotTable.REQ_STATE_TYPE), 
            metaInput.getAttributeInterpolationType("conservative"), 
            null
            );
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(InterpolatorSnapshotTable.REQ_STATE_DELIMITER),  
            metaInput.getAttributeDelimiter("conservative"), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceSolute.NAME_WATER_FLOW, 
            initialFlow.toString(), 
            null
            );
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(ResourceSolute.NAME_INJECT_MASS), 
            metaInput.getAttributeInjectMass("conservative").toString(), 
            null
            );
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(ResourceSolute.NAME_INJECT_DURATION), 
            metaInput.getAttributeInjectDuration("conservative").toString(), 
            null
            );
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(ResourceSolute.NAME_INJECT_START), 
            metaInput.getAttributeInjectStartInterval("conservative").toString(), 
            null
            );
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(ResourceSolute.NAME_SOLUTE_CONC),
            conserveBkgConc.toString(), 
            null
            );
   }

   @Override
   protected void configureDownstreamBoundary(ElementBoundary elementBoundary,
         long indexLastCell) throws Exception 
   {
      Behavior behavior = conserveResourceOTIS.getBehavior(
            ResourceSolute.BEHAVIOR_FLOWBOUND);
      ElementBehaviorMatrix elementBehavior = 
            elementBoundary.createBehaviorElement(behavior);
      elementBehavior.createInitValueElement(
            ResourceSolute.NAME_WATER_FLOW, 
            Double.toString(-initialFlow), 
            null
            );
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(ResourceSolute.NAME_SOLUTE_CONC), 
            conserveBkgConc.toString(), 
            null
            );
   }

   @Override
   protected void configureResources() throws Exception 
   {
      conserveResourceOTIS = new ResourceSoluteOTIS();
      conserveResourceOTIS.initialize("conserveOTIS");
   }

}
