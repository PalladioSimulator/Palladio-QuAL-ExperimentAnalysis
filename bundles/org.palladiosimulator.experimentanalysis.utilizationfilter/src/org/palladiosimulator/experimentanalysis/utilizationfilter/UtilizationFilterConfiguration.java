package org.palladiosimulator.experimentanalysis.utilizationfilter;

import java.util.HashMap;
import java.util.Map;

import jakarta.measure.Measure;
import jakarta.measure.quantity.Duration;
import jakarta.measure.unit.SI;
import jakarta.measure.unit.Unit;

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
     * Gets the default value of the <b>window increment</b> property.
     * @return A {@link Measure} denoting the default value of the property.
     */
	public static Measure<Double, Duration> getDefaultWindowIncrement() {
	    return DEFAULT_WINDOW_INCREMENT;
	}
	
	/**
     * Gets the default value of the <b>window length</b> property.
     * @return A {@link Measure} denoting the default value of the property.
     */
	public static Measure<Double, Duration> getDefaultWindowLength() {
	    return DEFAULT_WINDOW_LENGTH;
	}
	
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
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue) {
	    Object checkedValue = newValue;
	    if (newValue != getNotSetConstant()) {
            if (WINDOW_LENGTH_KEY.equals(key) || WINDOW_INCREMENT_KEY.equals(key)) {
                checkedValue = checkGetDurationMeasure(checkedValue);
            }
	    }
	    super.propertyChanged(key, oldValue, checkedValue);
	}
	
	@SuppressWarnings("unchecked")
    private Measure<Double, Duration> checkGetDurationMeasure(Object value) {
	    if (value == null) {
            throw new IllegalArgumentException("Given measure must not be null.");
        } else if (!(value instanceof Measure)) {
            throw new IllegalArgumentException("Given measure must be a valid JScience measure.");
        } else {
            Measure<?, ?> measure = (Measure<?, ?>) value;
            if (!(measure.getValue() instanceof Number)
                    || !Duration.UNIT.isCompatible(measure.getUnit())) {
                throw new IllegalArgumentException("Given measure must be a valid JScience duration measure.");
            }
            Number measureValue = (Number) measure.getValue(); 
            if (!(Double.compare(0d, measureValue.doubleValue()) < 0)) {
                throw new IllegalArgumentException("Given measure must denote a positive duration.");
            }
            return Measure.valueOf(measureValue.doubleValue(), (Unit<Duration>) measure.getUnit());            
        }
	}
	
	

}
