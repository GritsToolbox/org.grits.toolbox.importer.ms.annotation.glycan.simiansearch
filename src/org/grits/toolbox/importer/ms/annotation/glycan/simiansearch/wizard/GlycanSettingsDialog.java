package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.utilShare.FileSelectionAdapter;
import org.grits.toolbox.core.utilShare.TextFieldUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.FilterSettingLibrary;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanFilterCateogoryPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanFilterPreference;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.utils.DatabaseUtils;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.utils.GlycanStructureDatabase;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.utils.GlycanStructureDatabaseIndex;
import org.grits.toolbox.ms.annotation.structure.GlycanPreDefinedOptions;
import org.grits.toolbox.ms.annotation.structure.StructureHandlerFileSystem;
import org.grits.toolbox.ms.om.data.GlycanFilter;
import org.grits.toolbox.ms.om.data.GlycanSettings;
import org.grits.toolbox.ms.om.data.ReducingEnd;
import org.grits.toolbox.util.structure.glycan.filter.om.Category;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;

public class GlycanSettingsDialog extends TitleAreaDialog {
	
	private static final Logger logger = Logger.getLogger(GlycanSettingsDialog.class);
    private final static int OTHER_REDEND_MAX_LENGTH = 15;
    
    private Combo cmbSelectFilter;
	private Button chkUseDatabaseMetaInfo;
	//private Button btnAdvancedSettings;
	private Combo cmbPerDeriv;
	private Combo cmbRedEnd;
	private Label lblredEndName;
	private Text txtRedEndName;
	private Label lblMass;
	private Text txtRedEndMass;
	private Combo cmbDatabases;
	private Text txtNewDb;
	private Button btnBrowse;
	
	private FilterSetting filterSetting = null;
	private Filtering filtering;
	//private GlycanAdvancedSettings gas;
	private FilterSettingLibrary preferenceFilterLibrary;
	private Category preferredFilterCategory;
	private ArrayList<String> databases;
	private HashMap<String, GlycanStructureDatabase> m_databaseIndex = new HashMap<String, GlycanStructureDatabase>();
	protected GlycanSettings gSettings;
	//private Method method;
	private String m_databasePath;
	
	boolean editingMode = false;
	private String dbVersion = "1.0";

	public GlycanSettingsDialog(Shell parentShell, FiltersLibrary filterLibrary) {
		super(parentShell);
		this.gSettings = new GlycanSettings();
        loadFilterPreferences();
        this.filtering = new Filtering(getShell(), filterLibrary, preferredFilterCategory, null);
        //this.gas = new GlycanAdvancedSettings(getShell());
	}
	
	public GlycanSettingsDialog(Shell parentShell, FiltersLibrary filterLibrary, GlycanSettings gSettings) {
		super(parentShell);
		this.gSettings = gSettings;
        loadFilterPreferences();
        this.filtering = new Filtering(getShell(), filterLibrary, preferredFilterCategory, null);
        //this.gas = new GlycanAdvancedSettings(getShell());
        this.editingMode = true;
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	private void loadFilterPreferences() {
		try {
			MSGlycanFilterPreference preferences = MSGlycanFilterPreference.getMSGlycanFilterPreferences (
					MSGlycanFilterPreference.getPreferenceEntity());
			if (preferences != null) 
				this.preferenceFilterLibrary = preferences.getFilterSettings();
			MSGlycanFilterCateogoryPreference categoryPreferences = MSGlycanFilterCateogoryPreference.getMSGlycanFilterCategoryPreferences (
					MSGlycanFilterCateogoryPreference.getPreferenceEntity());
			if (categoryPreferences != null) 
				preferredFilterCategory = categoryPreferences.getCategoryPreference();
		} catch (UnsupportedVersionException e) {
			logger.error("Cannot load filter preference");
		}
	}
	
	protected Label setMandatoryLabel(Label lable)
    {
        lable.setText(lable.getText() + "*");
        lable.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        return lable;
    }

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Database Settings");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(4, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		addDatabaseControls(container);
		addUseMetaInfoControls(container);
		addSelectFilterItem(container);
		addPerDerivTypeControls(container);
		addReducingEndControls(container);

		//addAdvancedSettingsControls(container);
		
		return container;
	}
	
