package org.palladiosimulator.experimentanalysis.utilizationfilter;

import org.eclipse.ui.IMemento;
import org.palladiosimulator.edp2.datastream.configurable.IPropertyConfigurable;
import org.palladiosimulator.edp2.datastream.ui.configurable.DataSinkElementFactory;

public class UtilizationFilterInputFactory extends DataSinkElementFactory {

	static final String FACTORY_ID = UtilizationFilterInputFactory.class.getCanonicalName();
	
	@Override
	protected IPropertyConfigurable createElementInternal(IMemento memento) {
		return new UtilizationFilter();
	}

}
