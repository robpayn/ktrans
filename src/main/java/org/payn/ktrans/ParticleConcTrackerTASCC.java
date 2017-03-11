package org.payn.ktrans;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.payn.chsm.State;
import org.payn.chsm.values.ValueDouble;
import org.payn.chsm.values.ValueLong;
import org.payn.neoch.HolonBoundary;
import org.payn.neoch.HolonCell;
import org.payn.resources.solute.ResourceSolute;
import org.payn.resources.solute.boundary.BehaviorSoluteFlow;

public class ParticleConcTrackerTASCC {

   protected ReporterTASCC particleManager;
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
   private LinkedHashMap<String, Double> bufferedConcMap;
   private String firstResourceName;
   

   public ParticleConcTrackerTASCC(ReporterTASCC particleManager,
         ArrayList<String> resourceNames, double velocity) 
   {
      this.particleManager = particleManager;
      this.velocity = velocity;
      this.bufferedConcMap = new LinkedHashMap<String, Double>();
      for (String resource: resourceNames)
      {
         bufferedConcMap.put(resource, null);
      }
      firstResourceName = resourceNames.get(0);
   }

   public void buffer() throws IOException 
   {
      for (Entry<String, Double> entry: bufferedConcMap.entrySet())
      {
         String resourceName = entry.getKey();
         double bufferedConc = ((ValueDouble)currentBound.getCell().getState(
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
         entry.setValue(bufferedConc);
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
      File outputDir = new File(output.getAbsolutePath());
      if (!outputDir.exists())
      {
         outputDir.mkdirs();
      }
      writer = new BufferedWriter(new FileWriter(new File(
            outputDir.getAbsolutePath() + File.separator + 
            String.format("particle_%06d", particleCount) + ".txt"
            )));
      writer.write("iteration time");
      for (String resourceName: bufferedConcMap.keySet())
      {
         writer.write(" " + resourceName);
      }
      writer.newLine();
   }

   protected boolean isFlowPositive() 
   {
      State state = currentBound.getState(ResourceSolute.NAME_WATER_FLOW);
      if (state == null)
      {
         state = currentBound.getAdjacentBoundary().getState(
               ResourceSolute.NAME_WATER_FLOW
               );
         return -((ValueDouble)state.getValue()).n > 0;
      }
      else
      {
         return ((ValueDouble)state.getValue()).n > 0;
      }
   }
   
   public void close() throws IOException
   {
      writer.close();
   }

   public void initializeLocation(HolonCell releaseCell, HolonCell endCell) 
   {
      this.currentCell = releaseCell;
      this.endCell = endCell;
      ArrayList<HolonBoundary> bounds = currentCell.getBoundaries(firstResourceName + "." + ResourceSolute.BEHAVIOR_FLOW);
      if (bounds.size() < 2)
      {
         bounds = currentCell.getBoundaries(firstResourceName + "." + ResourceSolute.BEHAVIOR_CONCBOUND_INJECT);
         if (bounds.size() == 0)
         {
            bounds = currentCell.getBoundaries(firstResourceName + "." + ResourceSolute.BEHAVIOR_CONCBOUND);
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
      try
      {
         endDistance = ((ValueDouble)currentBound.getState(ResourceSolute.NAME_LENGTH).getValue()).n / 2;
      }
      catch (Exception e)
      {
         endDistance = ((ValueDouble)currentCell.getState(ResourceSolute.NAME_LENGTH).getValue()).n / 2;
      }
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
               String resourceName = firstResourceName;
               ArrayList<HolonBoundary> bounds = currentCell.getBoundaries(
                     resourceName + "." + ResourceSolute.BEHAVIOR_FLOW
                     );
               for (HolonBoundary bound: bounds)
               {
                  currentBound = bound;
                  if (!isFlowPositive())
                  {
                     break;
                  }
               }
               try
               {
                  endDistance = ((ValueDouble)currentBound.getState(ResourceSolute.NAME_LENGTH).getValue()).n / 2;
               }
               catch (Exception e)
               {
                  endDistance = ((ValueDouble)currentCell.getState(ResourceSolute.NAME_LENGTH).getValue()).n / 2;
               }
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
      writer.write(String.format("%d %f", bufferedIteration, bufferedTime));
      for (Entry<String, Double> entry: bufferedConcMap.entrySet())
      {
         writer.write(String.format(" %f", entry.getValue()));
      }
      writer.newLine();
   }

}
