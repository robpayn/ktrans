package org.payn.stream;

import org.payn.chsm.Behavior;
import org.payn.neoch.io.xmltools.ElementBehaviorMatrix;
import org.payn.neoch.io.xmltools.ElementBoundary;
import org.payn.neoch.io.xmltools.ElementHolonMatrix;
import org.payn.resources.water.ResourceWater;
import org.payn.resources.water.channel.boundary.dynamicwave.BehaviorDynamicWaveWiele;
import org.payn.resources.water.channel.cell.BehaviorChannelStorage;

/**
 * An input processor for building stream metabolism models
 * 
 * @author robpayn
 *
 */
public class MetabolismBuilderInputProcessorXML 
      extends StreamBuilderInputProcessorXML<MetabolismMetaInputXML> {

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
   public MetabolismBuilderInputProcessorXML(MetabolismMetaInputXML metaInput,
         StreamSimulator sim) 
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
            BehaviorChannelStorage.NAME_LENGTH, 
            Double.toString(cellLength), 
            null
            );
      double distance = (cellLength * index) - (cellLength / 2);
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_X, 
            Double.toString(distance), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_Y, 
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
            BehaviorChannelStorage.NAME_BANK_ELEVATION, 
            Double.toString(bedElevation + activeDepth), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_WATER_HEAD, 
            Double.toString(bedElevation + initialDepth), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_ACTIVE_WIDTH_AVG, 
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
      elementBehavior.createInitValueElement(
            BehaviorDynamicWaveWiele.REQ_STATE_WIELEINT, 
            Double.toString(wieleInt), 
            null
            );
      elementBehavior.createInitValueElement(
            BehaviorDynamicWaveWiele.REQ_STATE_WIELESLOPE, 
            Double.toString(wieleSlope), 
            null
            );
      elementBehavior.createInitValueElement(
            ResourceWater.NAME_ACTIVE_WIDTH_AVG, 
            Double.toString(averageWidth), 
            null
            );
   }

}
