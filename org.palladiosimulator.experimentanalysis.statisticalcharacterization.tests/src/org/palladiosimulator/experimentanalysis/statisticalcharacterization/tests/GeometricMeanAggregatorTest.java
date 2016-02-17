package org.palladiosimulator.experimentanalysis.statisticalcharacterization.tests;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.GeometricMeanAggregator;
import org.palladiosimulator.experimentanalysis.statisticalcharacterization.aggregators.StatisticalCharacterizationAggregator;

public class GeometricMeanAggregatorTest extends StatisticalCharacterizationAggregatorTest {

    @Override
    protected StatisticalCharacterizationAggregator getAggregatorUnderTest() {
        return new GeometricMeanAggregator(this.dummyRecorder, RESULT_METRIC);
    }

    @Override
    protected Measure<Double, Duration> getExpectedAggregatedResponseTimeFirstTest() {
        return Measure.valueOf(7.0533234616974002588733413490183, SI.SECOND);
    }

    @Override
    protected Measure<Double, Duration> getExpectedAggregatedResponseTimeSecondTest() {
        return Measure.valueOf(6.7110052498303912116958243874521, SI.SECOND);
    }

}
