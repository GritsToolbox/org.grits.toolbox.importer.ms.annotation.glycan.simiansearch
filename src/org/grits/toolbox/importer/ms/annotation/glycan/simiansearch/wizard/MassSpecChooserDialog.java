package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.MassOptions;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.dialog.ProjectExplorerDialog;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.EntrySelectionAdapter;

import org.grits.toolbox.entry.ms.property.MassSpecProperty;

public class MassSpecChooserDialog extends EntrySelectionAdapter {

	private Grid entryDialogGrid = null;
	private HashMap<String, String> listEntries = null;
	private java.util.List<Entry> entries = null;
	private String annotationMethod;

	public MassSpecChooserDialog(String a_propertyType,
			String a_dialogTitle, String a_dialogMessage, String a_strAnnotMethod) {
		super(a_propertyType, a_dialogTitle, a_dialogMessage);
		this.annotationMethod = a_strAnnotMethod;
		// TODO Auto-generated constructor stub
	}

	public static String getDisplayNameByEntryName( String _sEntryId ) {
		Entry root = PropertyHandler.getDataModel().getRoot();
		return getDisplayNameByEntryName(_sEntryId, root);

	}

	public static String getDisplayNameByEntryName( String _sEntryId, Entry root ) {
		List<Entry> alStack = new ArrayList<>();
		alStack.add(root);
		while( ! alStack.isEmpty() ) {
			Entry curEntry = alStack.remove(0);
			if( curEntry.getProperty() instanceof MassSpecProperty ) {
				//					if( ((MSGlycanAnnotationProperty) curEntry.getProperty()).getMSAnnotationMetaData().getAnnotationId().equals(_sEntryId) ) {
				//						Entry msEntry = curEntry.getParent();
				//						Entry sampleEntry = msEntry.getParent();
				//						String sName = sampleEntry.getDisplayName() + "." +
				//									   msEntry.getDisplayName() + "." + 
				//									   curEntry.getDisplayName();
				//									   
				//						return sName;
				//					}
			}
			List<Entry> children = curEntry.getChildren();
			for(Entry child : children) {
				alStack.add(child);
			}
		}
		return null;		
	}

	@Override
	public void widgetSelected(SelectionEvent event) 
	{
		Shell newShell = new Shell(parent.getShell(),SWT.PRIMARY_MODAL | SWT.SHEET);
		ProjectExplorerDialog dlg = new ProjectExplorerDialog(newShell);
		// Set a simglycan entry as a filter
		dlg.addFilter(MassSpecProperty.TYPE);
		// Change the title bar text
		dlg.setTitle("Mass Spec Selection");
		// Customizable message displayed in the dialog
		dlg.setMessage("Choose an MS Experiment to add");
		// Calling open() will open and run the dialog.
		if (dlg.open() == Window.OK) {
			Entry entry = dlg.getEntry();
			if (entry != null) {
				//				String displayName = MassSpecChooserDialog.getDisplayNameByEntryName( ((MSGlycanAnnotationProperty)entry.getProperty()).getMSAnnotationMetaData().getAnnotationId());
				String displayName = entry.getDisplayName();
				if( displayName == null ) {
					throw new NullPointerException("Unable to determine display name for entry: " + entry.getDisplayName() );
				}
				//				String name = entry.getParent().getParent().getDisplayName() + "/" +entry.getParent().getDisplayName() + "/" + entry.getDisplayName();
				if(listEntries.isEmpty())
				{
					addToList(entry,displayName);
				}
				//if something is there, then check if this entry has the same parent!
				else if(!this.listEntries.containsKey(displayName))
				{
					//					//has to be in the same parent 
					//					if(sameParent(entry,ProjectProperty.TYPE))
					//					{
					addToList(entry, displayName);
					//					}
					//					else
					//					{
					//						//show a message. Please choose the entry in the same parent 
					//						ErrorUtils.createWarningMessageBox(new Shell(parent.getShell(),SWT.PRIMARY_MODAL | SWT.SHEET), "Wrong Simglycan", "Cannot add the selected simglycan\nSelected simglycan is not in the same project of other simglycans.");
					//					}
				}
			}
		}
	}

	private boolean sameParent(Entry entry, String type) {
		//get a element from the list 
		//and get its parent
		//then compare!
		Entry curParent = DataModelSearch.findParentByType(this.entries.get(0), type);
		Entry myParent = DataModelSearch.findParentByType(entry, type);
		if(curParent.equals(myParent)) {
			return true;
		}
		return false;
	}

	private void addToList(Entry entry, String name) {
		//update the hashMap entry history
		String stamp = GeneralInformationMulti.getAnnotationPostfix(this.annotationMethod);
		this.listEntries.put(name, name + stamp);
		//update list

		GridItem item = new GridItem(this.entryDialogGrid, SWT.NONE);
		item.setText(0, name);
		item.setText(1,  name + stamp);
		this.entryDialogGrid.setSelection(this.entryDialogGrid.getItemCount()-1);
		//update the entries
		this.entries.add(entry);
		this.entryDialogGrid.notifyListeners(SWT.Modify, null);
	}

	public void setList(Grid entryDialogGrid) {
		this.entryDialogGrid = entryDialogGrid;
	}

	public void setListEntries(HashMap<String, String> listEntries) {
		this.listEntries = listEntries;
	}

	public void setEntries(java.util.List<Entry> simGLycanEntryList) {
		this.entries = simGLycanEntryList;
	}



}
