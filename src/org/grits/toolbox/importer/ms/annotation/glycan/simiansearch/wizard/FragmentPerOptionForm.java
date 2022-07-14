package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.ms.om.data.Fragment;

public abstract class FragmentPerOptionForm extends TitleAreaDialog {
	protected Button btnB,btnY,btnC,btnZ,btnA,btnX;
	protected Button btnAdd;
	protected FragmentSettingsForm parentForm = null;
	private int addCnt = 0;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public FragmentPerOptionForm(Shell parentShell, FragmentSettingsForm parentForm) {
		super(parentShell);
		this.parentForm = parentForm;
	}

	protected void setDirty( boolean _bModified, boolean _bValid ) {
		if( _bModified && _bValid ) {
			this.addCnt++;
		}
//		btnAdd.setEnabled( _bModified && _bValid);
		btnAdd.setEnabled( _bValid);
		Button bOK = getButton(IDialogConstants.OK_ID);
		if( ! _bValid ) {
			bOK.setText(IDialogConstants.CANCEL_LABEL);
		} else if ( ! _bModified && _bValid && addCnt > 0 ){
			bOK.setText(IDialogConstants.FINISH_LABEL);						
		} else {
			bOK.setText(IDialogConstants.CLOSE_LABEL);	
		}
//		bOK.setEnabled(! _bModified);
	}

	protected abstract String getFormTitle();
	protected abstract void addPreFragmentControls(Composite container);
	protected abstract void addPostFragmentControls(Composite container);
	protected abstract void addButtonEventListeners();
	
	protected void addFragmentControls(Composite container) {
		Label label_2 = new Label(container, SWT.NONE);
		label_2.setText("Glyco Cleavages");
		
		btnB = new Button(container, SWT.CHECK);
		btnB.setSelection(true);
		btnB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true, validatInput());
			}
		});

		btnB.setText("B");
		new Label(container, SWT.NONE);

		btnY = new Button(container, SWT.CHECK);
		btnY.setSelection(true);
		btnY.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true, validatInput());
			}
		});

		btnY.setText("Y");
		new Label(container, SWT.NONE);

		btnC = new Button(container, SWT.CHECK);
		btnC.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true, validatInput());
			}
		});
		btnC.setText("C");
		new Label(container, SWT.NONE);

		btnZ = new Button(container, SWT.CHECK);
		btnZ.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true, validatInput());
			}
		});
		btnZ.setText("Z");

		Label label_3 = new Label(container, SWT.NONE);
		label_3.setText("Cross Ring Cleavages");

		btnA = new Button(container, SWT.CHECK);
		btnA.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true, validatInput());
			}
		});
		btnA.setText("A");
		new Label(container, SWT.NONE);

		btnX = new Button(container, SWT.CHECK);
		btnX.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setDirty(true, validatInput());
			}
		});
		btnX.setText("X");		
	}

	
	protected void addAddButton(Composite container) {
		btnAdd = new Button(container, SWT.PUSH);
		btnAdd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		btnAdd.setText("Add");
	}
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(getFormTitle());
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		addPreFragmentControls(container);
		addFragmentControls(container);
		addPostFragmentControls(container);
		addAddButton(container);
		addButtonEventListeners();
		
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		return area;
	}

	public List<Fragment> getFragments(){
		List<Fragment> fragments = new ArrayList<Fragment>();
		if(btnB.getSelection()){
			Fragment fragment = new Fragment();
			fragment.setNumber(Fragment.UNKNOWN);
			fragment.setType(Fragment.TYPE_B);
			fragments.add(fragment);
		}
		if(btnY.getSelection()){
			Fragment fragment = new Fragment();
			fragment.setNumber(Fragment.UNKNOWN);
			fragment.setType(Fragment.TYPE_Y);
			fragments.add(fragment);
		}
		if(btnC.getSelection()){
			Fragment fragment = new Fragment();
			fragment.setNumber(Fragment.UNKNOWN);
			fragment.setType(Fragment.TYPE_C);
			fragments.add(fragment);
		}
		if(btnZ.getSelection()){
			Fragment fragment = new Fragment();
			fragment.setNumber(Fragment.UNKNOWN);
			fragment.setType(Fragment.TYPE_Z);
			fragments.add(fragment);
		}
		if(btnA.getSelection()){
			Fragment fragment = new Fragment();
			fragment.setNumber(Fragment.UNKNOWN);
			fragment.setType(Fragment.TYPE_A);
			fragments.add(fragment);
		}
		if(btnX.getSelection()){
			Fragment fragment = new Fragment();
			fragment.setNumber(Fragment.UNKNOWN);
			fragment.setType(Fragment.TYPE_X);
			fragments.add(fragment);
		}

		return fragments;
	}

	public boolean validatInput(){
		if(!btnB.getSelection() && !btnY.getSelection() && !btnC.getSelection() && !btnZ.getSelection() && !btnA.getSelection() && !btnX.getSelection()){
			setErrorMessage("Please select at least one glyco cleavage type or cross ring type");
			return false;
		}

		setErrorMessage(null);
		return true;

	}
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL,	true);
//		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
//		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 500);
	}

}
