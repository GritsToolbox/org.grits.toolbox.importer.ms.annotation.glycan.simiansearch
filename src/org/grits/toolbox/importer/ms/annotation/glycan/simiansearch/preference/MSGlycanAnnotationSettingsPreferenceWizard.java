package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.preference;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.grits.toolbox.entry.ms.annotation.glycan.preference.MSGlycanAnnotationPreference;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.AddAdductsForm;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.AddIonExchangeForm;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.AddNeutralLossForm;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.FragmentSettingsForm;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.GlycanSettingsForm;
import org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard.MSGlycanAnnotationWizard;
import org.grits.toolbox.ms.om.data.Method;
import org.grits.toolbox.util.structure.glycan.util.FilterUtils;

public class MSGlycanAnnotationSettingsPreferenceWizard extends MSGlycanAnnotationWizard {
	//log4J Logger
	private static final Logger logger = Logger.getLogger(MSGlycanAnnotationWizard.class);
	private String preferenceName;

	/**
	 * constructor for creating a new preference
	 * 
	 * @param name name of the preference
	 */
	public MSGlycanAnnotationSettingsPreferenceWizard(String name){
		setWindowTitle("MS Annotation Settings");
		method = new Method();
		this.preferenceName = name;
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
	
	/**
	 * constructor used for editing
	 * 
	 * @param preference to be displayed
	 */
	public MSGlycanAnnotationSettingsPreferenceWizard(MSGlycanAnnotationPreference preference) {
		this(preference.getName());
		this.method = preference.getMethod();
		this.preferences = preference;
	}
		
	@Override
	public void addPages() {
		glycanSettingsForm = new GlycanSettingsForm(method,this,filterLibrary);
		fragmentSettingsForm = new FragmentSettingsForm(method);
		ionSettingsForm = new AddAdductsForm(method);
		ionExchangeForm = new AddIonExchangeForm(method);
		neutralLossForm = new AddNeutralLossForm(method);
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
			   glycanSettingsForm.canFlipToNextPage() &&
			   fragmentSettingsForm.canFinish() &&
			   ionExchangeForm.isPageComplete() &&
			   neutralLossForm.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		boolean bCanFinish = canFinish();
		if( bCanFinish ) { // update prefs
			if (preferences == null) 
				preferences = new MSGlycanAnnotationPreference();
			preferences.setName(preferenceName);
			preferences.setMethod(method);
		}
		return bCanFinish;
	}
	
	@Override
	public MSGlycanAnnotationPreference getPreferences() {
		return preferences;
	}
}
