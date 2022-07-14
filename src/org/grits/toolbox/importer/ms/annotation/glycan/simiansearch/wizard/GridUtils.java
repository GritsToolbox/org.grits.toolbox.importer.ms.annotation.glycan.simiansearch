package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.AddEditIonDialog.DialogType;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.Molecule;
import org.grits.toolbox.ms.om.data.MoleculeSettings;

public class GridUtils {

	public static void addToAdductGrid(Grid gridAdduct, List<GridItem> adductGridItems, Map<String, Object> adducts, Molecule adduct){
		GridItem item = new GridItem(gridAdduct, SWT.NONE);
		String sName = "";
		String sLabel = "";
		String sPolarity = "";
		String sCharge = "";
		String sMass = "";
		String sCount = "";
		if( adduct instanceof IonSettings ) {
			sName = ((IonSettings) adduct).getName();
			sLabel = ((IonSettings) adduct).getLabel();
			sPolarity = ((IonSettings) adduct).getPolarity() ? "Positive" : "Negative";
			sCharge = ((IonSettings) adduct).getCharge() + "";
			sMass = ((IonSettings) adduct).getMass() + "";
			sCount = ((IonSettings) adduct).getCounts().get(0) + "";;
		} else {
			sName = ((MoleculeSettings) adduct).getName();
			sLabel = ((MoleculeSettings) adduct).getLabel();
			sMass = ((MoleculeSettings) adduct).getMass() + "";
			sCount = ((MoleculeSettings) adduct).getCount() + "";;			
		}
		int i = 0;
		item.setText(i++, sName);
		item.setText(i++, sLabel);
		if( adduct instanceof IonSettings ) {
			item.setText(i++, sPolarity);
			item.setText(i++, sCharge);			
		}
		item.setText(i++, sMass);
		item.setText(i++, sCount);
		adducts.put(adduct.getLabel(), adduct);
		adductGridItems.add(item);
	}
	
	public static Molecule getDuplicateAdduct(Map<String, Object> adducts, Molecule adduct){
		if(adducts.get(adduct.getLabel())!=null){
			return (Molecule) adducts.get(adduct.getLabel());
		} else {
			return null;
		}
	}
	
	public static void deleteAdductsFromGrid(Grid gridAdduct, List<GridItem> adductGridItems, Map<String,Object> adducts){
		int[] toRemove = gridAdduct.getSelectionIndices();
		for(int i = 0 ; i < toRemove.length; i++){
			if( adducts.containsKey(gridAdduct.getItem(toRemove[i]).getText(1).trim()) ) {
				adducts.remove(gridAdduct.getItem(toRemove[i]).getText(1).trim());
				gridAdduct.remove(toRemove[i]);
				adductGridItems.remove(toRemove[i]);
			}
		}
	}

	
	public static void deleteAdductFromGrid( Grid gridAdduct, List<GridItem> adductGridItems, Map<String,Object> adducts, String a_label ) {
		int iFoundInx = -1;
		for( int i = 0; i < gridAdduct.getItemCount(); i++ ) {
			if ( gridAdduct.getItem(i).getText(1).trim().equals(a_label) ) {
				iFoundInx = i;
				break;
			}
		}
		if( iFoundInx >= 0 ) {
			adducts.remove(a_label);
			gridAdduct.remove(iFoundInx);
			adductGridItems.remove(iFoundInx);			
		}
	}

	public static void addButtonPressed( String sTitle, Grid gridAdduct, List<GridItem> adductGridItems, 
			Map<String,Object> adducts, List<Molecule> adductList, 
			boolean bAddOther, DialogType dialogType, Shell shell ) {
		AddEditIonDialog dialog = new AddEditIonDialog(shell, sTitle, adductList, bAddOther, dialogType);
		int result = dialog.open();
		Molecule adduct = null;
		if(result == 0){
			adduct = dialog.getAdduct();
			Molecule gridIonSettings = GridUtils.getDuplicateAdduct( adducts, adduct);
			if( gridIonSettings != null ){
				GridUtils.deleteAdductFromGrid(gridAdduct, adductGridItems, adducts, gridIonSettings.getLabel());
			} 
			GridUtils.addToAdductGrid(gridAdduct, adductGridItems, adducts, adduct);
		}		
	}

	public static void editButtonPressed( String sTitle, Grid gridAdduct, List<GridItem> adductGridItems, 
			Map<String,Object> adducts, List<Molecule> adductList, 
			boolean bAddOther, DialogType dialogType, Shell shell ) {
		Molecule adduct = null;
		adduct = (Molecule) adducts.get(gridAdduct.getItem(gridAdduct.getSelectionIndex()).getText(1).trim());
		if(adduct != null){
			AddEditIonDialog dialog = new AddEditIonDialog(shell, sTitle, adduct, adductList, bAddOther, dialogType);
			int result = dialog.open();

			if(result == 0){
				adduct = dialog.getAdduct();
				GridUtils.deleteAdductsFromGrid(gridAdduct, adductGridItems, adducts);						
				Molecule gridIonSettings = GridUtils.getDuplicateAdduct(adducts, adduct);
				if( gridIonSettings != null ){
					GridUtils.deleteAdductFromGrid(gridAdduct, adductGridItems, adducts, gridIonSettings.getLabel());
				} 
				GridUtils.addToAdductGrid(gridAdduct, adductGridItems, adducts, adduct);
			}
		}		
	}

	public static void deleteButtonPressed(Grid gridAdduct, List<GridItem> adductGridItems, Map<String,Object> adducts) {
		GridUtils.deleteAdductsFromGrid(gridAdduct, adductGridItems, adducts);
	}
}
