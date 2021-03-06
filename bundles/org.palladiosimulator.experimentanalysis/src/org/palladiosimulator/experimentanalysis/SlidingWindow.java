package org.palladiosimulator.experimentanalysis;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.eclipse.emf.ecore.EClass;
import org.palladiosimulator.commons.designpatterns.AbstractObservable;
import org.palladiosimulator.edp2.models.ExperimentData.Measurement;
import org.palladiosimulator.edp2.util.MetricDescriptionUtility;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowUtilizationAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.BaseMetricDescription;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.MetricSpecPackage;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

/**
 * This class is an implementation of a fixed length sliding window.<br>
 * Purpose of this window is to collect measurements of a certain kind that occur within the window
 * length. These measurements are written into the window by a {@link SlidingWindowRecorder}.<br>
 * Once the window is full, the window moves forward by a fixed increment and the collected
 * measurements are propagated to all connected {@link ISlidingWindowListener}s, e.g., certain
 * {@link SlidingWindowAggregator}s.<br>
 * Additionally, each window instance has an attached {@link ISlidingWindowMoveOnStrategy} that
 * defines how the collected data (i.e., the measurements) is adjusted when the window moves
 * forward.
 * 
 * @see KeepLastElementPriorToLowerBoundStrategy
 * @see SlidingWindowAggregator
 * @see SlidingWindowUtilizationAggregator
 * 
 * @author Florian Rosenthal
 */
public abstract class SlidingWindow extends AbstractObservable<ISlidingWindowListener> {

    private static final NumericalBaseMetricDescription POINT_IN_TIME_METRIC = (NumericalBaseMetricDescription) MetricDescriptionConstants.POINT_IN_TIME_METRIC;
    private static final EClass BASE_METRIC_DESC_ECLASS = MetricSpecPackage.Literals.BASE_METRIC_DESCRIPTION;

    private final Measure<Double, Duration> windowLength;
    private Measure<Double, Duration> currentLowerBound;
    private final Measure<Double, Duration> increment;
    private final MetricDescription acceptedMetrics;
    private final ISlidingWindowMoveOnStrategy moveOnStrategy;
    private final Deque<MeasuringValue> data = new LinkedList<>();

    // store whether the accepted metric is a (numerical) base metric
    private final boolean acceptsBaseMetric;

    /**
     * Initializes a new instance of the {@link SlidingWindow} class with the given parameters.
     * 
     * @param windowLength
     *            The length of the window, given in any arbitrary {@link Duration}. Additionally,
     *            this measure also denotes the window increment.
     * @param acceptedMetrics
     *            As each window only accepts measurements that adhere to a certain metric, a
     *            {@link MetricDescription} of must be specified.
     * @param moveOnStrategy
     *            The {@link ISlidingWindowMoveOnStrategy} instance that defines how the collected
     *            data (i.e., the measurements) is adjusted when the window moves forward.
     * @throws IllegalArgumentException
     *             In one of the following cases:
     *             <ul>
     *             <li>given window length is negative</li>
     *             <li>{@code acceptedMetrics} or {@code moveOnStrategy} is {@code null}</li>
     *             </ul>
     * @see SlidingWindow#SlidingWindow(Measure, Measure, MetricDescription,
     *      ISlidingWindowMoveOnStrategy)
     */
    public SlidingWindow(Measure<Double, Duration> windowLength, MetricDescription acceptedMetrics,
            ISlidingWindowMoveOnStrategy moveOnStrategy) {
        this(windowLength, windowLength, acceptedMetrics, moveOnStrategy);
    }

