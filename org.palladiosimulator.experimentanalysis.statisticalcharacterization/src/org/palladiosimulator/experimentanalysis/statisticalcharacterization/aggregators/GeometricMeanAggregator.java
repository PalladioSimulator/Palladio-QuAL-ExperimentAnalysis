package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;

import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;
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

    public GeometricMeanAggregator(NumericalBaseMetricDescription expectedWindowMetric) {
        super(expectedWindowMetric);
    }

    public GeometricMeanAggregator(IRecorder recorderToWriteInto, NumericalBaseMetricDescription expectedWindowMetric) {
        super(recorderToWriteInto, expectedWindowMetric);
    }

    public GeometricMeanAggregator(Collection<IRecorder> recordersToWriteInto,
            NumericalBaseMetricDescription expectedWindowMetric) {
        super(recordersToWriteInto, expectedWindowMetric);
    }

    @Override
    protected Measure<Double, Quantity> calculateStatisticalCharaterizationDiscrete(
            Iterable<MeasuringValue> windowData) {
        long numberOfElements = StreamSupport.stream(windowData.spliterator(), true).count();
        double geometricMean = StreamSupport.stream(windowData.spliterator(), false)
                .map(this::obtainDataValueFromMeasurement).reduce((left, right) -> left * right)
                .map(prod -> Math.pow(prod, 1d / numberOfElements)).orElse(0d);

        return Measure.valueOf(geometricMean, this.dataDefaultUnit);
    }

    @Override
    protected Measure<Double, Quantity> calculateStatisticalCharacterizationContinuous(
            Iterable<MeasuringValue> windowData) {

        double geometricMean = 0d;
        Iterator<MeasuringValue> iterator = windowData.iterator();

        if (iterator.hasNext()) {
            MeasuringValue currentMeasurement = iterator.next();

            Optional<MeasuringValue> nextMeasurement = null; // empty optional indicates
                                                             // no further elements
            do {
                if (iterator.hasNext()) {
                    nextMeasurement = Optional.of(iterator.next());
                } else {
                    nextMeasurement = Optional.empty();
                }
                // mac operation
                geometricMean += Math.log(obtainDataValueFromMeasurement(currentMeasurement))
                        * obtainCurrentMeasurementValidityScope(currentMeasurement, nextMeasurement)
                                .doubleValue(SI.SECOND);

                if (nextMeasurement.isPresent()) {
                    currentMeasurement = nextMeasurement.get();
                }
            } while (!nextMeasurement.isPresent());
        }
        return Measure.valueOf(Math.exp(geometricMean / this.windowLength.doubleValue(SI.SECOND)),
                this.dataDefaultUnit);
    }

}
