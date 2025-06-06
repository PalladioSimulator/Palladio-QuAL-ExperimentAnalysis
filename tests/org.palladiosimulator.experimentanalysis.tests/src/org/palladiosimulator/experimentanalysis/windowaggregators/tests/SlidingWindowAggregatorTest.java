package org.palladiosimulator.experimentanalysis.windowaggregators.tests;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.palladiosimulator.experimentanalysis.tests.utils.StoreLastMeasurementRecorder;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.recorderframework.core.IRecorder;

public abstract class SlidingWindowAggregatorTest {

    protected SlidingWindowAggregator aggregatorUnderTest;
    protected StoreLastMeasurementRecorder dummyRecorder;
    protected Deque<MeasuringValue> data;
    protected Measure<Double, Duration> windowLength;
    protected Measure<Double, Duration> currentLowerBound;

    @Before
    public void setUp() throws Exception {
        this.data = new LinkedList<>();
        this.windowLength = Measure.valueOf(10d, SI.SECOND);
        this.currentLowerBound = Measure.valueOf(0d, SI.SECOND);
        this.dummyRecorder = new StoreLastMeasurementRecorder();
    }

    @After
    public void tearDown() throws Exception {
        this.data.clear();
    }

    @Test(expected = NullPointerException.class)
    public final void testSlidingWindowAggregatorCtorNullRecorder() {
        IRecorder recorder = null;
        new DummySlidingWindowAggregator(recorder);
    }

    @Test(expected = NullPointerException.class)
    public final void testSlidingWindowAggregatorCtorNullRecorderCollection() {
        Collection<IRecorder> recorders = null;
        new DummySlidingWindowAggregator(recorders);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testSlidingWindowAggregatorCtorEmptyRecorderCollection() {
        Collection<IRecorder> recorders = Collections.emptyList();
        new DummySlidingWindowAggregator(recorders);
    }

    private static class DummySlidingWindowAggregator extends SlidingWindowAggregator {

        private DummySlidingWindowAggregator(IRecorder recorderToWriteInto) {
            super(recorderToWriteInto);
        }

        private DummySlidingWindowAggregator(Collection<IRecorder> recordersToWriteInto) {
            super(recordersToWriteInto);
        }

        @Override
        public MetricDescription getExpectedWindowDataMetric() {
            return null;
        }

        @Override
        protected MeasuringValue processWindowData(Iterable<MeasuringValue> windowData,
                Measure<Double, Duration> windowLeftBound, Measure<Double, Duration> windowLength) {
            return null;
        }
    }

}
