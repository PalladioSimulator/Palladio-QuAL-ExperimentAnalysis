package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;
import org.palladiosimulator.monitorrepository.statisticalcharacterization.StatisticalCharacterizationAggregator;
import org.palladiosimulator.recorderframework.IRecorder;

/**
 * Specialization of the {@link SlidingWindowAggregator} which is devoted to aggregate the
 * measurements collected by a sliding window in a statistical manner. That is, subclasses of this
 * class shall calculate some statistical measure/characteristic variable.
 * 
 * @author Florian Rosenthal
 *
 */
public class SlidingWindowStatisticalCharacterizationAggregator extends SlidingWindowAggregator {

    private final StatisticalCharacterizationAggregator aggregator;

    /**
     * Initializes a new instance of the {@link SlidingWindowStatisticalCharacterizationAggregator}
     * class with the given parameter.
     * 
     * @param expectedWindowMetric
     *            The {@link NumericalBaseMetricDescription} that describes which kind of
     *            measurements are expected to aggregate.
     * @throws NullPointerException
     *             In case {@code expectedWindowMetric == null}.
     */
    public SlidingWindowStatisticalCharacterizationAggregator(StatisticalCharacterizationAggregator aggregator) {
        this.aggregator = Objects.requireNonNull(aggregator);
    }

    /**
     * Initializes a new instance of the {@link SlidingWindowStatisticalCharacterizationAggregator}
     * class with the given parameters.
     * 
     * @param recorderToWriteInto
     *            An {@link IRecorder} implementation to write the aggregated data into.
     * @param expectedWindowMetric
     *            The {@link NumericalBaseMetricDescription} that describes which kind of
     *            measurements are expected to aggregate.
     * @throws NullPointerException
     *             In case {@code expectedWindowMetric == null}.
     */
    public SlidingWindowStatisticalCharacterizationAggregator(IRecorder recorderToWriteInto,
            StatisticalCharacterizationAggregator aggregator) {
        super(recorderToWriteInto);

        this.aggregator = Objects.requireNonNull(aggregator);
    }

    /**
     * Initializes a new instance of the {@link SlidingWindowStatisticalCharacterizationAggregator}
     * class with the given parameters.
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
    public SlidingWindowStatisticalCharacterizationAggregator(Collection<IRecorder> recordersToWriteInto,
            StatisticalCharacterizationAggregator aggregator) {
        super(recordersToWriteInto);
        this.aggregator = Objects.requireNonNull(aggregator);
    }

    /**
     * @return A {@link NumericalBaseMetricDescription} indicating the type of measurements this
     *         aggregator can process.
     */
    @Override
    public final NumericalBaseMetricDescription getExpectedWindowDataMetric() {
        return this.aggregator.getDataMetric();
    }

    @Override
    protected final MeasuringValue processWindowData(Iterable<MeasuringValue> windowData,
            Measure<Double, Duration> windowLeftBound, Measure<Double, Duration> windowLength) {

        Amount<Duration> leftBound = Amount.valueOf(windowLeftBound.getValue(), windowLeftBound.getUnit());
        Amount<Duration> length = Amount.valueOf(windowLength.getValue(), windowLength.getUnit());
        Amount<Duration> rightBound = leftBound.plus(length);

        return this.aggregator.aggregateData(windowData, leftBound, rightBound, Optional.of(length));
    }
}
