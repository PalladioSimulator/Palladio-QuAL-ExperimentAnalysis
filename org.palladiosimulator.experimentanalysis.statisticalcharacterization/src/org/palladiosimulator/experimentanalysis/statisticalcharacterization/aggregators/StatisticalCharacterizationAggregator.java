package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.BasicMeasurement;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;
import org.palladiosimulator.metricspec.ScopeOfValidity;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.recorderframework.IRecorder;

/**
 * Specialization of the {@link SlidingWindowAggregator} which is devoted to aggregate the
 * measurements collected by a sliding window in a statistical manner. That is, subclasses of this
 * class shall calculate some statistical measure/characteristic variable.
 * 
 * @author Florian Rosenthal
 *
 */
public abstract class StatisticalCharacterizationAggregator extends SlidingWindowAggregator {

    protected final NumericalBaseMetricDescription dataMetric;
    protected final Unit<Quantity> dataDefaultUnit;

    protected Amount<Duration> windowLeftBound;
    protected Amount<Duration> windowLength;
    protected Amount<Duration> windowRightBound;

    /**
     * Initializes a new instance of the {@link StatisticalCharacterizationAggregator} class with
     * the given parameter.
     * 
     * @param expectedWindowMetric
     *            The {@link NumericalBaseMetricDescription} that describes which kind of
     *            measurements are expected to aggregate.
     * @throws NullPointerException
     *             In case {@code expectedWindowMetric == null}.
     */
    public StatisticalCharacterizationAggregator(NumericalBaseMetricDescription expectedWindowMetric) {
        super();
        this.dataMetric = Objects.requireNonNull(expectedWindowMetric);
        this.dataDefaultUnit = dataMetric.getDefaultUnit();
    }

    /**
     * Initializes a new instance of the {@link StatisticalCharacterizationAggregator} class with
     * the given parameters.
     * 
     * @param recorderToWriteInto
     *            An {@link IRecorder} implementation to write the aggregated data into.
     * @param expectedWindowMetric
     *            The {@link NumericalBaseMetricDescription} that describes which kind of
     *            measurements are expected to aggregate.
     * @throws NullPointerException
     *             In case {@code expectedWindowMetric == null}.
     */
    public StatisticalCharacterizationAggregator(IRecorder recorderToWriteInto,
            NumericalBaseMetricDescription expectedWindowMetric) {
        super(recorderToWriteInto);

        this.dataMetric = Objects.requireNonNull(expectedWindowMetric);
        this.dataDefaultUnit = dataMetric.getDefaultUnit();
    }

    /**
     * Initializes a new instance of the {@link StatisticalCharacterizationAggregator} class with
     * the given parameters.
     * 
     * @param recordersToWriteInto
     *            A Collection of {@link IRecorder} implementations to write the aggregated data
     *            into.
     * @param expectedWindowMetric
     *            The {@link MetricSetDescription} that describes which kind of measurements are
     *            expected to aggregate.
     * @throws NullPointerException
     *             In case {@code expectedWindowMetric == null}.
     */
    public StatisticalCharacterizationAggregator(Collection<IRecorder> recordersToWriteInto,
            NumericalBaseMetricDescription expectedWindowMetric) {
        super(recordersToWriteInto);
        this.dataMetric = Objects.requireNonNull(expectedWindowMetric);
        this.dataDefaultUnit = dataMetric.getDefaultUnit();
    }

    private Measure<Double, Quantity> obtainDataFromMeasurementAsMeasure(MeasuringValue measurement) {
        return measurement.getMeasureForMetric(this.dataMetric);
    }

    /**
     * @return A {@link NumericalBaseMetricDescription} indicating the type of measurements this
     *         aggregator can process.
     */
    @Override
    public final NumericalBaseMetricDescription getExpectedWindowDataMetric() {
        return this.dataMetric;
    }

    @Override
    protected final MeasuringValue processWindowData(Iterable<MeasuringValue> windowData,
            Measure<Double, Duration> windowLeftBound, Measure<Double, Duration> windowLength) {

        this.windowLeftBound = Amount.valueOf(windowLeftBound.getValue(), windowLeftBound.getUnit());
        this.windowLength = Amount.valueOf(windowLength.getValue(), windowLength.getUnit());
        this.windowRightBound = this.windowLeftBound.plus(this.windowLength);

        Measure<Double, Quantity> result = null;
        switch (this.dataMetric.getScopeOfValidity()) {
        case CONTINUOUS:
            result = calculateStatisticalCharacterizationContinuous(windowData);
            break;
        case DISCRETE:
            result = calculateStatisticalCharaterizationDiscrete(windowData);
            break;
        default:
            throw new AssertionError();
        }
        return new BasicMeasurement<Double, Quantity>(result, this.dataMetric);
    }

    protected abstract Measure<Double, Quantity> calculateStatisticalCharaterizationDiscrete(
            Iterable<MeasuringValue> windowData);

    protected abstract Measure<Double, Quantity> calculateStatisticalCharacterizationContinuous(
            Iterable<MeasuringValue> windowData);

    /**
     * Gets the numeric value of desired data (i.e., the data that adheres to the expected window
     * data metric) from the given measurement.
     * 
     * @param measurement
     *            A {@link MeasuringValue} containing window data.
     * @return The desired data value, expressed in terms of a double value in the default unit of
     *         the corresponding metric.
     * 
     * @see #getExpectedWindowDataMetric()
     * @see NumericalBaseMetricDescription#getDefaultUnit()
     * @see #obtainDataFromMeasurement(MeasuringValue)
     */
    protected final double obtainDataValueFromMeasurement(MeasuringValue measurement) {
        return obtainDataFromMeasurementAsMeasure(measurement).doubleValue(this.dataDefaultUnit);
    }

    /**
     * Gets the desired data (i.e., the data that adheres to the expected window data metric) from
     * the given measurement.
     * 
     * @param measurement
     *            A {@link MeasuringValue} containing window data.
     * @return The desired data, expressed in terms of an {@link Amount}.
     * 
     * @see #getExpectedWindowDataMetric()
     * @see #obtainDataValueFromMeasurement(MeasuringValue)
     */
    protected final Amount<Quantity> obtainDataFromMeasurement(MeasuringValue measurement) {
        Measure<Double, Quantity> measure = obtainDataFromMeasurementAsMeasure(measurement);
        return Amount.valueOf(measure.getValue(), measure.getUnit());
    }

    protected final Amount<Duration> obtainCurrentMeasurementValidityScope(MeasuringValue currentMeasurement,
            Optional<MeasuringValue> nextMeasurement) {
        if (this.dataMetric.getScopeOfValidity() != ScopeOfValidity.CONTINUOUS) {
            throw new IllegalStateException();
        }
        Amount<Duration> current = Amount.valueOf(currentMeasurement
                .<Double, Duration> getMeasureForMetric(MetricDescriptionConstants.POINT_IN_TIME_METRIC)
                .doubleValue(SI.SECOND), SI.SECOND);
        Amount<Duration> next = nextMeasurement
                .map(m -> Amount
                        .valueOf(
                                m.<Double, Duration> getMeasureForMetric(
                                        MetricDescriptionConstants.POINT_IN_TIME_METRIC).doubleValue(SI.SECOND),
                                SI.SECOND))
                .orElse(this.windowRightBound);
        // special treatment for amount that is out of window bounds:
        // consider only parts inside window
        return next.minus(current.isLessThan(this.windowLeftBound) ? this.windowLeftBound : current);
    }
}
