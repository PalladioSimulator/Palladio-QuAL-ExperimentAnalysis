package org.palladiosimulator.experimentanalysis.windowaggregators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

import org.palladiosimulator.experimentanalysis.ISlidingWindowListener;
import org.palladiosimulator.experimentanalysis.SlidingWindow;
import org.palladiosimulator.experimentanalysis.SlidingWindowRecorder;
import org.palladiosimulator.measurementframework.MeasuringValue;
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

    private final Collection<IRecorder> recordersToWriteInto = new ArrayList<>();

    /**
     * Initializes a new instance of the {@link SlidingWindowAggregator} class which shall not be
     * connected to a recorder (data sink) yet.
     * 
     * @see #addRecorder(IRecorder)
     */
    public SlidingWindowAggregator() {
    }

    /**
     * Initializes a new instance of the {@link SlidingWindowAggregator} class with the given
     * parameter.
     * 
     * @param recorderToWriteInto
     *            An {@link IRecorder} this instance writes the aggregated window data into.
     *            Typically, a recorder that writes into a persistence framework like EDP 2 is
     *            passed here.
     * @throws NullPointerException
     *             If the given {@link IRecorder} is {@code null}.
     */
    public SlidingWindowAggregator(IRecorder recorderToWriteInto) {
        this.recordersToWriteInto.add(Objects.requireNonNull(recorderToWriteInto, "Given recorder must not be null."));
    }

    /**
     * Initializes a new instance of the {@link SlidingWindowAggregator} class with the given
     * parameter.
     * 
     * @param recordersToWriteInto
     *            A @{@link Collection} of {@link IRecorder}s this instance writes the aggregated
     *            window data into. Typically, recorders that write data into persistence frameworks
     *            like EDP 2 are passed here.
     * @throws NullPointerException
     *             If the given collection of {@link IRecorder}s is {@code null}.
     * @throws IllegalArgumentException
     *             If the given collection of recorders is empty.
     */
    public SlidingWindowAggregator(Collection<IRecorder> recordersToWriteInto) {
        if (Objects.requireNonNull(recordersToWriteInto).isEmpty()) {
            throw new IllegalArgumentException("Given recorders collection must contain at least one recorder.");
        }
        this.recordersToWriteInto.addAll(recordersToWriteInto);
    }

    public void addRecorder(IRecorder recorder) {
        this.recordersToWriteInto.add(Objects.requireNonNull(recorder));
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
     * @return A {@link MeasuringValue} that is to be passed on to the attached recorder.
     */
    protected abstract MeasuringValue processWindowData(Iterable<MeasuringValue> windowData,
            Measure<Double, Duration> windowLeftBound, Measure<Double, Duration> windowLength);

    private void writeToRecorder(MeasuringValue newMeasurement) {
        this.recordersToWriteInto.forEach(recorder -> recorder.writeData(newMeasurement));
    }

    @Override
    public final void onSlidingWindowFull(Iterable<MeasuringValue> windowData,
            Measure<Double, Duration> windowLeftBound, Measure<Double, Duration> windowLength) {
        writeToRecorder(processWindowData(windowData, windowLeftBound, windowLength));
    }
}
