package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.measure.Measure;

import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.recorderframework.IRecorder;

/**
 * {@link SlidingWindowAggregator} which computes the arithmetic mean from the measurements in the
 * window once it moves on:<br>
 * AM=(x<sub>1</sub> + x<sub>2</sub> + ... + x<sub>n</sub>) / n
 * 
 * @author Florian Rosenthal
 *
 */
public class ArithmeticMeanAggregator extends StatisticalCharacterizationAggregator {

    public ArithmeticMeanAggregator(MetricSetDescription expectedWindowMetric) {
        super(expectedWindowMetric);
    }

    public ArithmeticMeanAggregator(IRecorder recorderToWriteInto, MetricSetDescription expectedWindowMetric) {
        super(recorderToWriteInto, expectedWindowMetric);

    }

    public ArithmeticMeanAggregator(Collection<IRecorder> recordersToWriteInto,
            MetricSetDescription expectedWindowMetric) {
        super(recordersToWriteInto, expectedWindowMetric);
    }

    @Override
    protected Measure<?, ?> calculateStatisticalCharaterization(Iterable<MeasuringValue> windowData) {
        double arithmeticMean = StreamSupport.stream(windowData.spliterator(), false)
                .map(measuringValue -> measuringValue.getMeasureForMetric(this.dataMetric))
                .collect(Collectors.averagingDouble(measure -> measure.doubleValue(this.dataDefaultUnit)));

        return Measure.valueOf(arithmeticMean, this.dataDefaultUnit);
    }

}