    /**
     * Initializes a new instance of the {@link SlidingWindow} class with the given parameters.
     * 
     * @param windowLength
     *            The length of the window, given in any arbitrary {@link Duration}.
     * @param increment
     *            This {@link Measure} indicates the increment by what the window is moved on, given
     *            in any arbitrary {@link Duration}.
     * @param acceptedMetrics
     *            As each window only accepts measurements that adhere to a certain metric, a
     *            {@link MetricDescription} of must be specified.
     * @param moveOnStrategy
     *            The {@link ISlidingWindowMoveOnStrategy} instance that defines how the collected
     *            data (i.e., the measurements) is adjusted when the window moves forward.
     * @throws IllegalArgumentException
     *             In one of the following cases:
     *             <ul>
     *             <li>given window length or increment is negative</li>
     *             <li>{@code acceptedMetrics} or {@code moveOnStrategy} is {@code null}</li>
     *             </ul>
     */
    public SlidingWindow(Measure<Double, Duration> windowLength, Measure<Double, Duration> increment,
            MetricDescription acceptedMetrics, ISlidingWindowMoveOnStrategy moveOnStrategy) {

        this(windowLength, increment, Measure.valueOf(0d, SI.SECOND), acceptedMetrics, moveOnStrategy);
    }

    /**
     * Initializes a new instance of the {@link SlidingWindow} class with the given parameters.
     * 
     * @param windowLength
     *            The length of the window, given in any arbitrary {@link Duration}.
     * @param increment
     *            This {@link Measure} indicates the increment by what the window is moved on, given
     *            in any arbitrary {@link Duration}.
     * @param initialLowerBound
     *            This {@link Measure} indicates the lower bound value at which the algorithm starts
     *            aggregating, given in any arbitrary {@link Duration}.
     * @param acceptedMetrics
     *            As each window only accepts measurements that adhere to a certain metric, a
     *            {@link MetricDescription} of must be specified.
     * @param moveOnStrategy
     *            The {@link ISlidingWindowMoveOnStrategy} instance that defines how the collected
     *            data (i.e., the measurements) is adjusted when the window moves forward.
     * @throws IllegalArgumentException
     *             In one of the following cases:
     *             <ul>
     *             <li>given window length or increment is negative</li>
     *             <li>{@code acceptedMetrics} or {@code moveOnStrategy} is {@code null}</li>
     *             </ul>
     */
    public SlidingWindow(Measure<Double, Duration> windowLength, Measure<Double, Duration> increment,
            Measure<Double, Duration> initialLowerBound, MetricDescription acceptedMetrics,
            ISlidingWindowMoveOnStrategy moveOnStrategy) {

        checkCtorParameters(windowLength, increment, initialLowerBound, acceptedMetrics, moveOnStrategy);

        // ensure that we have Doubles and not Integers, Longs, etc.
        // otherwise measure.getValue() doesn't yield a Double but a
        // ClassCastException
        this.windowLength = Measure.valueOf(windowLength.doubleValue(windowLength.getUnit()), windowLength.getUnit());
        this.increment = Measure.valueOf(increment.doubleValue(increment.getUnit()), increment.getUnit());
        this.currentLowerBound = initialLowerBound;
        this.acceptedMetrics = acceptedMetrics;
        this.moveOnStrategy = moveOnStrategy;

        this.acceptsBaseMetric = (BASE_METRIC_DESC_ECLASS.isInstance(this.acceptedMetrics));
    }

