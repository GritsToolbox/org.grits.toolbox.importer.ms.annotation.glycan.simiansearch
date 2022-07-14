package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.property.FileLockManager;
import org.grits.toolbox.entry.ms.property.FileLockingUtils;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.MSGlycanAnnotationWizard;

/**
 * OpenGelatoWizard - Opens the new GELATO wizard for glycan annotation of MS file(s)
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class OpenGelatoWizard {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(OpenGelatoWizard.class);

	@Inject private static IGritsDataModelService gritsDataModelService = null;
	@Inject static IGritsUIService gritsUIService = null;
	@Inject MApplication application;
	
	@Execute
	public Object execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			IEventBroker eventBroker, @Named (IServiceConstants.ACTIVE_SHELL) Shell shell, EPartService partService) {
		try {
			List<Entry> msEntries = getMSEntries(object);
			MSGlycanAnnotationWizard wizard = createNewMSAnnotDialog(shell, msEntries );
			if( wizard == null || wizard.getInitial().getMsEntryList() == null ) {
				return null;
			}
			Shell shell2 = new Shell(shell);
			NewGelatoHandler handler = new NewGelatoHandler();
			handler.setWizard(wizard);
			handler.setMsEntries(msEntries);
			List<Entry[]> resultEntries = handler.process(shell2);			
			if( resultEntries != null ) {
				for( int i = 0; i < resultEntries.size(); i++ ) {
					Entry[] curEntries = resultEntries.get(i);
					gritsDataModelService.addEntry(curEntries[0], curEntries[1]);
				}
			}

			if( resultEntries != null && ! resultEntries.isEmpty() && resultEntries.get(resultEntries.size()-1)[1] != null ) {
				// save the project first
				try
				{
					// parent of MS Entry is Sample, parent of Sample is the Project
					ProjectFileHandler.saveProject(resultEntries.get(resultEntries.size()-1)[0].getParent().getParent());
				} catch (IOException e)
				{
					logger.error("Something went wrong while saving project entry \n" + e.getMessage(),e);
					logger.fatal("Closing project entry \""
							+ resultEntries.get(resultEntries.size()-1)[0].getParent().getParent().getDisplayName() + "\"");
					gritsDataModelService.closeProject(resultEntries.get(resultEntries.size()-1)[0].getParent().getParent());
					throw e;
				}
				for( int i = 0; i < resultEntries.size(); i++ ) {
					Entry[] curEntries = resultEntries.get(i);
					lockFiles (wizard, curEntries[0], curEntries[1]);
				}
				final Entry lastMSEntry = (Entry) resultEntries.get(resultEntries.size()-1)[1] ;
				eventBroker.send(IGritsDataModelService.EVENT_SELECT_ENTRY, lastMSEntry);
				
				try {
				    // need to set the partService to refresh gritsUIServices' stale partService, see ticket #799
					gritsUIService.setPartService(partService);
					gritsUIService.openEntryInPart(lastMSEntry);
				} catch (Exception e) {
					logger.debug("Could not open the part", e);
				}
			}
			return resultEntries;
		} catch ( Exception ex ) {
			logger.error("General Exception executing OpenGelatoWizard.", ex);
		}
		return null;
	}


	/**
	 * lock the file used for the annotation so that the file cannot be removed before removing this annotation entry
	 * 
	 * @param wizard 
	 * @param msEntry
	 * @param msAnnotationEntry
	 */
	private void lockFiles(MSGlycanAnnotationWizard wizard, Entry msEntry, Entry msAnnotationEntry) {
		MassSpecProperty prop = (MassSpecProperty) msEntry.getProperty();
		FileLockManager mng;
		try {
			String lockFileLocation = prop.getLockFilePath(msEntry);
			mng = FileLockingUtils.readLockFile(lockFileLocation);
			MSPropertyDataFile file = wizard.getInitial().getFileMap().get(msEntry.getDisplayName());
			if (file != null) {
				mng.lockFile(file.getName(), msAnnotationEntry);
				FileLockingUtils.writeLockFile(mng, lockFileLocation);
			}
		} catch (IOException e) {
			logger.error("Could not lock the file", e);
		} catch (JAXBException e) {
			logger.error("Could not lock the file", e);
		}	
	}


	/**
	 * @param shell - current active Shell
	 * @param msEntries - List of Entry
	 * @return MSGlycanAnnotationWizard - new instance of the MSGlycanAnnotationWizard
	 */
	private MSGlycanAnnotationWizard createNewMSAnnotDialog(Shell shell, List<Entry> msEntries) {
		MSGlycanAnnotationWizard wizard = new MSGlycanAnnotationWizard();
		//set the Sample entry if there is one chosen
		wizard.setMSEntries(msEntries);
		WizardDialog dialog = new WizardDialog(shell, wizard);
		try {
			if (dialog.open() == Window.OK) {
				return wizard;
			}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			ErrorUtils.createErrorMessageBox(shell, "Exception", e);
		}
		return null;
	}


	/**
	 * @param object - an Entry or a StructuredSelection item
	 * @return List<Entry> - List of MS Entries to annotation
	 */
	private List<Entry> getMSEntries(Object object)  {
		List<Entry> entries = new ArrayList<Entry>();
		
		StructuredSelection to = null;
		Entry selectedEntry = null;
		if(object instanceof Entry)
		{
			selectedEntry = (Entry) object;
		}
		else if (object instanceof StructuredSelection)
		{
			if(((StructuredSelection) object).getFirstElement() instanceof Entry)
			{
				to = (StructuredSelection) object;
			}
		}
		if (selectedEntry != null) {
//			if(selectedEntry.getProperty().getType().equals(MassSpecProperty.TYPE)) {
			if(selectedEntry.getProperty() instanceof MassSpecProperty) {
				entries.add(selectedEntry);
			}
		}
		// try getting the last selection from the data model
		if(gritsDataModelService.getLastSelection() != null
				&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
		{
			to = gritsDataModelService.getLastSelection();
		}

		
		if(to != null) {
			List<Entry> selList = to.toList();
			for(int i=0; i < selList.size(); i++) {		
				Entry msEntry = selList.get(i);
				//if the right property
//				if(msEntry.getProperty().getType().equals(MassSpecProperty.TYPE)) {
				if(msEntry.getProperty() instanceof MassSpecProperty) {
					if (!entries.contains(msEntry))
						entries.add(msEntry);
				}
			}
		}

		return entries;
	}

}
