package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationSettingsPreference;
import org.grits.toolbox.entry.ms.annotation.glycan.util.FileUtils;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;
import org.grits.toolbox.util.structure.glycan.util.FilterUtils;

/**
 * 
 * @author Khalifeh AlJadda
 *
 */

public class MSGlycanAnnotationWizard extends Wizard {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationWizard.class);
	
	protected GeneralInformationMulti initial = null;
	protected GlycanSettingsForm glycanSettingsForm = null;
	protected AddAdductsForm ionSettingsForm = null;
	protected FragmentSettingsForm fragmentSettingsForm = null;
	protected AddIonExchangeForm ionExchangeForm = null;
	protected AddNeutralLossForm neutralLossForm = null;
	protected List<Entry> msEntries = null;
	protected Method method = null;
	protected MSGlycanAnnotationPreference preferences = null;
	protected FiltersLibrary filterLibrary;

	public MSGlycanAnnotationWizard(){
		setWindowTitle("MS Annotation");
		method = new Method();
		try {
			filterLibrary = FilterUtils.readFilters(getDefaultFiltersPath());
		} catch (UnsupportedEncodingException e) {
			logger.error("Error getting the filters", e);
		} catch (FileNotFoundException e) {
			logger.error("Error getting the filters", e);
		} catch (JAXBException e) {
			logger.error("Error getting the filters", e);
		}
	}
    
	public MSGlycanAnnotationPreference getPreferences() {
		return preferences;
	}
	
	public void setPreferences(MSGlycanAnnotationPreference preferences) {
		this.preferences = preferences;
		// update all the pages
		glycanSettingsForm.updateControlsFromPreferences();
		fragmentSettingsForm.updateControlsFromPreferences();
		ionSettingsForm.updateControlsFromPreferences();
		ionExchangeForm.updateControlsFromPreferences();
		neutralLossForm.updateControlsFromPreferences();
	}

	public FragmentSettingsForm getFragmentSettings() {
		return fragmentSettingsForm;
	}
	
	public AddIonExchangeForm getIonExchangeForm() {
		return ionExchangeForm;
	}
	
	public AddNeutralLossForm getNeutralLossForm() {
		return neutralLossForm;
	}
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public GlycanSettingsForm getGlycanSettingsForm() {
		return glycanSettingsForm;
	}

	public void setGlycanSettingsForm(GlycanSettingsForm one) {
		this.glycanSettingsForm = one;
	}

	public AddAdductsForm getIonSettingsForm() {
		return ionSettingsForm;
	}

	public void setIonSettingsForm(AddAdductsForm two) {
		this.ionSettingsForm = two;
	}

	public GeneralInformationMulti getInitial() {
		return initial;
	}

	public void setInitial(GeneralInformationMulti initial) {
		this.initial = initial;
	}

	@Override
	public void addPages() {
//		initial = new GeneralInformation(this,preFilters);
		initial = new GeneralInformationMulti(this.msEntries);
		try {
			initial.loadPreferences(MSGlycanAnnotationSettingsPreference.class, MSGlycanAnnotationSettingsPreference.getPreferenceEntity());
		} catch (UnsupportedVersionException e) {
			logger.error("Could not load preferences", e);
		}
		glycanSettingsForm = new GlycanSettingsForm(method,this,filterLibrary);
		fragmentSettingsForm = new FragmentSettingsForm(method);
		ionSettingsForm = new AddAdductsForm(method);
		ionExchangeForm = new AddIonExchangeForm(method);
		neutralLossForm = new AddNeutralLossForm(method);
		addPage(initial);
		addPage(glycanSettingsForm);
		addPage(fragmentSettingsForm);
		addPage(ionSettingsForm);
		addPage(ionExchangeForm);
		addPage(neutralLossForm);
	}
	
	@Override
	public boolean canFinish() {
		if(glycanSettingsForm.canFinish())
			glycanSettingsForm.save();
		else 
			return false;
		if(fragmentSettingsForm.canFinish())
			fragmentSettingsForm.save();
		else 
			return false;
		if(ionSettingsForm.canFlipToNextPage())
			ionSettingsForm.save();
		else 
			return false;
		if(ionExchangeForm.canFlipToNextPage())
			ionExchangeForm.save();
		else 
			return false;
		if(neutralLossForm.isPageComplete())
			neutralLossForm.save();
		else 
			return false;
		
		return ionSettingsForm.isPageComplete() &&
			   initial.canFlipToNextPage() &&
			   glycanSettingsForm.canFlipToNextPage() &&
			   fragmentSettingsForm.canFinish() &&
			   ionExchangeForm.isPageComplete() &&
			   neutralLossForm.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		boolean bCanFinish = canFinish();
		if( bCanFinish ) { // update prefs
			//TODO decide what to do with preference update!!!
			//glycanSettingsForm.updatePreferences();
			//preferences.saveValues();
		}
		return bCanFinish;
	}

	public List<Entry> getMSEntries() {
		return msEntries;
	}

	public void setMSEntries(List<Entry> msEntries) {
		this.msEntries = msEntries;
	}
	
	public String getDefaultFiltersPath() {
		return FileUtils.getFilterPath();
	}	
	
}
