package org.palladiosimulator.experimentanalysis.windowaggregators.tests;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowUtilizationAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.measurementframework.TupleMeasurement;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

public class SlidingWindowUtilizationAggregatorTest extends SlidingWindowAggregatorTest {

    protected MeasuringValue emptyUtilizationMeasurement;
    private MeasuringValue expectedNotEmptyUtilizationMeasurement;
    protected MetricSetDescription expectedWindowDataMetric;
    /**
     * indicates an idle resource without active processes
     */
    protected Measure<Long, Dimensionless> idleStateMeasure;
    /**
     * indicates a busy resource with 42 active processes
     */
    protected Measure<Long, Dimensionless> busyStateMeasure;
    /**
     * constant that denotes the maximum delta between double values for which both numbers are
     * still considered equal
     */
    protected static final double DELTA = Math.pow(10, -12);

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.expectedWindowDataMetric = MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE;
        this.aggregatorUnderTest = new SlidingWindowUtilizationAggregator(this.expectedWindowDataMetric,
                this.dummyRecorder);
        this.emptyUtilizationMeasurement = createEmptyUtilizationTupleMeasurement();
        this.expectedNotEmptyUtilizationMeasurement = createUtilizationTupleMeasurement(0.7d);
        this.idleStateMeasure = Measure.valueOf(0L, Unit.ONE);
        this.busyStateMeasure = Measure.valueOf(42L, Unit.ONE);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private MeasuringValue createUtilizationTupleMeasurement(final double utilization) {

        Measure<Double, Duration> resultPointInTimeMeasure = Measure.valueOf(
                this.currentLowerBound.doubleValue(SI.SECOND) + this.windowLength.doubleValue(SI.SECOND), SI.SECOND);
        return new TupleMeasurement(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                resultPointInTimeMeasure, Measure.valueOf(utilization, Unit.ONE));
    }

    private MeasuringValue createEmptyUtilizationTupleMeasurement() {

        return createUtilizationTupleMeasurement(0d);
    }

    private void addStateOfActiveResourceTupleMeasurementsNoUtilization() {
        // window position: [0-10]

        Measure<Double, Duration> pointInTimeMeasure = Measure.valueOf(0d, SI.SECOND);
        this.data.addLast(
                new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.idleStateMeasure));

