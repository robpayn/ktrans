package ktrans;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.payn.chsm.State;
import org.payn.chsm.values.ValueDouble;
import org.payn.chsm.values.ValueLong;
import org.payn.neoch.HolonBoundary;
import org.payn.neoch.HolonCell;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.solute.boundary.BehaviorSoluteFlow;

public class ParticleConcTrackerTASCC {

   protected OutputHandlerTASCC particleManager;
   protected String resourceName;
   private ValueLong tick;
   private ValueDouble time;
   protected ValueDouble timeStep;
   private long interval;
   private double startTime;
   protected HolonCell currentCell;
   protected HolonCell endCell;
   protected HolonBoundary currentBound;
   protected double endDistance;
   protected double currentDistance;
   protected BufferedWriter writer;
   protected double velocity;
   private long bufferedIteration;
   private double bufferedTime;
   private double bufferedConc;
   

   public ParticleConcTrackerTASCC(OutputHandlerTASCC particleManager,
         String resourceName, double velocity) 
   {
      this.particleManager = particleManager;
      this.resourceName = resourceName;
      this.velocity = velocity;
   }

   public void buffer() throws IOException 
   {
      bufferedConc = ((ValueDouble)currentBound.getCell().getState(
            resourceName + ResourceSolute.NAME_SOLUTE_CONC).getValue()).n;
      HolonBoundary adjBound = currentBound.getAdjacentBoundary();
      double adjConc;
      if (adjBound != null)
      {
         adjConc = ((ValueDouble)adjBound.getCell().getState(
               resourceName + ResourceSolute.NAME_SOLUTE_CONC).getValue()).n;
      }
      else
      {
         adjConc = ((ValueDouble)currentBound.getState(
               resourceName + ResourceSolute.NAME_SOLUTE_CONC).getValue()).n;
      }
      if (isFlowPositive())
      {
         bufferedConc += ((endDistance - currentDistance) / (2 * endDistance)) * (adjConc - bufferedConc);
      }
      else
      {
         bufferedConc += (currentDistance / (2 * endDistance)) * (adjConc - bufferedConc);
      }
      bufferedIteration = tick.n;
      bufferedTime = time.n;
   }

   public void initializeTime(ValueLong tick, ValueDouble time,
         ValueDouble timeStep, long interval) 
   {
      this.tick = tick;
      this.time = time;
      this.timeStep = timeStep;
      this.interval = interval;
      startTime = time.n;
   }


   public void initializeOutput(int particleCount, File output) throws IOException 
   {
      File outputDir = new File(output.getAbsolutePath() + File.separator + resourceName);
      if (!outputDir.exists())
      {
         outputDir.mkdirs();
      }
      writer = new BufferedWriter(new FileWriter(new File(
            outputDir.getAbsolutePath() + File.separator + 
            String.format("particle_%06d", particleCount) + ".txt"
            )));
   }

   protected boolean isFlowPositive() 
   {
      State state = currentBound.getState(BehaviorSoluteFlow.REQ_STATE_FLOW);
      if (state == null)
      {
         state = currentBound.getAdjacentBoundary().getState(
               BehaviorSoluteFlow.REQ_STATE_FLOW
               );
         return -((ValueDouble)state.getValue()).n > 0;
      }
      else
      {
         return ((ValueDouble)state.getValue()).n > 0;
      }
   }
   
   public String getResourceName()
   {
      return resourceName;
   }
   
   public void close() throws IOException
   {
      writer.close();
   }

   public void initializeLocation(HolonCell releaseCell, HolonCell endCell) 
   {
      this.currentCell = releaseCell;
      this.endCell = endCell;
      ArrayList<HolonBoundary> bounds = currentCell.getBoundaries(resourceName + "." + ResourceSolute.BEHAVIOR_FLOW);
      if (bounds.size() < 2)
      {
         bounds = currentCell.getBoundaries(resourceName + "." + ResourceSolute.BEHAVIOR_CONCBOUND_INJECT);
         if (bounds.size() == 0)
         {
            bounds = currentCell.getBoundaries(resourceName + "." + ResourceSolute.BEHAVIOR_CONCBOUND);
         }
         currentBound = bounds.get(0);
      }
      else
      {
         for (HolonBoundary bound: bounds)
         {
            currentBound = bound;
            if (isFlowPositive())
            {
               break;
            }
         }
      }
      
      endDistance = ((ValueDouble)currentBound.getState(BehaviorSoluteFlow.REQ_STATE_LENGTH).getValue()).n / 2;
      currentDistance = 0;
      
   }

   protected void move() throws IOException 
   {
      currentDistance += velocity * timeStep.n;
      if (currentDistance > endDistance)
      {
         double extraDistance = currentDistance - endDistance;
         if (isFlowPositive())
         {
            if (currentCell.getName().equals(endCell.getName()))
            {
               currentDistance = endDistance;
               particleManager.reportFinishedParticle(this);
            }
            else
            {
               ArrayList<HolonBoundary> bounds = currentCell.getBoundaries(resourceName + "." + ResourceSolute.BEHAVIOR_FLOW);
               for (HolonBoundary bound: bounds)
               {
                  currentBound = bound;
                  if (!isFlowPositive())
                  {
                     break;
                  }
               }
               endDistance = ((ValueDouble)currentBound.getState(BehaviorSoluteFlow.REQ_STATE_LENGTH).getValue()).n / 2;
               currentDistance = extraDistance;
            }
         } 
         else
         {
            currentBound = currentBound.getAdjacentBoundary();
            currentCell = currentBound.getCell();
            currentDistance = extraDistance;
         }
      }   
   }

   public void write() throws IOException 
   {
      writer.write(String.format("%d %f %f", bufferedIteration, bufferedTime, bufferedConc));
      writer.newLine();
   }

}
