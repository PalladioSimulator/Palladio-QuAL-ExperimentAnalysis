package org.palladiosimulator.experimentanalysis.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Deque;
import java.util.LinkedList;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.palladiosimulator.experimentanalysis.DiscardAllElementsPriorToLowerBoundStrategy;
import org.palladiosimulator.experimentanalysis.ISlidingWindowMoveOnStrategy;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.measurementframework.TupleMeasurement;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

public class DiscardAllElementsPriorToLowerBoundStrategyTest {

    private Deque<MeasuringValue> data;
    private ISlidingWindowMoveOnStrategy strategyUnderTest;
    private Measure<Double, Duration> currentLowerBound;
    private Measure<Double, Duration> increment;
    private MeasuringValue expectedMeasurement;
    private final MetricSetDescription metricDescription = MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE;

    @Before
    public void setUp() throws Exception {
        this.strategyUnderTest = new DiscardAllElementsPriorToLowerBoundStrategy();
        this.data = new LinkedList<>();

        this.currentLowerBound = Measure.valueOf(0d, SI.SECOND);
        this.increment = Measure.valueOf(5d, SI.SECOND);
        this.expectedMeasurement = new TupleMeasurement(metricDescription, Measure.valueOf(5.75, SI.SECOND),
                Measure.valueOf(42L, Unit.ONE));
    }

    @After
    public void tearDown() {
        this.data.clear();
    }

    private void initiallyFillWindowWithMeasurements() {
        // window position: [0-10] (implicitly)
        Measure<Double, Duration> pointInTimeMeasure;
        Measure<Long, Dimensionless> stateMeasure;
        for (int i = 0; i < 10; ++i) {
            if (i == 5) {
                this.data.addLast(this.expectedMeasurement); // the first to be in the list after
                                                             // strategy has been applied
            } else {
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
        }
        // 10 elements in window: (0.75s, 42), (1.75s, 42), ..., (9.75s, 42)
        // the last 5 of which should be in window after a move on (increment 5s): (5.75s, 42), ...,
        // (9.75s, 42)
    }

    private void initiallyFillWindowWithMeasurementsOneElement() {
        // window position: [0-10]
        this.data.addLast(this.expectedMeasurement);
        // 1 element in window: (5.75s, 42)
        // should be in window kept after move on
    }

    private void initiallyFillWindowWithMeasurementsNoElementToKeep() {
        // window position: [0-10]
        this.data.addLast(new TupleMeasurement(metricDescription, Measure.valueOf(0.000625, NonSI.HOUR),
                Measure.valueOf(42L, Unit.ONE)));
        // 1 element in window: (2.25s, 42) which is (0.000625h, 42)
        // should not be in window kept after move on
    }

    private void mockMoveOn() {
        this.currentLowerBound = Measure.valueOf(this.currentLowerBound.getValue() + this.increment.getValue(),
                SI.SECOND);
        // window position now: [5-15]
    }

    @Test
    public void testAdjustDataOneElementAvailable() {
        initiallyFillWindowWithMeasurementsOneElement();
        mockMoveOn();
        this.strategyUnderTest.adjustData(this.data, this.currentLowerBound, this.increment);
        assertEquals(1, this.data.size());
        assertEquals(this.expectedMeasurement, this.data.peekFirst());
    }

    @Test
    public void testAdjustDataOneElementAvailableNotToKeep() {
        initiallyFillWindowWithMeasurementsNoElementToKeep();
        mockMoveOn();
        this.strategyUnderTest.adjustData(this.data, this.currentLowerBound, this.increment);
        assertTrue(this.data.isEmpty());
        assertTrue(this.data.peekFirst() == null);
    }

    @Test
    public void testAdjustDataNoDataAvailable() {
        mockMoveOn();
        this.strategyUnderTest.adjustData(this.data, this.currentLowerBound, this.increment);
        assertEquals(0, this.data.size());
    }

    @Test
    public void testAdjustData() {
        initiallyFillWindowWithMeasurements();
        mockMoveOn();
        this.strategyUnderTest.adjustData(this.data, this.currentLowerBound, this.increment);
        assertEquals(5, this.data.size());
        assertEquals(this.expectedMeasurement, this.data.peekFirst());
    }

}
