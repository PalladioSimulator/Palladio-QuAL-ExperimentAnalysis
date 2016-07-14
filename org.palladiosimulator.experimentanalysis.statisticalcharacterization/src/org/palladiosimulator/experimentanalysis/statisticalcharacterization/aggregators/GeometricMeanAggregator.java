package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;

import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;

/**
 * {@link SlidingWindowAggregator} which computes the geometric mean of a sequence of measurements:
 * <br>
 * GM=(x<sub>1</sub> * x<sub>2</sub> * ... * x<sub>n</sub>) ^ 1/n
 * 
 * @author Florian Rosenthal
 *
 */
public class GeometricMeanAggregator extends StatisticalCharacterizationAggregator {

    public GeometricMeanAggregator(NumericalBaseMetricDescription expectedWindowMetric) {
        super(expectedWindowMetric);
    }

    @Override
    protected Measure<Double, Quantity> calculateStatisticalCharaterizationDiscrete(
            Iterable<MeasuringValue> windowData) {
        double meanOfLogs = StreamSupport.stream(windowData.spliterator(), false)
                .collect(Collectors.averagingDouble(m -> Math.log(this.obtainDataValueFromMeasurement(m))));
        double geometricMean = Double.compare(meanOfLogs, 0d) == 0 ? 0d : Math.exp(meanOfLogs);

        return Measure.valueOf(geometricMean, super.getDataDefaultUnit());
    }

    @Override
    protected Measure<Double, Quantity> calculateStatisticalCharacterizationContinuous(
            Iterable<MeasuringValue> windowData) {

        Measure<Double, Quantity> geometricMean = Measure.valueOf(0d, super.getDataDefaultUnit());
        Iterator<MeasuringValue> iterator = windowData.iterator();

        if (iterator.hasNext()) {
            double sumOfLogs = 0d;
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
                sumOfLogs += Math.log(obtainDataValueFromMeasurement(currentMeasurement))
                        * obtainCurrentMeasurementValidityScope(currentMeasurement, nextMeasurement)
                                .doubleValue(SI.SECOND);

                if (nextMeasurement.isPresent()) {
                    currentMeasurement = nextMeasurement.get();
                }
            } while (nextMeasurement.isPresent());

            geometricMean = Measure.valueOf(Math.exp(sumOfLogs / super.getIntervalLength().doubleValue(SI.SECOND)),
                    super.getDataDefaultUnit());
        }
        return geometricMean;
    }

}
