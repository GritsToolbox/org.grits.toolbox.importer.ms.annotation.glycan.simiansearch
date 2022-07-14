package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

/**
 * 
 * @author Khalifeh AlJadda
 *
 */
public class GlycanAdvancedSettings extends TitleAreaDialog {
	private Text shiftText;
    private Button button;
    private boolean allowInnerFragment=false;
    private double shift = 0.0;
    private Combo innerFragment;
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public GlycanAdvancedSettings(Shell parentShell) {
		super(parentShell);
//		this.gSettings = gSettings;
//		this.method = method;
////		gSettings.setAllowInnerFragments(false);
////		method.setShift(0.0);
		
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Advanced Settings");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.widthHint = 48;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("Shift");
		
		shiftText = new Text(container, SWT.BORDER);
		shiftText.setText("0.0");
		shiftText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		shiftText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(validateInput()){
				 button.setEnabled(true);
				}else{
					button.setEnabled(false);
				}
				
			}
		});
		
		Label lblInnerFragment = new Label(container, SWT.NONE);
		lblInnerFragment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblInnerFragment.setText("Allow Inner Fragment");
		
		innerFragment = new Combo(container, SWT.NONE);
		innerFragment.setItems(new String[] {"True", "False"});
		innerFragment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		innerFragment.select(1);

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		 button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		 button.addMouseListener(new MouseAdapter() {
		 	@Override
		 	public void mouseDown(MouseEvent e) {
		 		if(innerFragment.getItem(innerFragment.getSelectionIndex()).equals("True"))
					allowInnerFragment = true;
				shift = Double.parseDouble(shiftText.getText());
		 	}
		 });
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				 
				
			}
		});
		button.setText("Apply");
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	
	public boolean validateInput(){
		if(TextFieldUtils.isEmpty(shiftText) || ! TextFieldUtils.isDouble(shiftText)){
			setErrorMessage("Enter a valid number in the Accuracy field");
			return false;
		}		
		setErrorMessage(null);
		return true;		
	}
	
	public double getShift() {
		return shift;
	}

	public void setShift(double shift) {
		this.shift = shift;
	}

	public boolean isAllowInnerFragment() {
		return allowInnerFragment;
	}

	public void setAllowInnerFragment(boolean allowInnerFragment) {
		this.allowInnerFragment = allowInnerFragment;
	}

}
