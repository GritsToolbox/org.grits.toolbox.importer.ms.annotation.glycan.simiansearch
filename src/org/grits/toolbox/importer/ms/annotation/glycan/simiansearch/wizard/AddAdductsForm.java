package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseListener;
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
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.AddEditIonDialog.DialogType;
import org.grits.toolbox.ms.annotation.structure.GlycanPreDefinedOptions;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.Molecule;

public class AddAdductsForm extends WizardPage {
	private Method method;
	private Text txtMaxCharge;
	private List<GridItem> adductGridItems = new ArrayList<GridItem>();
	private Map<String,Object> adducts = new HashMap<String,Object>();
	private Grid gridAdduct;
	private Button btnDeleteAdduct;
	private Button btnEditAdduct;
	private boolean canFlip = false;
	private List<Molecule> allPossibleAdducts;
	private ModifyListener txtMaxChargeModifyListener;
	private final static String ADD_DIALOG_TITLE = "Add/Edit Adduct Dialog";
	
	/**
	 * Create the wizard.
	 */
	public AddAdductsForm(Method method) {
		super("wizardPage");
		setTitle("Ion Settings");
		setDescription("Add different ion settings");
		this.setMethod(method);
		if (method.getIons() == null || method.getIons().isEmpty()) {
			//create a default adduct
			IonSettings defaultAdduct = new IonSettings(GlycanPreDefinedOptions.ION_ADDUCT_SODIUM);
			defaultAdduct.getCounts().add(4);
			adducts.put(GlycanPreDefinedOptions.ION_ADDUCT_SODIUM.getLabel(), defaultAdduct);
		}
	}

