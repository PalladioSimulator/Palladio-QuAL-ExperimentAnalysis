package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Collection;
import java.util.Objects;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.BasicMeasurement;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;
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

    @Override
    public final MetricDescription getExpectedWindowDataMetric() {
        return this.dataMetric;
    }

    @Override
    protected final MeasuringValue processWindowData(Iterable<MeasuringValue> windowData,
            Measure<Double, Duration> windowLeftBound, Measure<Double, Duration> windowLength) {

        return new BasicMeasurement<Double, Quantity>(calculateStatisticalCharaterization(windowData), this.dataMetric);
    }

    protected abstract Measure<Double, Quantity> calculateStatisticalCharaterization(
            Iterable<MeasuringValue> windowData);
}
