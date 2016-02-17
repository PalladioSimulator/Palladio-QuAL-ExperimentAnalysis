package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.palladiosimulator.edp2.util.MetricDescriptionUtility;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.measurementframework.TupleMeasurement;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;
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

    private static final NumericalBaseMetricDescription POINT_IN_TIME_METRIC = (NumericalBaseMetricDescription) MetricDescriptionConstants.POINT_IN_TIME_METRIC;

    private final MetricSetDescription expectedWindowDataMetric;
    protected final NumericalBaseMetricDescription dataMetric;
    protected final Unit<Quantity> dataDefaultUnit;

    /**
     * Initializes a new instance of the {@link StatisticalCharacterizationAggregator} class with
     * the given parameter.
     * 
     * @param expectedWindowMetric
     *            The {@link MetricSetDescription} that describes which kind of measurements are
     *            expected to aggregate.
     * @throws NullPointerException
     *             In case {@code expectedWindowmetric == null}.
     * @throws IllegalArgumentException
     *             In case the passed metric does not exactly subsume 2 metrics or neither of them
     *             is 'point in time'.
     */
    public StatisticalCharacterizationAggregator(MetricSetDescription expectedWindowMetric) {
        super();
        this.expectedWindowDataMetric = checkMetricSetDescriptionValidity(expectedWindowMetric);
        this.dataMetric = getDataMetric();
        this.dataDefaultUnit = dataMetric.getDefaultUnit();
    }

    /**
     * Initializes a new instance of the {@link StatisticalCharacterizationAggregator} class with
     * the given parameters.
     * 
     * @param recorderToWriteInto
     *            An {@link IRecorder} implementation to write the aggregated data into.
     * @param expectedWindowMetric
     *            The {@link MetricSetDescription} that describes which kind of measurements are
     *            expected to aggregate.
     * @throws NullPointerException
     *             In case {@code expectedWindowmetric == null}.
     * @throws IllegalArgumentException
     *             In case the passed metric does not exactly subsume 2 metrics or neither of them
     *             is 'point in time'.
     */
    public StatisticalCharacterizationAggregator(IRecorder recorderToWriteInto,
            MetricSetDescription expectedWindowMetric) {
        super(recorderToWriteInto);

        this.expectedWindowDataMetric = checkMetricSetDescriptionValidity(expectedWindowMetric);
        this.dataMetric = getDataMetric();
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
     *             In case {@code expectedWindowmetric == null}.
     * @throws IllegalArgumentException
     *             In case the passed metric does not exactly subsume 2 metrics or neither of them
     *             is 'point in time'.
     */
    public StatisticalCharacterizationAggregator(Collection<IRecorder> recordersToWriteInto,
            MetricSetDescription expectedWindowMetric) {
        super(recordersToWriteInto);
        this.expectedWindowDataMetric = checkMetricSetDescriptionValidity(expectedWindowMetric);
        this.dataMetric = getDataMetric();
        this.dataDefaultUnit = dataMetric.getDefaultUnit();
    }

    private NumericalBaseMetricDescription getDataMetric() {
        return Arrays.stream(MetricDescriptionUtility.toBaseMetricDescriptions(this.expectedWindowDataMetric))
                .filter(m -> !MetricDescriptionUtility.metricDescriptionIdsEqual(m, POINT_IN_TIME_METRIC)).findAny()
                .map(m -> (NumericalBaseMetricDescription) m)
                .orElseThrow(() -> new IllegalArgumentException("Data metric could not be found."));

    }

    @Override
    public final MetricDescription getExpectedWindowDataMetric() {
        return this.expectedWindowDataMetric;
    }

    @Override
    protected final MeasuringValue processWindowData(Iterable<MeasuringValue> windowData,
            Measure<Double, Duration> windowLeftBound, Measure<Double, Duration> windowLength) {

        Unit<Duration> durationUnit = windowLeftBound.getUnit();
        Measure<Double, Duration> pointInTime = Measure.valueOf(
                windowLeftBound.doubleValue(durationUnit) + windowLength.doubleValue(durationUnit), durationUnit);

        return new TupleMeasurement(this.expectedWindowDataMetric, pointInTime,
                calculateStatisticalCharaterization(windowData));
    }

    protected abstract Measure<?, ?> calculateStatisticalCharaterization(Iterable<MeasuringValue> windowData);

    /**
     * Checks whether the given {@link MetricSetDescription} is valid: It must exactly subsume 2
     * metrics and one of them must be 'point in time'.
     * 
     * @param metricSetDescription
     * @return The passed {@link MetricSetDescription} in case of success. Otherwise, an exception
     *         is issued.
     */
    private static MetricSetDescription checkMetricSetDescriptionValidity(MetricSetDescription metricSetDescription) {
        List<MetricDescription> subsumedMetrics = Objects
                .requireNonNull(metricSetDescription, "Given metric description must not be null!")
                .getSubsumedMetrics();
        if (subsumedMetrics.size() == 2 && (MetricDescriptionUtility
                .isBaseMetricDescriptionSubsumedByMetricDescription(POINT_IN_TIME_METRIC, metricSetDescription))) {
            return metricSetDescription;
        }
        throw new IllegalArgumentException(
                "MetricSetDescription must subsume exactly 2 metrics, and either of them must be '"
                        + POINT_IN_TIME_METRIC.getName() + "'");
    }
}
