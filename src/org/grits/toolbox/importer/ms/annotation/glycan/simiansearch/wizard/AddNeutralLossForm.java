package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.AddEditIonDialog.DialogType;
import org.grits.toolbox.ms.annotation.structure.GlycanPreDefinedOptions;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.ms.om.data.Molecule;
import org.grits.toolbox.ms.om.data.MoleculeSettings;

public class AddNeutralLossForm extends WizardPage {
	private Method method;
	private List<GridItem> adductGridItems = new ArrayList<GridItem>();
	private HashMap<String,Object> adducts = new HashMap<String,Object>();
	private Grid gridAdduct;
	private Button btnDeleteAdduct;
	private Button btnEditAdduct;
	private List<Molecule> allPossibleAdducts;
	private final static String ADD_DIALOG_TITLE = "Add/Edit Neutral Loss/Gain Dialog";

	/**
	 * Create the wizard.
	 */
	public AddNeutralLossForm(Method method) {
		super("wizardPage");
		setTitle("Neutral Loss/Gain Settings");
		setDescription("Add different neutral losses or gains.");
		this.setMethod(method);
	}

	private List<Molecule> getPossibleAdducts() {
		List<Molecule> adductList = new ArrayList<Molecule>();
		MoleculeSettings water = new MoleculeSettings(GlycanPreDefinedOptions.LOSS_H20);
		water.setMass(-1.0 * water.getMass());
		adductList.add(water);
		MoleculeSettings methyl = new MoleculeSettings(GlycanPreDefinedOptions.LOSS_METHYL);
		methyl.setMass(-1.0 * methyl.getMass());
		adductList.add(methyl);
		MoleculeSettings sialNeg = new MoleculeSettings(GlycanPreDefinedOptions.LOSS_SIAL_NEG);
		sialNeg.setMass(-1.0 * sialNeg.getMass());
		adductList.add(sialNeg);
		MoleculeSettings sialPos = new MoleculeSettings(GlycanPreDefinedOptions.LOSS_SIAL_POS);
		sialPos.setMass(-1.0 * sialPos.getMass());
		adductList.add(sialPos);
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

		Label lblAdductSettings = new Label(container, SWT.NONE);
		lblAdductSettings.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblAdductSettings.setText("Neutral Loss/Gain Settings*");

		Button btnAddAdduct = new Button(container, SWT.NONE);
		btnAddAdduct.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridUtils.addButtonPressed(ADD_DIALOG_TITLE, gridAdduct, adductGridItems, adducts, 
						allPossibleAdducts, true, DialogType.NEUTRAL_LOSSGAIN, getShell());
				if(validateInput())
					setPageComplete(true);
				else
					setPageComplete(false);

			}
		});
		btnAddAdduct.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddAdduct.setText(" Add Neutral Loss or Gain");
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
		btnDeleteAdduct.setText("Delete Neutral Loss or Gain");
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
		btnEditAdduct.setText("Edit Neutral Loss or Gain");
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
		gridColumn.setWidth(200);
		gridColumn.setText("Name");

		GridColumn gridColumn_2 = new GridColumn(gridAdduct, SWT.NONE);
		gridColumn_2.setWidth(100);
		gridColumn_2.setText("Label");

		GridColumn gridColumn_3 = new GridColumn(gridAdduct, SWT.NONE);
		gridColumn_3.setWidth(125);
		gridColumn_3.setText("Mass");

		GridColumn gridColumn_4 = new GridColumn(gridAdduct, SWT.NONE);
		gridColumn_4.setWidth(125);
		gridColumn_4.setText("Number");

		if(gridAdduct.getItemCount() == 0 && adducts.keySet().size() == 1){
			GridUtils.addToAdductGrid(gridAdduct, adductGridItems, adducts, (Molecule) adducts.get(GlycanPreDefinedOptions.LOSS_H20.getLabel()));
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

		setPageComplete(true);
	}
	
	public void handleEdit() {
		if( adductGridItems.isEmpty() || gridAdduct.getSelectionCount() == 0) {
			btnDeleteAdduct.setEnabled(false);
			btnEditAdduct.setEnabled(false);
		}
		if(gridAdduct.getSelectionIndices().length>1){
			setErrorMessage("You can edit only one adduct at a time");
			return;
		}
		GridUtils.editButtonPressed(ADD_DIALOG_TITLE, gridAdduct, adductGridItems, 
				adducts, allPossibleAdducts, true, DialogType.NEUTRAL_LOSSGAIN, getShell());
		if(validateInput())
			setPageComplete(true);
		else
			setPageComplete(false);
	}

	public boolean validateInput(){
		if( gridAdduct.getSelectionCount() == 0 || (adducts.isEmpty() && btnDeleteAdduct != null) ) {
			btnDeleteAdduct.setEnabled(false);
			btnEditAdduct.setEnabled(false);			
		}
		setErrorMessage(null);
		save();
		return true;
	}

	public boolean checkAdduct(){
		if(adductGridItems.size() == 0)
			return false;
		return true;
	}



	public void save(){
		List<MoleculeSettings> adductsFinalList = new ArrayList<MoleculeSettings>();
		if(adducts.keySet().size() != 0){
			for(Object adduct : adducts.values())
				adductsFinalList.add((MoleculeSettings) adduct);
		}
		method.setNeutralLoss(adductsFinalList);
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete() && getNextPage() != null;
	}	
	
	@Override
	public IWizardPage getNextPage() {
		return null;
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
    	MSGlycanAnnotationWizard myWizard = (MSGlycanAnnotationWizard) getWizard();
    	if (myWizard.getPreferences() != null && myWizard.getPreferences().getMethod() != null) {
    		//setMethod(myWizard.getPreferences().getMethod());
    		List<MoleculeSettings> moleculeSettings = myWizard.getPreferences().getMethod().getNeutralLoss();
    		adducts.clear();
    		//gridAdduct.removeAll();
    		gridAdduct.disposeAllItems();
    		for (MoleculeSettings adduct : moleculeSettings) {
    			if (GridUtils.getDuplicateAdduct(adducts, adduct) == null)
    				GridUtils.addToAdductGrid(gridAdduct, adductGridItems, adducts, adduct);
			}
    	}
    }
}
