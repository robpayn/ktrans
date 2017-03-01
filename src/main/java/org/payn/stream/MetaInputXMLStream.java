package org.payn.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.payn.chsm.io.xml.ElementHelper;

/**
 * Abstract meta input for a NEOCH stream model
 * 
 * @author robpayn
 *
 */
public abstract class MetaInputXMLStream extends MetaInputXMLNEOCH {
   
   /**
    * An element helper for the geometry element
    * 
    * @author v78h241
    *
    */
   private class ElementGeometry extends ElementHelper {

      /**
       * Elevation element
       */
      private ElementHelper elementElevation;
      
      /**
       * Active channel element
       */
      private ElementHelper elementActiveChannel;

      /**
       * Construct a new instance associated with the provided element
       * 
       * @param helper 
       *        the element helper containing the geometry element
       */
      public ElementGeometry(ElementHelper helper) 
      {
         super(helper.getFirstChildElement("channelgeometry"));
      }

      /**
       * Get the length attribute value
       * 
       * @return
       *        length value
       */
      public String getAttributeStreamLength() 
      {
         return getAttribute("length");
      }
      
      /**
       * Get the elevation element
       * 
       * @return
       *       Elevation element reference
       */
      private ElementHelper getElementElevation() 
      {
         if (elementElevation == null)
         {
            elementElevation = helper.getFirstChildElementHelper("elevation");
         }
         return elementElevation;
      }

      /**
       * Get the element for active channel configuration
       * 
       * @return
       *        active channel element
       */
      private ElementHelper getElementActiveChannel() 
      {
         if (elementActiveChannel == null)
         {
            elementActiveChannel = helper.getFirstChildElementHelper("activechannel");
         }
         return elementActiveChannel;
      }

      /**
       * Get the datum attribute value
       * 
       * @return
       *        datum value
       */
      public String getAttributeElevationDatum() 
      {
         return getElementElevation().getAttribute("datum");
      }

      /**
       * Get the bed slope attribute value
       * 
       * @return
       *        bed slope attribute value
       */
      public String getAttributeBedSlope() 
      {
         return getElementElevation().getAttribute("bedSlope");
      }

      /**
       * Get the value for the depth attribute
       * 
       * @return
       *        value for the depth attribute
       */
      public String getAttributeActiveDepth() 
      {
         return getElementActiveChannel().getAttribute("depth");
      }

      /**
       * Get the value for the average width attribute
       * 
       * @return
       *        average width value
       */
      public String getAttributeAverageWidth() 
      {
         return getElementActiveChannel().getAttribute("averageWidth");
      }

   }

   /**
    * Element for flow configuration data
    * 
    * @author v78h241
    *
    */
   private class ElementFlow extends ElementHelper {

      /**
       * Element for friction
       */
      private ElementHelper elementFricton;
      
      /**
       * Element for upstream boundary conditions
       */
      private ElementHelper elementUpstreamBound;

      /**
       * Construct a flow element based on the flow element
       * in the provided helper
       * 
       * @param helper
       */
      public ElementFlow(ElementHelper helper) 
      {
         super(helper.getFirstChildElement("flow"));
      }

      /**
       * Get the initial depth attribute
       * 
       * @return
       *    value of initial depth
       */
      public String getAttributeInitialDepth() 
      {
         return getAttribute("initialDepth");
      }
      
      /**
       * Get the initial flow attribute
       * 
       * @return
       *        value of initial flow
       */
      public String getAttributeInitialFlow() 
      {
         return getAttribute("initialFlow");
      }

      /**
       * Get the friction element
       * 
       * @return
       *        friction element
       */
      private ElementHelper getElementFriction() 
      {
         if (elementFricton == null)
         {
            elementFricton = helper.getFirstChildElementHelper("friction");
         }
         return elementFricton;
      }

      /**
       * Get the upstream boundary element
       * 
       * @return
       *        upstream boundary element
       */
      private ElementHelper getElementUpstreamBound() 
      {
         if (elementUpstreamBound == null)
         {
            elementUpstreamBound = helper.getFirstChildElementHelper("upstreambound");
         }
         return elementUpstreamBound;
      }

      /**
       * Get the Wiele model intercept attribute
       * 
       * @return
       *        value of Wiele model intercept
       */
      public String getAttributeWieleInt() 
      {
         return getElementFriction().getAttribute("wieleInt");
      }

      /**
       * Get the Wiele model slope attribute
       * 
       * @return
       *        value of the Wiele model slope
       */
      public String getAttributeWieleSlope() 
      {
         return getElementFriction().getAttribute("wieleSlope");
      }

