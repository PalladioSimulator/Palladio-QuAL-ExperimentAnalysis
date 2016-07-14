package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.NumericalBaseMetricDescription;

/**
 * {@link SlidingWindowAggregator} which computes the geometric mean of a sequence of measurements:
 * <br>
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

    @Override
    protected Measure<Double, Quantity> calculateStatisticalCharaterizationDiscrete(
            Iterable<MeasuringValue> windowData) {
        // harmonic mean is not defined in case that any of the elements equals
        // zero, this implementation will then return 0
        double arithmeticMeanOfInverses = StreamSupport.stream(windowData.spliterator(), false).collect(
                Collectors.averagingDouble(measurement -> 1d / this.obtainDataValueFromMeasurement(measurement)));

        return Measure.valueOf(arithmeticMeanOfInverses == Double.NaN || arithmeticMeanOfInverses == 0 ? 0
                : 1 / arithmeticMeanOfInverses, super.getDataDefaultUnit());

    }

    @Override
    protected Measure<Double, Quantity> calculateStatisticalCharacterizationContinuous(
            Iterable<MeasuringValue> windowData) {

        Measure<Double, Quantity> harmonicMean = Measure.valueOf(0d, super.getDataDefaultUnit());
        Iterator<MeasuringValue> iterator = windowData.iterator();

        if (iterator.hasNext()) {
            Amount<? extends Quantity> area = Amount.valueOf(0d, Duration.UNIT.divide(super.getDataDefaultUnit()));
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
                area = area.plus(obtainCurrentMeasurementValidityScope(currentMeasurement, nextMeasurement)
                        .divide(obtainDataFromMeasurement(currentMeasurement)));

                if (nextMeasurement.isPresent()) {
                    currentMeasurement = nextMeasurement.get();
                }
            } while (nextMeasurement.isPresent());
            @SuppressWarnings("unchecked")
            Amount<Quantity> harmonicMeanAmount = (Amount<Quantity>) super.getIntervalLength().divide(area);

            harmonicMean = Measure.valueOf(harmonicMeanAmount.doubleValue(super.getDataDefaultUnit()),
                    super.getDataDefaultUnit());
        }
        return harmonicMean;
    }
}
