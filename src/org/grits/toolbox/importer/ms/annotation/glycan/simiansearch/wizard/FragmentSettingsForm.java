package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.grits.toolbox.core.utilShare.TextFieldUtils;
import org.grits.toolbox.ms.om.data.AnalyteSettings;
import org.grits.toolbox.ms.om.data.Fragment;
import org.grits.toolbox.ms.om.data.FragmentPerActivationMethod;
import org.grits.toolbox.ms.om.data.FragmentPerMsLevel;
import org.grits.toolbox.ms.om.data.GlycanSettings;
import org.grits.toolbox.ms.om.data.Method;

public class FragmentSettingsForm extends WizardPage {
	private Text txtDefMaxNumClvg;
	private Text txtDefMaxNumCR;
	private Button btnB, btnY, btnC, btnZ, btnA, btnX;
	private Grid gridActivation, gridMs;
	GridColumn clmnActivationMethod, clmnFragSettings, clmnEnabled;
	private HashMap<String, FragmentPerActivationMethod> fpa = null;
	private HashMap<Integer, FragmentPerMsLevel> fpml = null;
	private List<GridItem> activationGridItems = new ArrayList<GridItem>();
	private List<GridItem> msGridItems = new ArrayList<GridItem>();
	private HashMap<String, GridItem> filter = new HashMap<String, GridItem>();
	private HashMap<Integer, GridItem> filterMsLevel = new HashMap<Integer, GridItem>();
	private boolean readyToFinish = false;
	private Method method;
	private Label lblNewLabel;
	private ModifyListener maxNumClvgModifyListener;
	private ModifyListener maxNumCRModifyListener;
	private SelectionAdapter btnSelectionAdapter;

