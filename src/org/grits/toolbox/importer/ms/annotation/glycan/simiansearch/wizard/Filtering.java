package org.grits.toolbox.importer.ms.annotation.glycan.simiansearch.wizard;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.entry.ms.annotation.glycan.filter.MSGlycanAnnotationFilterSetup;
import org.grits.toolbox.util.structure.glycan.filter.om.Category;
import org.grits.toolbox.util.structure.glycan.filter.om.Filter;
import org.grits.toolbox.util.structure.glycan.filter.om.FilterSetting;
import org.grits.toolbox.util.structure.glycan.filter.om.FiltersLibrary;
import org.grits.toolbox.util.structure.glycan.gui.FilterChangedListener;
import org.grits.toolbox.util.structure.glycan.gui.FilterTableSetup;

public class Filtering extends TitleAreaDialog implements FilterChangedListener {
	
	private static final Logger logger = Logger.getLogger(Filtering.class);
	
	private List<Filter> preFilters = null;
	FilterTableSetup filterTableSetup;
	FilterSetting filterSetting;

	private Category preferredCategory;
	private FiltersLibrary filterLibrary;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public Filtering(Shell parentShell, FiltersLibrary fLib, Category filterCategory, FilterSetting filterSetting) {
		super(parentShell);
		this.filterLibrary = fLib;
		this.preFilters = fLib.getFilters();
		this.filterSetting = filterSetting;
		this.preferredCategory = filterCategory;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Filtering Settings");
		Composite area = (Composite) super.createDialogArea(parent);
		
		ScrolledComposite sc = new ScrolledComposite(area, SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 400;
		data.widthHint = 700;
		sc.setLayoutData(data);
		
		Composite comp1 = new Composite(sc, SWT.NONE);
		comp1.setLayout(new GridLayout(4, false));
		
		//addSelectFilterItem(comp1);
		
		Composite container = new Composite(comp1, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//filterTableSetup = new FilterTableSetup();
		if (filterLibrary.getCategories() != null)
			filterTableSetup = new MSGlycanAnnotationFilterSetup(filterLibrary.getCategories());
		else
			filterTableSetup = new MSGlycanAnnotationFilterSetup();
		filterTableSetup.setFilterList(preFilters);
		((MSGlycanAnnotationFilterSetup)filterTableSetup).setSelectedCategory(preferredCategory);
		try {
			filterTableSetup.createFilterTableSection(container);
			if (filterSetting != null)
				filterTableSetup.setExistingFilters (filterSetting);
			filterTableSetup.addFilterChangedListener(this);
		} catch (Exception e) {
			logger.error("Error creating the filter table", e);
			MessageDialog.openError(getShell(), "Error", "Error creating the filter table!");
		}
		
		sc.setContent(comp1);
		sc.setMinSize(comp1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return area;
	}

	/**
	 * Return the initial size of the dialog.
	 */
	/*@Override
	protected Point getInitialSize() {
		return new Point(450, 415);
	}*/

	@Override
	public void filterChanged() {
		if (filterTableSetup != null) {
			filterSetting = filterTableSetup.getFilterSetting();
		}
	}
	
	public FilterSetting getFilterSetting() {
		return filterSetting;
	}
	
	public void setFilterSetting(FilterSetting filterSetting) {
		this.filterSetting = filterSetting;
	}
}
