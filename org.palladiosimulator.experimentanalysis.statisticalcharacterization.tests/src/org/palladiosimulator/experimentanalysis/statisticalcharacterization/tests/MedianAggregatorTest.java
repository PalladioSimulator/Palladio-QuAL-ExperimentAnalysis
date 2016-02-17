package org.palladiosimulator.experimentanalysis.statisticalcharacterization.tests;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.MedianAggregator;
import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.StatisticalCharacterizationAggregator;

public class MedianAggregatorTest extends StatisticalCharacterizationAggregatorTest {

    @Override
    protected StatisticalCharacterizationAggregator getAggregatorUnderTest() {
        return new MedianAggregator(this.dummyRecorder, RESULT_METRIC);
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
