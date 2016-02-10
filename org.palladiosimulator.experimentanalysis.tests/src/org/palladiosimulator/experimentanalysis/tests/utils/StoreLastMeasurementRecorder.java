package org.palladiosimulator.experimentanalysis.tests.utils;

import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.recorderframework.IRecorder;
import org.palladiosimulator.recorderframework.config.IRecorderConfiguration;

/**
 * This is a dummy implementation of a recorder which just retains the most recent measurement.
 * 
 * @author Florian Rosenthal
 *
 */
public class StoreLastMeasurementRecorder implements IRecorder {

    private MeasuringValue lastMeasurement = null;

    /**
     * Gets the most recent measurement that was written to this instance.
     * 
     * @return The {@link MeasuringValue} that represents the most recent recorder measurement.
     */
    public MeasuringValue getLastMeasurement() {
        return this.lastMeasurement;
    }

    @Override
    public void writeData(MeasuringValue measurement) {
        this.lastMeasurement = measurement;
    }

    @Override
    public void initialize(IRecorderConfiguration recorderConfiguration) {
    }

    @Override
    public void flush() {
    }

    @Override
    public void newMeasurementAvailable(MeasuringValue arg0) {
    }

    @Override
    public void preUnregister() {
    }
}