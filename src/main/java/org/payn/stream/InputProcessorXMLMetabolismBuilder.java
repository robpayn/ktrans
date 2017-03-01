package org.payn.stream;

import org.payn.chsm.Behavior;
import org.payn.chsm.io.file.initialize.InitialConditionTable;
import org.payn.chsm.io.file.interpolate.ProcessorInterpolateSnapshotTable;
import org.payn.neoch.io.xmltools.ElementBehaviorMatrix;
import org.payn.neoch.io.xmltools.ElementBoundary;
import org.payn.neoch.io.xmltools.ElementHolonMatrix;
import org.payn.resources.water.ResourceWater;

/**
 * An input processor for building stream metabolism models
 * 
 * @author robpayn
 *
 */
public class InputProcessorXMLMetabolismBuilder 
      extends InputProcessorXMLStreamBuilder<MetaInputXMLMetabolism> {

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
   private Behavior behaviorDynamicWaveWiele;

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
      behaviorChannelStorage = 
            resourceWater.getBehavior(ResourceWater.BEHAVIOR_CHANNEL_STORAGE);
      behaviorDynamicWaveWiele =
            resourceWater.getBehavior(ResourceWater.BEHAVIOR_DYNAMIC_WAVE_WIELE);
      
   }

   @Override
   protected void configureStreamCell(ElementHolonMatrix elementCell, long index) 
   {
      ElementBehaviorMatrix elementBehavior = 
            elementCell.createBehaviorElement(behaviorChannelStorage);
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_LENGTH, 
            Double.toString(cellLength), 
            null
            );
      double distance = (cellLength * index) - (cellLength / 2);
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_COORDINATE_X, 
            Double.toString(distance), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_COORDINATE_Y, 
            Double.toString(0), 
            null
            );
      double bedElevation = elevationDatum + bedSlope * (streamLength - distance);
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_BED_ELEVATION, 
            Double.toString(bedElevation), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_BANK_ELEVATION, 
            Double.toString(bedElevation + activeDepth), 
            null
            );
      if (isInitialConditions)
      {
         elementBehavior.createInitValueElement(
               InitialConditionTable.NAME_INITIAL_CONDITION_PATH, 
               metaInput.getAttributeInitialConditionPathCell(),
               null
               );
         elementBehavior.createInitValueElement(
               InitialConditionTable.NAME_INITIAL_CONDITION_DELIMITER, 
               metaInput.getAttributeInitialConditionDelimiterCell(),
               null
               );
      }
      else
      {
         elementBehavior.createInitValueElement(
               ResourceWater.NAME_WATER_HEAD, 
               Double.toString(bedElevation + initialDepth), 
               null
               );
      }
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_ACTIVE_CHANNEL_WIDTH_AVERAGE, 
            Double.toString(averageWidth), 
            null
            );
   }

   @Override
   protected void configureStreamBoundary(ElementBoundary elementBoundary,
         ElementBoundary elementBoundaryAdj, int index) 
   {
      ElementBehaviorMatrix elementBehavior = 
            elementBoundary.createBehaviorElement(behaviorDynamicWaveWiele);
      if (isInitialConditions)
      {
         elementBehavior.createInitValueElement(
               InitialConditionTable.NAME_INITIAL_CONDITION_PATH, 
               metaInput.getAttributeInitialConditionPathBound(),
               null
               );
         elementBehavior.createInitValueElement(
               InitialConditionTable.NAME_INITIAL_CONDITION_DELIMITER, 
               metaInput.getAttributeInitialConditionDelimiterBound(),
               null
               );
      }
      else
      {
         elementBehavior.createInitValueElement(
               ResourceWater.NAME_WATER_FLOW, 
               Double.toString(initialFlow), 
               null
               );
      }
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_WIELE_MODEL_INTERCEPT, 
            Double.toString(wieleInt), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_WIELE_MODEL_SLOPE, 
            Double.toString(wieleSlope), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_ACTIVE_CHANNEL_WIDTH_AVERAGE, 
            Double.toString(averageWidth), 
            null
            );
   }

   @Override
   protected void configureUpstreamBoundary(ElementBoundary elementBoundary,
         int indexFirstCell) throws Exception 
   {
      ElementBehaviorMatrix elementBehavior =
            elementBoundary.createBehaviorElement(
                  this.resourceWater.getBehavior(ResourceWater.BEHAVIOR_FLOW_INTERPOLATE)
                  );
      elementBehavior.createInitValueElement(
            ProcessorInterpolateSnapshotTable.REQ_STATE_DELIMITER, 
            metaInput.getAttributeUpstreamFlowDelimiter(), 
            null
            );
      elementBehavior.createInitValueElement(
            ProcessorInterpolateSnapshotTable.REQ_STATE_PATH, 
            metaInput.getAttributeUpstreamFlowPath(), 
            null
            );
      elementBehavior.createInitValueElement(
            ProcessorInterpolateSnapshotTable.REQ_STATE_TYPE, 
            metaInput.getAttributeUpstreamInterpType(), 
            null
            );
   }

   @Override
   protected void configureDownstreamBoundary(ElementBoundary elementBoundary,
         long indexLastCell) throws Exception 
   {
      ElementBehaviorMatrix elementBehavior =
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
            ResourceWater.NAME_UPSTREAM_BOUNDARY_NAME, 
            boundaryName, 
            null
            );
   }

}