	private void addUseMetaInfoControls(Composite container)
    {
        chkUseDatabaseMetaInfo = new Button(container, SWT.CHECK);
        chkUseDatabaseMetaInfo.setText("Use specified structure settings from database.");
        chkUseDatabaseMetaInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        if (editingMode) chkUseDatabaseMetaInfo.setSelection(gSettings.getFilter().getUseDatabaseStructureMetaInfo());
        
        chkUseDatabaseMetaInfo.addSelectionListener(new SelectionListener()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (cmbPerDeriv != null)
                {
                    cmbPerDeriv.setEnabled(!chkUseDatabaseMetaInfo.getSelection());
                }
                if (cmbRedEnd != null)
                {
                    cmbRedEnd.setEnabled(!chkUseDatabaseMetaInfo.getSelection());
                }
                /*
                if (btnAdvancedSettings != null)
                {
                    btnAdvancedSettings.setEnabled(!chkUseDatabaseMetaInfo.getSelection());
                }
                */
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        new Label(container, SWT.NONE);
    }
	
	/*private void addAdvancedSettingsControls(Composite container)
    {
        btnAdvancedSettings = new Button(container, SWT.NONE);
        btnAdvancedSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        btnAdvancedSettings.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                
                gas.open();
                gSettings.setAllowInnerFragments(gas.isAllowInnerFragment());
                method.setShift(gas.getShift());

            }
        });
        btnAdvancedSettings.setText("Advanced Settings");
    }*/


    private void addPerDerivTypeControls(Composite container)
    {
        Label lblPerdrivType = new Label(container, SWT.NONE);
        lblPerdrivType.setText("PerDeriv Type");
        lblPerdrivType = setMandatoryLabel(lblPerdrivType);

        cmbPerDeriv = new Combo(container, SWT.NONE);
        cmbPerDeriv.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        
        cmbPerDeriv.setItems(GlycanPreDefinedOptions.getAllDerivitizationTypes());
        cmbPerDeriv.select(0);
        
        if (editingMode) {
        	int selected = -1;
        	int i=0;
        	for (String item: cmbPerDeriv.getItems()) {
        		if (gSettings.getPerDerivatisationType().equals(item)) {
        			selected = i;
        			break;
        		}
        		i++;
        	}
        	if (selected != -1) cmbPerDeriv.select(selected);
        }
    }

    private void addReducingEndControls(Composite container)
    {
        Label lblReducingEnd = new Label(container, SWT.NONE);
        lblReducingEnd.setText("Reducing End");
        lblReducingEnd = setMandatoryLabel(lblReducingEnd);

        cmbRedEnd = new Combo(container, SWT.NONE);

        cmbRedEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        cmbRedEnd.setItems(GlycanPreDefinedOptions.getAllReducingEndTypes());
        cmbRedEnd.select(0);
        
        new Label(container, SWT.NONE);

        lblredEndName = new Label(container, SWT.NONE);
        lblredEndName.setText("Reducing End Name");

        txtRedEndName = new Text(container, SWT.BORDER);
        txtRedEndName.setEnabled(false);
        txtRedEndName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        new Label(container, SWT.NONE);

        lblMass = new Label(container, SWT.NONE);
        lblMass.setText("Reducing End Mass");

        txtRedEndMass = new Text(container, SWT.BORDER);
        txtRedEndMass.setEnabled(false);
        txtRedEndMass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        
        if (editingMode) {
        	int selected = -1;
        	int i=0;
        	if (gSettings.getReducingEnd() != null && !gSettings.getReducingEnd().getType().equals(GlycanPreDefinedOptions.OTHER)) {
	        	for (String item: cmbRedEnd.getItems()) {
	        		if (gSettings.getReducingEnd().getLabel().equals(item)) {
	        			selected = i;
	        			break;
	        		}
	        		i++;
	        	}
        	}
        	if (selected != -1) cmbRedEnd.select(selected);
        	else {
        		if (gSettings.getReducingEnd() != null && gSettings.getReducingEnd().getType().equals(GlycanPreDefinedOptions.OTHER)) {
        			cmbRedEnd.select(cmbRedEnd.getItemCount()-1);  // select "Other" - last option
        			txtRedEndName.setEnabled(true);
                    txtRedEndMass.setEnabled(true);
        			txtRedEndName.setText(gSettings.getReducingEnd().getLabel());
        			txtRedEndMass.setText(gSettings.getReducingEnd().getMass() + "");
        		}
        	}
        }
        
        txtRedEndName.addModifyListener(new ModifyListener()
        {

            @Override
            public void modifyText(ModifyEvent e)
            {
            	getButton(OK).setEnabled(validateInput());
            }
        });

        txtRedEndMass.addModifyListener(new ModifyListener()
        {

            @Override
            public void modifyText(ModifyEvent e)
            {
            	getButton(OK).setEnabled(validateInput());
            }
        });
        
        cmbRedEnd.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (cmbRedEnd.getItem(cmbRedEnd.getSelectionIndex()).equals("other"))
                {
                    txtRedEndName.setEnabled(true);
                    txtRedEndMass.setEnabled(true);
                }
                else
                {
                    txtRedEndName.setEnabled(false);
                    txtRedEndMass.setEnabled(false);
                }
                getButton(OK).setEnabled(validateInput());
            }
        });
    }

    private void addDatabaseControls(Composite container) {
        databases = new ArrayList<String>();
        try {
            GlycanStructureDatabaseIndex t_databaseIndex = DatabaseUtils.getGelatoDatabases();
            for (GlycanStructureDatabase t_db : t_databaseIndex.getDatabase()) {
                String t_nameString = t_db.getName() + " - " + t_db.getNumberOfStructures().toString() + " glycans";
                databases.add(t_nameString);
                this.m_databaseIndex.put(t_nameString, t_db);
            }
            this.m_databasePath = DatabaseUtils.getDatabasePath();
        }
        catch (IOException e)
        {
            logger.error("Unable to find GELATO database index", e);
        }
        catch (JAXBException e)
        {
            logger.error("XML format problem in GELATO database index", e);
        }
        databases.add(GlycanPreDefinedOptions.OTHER);
        String[] cmbDBs = databases.toArray(new String[databases.size()]);  
        int inx = 0;
        Label lblDatabase = new Label(container, SWT.NONE);
        lblDatabase.setText("Database");
        lblDatabase = setMandatoryLabel(lblDatabase);

        cmbDatabases = new Combo(container, SWT.NONE);

        GridData gd_cmbDatabases = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        gd_cmbDatabases.widthHint = 131;
        cmbDatabases.setLayoutData(gd_cmbDatabases);
        cmbDatabases.setItems(cmbDBs);
        cmbDatabases.select(inx);
        new Label(container, SWT.NONE);

        txtNewDb = new Text(container, SWT.BORDER);
        
        txtNewDb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        btnBrowse = new Button(container, SWT.PUSH);
        if( ! cmbDatabases.getItem(cmbDatabases.getSelectionIndex()).equals(GlycanPreDefinedOptions.OTHER) ) {
            btnBrowse.setEnabled(false);        	
            txtNewDb.setEnabled(false);
        } 
        btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        btnBrowse.setText("Browse");
        FileSelectionAdapter rawFileBrowserSelectionAdapter = new FileSelectionAdapter();
        rawFileBrowserSelectionAdapter.setShell(container.getShell());
        rawFileBrowserSelectionAdapter.setText(txtNewDb);
        btnBrowse.addSelectionListener(rawFileBrowserSelectionAdapter);

        new Label(container, SWT.NONE);
        
        if (editingMode) {
        	GlycanFilter filter = gSettings.getFilter();
        	if (filter != null && filter.getDatabase() != null) {
        		String dbNameString= null;
        		for (GlycanStructureDatabase t_db: m_databaseIndex.values()) {
        			if (t_db.getPath().contains(filter.getDatabase())) {
        				dbNameString = t_db.getName() + " - " + t_db.getNumberOfStructures().toString() + " glycans";
        				break;
        			}
        		}
        		if (dbNameString != null) {
        			int selected = -1;
        			int i=0;
        			for (String item: cmbDatabases.getItems()) {
        				if (dbNameString.equals(item)) {
        					selected = i;
        					break;
        				}
        				i++;
        			}
        			if (selected != -1)
        				cmbDatabases.select(selected);
        		}
        		else {
        			cmbDatabases.select(cmbDatabases.getItemCount()-1);  // select the last option "OTHER"
        			txtNewDb.setText(filter.getDatabase());
        		}
        	}
        }
        
        txtNewDb.addModifyListener(new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
            	getButton(OK).setEnabled(validateInput());
            }
        });
        
        cmbDatabases.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (cmbDatabases.getItem(cmbDatabases.getSelectionIndex()).equals("other"))
                {
                    txtNewDb.setEnabled(true);
                    btnBrowse.setEnabled(true);
                    getButton(OK).setEnabled(validateInput());
                }
                else
                {
                    txtNewDb.setEnabled(false);
                    btnBrowse.setEnabled(false);
                    getButton(OK).setEnabled(validateInput());
                }
            }

        });
    }

    private void addSelectFilterItem( Composite parent ) {
		GridData gd1 = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		Label lblSelectFilter = new Label(parent, SWT.NONE);
		lblSelectFilter.setText("Database Filter");
		lblSelectFilter.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		cmbSelectFilter = new Combo(parent, SWT.READ_ONLY);
		cmbSelectFilter.setLayoutData(gd2);
		initStoredFiltersList();
		
		if (editingMode) {
			if (gSettings.getFilterSetting() != null && gSettings.getFilterSetting().getName() != null) {
				// named filter, select from the combo
				int selected = -1;
				int i=0;
				for (String filter: cmbSelectFilter.getItems()) {
					if (gSettings.getFilterSetting().getName().equals(filter)) {
						selected = i;
						break;
					}
					i++;
				}
				if (selected != -1)
					cmbSelectFilter.select(selected);
			} else if (gSettings.getFilterSetting() != null) {
				// OTHER
				cmbSelectFilter.select(cmbSelectFilter.getItemCount()-1);
			}
		}
		
		cmbSelectFilter.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				processSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
    private void initStoredFiltersList() {
		cmbSelectFilter.removeAll();
		if ( preferenceFilterLibrary != null && preferenceFilterLibrary.getFilterSettings() != null ) {
			for (FilterSetting filter : preferenceFilterLibrary.getFilterSettings()) {
				cmbSelectFilter.add(filter.getName());
			}
		}
		cmbSelectFilter.add(GlycanPreDefinedOptions.OTHER);
		cmbSelectFilter.add("", 0);
	}
	
    private void processSelection() {
		if(!cmbSelectFilter.getText().trim().equals("") ) {
			if (cmbSelectFilter.getText().equals(GlycanPreDefinedOptions.OTHER)) {
				// open up filter dialog
				if (filtering != null && filtering.open() == Window.OK) {
                	filterSetting = filtering.getFilterSetting();
                	filtering.setFilterSetting (filterSetting);
                }
			}
			else {
				filterSetting = getCurrentFilter(cmbSelectFilter.getText().trim());
			}
		} else
			filterSetting = null;
	}
	
    private FilterSetting getCurrentFilter( String selFilter ) {
		if (preferenceFilterLibrary != null && preferenceFilterLibrary.getFilterSettings() != null) {
			for( int i = 0; i < preferenceFilterLibrary.getFilterSettings().size(); i++ ) {
				FilterSetting curFilter =  preferenceFilterLibrary.getFilterSettings().get(i);
				if( curFilter.getName().equals(selFilter) ) {
					return curFilter;
				}
	
			}
		}
		return null;
	}
    
    public boolean validateInput()
    {
        if (txtRedEndName != null && txtRedEndName.getEnabled())
        {
            if (TextFieldUtils.isEmpty(txtRedEndName))
            {
                setErrorMessage("Please enter a valid name for the reducing end.");
                return false;
            }
            else if (txtRedEndName.getText().trim().length() > OTHER_REDEND_MAX_LENGTH)
            {
                setErrorMessage("The maximum length for \"Other\" reducing end is " + OTHER_REDEND_MAX_LENGTH + ".");
                return false;
            }
        }
        if (txtRedEndMass != null && txtRedEndMass.getEnabled())
        {
            if (TextFieldUtils.isEmpty(txtRedEndMass) || !TextFieldUtils.isDouble(txtRedEndMass))
            {
                setErrorMessage("Please enter a valid mass for the reducing end ");
                return false;
            }
        }
        if (txtNewDb != null && txtNewDb.getEnabled())
        {
            if (TextFieldUtils.isEmpty(txtNewDb) || !isValidDatabase(txtNewDb.getText()))
            {
                setErrorMessage("Please select a valid database");
                return false;
            }
        }

        setErrorMessage(null);
        return true;

    }

    private boolean isValidDatabase(String dbName)
    {
        try
        {
            GlycanFilter filter = new GlycanFilter();
            if (dbName.indexOf(File.separator) == -1) {  // does not have the full path
				try {
					filter.setDatabase(DatabaseUtils.getDatabasePath() + File.separator + dbName);
				} catch (IOException e) {
					logger.error("Database path cannot be determined", e);
				}
			} else 
				filter.setDatabase(dbName);   // if "other" is selected, the dbName will contain the full path to the file
            StructureHandlerFileSystem handler = new StructureHandlerFileSystem();
            if (handler.getStructures(filter) == null)
                return false;
            else {
            	// get the version
            	dbVersion  = filter.getVersion();
                return true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    @Override
    protected void okPressed() {
    	if (validateInput()) {
    		saveGlycanSettings();
    		super.okPressed();
    	}
    }

	private void saveGlycanSettings() {
		
		String sSelectedDb = cmbDatabases.getText();
		String sSelectedPerDerivType = cmbPerDeriv.getText();
		String sSelectedRedEndName = cmbRedEnd.getText();
		String sOtherRedEndName = "";
		double dOtherRedEndMass = 0.0;
		if( cmbRedEnd.getText().equals(GlycanPreDefinedOptions.OTHER) ) {
			sOtherRedEndName = txtRedEndName.getText().trim();
			dOtherRedEndMass = Double.parseDouble(txtRedEndMass.getText().trim());
		}
		gSettings.setPerDerivatisationType(sSelectedPerDerivType);

        // Reducing End
        ReducingEnd re = new ReducingEnd();
        if ( sSelectedRedEndName.equalsIgnoreCase( GlycanPreDefinedOptions.OTHER) ) {
        	re.setLabel(sOtherRedEndName);
            re.setMass(dOtherRedEndMass);
            re.setType(GlycanPreDefinedOptions.OTHER);
        }
        else
        {
        	re.setLabel(sSelectedRedEndName);
        	re.setType(sSelectedRedEndName);
        }

        gSettings.setReducingEnd(re);

        GlycanStructureDatabase selectedDatabase = this.m_databaseIndex.get(sSelectedDb);
        
        GlycanFilter filter = new GlycanFilter();

        if (selectedDatabase == null)
        {
            // other
            filter.setDatabase(txtNewDb.getText());
        }
        else
        {
            filter.setDatabase(selectedDatabase.getFileName());
        }
        filter.setUseDatabaseStructureMetaInfo(chkUseDatabaseMetaInfo.getSelection());
        filter.setVersion(dbVersion != null ? dbVersion : "1.0");
        gSettings.setFilter(filter);
        
        // save filter settings
        gSettings.setFilterSetting(filterSetting);
	}
	
	public GlycanSettings getGlycanSettings () {
		return gSettings;
	}
}
