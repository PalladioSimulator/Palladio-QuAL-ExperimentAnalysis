package org.palladiosimulator.experimentanalysis;

import java.util.Deque;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

import org.palladiosimulator.measurementframework.MeasuringValue;

/**
 * Implementation of a trivial {@link ISlidingWindowMoveOnStrategy} which just discards all the
 * window data once it moves on.
 * 
 * @author Florian Rosenthal
 *
 */
public class FlushWindowStrategy implements ISlidingWindowMoveOnStrategy {

    @Override
    public void adjustData(Deque<MeasuringValue> currentData, Measure<Double, Duration> newLowerBound,
            Measure<Double, Duration> increment) {
        currentData.clear();
    }

}