    /**
     * This method proves the validity of the arguments passed to either of the constructors.
     * 
     * @param windowLength
     *            The length of the window, given in any arbitrary {@link Duration}.
     * @param increment
     *            This {@link Measure} indicates the increment by what the window is moved on, given
     *            in any arbitrary {@link Duration}.
     * @param acceptedMetrics
     *            As each window only accepts measurements that adhere to a certain metric, a
     *            {@link MetricDescription} of must be specified.
     * @param moveOnStrategy
     *            The {@link ISlidingWindowMoveOnStrategy} instance that defines how the collected
     *            data (i.e., the measurements) is adjusted when the window moves forward.
     * @throws IllegalArgumentException
     *             In one of the following cases:
     *             <ul>
     *             <li>given window length or increment is negative</li>
     *             <li>{@code acceptedMetrics} or {@code moveOnStrategy} is {@code null}</li>
     *             </ul>
     */
    private static void checkCtorParameters(Measure<Double, Duration> windowLength, Measure<Double, Duration> increment,
            Measure<Double, Duration> initialLowerBound, MetricDescription acceptedMetrics,
            ISlidingWindowMoveOnStrategy moveOnStrategy) {

        if (!isDurationMeasureValid(windowLength, false)) {
            throw new IllegalArgumentException("Given window length is invalid.");
        }
        if (!isDurationMeasureValid(increment, false)) {
            throw new IllegalArgumentException("Given increment is invalid.");
        }
        if (!isDurationMeasureValid(initialLowerBound, true)) {
            throw new IllegalArgumentException("Given inital lower bound is invalid.");
        }
        if (acceptedMetrics == null) {
            throw new IllegalArgumentException("A sliding window only accepts measurements that adhere to a metric'.\n"
                    + "Such a metric description was not given.");
        }
        if (moveOnStrategy == null) {
            throw new IllegalArgumentException(
                    "An moveOnStrategy must be given that defines what happens to the (potentially obsolete)"
                            + "window data when the window is incremented.");
        }
    }

    /**
     * Checks whether the given duration {@link Measure} is valid.
     * 
     * @param measure
     *            A {@link Measure} denoting a point in time.
     * @return {@code true} if the point in time value denoted by the given measure is
     *         {@code != null, != infinite, != NaN} and {@code >= 0}, {@code false} otherwise.
     */
    private static boolean isDurationMeasureValid(Measure<Double, Duration> measure, boolean greaterEqualsZero) {
        boolean result = false;
        if (measure != null) {
            // ensure that we have a double, as value may actually be a Long,
            // Integer, ...
            Double value = measure.doubleValue(measure.getUnit());
            result = (value != null && !value.isInfinite() && !value.isNaN()
                    && (greaterEqualsZero ? (value.doubleValue() >= 0d) : (value.doubleValue() > 0d)));
        }
        return result;
    }

    /**
     * This method is invoked once the sliding window is full.<br>
     * All attached {@link ISlidingWindowListener} are notified and the window moves on by the
     * specified increment.
     */
    protected final void onWindowFullEvent() {
        notifyObserversOnWindowFull();
        moveOn();
    }

    /**
     * Notifies all attached observers that the sliding window is full, i.e., invokes their
     * respective {@link ISlidingWindowListener#onSlidingWindowFull(Iterable, Measure, Measure)}
     * callback method.
     */
    private void notifyObserversOnWindowFull() {
        this.getEventDispatcher().onSlidingWindowFull(Collections.unmodifiableCollection(this.data),
                this.currentLowerBound, getEffectiveWindowLength());
    }

    /**
     * Indicates whether the given measurements adheres to, that is, is compatible with this
     * window's accepted metric.
     * 
     * @param measurement
     *            A {@link MeasuringValue} instance.
     * @return {@code true} if the measurement is compatible, otherwise {@code false}.
     */
    private boolean measurementAdheresToMetric(MeasuringValue measurement) {
        // consider special case that window accepts a base metric and
        // measurement is a tuple which contains this base metric
        // e.g., it is valid if a 'Response Time Tuple' is received and window
        // expects 'Response Time' only
        return measurement.isCompatibleWith(this.acceptedMetrics)
                || (acceptsBaseMetric() && MetricDescriptionUtility.isBaseMetricDescriptionSubsumedByMetricDescription(
                        (BaseMetricDescription) this.acceptedMetrics, measurement.getMetricDesciption()));

    }

    /**
     * Adds a new measurement to the data this window collects.
     * 
     * @param newMeasurement
     *            The {@link MeasuringValue} instance to add.
     * @throws IllegalArgumentException
     *             If the given measurement is {@code null} or does not adhere to the metric this
     *             window accepts.
     */
    public void addMeasurement(MeasuringValue newMeasurement) {
        checkAddMeasurementPrerequisites(newMeasurement);
        addMeasurementInternal(newMeasurement);
    }

