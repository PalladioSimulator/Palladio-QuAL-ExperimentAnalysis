package org.palladiosimulator.experimentanalysis.statisticalcharacterization.tests;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.HarmonicMeanAggregator;
import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.StatisticalCharacterizationAggregator;

public class HarmonicMeanAggregatorTest extends StatisticalCharacterizationAggregatorTest {

    @Override
    protected StatisticalCharacterizationAggregator getAggregatorUnderTest() {
        return new HarmonicMeanAggregator(this.dummyRecorder, WINDOW_DATA_METRIC);
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
