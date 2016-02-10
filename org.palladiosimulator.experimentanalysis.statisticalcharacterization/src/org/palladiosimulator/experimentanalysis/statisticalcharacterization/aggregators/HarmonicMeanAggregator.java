package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.measure.Measure;

import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.recorderframework.IRecorder;

public class HarmonicMeanAggregator extends StatisticalCharacterizationAggregator {

    public HarmonicMeanAggregator(MetricSetDescription expectedWindowMetric) {
        super(expectedWindowMetric);
    }

    public HarmonicMeanAggregator(IRecorder recorderToWriteInto, MetricSetDescription expectedWindowMetric) {
        super(recorderToWriteInto, expectedWindowMetric);
    }

    public HarmonicMeanAggregator(Collection<IRecorder> recordersToWriteInto,
            MetricSetDescription expectedWindowMetric) {
        super(recordersToWriteInto, expectedWindowMetric);
    }

    @Override
    protected Measure<?, ?> calculateStatisticalCharaterization(Iterable<MeasuringValue> windowData) {
        // harmonic mean is not defined in case that any of the elements equals zero
        // this implementation will then return 0
        DoubleSummaryStatistics inverseValuesStatistics = StreamSupport.stream(windowData.spliterator(), false)
                .map(measuringValue -> measuringValue.getMeasureForMetric(this.dataMetric))
                .collect(Collectors.summarizingDouble(measure -> 1d / measure.doubleValue(this.dataDefaultUnit)));
        long numberOfElements = inverseValuesStatistics.getCount();
        return Measure.valueOf(numberOfElements == 0 || inverseValuesStatistics.getSum() == Double.NaN ? 0
                : numberOfElements / inverseValuesStatistics.getSum(), this.dataDefaultUnit);
    }

}
