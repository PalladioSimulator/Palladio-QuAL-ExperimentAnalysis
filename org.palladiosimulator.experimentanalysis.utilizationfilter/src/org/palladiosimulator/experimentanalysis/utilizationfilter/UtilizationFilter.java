package org.palladiosimulator.experimentanalysis.utilizationfilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.IPersistableElement;
import org.palladiosimulator.edp2.datastream.IDataSource;
import org.palladiosimulator.edp2.datastream.IDataStream;
import org.palladiosimulator.edp2.datastream.configurable.PropertyConfigurable;
import org.palladiosimulator.edp2.datastream.filter.AbstractFilter;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowUtilizationAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.recorderframework.IRecorder;
import org.palladiosimulator.recorderframework.config.IRecorderConfiguration;

/**
 * This class is a moving average implementation that calculates the utilization of an active
 * resource based on set of {@code (point in time, state of active resource)} tuple measurements.
 * <br>
 * That is, when being applied to an {@link IDataSource} that provides measurements adhering to this
 * metric, this filter outputs a set of subsequent {@code (point in time, utilization)} tuples. This
 * result set can be obtained by calling the {@link UtilizationFilter#getDataStream()} method.
 *
 * @see AbstractFilter
 * @see UtilizationFilterSlidingWindow
 * @see IDataSource
 * @see IDataStream
 *
 * @author Florian Rosenthal
 *
 */
public final class UtilizationFilter extends AbstractFilter implements IPersistable, IPersistableElement {

    private static final MetricDescription FILTER_RESULT_METRIC = MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE;
    private static final MetricDescription FILTER_INPUT_METRIC = MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE;

    /**
     * Initializes a new instance of the {@link UtilizationFilter} class.
     */
    public UtilizationFilter() {
        super(FILTER_INPUT_METRIC);
    }

    /**
     * Initializes a new instance of the {@link UtilizationFilter} class with the given data source.
     *
     * @param datasource
     *            The {@link IDataSource} this filter is applied to.
     * @throws IllegalArgumentException
     *             if the data source metric does not match the input metric expected by the filter.
     */
    public UtilizationFilter(final IDataSource datasource) {
        super(datasource, FILTER_INPUT_METRIC);
    }

    /**
     * Obtains this filter's output data (i.e., a set of subsequent
     * {@code (point in time, utilization)} tuples) based on the current input data.
     *
     * @return An {@link IDataStream} containing the resulting utilization measurements.
     * @throws IllegalStateException
     *             if no input data source has been set beforehand.
     */
    @SuppressWarnings("unchecked")
    @Override
    public IDataStream<MeasuringValue> getDataStream() {
        if (this.getDataSource() != null) {
            final IDataStream<MeasuringValue> inputData = this.getDataSource().getDataStream();
            return new UtilizationFilterOutputDataStream(inputData);
        }
        throw new IllegalStateException("No input data available. UtilizationFilter cannot be applied.");
    }

    /**
     * This class contains the output data, i.e., the stream of {@code (point in time, utilization)}
     * tuples this filter creates.<br>
     *
     * @author Florian Rosenthal
     *
     */
    private final class UtilizationFilterOutputDataStream implements IDataStream<MeasuringValue>, IRecorder {

        private final IDataStream<MeasuringValue> inputData;
        private final List<MeasuringValue> outputData;
        private final UtilizationFilterSlidingWindow slidingWindow;

        /**
         * Initializes of the {@link UtilizationFilterOutputDataStream} class with the given
         * parameter.
         *
         * @param inputData
         *            A {@link IDataStream} containing {@code state of active resource tuple}
         *            measurements.
         */
        private UtilizationFilterOutputDataStream(final IDataStream<MeasuringValue> inputData) {
            final Measure<Double, Duration> windowLength = UtilizationFilter.this
                    .<UtilizationFilterConfiguration> getConfiguration().getWindowLength();

            final Measure<Double, Duration> windowIncrement = UtilizationFilter.this
                    .<UtilizationFilterConfiguration> getConfiguration().getWindowIncrement();

            this.slidingWindow = new UtilizationFilterSlidingWindow(windowLength, windowIncrement,
                    new SlidingWindowUtilizationAggregator(FILTER_INPUT_METRIC, this));
            this.inputData = inputData;
            this.outputData = new ArrayList<MeasuringValue>();
            initializeOutputStream();
        }

        /**
         * Initializes the output stream, that is, the underlying
         * {@link UtilizationFilterSlidingWindow} is filled and the corresponding
         * {@link SlidingWindowUtilizationAggregator} is triggered each time the window is full.
         */
        private void initializeOutputStream() {
            // write all the data into the window
            for (final MeasuringValue measurement : inputData) {
                this.slidingWindow.addMeasurement(measurement);
            }
            // handle the case that the last measurement is prior to window length, deal with last
            // measurements (that were taken later than last window move on)
            this.slidingWindow.noMoreDataAvailable();
        }

        @Override
        public Iterator<MeasuringValue> iterator() {
            return outputData.iterator();
        }

        @Override
        public MetricDescription getMetricDesciption() {
            return FILTER_RESULT_METRIC;
        }

        @Override
        public boolean isCompatibleWith(final MetricDescription other) {
            return getMetricDesciption().equals(other);
        }

        @Override
        public void close() {
            this.inputData.close();
            this.flush();

        }

        @Override
        public int size() {
            return this.outputData.size();
        }

        @Override
        public void initialize(final IRecorderConfiguration recorderConfiguration) {
            // do nothing
        }

        @Override
        public void writeData(final MeasuringValue measurement) {
            this.outputData.add(measurement);
        }

        @Override
        public void flush() {
            this.outputData.clear();
        }

        @Override
        public void newMeasurementAvailable(final MeasuringValue newMeasurement) {
            // TODO Auto-generated method stub

        }

        @Override
        public void preUnregister() {
            // TODO Auto-generated method stub

        }

    }

    @Override
    public PropertyConfigurable createProperties() {
        return new UtilizationFilterConfiguration();
    }

    @Override
    public String getFactoryId() {
        return UtilizationFilterInputFactory.FACTORY_ID;
    }

    @Override
    public void saveState(final IMemento memento) {
        UtilizationFilterInputFactory.saveState(memento, this);

    }
}
