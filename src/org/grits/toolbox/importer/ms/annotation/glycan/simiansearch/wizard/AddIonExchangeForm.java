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

public class AddIonExchangeForm extends WizardPage {
	private Method method;
	private Text txtMaxCharge;
	private List<GridItem> adductGridItems = new ArrayList<GridItem>();
	private HashMap<String,Object> adducts = new HashMap<String,Object>();
	private Grid gridAdduct;
	private Button btnDeleteAdduct;
	private Button btnEditAdduct;
	private boolean canFlip = true;
	private boolean bTraversedTo = false;
	private List<Molecule> possibleIons = null;
	private ModifyListener txtMaxChargeModifyListener;
	private final static String ADD_DIALOG_TITLE = "Add/Edit Ion Exchange Dialog";
	
	/**
	 * Create the wizard.
	 */
	public AddIonExchangeForm(Method method) {
		super("wizardPage");
		setTitle("Ion Exchange Settings");
		setDescription("Add ion exchange settings");
		this.setMethod(method);		
	}


	public void updatePossibleIons() {
		this.possibleIons = buildAdductList();		
	}

	private List<Molecule> buildAdductList() {
		List<Molecule> possibleIons = new ArrayList<Molecule>();
		for( int i = 0; i < method.getIons().size(); i++ ) {
			if ( ! method.getIons().get(i).getLabel().equals(GlycanPreDefinedOptions.ION_ADDUCT_HYDROGEN.getLabel()) && 
					! method.getIons().get(i).getLabel().equals(GlycanPreDefinedOptions.ION_ADDUCT_NEGHYDROGEN.getLabel() ) ) {
				possibleIons.add(method.getIons().get(i));
			}
		}
		return possibleIons;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(2, false));

		Label lblMaxCharge = new Label(container, SWT.NONE);
		lblMaxCharge.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblMaxCharge.setText("Max Count*");

