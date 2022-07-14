package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.handler;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationFileInfo;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.GeneralInformationMulti;
import org.grits.toolbox.ms.annotation.gelato.AnalyteStructureAnnotation;
import org.grits.toolbox.ms.annotation.gelato.glycan.GlycanStructureAnnotation;
import org.grits.toolbox.ms.annotation.gelato.glycan.GlycanStructureAnnotationLCMSMS;
import org.grits.toolbox.ms.file.FileCategory;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.om.data.Data;

/**
 * NewGelatoHandlerLCMSMS
 * @author D. Brent Weatherly
 *
 */
public class NewGelatoHandlerLCMSMS {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(NewGelatoHandlerLCMSMS.class);

	public static AnalyteStructureAnnotation getNewStructureAnnotationObject(
			Data data, String t_tempFolder, String msAnnotationFolder,
			MSGlycanAnnotationProperty t_property, MSFile msFile) {
		try {
			return new GlycanStructureAnnotationLCMSMS(data, t_tempFolder, msAnnotationFolder + File.separator + t_property.getMSAnnotationMetaData().getAnnotationId(),
					msFile);
		} catch (Exception ex) {
			logger.error("General error.", ex);
		}
		return null;
	}
	
	/**
	 * Determines the annotation file name and folder name (if applicable), creates MSPropertyDataFile objects for them, and adds
	 * them to the meta data for the result files.
	 * 
	 * @param gsa, the GlycanStructureAnnotation specific for the MS Type
	 * @param msAnnotProperty, the Property to add the results file to
	 */
	public static void addResultFileToMetaData(AnalyteStructureAnnotation gsa, MSGlycanAnnotationProperty msAnnotProperty) {
		String sAnnotationFile = gsa.getOverviewFileName();
		File annotationFile = new File(sAnnotationFile);
		MSPropertyDataFile pdf = new MSPropertyDataFile(annotationFile.getName(), 
				MSAnnotationFileInfo.MS_ANNOTATION_CURRENT_VERSION, 
				MSAnnotationFileInfo.MS_ANNOTATION_TYPE_FILE,
				FileCategory.ANNOTATION_CATEGORY, 
				GeneralInformationMulti.FILE_TYPE_GELATO,
				sAnnotationFile, new ArrayList<String>() );
		msAnnotProperty.getMSAnnotationMetaData().addFile(pdf);

		if( gsa.needsOverview() ) { // should be a folder then
			String sPreAnnotationFolder = gsa.getFinalArchiveName();
			File preAnnotationFolder = new File(sPreAnnotationFolder);
			File annotationFolder = new File( preAnnotationFolder.getParent() );
			MSPropertyDataFile pdfFolder = new MSPropertyDataFile(annotationFolder.getName() + File.separator, 
					MSAnnotationFileInfo.MS_ANNOTATION_CURRENT_VERSION, 
					MSAnnotationFileInfo.MS_ANNOTATION_TYPE_FOLDER,
					FileCategory.ANNOTATION_CATEGORY, 
					GeneralInformationMulti.FILE_TYPE_GELATO,
					annotationFolder.getPath(), new ArrayList<String>() );
			msAnnotProperty.getMSAnnotationMetaData().addFile(pdfFolder);
		}
	}

	/**
	 * Delete any created result files (used in case of a CANCEL)
	 * 
	 * @param gsa current GlycanStructureAnnotation object (to get the archive file/folder name)
	 */
	public static void deleteResultFiles(AnalyteStructureAnnotation gsa) {
		try {
			String sAnnotationFile = gsa.getOverviewFileName();
			File annotationFile = new File(sAnnotationFile);
			if (annotationFile.exists())
				annotationFile.delete();
			if( gsa.needsOverview() ) { // should be a folder then
				String sPreAnnotationFolder = gsa.getFinalArchiveName();
				File preAnnotationFolder = new File(sPreAnnotationFolder);
				File annotationFolder = new File( preAnnotationFolder.getParent() );
				if (annotationFolder.exists() && annotationFolder.isDirectory()) {
					for (File f: annotationFolder.listFiles()) {
						f.delete();
					}
					annotationFolder.delete();
				}
			}
		} catch (Exception e) {
			logger.warn("Could not delete generated files", e);
		}
		
	}
	
}
