package org.payn.stream.reporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.payn.chsm.io.reporters.ReporterSingleThread;
import org.payn.chsm.resources.time.BehaviorTime;
import org.payn.chsm.resources.time.Iteration;
import org.payn.chsm.resources.time.Time;
import org.payn.chsm.values.ValueDouble;
import org.payn.chsm.values.ValueLong;
import org.payn.neoch.HolonCell;
import org.payn.neoch.HolonMatrix;

/**
 * A reporter that tracks particles in a one-dimensional
 * flow
 * 
 * @author robpayn
 *
 */
public class ReporterTASCC extends ReporterSingleThread {

   /**
    * Map of resources being tracked by particles
    */
   ArrayList<ParticleConcTrackerTASCC> finishedParticles;
   /**
    * List of active particles
    */
   ArrayList<ParticleConcTrackerTASCC> particles;
   /**
    * Cell where particles are released
    */
   private HolonCell releaseCell;
   /**
    * Cells where particles are finished
    */
   private HolonCell endCell;
   /**
    * Interval
    */
   private ValueLong tick;
   /**
    * Simulation time
    */
   private ValueDouble time;
   /**
    * Time step
    */
   private ValueDouble timeStep;
   /**
    * Interval at which particles record the resource
    */
   private Long releaseIteration;
   /**
    * File with particle velocities
    */
   private String velocityFile;
   /**
    * Resource names
    */
   private ArrayList<String> resourceNames;
   
   /**
    * Construct a new instance with the provided working directory and
    * argument map
    * 
    * @param workingDir
    * @param argMap
    */
   public ReporterTASCC(File workingDir, HashMap<String, String> argMap) 
   {
      super(workingDir, argMap);
   }

   @Override
   public void openLocation() throws Exception 
   {
      // Locations are opened by individual particles
   }

   @Override
   protected void bufferOutput() throws Exception 
   {
      for(ParticleConcTrackerTASCC particle: finishedParticles)
      {
         particle.close();
         particles.remove(particle);
      }
      finishedParticles.clear();
      for (ParticleConcTrackerTASCC particle: particles)
      {
         particle.buffer();
      }
   }

   @Override
   protected void backgroundWrite() throws Exception 
   {
      for (ParticleConcTrackerTASCC particle: particles)
      {
         particle.write();
      }
   }

   @Override
   protected void closeWhenFinished() throws Exception 
   {
      for(ParticleConcTrackerTASCC particle: particles)
      {
         particle.close();
      }      
   }

   /**
    * Override to advance the particles
    */
   @Override
   public void conditionalWrite() throws Exception
   {
      if (tick.n == releaseIteration)
      {
         BufferedReader reader = new BufferedReader(new FileReader(new File(velocityFile)));
         int particleCount = 0;
         while(reader.ready())
         {
            double velocity = Double.valueOf(reader.readLine());
            ParticleConcTrackerTASCC particle = new ParticleConcTrackerTASCC(this, resourceNames, velocity);
            particle.initializeTime(tick, time, timeStep);
            particle.initializeLocation(releaseCell, endCell);
            particle.initializeOutput(particleCount, outputDir);
            particles.add(particle);
            particleCount++;
         }
         reader.close();
      }
      super.conditionalWrite();
      for(ParticleConcTrackerTASCC particle: particles)
      {
         particle.move();
      }      
   }

   /**
    * Initialize the reporter
    */
   public void initializeReporterTASCC() 
   {
      finishedParticles = new ArrayList<ParticleConcTrackerTASCC>();
      particles = new ArrayList<ParticleConcTrackerTASCC>();
      resourceNames = new ArrayList<String>();
      tick = (ValueLong)source.getState(
            Iteration.class.getSimpleName()
            ).getValue();
      time = (ValueDouble)source.getState(
            Time.class.getSimpleName()
            ).getValue();
      timeStep = (ValueDouble)source.getState(
            BehaviorTime.ITERATION_INTERVAL
            ).getValue();
   }

   /**
    * Add the resources based on a comma-delimited list of resource names
    * 
    * @param resourceList
    */
   public void addResources(String resourceList) 
   {
      String[] resourceNameArray = resourceList.split(",");
      for (String resourceName: resourceNameArray)
      {
         resourceNames.add(resourceName);
      }
   }

   /**
    * Set the cell where particles are released
    * 
    * @param cellName
    *       name of cell
    */
   public void setReleaseCell(String cellName) 
   {
      releaseCell = ((HolonMatrix)source).getCell(cellName);
   }

   /**
    * Set the cell where particles are collected
    * 
    * @param cellName
    *       name of cell
    */
   public void setEndCell(String cellName) 
   {
      endCell = ((HolonMatrix)source).getCell(cellName);
   }

   /**
    * Set the iteration when particles are released
    * 
    * @param releaseIteration
    *       iteration
    */
   public void setReleaseIteration(Long releaseIteration) 
   {
      this.releaseIteration = releaseIteration;
   }

   /**
    * Set the file name with the velocity data for the
    * particles
    * 
    * @param velocityFile
    *       file with velocity data
    */
   public void setVelocityFile(String velocityFile) 
   {
      this.velocityFile = workingDir + File.separator + velocityFile;
   }

   /**
    * Report that a particle has reached the end cell
    * 
    * @param particle
    *       particle that has finished
    */
   public void reportFinishedParticle(ParticleConcTrackerTASCC particle) 
   {
      finishedParticles.add(particle);
   }

}
