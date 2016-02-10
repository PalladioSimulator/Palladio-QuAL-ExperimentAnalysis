package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import javax.measure.Measure;

import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.recorderframework.IRecorder;

public class MedianAggregator extends StatisticalCharacterizationAggregator {

    public MedianAggregator(MetricSetDescription expectedWindowMetric) {
        super(expectedWindowMetric);
    }

    public MedianAggregator(IRecorder recorderToWriteInto, MetricSetDescription expectedWindowMetric) {
        super(recorderToWriteInto, expectedWindowMetric);
    }

    public MedianAggregator(Collection<IRecorder> recordersToWriteInto, MetricSetDescription expectedWindowMetric) {
        super(recordersToWriteInto, expectedWindowMetric);
    }

    @Override
    protected Measure<?, ?> calculateStatisticalCharaterization(Iterable<MeasuringValue> windowData) {
        List<Double> data = StreamSupport.stream(windowData.spliterator(), false)
                .map(measuringValue -> measuringValue.getMeasureForMetric(this.dataMetric))
                .map(measure -> measure.doubleValue(this.dataDefaultUnit)).sorted().collect(toList());
        double median = 0d;
        if (!data.isEmpty()) {
            int middle = (int) Math.floor(data.size() / 2);
            if (data.size() % 2 == 0) {
                median = 0.5 * (data.get(middle) + data.get(middle - 1));
            } else {
                median = data.get(middle);
            }
        }
        return Measure.valueOf(median, this.dataDefaultUnit);
    }

}
