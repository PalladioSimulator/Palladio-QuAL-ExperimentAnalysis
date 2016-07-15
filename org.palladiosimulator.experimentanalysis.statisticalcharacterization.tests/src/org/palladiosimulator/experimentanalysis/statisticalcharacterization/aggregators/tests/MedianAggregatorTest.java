package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.tests;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.SlidingWindowStatisticalCharacterizationAggregator;
import org.palladiosimulator.monitorrepository.statisticalcharacterization.MedianAggregator;
import org.palladiosimulator.monitorrepository.statisticalcharacterization.StatisticalCharacterizationAggregator;

public class MedianAggregatorTest extends StatisticalCharacterizationAggregatorTest {

    @Override
    protected SlidingWindowStatisticalCharacterizationAggregator getAggregatorUnderTest() {
        StatisticalCharacterizationAggregator aggregator = new MedianAggregator(RESULT_METRIC);
        return new SlidingWindowStatisticalCharacterizationAggregator(this.dummyRecorder, aggregator);
    }

    @Override
    protected Measure<Double, Duration> getExpectedAggregatedResponseTimeFirstTest() {
        return Measure.valueOf(4d, SI.SECOND);
    }

    @Override
    protected Measure<Double, Duration> getExpectedAggregatedResponseTimeSecondTest() {
        return Measure.valueOf(5.5, SI.SECOND);
    }

}
