package org.palladiosimulator.experimentanalysis.windowaggregators;

import java.util.Objects;

import jakarta.measure.Measurable;
import jakarta.measure.Measure;
import jakarta.measure.quantity.Dimensionless;
import jakarta.measure.quantity.Duration;
import jakarta.measure.unit.SI;
import jakarta.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.palladiosimulator.experimentanalysis.SlidingWindow;
import org.palladiosimulator.experimentanalysis.SlidingWindowRecorder;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.measurementframework.TupleMeasurement;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.recorderframework.IRecorder;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

/**
 * This class is a specialized {@link SlidingWindowUtilizationAggregator} implementation which
 * calculates the utilization or, (which is more precisely in the scope of this class), the average
 * load) of an active resource based on a sequence of
 * {@code (point in time, state of active resource)} or
 * {@code (point in time, utilization of active resource)} tuples collected by a
 * {@link SlidingWindow} by employing the algorithm that is used in the Linux kernel to compute the
 * average load (Details of this algorithm can be found
 * <a href="http://www.teamquest.com/import/pdfs/whitepaper/ldavg1.pdf">in this paper</a>, or more
 * succinctly, <a href="http://www.perfdynamics.com/CMG/CMGslides4up.pdf">in this presentation</a>
 * both due to Neil Gunther). <br>
 * The calculated load is passed to all attached {@link IRecorder}s in the form of a
 * {@code (point in time, utilization of active resource)} tuple each time this implementation
 * processes new window data. Such a point in time is referred to as <em>reporting period</em> and
 * equal to the length of the corresponding {@link SlidingWindow}. <br>
 * <br>
 * Note: The afore-mentioned algorithm is a special case of <em>exponential smoothing</em> with the
 * smoothing factor <code>&alpha;</code> derived from the given sampling rate and reporting period.
 * 
 * @see SlidingWindowRecorder
 * @see #getAllowedWindowDataMetrics()
 * 
 * @author Florian Rosenthal
 */
// http://www.perfdynamics.com/CMG/CMGslides4up.pdf (Linux Load Average, function CALC_LOAD)
// http://www.teamquest.com/import/pdfs/whitepaper/ldavg1.pdf
public final class SlidingWindowExponentialSmoothingUtilizationAggregator extends SlidingWindowUtilizationAggregator {

    private static final Amount<Dimensionless> ZERO_LOAD = Amount.ZERO;
    private Amount<Dimensionless> currentLoadAverage = ZERO_LOAD; // usually not bounded on [0,1]
    private final double smoothingFactor; // usually denoted alpha
    private final double oneMinusSmoothingFactor; // 1 - alpha, store for convenience
    private final Amount<Duration> samplingRate; // use seconds internally

    /**
     * Initializes a new instance of the
     * {@link SlidingWindowExponentialSmoothingUtilizationAggregator} class with the given
     * arguments.
     * 
     * @param windowDataMetric
     *            The {@link MetricDescription} of the measurements to be processed, i.e, which are
     *            used to compute the utilization.
     * @param recorderToWriteInto
     *            An {@link IRecorder} this instance writes the aggregated window data into.
     *            Typically, a recorder that writes into a persistence framework like EDP 2 is
     *            passed here.
     * @param samplingRate
     *            An {@link Amount} denoting the sampling rate which equals the increment of the
     *            associated {@link SlidingWindow}.<br>
     *            For instance, if the given amount is 5s, the average load/utilization is updated
     *            every 5 seconds, starting at the window's left bound.
     * @param reportingPeriod
     *            An {@link Amount} describing the reporting period which is simply the length of
     *            the associated {@link SlidingWindow}.<br>
     *            For instance, if the given amount is 1min, the computed average load/utilization
     *            is forwarded every minute to the attached recorders.<br>
     * 
     * @throws NullPointerException
     *             If any of the arguments is {@code null}.
     * @throws IllegalArgumentException
     *             If the given metric is not supported by this aggregator, or either of the given
     *             amounts is not positive.
     * 
     */
    public SlidingWindowExponentialSmoothingUtilizationAggregator(final MetricDescription windowDataMetric,
            final IRecorder recorderToWriteInto, final Measurable<Duration> samplingRate,
            final Measurable<Duration> reportingPeriod) {
        super(windowDataMetric, recorderToWriteInto);
        // throws exception in case of failure
        checkDurationMeasurables(samplingRate, reportingPeriod);

        this.samplingRate = Amount.valueOf(samplingRate.doubleValue(SI.SECOND), SI.SECOND);
        this.smoothingFactor = computeSmoothingFactor(samplingRate.doubleValue(SI.SECOND),
                reportingPeriod.doubleValue(SI.SECOND));
        this.oneMinusSmoothingFactor = 1 - this.smoothingFactor;
    }

