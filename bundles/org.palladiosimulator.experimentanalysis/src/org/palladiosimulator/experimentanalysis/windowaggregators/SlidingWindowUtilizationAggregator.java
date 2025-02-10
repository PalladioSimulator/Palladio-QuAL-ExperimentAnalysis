package org.palladiosimulator.experimentanalysis.windowaggregators;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.measure.Measure;
import jakarta.measure.quantity.Dimensionless;
import jakarta.measure.quantity.Duration;
import jakarta.measure.unit.SI;
import jakarta.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.palladiosimulator.edp2.models.ExperimentData.Measurement;
import org.palladiosimulator.edp2.util.MetricDescriptionUtility;
import org.palladiosimulator.experimentanalysis.SlidingWindow;
import org.palladiosimulator.experimentanalysis.SlidingWindowRecorder;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.measurementframework.TupleMeasurement;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.recorderframework.IRecorder;

/**
 * This class is a {@link SlidingWindowAggregator} implementation which calculates the utilization
 * of an active resource based on a sequence of {@code (point in time, state of active resource)} or
 * {@code (point in time, utilization of active resource)} tuples collected by a
 * {@link SlidingWindow}. <br>
 * The calculated utilization is passed to the attached {@link IRecorder} in the form of a
 * {@code (point in time, utilization of active resource)} tuple each time this implementation
 * processes new window data.
 * 
 * @see SlidingWindowRecorder
 * @see #getAllowedWindowDataMetrics()
 * 
 * @author Florian Rosenthal
 *
 */
public class SlidingWindowUtilizationAggregator extends SlidingWindowAggregator {

    protected static final Amount<Duration> ZERO_DURATION = Amount.valueOf(0, SI.SECOND);

    /**
     * map tuple metrics of measurements that can be processed to their respective numerical base
     * metric containing the 'state of active resource' values to be aggregated
     * 
     * @see #obtainStateValueFromMeasurement(MeasuringValue)
     */
    private static final Map<MetricDescription, NumericalBaseMetricDescription> EXPECTED_WINDOW_METRICS_MAP = new HashMap<>();

    // for convenience, store the key set
    private static final Collection<MetricDescription> EXPECTED_WINDOW_DATA_METRICS = EXPECTED_WINDOW_METRICS_MAP
            .keySet();

