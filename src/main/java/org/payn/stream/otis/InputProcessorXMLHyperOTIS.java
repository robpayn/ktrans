package org.payn.stream.otis;

import java.io.File;

import org.payn.chsm.Behavior;
import org.payn.chsm.io.interpolate.InterpolatorSnapshotTable;
import org.payn.chsm.io.xmltools.ElementBehavior;
import org.payn.chsm.io.xmltools.ElementHolon;
import org.payn.neoch.io.xmltools.ElementBoundary;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.solute.concentration.ResourceSoluteConcentration;
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
         SimulatorStream simulator = new SimulatorStream(workingDir, args);
         
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
   private ResourceSoluteConcentration conserveResourceOTIS;
   
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
    * Flag indicating if the active tracer is configured
    * and active
    */
   private boolean isActiveConfigured;

   /**
    * The resource for the active tracer
    */
   private ResourceSoluteConcentration activeResourceOTIS;

   /**
    * The storage behavior for the active tracer
    */
   private Behavior activeBehaviorStorage;

   /**
    * The background concentration of the active tracer
    */
   private Double activeBkgConc;

   /**
    * Maximum uptake of the active tracer (hyperbolic function)
    */
   private Double uptakeMax;

   /**
    * Concentration at half the maximum uptake (hyperbolic function)
    */
   private Double halfSat;

   /**
    * Behavior for uptake with hyperbolic kinetics
    */
   private Behavior activeBehaviorStorageUptake;

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
      xSectionalArea = averageWidth * initialDepth;
      
      conserveBehaviorStorage = conserveResourceOTIS.getBehavior(
            ResourceSolute.BEHAVIOR_STORAGE
            );
      conserveBehaviorFlow = conserveResourceOTIS.getBehavior(
            ResourceSolute.BEHAVIOR_FLOW
            );
      conserveBkgConc = metaInput.getAttributeBkgConc("conservative");
      ElementBehavior elementBehavior = 
            documentHolon.createDefaultBehaviorElement(conserveBehaviorStorage);
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
     
      if (isActiveConfigured)
      {
         activeBehaviorStorage = activeResourceOTIS.getBehavior(
               ResourceSolute.BEHAVIOR_STORAGE
               );
         activeBehaviorStorageUptake = activeResourceOTIS.getBehavior(
               ResourceSolute.BEHAVIOR_STORAGE_HYPER
               );
         activeBkgConc = metaInput.getAttributeBkgConc("active");
         uptakeMax = metaInput.getAttributeUptakeMax("active");
         halfSat = metaInput.getAttributeConcHalfSat("active");
         elementBehavior = 
               documentHolon.createDefaultBehaviorElement(activeBehaviorStorage);
         elementBehavior.createInitValueElement(
               activeBehaviorStorage.getAbstractStateName(ResourceSolute.NAME_SOLUTE_CONC), 
               activeBkgConc.toString(), 
               null
               );
         elementBehavior = 
               documentHolon.createDefaultBehaviorElement(activeBehaviorStorageUptake);
         elementBehavior.createInitValueElement(
               activeBehaviorStorage.getAbstractStateName(ResourceSolute.NAME_UPTAKE_MAX), 
               uptakeMax.toString(), 
               null
               );
         elementBehavior.createInitValueElement(
               activeBehaviorStorage.getAbstractStateName(ResourceSolute.NAME_CONC_HALF_SAT), 
               halfSat.toString(), 
               null
               );
         elementBehavior.createInitValueElement(
               activeBehaviorStorage.getAbstractStateName(ResourceSolute.NAME_BKG_CONC), 
               activeBkgConc.toString(), 
               null
               );
         elementBehavior.createInitValueElement(
               ResourceSolute.NAME_DEPTH, 
               initialDepth.toString(), 
               null
               );
      }
   }

   @Override
   protected void configureStreamCell(ElementHolon elementCell,
         long index) 
   {
      elementCell.createBehaviorElement(conserveBehaviorStorage);
      
      if (isActiveConfigured)
      {
         elementCell.createBehaviorElement(activeBehaviorStorage);         
         elementCell.createBehaviorElement(activeBehaviorStorageUptake);         
      }
      
   }

   @Override
   protected void configureStreamBoundary(ElementBoundary elementBoundary,
         ElementBoundary elementBoundaryAdj, int index) 
   {
      ElementBehavior elementBehavior = 
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
      boolean isInject = metaInput.isUpstreamInject("conservative");
      Behavior behavior = null;
      if (isInject)
      {
         behavior = conserveResourceOTIS.getBehavior(
               ResourceSolute.BEHAVIOR_CONCBOUND_INJECT);
      }
      else
      {
         behavior = conserveResourceOTIS.getBehavior(
               ResourceSolute.BEHAVIOR_CONCBOUND);
      }
      ElementBehavior elementBehavior = 
            elementBoundary.createBehaviorElement(behavior);
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(InterpolatorSnapshotTable.NAME_PATH), 
            metaInput.getAttributeConcBoundFile("conservative"), 
            null
            );
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(InterpolatorSnapshotTable.NAME_TYPE), 
            metaInput.getAttributeInterpolationType("conservative"), 
            null
            );
      elementBehavior.createInitValueElement(
            behavior.getAbstractStateName(InterpolatorSnapshotTable.NAME_DELIMITER),  
            metaInput.getAttributeDelimiter("conservative"), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceSolute.NAME_WATER_FLOW, 
            initialFlow.toString(), 
            null
            );
      if (isInject)
      {
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
      
      if (isActiveConfigured)
      {
         if (isInject)
         {
            behavior = activeResourceOTIS.getBehavior(
                  ResourceSolute.BEHAVIOR_CONCBOUND_INJECT);
         }
         else
         {
            behavior = activeResourceOTIS.getBehavior(
                  ResourceSolute.BEHAVIOR_CONCBOUND);
         }
         elementBehavior = 
               elementBoundary.createBehaviorElement(behavior);
         elementBehavior.createInitValueElement(
               behavior.getAbstractStateName(InterpolatorSnapshotTable.NAME_PATH), 
               metaInput.getAttributeConcBoundFile("active"), 
               null
               );
         elementBehavior.createInitValueElement(
               behavior.getAbstractStateName(InterpolatorSnapshotTable.NAME_TYPE), 
               metaInput.getAttributeInterpolationType("active"), 
               null
               );
         elementBehavior.createInitValueElement(
               behavior.getAbstractStateName(InterpolatorSnapshotTable.NAME_DELIMITER),  
               metaInput.getAttributeDelimiter("active"), 
               null
               );
         if (isInject)
         {
            elementBehavior.createInitValueElement(
                  behavior.getAbstractStateName(ResourceSolute.NAME_INJECT_MASS), 
                  metaInput.getAttributeInjectMass("active").toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  behavior.getAbstractStateName(ResourceSolute.NAME_INJECT_DURATION), 
                  metaInput.getAttributeInjectDuration("active").toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  behavior.getAbstractStateName(ResourceSolute.NAME_INJECT_START), 
                  metaInput.getAttributeInjectStartInterval("active").toString(), 
                  null
                  );
            elementBehavior.createInitValueElement(
                  behavior.getAbstractStateName(ResourceSolute.NAME_SOLUTE_CONC),
                  activeBkgConc.toString(), 
                  null
                  );
         }
      }
   }

   @Override
   protected void configureDownstreamBoundary(ElementBoundary elementBoundary,
         long indexLastCell) throws Exception 
   {
      Behavior behavior = conserveResourceOTIS.getBehavior(
            ResourceSolute.BEHAVIOR_FLOWBOUND);
      ElementBehavior elementBehavior = 
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
      
      if (isActiveConfigured)
      {
         behavior = activeResourceOTIS.getBehavior(
               ResourceSolute.BEHAVIOR_FLOWBOUND);
         elementBehavior = 
               elementBoundary.createBehaviorElement(behavior);
         elementBehavior.createInitValueElement(
               behavior.getAbstractStateName(ResourceSolute.NAME_SOLUTE_CONC), 
               activeBkgConc.toString(), 
               null
               );
      }
   }

   @Override
   protected void configureResources() throws Exception 
   {
      conserveResourceOTIS = new ResourceSoluteConcentration();
      conserveResourceOTIS.initialize("conserveOTIS");
      
      isActiveConfigured = metaInput.isSoluteConfigured("active");
      if (isActiveConfigured)
      {
        activeResourceOTIS = new ResourceSoluteConcentration();
        activeResourceOTIS.initialize("activeOTIS");
      }
   }

}
