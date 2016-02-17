package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Collection;
import java.util.stream.StreamSupport;

import javax.measure.Measure;

import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.recorderframework.IRecorder;

/**
 * {@link SlidingWindowAggregator} which computes the geometric mean from the measurements in the
 * window once it moves on:<br>
 * GM=(x<sub>1</sub> * x<sub>2</sub> * ... * x<sub>n</sub>) ^ 1/n
 * 
 * @author Florian Rosenthal
 *
 */
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
