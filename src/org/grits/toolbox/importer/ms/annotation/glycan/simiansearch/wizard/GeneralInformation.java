package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.utilShare.SelectionInterface;
import org.grits.toolbox.core.utilShare.TextFieldUtils;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.ExperimentSelectionAdapter;
import org.grits.toolbox.ms.annotation.filter.Filter;
/**
 * 
 * @author Khalifeh AlJadda
 *
 */
public class GeneralInformation extends WizardPage implements SelectionInterface{
	protected final Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT); 
	private Text txtExp;
	private Text txtName;
	private Text txtDesc;
	private MSGlycanAnnotationWizard myWizard;
	private boolean readyToFinish = false;
	private String displayName = "";
	private String annDescription = "";
	
	/**
	 * Create the wizard.
	 */
	public GeneralInformation(MSGlycanAnnotationWizard wizard,List<Filter> preFilters) {
		super("wizardPage");
		setTitle("General Information");
		setDescription("Input general information");
		this.myWizard = wizard;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(3, false));
		
		Label lblMsExperiment = new Label(container, SWT.NONE);
		lblMsExperiment.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMsExperiment.setText("MS Experiment");
		
		txtExp = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		txtExp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnBrowse = new Button(container, SWT.NONE);
		btnBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnBrowse.setText("Browse");
		if(myWizard.getMSEntries() != null){
//			txtExp.setText(myWizard.getMSEntries().getDisplayName());
//			btnBrowse.setEnabled(false);
		}
		
		ExperimentSelectionAdapter sampleProjectSelectionAdapter = new ExperimentSelectionAdapter();
		sampleProjectSelectionAdapter.setParent(container);
//		sampleProjectSelectionAdapter.setEntry(myWizard.getMSEntries());
		sampleProjectSelectionAdapter.setParentWindow(this);
//		sampleProjectSelectionAdapter.setText(sampleText);
		btnBrowse.addSelectionListener(sampleProjectSelectionAdapter);
		
		Label lblDisplayName = new Label(container, SWT.NONE);
		lblDisplayName.setText("Display Name");
		lblDisplayName = setMandatoryLabel(lblDisplayName);
		
		txtName = new Text(container, SWT.BORDER);
		txtName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setDisplayName(txtName.getText().trim());
				if(validateInput()){	
					 canFlipToNextPage();
					 getWizard().getContainer().updateButtons();
					}
					else{
						readyToFinish = false;
						canFlipToNextPage();
						getWizard().getContainer().updateButtons();
					}
				
			}
		});
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label lblDescription = new Label(container, SWT.NONE);
		lblDescription.setText("Description");
		
		txtDesc = new Text(container, SWT.BORDER | SWT.MULTI);
		txtDesc.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				annDescription = txtDesc.getText().trim();
					
			}
		});
		GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_txtDesc.heightHint = 66;
		txtDesc.setLayoutData(gd_txtDesc);
		setControl(container);
		
		
		
		setControl(container);
		
		setPageComplete(false);
	}
	
	protected Label setMandatoryLabel(Label lable) {
		lable.setText(lable.getText()+"*");
		lable.setFont(boldFont);
		return lable;
	}
	
	public IWizardPage getNextPage() {
		return super.getNextPage();
	}
	
	
	public boolean validateInput(){
		if(TextFieldUtils.isEmpty(txtExp)){
			setErrorMessage("Please select MS Experiment");
			return false;
		}
//		if(isDuplicate(txtName)){
//			setErrorMessage("Duplicate Name");
//			return false;
//		}
		if(TextFieldUtils.isEmpty(txtName)){
			setErrorMessage("Please input display name");
			return false;
		}
		
		setErrorMessage(null);
		readyToFinish = true;
		return true;
	}

//	public boolean isDuplicate(Text input){
//		List<Entry> entries = myWizard.getMSEntries().getChildren();
//		if(entries.size() == 0)
//			return false;
//		for(Entry entry : entries){
//			if(entry.getDisplayName().equals(input.getText().trim()))
//				return true;
//		}
//		return false;
		
//	}
	
	@Override
	public boolean canFlipToNextPage(){
		if(readyToFinish){
			return true;
		}
		else
			return false;
	}

	@Override
	public void updateComponent(SelectionAdapter adapter) {
		if ( adapter instanceof ExperimentSelectionAdapter ) { // update the Sample Data
//			myWizard.getMSEntries(( (ExperimentSelectionAdapter) adapter).getEntry());
			txtExp.setText( ( (ExperimentSelectionAdapter) adapter).getEntry().getDisplayName() );
			if(validateInput()){
				 canFlipToNextPage();
				 getWizard().getContainer().updateButtons();
				}else{
					readyToFinish = false;
					canFlipToNextPage();
					getWizard().getContainer().updateButtons();
				}
		}	
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getAnnDescription() {
		return annDescription;
	}

	public void setAnnDescription(String annDescription) {
		this.annDescription = annDescription;
	}

}
