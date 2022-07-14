package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.grits.toolbox.core.utilShare.TextFieldUtils;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationPreference;
import org.grits.toolbox.ms.file.reader.IMSFileReader;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;

/**
 *
 * @author Khalifeh AlJadda
 *
 */
public class GlycanSettingsForm extends WizardPage implements IPropertyChangeListener
{
    // log4J Logger
    private static final Logger logger = Logger.getLogger(GlycanSettingsForm.class);
    protected final Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
    private Text txtAccuracy;
    private Combo cmbMono;
    private Combo cmbAccuracy;
    private boolean readyToFinish = false;
    private Method method = null;
    private MSGlycanAnnotationWizard myWizard;
    private String displayName = "";
    private Label lblAccuracyMsn;
    private Text txtMsnAccuracy;
    private Combo cmbMsnAccuracyType;
    private Button chkTrustCharge;
    private double cutOff = 0.0;
    private String cutOffType = null;
    private double cutOffPrecursor = 0.0;
    private String cutOffTypePrecursor = null;
    private Label label;
    private Text txtCutOff;
    private Combo cmbCutOffType;
    private Text txtCutOffPrecursor;
    private Combo cmbCutOffTypePrecursor;
    private Button chkUseDatabaseMetaInfo;
    private String sMassType = null;
    private Double dPrecursorAccuracy = null;
    private boolean bPrecursorAccuracyIsPPM = true;
    private Double dFragmentAccuracy = null;
    private boolean bFragmentAccuracyIsPPM = true;
    private boolean bTrustCharge = false;
	private GlycanSettingsTableWithActions databaseSettings;
	private FiltersLibrary filterLibrary;
	private double dIntensityCutoff;
	private double dPrecursorIntensityCutoff;
	private ModifyListener txtCutOffPrecursorListener;
	private SelectionAdapter cmbCutOffTypePrecursotSelectionListener;
	private ModifyListener txtCutOffListener;
	private SelectionAdapter cmbCutOffTypeListener;
	private ModifyListener txtAccuracyListener;
	private ModifyListener txtMsnAccuracyListener;
    
    /**
     * Create the wizard.
     */
    public GlycanSettingsForm(Method method, MSGlycanAnnotationWizard wizard, FiltersLibrary filterLibrary)
    {
        this("glycanSettings", method, wizard, filterLibrary);
        setTitle("Glycan Settings");
        setDescription("Describe general glycan settings");
	}
    
    /**
     * Create the wizard with page name.
     * @author Masaaki Matsubara
     * @param pageName - String of this page name
     * @param method - Method to be set information in this page
     * @param wizard - MSGlycanAnnotationWizard having this page
     * @param filterLibrary - FilerLibrary for the glycan database
     */
    public GlycanSettingsForm(String pageName, Method method, MSGlycanAnnotationWizard wizard, FiltersLibrary filterLibrary)
    {
    	super(pageName);
        setPageComplete(false);
        this.method = method;
        method.setShift(0.0);
        this.myWizard = wizard;
        this.filterLibrary = filterLibrary;
    }
        
    protected Label setMandatoryLabel(Label lable)
    {
        lable.setText(lable.getText() + "*");
        lable.setFont(boldFont);
        return lable;
    }

