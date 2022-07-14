package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridEditor;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.utilShare.ListenerFactory;
import org.grits.toolbox.core.utilShare.SelectionInterface;
import org.grits.toolbox.core.utilShare.TextFieldUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationSettingsPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.util.PreferenceUtils;
import org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class GeneralInformationMulti extends WizardPage implements SelectionInterface {
	// setup bold font
	private static final Logger logger = Logger.getLogger(GeneralInformationMulti.class);
	protected final Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT); 

	private Composite container;
	private Text descriptionText;
	private Label descriptionLabel;
	private String description = "";

	private Grid massSpecGrid = null;
	private Button btnUp = null;
	private Button btnDown = null;
	private Label listLabel;
	private java.util.List<Entry> msEntryList = null;
	// create HashSet
	private HashMap<String, String> listEntries = new HashMap<String, String>();	
	private MassSpecChooserDialog massSpecChooser = null;
	// entry to file map
	private HashMap<String, MSPropertyDataFile> fileMap = new HashMap<>();

	// Name of annotation method
	private String annotationMethod;
	private Combo cmbSelectSetting;
	protected MSGlycanAnnotationSettingsPreference preferences;

	public final static String FILE_TYPE_GELATO = "GELATO";
	/**
	 * Constructor for GELATO annotation
	 * @param entries List of Entries
	 */
	public GeneralInformationMulti(	java.util.List<Entry> entries) {
		this(entries, "New MS Glycan Annotation", "Identify Glycans using GELATO.", FILE_TYPE_GELATO);
	}

	/**
	 * Constructor for using in other plug-in's annotation setting
	 * @author Masaaki Matsubara
	 * @param entries List of Entries
	 * @param title Title of this wizard page
	 * @param description Description of this wizard page
	 * @param annotMethod Name of annottaion method ("GELATO" is default)
	 */
	public GeneralInformationMulti(	java.util.List<Entry> entries, String title, String description, String annotMethod ) {
		super(title);
		setTitle(title);
		setDescription(description);
		if (entries == null) {
			this.msEntryList = new java.util.ArrayList<Entry>();
		} else {
			this.msEntryList = entries;
		}
		this.annotationMethod = annotMethod;
	}

	/**
	 * Loads preferences of type "preferenceClass" class and preference entity from the preferences file
	 * to be displayed in the preference selection drop-down
	 *  
	 * @param preferenceClass any class that extents MSGlycanAnnotationSettingsPreference
	 * @param preferenceEntity preference entity to be loaded
	 */
	public void loadPreferences(Class<? extends MSGlycanAnnotationSettingsPreference> preferenceClass, PreferenceEntity preferenceEntity) {
		try {
			preferences = PreferenceUtils.getMSGlycanAnnotationSettingsPreferences(preferenceEntity, preferenceClass);
		} catch (UnsupportedVersionException e) {
			logger.error("Cannot load annotation setting preferences", e);
		} catch (InstantiationException e) {
			logger.error("Cannot load annotation setting preferences", e);
		} catch (IllegalAccessException e) {
			logger.error("Cannot load annotation setting preferences", e);
		} catch (ClassNotFoundException e) {
			logger.error("Cannot load annotation setting preferences", e);
		}
	}

	public java.util.List<Entry> getMsEntryList() {
		return msEntryList;
	}

	public HashMap<String, String> getListEntries() {
		return listEntries;
	}
	
	public HashMap<String, MSPropertyDataFile> getFileMap() {
		return fileMap;
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(3, false));

		initGrid(container);		
		createListHeader(container);		
		createList(container);
		createAddAndDelButtons(container);
		createDescriptionHeader(container);
		createDescription(container);	
		createPreferenceChooser(container);
	}

	private void createDescriptionHeader(Composite parent) {
		/*
		 * third row starts:List
		 */
		GridData descriptionData = new GridData();
		descriptionLabel = new Label(parent, SWT.NONE);
		descriptionData.grabExcessHorizontalSpace = true;
		descriptionData.horizontalSpan = 7;
		descriptionLabel.setText("Description");
		descriptionLabel.setLayoutData(descriptionData);
	}

	private void createDescription(Composite parent) {
		GridData descriptionTextData = new GridData(GridData.FILL_BOTH);
		descriptionTextData.minimumHeight = 80;
		descriptionTextData.grabExcessHorizontalSpace = true;
		descriptionTextData.horizontalSpan = 7;
		descriptionText = new Text(parent, SWT.MULTI | SWT.V_SCROLL
				| SWT.BORDER);
		descriptionText.setLayoutData(descriptionTextData);
		descriptionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
		descriptionText.addTraverseListener(ListenerFactory
				.getTabTraverseListener());
		descriptionText.addKeyListener(ListenerFactory.getCTRLAListener());		
	}
	
	private void createPreferenceChooser(Composite parent) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		Label lblSelectSetting = new Label(parent, SWT.NONE);
		lblSelectSetting.setText("Preference Setting to Use");
		lblSelectSetting.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1);
		cmbSelectSetting = new Combo(parent, SWT.NONE);
		cmbSelectSetting.setLayoutData(gd2);
		initStoredSettingList();
		cmbSelectSetting.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (cmbSelectSetting.getSelectionIndex() != -1) {
					String preferenceName = cmbSelectSetting.getItem(cmbSelectSetting.getSelectionIndex());
					if (preferences != null && preferences.getPreferenceList() != null) {
						for (MSGlycanAnnotationPreference setting : preferences.getPreferenceList()) {
							if (setting.getName().equals(preferenceName)) {
								((MSGlycanAnnotationWizard) getWizard()).setPreferences(setting);
								if (isReadyToFinish()) {
									setPageComplete(true);
								} else {
									setPageComplete(false);
								}
								break;
							}
						}
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		if (cmbSelectSetting.getItemCount() > 0)
			cmbSelectSetting.select(0);
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

	private void initGrid(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 7;
		gridLayout.verticalSpacing = 10;
		parent.setLayout(gridLayout);
	}

	private void createListHeader(Composite parent) {
		/*
		 * third row starts:List
		 */
		GridData listLabelData = new GridData();
		listLabel = new Label(parent, SWT.NONE);
		listLabelData.grabExcessHorizontalSpace = true;
		listLabelData.horizontalSpan = 7;
		listLabel.setText("Mass Spec Analyses to Process");
		listLabel.setLayoutData(listLabelData);
		listLabel = setMandatoryLabel(listLabel);		
	}

	private void createList(Composite parent) {
		massSpecGrid = new Grid(parent, SWT.BORDER);	
		massSpecGrid.setHeaderVisible(true);

		GridColumn gridColumn = new GridColumn(massSpecGrid, SWT.NONE);
		gridColumn.setWidth(300);
		gridColumn.setText("Mass Spec Entry Name");

		GridColumn gridColumn2 = new GridColumn(massSpecGrid, SWT.NONE);
		gridColumn2.setWidth(300);
		gridColumn2.setText("Annotation Result Name");
		
		GridColumn gridColumn3 = new GridColumn(massSpecGrid, SWT.NONE);
		gridColumn3.setWidth(300);
		gridColumn3.setText("Annotation File");

		setListData();
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.minimumHeight = 80;
		gridData.horizontalSpan = 6;
		gridData.verticalSpan = 2;
		massSpecGrid.setLayoutData(gridData);

		btnUp = new Button(parent, SWT.ARROW | SWT.UP | SWT.BORDER);
		btnUp.setText("Up");
		GridData gdBtnUp = new GridData(SWT.END, SWT.BEGINNING, false, true);
		gdBtnUp.horizontalSpan = 1;
		btnUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				moveItem(SWT.UP);
			}
		});

		btnUp.setLayoutData(gdBtnUp);

		btnDown = new Button(parent,  SWT.ARROW | SWT.DOWN | SWT.BORDER);
		btnDown.setText("D");
		GridData gdBtnDown = new GridData(SWT.END, SWT.END, false, true);
		gdBtnDown.horizontalSpan = 1;
		btnDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				moveItem(SWT.DOWN);
			}
		});

		btnDown.setLayoutData(gdBtnDown);

		// update list
		massSpecGrid.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				if (isReadyToFinish()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});

		final GridEditor editor = new GridEditor(massSpecGrid);
		final GridEditor editor2 = new GridEditor(massSpecGrid);
		massSpecGrid.addMouseListener(new MouseAdapter() {
			public void mouseDown( MouseEvent e) {

				Point pt = new Point( e.x, e.y);

				final GridItem item = massSpecGrid.getItem( pt);
				final Point cell = massSpecGrid.getCell( pt);
				if ( item == null || cell == null)
					return;

				if( cell.x == 0 ) { // can't edit the first column!
					return;
				}

				massSpecGrid.deselect(cell.y);
				
				if (cell.x == 1) {  // alias column
					Control oldEditor = editor.getEditor();
					if ( oldEditor != null)
						oldEditor.dispose();
				
					// The control that will be the editor must be a child of the Table
					final Text newEditor = new Text( massSpecGrid, SWT.BORDER | SWT.SINGLE);
					String curText = item.getText(cell.x);
					newEditor.setText(curText);
					editor.setEditor( newEditor, item, cell.x);
					editor.grabHorizontal = true;
					editor.grabVertical = true;
					newEditor.addKeyListener(new KeyListener() {
	
						@Override
						public void keyReleased(KeyEvent e) {
							item.setText(cell.x, newEditor.getText());		
							listEntries.put( item.getText(0), newEditor.getText() );
							if (isReadyToFinish()) {
								setPageComplete(true);
							} else {
								setPageComplete(false);
							}
						}
	
						@Override
						public void keyPressed(KeyEvent e) {					
						}
					});
					newEditor.forceFocus();
					newEditor.setSelection(newEditor.getText().length());
					newEditor.selectAll();
				} else if (cell.x == 2) { // file column
					Control oldEditor = editor2.getEditor();
					if ( oldEditor != null)
						oldEditor.dispose();
					ComboViewer fileCombo = new ComboViewer(massSpecGrid, SWT.NONE);
					fileCombo.setContentProvider(new ArrayContentProvider());
					fileCombo.setLabelProvider(new LabelProvider() {
					    @Override
					    public String getText(Object element) {
					        if (element instanceof MSPropertyDataFile) {
					        	String fileName = ((MSPropertyDataFile) element).getName();
					        	if (!fileName.isEmpty() && fileName.contains(File.separator)) 
					        		fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
					        	return fileName;
					        }
					        return super.getText(element);
					    }
					});
					
					List<MSPropertyDataFile> fileList = getAnnotationFilesForEntry(item.getText(0));
					fileCombo.setInput(fileList);
					editor2.minimumWidth = 100;
					editor2.grabHorizontal = true;
					editor2.grabVertical = true;
					editor2.setEditor(fileCombo.getCombo(), item, cell.x);
					
					fileCombo.addSelectionChangedListener(new ISelectionChangedListener() {
						@Override
						public void selectionChanged (SelectionChangedEvent event) {
							IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					        if (selection.size() > 0) {
					        	item.setText(cell.x, ((MSPropertyDataFile) selection.getFirstElement()).getName());	
					            fileMap.put(item.getText(0), (MSPropertyDataFile) selection.getFirstElement());
					        }
					        if (isReadyToFinish()) {
								setPageComplete(true);
							} else {
								setPageComplete(false);
							}
							
						}
					}); 
					fileCombo.setSelection(new StructuredSelection(fileList.get(0)));
				}
			}
		});
	}	
	
	/**
	 * get all files that can be used for annotation for the entry given by the display name
	 * 
	 * @param displayName name of the entry 
	 * @return all files that can be used for annotation
	 */
	private List<MSPropertyDataFile> getAnnotationFilesForEntry(String displayName) {
		List<MSPropertyDataFile> annotationFiles = new ArrayList<>();
		for (Entry e: msEntryList) {
			if (e.getDisplayName().equals(displayName)) {
				Property prop = e.getProperty();
				if (prop instanceof MassSpecProperty) {
					List<MSPropertyDataFile> files = ((MassSpecProperty) prop).getMassSpecMetaData().getAnnotationFiles();
					for (MSPropertyDataFile propertyDataFile : files) {
						annotationFiles.add(propertyDataFile);
					}
				}
				break;
			}
		}
		return annotationFiles;
	}

	/**
	 * get all the file names that can be used for annotation for the entry
	 * 
	 * @param displayName name of the entry
	 * @return an array of filenames
	 */
	String[] getAnnotationFileNamesForEntry (String displayName) {
		List<String> fileNames = new ArrayList<>();
		for (Entry e: msEntryList) {
			if (e.getDisplayName().equals(displayName)) {
				Property prop = e.getProperty();
				if (prop instanceof MassSpecProperty) {
					List<MSPropertyDataFile> files = ((MassSpecProperty) prop).getMassSpecMetaData().getAnnotationFiles();
					for (MSPropertyDataFile propertyDataFile : files) {
						fileNames.add(propertyDataFile.getName());
					}
				}
				break;
			}
		}
		return fileNames.toArray(new String[fileNames.size()]);
	}

	public static String getAnnotationPostfix(String annotationMethod) {
		long timeNow = System.currentTimeMillis();
		String secs = Long.toString(timeNow);
		secs = secs.substring(9);

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		int day = calendar.get(Calendar.DATE);
		//Note: +1 the month for current month
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		String stamp = year + "." + month + "." + day + "_" + secs;
		return "."+annotationMethod+"." + stamp;
	}

	private void setListData() {
		for (int i = 0; i < msEntryList.size(); i++) {
			Entry entry = msEntryList.get(i);
			String displayName = entry.getDisplayName();
			String stamp = GeneralInformationMulti.getAnnotationPostfix(this.annotationMethod);
			String annotName = displayName + stamp;
			listEntries.put(displayName, annotName);
			GridItem item = new GridItem(massSpecGrid, SWT.NONE);

			item.setText(0, displayName);

			item.setBackground(0, Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

			item.setText(1, annotName);
			
			// select the first available file by default
			List<MSPropertyDataFile> fileList = getAnnotationFilesForEntry(displayName);
			if (fileList != null && !fileList.isEmpty()) {
				String fileName = fileList.get(0).getName();
	        	if (!fileName.isEmpty() && fileName.contains(File.separator)) 
	        		fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
				item.setText(2, fileName);
				fileMap.put(displayName, fileList.get(0));
			}
		}	
		
		if (isReadyToFinish()) {
			setPageComplete(true);
		} else {
			setPageComplete(false);
		}
			
	}

	private void moveItem( int direction ) {
		int iFoundPos = massSpecGrid.getSelectionIndex();
		if( iFoundPos < 0 ) {
			return;
		}
		GridItem foundItem = massSpecGrid.getItem(iFoundPos);
		GridItem switchItem = null;
		int iSwitchInx = -1;
		if( direction == SWT.UP ) {
			if( iFoundPos == 0 ) {
				return;
			}
			iSwitchInx = iFoundPos - 1;
		} else {
			if( iFoundPos == massSpecGrid.getItemCount() - 1) {
				return;
			}
			iSwitchInx = iFoundPos + 1;
		}
		switchItem = massSpecGrid.getItem(iSwitchInx);
		String s0 = switchItem.getText(0);
		String s1 = switchItem.getText(1);

		switchItem.setText(0, foundItem.getText(0));
		switchItem.setText(1, foundItem.getText(1));

		foundItem.setText(0, s0);
		foundItem.setText(1, s1);
		massSpecGrid.setSelection(iSwitchInx);

		Entry curEntry = msEntryList.remove(iFoundPos);
		msEntryList.add(iSwitchInx, curEntry);
		logger.debug("Done with move");
	}

	private void createAddAndDelButtons(Composite parent) {
		// create a grdiData for OKButton
		Label dummy = new Label(parent, SWT.NONE);
		GridData gdDummy = new GridData();
		dummy.setLayoutData(gdDummy);

		Label dummy2 = new Label(parent, SWT.NONE);
		GridData gdDummy2 = new GridData();
		gdDummy2.horizontalSpan = 2;
		gdDummy2.grabExcessHorizontalSpace = true;
		dummy2.setLayoutData(gdDummy2);

		Label dummy3 = new Label(parent, SWT.NONE);
		GridData gdDummy3 = new GridData();
		dummy3.setLayoutData(gdDummy3);

		GridData gdAddBtn = new GridData();
		gdAddBtn.grabExcessHorizontalSpace = false;
		gdAddBtn.horizontalAlignment = GridData.END;
		gdAddBtn.horizontalSpan = 1;
		Button btnAddButton = new Button(parent, SWT.PUSH);
		btnAddButton.setText("  Add  ");
		massSpecChooser = new MassSpecChooserDialog(
				MSAnnotationProperty.TYPE, "Select MS Entry",
				"Select an MS Entry", this.annotationMethod);
		massSpecChooser.setParent(parent);
		massSpecChooser.setList(massSpecGrid);
		massSpecChooser.setListEntries(listEntries);
		massSpecChooser.setEntries(msEntryList);
		btnAddButton.addSelectionListener(massSpecChooser);
		btnAddButton.setLayoutData(gdAddBtn);

		Button deleteButton = new Button(parent, SWT.PUSH);
		GridData gdDelBtn = new GridData();
		gdDelBtn.grabExcessHorizontalSpace = false;
		gdDelBtn.horizontalAlignment = GridData.END;
		gdDelBtn.horizontalSpan = 1;
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(gdDelBtn);
		deleteButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// then delete selected items from the list
				int iRemoveInx = massSpecGrid.getSelectionIndex();
				if (iRemoveInx == -1)
					return;
				fileMap.remove(massSpecGrid.getItem(iRemoveInx).getText(0).trim());
				msEntryList.remove(iRemoveInx);
				massSpecGrid.remove(iRemoveInx);
				listEntries.clear();
				
				for (int i = 0; i < massSpecGrid.getItemCount(); i++) {
					String displayName = massSpecGrid.getItem(i).getText(0).trim();
					String alias = massSpecGrid.getItem(i).getText(1).trim();
					listEntries.put(displayName, alias);
				}

				// if list is empty should disable OKbutton
				if (listEntries.isEmpty()) {
					setPageComplete(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		Label dummy4 = new Label(parent, SWT.NONE);		
	}


	protected Label createSeparator(Composite container, int span) {
		GridData separatorData = new GridData();
		separatorData.grabExcessHorizontalSpace = true;
		separatorData.horizontalAlignment = GridData.FILL;
		separatorData.horizontalSpan = span;
		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(separatorData);
		return separator;
	}

	protected Label setMandatoryLabel(Label lable) {
		lable.setText(lable.getText()+"*");
		lable.setFont(boldFont);
		return lable;
	}

	public void setMassSpecEntryList(java.util.List<Entry> msEntryList) {
		this.msEntryList = msEntryList;
	}

	public static boolean isDuplicateDisplayName( String _sDisplayName, Entry root ) {
		List<Entry> alStack = new ArrayList<>();
		alStack.add(root);
		while( ! alStack.isEmpty() ) {
			Entry curEntry = alStack.remove(0);
			if( curEntry.getDisplayName().trim().equals(_sDisplayName) ) {
				return true;
			}
			List<Entry> children = curEntry.getChildren();
			for(Entry child : children) {
				alStack.add(child);
			}
		}
		return false;		
	}
	

	/**
	 * Check if ready to finish
	 * @return
	 */
	private boolean isReadyToFinish() {
		if( msEntryList == null || msEntryList.isEmpty() || listEntries == null || listEntries.isEmpty() ) {
			setErrorMessage("Please select one or more MS entries for annotation.");
			return false;
		}
		Entry root = PropertyHandler.getDataModel().getRoot();
		Set<String> sKeys = listEntries.keySet();
		Iterator<String> itr = sKeys.iterator();
		List<String> newEntries = new ArrayList<String>();
		while( itr.hasNext() ) {
			String sEntryName = itr.next();
			String sNameText = listEntries.get(sEntryName);
			MSPropertyDataFile file = fileMap.get(sEntryName);
			if ( sNameText.trim().equals("") ) {
				setErrorMessage("Please enter a name for entry: " + sEntryName);
				return false;			
			}
			//name should be at least 1 character
			if( sNameText.trim().length() >= 128) {
				setErrorMessage("Name for entry: " + sEntryName + " must be less than 128 characters.");
				return false;
			}	
			if (file == null || file.getName() == null || file.getName().isEmpty()) {
				// check if there are no annotation files, display a different error message
				if (getAnnotationFilesForEntry(sEntryName).isEmpty()) 
					setErrorMessage("There are no files that can be used for annotation for entry: " + sEntryName + ". \nPlease go back to the entry and convert instrument files as necessary!");
				else setErrorMessage("Please choose a file for annotation for entry: " + sEntryName);
				return false;
			}
			//name should be unique within the entry, (Sena) removed checking within the same list ticket #525 
			if( /*newEntries.contains(sNameText) || */ isDuplicateDisplayName(sNameText, root) ) {
				setErrorMessage("Name for entry: " + sEntryName + " (" + sNameText + ") already exists.");
				return false;
			}	
			newEntries.add(sNameText);
		}

		if( descriptionText != null && ! TextFieldUtils.isEmpty(descriptionText) && 
				descriptionText.getText().trim().length() >= Integer.parseInt(PropertyHandler.getVariable("descriptionLength"))) {
			setErrorMessage("Description must be less than 1024 characters.");
			return false;
		}

		setErrorMessage(null);
		return true;
	}

	public void save() {
		//need to save variables
		this.description = descriptionText.getText().trim();
	}

	public IWizardPage getNextPage() {
		save();
		return super.getNextPage();
	}

	public String getMSDescription() {
		return description;
	}

	public void setMSDescription(String description) {
		this.description = description;
	}

	@Override
	public void updateComponent(SelectionAdapter adapter) {
		// TODO Auto-generated method stub

	}

}