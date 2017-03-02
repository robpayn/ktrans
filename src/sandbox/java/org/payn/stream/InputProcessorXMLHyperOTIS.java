package org.payn.stream;

import java.io.File;

import org.payn.neoch.io.xmltools.ElementBoundary;
import org.payn.neoch.io.xmltools.ElementHolonMatrix;

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

   public InputProcessorXMLHyperOTIS(MetaInputXMLHyperOTIS metaInput,
         SimulatorStream simulator) 
   {
      super(metaInput, simulator);
   }

   @Override
   protected void configureStreamLoop() throws Exception 
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void configureStreamCell(ElementHolonMatrix elementCell,
         long index) 
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void configureStreamBoundary(ElementBoundary elementBoundary,
         ElementBoundary elementBoundaryAdj, int index) 
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void configureUpstreamBoundary(ElementBoundary elementBoundary,
         int indexFirstCell) throws Exception 
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void configureDownstreamBoundary(ElementBoundary elementBoundary,
         long indexLastCell) throws Exception 
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void configureResources() throws Exception 
   {
      // TODO Auto-generated method stub
      
   }

}