    /**
     * This method is intended to be overridden by subclasses. They might do so to check their
     * additional (potentially more specific) prerequisites.
     * 
     * @param newMeasurement
     *            The {@link MeasuringValue} to be added to the window data.
     * @throws IllegalArgumentException
     *             If the given measurement is {@code null} or does not adhere to the metric this
     *             window accepts.
     */
    protected void checkAddMeasurementPrerequisites(MeasuringValue newMeasurement) {
        if (newMeasurement == null) {
            throw new IllegalArgumentException("Given measurement is null.");
        }
        if (!measurementAdheresToMetric(newMeasurement)) {
            throw new IllegalArgumentException("Given measurement does not adhere to (or subsume) specified metric.\n"
                    + "Expected metric: " + this.acceptedMetrics.getName() + "\nGiven measurement metric: "
                    + newMeasurement.getMetricDesciption().getName());
        }
    }

    /**
     * This method adds the given measurement to the window data. <b>No prerequisite checks are
     * performed.</b><br>
     * As this method is intended to be used by subclasses when they override
     * {@link SlidingWindow#addMeasurement(MeasuringValue)}, implementations should take care to
     * call {@link SlidingWindow#checkAddMeasurementPrerequisites(MeasuringValue)} beforehand to
     * ensure uncorrupt window data.
     * 
     * @param newMeasurement
     *            The {@link Measurement} to be added.
     */
    protected final void addMeasurementInternal(MeasuringValue newMeasurement) {
        Measure<?, Duration> pointInTime = newMeasurement.getMeasureForMetric(POINT_IN_TIME_METRIC);
        if (getCurrentLowerBound().compareTo(pointInTime) > 0) {
            // this indicates a "gap": we only must keep the last measurement
            // prior to the new lower bound
            // hence, discard all previous ones
            this.flush();
        }
        // consider special case that window accepts a base metric and
        // measurement is a tuple which contains this base metric
        // e.g., a 'Response Time Tuple' is to be added and window
        // expects 'Response Time' only:
        // store full tuple measurement also in such a case since the move on strategy
        // associated with this instance
        // might require the corresponding pint in times of the data in order to perform the
        // adjustment
        this.data.addLast(newMeasurement);
    }

    /**
     * Discards the data (i.e., the measurements) currently collected by this window.
     */
    public final void flush() {
        this.data.clear();
    }

    /**
     * Gets the metric this window accepts.
     * 
     * @return A {@link MetricDescription} denoting the metric this window accepts.
     * @see SlidingWindow#addMeasurement(Measurement)
     */
    public final MetricDescription getAcceptedMetric() {
        return this.acceptedMetrics;
    }

    /**
     * Gets whether the metric accepted by this window is a {@link BaseMetricDescription}. The same
     * result is obtained by calling {@link #getAcceptedMetric()} and then doing a type check.
     * 
     * @return {@code true} if the metric is a base metric, otherwise {@code false}.
     * @see #getAcceptedMetric()
     */
    protected final boolean acceptsBaseMetric() {
        return this.acceptsBaseMetric;
    }

    /**
     * Gets the current lower (i.e., left) bound of the window.
     * 
     * @return A {@link Measure} denoting the current upper bound.
     * @see SlidingWindow#getEffectiveWindowLength()
     * @see SlidingWindow#getCurrentUpperBound()
     */
    public final Measure<Double, Duration> getCurrentLowerBound() {
        return this.currentLowerBound;
    }

    /**
     * Gets the current upper bound of the window.
     * 
     * @return A {@link Measure} denoting the current upper bound.
     * @see SimulationGovernedSlidingWindow#getEffectiveWindowLength()
     */
    public Measure<Double, Duration> getCurrentUpperBound() {
        double lowerBoundValue = this.currentLowerBound.getValue();
        double upperBoundValue = lowerBoundValue
                + getSpecifiedWindowLength().doubleValue(this.currentLowerBound.getUnit());

        return Measure.valueOf(upperBoundValue, this.currentLowerBound.getUnit());
    }

