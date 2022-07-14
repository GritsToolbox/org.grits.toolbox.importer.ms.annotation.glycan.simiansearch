package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.utilShare.TextFieldUtils;
import org.grits.toolbox.ms.annotation.structure.GlycanPreDefinedOptions;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.Molecule;
import org.grits.toolbox.ms.om.data.MoleculeSettings;

public class AddEditIonDialog extends TitleAreaDialog {
	private Text txtPreAdductNum;
	private Text txtAdductName;
	private Text txtAdductLabel;
	private Text txtAdductCharge;
	private Text txtAdductMass;
	private Combo cmbAdducts = null;
	private Combo cmbPositive = null;
	private Button button = null;
	private String sTitle = null;
	
	private String mode = null;
	private Molecule adduct = null;
	private List<Molecule> possibleIons = null;
	private boolean bAddOther = false;

	private DialogType dialogType = DialogType.ION_SETTINGS;

	private final static int OTHER_ADDUCT_NAME_MAX_LENGTH = 50;
	private final static int OTHER_ADDUCT_LABEL_MAX_LENGTH = 15;
	
	public enum DialogType { ION_SETTINGS, ION_EXCHANGE, NEUTRAL_LOSSGAIN }
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public AddEditIonDialog(Shell parentShell, String sTitle, List<Molecule> possibleIons, 
			boolean _bAddOther, DialogType dialogType ) {
		super(parentShell);
		this.sTitle = sTitle;
		adduct = new IonSettings();
		this.possibleIons = possibleIons;
		this.bAddOther = _bAddOther;		
		this.dialogType = dialogType;
	}