        pointInTimeMeasure = Measure.valueOf(3d, SI.SECOND);
        this.data.addLast(
                new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.idleStateMeasure));

        pointInTimeMeasure = Measure.valueOf(6d, SI.SECOND);
        this.data.addLast(
                new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.idleStateMeasure));

        // this setup should yield a utilization of 0
        // [0-3], [4-6], [7-10]: 10s idleness
    }

    private void addStateOfActiveResourceTupleMeasurementsUtilizationSameDurationUnits() {
        // window position: [0-10]

        // start with utilization
        Measure<Double, Duration> pointInTimeMeasure = Measure.valueOf(0d, SI.SECOND);
        this.data.addLast(
                new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.busyStateMeasure));

        // no utilization next
        pointInTimeMeasure = Measure.valueOf(3d, SI.SECOND);
        this.data.addLast(
                new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.idleStateMeasure));

        // again some processes active
        pointInTimeMeasure = Measure.valueOf(6d, SI.SECOND);
        this.data.addLast(
                new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.busyStateMeasure));

        // this setup should yield a utilization of 0.70 (70%)
        // [0-2], [6-10]: 7s activity
        // [3-5]: 3s idleness
    }

    private void addStateOfActiveResourceTupleMeasurementsUtilizationDifferentDurationUnits() {
        // window position: [0-10]

        // start with utilization
        Measure<Double, Duration> pointInTimeMeasure = Measure.valueOf(0d, SI.SECOND);
        this.data.addLast(
                new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.busyStateMeasure));

        // no utilization next
        pointInTimeMeasure = Measure.valueOf(3000d, SI.MILLI(SI.SECOND)); // 3000ms = 3s
        this.data.addLast(
                new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.idleStateMeasure));

        // again some processes active
        pointInTimeMeasure = Measure.valueOf(600d, SI.CENTI(SI.SECOND)); // 600 cs = 3s
        this.data.addLast(
                new TupleMeasurement(this.expectedWindowDataMetric, pointInTimeMeasure, this.busyStateMeasure));

        // this setup should yield a utilization of 0.70 (70%)
        // [0-2], [6-10]: 7s activity
        // [3-5]: 3s idleness
    }

    @Test(expected = NullPointerException.class)
    public void testSlidingWindowUtilizationAggregatorCtorNullMetric() {
        new SlidingWindowUtilizationAggregator(null, this.dummyRecorder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSlidingWindowUtilizationAggregatorCtorWrongMetric() {
        MetricDescription wrongMetric = MetricDescriptionConstants.COST_OVER_TIME;
        new SlidingWindowUtilizationAggregator(wrongMetric, this.dummyRecorder);
    }

    @Test
    public void testGetExpectedWindowDataMetric() {
        assertEquals(this.expectedWindowDataMetric, this.aggregatorUnderTest.getExpectedWindowDataMetric());
    }

    @Test
    public void testGetAllowedWindowDataMetrics() {
        // compare ids of metrics rather than metrics directly
        List<String> allowedMetricIds = SlidingWindowUtilizationAggregator.getAllowedWindowDataMetrics().stream()
                .map(MetricDescription::getId).collect(toList());

        assertEquals(2, allowedMetricIds.size());

        assertTrue(allowedMetricIds.contains(MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE.getId()));
        assertTrue(allowedMetricIds.contains(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE.getId()));
    }

    @Test
    public void testOnSlidingWindowFullEmptyData() {
        this.aggregatorUnderTest.onSlidingWindowFull(this.data, this.currentLowerBound, this.windowLength);
        assertLastRecordedMeasurementEquals(this.emptyUtilizationMeasurement);
    }

    @Test
    public void testOnSlidingWindowFullNoUtilization() {
        addStateOfActiveResourceTupleMeasurementsNoUtilization();
        this.aggregatorUnderTest.onSlidingWindowFull(this.data, this.currentLowerBound, this.windowLength);
        assertLastRecordedMeasurementEquals(this.emptyUtilizationMeasurement);
    }

    @Test
    public void testOnSlidingWindowFullUtilizationSameDurationUnits() {
        addStateOfActiveResourceTupleMeasurementsUtilizationSameDurationUnits();
        this.aggregatorUnderTest.onSlidingWindowFull(this.data, this.currentLowerBound, this.windowLength);
        assertLastRecordedMeasurementEquals(this.expectedNotEmptyUtilizationMeasurement);
    }

    @Test
    public void testOnSlidingWindowFullUtilizationDifferentDurationUnits() {
        addStateOfActiveResourceTupleMeasurementsUtilizationDifferentDurationUnits();
        this.aggregatorUnderTest.onSlidingWindowFull(this.data, this.currentLowerBound, this.windowLength);
        assertLastRecordedMeasurementEquals(this.expectedNotEmptyUtilizationMeasurement);
    }

    protected final void assertLastRecordedMeasurementEquals(final MeasuringValue expected) {
        MeasuringValue lastMeasurement = this.dummyRecorder.getLastMeasurement();
        assertNotNull(lastMeasurement);
        assertMeasurementsEqual(expected, lastMeasurement);
    }

    private static void assertMeasurementsEqual(final MeasuringValue expected, final MeasuringValue actual) {
        Measure<Double, Dimensionless> expectedUtilization = expected
                .getMeasureForMetric(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
        Measure<Double, Dimensionless> actualUtilization = actual
                .getMeasureForMetric(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
        Measure<Double, Duration> expectedPointInTime = expected
                .getMeasureForMetric(MetricDescriptionConstants.POINT_IN_TIME_METRIC);
        Measure<Double, Duration> actualPointInTime = actual
                .getMeasureForMetric(MetricDescriptionConstants.POINT_IN_TIME_METRIC);

        // ensure that compared values are given in the same unit
        assertEquals(expectedUtilization.doubleValue(Unit.ONE), actualUtilization.doubleValue(Unit.ONE), DELTA);
        assertEquals(expectedPointInTime.doubleValue(SI.SECOND), actualPointInTime.doubleValue(SI.SECOND), DELTA);

    }

}
