package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Collection;
import java.util.stream.StreamSupport;

import javax.measure.Measure;

import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.recorderframework.IRecorder;

public class GeometricMeanAggregator extends StatisticalCharacterizationAggregator {

    public GeometricMeanAggregator(MetricSetDescription expectedWindowMetric) {
        super(expectedWindowMetric);
    }

    public GeometricMeanAggregator(IRecorder recorderToWriteInto, MetricSetDescription expectedWindowMetric) {
        super(recorderToWriteInto, expectedWindowMetric);
    }

    public GeometricMeanAggregator(Collection<IRecorder> recordersToWriteInto,
            MetricSetDescription expectedWindowMetric) {
        super(recordersToWriteInto, expectedWindowMetric);
    }

    @Override
    protected Measure<?, ?> calculateStatisticalCharaterization(Iterable<MeasuringValue> windowData) {
        long numberOfElements = StreamSupport.stream(windowData.spliterator(), true).count();
        double geometricMean = StreamSupport.stream(windowData.spliterator(), false)
                .map(measuringValue -> measuringValue.getMeasureForMetric(this.dataMetric))
                .map(measure -> measure.doubleValue(this.dataDefaultUnit)).reduce((left, right) -> left * right)
                .map(prod -> Math.pow(prod, 1d / numberOfElements)).orElse(0d);

        return Measure.valueOf(geometricMean, this.dataDefaultUnit);
    }

}
