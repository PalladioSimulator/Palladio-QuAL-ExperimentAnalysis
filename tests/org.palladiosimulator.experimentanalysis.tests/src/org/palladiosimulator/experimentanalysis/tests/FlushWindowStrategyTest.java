package org.palladiosimulator.experimentanalysis.tests;

import static org.junit.Assert.assertTrue;

import java.util.Deque;
import java.util.LinkedList;

import jakarta.measure.Measure;
import jakarta.measure.quantity.Dimensionless;
import jakarta.measure.quantity.Duration;
import jakarta.measure.unit.SI;
import jakarta.measure.unit.Unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.palladiosimulator.experimentanalysis.FlushWindowStrategy;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.measurementframework.TupleMeasurement;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

public class FlushWindowStrategyTest {

    private Deque<MeasuringValue> data;
    private FlushWindowStrategy strategyUnderTest;
    private Measure<Double, Duration> currentLowerBound;
    private Measure<Double, Duration> increment;
    private final MetricSetDescription metricDescription = MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE;

    @Before
    public void setUp() throws Exception {
        this.strategyUnderTest = new FlushWindowStrategy();
        this.data = new LinkedList<>();

        this.currentLowerBound = Measure.valueOf(0d, SI.SECOND);
        this.increment = Measure.valueOf(5d, SI.SECOND);
    }

    @After
    public void tearDown() {
        this.data.clear();
    }

    @Test
    public void testAdjustData() {
        initiallyFillWindowWithMeasurements();
        mockMoveOn();
        this.strategyUnderTest.adjustData(this.data, this.currentLowerBound, this.increment);
        assertTrue(this.data.isEmpty());
    }

    private void mockMoveOn() {
        this.currentLowerBound = Measure.valueOf(this.currentLowerBound.getValue() + this.increment.getValue(),
                SI.SECOND);
        // window position now: [5-15]
    }

    private void initiallyFillWindowWithMeasurements() {
        // window position: [0-10]
        Measure<Double, Duration> pointInTimeMeasure;
        Measure<Long, Dimensionless> stateMeasure;
        for (int i = 0; i < 10; ++i) {
            if (i % 2 == 0) {
                // provide point in time in s
                pointInTimeMeasure = Measure.valueOf(i + 0.75, SI.SECOND);

            } else {
                // provide point in time in ms
                pointInTimeMeasure = Measure.valueOf((i + 0.75) * 1000, SI.MILLI(SI.SECOND));
            }
            stateMeasure = Measure.valueOf(42L, Unit.ONE);
            this.data.addLast(new TupleMeasurement(metricDescription, pointInTimeMeasure, stateMeasure));
        }
        // 10 elements in window
    }

}
