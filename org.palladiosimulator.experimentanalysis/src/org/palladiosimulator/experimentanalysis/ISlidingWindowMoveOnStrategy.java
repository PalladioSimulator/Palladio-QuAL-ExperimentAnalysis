package org.palladiosimulator.experimentanalysis;

import java.util.Deque;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

import org.palladiosimulator.measurementframework.MeasuringValue;

/**
 * Each window instance has an attached {@link ISlidingWindowMoveOnStrategy}
 * that defines how the collected data (i.e., the measurements) are adjusted
 * when the window moves forward. For instance, one strategy might be to
 * discard all measurements that are now "outside" the window.
 * 
 * @author Florian Rosenthal
 *
 */
public interface ISlidingWindowMoveOnStrategy {
	/**
	 * This method specifies how the collected data (i.e., the measurements)
	 * are adjusted when the window moves forward. It is called by the
	 * associated {@link SlidingWindow} instance each time it has moved
	 * forward.<br>
	 * Note that the adjustment of the data has to be done in-place.
	 * 
	 * @param currentData
	 *            A {@link Deque} containing the window data (i.e., the
	 *            collected measurements) at the moment it moved forward.
	 *            The adjustment of the data has to be done in-place, i.e.,
	 *            this Deque has to be manipulated directly.
	 * @param newLowerBound
	 *            A point in time (in seconds) denoting the new lower bound
	 *            of the window.
	 * @param increment
	 *            A {@link Measure} indicating by what the window moved
	 *            forward.
	 */
	public void adjustData(Deque<MeasuringValue> currentData, Measure<Double, Duration> newLowerBound,
			Measure<Double, Duration> increment);
}
