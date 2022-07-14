package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.property.MSGlycanAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.glycan.property.datamodel.MSGlycanAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationFileInfo;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.utils.DatabaseUtils;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.GeneralInformationMulti;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.MSGlycanAnnotationWizard;
import org.grits.toolbox.ms.annotation.gelato.AnalyteStructureAnnotation;
import org.grits.toolbox.ms.annotation.process.GelatoWorker;
import org.grits.toolbox.ms.file.FileCategory;
import org.grits.toolbox.ms.file.MSFile;
import org.grits.toolbox.ms.file.reader.IMSAnnotationFileReader;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Data;
import org.grits.toolbox.ms.om.data.DataHeader;
import org.grits.toolbox.ms.om.data.GlycanFilter;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.widgets.processDialog.GRITSProgressDialog;
import org.grits.toolbox.widgets.progress.IProgressListener;
import org.grits.toolbox.widgets.progress.IProgressListener.ProgressType;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;
import org.grits.toolbox.widgets.tools.GRITSWorker;
import org.grits.toolbox.widgets.tools.INotifyingProcess;
import org.grits.toolbox.widgets.tools.NotifyingProcessUtil;

/**
 * NewGelatoHandler
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class NewGelatoHandler extends GRITSWorker implements INotifyingProcess {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(NewGelatoHandler.class);
	protected MSGlycanAnnotationWizard wizard = null;
	protected List<Entry> msEntries = null;
	protected List<IProgressListener> myProgressListeners = null;
	protected List<Entry[]> returnList = new ArrayList<Entry[]>(); 
	protected GRITSProgressDialog gpd = null;
	protected Shell shell = null;
	protected AnalyteStructureAnnotation gsa = null;

	public NewGelatoHandler() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * getNewDataObject
	 * @param MSGlycanAnnotationWizard - wizard calling to create the new Data object
	 * @return org.grits.toolbox.ms.om.data.Data
	 */
	protected Data getNewDataObject(MSGlycanAnnotationWizard wizard) {
		Data data = new Data();
		DataHeader dHeader = new DataHeader();
		data.setDataHeader(dHeader);
		data.getDataHeader().setMethod(wizard.getMethod());
		//The following already got set correctly in method in the wizard
		//dHeader.getMethod().setIntensityCutoff(wizard.getGlycanSettingsForm().getCutOff());
		//dHeader.getMethod().setIntensityCutoffType(wizard.getGlycanSettingsForm().getCutOffType());
		//dHeader.getMethod().setPrecursorIntensityCutoff(wizard.getGlycanSettingsForm().getCutOffPrecursor());
		//dHeader.getMethod().setPrecursorIntensityCutoffType(wizard.getGlycanSettingsForm().getCutOffTypePrecursor());
		return data;
	}

	/**
	 * @return List<Entry> - List of MS entries
	 */
	public List<Entry> getMsEntries() {
		return msEntries;
	}

	/**
	 * @param msEntries - List of MS entries
	 */
	public void setMsEntries(List<Entry> msEntries) {
		this.msEntries = msEntries;
	}

	/**
	 * @param wizard - the calling MSGlycanAnnotationWizard
	 */
	public void setWizard(MSGlycanAnnotationWizard wizard) {
		this.wizard = wizard;
	}

	/**
	 * @return MSGlycanAnnotationWizard
	 */
	public MSGlycanAnnotationWizard getWizard() {
		return wizard;
	}

	/**
	 * @return String (the workspace location stored in the PropertyHandler)
	 */
	private String getWorkspaceLocation() {
		return  PropertyHandler.getVariable("workspace_location");
	}

	/**
	 * @return File - the temporary folder for creation of intermediate files during GELATO processing
	 */
	protected File getTempFolder() {
		String workspaceLocation = getWorkspaceLocation();
		String t_tempFolder = workspaceLocation + ".temp" + File.separator + "GELATO_" + Long.toString(System.currentTimeMillis()) + File.separator;
		File t_tempFolderFile = new File(t_tempFolder);
		t_tempFolderFile.mkdirs();
		return t_tempFolderFile;
	}

	/**
	 * @param msEntry - the current MS Entry to annotation
	 * @return File - the destination folder for GELATO output files
	 */
	protected File getAnnotationFolder( Entry msEntry ) {

		String workspaceLocation = getWorkspaceLocation();
		MSGlycanAnnotationProperty t_property = new MSGlycanAnnotationProperty();
		Entry projectEntry = DataModelSearch.findParentByType(msEntry, ProjectProperty.TYPE);
		String projectName = projectEntry.getDisplayName();

		String msAnnotationFolder = workspaceLocation + projectName + File.separator + t_property.getArchiveFolder();	
		File msAnnotationFolderFile = new File(msAnnotationFolder);
		msAnnotationFolderFile.mkdirs();

		return msAnnotationFolderFile;
	}

	/**
	 * @param shell - the current active shell
	 * @param wizard - the current active MSGlycanAnnotationWizard
	 * @param msAnnotationFolder - the destination folder for output
	 * @return MSGlycanAnnotationProperty - property of the MS Glycan Annotation object
	 */
	protected MSAnnotationProperty getMSAnnotationProperty(Shell shell, MSGlycanAnnotationWizard wizard, String msAnnotationFolder) {
		MSGlycanAnnotationProperty t_property = new MSGlycanAnnotationProperty();
		MSGlycanAnnotationMetaData metaData = new MSGlycanAnnotationMetaData();		
		t_property.setMSAnnotationMetaData(metaData);
		try {
			metaData.setAnnotationId(this.createRandomId(msAnnotationFolder));
			metaData.setDescription(wizard.getInitial().getMSDescription());
			metaData.setVersion(MSGlycanAnnotationMetaData.CURRENT_VERSION);
			metaData.setName(t_property.getMetaDataFileName());
		} catch (IOException e2) {
			logger.error(e2.getMessage(), e2);
			ErrorUtils.createErrorMessageBox(shell, "Exception", e2);
			return null;
		}

		return t_property;
	}

	/**
	 * @param msAnnotation - String representation of the annotation folder
	 * @return String - an integer value for the entry's id
	 * @throws IOException
	 */
	protected String createRandomId(String msAnnotation) throws IOException {
		return MSAnnotationProperty.createRandomId(msAnnotation);
	}

	/**
	 * @param shell - current active shell
	 * @return List<Entry[]> - 
	 * @throws ExecutionException
	 */
	public List<Entry[]> process(Shell shell) throws ExecutionException {
		try {
			// 1 listener for gelato worker, the rest from annotation
			gpd = new GRITSProgressDialog(shell, AnalyteStructureAnnotation.getNumListenersNeeded() + 1, false);
			gpd.open();
			gpd.setGritsWorker(this);
			List<IProgressListener> majorListeners = new ArrayList<>();
			setProgressListeners(majorListeners);
			int iRes = gpd.startWorker();
			if( iRes != GRITSProcessStatus.OK && returnList != null && ! returnList.isEmpty() ) {
				String sMessage = null;
				if( iRes == GRITSProcessStatus.ERROR ) {
					sMessage = "An error occurred during processing. Save completed results?";
				} else {					
					sMessage = "Processing canceled. Save completed results?";
				}
				int iYesNo = ErrorUtils.createSingleConfirmationMessageBoxReturn(shell, "Process did not complete", sMessage);		
				if (iYesNo != SWT.YES ) {
					returnList.clear();
				}
			}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		updateListeners("Done!", getMsEntries().size());
		return returnList;
	}

	/**
	 * Updates the database path references to overcome any issues with sharing workspaces between different computers.
	 * 
	 * @param method
	 */
	private void updateDatabaseReferences(Method method) {
		for( AnalyteSettings aSettings : method.getAnalyteSettings() ) {
			GlycanFilter filter = aSettings.getGlycanSettings().getFilter();
			if (filter.getDatabase().indexOf(File.separator) == -1) {  // does not have the full path
				try {
					filter.setDatabase(DatabaseUtils.getDatabasePath() + File.separator + filter.getDatabase());
				} catch (IOException e) {
					logger.error("Database path cannot be determined", e);
				}
			}					
		}
	}
	
	
	/**
	 * Attempts to determine the experiment type. If it is a known type, then instantiate the associated Annotation object.
	 * Otherwise, scan the MSFile to see if we can support it some other way. 
	 * 
	 * @param data - the org.grits.toolbox.ms.om.data.Data object 
	 * @param t_tempFolder - the String value of the location of the temporary, intermediate files for GELATO
	 * @param msAnnotationFolder - the String value of the destination folder for the GELATO results  
	 * @param t_property - the MSGlycanAnnotationProperty for the entry
	 * @param msFile - MS file to be processed
	 * @return AnalyteStructureAnnotation, if determined, otherwise null
	 */
	protected AnalyteStructureAnnotation getAnnotationStructure( Data data, String t_tempFolder, 
			String msAnnotationFolder, MSGlycanAnnotationProperty t_property, MSFile msFile ) {
		String sMSType =  msFile.getExperimentType();
		AnalyteStructureAnnotation newGSA = null;
		if( sMSType.equals(Method.MS_TYPE_INFUSION) ) {
			if (msFile.getReader() != null && msFile.getReader() instanceof IMSAnnotationFileReader) {
				if (((IMSAnnotationFileReader)msFile.getReader()).hasMS1Scan(msFile))
					newGSA = NewGelatoHandlerDirectInfusion.getNewStructureAnnotationObject(data, t_tempFolder, msAnnotationFolder, t_property, msFile);
				else
					newGSA = NewGelatoHandlerTIM.getNewStructureAnnotationObject(data, t_tempFolder, msAnnotationFolder, t_property, msFile);
			} 
		} else if ( sMSType.equals(Method.MS_TYPE_TIM) ) {
			newGSA = NewGelatoHandlerTIM.getNewStructureAnnotationObject(data, t_tempFolder, msAnnotationFolder, t_property, msFile);
		} else if ( sMSType.equals(Method.MS_TYPE_LC) ) {
			newGSA = NewGelatoHandlerLCMSMS.getNewStructureAnnotationObject(data, t_tempFolder, msAnnotationFolder, t_property, msFile);
		} else if ( sMSType.equals(Method.MS_TYPE_MSPROFILE) ) {
			newGSA = NewGelatoHandlerMSProfile.getNewStructureAnnotationObject(data, t_tempFolder, msAnnotationFolder, t_property, msFile);
		} else {
			int iNumMS1 = ((IMSAnnotationFileReader)msFile.getReader()).getNumMS1Scans(msFile);
			int iNumMS2 = ((IMSAnnotationFileReader)msFile.getReader()).getNumMS2Scans(msFile);
			double dRatio = (double) iNumMS1 / (double) iNumMS2;
			if( iNumMS1 == 0 && iNumMS2 > 0 ) { // treat as TIM
				newGSA = NewGelatoHandlerTIM.getNewStructureAnnotationObject(data, t_tempFolder, msAnnotationFolder, t_property, msFile); 
			} else if ( iNumMS1 > 0 && iNumMS2 == 0 ) { // treat as MS Profile
				newGSA = NewGelatoHandlerMSProfile.getNewStructureAnnotationObject(data, t_tempFolder, msAnnotationFolder, t_property, msFile);
			} else if ( iNumMS1 > 0 && iNumMS2 > 0 ) {
				if( dRatio < 0.1 ) { // less than 10% of scans are MS1, treat as direct infusion?
					newGSA = NewGelatoHandlerDirectInfusion.getNewStructureAnnotationObject(data, t_tempFolder, msAnnotationFolder, t_property, msFile);
				} else { // treat as LC
					newGSA = NewGelatoHandlerLCMSMS.getNewStructureAnnotationObject(data, t_tempFolder, msAnnotationFolder, t_property, msFile);
				}
			}
		}
		
		return newGSA;
		
	}

	/**
	 * @param gpd - the GRITSProgressDialog that will be updated during annotation, can be null
	 * @param data - the org.grits.toolbox.ms.om.data.Data object 
	 * @param t_tempFolder - the String value of the location of the temporary, intermediate files for GELATO
	 * @param msAnnotationFolder - the String value of the destination folder for the GELATO results  
	 * @param t_property - the MSGlycanAnnotationProperty for the entry
	 * @param filterSetting - the FilterSetting object, optional
	 * @param msFile - MS file to be processed
	 * @return int - a GRITSProcessStatus value
	 */
	private int performAnnotation( GRITSProgressDialog gpd, Data data, String t_tempFolder, 
			String msAnnotationFolder, MSGlycanAnnotationProperty t_property, MSFile msFile) {
		logger.debug("Starting job: determineScanBounds");
		try {
			// first update the Database references
			updateDatabaseReferences(data.getDataHeader().getMethod());
			gsa = getAnnotationStructure(data, t_tempFolder, msAnnotationFolder, t_property, msFile);
			if( gsa == null ) {
				return GRITSProcessStatus.ERROR;				
			}
			//			we have to set the MS Type for each MS run. Everything else is the same!
			String sMSType =  msFile.getExperimentType();
			wizard.getMethod().setMsType( sMSType );

			List<IProgressListener> workerListeners = new ArrayList<>();
			workerListeners.add(gpd.getMinorProgressBarListeners()[0]);
			List<IProgressListener> annotationListeners = new ArrayList<>();
			annotationListeners.add(gpd.getMinorProgressBarListeners()[1]);

			GelatoWorker gw = new GelatoWorker(gsa, gpd, gpd, workerListeners, annotationListeners, null);
			int iRes = gw.doWork();
			return iRes;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return GRITSProcessStatus.ERROR;	
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.NotifyingProcess#updateListeners(java.lang.String, int)
	 */
	@Override
	public void updateListeners( String _sMsg, int _iVal ) {
		NotifyingProcessUtil.updateListeners(getProgressListeners(), _sMsg, _iVal);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.NotifyingProcess#updateErrorListener(java.lang.String)
	 */
	@Override
	public void updateErrorListener(String _sMsg) {
		NotifyingProcessUtil.updateErrorListener(getProgressListeners(), _sMsg);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.NotifyingProcess#updateErrorListener(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void updateErrorListener(String _sMsg, Throwable t) {
		NotifyingProcessUtil.updateErrorListener(getProgressListeners(), _sMsg, t);
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.NotifyingProcess#setMaxValue(int)
	 */
	@Override
	public void setMaxValue(int _iVal) {
		NotifyingProcessUtil.setMaxValue(getProgressListeners(), _iVal);		
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.NotifyingProcess#addProgressListeners(org.grits.toolbox.widgets.progress.IProgressListener)
	 */
	@Override
	public void addProgressListeners(IProgressListener lProgressListener) {
		this.myProgressListeners.add(lProgressListener);	
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.NotifyingProcess#getProgressListeners()
	 */
	@Override
	public List<IProgressListener> getProgressListeners() {
		return myProgressListeners;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.NotifyingProcess#setProgressListeners(java.util.List)
	 */
	@Override
	public void setProgressListeners(List<IProgressListener> lProgressListeners) {
		this.myProgressListeners = lProgressListeners;
	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.NotifyingProcess#setProgressType(org.grits.toolbox.widgets.progress.IProgressListener.ProgressType)
	 */
	@Override
	public void setProgressType(ProgressType progressType) {
		NotifyingProcessUtil.setProgressType(getProgressListeners(), progressType);

	}

	/* (non-Javadoc)
	 * @see org.grits.toolbox.widgets.tools.GRITSWorker#doWork()
	 */
	@Override
	public int doWork() {
		File fWorkspaceFolderFile = getTempFolder();	
		int iStatus = GRITSProcessStatus.OK;
		try {
			setMaxValue(wizard.getInitial().getMsEntryList().size());			
			// iterate over all selected MS Entries. Continue so long as an error or cancel is not encountered
			for( int i = 0; i < wizard.getInitial().getMsEntryList().size() && iStatus == GRITSProcessStatus.OK; i++ ) {
				// get the current MS Entry
				Entry msEntry = wizard.getInitial().getMsEntryList().get(i);
				updateListeners("Processing entry: " + msEntry.getDisplayName(), i);

				final MassSpecProperty prop = (MassSpecProperty) msEntry.getProperty();
				Entry msAnnotationEntry = new Entry();		
				String sWorkspaceFolder = fWorkspaceFolderFile.getAbsolutePath();

				File msAnnFile = getAnnotationFolder(msEntry);	
				String msAnnotationFolder = msAnnFile.getAbsolutePath();
				MSGlycanAnnotationProperty msAnnotProperty = (MSGlycanAnnotationProperty) getMSAnnotationProperty(shell, wizard, msAnnotationFolder);

				String msEntryDisplayName = msEntry.getDisplayName();
				String msAnnotName = wizard.getInitial().getListEntries().get(msEntryDisplayName);
				//			msAnnotationEntry.setDisplayName(wizard.getInitial().getDisplayName());
				msAnnotationEntry.setDisplayName(msAnnotName);
				msAnnotationEntry.setProperty(msAnnotProperty);

				Data data = getNewDataObject(wizard);
				DataHeader dHeader = data.getDataHeader();

				MSPropertyDataFile dataFile = wizard.getInitial().getFileMap().get(msEntry.getDisplayName());
				String workspaceLocation = PropertyHandler.getVariable("workspace_location");
				String projectName = DataModelSearch.findParentByType(msEntry, ProjectProperty.TYPE).getDisplayName();
				String pathToFile = workspaceLocation + projectName + File.separator + MassSpecProperty.getFoldername();

				// call performAnnotation - does all the work. 
				iStatus = performAnnotation( gpd, data, sWorkspaceFolder, msAnnotationFolder, msAnnotProperty, 
						dataFile.getMSFileWithReader(pathToFile, prop.getMassSpecMetaData().getMsExperimentType()));
				if( iStatus == GRITSProcessStatus.OK ) {
					// if everything worked, save to workspace all of the appropriate files for Annotation
					msAnnotProperty.getMSAnnotationMetaData().addAnnotationFile(dataFile);

					PropertyDataFile msMetaData = MSAnnotationProperty.getNewSettingsFile(msAnnotProperty.getMetaDataFileName(), msAnnotProperty.getMSAnnotationMetaData());
					msAnnotProperty.getDataFiles().add(msMetaData);
					addResultFileToMetaData(dHeader.getMethod().getMsType(), msAnnotProperty);
					MSAnnotationProperty.marshallSettingsFile(msAnnotProperty.getAnnotationFolder(msEntry) + File.separator +
							msAnnotProperty.getMetaDataFileName(), msAnnotProperty.getMSAnnotationMetaData());

					Entry[] entries = new Entry[2];
					entries[0] = msEntry; // entry for the source MS
					entries[1] = msAnnotationEntry; // the new entry for the MS Annotation

					logger.debug ("Finished creating the annotation archive");
					// return list is a List<Entry[]>. 
					returnList.add(entries);
				} else if (iStatus == GRITSProcessStatus.CANCEL) {
					deleteResultFiles(dHeader.getMethod().getMsType());
				}
			}			
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			iStatus = GRITSProcessStatus.ERROR;
		}
		for( File f : fWorkspaceFolderFile.listFiles() ) {
			f.delete();
		}
		fWorkspaceFolderFile.delete();
		updateListeners("Done!", getMsEntries().size());
		return iStatus;
	}

	/**
	 * delete any result files created so far (used in case of a CANCEL)
	 * @param msType type of the experiment
	 */
	protected void deleteResultFiles(String msType) {
		if( msType.equals(Method.MS_TYPE_LC) ) {
			NewGelatoHandlerLCMSMS.deleteResultFiles(gsa);
		} else {
			NewGelatoHandler.deleteResultFiles(gsa);
		}
	}

	/**
	 * Delete any created result files (used in case of a CANCEL)
	 * 
	 * @param gsa current GlycanStructureAnnotation object (to get the archive name)
	 */
	private static void deleteResultFiles(AnalyteStructureAnnotation gsa) {
		try {
			String sAnnotationFile = gsa.getFinalArchiveName();
			File annotationFile = new File( sAnnotationFile );
			if (annotationFile.exists())
				annotationFile.delete();
		} catch (Exception e) {
			logger.warn("Could not delete generated files", e);
		}
	}

	/**
	 * Calls the appropriate method (based on MS Type) for populating the MS Glycan Annotation Property with the result file(s)
	 * 
	 * @param sMSType, the MS Type from the Method in the DataHeader
	 * @param msAnnotProperty, the Property to add the results file to
	 */
	protected void addResultFileToMetaData(String sMSType, MSAnnotationProperty msAnnotProperty) {
		if( sMSType.equals(Method.MS_TYPE_LC) ) {
			NewGelatoHandlerLCMSMS.addResultFileToMetaData(gsa, (MSGlycanAnnotationProperty) msAnnotProperty);
		} else {
			NewGelatoHandler.addResultFileToMetaData(gsa, msAnnotProperty);
		}		
	}

	/**
	 * Determines the annotation file name and folder name (if applicable), creates MSPropertyDataFile objects for them, and adds
	 * them to the meta data for the result files.
	 * 
	 * @param gsa, the GlycanStructureAnnotation specific for the MS Type other than LC-MS/MS
	 * @param msAnnotProperty, the Property to add the results file to
	 */
	private static void addResultFileToMetaData(AnalyteStructureAnnotation gsa, MSAnnotationProperty msAnnotProperty) {
		String sAnnotationFile = gsa.getFinalArchiveName();
		File annotationFile = new File( sAnnotationFile );
		MSPropertyDataFile pdfFolder = new MSPropertyDataFile(annotationFile.getName(), 
				MSAnnotationFileInfo.MS_ANNOTATION_CURRENT_VERSION, 
				MSAnnotationFileInfo.MS_ANNOTATION_TYPE_FILE,
				FileCategory.ANNOTATION_CATEGORY, 
				GeneralInformationMulti.FILE_TYPE_GELATO,
				annotationFile.getPath(), new ArrayList<String>() );
		msAnnotProperty.getMSAnnotationMetaData().addFile(pdfFolder);

	}
}
