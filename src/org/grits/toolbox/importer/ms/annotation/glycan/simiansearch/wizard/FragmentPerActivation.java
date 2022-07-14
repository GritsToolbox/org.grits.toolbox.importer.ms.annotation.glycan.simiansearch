package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.utilShare.TextFieldUtils;

import org.grits.toolbox.ms.om.data.FragmentPerActivationMethod;

public class FragmentPerActivation extends FragmentPerOptionForm {
	private Text txtActivationName;
	private Text txtMaxClvg;
	private Text txtMaxCR;
	private Combo cmbActivationMethod;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public FragmentPerActivation(Shell parentShell, FragmentSettingsForm parentForm) {
		super(parentShell, parentForm);
	}
	
	@Override
	protected String getFormTitle() {
		return "Fragments Per Activation Method";
	}

	@Override
	protected void addPreFragmentControls(Composite container) {
		Label lblActivationMethod = new Label(container, SWT.NONE);
		lblActivationMethod.setText("Activation Method");

		cmbActivationMethod = new Combo(container, SWT.NONE);
		cmbActivationMethod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(cmbActivationMethod.getItem(cmbActivationMethod.getSelectionIndex()).equals("Other")){
					txtActivationName.setEnabled(true);
				}else{
					txtActivationName.setEnabled(false);
				}
				setDirty(true, true);
			}
		});
		cmbActivationMethod.setItems(new String[] {"CID", "HCD", "ETD", "ECD", "IRMPD", "Other"});
		cmbActivationMethod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbActivationMethod.select(0);

		Label lblName = new Label(container, SWT.NONE);
		lblName.setText("Name");

		txtActivationName = new Text(container, SWT.BORDER);
		txtActivationName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDirty(true, validatInput());
			}
		});
		txtActivationName.setEnabled(false);
		txtActivationName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblMaxClvg = new Label(container, SWT.NONE);
		lblMaxClvg.setText("Max Num of Cleavages");

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
		btnB.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Selection: " + btnB.getSelection());
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		btnAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				String activationSelectedName;
				if(cmbActivationMethod.getItem(cmbActivationMethod.getSelectionIndex()).equals("Other")){
					activationSelectedName = txtActivationName.getText().trim();
				}else{
					activationSelectedName = cmbActivationMethod.getItem(cmbActivationMethod.getSelectionIndex());
				}
				FragmentPerActivationMethod method = new FragmentPerActivationMethod();
				method.setActivationMethod(activationSelectedName);
				method.setFragments(getFragments());
				method.setMaxNumOfCleavages(Integer.parseInt(txtMaxClvg.getText()));
				method.setMaxNumOfCrossRingCleavages(Integer.parseInt(txtMaxCR.getText()));
				parentForm.addToGrid(method);
//				perActivation.put(activationSelectedName, method);
				//reset
//				txtMaxClvg.setText("2");
//				txtMaxCR.setText("0");
//				txtActivationName.setEnabled(false);
//				txtActivationName.setText("");
				setMessage("Added Successfully");
				setDirty(false, true);				
			}
		});
	}
	@Override
	public boolean validatInput(){
		boolean bVal = super.validatInput();
		if( ! bVal ) 
			return false;
		
		if( ! TextFieldUtils.isNonZero(txtMaxClvg)){
			setErrorMessage("Please enter a valid number");
			return false;
		}

		if(txtActivationName.getEnabled() && TextFieldUtils.isEmpty(txtActivationName)){
			setErrorMessage("Please enter a valid name");
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
			setErrorMessage("Please enter Max Cleavge value ");
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
