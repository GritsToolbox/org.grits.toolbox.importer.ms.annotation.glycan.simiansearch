package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.utilShare.TextFieldUtils;

import org.grits.toolbox.ms.om.data.FragmentPerMsLevel;

public class FragmentPerMsLevelForm extends FragmentPerOptionForm {
	private Text txtMaxClvg;
	private Text txtMaxCR;
	Combo cmbMsLevel;
	
	public FragmentPerMsLevelForm(Shell parentShell, FragmentSettingsForm parentForm) {
		super(parentShell, parentForm);
	}
	
	@Override
	protected String getFormTitle() {
		return "Fragment Settings Per MS Level";
	}

	@Override
	protected void addPreFragmentControls(Composite container) {
		Label lblMsLevel = new Label(container, SWT.NONE);
		lblMsLevel.setText("MS Level");
		
		cmbMsLevel = new Combo(container, SWT.NONE);
		cmbMsLevel.setItems(new String[] {"MS2", "MS3", "MS4", "MS5", "MS6"});
		cmbMsLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbMsLevel.select(0);
		cmbMsLevel.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				setDirty(true, validatInput());				
			}
		});
		Label label = new Label(container, SWT.NONE);
		label.setText("Max Num of Cleavages");
		
		txtMaxClvg = new Text(container, SWT.BORDER);
		txtMaxClvg.setText("2");
		txtMaxClvg.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true, validatInput());
			}
		});
		
		txtMaxClvg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label label_1 = new Label(container, SWT.NONE);
		label_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_1.setText("Max Num of CrossRing Cleavages");
		
		txtMaxCR = new Text(container, SWT.BORDER);
		txtMaxCR.setText("0");
		txtMaxCR.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true, validatInput());
			}
		});
		
		txtMaxCR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}
	
	@Override
	protected void addButtonEventListeners() {
		btnAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				FragmentPerMsLevel method = new FragmentPerMsLevel();
				
				method.setMsLevel(cmbMsLevel.getSelectionIndex()+2);
				method.setFragments(getFragments());
				method.setM_maxNumOfCleavages(Integer.parseInt(txtMaxClvg.getText()));
				method.setM_maxNumOfCrossRingCleavages(Integer.parseInt(txtMaxCR.getText()));
				parentForm.addToMsLevelGrid(method);
				setMessage("Added Successfully");
				setDirty(false, true);				
			}
		});
	}
	
	@Override
	public boolean validatInput(){
		boolean bVal = super.validatInput();
		if( ! bVal ) {
			return false;
		}
		if( ! TextFieldUtils.isNonZero(txtMaxClvg)){
			setErrorMessage("Please enter a valid number");
			return false;
		}
				
		if( TextFieldUtils.isNonZero(txtMaxCR) && !btnA.getSelection() && !btnX.getSelection()){
			setErrorMessage("Please select at least one cross ring cleavage type ");
			return false;
		}
		
		if( ! TextFieldUtils.isNonZero(txtMaxCR) && (btnA.getSelection() || btnX.getSelection())){
			setErrorMessage("Please enter Max cross ring value ");
			return false;
		}
		
		if( ! TextFieldUtils.isNonZero(txtMaxClvg) && (btnB.getSelection() || btnY.getSelection()|| btnC.getSelection()|| btnZ.getSelection())){
			setErrorMessage("Please enter Max Cleavage value ");
			return false;
		}
		
		setErrorMessage(null);
		return true;
	}

	@Override
	protected void addPostFragmentControls(Composite container) {
		// TODO Auto-generated method stub
	}
}