	/**
	 * @wbp.parser.constructor
	 */
	public AddEditIonDialog(Shell parentShell, String sTitle, Molecule adduct, List<Molecule> possibleIons, 
			boolean _bAddOther, DialogType dialogType ) {
		super(parentShell);
		this.sTitle = sTitle;
		this.adduct = adduct;
		mode = "edit";
		this.possibleIons = possibleIons;
		this.bAddOther = _bAddOther;
		this.dialogType = dialogType;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(sTitle);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblPredefineAdducts = new Label(container, SWT.NONE);
		lblPredefineAdducts.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		if( dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
			lblPredefineAdducts.setText("Adducts");
		} else {
			lblPredefineAdducts.setText("Modifications");
		}

		cmbAdducts = new Combo(container, SWT.NONE);
		int iAdder =  this.bAddOther ? 1 : 0;
		String[] sLabels = new String[possibleIons.size() + iAdder];
		for( int i = 0; i < possibleIons.size(); i++ ) {
			sLabels[i] = possibleIons.get(i).getLabel();
		}
		if( this.bAddOther ) {
			sLabels[sLabels.length-1] = GlycanPreDefinedOptions.OTHER;
		}
		cmbAdducts.setItems(sLabels);
		cmbAdducts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNumber = new Label(container, SWT.NONE);
		lblNumber.setText("Number");

		txtPreAdductNum = new Text(container, SWT.BORDER);

		txtPreAdductNum.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		Label lblName = new Label(container, SWT.NONE);
		lblName.setText("Name");

		txtAdductName = new Text(container, SWT.BORDER);
		txtAdductName.setEnabled(false);
		txtAdductName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblLabel = new Label(container, SWT.NONE);
		lblLabel.setText("Label");
		txtAdductLabel = new Text(container, SWT.BORDER);
		txtAdductLabel.setEnabled(false);
		txtAdductLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		if( this.dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
			Label lblPositive = new Label(container, SWT.NONE);
			lblPositive.setText("Polarity");

			cmbPositive = new Combo(container, SWT.NONE);
			cmbPositive.setEnabled(false);
			cmbPositive.setItems(new String[] {"Positive", "Negative"});
			cmbPositive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			cmbPositive.select(0);

			Label lblCharge = new Label(container, SWT.NONE);
			lblCharge.setText("Charge");

			txtAdductCharge = new Text(container, SWT.BORDER);

			txtAdductCharge.setEnabled(false);
			txtAdductCharge.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		Label lblMass = new Label(container, SWT.NONE);
		if( dialogType == DialogType.NEUTRAL_LOSSGAIN ) {
			lblMass.setText("Mass*");
		} else { 
			lblMass.setText("Mass");
		}

		txtAdductMass = new Text(container, SWT.BORDER);

		txtAdductMass.setEnabled(false);
		txtAdductMass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if( dialogType == DialogType.NEUTRAL_LOSSGAIN ) {
			Label lblMassHint = new Label(container, SWT.NONE);
			FontData fontData = label.getFont().getFontData()[0];
			Font font = new Font(getShell().getDisplay(), new FontData(fontData.getName(), (int) (fontData.getHeight() * 0.9), SWT.ITALIC));
			lblMassHint.setFont(font);			
			lblMassHint.setText("* For neutral loss, use negative value for mass. For gain, use positive value.");
			lblMassHint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}

		//if edit selected
		if(mode != null){
			int iIndexOther = updateTextFields();
			if(iIndexOther >= 0){
				txtAdductMass.setEnabled(true);
				txtAdductName.setEnabled(true);
				txtAdductLabel.setEnabled(true);
				cmbAdducts.select(iIndexOther);
				if( this.dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
					cmbPositive.setEnabled(true);
					txtAdductCharge.setEnabled(true);
				}
			} 
		} else {
			cmbAdducts.select(0);
			updateAdduct();
			updateTextFields();		
			validateInput();
		}

		txtAdductMass.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(validateInput()){
					button.setEnabled(true);
				}else{
					button.setEnabled(false);
				}
			}

		});

		txtAdductName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(validateInput()){
					button.setEnabled(true);
				}else{
					button.setEnabled(false);
				}
			}
		});
		txtAdductLabel.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(validateInput()){
					button.setEnabled(true);
				}else{
					button.setEnabled(false);
				}
			}
		});
		txtPreAdductNum.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(validateInput()){
					button.setEnabled(true);
				}else{
					button.setEnabled(false);
				}
			}
		});

		if( dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
			txtAdductCharge.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if(validateInput()){
						button.setEnabled(true);
					}else{
						button.setEnabled(false);
					}
				}
			});			
		}
		cmbAdducts.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(cmbAdducts.getItem(cmbAdducts.getSelectionIndex()).equalsIgnoreCase(GlycanPreDefinedOptions.OTHER)){
					txtAdductName.setEnabled(true);
					if( ! adduct.getLabel().equals(GlycanPreDefinedOptions.OTHER) ) {
						txtAdductLabel.setText("");
					}
					txtAdductLabel.setEnabled(true);
					txtAdductMass.setEnabled(true);
					if( dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
						cmbPositive.setEnabled(true);
						txtAdductCharge.setEnabled(true);
					}

				}else{
					txtAdductName.setEnabled(false);
					txtAdductLabel.setEnabled(false);
					txtAdductMass.setEnabled(false);
					if( dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
						cmbPositive.setEnabled(false);
						txtAdductCharge.setEnabled(false);
					}
				}
				updateAdduct();
				updateTextFields();

				if(validateInput()){
					button.setEnabled(true);
				}else{
					button.setEnabled(false);
				}
			}
		});

		return area;
	}

	private int updateTextFields() {
		if( cmbAdducts.getItemCount() == 0 )
			return -1;
		int iIndex = 0;
		int iIndexOther = -1;
		for(String adductName : cmbAdducts.getItems()){
			if(adductName.equalsIgnoreCase(adduct.getLabel()) ){
				cmbAdducts.select(iIndex);
				break;
			}
			if( adductName.equals(GlycanPreDefinedOptions.OTHER) ) {
				iIndexOther = iIndex;
			}
			iIndex++;
		}
		if( adduct.getMass() != null )
			txtAdductMass.setText(adduct.getMass()+"");
		if( adduct.getName() != null )
			txtAdductName.setText(adduct.getName());
		if( adduct.getLabel() != null )
			txtAdductLabel.setText(adduct.getLabel());
		if( dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
			cmbPositive.select( ((IonSettings) adduct).getPolarity() ? 0 : 1 );
			if( ((IonSettings) adduct).getCharge() != null )
				txtAdductCharge.setText(((IonSettings) adduct).getCharge()+"");
		}
		if( dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
			if(((IonSettings) adduct).getCounts().size() > 0)
				txtPreAdductNum.setText( ((IonSettings) adduct).getCounts().get(0) + "");
		} else {
			if( ((MoleculeSettings) adduct).getCount() != null )
				txtPreAdductNum.setText( ((MoleculeSettings) adduct).getCount() + "");
		}
		return iIndexOther;
	}

	private int getOtherIndex() {
		int iIndex = 0;
		int iIndexOther = -1;
		for(String adductName : cmbAdducts.getItems()){
			if( adductName.equals(GlycanPreDefinedOptions.OTHER) ) {
				iIndexOther = iIndex;
			}
			iIndex++;
		}
		return iIndexOther;
	}

	private void updateAdduct() {
		if( possibleIons == null || possibleIons.isEmpty() ) {
			return;
		}
		String curAdduct = cmbAdducts.getItem(cmbAdducts.getSelectionIndex());
		int iIndex = 0;
		this.adduct = null;
		for(Molecule possibleIon : this.possibleIons){
			if(possibleIon.getLabel().equalsIgnoreCase(curAdduct) ){
				cmbAdducts.select(iIndex);
				this.adduct = possibleIon;
				break;
			}
		}
		iIndex = 0;
		int iIndexOther = getOtherIndex();
		if(this.adduct == null) {
			if( dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
				adduct = new IonSettings();
			} else {
				adduct = new MoleculeSettings();
			}
			if( ! TextFieldUtils.isEmpty( txtAdductMass ) && TextFieldUtils.isDouble( txtAdductMass) )
				adduct.setMass(Double.parseDouble(txtAdductMass.getText()));
			if( ! TextFieldUtils.isEmpty( txtAdductName ) )
				adduct.setName(txtAdductName.getText().trim());
			if( ! TextFieldUtils.isEmpty( txtAdductLabel ) )
				adduct.setLabel(txtAdductLabel.getText().trim());
			if( dialogType != DialogType.NEUTRAL_LOSSGAIN) {
				if( ! TextFieldUtils.isEmpty( txtAdductCharge ) && TextFieldUtils.isInteger( txtAdductCharge) )
					((IonSettings) adduct).setCharge(Integer.parseInt(txtAdductCharge.getText()));
				((IonSettings) adduct).setPolarity(cmbPositive.getText().equals("Positive"));
			} 
			cmbAdducts.select(iIndexOther);
		}
		if( dialogType != DialogType.NEUTRAL_LOSSGAIN ) {
			if( ! TextFieldUtils.isEmpty( txtPreAdductNum ) && TextFieldUtils.isInteger( txtPreAdductNum) ) {
				((IonSettings) adduct).getCounts().clear();
				((IonSettings) adduct).getCounts().add(Integer.parseInt(txtPreAdductNum.getText()));
			}
		} else {
			if( ! TextFieldUtils.isEmpty( txtPreAdductNum ) && TextFieldUtils.isInteger( txtPreAdductNum) )
				((MoleculeSettings) adduct).setCount(Integer.parseInt(txtPreAdductNum.getText()));
		}

	}


	public boolean validateInput(){
		if( cmbAdducts.getItemCount() == 0 ) {
			return true;
		}
		int iSelInx = cmbAdducts.getSelectionIndex();
		if( iSelInx >= 0 && ! cmbAdducts.getItem(cmbAdducts.getSelectionIndex()).equalsIgnoreCase(GlycanPreDefinedOptions.OTHER)){
			if(TextFieldUtils.isEmpty(txtPreAdductNum)) {
				setErrorMessage("Please enter a valid number");
				return false;
			}
			if(!TextFieldUtils.isNonZero(txtPreAdductNum)) {
				setErrorMessage("Please enter a valid number");
				return false;
			}
		}else{
			if(TextFieldUtils.isEmpty(txtAdductName)) {
				setErrorMessage("Please enter a valid name");
				return false;
			} else if ( txtAdductName.getText().trim().length() > OTHER_ADDUCT_NAME_MAX_LENGTH ) {
				setErrorMessage("The maximum length for the name of a custom ion is " + OTHER_ADDUCT_NAME_MAX_LENGTH + ".");
				return false;												
			}
			if(TextFieldUtils.isEmpty(txtAdductLabel)) {
				setErrorMessage("Please enter a valid label");
				return false;
			} else if ( txtAdductLabel.getText().trim().length() > OTHER_ADDUCT_LABEL_MAX_LENGTH ) {
				setErrorMessage("The maximum length for the label of a custom ion is " + OTHER_ADDUCT_LABEL_MAX_LENGTH + ".");
				return false;												
			}
			if(TextFieldUtils.isEmpty(txtAdductMass)||!TextFieldUtils.isDouble(txtAdductMass)) {
				setErrorMessage("Please enter a valid mass");
				return false;
			}
			if(TextFieldUtils.isEmpty(txtPreAdductNum)||!TextFieldUtils.isNonZero(txtPreAdductNum)) {
				setErrorMessage("Please enter a valid number");
				return false;
			}
			if( dialogType != DialogType.NEUTRAL_LOSSGAIN && (TextFieldUtils.isEmpty(txtAdductCharge) || ! TextFieldUtils.isNonZero(txtAdductCharge) )) {
				setErrorMessage("Please enter a valid charge");
				return false;
			}
			for(String adductName : cmbAdducts.getItems()) {
				if(adductName.equalsIgnoreCase(txtAdductLabel.getText().trim()) ) {
					setErrorMessage("Specified label is already predefined. Please specify a different label.");
					return false;
				}
			}
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
		button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		button.setEnabled(validateInput());
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				updateAdduct();
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 500);
	}

	public Molecule getAdduct() {
		return adduct;
	}

	public void setAdduct(Molecule adduct) {
		this.adduct = adduct;
	}

}