    /**
     * Create contents of the wizard.
     *
     * @param parent
     */
    @Override
    public void createControl(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NONE);
        container.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));

        setControl(container);
        GridLayout gl_container = new GridLayout(3, false);
        gl_container.verticalSpacing = 10;
        container.setLayout(gl_container);

        addMassTypeControls(container);
        addAccuracyControls(container);
        addIntensityCutoffControls(container);
        addTrustChargeControls(container); 
        addDatabaseSettings(container);
       
        setControl(container);

        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        setPageComplete(false);
        if (validateInput())
        {
            canFlipToNextPage();
        }
        else
        {
            readyToFinish = false;
            canFlipToNextPage();
        }
    }
    
    protected void addDatabaseSettings(Composite container) {
    	databaseSettings = getSettingsTable(container);
        databaseSettings.setFilterLibrary (this.filterLibrary);
        databaseSettings.setPreferences(myWizard.getPreferences());
        databaseSettings.initComponents();
    }
    
    protected GlycanSettingsTableWithActions getSettingsTable (Composite container) {
    	return new GlycanSettingsTableWithActions(container, SWT.NONE, this);
    }

    public boolean useMetaInfoControls()
    {
        return chkUseDatabaseMetaInfo.getSelection();
    }

    private void addTrustChargeControls(Composite container)
    {
        label = new Label(container, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        label.setText("Charge Assignment");        
        chkTrustCharge = new Button(container, SWT.CHECK);
        chkTrustCharge.setText("Trust Charge in MS XML File (mzML, mzXML)");
        boolean bValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isTrustCharge() : true;
        chkTrustCharge.setSelection(bValue);
        new Label(container, SWT.NONE);
    }

    private void addIntensityCutoffControls(Composite container)
    {
        label = new Label(container, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        label.setText("Precursor Intensity Cut-Off");        
        
        txtCutOffPrecursor = new Text(container, SWT.BORDER);
        Double dPreValue = (myWizard.getPreferences() != null && myWizard.getPreferences().getPrecursorIntensityCutoff() != null) ? 
        		myWizard.getPreferences().getPrecursorIntensityCutoff() : 0.0d;
        setCutOffPrecursor(dPreValue);
        String sPreValue = dPreValue > 0.0 ? dPreValue.toString() : "";
        txtCutOffPrecursor.setText(sPreValue);
        txtCutOffPrecursorListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                if (validateInput())
                {
                	double dVal = ! txtCutOffPrecursor.getText().isEmpty() ? Double.parseDouble(txtCutOffPrecursor.getText()) : 0.0;
                    setCutOffPrecursor(dVal);
                    setCutOffTypePrecursor(cmbCutOffTypePrecursor.getItem(cmbCutOffTypePrecursor.getSelectionIndex()));
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }
                else
                {
                    readyToFinish = false;
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }

            }
        };
        txtCutOffPrecursor.addModifyListener(txtCutOffPrecursorListener);
        txtCutOffPrecursor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        cmbCutOffTypePrecursor = new Combo(container, SWT.NONE);
        
        cmbCutOffTypePrecursotSelectionListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (validateInput())
                {
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }
                else
                {
                    readyToFinish = false;
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }

            }
        };
        cmbCutOffTypePrecursor.addSelectionListener(cmbCutOffTypePrecursotSelectionListener);
        cmbCutOffTypePrecursor.setItems(new String[] { IMSFileReader.FILTER_PERCENTAGE, IMSFileReader.FILTER_ABSOLUTE });
        cmbCutOffTypePrecursor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        boolean bPreValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isPrecursorIntensityCutoffIsPercentage() : true;
        if( bPreValue ) {
        	cmbCutOffTypePrecursor.select(0);
        } else {
        	cmbCutOffTypePrecursor.select(1);
        }
        setCutOffTypePrecursor(cmbCutOffTypePrecursor.getItem(cmbCutOffTypePrecursor.getSelectionIndex()));    	
        
        label = new Label(container, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        label.setText("Fragment Intensity Cut-Off");

        txtCutOff = new Text(container, SWT.BORDER);
        Double dValue = (myWizard.getPreferences() != null && myWizard.getPreferences().getIntensityCutoff() != null) ? 
        		myWizard.getPreferences().getIntensityCutoff() : 0.0d;
        setCutOff(dValue);
        String sValue = dValue > 0.0 ? dValue.toString() : "";
        txtCutOff.setText(sValue);
        txtCutOffListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                if (validateInput())
                {
                	double dVal = ! txtCutOff.getText().isEmpty() ? Double.parseDouble(txtCutOff.getText()) : 0.0;
                    setCutOff(dVal);
                    setCutOffType(cmbCutOffType.getItem(cmbCutOffType.getSelectionIndex()));
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }
                else
                {
                    readyToFinish = false;
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }

            }
        };
        txtCutOff.addModifyListener(txtCutOffListener);
        txtCutOff.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        cmbCutOffType = new Combo(container, SWT.NONE);
        cmbCutOffTypeListener = new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (validateInput())
                {
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }
                else
                {
                    readyToFinish = false;
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }

            }
        };
        cmbCutOffType.addSelectionListener(cmbCutOffTypeListener);
        cmbCutOffType.setItems(new String[] { IMSFileReader.FILTER_PERCENTAGE, IMSFileReader.FILTER_ABSOLUTE });
        cmbCutOffType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        boolean bValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isIntensityCutoffIsPercentage() : true;
        if( bValue ) {
        	cmbCutOffType.select(0);
        } else {
        	cmbCutOffType.select(1);
        }      
        setCutOffType(cmbCutOffType.getItem(cmbCutOffType.getSelectionIndex()));
    }

    private void addAccuracyControls(Composite container)
    {
        Label lblAccuracy = new Label(container, SWT.NONE);
        lblAccuracy.setText("Accuracy MS");
        lblAccuracy = setMandatoryLabel(lblAccuracy);

        txtAccuracy = new Text(container, SWT.BORDER);
        String sValue = (myWizard.getPreferences() != null && myWizard.getPreferences().getPrecursorTol() != null) ? 
        		myWizard.getPreferences().getPrecursorTol().toString() : "500";
        txtAccuracy.setText( sValue );
        GridData gd_txtAccuracy = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_txtAccuracy.widthHint = 131;
        txtAccuracy.setLayoutData(gd_txtAccuracy);
        txtAccuracyListener = new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                if (validateInput())
                {
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }
                else
                {
                    readyToFinish = false;
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }

            }
        };
        txtAccuracy.addModifyListener(txtAccuracyListener);

        cmbAccuracy = new Combo(container, SWT.NONE);
        GridData gd_cmbAccuracy = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_cmbAccuracy.widthHint = 131;
        cmbAccuracy.setLayoutData(gd_cmbAccuracy);
        cmbAccuracy.setItems(new String[] { MSGlycanAnnotationPreference.DALTON, MSGlycanAnnotationPreference.PPM });
        boolean bValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isPrecursorTolIsPPM() : true;
        if( bValue ) {
        	cmbAccuracy.select(1);
        } else {
        	cmbAccuracy.select(0);
        }

        lblAccuracyMsn = new Label(container, SWT.NONE);
        lblAccuracyMsn.setText("Accuracy MSn");

        txtMsnAccuracy = new Text(container, SWT.BORDER);
        sValue = (myWizard.getPreferences() != null && myWizard.getPreferences().getFragmentTol() != null) ? 
        		myWizard.getPreferences().getFragmentTol().toString() : "500";
        txtMsnAccuracy.setText(sValue);
        txtMsnAccuracyListener = new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                if (validateInput())
                {
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }
                else
                {
                    readyToFinish = false;
                    canFlipToNextPage();
                    getWizard().getContainer().updateButtons();
                }

            }
        };
        txtMsnAccuracy.addModifyListener(txtMsnAccuracyListener);

        txtMsnAccuracy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        cmbMsnAccuracyType = new Combo(container, SWT.NONE);
        cmbMsnAccuracyType.setItems(new String[] { MSGlycanAnnotationPreference.DALTON, MSGlycanAnnotationPreference.PPM });
        cmbMsnAccuracyType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        bValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isFragmentTolIsPPM() : true;
        if( bValue ) {
        	cmbMsnAccuracyType.select(1);
        } else {
        	cmbMsnAccuracyType.select(0);
        }
    }

    private void addMassTypeControls(Composite container)
    {
        Label lblMonoisotopic = new Label(container, SWT.NONE);
        lblMonoisotopic.setText("Mass Type");
        lblMonoisotopic = setMandatoryLabel(lblMonoisotopic);

        cmbMono = new Combo(container, SWT.NONE);
        cmbMono.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        cmbMono.setItems(new String[] { "Monoisotopic", "Average" });
        String sValue = myWizard.getPreferences() != null ? myWizard.getPreferences().getMassType() : "Monoisotopic";  // default is Monoisotopic
        if( sValue.equalsIgnoreCase("Monoisotopic") ) {
        	cmbMono.select(0);
        } else {
        	cmbMono.select(1);
        }
    } 
	
    @Override
    public IWizardPage getNextPage()
    {
        save();
        return super.getNextPage();
    }
    
    /**
     * This method updates the values of all controls with the current values from the preferences
     * used in editing
     */
    public void updateControlsFromPreferences() {
    	if (myWizard.getPreferences() != null && myWizard.getPreferences().getMethod() != null) {
    		removeListeners();
    		if (databaseSettings != null)
    			databaseSettings.setAnalyteSettings(myWizard.getPreferences().getMethod().getAnalyteSettings());
    		boolean bValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isTrustCharge() : true;
            chkTrustCharge.setSelection(bValue);
            Double dPreValue = (myWizard.getPreferences() != null && myWizard.getPreferences().getPrecursorIntensityCutoff() != null) ? 
            		myWizard.getPreferences().getPrecursorIntensityCutoff() : 0.0d;
            setCutOffPrecursor(dPreValue);
            String sPreValue = dPreValue > 0.0 ? dPreValue.toString() : "";
            txtCutOffPrecursor.setText(sPreValue);
            boolean bPreValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isPrecursorIntensityCutoffIsPercentage() : true;
            if( bPreValue ) {
            	cmbCutOffTypePrecursor.select(0);
            } else {
            	cmbCutOffTypePrecursor.select(1);
            }
            setCutOffTypePrecursor(cmbCutOffTypePrecursor.getItem(cmbCutOffTypePrecursor.getSelectionIndex()));  
            Double dValue = (myWizard.getPreferences() != null && myWizard.getPreferences().getIntensityCutoff() != null) ? 
            		myWizard.getPreferences().getIntensityCutoff() : 0.0d;
            setCutOff(dValue);
            String sValue = dValue > 0.0 ? dValue.toString() : "";
            txtCutOff.setText(sValue);
            bValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isIntensityCutoffIsPercentage() : true;
            if( bValue ) {
            	cmbCutOffType.select(0);
            } else {
            	cmbCutOffType.select(1);
            }      
            setCutOffType(cmbCutOffType.getItem(cmbCutOffType.getSelectionIndex()));
            sValue = (myWizard.getPreferences() != null && myWizard.getPreferences().getPrecursorTol() != null) ? 
            		myWizard.getPreferences().getPrecursorTol().toString() : "500";
            txtAccuracy.setText( sValue );
            bValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isPrecursorTolIsPPM() : true;
            if( bValue ) {
            	cmbAccuracy.select(1);
            } else {
            	cmbAccuracy.select(0);
            }
            sValue = (myWizard.getPreferences() != null && myWizard.getPreferences().getFragmentTol() != null) ? 
            		myWizard.getPreferences().getFragmentTol().toString() : "500";
            txtMsnAccuracy.setText(sValue);
            bValue = myWizard.getPreferences() != null ? myWizard.getPreferences().isFragmentTolIsPPM() : true;
            if( bValue ) {
            	cmbMsnAccuracyType.select(1);
            } else {
            	cmbMsnAccuracyType.select(0);
            }
            sValue = myWizard.getPreferences() != null ? myWizard.getPreferences().getMassType() : "";
            if( sValue.equalsIgnoreCase("Monoisotopic") ) {
            	cmbMono.select(0);
            } else {
            	cmbMono.select(1);
            }
            setErrorMessage(null);
            readyToFinish = true;
            addListeners();
    	}
    }
    
    private void addListeners() {
    	txtCutOffPrecursor.addModifyListener(txtCutOffPrecursorListener);
    	cmbCutOffTypePrecursor.addSelectionListener(cmbCutOffTypePrecursotSelectionListener);
    	txtCutOff.addModifyListener(txtCutOffListener);
    	cmbCutOffType.addSelectionListener(cmbCutOffTypeListener);
    	txtAccuracy.addModifyListener(txtAccuracyListener);
    	txtMsnAccuracy.addModifyListener(txtMsnAccuracyListener);
	}

	private void removeListeners() {
		txtCutOffPrecursor.removeModifyListener(txtCutOffPrecursorListener);
		cmbCutOffTypePrecursor.removeSelectionListener(cmbCutOffTypePrecursotSelectionListener);
		txtCutOff.removeModifyListener(txtCutOffListener);
		cmbCutOffType.removeSelectionListener(cmbCutOffTypeListener);
		txtAccuracy.removeModifyListener(txtAccuracyListener);
		txtMsnAccuracy.removeModifyListener(txtMsnAccuracyListener);
	}
    
    public void save() {
		sMassType = cmbMono.getText();
		dPrecursorAccuracy = Double.parseDouble(txtAccuracy.getText());
		bPrecursorAccuracyIsPPM = cmbAccuracy.getText().equals(MSGlycanAnnotationPreference.PPM);
		dFragmentAccuracy = Double.parseDouble(txtMsnAccuracy.getText());
		bFragmentAccuracyIsPPM = cmbMsnAccuracyType.getText().equals(MSGlycanAnnotationPreference.PPM);
		dIntensityCutoff = 0.0;
		if( ! txtCutOff.getText().trim().equals("") ) {
			dIntensityCutoff = Double.parseDouble( txtCutOff.getText().trim() );
		}
		dPrecursorIntensityCutoff = 0.0;
		if( ! txtCutOffPrecursor.getText().trim().equals("") ) {
			dPrecursorIntensityCutoff = Double.parseDouble( txtCutOffPrecursor.getText().trim() );
		}

		bTrustCharge = chkTrustCharge.getSelection();
    	
        method.setAccuracy(dPrecursorAccuracy);
        method.setFragAccuracy(dFragmentAccuracy);
        method.setAccuracyPpm(bPrecursorAccuracyIsPPM);
        method.setFragAccuracyPpm(bFragmentAccuracyIsPPM);
        method.setTrustMzCharge(bTrustCharge);
        if (sMassType.equalsIgnoreCase("Monoisotopic"))
            method.setMonoisotopic(true);
        else
            method.setMonoisotopic(false);
        method.setAnnotationType(Method.ANNOTATION_TYPE_GLYCAN);
        method.setAnnotationSource(Method.ANNOTATION_SRC_SIMIAN);
        method.setIntensityCutoff(dIntensityCutoff);
        method.setIntensityCutoffType(cmbCutOffType.getText());
        method.setPrecursorIntensityCutoff(dPrecursorIntensityCutoff);
        method.setPrecursorIntensityCutoffType(cmbCutOffTypePrecursor.getText());
        if (databaseSettings != null)
        	method.setAnalyteSettings(databaseSettings.getAnalyteSettings());
    }

    @Override
    public boolean canFlipToNextPage()
    {
        if (readyToFinish)
        {
            return true;
        }
        else
            return false;
    }

    public boolean canFinish()
    {
        return readyToFinish;
    }

    public boolean validateInput()
    {
        if (TextFieldUtils.isEmpty(txtAccuracy) || !TextFieldUtils.isDouble(txtAccuracy))
        {
            setErrorMessage("Enter a valid number in the MS Accuracy field");
            return false;
        }
        if (TextFieldUtils.isEmpty(txtMsnAccuracy) || !TextFieldUtils.isDouble(txtMsnAccuracy))
        {
            setErrorMessage("Enter a valid number in the Msn Accuracy field");
            return false;
        }
        if (!TextFieldUtils.isEmpty(txtCutOff) && !TextFieldUtils.isNonZero(txtCutOff))
        {
            setErrorMessage("Please input a valid fragment cutoff value");
            return false;
        }
        if (cmbCutOffType.getSelectionIndex() != -1 && cmbCutOffType.getItem(cmbCutOffType.getSelectionIndex()).equals(IMSFileReader.FILTER_PERCENTAGE))
        {
            if (!TextFieldUtils.isEmpty(txtCutOff) && !TextFieldUtils.isValidPercent(txtCutOff))
            {
                setErrorMessage("Please input a valid fragment cutoff value");
                return false;
            }
        } else if (cmbCutOffType.getSelectionIndex() == -1) {
        	if (!TextFieldUtils.isEmpty(txtCutOff) && !TextFieldUtils.isValidPercent(txtCutOff))
            {
                setErrorMessage("Please input a valid fragment cutoff value");
                return false;
            }
        }
        if (!TextFieldUtils.isEmpty(txtCutOffPrecursor) && !TextFieldUtils.isNonZero(txtCutOffPrecursor))
        {
            setErrorMessage("Please input a valid precursor cutoff value");
            return false;
        }
        if (cmbCutOffTypePrecursor.getSelectionIndex() != -1 && cmbCutOffTypePrecursor.getItem(cmbCutOffTypePrecursor.getSelectionIndex()).equals(IMSFileReader.FILTER_PERCENTAGE))
        {
            if (!TextFieldUtils.isEmpty(txtCutOffPrecursor) && !TextFieldUtils.isValidPercent(txtCutOffPrecursor))
            {
                setErrorMessage("Please input a valid precursor cutoff value");
                return false;
            }
        }
        else if (cmbCutOffTypePrecursor.getSelectionIndex() == -1) {
        	if (!TextFieldUtils.isEmpty(txtCutOffPrecursor) && !TextFieldUtils.isValidPercent(txtCutOffPrecursor))
            {
                setErrorMessage("Please input a valid precursor cutoff value");
                return false;
            }
        }
        if (!databaseValid()) {
        	setErrorMessage("Please add at least one valid database");
        	return false;
        }

        setErrorMessage(null);
        readyToFinish = true;
        return true;

    }
    
    protected boolean databaseValid() {
    	if (databaseSettings == null || databaseSettings.getAnalyteSettings() == null || databaseSettings.getAnalyteSettings().isEmpty()) 
    		return false;
    	return true;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getCutOffType()
    {
        return cutOffType;
    }

    public void setCutOffType(String cutOffType)
    {
        this.cutOffType = cutOffType;
    }

    public double getCutOff()
    {
        return cutOff;
    }

    public void setCutOff(double cutOff)
    {
        this.cutOff = cutOff;
    }

    public String getCutOffTypePrecursor()
    {
        return cutOffTypePrecursor;
    }

    public void setCutOffTypePrecursor(String cutOffTypePrecursor)
    {
        this.cutOffTypePrecursor = cutOffTypePrecursor;
    }

    public double getCutOffPrecursor()
    {
        return cutOffPrecursor;
    }

    public void setCutOffPrecursor(double cutOffPrecursor)
    {
        this.cutOffPrecursor = cutOffPrecursor;
    }

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (validateInput())
        {
            canFlipToNextPage();
            getWizard().getContainer().updateButtons();
        }
        else
        {
            readyToFinish = false;
            canFlipToNextPage();
            getWizard().getContainer().updateButtons();
        }	
	}
}
