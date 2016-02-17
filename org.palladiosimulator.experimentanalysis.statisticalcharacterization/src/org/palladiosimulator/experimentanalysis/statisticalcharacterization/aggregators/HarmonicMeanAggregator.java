package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;

import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;
import org.palladiosimulator.recorderframework.IRecorder;

/**
 * {@link SlidingWindowAggregator} which computes the geometric mean from the measurements in the
 * window once it moves on:<br>
 * It is equal to the reciprocal of the arithmetic mean of the reciprocals of the measurements.<br>
 * Note that it is not defined if any of the measurements is exactly 0. In such a case this
 * implementation returns 0
 * 
 * @author Florian Rosenthal
 *
 */
public class HarmonicMeanAggregator extends StatisticalCharacterizationAggregator {

    public HarmonicMeanAggregator(NumericalBaseMetricDescription expectedWindowMetric) {
        super(expectedWindowMetric);
    }

    public HarmonicMeanAggregator(IRecorder recorderToWriteInto, NumericalBaseMetricDescription expectedWindowMetric) {
        super(recorderToWriteInto, expectedWindowMetric);
    }

    public HarmonicMeanAggregator(Collection<IRecorder> recordersToWriteInto,
            NumericalBaseMetricDescription expectedWindowMetric) {
        super(recordersToWriteInto, expectedWindowMetric);
    }

    @Override
    protected Measure<Double, Quantity> calculateStatisticalCharaterization(Iterable<MeasuringValue> windowData) {
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