	private List<Molecule> getPossibleAdducts() {
		List<Molecule> adductList = new ArrayList<Molecule>();
		adductList.add(new IonSettings(GlycanPreDefinedOptions.ION_ADDUCT_SODIUM));
		adductList.add(new IonSettings(GlycanPreDefinedOptions.ION_ADDUCT_HYDROGEN));
		adductList.add(new IonSettings(GlycanPreDefinedOptions.ION_ADDUCT_CHLORINE));
		adductList.add(new IonSettings(GlycanPreDefinedOptions.ION_ADDUCT_LITHIUM));
		adductList.add(new IonSettings(GlycanPreDefinedOptions.ION_ADDUCT_POTASSIUM));
		adductList.add(new IonSettings(GlycanPreDefinedOptions.ION_ADDUCT_NEGHYDROGEN));
		adductList.add(new IonSettings(GlycanPreDefinedOptions.ION_ADDUCT_CALCIUM));
		return adductList;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		allPossibleAdducts = getPossibleAdducts();
		setControl(container);
		container.setLayout(new GridLayout(2, false));

		Label lblMaxCharge = new Label(container, SWT.NONE);
		lblMaxCharge.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblMaxCharge.setText("Max Charge*");

		txtMaxCharge = new Text(container, SWT.BORDER);
		txtMaxCharge.setText("4");
		txtMaxChargeModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(validateInput())
					setPageComplete(true);
				else
					setPageComplete(false);
			}
		};
		txtMaxCharge.addModifyListener(txtMaxChargeModifyListener);
		txtMaxCharge.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblAdductSettings = new Label(container, SWT.NONE);
		lblAdductSettings.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblAdductSettings.setText("Adduct Settings*");
	
		Button btnAddAdduct = new Button(container, SWT.NONE);
		btnAddAdduct.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridUtils.addButtonPressed(ADD_DIALOG_TITLE, gridAdduct, adductGridItems, adducts, allPossibleAdducts, 
						true, DialogType.ION_SETTINGS, getShell());
				if(validateInput())
					setPageComplete(true);
				else
					setPageComplete(false);

			}
		});
		btnAddAdduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddAdduct.setText(" Add Adduct");
		new Label(container, SWT.NONE);

		btnDeleteAdduct = new Button(container, SWT.NONE);
		btnDeleteAdduct.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridUtils.deleteAdductsFromGrid(gridAdduct, adductGridItems, adducts);
				if(validateInput())
					setPageComplete(true);
				else
					setPageComplete(false);
			}
		});
		btnDeleteAdduct.setEnabled(false);
		btnDeleteAdduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnDeleteAdduct.setText("Delete Adduct");
		new Label(container, SWT.NONE);

		btnEditAdduct = new Button(container, SWT.NONE);
		btnEditAdduct.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEdit();
			}
		});
		btnEditAdduct.setEnabled(false);
		btnEditAdduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnEditAdduct.setText("Edit Adduct");
		new Label(container, SWT.NONE);

		gridAdduct = new Grid(container, SWT.BORDER);
		gridAdduct.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(gridAdduct.getSelection().length != 0){
					btnDeleteAdduct.setEnabled(true);
					btnEditAdduct.setEnabled(true);
				}

			}
		});
		gridAdduct.setHeaderVisible(true);
		GridData gd_gridAdduct = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_gridAdduct.heightHint = 20;
		gridAdduct.setLayoutData(gd_gridAdduct);

		GridColumn gridColumn = new GridColumn(gridAdduct, SWT.NONE);
		gridColumn.setWidth(150);
		gridColumn.setText("Name");

		GridColumn gridColumn_0 = new GridColumn(gridAdduct, SWT.NONE);
		gridColumn_0.setWidth(50);
		gridColumn_0.setText("Label");

		GridColumn gridColumn_1 = new GridColumn(gridAdduct, SWT.NONE);
		gridColumn_1.setWidth(100);
		gridColumn_1.setText("Polarity");

		GridColumn gridColumn_2 = new GridColumn(gridAdduct, SWT.NONE);
		gridColumn_2.setWidth(50);
		gridColumn_2.setText("Charge");

		GridColumn gridColumn_3 = new GridColumn(gridAdduct, SWT.NONE);
		gridColumn_3.setWidth(100);
		gridColumn_3.setText("Mass");

		GridColumn gridColumn_4 = new GridColumn(gridAdduct, SWT.NONE);
		gridColumn_4.setWidth(100);
		gridColumn_4.setText("Number");

		if(gridAdduct.getItemCount() == 0 && adducts.keySet().size() == 1){
			GridUtils.addToAdductGrid(gridAdduct, adductGridItems, adducts, (Molecule) adducts.get(GlycanPreDefinedOptions.ION_ADDUCT_SODIUM.getLabel()));
		}
		gridAdduct.addMouseListener( new MouseListener() {
			
			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
				handleEdit();
			}
		});
		setControl(container);
		
		updateControlsFromPreferences();

		if(validateInput())
			setPageComplete(true);
		else
			setPageComplete(false);
	}

	public void handleEdit() {
		if(gridAdduct.getSelectionIndices().length == 0){
			setErrorMessage("You must select an adduct to edit.");
			return;
		}
		if(gridAdduct.getSelectionIndices().length>1){
			setErrorMessage("You can edit only one adduct at a time");
			return;
		}
		GridUtils.editButtonPressed(ADD_DIALOG_TITLE, gridAdduct, adductGridItems, adducts, 
				allPossibleAdducts, true, DialogType.ION_SETTINGS, getShell());
		if(validateInput())
			setPageComplete(true);
		else
			setPageComplete(false);
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return canFlip;
	}

	public boolean validateInput(){
		if( adductGridItems.isEmpty() || gridAdduct.getSelectionCount() == 0) {
			btnDeleteAdduct.setEnabled(false);
			btnEditAdduct.setEnabled(false);
		}

		if(TextFieldUtils.isEmpty(txtMaxCharge) || ! TextFieldUtils.isInteger(txtMaxCharge) || ! TextFieldUtils.isNonZero(txtMaxCharge)){
			setErrorMessage("Please enter a valid number for charge");
			canFlip = false;
			return false;
		}
		if(adducts.keySet().size() == 0){
			setErrorMessage("Please add a valid adduct");
			canFlip = false;
			return false;
		}
		setErrorMessage(null);
		save();
		canFlip = true;
		return true;
	}

	public boolean checkAdduct(){
		if(adductGridItems.size() == 0)
			return false;
		return true;
	}

	public void save() {
		if( ! isPageComplete() ) { 
			return;
		}
		method.setMaxIonCount(Integer.parseInt(txtMaxCharge.getText()));
		List<IonSettings> adductsFinalList = new ArrayList<IonSettings>();
		for(Object adduct : adducts.values())
			adductsFinalList.add((IonSettings) adduct);
		method.setIons(adductsFinalList);
		IWizardPage nextPage = getNextPage();
		if (nextPage != null && nextPage instanceof AddIonExchangeForm) {
			( (AddIonExchangeForm) nextPage).updatePossibleIons();
			( (AddIonExchangeForm) nextPage).setTraversedTo(true);
			if( ! ( (AddIonExchangeForm) nextPage).canFlipToNextPage() ) {
				setErrorMessage("Error on Ion Exchange page. Please fix the next page.");
				setPageComplete(false);
				return;
			}
		}
		setErrorMessage(null);
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	/**
     * This method updates the values of all controls with the current values from the preferences
     * used in editing
     */
    public void updateControlsFromPreferences() {
    	if (!(getWizard() instanceof MSGlycanAnnotationWizard))
    		return;
    	removeListeners();
    	MSGlycanAnnotationWizard myWizard = (MSGlycanAnnotationWizard) getWizard();
    	if (myWizard.getPreferences() != null && myWizard.getPreferences().getMethod() != null) {
    		//setMethod(myWizard.getPreferences().getMethod());
    		Integer ionCount = myWizard.getPreferences().getMethod().getMaxIonCount();
    		txtMaxCharge.setText(ionCount + "");
    		adducts.clear();
    		gridAdduct.disposeAllItems();
    		//gridAdduct.removeAll();
    		List<IonSettings> ionSettings = myWizard.getPreferences().getMethod().getIons();
    		for (IonSettings adduct : ionSettings) {
    			if (GridUtils.getDuplicateAdduct(adducts, adduct) == null)
    				GridUtils.addToAdductGrid(gridAdduct, adductGridItems, adducts, adduct);
			}
    	}
    	addListeners();
    }
    
    private void addListeners() {
    	txtMaxCharge.addModifyListener(txtMaxChargeModifyListener);
	}

	private void removeListeners() {
		txtMaxCharge.removeModifyListener(txtMaxChargeModifyListener);
	}

}
