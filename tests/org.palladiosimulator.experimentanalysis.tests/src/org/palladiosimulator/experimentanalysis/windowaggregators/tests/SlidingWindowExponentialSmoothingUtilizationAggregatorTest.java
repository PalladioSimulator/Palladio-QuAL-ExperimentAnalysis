package org.palladiosimulator.experimentanalysis.windowaggregators.tests;

import static org.junit.Assert.assertEquals;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowExponentialSmoothingUtilizationAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.measurementframework.TupleMeasurement;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

public class SlidingWindowExponentialSmoothingUtilizationAggregatorTest extends SlidingWindowUtilizationAggregatorTest {

    private static final Amount<Duration> ONE_SECOND = Amount.valueOf(1, SI.SECOND);
    private static final Amount<Duration> ZERO_DURATION = Amount.valueOf(0d, SI.SECOND);

    private Measure<Integer, Duration> samplingRate;
    private Amount<Duration> reportingPeriod;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.samplingRate = Measure.valueOf(5, SI.SECOND);
        this.reportingPeriod = Amount.valueOf(0.5, NonSI.MINUTE); // 30 seconds

        this.windowLength = Measure.valueOf(this.reportingPeriod.doubleValue(NonSI.HOUR), NonSI.HOUR);
        this.emptyUtilizationMeasurement = new TupleMeasurement(
                MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE, this.windowLength,
                Measure.valueOf(0d, Unit.ONE));

        this.aggregatorUnderTest = new SlidingWindowExponentialSmoothingUtilizationAggregator(
                this.expectedWindowDataMetric, this.dummyRecorder, this.samplingRate, this.reportingPeriod);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetSamplingRate() {
        Measurable<Duration> actualRate = ((SlidingWindowExponentialSmoothingUtilizationAggregator) this.aggregatorUnderTest)
                .getSamplingRate();

        assertEquals(this.samplingRate.doubleValue(SI.SECOND), actualRate.doubleValue(SI.SECOND), DELTA);
    }

    @Test
    public void testGetSmoothingFactor() {
        double actualFactor = ((SlidingWindowExponentialSmoothingUtilizationAggregator) this.aggregatorUnderTest)
                .getSmoothingFactor();

        double expectedFactor = 1
                - Math.exp(-this.samplingRate.doubleValue(SI.SECOND) / this.reportingPeriod.doubleValue(SI.SECOND));
        assertEquals(expectedFactor, actualFactor, DELTA);
    }

