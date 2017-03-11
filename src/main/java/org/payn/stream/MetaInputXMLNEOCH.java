package org.payn.stream;

import java.io.File;

import org.payn.chsm.io.xml.XMLDocumentModelConfig;
import org.payn.neoch.io.xmltools.ElementXMLInputMatrix;
import org.payn.simulation.metainputs.MetaInputXML;

/**
 * Abstract meta input for a NEOCH model builder
 * 
 * @author robpayn
 *
 */
public abstract class MetaInputXMLNEOCH extends MetaInputXML<XMLDocumentModelConfig> {

   /**
    * Input element for the model input
    */
   private ElementXMLInputMatrix xmlInputElement;

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
   public MetaInputXMLNEOCH(File workingDir, String path,
         String elementName) throws Exception 
   {
      super(workingDir, path, elementName);
      this.xmlInputElement = 
            new ElementXMLInputMatrix(
                  document.getBuilderElement().getXMLInputElement(),
                  workingDir
                  );
   }

   @Override
   protected XMLDocumentModelConfig createDocument(File metaInputFile)
         throws Exception 
   {
      return new XMLDocumentModelConfig(metaInputFile);
   }

   /**
    * Get the cell file from the NEOCH settings
    * 
    * @return
    *       cell file
    * @throws Exception
    */
   public File getCellFile() throws Exception 
   {
      return xmlInputElement.getCellFile();
   }

   /**
    * Get the boundary file from the NEOCH settings
    * 
    * @return
    *       boundary file
    * @throws Exception
    */
   public File getBoundaryFile() throws Exception 
   {
      return xmlInputElement.getBoundaryFile();
   }

}
