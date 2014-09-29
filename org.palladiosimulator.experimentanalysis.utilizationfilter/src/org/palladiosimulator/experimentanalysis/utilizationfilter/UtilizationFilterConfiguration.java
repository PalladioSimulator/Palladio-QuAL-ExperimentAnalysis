package org.palladiosimulator.experimentanalysis.utilizationfilter;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.palladiosimulator.edp2.datastream.configurable.reflective.ConfigurationProperty;
import org.palladiosimulator.edp2.datastream.configurable.reflective.ReflectivePropertyConfigurable;

/**
 * This class contains properties to configure the behavior of {@link UtilizationFilter}s.
 * With this class, window length and increment, that are used by {@link UtilizationFilter}s to process their input, can be adapted by the user.
 * @see UtilizationFilter
 * @author Florian Rosenthal
 *
 */
public final class UtilizationFilterConfiguration extends ReflectivePropertyConfigurable {

	public static final String WINDOW_LENGTH_KEY = "windowLength"; //must equal the respective property name!
	public static final String WINDOW_INCREMENT_KEY = "windowIncrement";
	private static final Measure<Double, Duration> DEFAULT_WINDOW_LENGTH = Measure.valueOf(10d, SI.SECOND);
	private static final Measure<Double, Duration> DEFAULT_WINDOW_INCREMENT = Measure.valueOf(10d, SI.SECOND);
		
	@ConfigurationProperty(description = "A measure denoting the current window length.")
	private Measure<Double, Duration> windowLength;
	
	@ConfigurationProperty(description = "A measure denoting the current window increment.")
	private Measure<Double, Duration> windowIncrement;
	
	/**
	 * Gets the current value of the <b>window increment</b> property.
	 * @return A {@link Measure} denoting the value of the property.
	 */
	public Measure<Double, Duration> getWindowIncrement() {
		return this.windowIncrement;
	}
	
	/**
     * Gets the current value of the <b>window length</b> property.
     * @return A {@link Measure} denoting the value of the property.
     */
	public Measure<Double, Duration> getWindowLength() {
        return this.windowLength;
    }
	
	/**
	 * Initializes a new instance of the {@link UtilizationFilterConfiguration} class.
	 */
	public UtilizationFilterConfiguration() {
	    super();
	}
	
	/**
	 * Gets the default configuration settings, that is default values for window length and window increment.
	 * @return A {@link Map} containing the default values of the window length and window increment properties.
	 */
	@Override
	public Map<String, Object> getDefaultConfiguration() {
		Map<String, Object> result = new HashMap<String, Object>(super.getDefaultConfiguration());
		result.put(WINDOW_LENGTH_KEY, DEFAULT_WINDOW_LENGTH);
		result.put(WINDOW_INCREMENT_KEY, DEFAULT_WINDOW_INCREMENT);
				
		return result;
	}

}
