package org.palladiosimulator.experimentanalysis.statisticalcharacterization.tests;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.ArithmeticMeanAggregator;
import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.StatisticalCharacterizationAggregator;

public class ArithmeticMeanAggregatorTest extends StatisticalCharacterizationAggregatorTest {

    @Override
    protected StatisticalCharacterizationAggregator getAggregatorUnderTest() {
        return new ArithmeticMeanAggregator(this.dummyRecorder, RESULT_METRIC);
    }

    @Override
    protected Measure<Double, Duration> getExpectedAggregatedResponseTimeFirstTest() {
        return Measure.valueOf(452.025, SI.SECOND);
    }

    @Override
    protected Measure<Double, Duration> getExpectedAggregatedResponseTimeSecondTest() {
        return Measure.valueOf(362.72, SI.SECOND);
    }

}