      /**
       * Get the flow path attribute
       * 
       * @return
       *        value of the flow path
       */
      public String getAttributeUpstreamFlowPath() 
      {
         return getElementUpstreamBound().getAttribute("upstreamPath");
      }

      /**
       * Get the delimiter attribute
       * 
       * @return
       *        value of the delimiter
       */
      public String getAttributeUpstreamFlowDelimeter() 
      {
         return getElementUpstreamBound().getAttribute("upstreamDelimiter");
      }

      /**
       * Get the interpolation type attribute
       * 
       * @return
       *        value of the interpolation type
       */
      public String getAttributeUpstreamInterpType() 
      {
         return getElementUpstreamBound().getAttribute("upstreamInterpType");
      }

   }
   
   /**
    * Element helper for the model structure
    */
   private ElementHelper elementStructure;

   /**
    * Element for channel geometry configuration
    */
   private ElementGeometry elementGeometry;
   
   /**
    * Element for the flow configuration
    */
   private ElementFlow elementFlow;

   /**
    * Construct a new instance that uses the provided working directory,
    * path to the configuration file, and the name of the XML element
    * with the configuration information
    * 
    * @param workingDir
    * @param path
    * @param elementName
    * @throws Exception
    */
   public MetaInputXMLStream(File workingDir, String path,
         String elementName) throws Exception 
   {
      super(workingDir, path, elementName);
   }
   
   /**
    * Get the model structure element
    * 
    * @return
    */
   private ElementHelper getElementModelStructure() 
   {
      if (elementStructure == null)
      {
         elementStructure = helper.getFirstChildElementHelper("modelstructure");
      }
      return elementStructure;
   }
   /**
    * Get the geometry element
    * 
    * @return
    *       geometry element helper object
    */
   public ElementGeometry getElementGeometry()
   {
      if (elementGeometry == null)
      {
         elementGeometry = new ElementGeometry(helper);
      }
      return elementGeometry;
   }

   /**
    * Get the flow element
    * 
    * @return
    *       flow element helper object
    */
   private ElementFlow getElementFlow() 
   {
      if (elementFlow == null)
      {
         elementFlow = new ElementFlow(helper);
      }
      return elementFlow;
   }

  /**
    * Get the number of cells from the meta input
    * 
    * @return
    *       number of cells
    */
   public Long getAttributeNumCells() 
   {
      return Long.valueOf(getElementModelStructure().getAttribute("numCells"));
   }
   
   /**
    * Get the length of the stream
    * 
    * @return
    *       stream length
    */
   public Double getAttributeStreamLength()
   {
      return Double.valueOf(getElementGeometry().getAttributeStreamLength());
   }

   /**
    * Get the length of the stream
    * 
    * @return
    *       stream length
    */
   public Double getAttributeBedSlope()
   {
      return Double.valueOf(getElementGeometry().getAttributeBedSlope());
   }

   /**
    * Get the elevation datum
    * 
    * @return
    *       stream length
    */
   public Double getAttributeElevationDatum()
   {
      return Double.valueOf(getElementGeometry().getAttributeElevationDatum());
   }

   /**
    * Get the active channel depth
    * 
    * @return
    *       active channel depth
    */
   public Double getAttributeActiveDepth()
   {
      return Double.valueOf(getElementGeometry().getAttributeActiveDepth());
   }

   /**
    * Get the initial channel depth
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeInitialDepth()
   {
      return Double.valueOf(getElementFlow().getAttributeInitialDepth());
   }

   /**
    * Get the average width of the active channel
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeAverageWidth()
   {
      return Double.valueOf(getElementGeometry().getAttributeAverageWidth());
   }

   /**
    * Get the intercept of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeWieleInt()
   {
      return Double.valueOf(getElementFlow().getAttributeWieleInt());
   }

   /**
    * Get the slope of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeWieleSlope()
   {
      return Double.valueOf(getElementFlow().getAttributeWieleSlope());
   }

   /**
    * Get the slope of the Wiele friction vs. depth relationship
    * 
    * @return
    *       initial channel depth
    */
   public Double getAttributeInitialFlow()
   {
      return Double.valueOf(getElementFlow().getAttributeInitialFlow());
   }
  
   /**
    * Get the delimiter for the upstream flow file
    * 
    * @return
    *       delimiter character
    */
   public String getAttributeUpstreamFlowDelimiter()
   {
      return getElementFlow().getAttributeUpstreamFlowDelimeter();
   }
   
