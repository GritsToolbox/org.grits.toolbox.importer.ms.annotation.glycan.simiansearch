package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.handler;

import java.io.File;

import org.apache.log4j.Logger;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.ms.annotation.gelato.AnalyteStructureAnnotation;
import org.grits.toolbox.ms.annotation.gelato.glycan.GlycanStructureAnnotationTIM;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.om.data.Data;

/**
 * NewGelatoHandlerTIM
 * @author D. Brent Weatherly
 *
 */
public class NewGelatoHandlerTIM  {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(NewGelatoHandlerTIM.class);

	public static AnalyteStructureAnnotation getNewStructureAnnotationObject(
			Data data, String t_tempFolder, String msAnnotationFolder,
			MSGlycanAnnotationProperty t_property, 
			MSFile msFile) {
		try {
			return new GlycanStructureAnnotationTIM(data, t_tempFolder, msAnnotationFolder + File.separator + t_property.getMSAnnotationMetaData().getAnnotationId(),
					msFile);
		} catch (Exception ex) {
			logger.error("General error.", ex);
		}
		return null;
	}
	
}
