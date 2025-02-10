package org.palladiosimulator.experimentanalysis.utilizationfilter.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import jakarta.measure.Measure;
import jakarta.measure.quantity.Duration;
import jakarta.measure.unit.SI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.palladiosimulator.experimentanalysis.utilizationfilter.UtilizationFilterConfiguration;

public class UtilizationFilterConfigurationTest {

    private UtilizationFilterConfiguration configurationUnderTest;
    
    @Before
    public void setUp() throws Exception {
        this.configurationUnderTest = new UtilizationFilterConfiguration();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetDefaultConfiguration() {
       Map<String, Object> defaultConfig = this.configurationUnderTest.getDefaultConfiguration();
       assertTrue(defaultConfig.size() == 2);
       assertTrue(defaultConfig.containsKey(UtilizationFilterConfiguration.WINDOW_INCREMENT_KEY));
       assertTrue(defaultConfig.containsKey(UtilizationFilterConfiguration.WINDOW_LENGTH_KEY));
    }

    @Test
    public void testGetWindowIncrement() {
        Map<String, Object> newConfig = new HashMap<String, Object>(this.configurationUnderTest.getDefaultConfiguration());
       
        Measure<Double, Duration> expected = Measure.valueOf(1500d, SI.MILLI(SI.SECOND)); //1.5s
        newConfig.put(UtilizationFilterConfiguration.WINDOW_INCREMENT_KEY, expected);
        this.configurationUnderTest.setProperties(newConfig);
        assertEquals(expected, this.configurationUnderTest.getWindowIncrement());
    }

    @Test
    public void testGetWindowLength() {
        Map<String, Object> newConfig = new HashMap<String, Object>(this.configurationUnderTest.getDefaultConfiguration());
                
        Measure<Double, Duration> expected = Measure.valueOf(200d, SI.CENTI(SI.SECOND)); //2s
        newConfig.put(UtilizationFilterConfiguration.WINDOW_LENGTH_KEY, expected);
        this.configurationUnderTest.setProperties(newConfig);
        assertEquals(expected, this.configurationUnderTest.getWindowLength());
    }

}
