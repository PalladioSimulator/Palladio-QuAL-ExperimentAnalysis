package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.tests;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.SlidingWindowStatisticalCharacterizationAggregator;
import org.palladiosimulator.monitorrepository.statisticalcharacterization.ArithmeticMeanAggregator;
import org.palladiosimulator.monitorrepository.statisticalcharacterization.StatisticalCharacterizationAggregator;

public class ArithmeticMeanAggregatorTest extends StatisticalCharacterizationAggregatorTest {

    @Override
    protected SlidingWindowStatisticalCharacterizationAggregator getAggregatorUnderTest() {
        StatisticalCharacterizationAggregator aggregator = new ArithmeticMeanAggregator(RESULT_METRIC);
        return new SlidingWindowStatisticalCharacterizationAggregator(this.dummyRecorder, aggregator);
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
