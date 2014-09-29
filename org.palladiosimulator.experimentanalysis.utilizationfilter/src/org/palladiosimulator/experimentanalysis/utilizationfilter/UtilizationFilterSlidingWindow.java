package org.palladiosimulator.experimentanalysis.utilizationfilter;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

import org.palladiosimulator.experimentanalysis.KeepLastElementPriorToLowerBoundStrategy;
import org.palladiosimulator.experimentanalysis.SlidingWindow;
import org.palladiosimulator.experimentanalysis.SlidingWindowUtilizationAggregator;
import org.palladiosimulator.measurementframework.Measurement;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

/**
 * This class is a {@link SlidingWindow} subclass intended to be used by utilization filters at
 * analysis time, e.g, to provide another another kind of visualization data.<br>
 * The window is thus not governed by any simulation logic. Once the window is full, i.e., a
 * {@link Measurement} is added that is out of the window's bounds (that is, the
 * {@code point in time} component is greater than the window's current upper bound) the window
 * moves forward by a fixed increment (until the new {@link Measurement} is within the window's
 * bounds) and the so far collected measurements are propagated to the connected
 * {@link SlidingWindowUtilizationAggregator} that is attached to the window upon initialization.<br>
 * 
 * @see SlidingWindowUtilizationAggregator
 * @see UtilizationFilter
 * @see SlidingWindow
 * 
 * @author Florian Rosenthal
 */
final class UtilizationFilterSlidingWindow extends SlidingWindow {

    private static final MetricDescription ACCEPTED_WINDOW_DATA_METRIC = MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE;

    /**
     * Initializes a new instance of the {@link UtilizationFilterSlidingWindow} class with the given
     * parameters.
     * 
     * @param windowLength
     *            The length of the window, given in any arbitrary {@link Duration}. Additionally,
     *            this measure also denotes the window increment.
     * @param aggregator
     *            A {@link SlidingWindowUtilizationAggregator} that is used to calculate the
     *            utilization based on the window data.
     * @throws IllegalArgumentException
     *             If the given window length is invalid, i.e., {@code null} or negative, or the
     *             given aggregator is {@code null.}
     * @see UtilizationFilterSlidingWindow#UtilizationFilterSlidingWindow(Measure, Measure)
     */
    UtilizationFilterSlidingWindow(Measure<Double, Duration> windowLength, SlidingWindowUtilizationAggregator aggregator) {
        this(windowLength, windowLength, aggregator);
    }

    /**
     * Initializes a new instance of the {@link UtilizationFilterSlidingWindow} class with the given
     * parameters.
     * 
     * @param windowLength
     *            The length of the window, given in any arbitrary {@link Duration}.
     * @param increment
     *            This {@link Measure} indicates the increment by what the window is moved on, given
     *            in any arbitrary {@link Duration}.
     * @param aggregator
     *            A {@link SlidingWindowUtilizationAggregator} that is used to calculate the
     *            utilization based on the window data.
     * @throws IllegalArgumentException
     *             If either of the measure arguments is invalid, i.e., {@code null} or negative, or
     *             the given aggregator is {@code null.}
     * @see UtilizationFilterSlidingWindow#UtilizationFilterSlidingWindow(Measure)
     */
    UtilizationFilterSlidingWindow(Measure<Double, Duration> windowLength, Measure<Double, Duration> increment,
            SlidingWindowUtilizationAggregator aggregator) {
        super(windowLength, increment, ACCEPTED_WINDOW_DATA_METRIC, new KeepLastElementPriorToLowerBoundStrategy());
        this.addObserver(aggregator);
    }

    /**
     * By calling this method, clients tell the window that no more measurements are to be added.<br>
     * It is reasonable to call this method after all data has been passed to the window in order to
     * ensure that all are processed correctly.
     */
    void noMoreDataAvailable() {
        onWindowFullEvent();
    }

    @Override
    public void addMeasurement(Measurement newMeasurement) {
        super.checkAddMeasurementPrerequisites(newMeasurement);
        Measure<Double, Duration> pointInTime = newMeasurement
                .getMeasureForMetric(MetricDescriptionConstants.POINT_IN_TIME_METRIC);
        while (pointInTime.compareTo(getCurrentUpperBound()) > 0) {
            // window is full, we have to move on
            // we have to do this inside a loop as the window increment might be small
            // and the new measurement still out of bounds after one "move on"
            onWindowFullEvent();
        }
        addMeasurementInternal(newMeasurement);
    }
}
