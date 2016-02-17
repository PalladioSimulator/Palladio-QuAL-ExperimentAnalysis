package org.palladiosimulator.experimentanalysis.tests.utils;

import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.recorderframework.IRecorder;
import org.palladiosimulator.recorderframework.config.IRecorderConfiguration;

/**
 * Dummy implementation of an {@link IRecorder} which just stores the most recently received (via
 * {@link #writeData(MeasuringValue)} measurement.
 * 
 * @author Florian Rosenthal
 *
 */
public class StoreLastMeasurementRecorder implements IRecorder {

    private MeasuringValue lastMeasurement = null;

    /**
     * Gets the last measurement (i.e., the only one that is stored) that has been received.
     * 
     * @return A {@link MeasuringValue} denoting the last received measurement.
     */
    public MeasuringValue getLastMeasurement() {
        return this.lastMeasurement;
    }

    /**
     * {@inheritDoc} This implementation just the stores this measurement and discards the previous
     * ones.
     */
    @Override
    public void writeData(MeasuringValue measurement) {
        this.lastMeasurement = measurement;
    }

    /**
     * {@inheritDoc} This implementation does nothing.
     */
    @Override
    public void initialize(IRecorderConfiguration recorderConfiguration) {
    }

    /**
     * {@inheritDoc} This implementation does nothing.
     */
    @Override
    public void flush() {
    }

    /**
     * {@inheritDoc} This implementation does nothing.
     */
    @Override
    public void newMeasurementAvailable(MeasuringValue arg0) {
    }

    /**
     * {@inheritDoc} This implementation does nothing.
     */
    @Override
    public void preUnregister() {
    }
}
