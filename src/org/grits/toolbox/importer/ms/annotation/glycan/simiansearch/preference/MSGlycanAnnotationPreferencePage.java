package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.preference;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationSettingsPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.util.PreferenceUtils;

public class MSGlycanAnnotationPreferencePage extends PreferencePage {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationPreferencePage.class);
	
	protected MSGlycanAnnotationSettingsPreference preferences = null;
	private MSGlycanAnnotationPreference currentSetting = null;
	
	private GridLayout gridLayout;
	private Combo cmbSelectSetting;
	private Button btnEditCurrent;
	private Button btnDeleteCurrent;
	private Button btnCreateNew;
	private Button btnExport;
	private Button btnImport;

	public MSGlycanAnnotationPreferencePage() {
		loadWorkspacePreferences();
	}

	@Override
	protected Control createContents(Composite parent) {
		initGridLayout();
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(gridLayout);
		addSelectSettingsItem(container);
		addExportCurrent(container);
		addDeleteCurrent(container);
		addEditCurrent(container);
		addCreateNewItem(container);
		addImport(container);
		setEditEnabled(false);
 
		return parent;
	}

	protected void initGridLayout() {
		gridLayout = new GridLayout(6, false);
	}
	
	protected void addSelectSettingsItem( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		Label lblSelectSetting = new Label(parent, SWT.NONE);
		lblSelectSetting.setText("Current Settings");
		lblSelectSetting.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		cmbSelectSetting = new Combo(parent, SWT.READ_ONLY);
		cmbSelectSetting.setLayoutData(gd2);
		initStoredSettingList();
		cmbSelectSetting.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				processSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	protected void processSelection() {				
		setEditEnabled(false);
		if( ! cmbSelectSetting.getText().trim().equals("") ) {
			setEditEnabled(true);
			MSGlycanAnnotationPreference selSetting = getCurrentSetting(cmbSelectSetting.getText());
			if( selSetting == null ) {
				return;
			}
			currentSetting = selSetting;
		} 		
	}
	
	public void setEditEnabled( boolean _bVal ) {
		btnDeleteCurrent.setEnabled(_bVal);
		btnEditCurrent.setEnabled(_bVal);
		btnExport.setEnabled(_bVal);
	}
	
	protected void initStoredSettingList() {
		cmbSelectSetting.removeAll();
		cmbSelectSetting.add("");
		if( preferences != null && preferences.getPreferenceList() != null ) {
			for (MSGlycanAnnotationPreference setting : preferences.getPreferenceList()) {
				cmbSelectSetting.add(setting.getName());
			}
		}
	}
	
	protected void addDeleteCurrent( Composite parent ) {
		GridData gd3 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnDeleteCurrent = new Button(parent, SWT.NONE);
		btnDeleteCurrent.setText("Delete Selected");
		btnDeleteCurrent.setLayoutData(gd3);				

		btnDeleteCurrent.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {

			}

			@Override
			public void mouseDown(MouseEvent e) {
				int iSelInx = cmbSelectSetting.getSelectionIndex();
				if (iSelInx == 0) // nothing is loaded from the list, do not delete
					return;
				boolean bVal = MessageDialog.openConfirm(getShell(), "Delete Selected?", "Delete selected. Are you sure?");
				if( bVal ) {
					MSGlycanAnnotationPreference setting = getCurrentSetting(cmbSelectSetting.getItem(iSelInx));
					if (setting == null)
						return;
					preferences.remove(setting);
					cmbSelectSetting.remove(iSelInx);
					currentSetting = null;
					clearValues();
					setEditEnabled(false);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	}
	
	protected MSGlycanAnnotationPreference getCurrentSetting( String settingName ) {
		if( preferences == null || preferences.getPreferenceList() == null ) {
			return null;
		}
		for( int i = 0; i < preferences.getPreferenceList().size(); i++ ) {
			MSGlycanAnnotationPreference curSetting =  preferences.getPreferenceList().get(i);
			if( curSetting.getName().equals(settingName) ) {
				return curSetting;
			}
		}
		return null;
	}
	protected void addEditCurrent( final Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnEditCurrent = new Button(parent, SWT.NONE);
		btnEditCurrent.setText("Edit Selected");
		btnEditCurrent.setLayoutData(gd1);					
		btnEditCurrent.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// open up wizard with the current setting
				MSGlycanAnnotationSettingsPreferenceWizard wizard = getMSAnnotationSettingsPreferenceWizard(currentSetting);
				WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
				try {
					if (dialog.open() == Window.OK) {
						preferences.getPreferenceList().remove(currentSetting);
						currentSetting = wizard.getPreferences();
						preferences.getPreferenceList().add(currentSetting);
					}
				} catch (Exception e1) { 
					logger.error(e1.getMessage(), e1);
					ErrorUtils.createErrorMessageBox(getShell(), "Exception", e1);
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	}
	
	protected void addCreateNewItem( final Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnCreateNew = new Button(parent, SWT.NONE);
		btnCreateNew.setText("Create New");
		btnCreateNew.setLayoutData(gd1);					
		btnCreateNew.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {			
				setEditEnabled(false);
				clearValues();	
				// open up the wizard to get the new settings
				IInputValidator myValidator = new IInputValidator() {
					
					@Override
					public String isValid(String newText) {
						// check against current names
						if (preferences != null) {
							for (MSGlycanAnnotationPreference setting : preferences.getPreferenceList()) {
								if (newText.equals(setting.getName()))
									return "Already exists! Please choose a different name";
							}
						}
						return null;
					}
				};
				InputDialog nameDialog = new InputDialog(parent.getShell(), "Preference Name", "Please enter a name for the new preference setting", "", myValidator);
				if (nameDialog.open() == Window.OK) {
					String preferenceName = nameDialog.getValue();
					if (preferenceName == null)
						return;
					// open up the wizard to get preference values
					MSGlycanAnnotationSettingsPreferenceWizard wizard = getMSAnnotationSettingsPreferenceWizard(preferenceName);
					WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
					try {
						if (dialog.open() == Window.OK) {
							currentSetting = wizard.getPreferences();
							preferences.getPreferenceList().add(currentSetting);
							//update the combo box
							cmbSelectSetting.add(currentSetting.getName());
							cmbSelectSetting.select(cmbSelectSetting.getItemCount() - 1);
						}
					} catch (Exception e1) { 
						logger.error(e1.getMessage(), e1);
						ErrorUtils.createErrorMessageBox(getShell(), "Exception", e1);
					}
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	}
	
	private void addImport(Composite parent) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnImport = new Button(parent, SWT.NONE);
		btnImport.setText("Import");
		btnImport.setLayoutData(gd1);					
		btnImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[]{"*.xml"});
				String fileName = dialog.open();
				if (fileName != null && !fileName.isEmpty()) {
					try {
					    BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
					    String line;
					    String xmlString = "";
					    while ((line = bufferedReader.readLine()) != null) {
					       xmlString += line + "\n";
					    }
					    bufferedReader.close();
						MSGlycanAnnotationPreference newPref = (MSGlycanAnnotationPreference) PreferenceUtils
								.unmarshallFromXML(xmlString, MSGlycanAnnotationPreference.class);
						if (newPref != null) {
							for (MSGlycanAnnotationPreference setting : preferences.getPreferenceList()) {
								if (newPref.getName().equals(setting.getName())) {
									MessageDialog.openInformation(getShell(), "Info", "Already exists! Updating the existing");
									setting.setMethod(newPref.getMethod());
									return;
								}
							}
							preferences.getPreferenceList().add(newPref);
							//update the combo box
							cmbSelectSetting.add(newPref.getName());
							cmbSelectSetting.select(cmbSelectSetting.getItemCount()-1);
							currentSetting = newPref;
						}
					} catch (FileNotFoundException e1) {
						MessageDialog.openError(getShell(), "Error", "Selected file cannot be found");
						logger.error("Selected file cannot be found", e1);
					} catch (IOException e1) {
						MessageDialog.openError(getShell(), "Error", "Selected file cannot be opened");
						logger.error("Selected file Selected file cannot be opened", e1);
					} catch (JAXBException e1) {
						MessageDialog.openError(getShell(), "Error", "Selected file does not contain a valid preference");
						logger.error("Selected file does not contain a valid preference", e1);
					}
				}
			};
		});
	}

	private void addExportCurrent(Composite parent) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		btnExport = new Button(parent, SWT.NONE);
		btnExport.setText("Export");
		btnExport.setLayoutData(gd1);					
		btnExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (currentSetting != null) {
					FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
					dialog.setOverwrite(true);
					dialog.setFileName(currentSetting.getName() + ".xml");
					dialog.setFilterExtensions(new String[]{"*.xml"});
					String fileName = dialog.open();
					if (fileName != null && !fileName.isEmpty()) {
						try {
							String xmlString = PreferenceUtils.marshalXML(currentSetting);
							FileWriter fileWriter = new FileWriter(fileName);
							fileWriter.write(xmlString);
							fileWriter.close();
						} catch (IOException e1) {
							logger.error("Could not write the selected preference settings to the given file: " + fileName, e1);
							MessageDialog.openError(getShell(), "Error", "Could not write the selected preference settings to the given file: " + fileName);
						} catch (JAXBException e1) {
							logger.error("Could not write the selected preference settings to the given file: " + fileName, e1);
							MessageDialog.openError(getShell(), "Error", "Could not write the selected preference settings to the given file: " + fileName);
						}
					}
				}
			};
		});
		
	}

	protected void clearValues() {
		cmbSelectSetting.select(0);
	}


	protected void addSeparatorLine1( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1);
		Label lblSeparator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);	
		lblSeparator.setLayoutData(gd1);
	}

	@Override
	protected void performApply() {
		save();
	}

	@Override
	public boolean performOk() {
		//need to save
		save();
		return true;
	}

	/**
	 * save values
	 */
	protected void save() {
		try {
			logger.debug("Time to save values!");
			preferences.saveValues();
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	protected void performDefaults() {
		boolean load = MessageDialog.openConfirm(getShell(), "Are you sure?", 
				"This will remove all the preferences you've created and load the default ones if any. Do you want to continue?");
		if (load) {
			preferences = new MSGlycanAnnotationSettingsPreference();
			preferences.loadDefaults();
			initStoredSettingList();
			setEditEnabled(false);
		}
	}
	
	protected boolean loadWorkspacePreferences() {
		try {
			preferences = PreferenceUtils.getMSGlycanAnnotationSettingsPreferences
					(MSGlycanAnnotationSettingsPreference.getPreferenceEntity(), MSGlycanAnnotationSettingsPreference.class);
		} catch (Exception ex) {
			logger.error("Error getting the mass spec preferences", ex);
		}
		return (preferences != null);
	}

	protected MSGlycanAnnotationSettingsPreferenceWizard getMSAnnotationSettingsPreferenceWizard(MSGlycanAnnotationPreference setting) {
		return new MSGlycanAnnotationSettingsPreferenceWizard(setting);
	}

	protected MSGlycanAnnotationSettingsPreferenceWizard getMSAnnotationSettingsPreferenceWizard(String preferenceName) {
		return new MSGlycanAnnotationSettingsPreferenceWizard(preferenceName);
	}
}