   /**
    * Get the path to the interpolation file for upstream boundary
    * 
    * @return
    *       relative path to working directory
    */
   public String getAttributeUpstreamFlowPath()
   {
      return getElementFlow().getAttributeUpstreamFlowPath();
   }
   
   /**
    * Get the interpolation type for upstream boundary
    * 
    * @return
    *       interpolation type
    */
   public String getAttributeUpstreamInterpType()
   {
      return getElementFlow().getAttributeUpstreamInterpType();
   }

   /**
    * Get the name for cells
    * 
    * @return
    *       cell name as a string
    */
   public String getAttributeCellName()
   {
      return getElementModelStructure().getAttribute("cellName");
   }
   
   /**
    * Get the name for cells
    * 
    * @return
    *       cell name as a string
    */
   public String getAttributeBoundaryName()
   {
      return getElementModelStructure().getAttribute("boundaryName");
   }
   
   /**
    * Is the initial conditions flag set?
    * 
    * @return
    *       true if initial conditions flag is set, false otherwise
    */
   public boolean isInitialConditions()
   {
      return Boolean.valueOf(helper.getAttribute("initialConditions"));
   }
   
   public String getAttributeInitialConditionPathCell()
   {
      return helper.getAttribute("cellPath");
   }

   public String getAttributeInitialConditionDelimiterCell()
   {
      return helper.getAttribute("cellDelimiter");
   }

   public String getAttributeInitialConditionPathBound()
   {
      return helper.getAttribute("boundPath");
   }

   public String getAttributeInitialConditionDelimiterBound()
   {
      return helper.getAttribute("boundDelimiter");
   }

   /**
    * Get the initial conditions cell map
    * 
    * @param workingDir 
    * @param cellNameRoot 
    * @param numCellsDigits 
    * 
    * @return
    *       map of initial values for cells
    * @throws Exception 
    */
   public LinkedHashMap<String, HashMap<String, Double>> getInitialConditionsCellMap(
         File workingDir, String cellNameRoot, Integer numCellsDigits) 
               throws Exception
   {
      File file = new File(workingDir.getAbsolutePath() + File.separator + helper.getAttribute("cellPath"));
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String[] line = reader.readLine().split(" ");
      LinkedHashMap<String, HashMap<String, Double>> hashMap = new LinkedHashMap<String, HashMap<String, Double>>();
      for (String header: line)
      {
         hashMap.put(header, new LinkedHashMap<String, Double>());
      }
      String[] headers = hashMap.keySet().toArray(new String[0]);
      int cellCount = 1;
      while(reader.ready())
      {
         line = reader.readLine().split(" ");
         int column = 0;
         for (String value: line)
         {
            String cellName = String.format(
                  "%s%0" + numCellsDigits.toString() + "d", 
                  cellNameRoot,
                  cellCount
                  );
            hashMap.get(headers[column]).put(cellName, Double.valueOf(value));
            column++;
         }
         cellCount++;
      }
      reader.close();
      return hashMap;
   }
   
   /**
    * Get the initial conditions bound map
    * 
    * @param workingDir 
    * @param boundNameRoot 
    * @param numCellsDigits 
    * 
    * @return
    *       map of intial values for bounds
    * @throws Exception 
    */
   public LinkedHashMap<String, HashMap<String, Double>> getInitialConditionsBoundMap(
         File workingDir, String boundNameRoot, Integer numCellsDigits) 
               throws Exception
   {
      File file = new File(workingDir.getAbsolutePath() + File.separator + helper.getAttribute("boundPath"));
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String[] line = reader.readLine().split(" ");
      LinkedHashMap<String, HashMap<String, Double>> hashMap = new LinkedHashMap<String, HashMap<String, Double>>();
      for (String header: line)
      {
         hashMap.put(header, new LinkedHashMap<String, Double>());
      }
      String[] headers = hashMap.keySet().toArray(new String[0]);
      int boundCount = 1;
      while(reader.ready())
      {
         line = reader.readLine().split(" ");
         int column = 0;
         for (String value: line)
         {
            String boundName = String.format(
                  "%s%0" + numCellsDigits.toString() + "d_%0" + numCellsDigits.toString() + "d", 
                  boundNameRoot,
                  boundCount + 1,
                  boundCount
                  );
            hashMap.get(headers[column]).put(boundName, Double.valueOf(value));
            column++;
         }
         boundCount++;
      }
      reader.close();
      return hashMap;
   }

}
