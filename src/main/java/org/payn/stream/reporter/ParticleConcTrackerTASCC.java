package org.payn.stream.reporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.payn.chsm.State;
import org.payn.chsm.values.ValueDouble;
import org.payn.chsm.values.ValueLong;
import org.payn.neoch.HolonBoundary;
import org.payn.neoch.HolonCell;
import org.payn.resources.solute.ResourceSolute;

/**
 * A particle in one dimensional flow for tracking concentration
 * 
 * @author robpayn
 *
 */
public class ParticleConcTrackerTASCC {

   /**
    * Reporter tracking the particle
    */
   protected ReporterTASCC reporter;
   
   /**
    * Model iteration
    */
   private ValueLong iteration;
   
   /**
    * Simulation time
    */
   private ValueDouble time;
   
   /**
    * Duration of an iteration
    */
   protected ValueDouble timeStep;
   
   /**
    * Current cell location
    */
   protected HolonCell currentCell;
   
   /**
    * End cell
    */
   protected HolonCell endCell;
   
   /**
    * Current boundary location
    */
   protected HolonBoundary currentBound;
   
   /**
    * The end distance for current boundary
    */
   protected double endDistance;
   
   /**
    * The current distance traveled along a boundary
    */
   protected double currentDistance;
   
   /**
    * A file writer for the particle reporting
    */
   protected BufferedWriter writer;
   
   /**
    * Velocity of particle movement
    */
   protected double velocity;
   
   /**
    * Buffered iteration value for asynchronous IO
    */
   private long bufferedIteration;
   
   /**
    * Buffered time value for asynchronous IO
    */
   private double bufferedTime;
   
   /**
    * Buffered concentrations for asynchronous IO
    */
   private LinkedHashMap<String, Double> bufferedConcMap;
   
   /**
    * First resource name in list
    */
   private String firstResourceName;
   
   /**
    * Construct a new instance with the provided reporter
    * 
    * @param reporter
    * @param resourceNames
    * @param velocity
    */
   public ParticleConcTrackerTASCC(ReporterTASCC reporter,
         ArrayList<String> resourceNames, double velocity) 
   {
      this.reporter = reporter;
      this.velocity = velocity;
      this.bufferedConcMap = new LinkedHashMap<String, Double>();
      for (String resource: resourceNames)
      {
         bufferedConcMap.put(resource, null);
      }
      firstResourceName = resourceNames.get(0);
   }

   /**
    * Buffer the output for asynchronous writing
    * 
    * @throws IOException
    */
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
      bufferedIteration = iteration.n;
      bufferedTime = time.n;
   }

   /**
    * Initialize the time
    * 
    * @param iteration
    *       model iteration
    * @param time
    *       simulation time
    * @param timeStep
    *       duration of iteration
    */
   public void initializeTime(ValueLong iteration, ValueDouble time,
         ValueDouble timeStep) 
   {
      this.iteration = iteration;
      this.time = time;
      this.timeStep = timeStep;
   }

   /**
    * Initialize the output
    * 
    * @param particleCount
    * @param output
    * @throws IOException
    */
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

   /**
    * Determine if water flow is positive in the current boundary
    * 
    * @return
    *       true if flow is positive, false otherwise
    */
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
   
   /**
    * Close the file writer for this particle
    * 
    * @throws IOException
    */
   public void close() throws IOException
   {
      writer.close();
   }

   /**
    * Initialize the location of the particle
    * 
    * @param releaseCell
    * @param endCell
    */
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

   /**
    * Move the particle
    * 
    * @throws IOException
    */
   public void move() throws IOException 
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
               reporter.reportFinishedParticle(this);
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

   /**
    * Write the buffered output to IO
    * 
    * @throws IOException
    */
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