    /**
     * Gets the window length as specified in
     * {@link SlidingWindow#SlidingWindow(Measure, MetricDescription, ISlidingWindowMoveOnStrategy)}
     * or
     * {@link SlidingWindow#SlidingWindow(Measure, Measure, MetricDescription, ISlidingWindowMoveOnStrategy)}
     * .<br>
     * Note, that the current, effective window length might be smaller.
     * 
     * @return A {@link Measure} denoting the specified window length.
     * @see SlidingWindow#getEffectiveWindowLength()
     * @see SlidingWindow#getCurrentUpperBound()
     */
    protected final Measure<Double, Duration> getSpecifiedWindowLength() {
        return this.windowLength;
    }

    /**
     * Gets the current, effective window length. Note that the effective window length might be
     * smaller than specified in
     * {@link SlidingWindow#SlidingWindow(Measure, MetricDescription, ISlidingWindowMoveOnStrategy)}
     * or
     * {@link SlidingWindow#SlidingWindow(Measure, Measure, MetricDescription, ISlidingWindowMoveOnStrategy)}
     * .<br>
     * 
     * @return A {@link Measure} denoting the instantaneous effective window length.
     * @see SlidingWindow#getCurrentUpperBound()
     */
    public Measure<Double, Duration> getEffectiveWindowLength() {
        return getSpecifiedWindowLength();
    }

    /**
     * Gets the value this window's lower bound is incremented by once the window moves on.
     * 
     * @return A {@link Measure} denoting the window increment.
     * @see SlidingWindow#SlidingWindow(Measure, MetricDescription, ISlidingWindowMoveOnStrategy)
     * @see SlidingWindow#SlidingWindow(Measure, Measure, MetricDescription,
     *      ISlidingWindowMoveOnStrategy)
     */
    public final Measure<Double, Duration> getIncrement() {
        return this.increment;
    }

    /**
     * Moves on, that is, the window's lower bound is incremented and the collected measurements are
     * adjusted according to the specified {@link ISlidingWindowMoveOnStrategy}.
     */
    private void moveOn() {
        adjustLowerBound();
        this.moveOnStrategy.adjustData(this.data, this.currentLowerBound, this.increment);
    }

    /**
     * Adjusts the window's lower bound, that increments it by the specified value.
     */
    private void adjustLowerBound() {
        this.currentLowerBound = Measure.valueOf(
                this.currentLowerBound.getValue() + this.increment.doubleValue(this.currentLowerBound.getUnit()),
                this.currentLowerBound.getUnit());
    }

    /**
     * Indicates if there are currently measurements available.
     * 
     * @return {@code false}, if yes, otherwise {@code true}
     */
    public final boolean isEmpty() {
        return this.data.isEmpty();
    }

    /**
     * Gets the number of currently collected measurements.
     * 
     * @return A nonnegative value denoting the number of elements.
     */
    public final int getNumberOfElements() {
        return this.data.size();
    }

    /**
     * Attaches a {@link ISlidingWindowListener} instance to this window.<br>
     * Most commonly, the given listener is a {@link SlidingWindowAggregator}.
     * 
     * @param arg0
     *            The listener to attach.
     * @throws IllegalArgumentException
     *             If the given listener is {@code null} or expects a metric other than this
     *             window's accepted one.
     * @see ISlidingWindowListener#getExpectedWindowDataMetric()
     */
    @Override
    public final void addObserver(ISlidingWindowListener arg0) {
        if (arg0 == null) {
            throw new IllegalArgumentException("Observer to attach must not be null.");
        }
        if (!this.acceptedMetrics.equals(arg0.getExpectedWindowDataMetric())) {
            throw new IllegalArgumentException(
                    "Listener cannot be attached as it is expecting a metric other than this window's accepted one.");
        }
        super.addObserver(arg0);
    }

    /**
     * Gets the currently attached observers.
     * 
     * @return An <b>unmodifiable</b> list containing the attached {@link ISlidingWindowListener}s.
     */
    public final List<ISlidingWindowListener> getAttachedObservers() {
        return Collections.unmodifiableList(super.getObservers());
    }
}
