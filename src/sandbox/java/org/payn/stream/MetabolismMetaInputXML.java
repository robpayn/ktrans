package org.payn.stream;

import java.io.File;

/**
 * Meta input for building a stream metabolism model
 * 
 * @author robpayn
 *
 */
public class MetabolismMetaInputXML extends StreamMetaInputXML {

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
   public MetabolismMetaInputXML(File workingDir, String path,
         String elementName) throws Exception 
   {
      super(workingDir, path, elementName);
   }

}
