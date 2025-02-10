package org.palladiosimulator.experimentanalysis;

import java.util.Deque;

import jakarta.measure.Measure;
import jakarta.measure.quantity.Duration;

import org.palladiosimulator.measurementframework.MeasuringValue;

/**
 * An implementation of {@link ISlidingWindowMoveOnStrategy} that discards all the measurements
 * which are prior to the new lower bound of the {@link SlidingWindow} after it moved on. If all
 * measurements are within the new bounds of the window, this implementation does nothing. <br>
 * 
 * @author Florian Rosenthal, Sebastian Krach
 *
 */
public final class DiscardAllElementsPriorToLowerBoundStrategy implements ISlidingWindowMoveOnStrategy {

    @Override
    public void adjustData(Deque<MeasuringValue> currentData, Measure<Double, Duration> newLowerBound,
            Measure<Double, Duration> increment) {

        while (!currentData.isEmpty() && KeepLastElementPriorToLowerBoundStrategy
                .isFirstElementPriorToCurrentLowerBound(currentData, newLowerBound)) {
            currentData.pollFirst();
        }
    }
}
