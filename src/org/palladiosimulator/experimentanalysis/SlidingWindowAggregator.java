package org.palladiosimulator.experimentanalysis;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

import org.palladiosimulator.measurementframework.Measurement;
import org.palladiosimulator.recorderframework.IRecorder;

/**
 * This class is the base class of all {@link ISlidingWindowListener} that aggregate/process the
 * measurements collected by a {@link SlidingWindow} once it is full (i.e., it moved on).<br>
 * In terms of signal processing and time series analysis/statistics, this class can be thought of
 * as a base class for any moving average implementation.
 * 
 * @see SlidingWindowRecorder
 * 
 * @author Florian Rosenthal
 *
 */
public abstract class SlidingWindowAggregator implements ISlidingWindowListener {

    private final IRecorder recorderToWriteInto;

    /**
     * Initializes a new instance of the {@link SlidingWindowAggregator} class with the given
     * parameter.
     * 
     * @param recorderToWriteInto
     *            An {@link IRecorder} this instance writes the aggregated window data into.
     *            Typically, a recorder that writes into a persistence framework like EDP 2 is
     *            passed here.
     * @throws IllegalArgumentException
     *             If the given {@link IRecorder} is {@code null}.
     */
    public SlidingWindowAggregator(IRecorder recorderToWriteInto) {
        if (recorderToWriteInto == null) {
            throw new IllegalArgumentException("Given recorder must not be null.");
        }
        this.recorderToWriteInto = recorderToWriteInto;
    }

    /**
     * By implementing this method, subclasses (i.e., concrete aggregators) specify how the window
     * data is aggregated/processed and what resulting measurement is passed on to the attached
     * recorder.
     * 
     * @param windowData
     *            The window data to be processed.
     * @param windowLeftBound
     *            A {@link Measure} denoting the new left bound of the sliding window.
     * @param windowLength
     *            A {@link Measure} denoting the length of the sliding window.
     * @return A {@link Measurement} that is to be passed on to the attached recorder.
     */
    protected abstract Measurement processWindowData(Iterable<Measurement> windowData,
            Measure<Double, Duration> windowLeftBound, Measure<Double, Duration> windowLength);

    private void writeToRecorder(Measurement newMeasurement) {
        this.recorderToWriteInto.writeData(newMeasurement);
    }

    @Override
    public final void onSlidingWindowFull(Iterable<Measurement> windowData, Measure<Double, Duration> windowLeftBound,
            Measure<Double, Duration> windowLength) {
        writeToRecorder(processWindowData(windowData, windowLeftBound, windowLength));
    }
}