    @Override
    @Test
    public void testOnSlidingWindowFullUtilizationSameDurationUnits() {

        // window position: [0-30] (in s) //0.5 min = 30s
        // sampling rate is 5Hz, i.e. 5s

        for (int i = 0; i < 30; i += 5) {
            Measure<Double, Duration> pointInTimeMeasure = Measure.valueOf(i + 0.5, SI.SECOND);
            if (i % 2 == 0) {
                this.data.addLast(
                        new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.busyStateMeasure));
            } else {
                this.data.addLast(
                        new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.idleStateMeasure));
            }

        }
        this.aggregatorUnderTest.onSlidingWindowFull(this.data, this.currentLowerBound, this.windowLength);

        // we added 3 busy and 3 idle measurements: 0.5s (busy), 5.5s (idle), 10.5s (busy), 15.5s
        // (idle), 20.5s (busy), 25.5s (idle)
        double smoothingFactor = 1
                - Math.exp(-this.samplingRate.doubleValue(SI.SECOND) / this.reportingPeriod.doubleValue(SI.SECOND));

        // after 0s:
        double expectedLoad = 0;
        // after 5s:
        expectedLoad = smoothingFactor * this.busyStateMeasure.doubleValue(Unit.ONE);
        // after 10s:
        expectedLoad *= (1 - smoothingFactor);
        // after 15s:
        expectedLoad = expectedLoad * (1 - smoothingFactor)
                + smoothingFactor * this.busyStateMeasure.doubleValue(Unit.ONE);
        // after 20s:
        expectedLoad *= (1 - smoothingFactor);
        // after 25s:
        expectedLoad = expectedLoad * (1 - smoothingFactor)
                + smoothingFactor * this.busyStateMeasure.doubleValue(Unit.ONE);
        // after 30s:
        expectedLoad *= (1 - smoothingFactor);

        MeasuringValue expected = new TupleMeasurement(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                this.windowLength, Measure.valueOf(expectedLoad, Unit.ONE));

        assertLastRecordedMeasurementEquals(expected);
    }

    @Override
    @Test
    public void testOnSlidingWindowFullUtilizationDifferentDurationUnits() {

        // window position: [0-30] (in s) //0.5 min = 30s
        // sampling rate is 5Hz, i.e. 5s

        for (int i = 0; i < 30; i += 5) {
            Measure<Double, Duration> pointInTimeMeasure = Measure.valueOf(i + 0.5, SI.SECOND);
            if (i % 2 == 0) {
                this.data.addLast(
                        new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.busyStateMeasure));
            } else {
                this.data.addLast(new TupleMeasurement(this.expectedWindowDataMetric,
                        pointInTimeMeasure.to(SI.DECI(NonSI.HOUR)), this.idleStateMeasure));
            }

        }
        this.aggregatorUnderTest.onSlidingWindowFull(this.data, this.currentLowerBound, this.windowLength);

        // we added 3 busy and 3 idle measurements: 0.5s (busy), 5.5s (idle), 10.5s (busy), 15.5s
        // (idle), 20.5s (busy), 25.5s (idle)
        double smoothingFactor = 1
                - Math.exp(-this.samplingRate.doubleValue(SI.SECOND) / this.reportingPeriod.doubleValue(SI.SECOND));

        // after 0s:
        double expectedLoad = 0;
        // after 5s:
        expectedLoad = smoothingFactor * this.busyStateMeasure.doubleValue(Unit.ONE);
        // after 10s:
        expectedLoad *= (1 - smoothingFactor);
        // after 15s:
        expectedLoad = expectedLoad * (1 - smoothingFactor)
                + smoothingFactor * this.busyStateMeasure.doubleValue(Unit.ONE);
        // after 20s:
        expectedLoad *= (1 - smoothingFactor);
        // after 25s:
        expectedLoad = expectedLoad * (1 - smoothingFactor)
                + smoothingFactor * this.busyStateMeasure.doubleValue(Unit.ONE);
        // after 30s:
        expectedLoad *= (1 - smoothingFactor);

        MeasuringValue expected = new TupleMeasurement(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                this.windowLength, Measure.valueOf(expectedLoad, Unit.ONE));

        assertLastRecordedMeasurementEquals(expected);
    }

    @Override
    @Test
    public void testOnSlidingWindowFullNoUtilization() {
        // window position: [0-30] (in s) //0.5 min = 30s
        // sampling rate is 5Hz, i.e. 5s

        // only add "idleness"
        for (int i = 0; i < 30; ++i) {
            this.data.addLast(new TupleMeasurement(this.expectedWindowDataMetric, Measure.valueOf(i + 0.1, SI.SECOND),
                    this.idleStateMeasure));
        }

        this.aggregatorUnderTest.onSlidingWindowFull(this.data, this.currentLowerBound, this.windowLength);

        assertLastRecordedMeasurementEquals(this.emptyUtilizationMeasurement);
    }

    @Test(expected = NullPointerException.class)
    public void testSlidingWindowUtilizationAggregatorCtorNullSamplingRate() {
        new SlidingWindowExponentialSmoothingUtilizationAggregator(super.expectedWindowDataMetric, super.dummyRecorder,
                null, ONE_SECOND);
    }

    @Test(expected = NullPointerException.class)
    public void testSlidingWindowUtilizationAggregatorCtorNullReportingPeriod() {
        new SlidingWindowExponentialSmoothingUtilizationAggregator(super.expectedWindowDataMetric, super.dummyRecorder,
                ONE_SECOND, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlidingWindowUtilizationAggregatorCtorInvalidSamplingRate() {
        new SlidingWindowExponentialSmoothingUtilizationAggregator(super.expectedWindowDataMetric, super.dummyRecorder,
                ZERO_DURATION, ONE_SECOND);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlidingWindowUtilizationAggregatorCtorInvalidReportingPeriod() {
        new SlidingWindowExponentialSmoothingUtilizationAggregator(super.expectedWindowDataMetric, super.dummyRecorder,
                ONE_SECOND, ONE_SECOND.opposite());
    }

}
