package org.payn.stream.metabolism;

import java.io.File;

import org.payn.chsm.Behavior;
import org.payn.chsm.io.interpolate.InterpolatorSnapshotTable;
import org.payn.chsm.io.xmltools.ElementBehavior;
import org.payn.chsm.io.xmltools.ElementHolon;
import org.payn.neoch.io.xmltools.ElementBoundary;
import org.payn.resources.water.ResourceWater;
import org.payn.stream.InputProcessorXMLStreamBuilder;
import org.payn.stream.SimulatorStream;

/**
 * An input processor for building stream metabolism models
 * 
 * @author robpayn
 *
 */
public class InputProcessorXMLMetabolismBuilder 
      extends InputProcessorXMLStreamBuilder<MetaInputXMLMetabolism> {

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

         simulator.getInputProcessorFactory().addMetabolismBuilderInputProcessor(
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
    * Water resource
    */
   private ResourceWater resourceWater;
   
   /**
    * Water storage behavior
    */
   private Behavior behaviorChannelStorage;

   /**
    * Water movement behavior
    */
   private Behavior behaviorDynamicWave;

   /**
    * Friction behavior
    */
   private Behavior behaviorWieleFriction;

   /**
    * Flag for configuration of wiele friction behavior
    */
   private boolean isWieleConfigured;

   /**
    * Construct a new instance with the given meta input and simulator
    * @param metaInput
    * @param sim
    */
   public InputProcessorXMLMetabolismBuilder(MetaInputXMLMetabolism metaInput,
         SimulatorStream sim) 
   {
      super(metaInput, sim);
   }

   @Override
   protected void configureResources() throws Exception 
   {
      resourceWater = new ResourceWater();
      resourceWater.initialize("water");
   }

   @Override
   protected void configureStreamLoop() throws Exception 
   {
      // Create behaviors
      behaviorChannelStorage = 
            resourceWater.getBehavior(ResourceWater.BEHAVIOR_CHANNEL_STORAGE);
      behaviorDynamicWave =
            resourceWater.getBehavior(ResourceWater.BEHAVIOR_DYNAMIC_WAVE);
      isWieleConfigured = metaInput.isWieleConfigured();
      behaviorWieleFriction =
            resourceWater.getBehavior(ResourceWater.BEHAVIOR_WIELE_FRICTION);
      
      // Set up default cell states
      ElementBehavior elementBehavior =
            documentCell.createDefaultBehaviorElement(behaviorChannelStorage);
      if (isInitialConditions)
      {
         elementBehavior.setInitTable(
               metaInput.getAttributeInitialConditionPathCell(),
               metaInput.getAttributeInitialConditionDelimiterCell()
               ); 
         elementBehavior.createInitValueElement(
               "WaterHead", 
               "",
               null
               );
      }
      elementBehavior.createInitValueElement(
            ResourceWater.DEFAULT_NAME_COORD_Y, 
            Double.toString(0), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.DEFAULT_NAME_LENGTH, 
            Double.toString(cellLength), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.DEFAULT_NAME_ACTIVE_CHANNEL_WIDTH_AVG, 
            Double.toString(averageWidth), 
            null
            );
         
      // Set up default boundary states
      elementBehavior =
            documentBoundary.createDefaultBehaviorElement(behaviorDynamicWave);
      if (isInitialConditions)
      {
         elementBehavior.setInitTable(
               metaInput.getAttributeInitialConditionPathBound(),
               metaInput.getAttributeInitialConditionDelimiterBound()
               ); 
         elementBehavior.createInitValueElement(
               "WaterFlow", 
               "",
               null
               );
         elementBehavior.createInitValueElement(
               "Velocity", 
               "",
               null
               );
      }
      else 
      {
         elementBehavior.createInitValueElement(
               ResourceWater.DEFAULT_NAME_FLOW, 
               Double.toString(initialFlow), 
               null
               );
      }
      elementBehavior.createInitValueElement(
            ResourceWater.DEFAULT_NAME_ACTIVE_CHANNEL_WIDTH_AVG, 
            Double.toString(averageWidth), 
            null
            );
      if (isWieleConfigured)
      {
         elementBehavior =
               documentBoundary.createDefaultBehaviorElement(behaviorWieleFriction);
         elementBehavior.createInitValueElement(
               ResourceWater.DEFAULT_NAME_WIELE_MODEL_INTERCEPT, 
               Double.toString(wieleInt), 
               null
               );
         elementBehavior.createInitValueElement(
               ResourceWater.DEFAULT_NAME_WIELE_MODEL_SLOPE, 
               Double.toString(wieleSlope), 
               null
               );
      }
      else
      {
         elementBehavior.createInitValueElement(
               ResourceWater.DEFAULT_NAME_CHEZEY,
               metaInput.getAttributeChezey().toString(),
               null
               );
      }
   }

   @Override
   protected void configureStreamCell(ElementHolon elementCell, long index) 
   {
      ElementBehavior elementBehavior = 
            elementCell.createBehaviorElement(behaviorChannelStorage);
      double distance = (cellLength * index) - (cellLength / 2);
      elementBehavior.createInitValueElement(
            ResourceWater.DEFAULT_NAME_COORD_X, 
            Double.toString(distance), 
            null
            );
      double bedElevation = elevationDatum + bedSlope * (streamLength - distance);
      elementBehavior.createInitValueElement(
            ResourceWater.DEFAULT_NAME_BED_ELEV, 
            Double.toString(bedElevation), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.DEFAULT_NAME_BANK_ELEV, 
            Double.toString(bedElevation + activeDepth), 
            null
            );
      if (!metaInput.isInitialConditions())
      {
         elementBehavior.createInitValueElement(
               ResourceWater.DEFAULT_NAME_HEAD, 
               Double.toString(bedElevation + initialDepth), 
               null
               );
      }
   }

   @Override
   protected void configureStreamBoundary(ElementBoundary elementBoundary,
         ElementBoundary elementBoundaryAdj, int index) 
   {
      elementBoundary.createBehaviorElement(behaviorDynamicWave);
      if (isWieleConfigured)
      {
         elementBoundary.createBehaviorElement(behaviorWieleFriction);
      }
   }

   @Override
   protected void configureUpstreamBoundary(ElementBoundary elementBoundary,
         int indexFirstCell) throws Exception 
   {
      ElementBehavior elementBehavior =
            elementBoundary.createBehaviorElement(
                  this.resourceWater.getBehavior(ResourceWater.BEHAVIOR_FLOW_INTERPOLATE)
                  );
      elementBehavior.createInitValueElement(
            InterpolatorSnapshotTable.NAME_DELIMITER, 
            metaInput.getAttributeUpstreamFlowDelimiter(), 
            null
            );
      elementBehavior.createInitValueElement(
            InterpolatorSnapshotTable.NAME_PATH, 
            metaInput.getAttributeUpstreamFlowPath(), 
            null
            );
      elementBehavior.createInitValueElement(
            InterpolatorSnapshotTable.NAME_TYPE, 
            metaInput.getAttributeUpstreamInterpType(), 
            null
            );
   }

   @Override
   protected void configureDownstreamBoundary(ElementBoundary elementBoundary,
         long indexLastCell) throws Exception 
   {
      ElementBehavior elementBehavior =
            elementBoundary.createBehaviorElement(
                  this.resourceWater.getBehavior(ResourceWater.BEHAVIOR_DYNAMIC_WAVE_DOWNSTREAM)
                  );
      String boundaryName = String.format(
            "%s%0" + numCellsDigits.toString() + "d_%0" + numCellsDigits.toString() + "d", 
            boundaryNameRoot,
            indexLastCell,
            indexLastCell - 1
            );
      elementBehavior.createInitValueElement(
            ResourceWater.DEFAULT_NAME_UPSTREAM_BOUNDARY_NAME, 
            boundaryName, 
            null
            );
   }

}
