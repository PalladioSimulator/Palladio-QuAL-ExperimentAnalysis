package org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.tests;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.SlidingWindowStatisticalCharacterizationAggregator;
import org.palladiosimulator.monitorrepository.statisticalcharacterization.HarmonicMeanAggregator;
import org.palladiosimulator.monitorrepository.statisticalcharacterization.StatisticalCharacterizationAggregator;

public class HarmonicMeanAggregatorTest extends StatisticalCharacterizationAggregatorTest {

    @Override
    protected SlidingWindowStatisticalCharacterizationAggregator getAggregatorUnderTest() {
        StatisticalCharacterizationAggregator aggregator = new HarmonicMeanAggregator(RESULT_METRIC);
        return new SlidingWindowStatisticalCharacterizationAggregator(this.dummyRecorder, aggregator);
    }

    @Override
    protected Measure<Double, Duration> getExpectedAggregatedResponseTimeFirstTest() {
        double sumOfInverses = 1 / 0.1 + 1 / 5.5 + 1 / 2.5 + 1 / 1800;
        return Measure.valueOf(4d / sumOfInverses, SI.SECOND);
    }

    @Override
    protected Measure<Double, Duration> getExpectedAggregatedResponseTimeSecondTest() {
        double sumOfInverses = 1 / 0.1 + 2 / 5.5 + 1 / 2.5 + 1 / 1800;
        return Measure.valueOf(5 / sumOfInverses, SI.SECOND);
    }

}
