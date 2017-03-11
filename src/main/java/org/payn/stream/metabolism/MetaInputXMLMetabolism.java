package org.payn.stream.metabolism;

import java.io.File;

import org.payn.stream.MetaInputXMLStream;

/**
 * Meta input for building a stream metabolism model
 * 
 * @author robpayn
 *
 */
public class MetaInputXMLMetabolism extends MetaInputXMLStream {

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
   public MetaInputXMLMetabolism(File workingDir, String path,
         String elementName) throws Exception 
   {
      super(workingDir, path, elementName);
   }

}