    static {
        // fill the map
        // remark: value (numerical base metrics) should have a default unit compatible to
        // Dimensionless.UNIT
        EXPECTED_WINDOW_METRICS_MAP.put(MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE,
                (NumericalBaseMetricDescription) MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC);
        // the following is required for multi-core resources where utilization is measured rather
        // than state;
        // also confer CalculatorHelper.setupOverallUtilizationCalculator(...) in the
        // simucomframework plugin!
        EXPECTED_WINDOW_METRICS_MAP.put(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                (NumericalBaseMetricDescription) MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
    }

    private final MetricDescription windowDataMetric;
    private final NumericalBaseMetricDescription stateOfResourceMetric;

    /**
     * Initializes a new instance of the {@link SlidingWindowUtilizationAggregator} class with the
     * given parameter.
     * 
     * @param windowDataMetric
     *            The {@link MetricDescription} of the measurements to be processed, i.e, which are
     *            used to compute the utilization.
     * @param recorderToWriteInto
     *            An {@link IRecorder} this instance writes the aggregated window data into.
     *            Typically, a recorder that writes into a persistence framework like EDP 2 is
     *            passed here.
     * @throws NullPointerException
     *             If either argument is {@code null}.
     * @throws IllegalArgumentException
     *             If the given metric is not supported by this aggregator.
     */
    public SlidingWindowUtilizationAggregator(final MetricDescription windowDataMetric,
            final IRecorder recorderToWriteInto) {
        super(recorderToWriteInto);
        Objects.requireNonNull(windowDataMetric, "Given metric must not be null.");
        // get the expected metric (based on id equality)
        Optional<MetricDescription> foundMetric = EXPECTED_WINDOW_DATA_METRICS.stream()
                .filter(m -> MetricDescriptionUtility.metricDescriptionIdsEqual(m, windowDataMetric)).findAny();

        this.windowDataMetric = foundMetric.orElseThrow(() -> new IllegalArgumentException(
                "This aggregator cannot deal with window data of the given metric."));
        this.stateOfResourceMetric = EXPECTED_WINDOW_METRICS_MAP.get(this.windowDataMetric);
    }

    @Override
    protected MeasuringValue processWindowData(final Iterable<MeasuringValue> windowData,
            final Measure<Double, Duration> windowLeftBound, final Measure<Double, Duration> windowLength) {

        Amount<Duration> windowLeftBoundAmount = Amount.valueOf(windowLeftBound.getValue(), windowLeftBound.getUnit());
        Amount<Duration> windowLengthAmount = Amount.valueOf(windowLength.getValue(), windowLength.getUnit());
        Amount<Duration> windowRightBoundAmount = windowLengthAmount.plus(windowLeftBoundAmount);

        MeasuringValue result = null;
        Amount<Duration> busyTime = ZERO_DURATION;
        Iterator<MeasuringValue> iterator = windowData.iterator();

        if (iterator.hasNext()) {
            MeasuringValue currentMeasurement;
            double currentStateValue;
            Amount<Duration> currentPointInTimeAmount;
            MeasuringValue nextMeasurement = null;
            Amount<Duration> nextPointInTimeAmount = null;

            currentMeasurement = iterator.next(); // not null, as windowData not
                                                  // empty!
            currentStateValue = obtainStateValueFromMeasurement(currentMeasurement);
            currentPointInTimeAmount = obtainPointInTimeAmountFromMeasurement(currentMeasurement);
            boolean endLoop = false;

            do {
                // special treatment for amount that is out of window bounds:
                // consider only parts inside window
                currentPointInTimeAmount = currentPointInTimeAmount.isLessThan(windowLeftBoundAmount)
                        ? windowLeftBoundAmount : currentPointInTimeAmount;

                if (iterator.hasNext()) {
                    nextMeasurement = iterator.next();
                    nextPointInTimeAmount = obtainPointInTimeAmountFromMeasurement(nextMeasurement);
                } else {
                    // nextPointInTimeAmount is now the window's upper bound
                    nextPointInTimeAmount = windowRightBoundAmount;
                    endLoop = true; // no further elements available
                }
                // mac operation
                busyTime = busyTime.plus(
                        nextPointInTimeAmount.minus(currentPointInTimeAmount).times(Math.min(currentStateValue, 1d)));

                if (!endLoop) {
                    currentMeasurement = nextMeasurement;
                    currentStateValue = obtainStateValueFromMeasurement(nextMeasurement);
                    currentPointInTimeAmount = nextPointInTimeAmount;
                }

            } while (!endLoop);
        }
        result = createUtilizationTupleMeasurement(busyTime, windowLengthAmount, windowRightBoundAmount);
        return result;
    }

    /**
     * Creates a {@link Measurement} containing the {@code utilization of active resource} captured
     * at the given {@code point in time}.
     * 
     * @param busyTime
     *            An {@link Amount} indicating the busy time with the last sliding window period.
     * @param windowLength
     *            An {@link Amount} indicating the length of the sliding window.
     * @param pointInTime
     *            An {@link Amount} indicating the {@code point in time} this measurement is
     *            captured.
     * @return A {@link MeasuringValue} denoting the utilization {@code U} which is calculated as
     *         follows: {@code U = busyTime / windowLength}.
     */
    private static MeasuringValue createUtilizationTupleMeasurement(final Amount<Duration> busyTime,
            final Amount<Duration> windowLength, final Amount<Duration> pointInTime) {

        assert windowLength.isGreaterThan(ZERO_DURATION);

        @SuppressWarnings("unchecked")
        Amount<Dimensionless> utilization = (Amount<Dimensionless>) busyTime.divide(windowLength);
        Measure<Double, Dimensionless> resultUtilizationMeasure = Measure.valueOf(utilization.doubleValue(Unit.ONE),
                Unit.ONE);
        Measure<Double, Duration> resultPointInTimeMeasure = Measure
                .valueOf(pointInTime.doubleValue(pointInTime.getUnit()), pointInTime.getUnit());
        return new TupleMeasurement(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE,
                resultPointInTimeMeasure, resultUtilizationMeasure);
    }

    /**
     * Gets the {@code point in time} the given measurement was taken.
     * 
     * @param measurement
     *            A ({@code not null}) {@link MeasuringValue} instance containing a
     *            {@code point in time} measure.
     * @return An {@link Amount} that represents the {@code point in time} the given measurement was
     *         taken.
     */
    protected static Amount<Duration> obtainPointInTimeAmountFromMeasurement(final MeasuringValue measurement) {
        Measure<?, Duration> measure = measurement.getMeasureForMetric(MetricDescriptionConstants.POINT_IN_TIME_METRIC);
        return Amount.valueOf(measure.doubleValue(measure.getUnit()), measure.getUnit());
    }

    /**
     * Gets the {@code state of active resource} measurement captured by the given measuring value.
     * 
     * @param measurement
     *            A ({@code not null}) {@link MeasuringValue} instance containing a dimensionless
     *            {@code state of active resource} measurement, or, in case of multi-core
     *            utilization, a {@code utilization of active resource} measurement.
     * @return A {@link Dimensionless} {@link Amount} denoting the {@code state of active resource}
     *         .
     */
    protected Amount<Dimensionless> obtainStateAmountFromMeasurement(final MeasuringValue measurement) {
        assert measurement != null && measurement.isCompatibleWith(this.windowDataMetric);

        Measure<?, Dimensionless> measure = measurement.getMeasureForMetric(this.stateOfResourceMetric);

        return Amount.valueOf(measure.doubleValue(measure.getUnit()), measure.getUnit());
    }

    /**
     * Gets the {@code state of active resource} value captured by the given measurement.
     * 
     * @param measurement
     *            A ({@code not null}) {@link MeasuringValue} instance containing a dimensionless
     *            {@code state of active resource} measurement, or, in case of multi-core
     *            utilization, a {@code utilization of active resource} measurement.
     * @return A <b>nonnegative</b> double denoting the {@code state of active resource} value.
     */
    private double obtainStateValueFromMeasurement(final MeasuringValue measurement) {
        assert measurement != null && measurement.isCompatibleWith(this.windowDataMetric);

        Measure<?, Dimensionless> measure = measurement.getMeasureForMetric(this.stateOfResourceMetric);
        return measure.doubleValue(Dimensionless.UNIT);
    }

    /**
     * Returns the {@link MetricDescription} all window data must adhere to in order to be processed
     * by this aggregator.
     * 
     * @return The {@link MetricDescription} of the data this instance is processing.
     */
    @Override
    public MetricDescription getExpectedWindowDataMetric() {
        return this.windowDataMetric;

    }

    /**
     * Gets all data metrics that can be processed by instances of this class.
     * 
     * @return An <b>unmodifiable</b> {@link Collection} of those {@link MetricDescription}s which
     *         can be processed.
     */
    public static Collection<MetricDescription> getAllowedWindowDataMetrics() {
        return Collections.unmodifiableCollection(EXPECTED_WINDOW_DATA_METRICS);
    }
}