		txtMaxCharge = new Text(container, SWT.BORDER);
		txtMaxChargeModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(validateInput()) {
					((AddAdductsForm) getPreviousPage()).setPageComplete(true);
					setPageComplete(true);
				}
				else
					setPageComplete(false);
			}
		};
		txtMaxCharge.addModifyListener(txtMaxChargeModifyListener);
		txtMaxCharge.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblAdductSettings = new Label(container, SWT.NONE);
		lblAdductSettings.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblAdductSettings.setText("Ion Exchange Settings*");

		Button btnAddAdduct = new Button(container, SWT.NONE);
		btnAddAdduct.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ( possibleIons != null && ! possibleIons.isEmpty() ) {
					GridUtils.addButtonPressed(ADD_DIALOG_TITLE, gridAdduct, adductGridItems, 
							adducts, possibleIons, false, DialogType.ION_EXCHANGE, getShell());
					if(validateInput()) {
						((AddAdductsForm) getPreviousPage()).setPageComplete(true);
						setPageComplete(true);
					}
					else
						setPageComplete(false);
				} else {
					setPageComplete(true);
					setErrorMessage("No valid adducts appropriate exchange");			
				} 

			}
		});

		btnAddAdduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddAdduct.setText(" Add Ion Exchange");
		new Label(container, SWT.NONE);

		btnDeleteAdduct = new Button(container, SWT.NONE);
		btnDeleteAdduct.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridUtils.deleteButtonPressed(gridAdduct, adductGridItems, adducts);
				if(validateInput()) {
					((AddAdductsForm) getPreviousPage()).setPageComplete(true);
					setPageComplete(true);
				}
				else
					setPageComplete(false);
			}
		});
		btnDeleteAdduct.setEnabled(false);
		btnDeleteAdduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnDeleteAdduct.setText("Delete Ion Exchange");
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
		btnEditAdduct.setText("Edit Ion Exchange");
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
			setErrorMessage("You must select an ion exchange to edit.");
			return;
		}
		if(gridAdduct.getSelectionIndices().length>1){
			setErrorMessage("You can edit only one ion exchange at a time");
			return;
		}
		IonSettings adduct = new IonSettings();
		adduct = (IonSettings) adducts.get(gridAdduct.getItem(gridAdduct.getSelectionIndex()).getText(1).trim());
		//to find the item in the hashmap if the user changed the name
		String currentName = adduct.getName();
		if(adduct != null){
			if(gridAdduct.getSelectionIndices().length>1){
				setErrorMessage("You can edit only one adduct at a time");
				return;
			}
			if( possibleIons != null && ! possibleIons.isEmpty() ) {
				GridUtils.editButtonPressed(ADD_DIALOG_TITLE, gridAdduct, adductGridItems, adducts, 
						possibleIons, false, DialogType.ION_EXCHANGE, getShell());
				if(validateInput()) {
					((AddAdductsForm) getPreviousPage()).setPageComplete(true);
					setPageComplete(true);
				}
				else
					setPageComplete(false);
			}
		}
	}

	@Override
	public boolean canFlipToNextPage() {
		return canFlip;
	}

	public void setTraversedTo( boolean _bVal ) {
		bTraversedTo = _bVal;
	}

	public boolean validateInput() {
		if( gridAdduct.getSelectionCount() == 0 || (adducts.isEmpty() && btnDeleteAdduct != null) ) {
			btnDeleteAdduct.setEnabled(false);
			btnEditAdduct.setEnabled(false);			
		}
		if ( ! adducts.isEmpty() ) {
			// check possible ions to see if any in aducts list not in possible ions list!
			if(TextFieldUtils.isEmpty(txtMaxCharge) || ! TextFieldUtils.isInteger(txtMaxCharge)|| ! TextFieldUtils.isNonZero(txtMaxCharge)){
				setErrorMessage("Please enter a valid number for charge");
				canFlip = false;
				return false;
			}
			if(adducts.keySet().size() == 0){
				setErrorMessage("Please add a valid adduct");
				canFlip = false;
				return false;
			}

		} 
		save();
		canFlip = true;
		setErrorMessage(null);
		setTraversedTo(false);
		return true;
	}

	public boolean checkAdduct(){
		if(adductGridItems.size() == 0)
			return false;
		return true;
	}

	public void save(){
		if( ! isPageComplete() ) {
			return;
		}
		List<IonSettings> adductsFinalList = new ArrayList<IonSettings>();
		if( ! adducts.isEmpty() ){
			for( Object curAdduct : adducts.values() ) {
				boolean bNotFound = true;
				for( Molecule posAdduct : possibleIons ) {
					if( ((IonSettings) curAdduct).getLabel().equals(posAdduct.getLabel())) {
						bNotFound = false;
						break;
					}
				}
				if( bNotFound ) {
					setErrorMessage("Exchange ion: " + curAdduct + " not appropriate for current adduct list." );
					canFlip = false;
					setPageComplete(false);
					( (AddAdductsForm) getPreviousPage()).setErrorMessage("Error on Ion Exchange page. Please fix the next page.");
					return;
				}

			}
			for(Object adduct : adducts.values())
				adductsFinalList.add((IonSettings) adduct);
		} else if ( bTraversedTo ) {
			bTraversedTo = false;
		}
		if ( !txtMaxCharge.getText().isEmpty() )
			method.setMaxIonExchangeCount(Integer.parseInt(txtMaxCharge.getText()));
		method.setIonExchanges(adductsFinalList);
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
    		Integer ionCount = myWizard.getPreferences().getMethod().getMaxIonExchangeCount();
    		if (ionCount != null) txtMaxCharge.setText(ionCount + "");
    		List<IonSettings> ionSettings = myWizard.getPreferences().getMethod().getIonExchanges();
    		adducts.clear();
    		//gridAdduct.removeAll();
    		gridAdduct.disposeAllItems();
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