	/**
	 * Create the wizard.
	 */
	public FragmentSettingsForm(Method method) {
		super("wizardPage");
		setTitle("Fragment Settings");
		setDescription("Choose the fragment settings from different options");
		this.method = method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	/**
	 * Create contents of the wizard.
	 *
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(2, false));

		Label lblDefaultSettings_1 = new Label(container, SWT.NONE);
		lblDefaultSettings_1.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblDefaultSettings_1.setText("Default Settings");
		new Label(container, SWT.NONE);

		Label lblMaxNumOf = new Label(container, SWT.NONE);
		lblMaxNumOf.setText("Max Num of Cleavages");

		txtDefMaxNumClvg = new Text(container, SWT.BORDER);
		txtDefMaxNumClvg.setText("2");
		maxNumClvgModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (validateInput()) {
					canFlipToNextPage();
					getWizard().getContainer().updateButtons();
				} else {
					readyToFinish = false;
					canFlipToNextPage();
					getWizard().getContainer().updateButtons();
				}
			}
		};
		txtDefMaxNumClvg.addModifyListener(maxNumClvgModifyListener);

		txtDefMaxNumClvg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblMaxNumOf_1 = new Label(container, SWT.NONE);
		lblMaxNumOf_1.setText("Max Num of CrossRing Cleavages");

		txtDefMaxNumCR = new Text(container, SWT.BORDER);
		txtDefMaxNumCR.setText("0");
		maxNumCRModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (validateInput()) {
					canFlipToNextPage();
					getWizard().getContainer().updateButtons();
				} else {
					readyToFinish = false;
					canFlipToNextPage();
					getWizard().getContainer().updateButtons();
				}
			}
		};
		txtDefMaxNumCR.addModifyListener(maxNumCRModifyListener);

		txtDefMaxNumCR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblGlycoCleavages = new Label(container, SWT.NONE);
		lblGlycoCleavages.setText("Glycosidic Cleavages");

		btnB = new Button(container, SWT.CHECK);
		btnB.setSelection(true);
		btnSelectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (validateInput()) {
					canFlipToNextPage();
					getWizard().getContainer().updateButtons();
				} else {
					readyToFinish = false;
					canFlipToNextPage();
					getWizard().getContainer().updateButtons();
				}
			}
		};
		btnB.addSelectionListener(btnSelectionAdapter);

		btnB.setText("B");
		new Label(container, SWT.NONE);

		btnY = new Button(container, SWT.CHECK);
		btnY.setSelection(true);
		btnY.addSelectionListener(btnSelectionAdapter);

		btnY.setText("Y");
		new Label(container, SWT.NONE);

		btnC = new Button(container, SWT.CHECK);
		btnC.addSelectionListener(btnSelectionAdapter);
		btnC.setText("C");
		new Label(container, SWT.NONE);

		btnZ = new Button(container, SWT.CHECK);
		btnZ.addSelectionListener(btnSelectionAdapter);
		btnZ.setText("Z");

		Label lblCrossRingCleavages = new Label(container, SWT.NONE);
		lblCrossRingCleavages.setText("Cross Ring Cleavages");

		btnA = new Button(container, SWT.CHECK);
		btnA.addSelectionListener(btnSelectionAdapter);
		btnA.setText("A");
		new Label(container, SWT.NONE);

		btnX = new Button(container, SWT.CHECK);
		btnX.addSelectionListener(btnSelectionAdapter);
		btnX.setText("X");

		Button btnFragmentsPerActivation = new Button(container, SWT.NONE);
		btnFragmentsPerActivation.addSelectionListener(new FragmentSelectionAdapter(this) {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FragmentPerOptionForm dialog = new FragmentPerActivation(getShell(), getParentForm());
				int result = dialog.open();
				if (result == 0) { // nothing to do on close. updates are "live"
					// fpa = dialog.getPerActivation();
					// addToGrid(fpa);
				}
			}
		});
		btnFragmentsPerActivation.setText("Fragments Per Activation Method");

		gridActivation = new Grid(container, SWT.BORDER);
		gridActivation.setHeaderVisible(true);
		gridActivation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		clmnActivationMethod = new GridColumn(gridActivation, SWT.NONE);
		clmnActivationMethod.setText("Activation Method");
		clmnActivationMethod.setWidth(150);

		clmnFragSettings = new GridColumn(gridActivation, SWT.NONE);
		clmnFragSettings.setText("Fragment Settings");
		clmnFragSettings.setWidth(150);

		clmnEnabled = new GridColumn(gridActivation, SWT.CHECK);
		clmnEnabled.setText("Enabled");
		clmnEnabled.setWidth(150);

		Button btnFragmentsPerMs = new Button(container, SWT.NONE);
		btnFragmentsPerMs.addSelectionListener(new FragmentSelectionAdapter(this) {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FragmentPerOptionForm dialog = new FragmentPerMsLevelForm(getShell(), getParentForm());
				int result = dialog.open();
				if (result == 0) { // nothing to do on close. updates are "live"
					// fpml = dialog.getPerMsLevel();
					// addToMsLevelGrid(fpml);
				}

			}
		});
		btnFragmentsPerMs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnFragmentsPerMs.setText("Fragments Per Ms Level");

		gridMs = new Grid(container, SWT.BORDER);
		gridMs.setHeaderVisible(true);
		gridMs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		GridColumn clmnMsLevel = new GridColumn(gridMs, SWT.NONE);
		clmnMsLevel.setText("MS Level");
		clmnMsLevel.setWidth(150);

		GridColumn clmnMsFragSettings = new GridColumn(gridMs, SWT.NONE);
		clmnMsFragSettings.setText("Fragment Settings");
		clmnMsFragSettings.setWidth(150);

		GridColumn clmnMsEnabled = new GridColumn(gridMs, SWT.CHECK);
		clmnMsEnabled.setText("Enabled");
		clmnMsEnabled.setWidth(150);

		setControl(container);

		lblNewLabel = new Label(container, SWT.WRAP);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_lblNewLabel.widthHint = 567;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText(
				"*If more than one fragment settings given, the per-activation settings will overwrite the others if it is given, otherwise the per-MS level settings will be used if it is given, in the other cases the default settings will be used");
		
		fpa = new HashMap<>();
		fpml = new HashMap<>();
		
		updateControlsFromPreferences();
		
		setPageComplete(false);
		if (validateInput()) {
			canFlipToNextPage();
			// getWizard().getContainer().updateButtons();
		} else {
			readyToFinish = false;
			canFlipToNextPage();
			// getWizard().getContainer().updateButtons();
		}
	}

	@Override
	public boolean canFlipToNextPage() {
		if (readyToFinish) {
			// return ! wizard.getGlycanSettingsForm().useMetaInfoControls();
			return true;
		} else
			return false;
	}

	public boolean canFinish() {
		return readyToFinish;
	}

	// adds new or overwrites existing
	public boolean addToGrid(FragmentPerActivationMethod method) {
		if (filter.get(method.getActivationMethod()) == null) {
			GridItem item = new GridItem(gridActivation, SWT.NONE);
			item.setText(0, method.getActivationMethod());
			StringBuilder builder = new StringBuilder();
			builder.append("max clvg: " + method.getMaxNumOfCleavages() + ",");
			builder.append("max CR: " + method.getMaxNumOfCrossRingCleavages() + ",");
			builder.append("fragment types: ");
			for (Fragment f : method.getFragments()) {
				builder.append(f.getType() + " ");
			}
			item.setText(1, builder.toString());
			item.setChecked(2, true);
			activationGridItems.add(item);
			filter.put(method.getActivationMethod(), item);
		} else {
			int index = 0;
			for (int i = 0; i < gridActivation.getItems().length; i++)
				if (gridActivation.getItem(i).getText(0).trim().equals(method.getActivationMethod()))
					index = i;
			gridActivation.remove(index);
			GridItem item = new GridItem(gridActivation, SWT.NONE);
			item.setText(0, method.getActivationMethod());
			StringBuilder builder = new StringBuilder();
			builder.append("max clvg: " + method.getMaxNumOfCleavages() + ",");
			builder.append("max CR: " + method.getMaxNumOfCrossRingCleavages() + ",");
			builder.append("fragment types: ");
			for (Fragment f : method.getFragments()) {
				builder.append(f.getType() + " ");
			}
			item.setText(1, builder.toString());
			item.setChecked(2, true);
			activationGridItems.add(item);
			filter.put(method.getActivationMethod(), item);

		}
		fpa.put(method.getActivationMethod(), method);
		return true;
	}

	public boolean addToMsLevelGrid(FragmentPerMsLevel method) {
		if (filterMsLevel.get(method.getMsLevel()) == null) {
			GridItem item = new GridItem(gridMs, SWT.NONE);
			item.setText(0, "" + method.getMsLevel());
			StringBuilder builder = new StringBuilder();
			builder.append("max clvg: " + method.getM_maxNumOfCleavages() + ",");
			builder.append("max CR: " + method.getM_maxNumOfCrossRingCleavages() + ",");
			builder.append("fragment types: ");
			for (Fragment f : method.getFragments()) {
				builder.append(f.getType() + " ");
			}
			item.setText(1, builder.toString());
			item.setChecked(2, true);
			msGridItems.add(item);
			filterMsLevel.put(method.getMsLevel(), item);
		} else {
			int index = 0;
			for (int i = 0; i < gridMs.getItems().length; i++)
				if (gridMs.getItem(i).getText(0).trim().equals("" + method.getMsLevel()))
					index = i;
			gridMs.remove(index);
			GridItem item = new GridItem(gridMs, SWT.NONE);
			item.setText(0, "" + method.getMsLevel());
			StringBuilder builder = new StringBuilder();
			builder.append("max clvg: " + method.getM_maxNumOfCleavages() + ",");
			builder.append("max CR: " + method.getM_maxNumOfCrossRingCleavages() + ",");
			builder.append("fragment types: ");
			for (Fragment f : method.getFragments()) {
				builder.append(f.getType() + " ");
			}
			item.setText(1, builder.toString());
			item.setChecked(2, true);
			activationGridItems.add(item);
			filterMsLevel.put(method.getMsLevel(), item);
		}
		fpml.put(method.getMsLevel(), method);
		return true;
	}

	// @Override
	// public IWizardPage getNextPage() {
	// save();
	// return super.getNextPage();
	// }

	public void save() {
		// Default Settings
		
		List<Fragment> fragments = new ArrayList<>();
		Fragment f = null;
		if (btnB.getSelection()) {
			f = new Fragment();
			f.setNumber(Fragment.UNKNOWN);
			f.setType(Fragment.TYPE_B);
			fragments.add(f);
		}
		if (btnY.getSelection()) {
			f = new Fragment();
			f.setNumber(Fragment.UNKNOWN);
			f.setType(Fragment.TYPE_Y);
			fragments.add(f);
		}
		if (btnC.getSelection()) {
			f = new Fragment();
			f.setNumber(Fragment.UNKNOWN);
			f.setType(Fragment.TYPE_C);
			fragments.add(f);
		}
		if (btnZ.getSelection()) {
			f = new Fragment();
			f.setNumber(Fragment.UNKNOWN);
			f.setType(Fragment.TYPE_Z);
			fragments.add(f);
		}
		if (btnA.getSelection()) {
			f = new Fragment();
			f.setNumber(Fragment.UNKNOWN);
			f.setType(Fragment.TYPE_A);
			fragments.add(f);
		}
		if (btnX.getSelection()) {
			f = new Fragment();
			f.setNumber(Fragment.UNKNOWN);
			f.setType(Fragment.TYPE_X);
			fragments.add(f);
		}
		
		// set for all analyte settings
		for (AnalyteSettings analyte: method.getAnalyteSettings()) {
			if (analyte.getGlycanSettings() != null) {
				analyte.getGlycanSettings().setMaxNumOfCleavages(Integer.parseInt(txtDefMaxNumClvg.getText()));
				analyte.getGlycanSettings().setMaxNumOfCrossRingCleavages(Integer.parseInt(txtDefMaxNumCR.getText()));
				analyte.getGlycanSettings().setGlycanFragments(fragments);
				
				analyte.getGlycanSettings().getPerActivation().clear();
				if (fpa != null && !fpa.isEmpty()) {
					for (GridItem item : activationGridItems) {
						if (!item.isDisposed() && item.getChecked(2)) {
							FragmentPerActivationMethod activationMethodFragments = fpa.get(item.getText(0).trim());
							analyte.getGlycanSettings().getPerActivation().add(activationMethodFragments);
						} // if item

					} // for GridItem
				} // if fpa
				
				analyte.getGlycanSettings().getPerMsLevel().clear();
				if (fpml != null && !fpml.isEmpty()) {
					for (GridItem item : msGridItems) {
						if (!item.isDisposed() && item.getChecked(2)) {
							FragmentPerMsLevel msLevelFragments = fpml.get(Integer.parseInt(item.getText(0).trim()));
							analyte.getGlycanSettings().getPerMsLevel().add(msLevelFragments);
						} // if item

					} // for GridItem
				} // if fpa
				
			}
		}
	}

	public boolean validateInput() {
		if (!TextFieldUtils.isNonZero(txtDefMaxNumClvg)) {
			setErrorMessage("Please enter a valid number");
			return false;
		}

		if (!btnB.getSelection() && !btnY.getSelection() && !btnC.getSelection() && !btnZ.getSelection()
				&& !btnA.getSelection() && !btnX.getSelection()) {
			setErrorMessage("Please select at least one glyco cleavage type or cross ring type");
			return false;
		}

		if (TextFieldUtils.isNonZero(txtDefMaxNumCR) && !btnA.getSelection() && !btnX.getSelection()) {
			setErrorMessage("Please select at least one cross ring cleavage type ");
			return false;
		}

		if (!TextFieldUtils.isNonZero(txtDefMaxNumCR) && (btnA.getSelection() || btnX.getSelection())) {
			setErrorMessage("Please enter Max cross ring value ");
			return false;
		}

		if (!TextFieldUtils.isNonZero(txtDefMaxNumClvg)
				&& (btnB.getSelection() || btnY.getSelection() || btnC.getSelection() || btnZ.getSelection())) {
			setErrorMessage("Please enter Max Cleavge value ");
			return false;
		}

		setErrorMessage(null);
		readyToFinish = true;
		return true;

	}

	class FragmentSelectionAdapter extends SelectionAdapter {
		FragmentSettingsForm parentForm = null;

		public FragmentSelectionAdapter(FragmentSettingsForm parentForm) {
			this.parentForm = parentForm;
		}

		public FragmentSettingsForm getParentForm() {
			return parentForm;
		}
	}
	
	/**
     * This method updates the values of all controls with the current values from the preferences
     * used in editing
     */
    public void updateControlsFromPreferences() {
    	if (!(getWizard() instanceof MSGlycanAnnotationWizard))
    		return;
    	MSGlycanAnnotationWizard myWizard = (MSGlycanAnnotationWizard) getWizard();
    	if (myWizard.getPreferences() != null && myWizard.getPreferences().getMethod() != null) {
    		//setMethod(myWizard.getPreferences().getMethod());
    		removeListeners();  // required to prevent overriding the settings with the default ones
    		if (myWizard.getPreferences().getMethod().getAnalyteSettings() != null) {
    			for (AnalyteSettings analyteSetting: myWizard.getPreferences().getMethod().getAnalyteSettings()) {
    				GlycanSettings glycanSettings = analyteSetting.getGlycanSettings();
    				if (glycanSettings != null) {
    					txtDefMaxNumClvg.setText(glycanSettings.getMaxNumOfCleavages() + "");
    					txtDefMaxNumCR.setText(glycanSettings.getMaxNumOfCrossRingCleavages() + "");
    					List<Fragment> fragments = glycanSettings.getGlycanFragments();
    					for (Fragment fragment : fragments) {
							switch (fragment.getType()) {
							case Fragment.TYPE_A:
								btnA.setSelection(true);
								break;
							case Fragment.TYPE_B:
								btnB.setSelection(true);
								break;
							case Fragment.TYPE_C:
								btnC.setSelection(true);
								break;
							case Fragment.TYPE_X:
								btnX.setSelection(true);
								break;
							case Fragment.TYPE_Y:
								btnY.setSelection(true);
								break;
							case Fragment.TYPE_Z:
								btnZ.setSelection(true);
								break;
							}
						}
    					List<FragmentPerActivationMethod> perActivationList = glycanSettings.getPerActivation();
    					for (FragmentPerActivationMethod fpa : perActivationList) {
							addToGrid(fpa);
						}
    					List<FragmentPerMsLevel> perMSLevelList = glycanSettings.getPerMsLevel();
    					for (FragmentPerMsLevel fragmentPerMsLevel : perMSLevelList) {
							addToMsLevelGrid(fragmentPerMsLevel);
						}
    				}
    				break; // all of them are supposed to be the same, do it for the first one only
    			}
    		}
    		addListeners();  // put listeners back to allow further editing
    	}
    }

	private void addListeners() {
		txtDefMaxNumClvg.addModifyListener(maxNumClvgModifyListener);
		txtDefMaxNumCR.addModifyListener(maxNumCRModifyListener);
		btnA.addSelectionListener(btnSelectionAdapter);
		btnB.addSelectionListener(btnSelectionAdapter);
		btnC.addSelectionListener(btnSelectionAdapter);
		btnX.addSelectionListener(btnSelectionAdapter);
		btnY.addSelectionListener(btnSelectionAdapter);
		btnZ.addSelectionListener(btnSelectionAdapter);
		
	}

	private void removeListeners() {
		txtDefMaxNumClvg.removeModifyListener(maxNumClvgModifyListener);
		txtDefMaxNumCR.removeModifyListener(maxNumCRModifyListener);
		btnA.removeSelectionListener(btnSelectionAdapter);
		btnB.removeSelectionListener(btnSelectionAdapter);
		btnC.removeSelectionListener(btnSelectionAdapter);
		btnX.removeSelectionListener(btnSelectionAdapter);
		btnY.removeSelectionListener(btnSelectionAdapter);
		btnZ.removeSelectionListener(btnSelectionAdapter);
	}
}
