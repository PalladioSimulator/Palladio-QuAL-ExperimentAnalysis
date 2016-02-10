package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Arrays;
import java.util.Collection;
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

public abstract class StatisticalCharacterizationAggregator extends SlidingWindowAggregator {

    private static final NumericalBaseMetricDescription POINT_IN_TIME_METRIC = (NumericalBaseMetricDescription) MetricDescriptionConstants.POINT_IN_TIME_METRIC;

    private final MetricSetDescription expectedWindowDataMetric;
    protected final NumericalBaseMetricDescription dataMetric;
    protected final Unit<Quantity> dataDefaultUnit;

    public StatisticalCharacterizationAggregator(MetricSetDescription expectedWindowMetric) {
        super();
        this.expectedWindowDataMetric = Objects.requireNonNull(expectedWindowMetric,
                "Given metric set description must not be null!");
        this.dataMetric = getDataMetric();
        this.dataDefaultUnit = dataMetric.getDefaultUnit();
    }

    public StatisticalCharacterizationAggregator(IRecorder recorderToWriteInto,
            MetricSetDescription expectedWindowMetric) {
        super(recorderToWriteInto);

        this.expectedWindowDataMetric = Objects.requireNonNull(expectedWindowMetric,
                "Given metric set description must not be null!");
        this.dataMetric = getDataMetric();
        this.dataDefaultUnit = dataMetric.getDefaultUnit();
    }

    public StatisticalCharacterizationAggregator(Collection<IRecorder> recordersToWriteInto,
            MetricSetDescription expectedWindowMetric) {
        super(recordersToWriteInto);
        this.expectedWindowDataMetric = Objects.requireNonNull(expectedWindowMetric,
                "Given metric description must not be null!");
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
}