    private static void checkDurationMeasurables(final Measurable<Duration> samplingRate,
            final Measurable<Duration> reportingPeriod) {
        if (ZERO_DURATION.compareTo(Objects.requireNonNull(samplingRate)) >= 0) {
            throw new IllegalArgumentException("Passed sampling rate must be positive!");
        }
        if (ZERO_DURATION.compareTo(Objects.requireNonNull(reportingPeriod)) >= 0) {
            throw new IllegalArgumentException("Passed reporting period must be positive!");
        }
    }

    private static double computeSmoothingFactor(final double samplingRate, final double reportingPeriod) {
        // both double values are assumed to be given in seconds
        return -Math.expm1(-samplingRate / reportingPeriod);
        // smoothing factor alpha is given by 1-e^(-x) = -(e^(-x)-1)
    }

    private void updateLoadAverage(final Amount<Dimensionless> stateAmount) {
        this.currentLoadAverage = this.currentLoadAverage.times(this.oneMinusSmoothingFactor)
                .plus(stateAmount.times(this.smoothingFactor));
    }

    @Override
    protected MeasuringValue processWindowData(final Iterable<MeasuringValue> windowData,
            final Measure<Double, Duration> windowLeftBound, final Measure<Double, Duration> windowLength) {

        Unit<Duration> usedDurationUnit = windowLeftBound.getUnit();
        Amount<Duration> windowLeftBoundAmount = Amount.valueOf(windowLeftBound.getValue(), usedDurationUnit);

        Amount<Duration> currentSamplingPoint = windowLeftBoundAmount;
        PeekingIterator<MeasuringValue> iterator = Iterators.peekingIterator(windowData.iterator());

        while (iterator.hasNext()) {
            MeasuringValue current = iterator.next();
            Amount<Duration> currentPointInTime = obtainPointInTimeAmountFromMeasurement(current);
            Amount<Dimensionless> currentState = obtainStateAmountFromMeasurement(current);

            while (currentPointInTime.isGreaterThan(currentSamplingPoint)) {
                currentSamplingPoint = currentSamplingPoint.plus(this.samplingRate);
            }
            if (iterator.hasNext()) {
                Amount<Duration> peekedNextPointInTime = obtainPointInTimeAmountFromMeasurement(iterator.peek());

                while (peekedNextPointInTime.isGreaterThan(currentSamplingPoint)) {
                    // incorporate current state of resource into the computation
                    assert !currentPointInTime.isGreaterThan(currentSamplingPoint);
                    updateLoadAverage(currentState);
                    currentSamplingPoint = currentSamplingPoint.plus(this.samplingRate);
                }
            }
            // last measurement is only processed if not "out of bounds"
            else if (!currentPointInTime.isGreaterThan(currentSamplingPoint)) {
                updateLoadAverage(currentState);
            }
        }
        return new TupleMeasurement(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE, Measure
                .valueOf(windowLeftBound.getValue() + windowLength.doubleValue(usedDurationUnit), usedDurationUnit),
                Measure.valueOf(this.currentLoadAverage.doubleValue(Unit.ONE), Unit.ONE));

    }

    public Measurable<Duration> getSamplingRate() {
        return this.samplingRate;
    }

    public double getSmoothingFactor() {
        return this.smoothingFactor;
    }
}
