package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Objects;
import java.util.Optional;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.slidingwindow.SlidingWindowStatisticalCharacterizationAggregator;
import org.palladiosimulator.measurementframework.BasicMeasurement;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;
import org.palladiosimulator.metricspec.ScopeOfValidity;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

public abstract class StatisticalCharacterizationAggregator {

    private final NumericalBaseMetricDescription dataMetric;
    private final Unit<Quantity> dataDefaultUnit;

    private Amount<Duration> intervalLeftBound;
    private Amount<Duration> intervalLength;
    private Amount<Duration> intervalRightBound;

    /**
     * Initializes a new instance of the {@link SlidingWindowStatisticalCharacterizationAggregator}
     * class with the given parameter.
     * 
     * @param expectedDataMetric
     *            The {@link NumericalBaseMetricDescription} that describes which kind of
     *            measurements are expected to aggregate.
     * @throws NullPointerException
     *             In case {@code expectedWindowMetric == null}.
     */
    public StatisticalCharacterizationAggregator(NumericalBaseMetricDescription expectedDataMetric) {
        super();
        this.dataMetric = Objects.requireNonNull(expectedDataMetric);
        this.dataDefaultUnit = dataMetric.getDefaultUnit();
    }

    public NumericalBaseMetricDescription getDataMetric() {
        return this.dataMetric;
    }

    public Unit<Quantity> getDataDefaultUnit() {
        return this.dataDefaultUnit;
    }

    public final MeasuringValue aggregateData(Iterable<MeasuringValue> data, Amount<Duration> intervalLeftBound,
            Amount<Duration> intervalRightBound, Optional<Amount<Duration>> intervalLength) {

        Objects.requireNonNull(data);

        this.intervalLeftBound = Objects.requireNonNull(intervalLeftBound);
        this.intervalRightBound = Objects.requireNonNull(intervalRightBound);
        this.intervalLength = Objects.requireNonNull(intervalLength)
                .orElse(this.intervalRightBound.minus(this.intervalLeftBound));

        Measure<Double, Quantity> result = null;
        switch (this.dataMetric.getScopeOfValidity()) {
        case CONTINUOUS:
            result = calculateStatisticalCharacterizationContinuous(data);
            break;
        case DISCRETE:
            result = calculateStatisticalCharaterizationDiscrete(data);
            break;
        default:
            throw new AssertionError();
        }
        return new BasicMeasurement<Double, Quantity>(result, this.dataMetric);
    }

    private Measure<Double, Quantity> obtainDataFromMeasurementAsMeasure(MeasuringValue measurement) {
        return measurement.getMeasureForMetric(this.dataMetric);
    }

    protected final Amount<Duration> getIntervalLength() {
        return this.intervalLength;
    }

    /**
     * Gets the desired data (i.e., the data that adheres to the expected data metric) from the
     * given measurement.
     * 
     * @param measurement
     *            A {@link MeasuringValue} containing the data.
     * @return The desired data, expressed in terms of an {@link Amount}.
     * 
     * @see #obtainDataValueFromMeasurement(MeasuringValue)
     * @see NumericalBaseMetricDescription#getDefaultUnit()
     */
    protected final Amount<Quantity> obtainDataFromMeasurement(MeasuringValue measurement) {
        Measure<Double, Quantity> measure = obtainDataFromMeasurementAsMeasure(measurement);
        return Amount.valueOf(measure.getValue(), measure.getUnit());
    }

    /**
     * Gets the numeric value of desired data (i.e., the data that adheres to the expected data
     * metric) from the given measurement.
     * 
     * @param measurement
     *            A {@link MeasuringValue} containing the data.
     * @return The desired data value, expressed in terms of a double value in the default unit of
     *         the corresponding metric.
     * 
     * @see NumericalBaseMetricDescription#getDefaultUnit()
     * @see #obtainDataFromMeasurement(MeasuringValue)
     */
    protected final double obtainDataValueFromMeasurement(MeasuringValue measurement) {
        return obtainDataFromMeasurementAsMeasure(measurement).doubleValue(this.dataDefaultUnit);
    }

    protected abstract Measure<Double, Quantity> calculateStatisticalCharaterizationDiscrete(
            Iterable<MeasuringValue> dataToAggregate);

    protected abstract Measure<Double, Quantity> calculateStatisticalCharacterizationContinuous(
            Iterable<MeasuringValue> dataToAggregate);

    protected final Amount<Duration> obtainCurrentMeasurementValidityScope(MeasuringValue currentMeasurement,
            Optional<MeasuringValue> nextMeasurement) {
        if (this.dataMetric.getScopeOfValidity() != ScopeOfValidity.CONTINUOUS) {
            throw new IllegalStateException("Method is only reasonable for metrics with continuous scope of validity!");
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
                .orElse(this.intervalRightBound);
        // special treatment for amount that is out of interval bounds:
        // consider only parts inside interval
        return next.minus(current.isLessThan(this.intervalLeftBound) ? this.intervalLeftBound : current);
    }
}
