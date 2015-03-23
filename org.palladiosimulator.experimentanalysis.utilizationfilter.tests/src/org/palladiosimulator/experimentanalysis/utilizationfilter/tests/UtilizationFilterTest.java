package org.palladiosimulator.experimentanalysis.utilizationfilter.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.palladiosimulator.edp2.datastream.AbstractDataSource;
import org.palladiosimulator.edp2.datastream.IDataSource;
import org.palladiosimulator.edp2.datastream.IDataStream;
import org.palladiosimulator.edp2.datastream.configurable.PropertyConfigurable;
import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.experimentanalysis.utilizationfilter.UtilizationFilter;
import org.palladiosimulator.experimentanalysis.utilizationfilter.UtilizationFilterConfiguration;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.measurementframework.TupleMeasurement;
import org.palladiosimulator.measurementframework.measureprovider.IMeasureProvider;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.MetricSetDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

public class UtilizationFilterTest {

	private UtilizationFilter filterUnderTest;
	private IDataSource inputData;
	private MeasuringValue expectedUtilization;
	private PropertyConfigurable filterUnderTestProperties;
	private Measure<Double, Duration> defaultWindowLength;
	private static final MetricSetDescription expectedInputDataMetric = MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE;
	private static final MetricSetDescription expectedOutputDataMetric = MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE;
	//constant that denotes  the maximum delta between double values for which both numbers are still considered equal
	private static final double DELTA = Math.pow(10, -12);

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		this.filterUnderTest = new UtilizationFilter();
		this.filterUnderTestProperties = this.filterUnderTest
				.createProperties();
		// the following cast is typesafe, as property is of required measure
		// type (cf. class UtilizationFilterConfiguration)
		this.defaultWindowLength = (Measure<Double, Duration>) this.filterUnderTestProperties
				.getDefaultConfiguration().get(UtilizationFilterConfiguration.WINDOW_LENGTH_KEY);
		this.inputData = new MockDataSource(expectedInputDataMetric,
				this.defaultWindowLength);
		this.expectedUtilization = new TupleMeasurement(
				expectedOutputDataMetric, this.defaultWindowLength,
				Measure.valueOf(0.7, Unit.ONE));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = IllegalStateException.class)
	public void testGetDataStreamNoDatasource() {
		this.filterUnderTest.getDataStream();
	}

	@Test
	public void testGetDataStream() {
		// make sure that input data is set, then obtain result stream
		this.filterUnderTest.setDataSource(inputData);
		IDataStream<MeasuringValue> result = this.filterUnderTest.getDataStream();
		// now check whether the result stream is as expected: one utilization
		// measure is expected
		assertTrue(result.isCompatibleWith(expectedOutputDataMetric));
		assertEquals(expectedOutputDataMetric, result.getMetricDesciption());
		assertEquals(1, result.size());
		
		MeasuringValue measurement = result.iterator().next();
		assertTrue(measurement.isCompatibleWith(expectedOutputDataMetric));
		Measure<Double, Dimensionless> expected = this.expectedUtilization.getMeasureForMetric(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
		Measure<Double, Dimensionless> actual = measurement.getMeasureForMetric(MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
		assertEquals(expected.getValue(), actual.getValue(), DELTA);

		// close the stream and check length afterwards
		result.close();
		assertEquals(0, result.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUtilizationFilterCtorDatasourceWrongMetric() {
		IDataSource dataSource = new MockDataSource(MetricDescriptionConstants.EXECUTION_RESULT_METRIC_TUPLE, this.defaultWindowLength);
		new UtilizationFilter(dataSource);
	}

	private static class MockDataSource extends AbstractDataSource {

		private List<MeasuringValue> data = new ArrayList<>();
		private Measure<Double, Duration> windowLength;

		private MockDataSource(MetricDescription metric,
				Measure<Double, Duration> windowLength) {
			super(metric);
			this.windowLength = windowLength;
			initializeDataSource();
		}

		private void initializeDataSource() {
			// create the filter input data, (point in time, state of active
			// resource) tuples
			Measure<Long, Dimensionless> idleStateMeasure = Measure.valueOf(0L, Unit.ONE);
			Measure<Long, Dimensionless> busyStateMeasure = Measure.valueOf(42L, Unit.ONE);

			// start with utilization
			Measure<Double, Duration> pointInTimeMeasure = Measure.valueOf(0d, SI.SECOND);
			this.data.add(new TupleMeasurement(expectedInputDataMetric,
					pointInTimeMeasure, busyStateMeasure));

			// no utilization next
			pointInTimeMeasure = Measure.valueOf(
					this.windowLength.doubleValue(SI.MILLI(SI.SECOND)) * 0.3,
					SI.MILLI(SI.SECOND));
			this.data.add(new TupleMeasurement(expectedInputDataMetric,
					pointInTimeMeasure, idleStateMeasure));

			// again some processes active
			pointInTimeMeasure = Measure.valueOf(
					this.windowLength.doubleValue(SI.CENTI(SI.SECOND)) * 0.6,
					SI.CENTI(SI.SECOND));
			this.data.add(new TupleMeasurement(expectedInputDataMetric,
					pointInTimeMeasure, busyStateMeasure));

			// this setup should yield a utilization of 0.70 (70%)
			// 70% of window is activity, rest is idleness
		}

		//this gets the input data, i.e., (point in time, state of active resource) tuples
		@Override
		public <M extends IMeasureProvider> IDataStream<M> getDataStream() {
			return new IDataStream<M>() {

				@Override
				public Iterator<M> iterator() {
					@SuppressWarnings("unchecked")
					//that cast is type safe as Measurement implements the IMeasureProvider interface 
					Iterator<M> result = (Iterator<M>) MockDataSource.this.data.iterator();
					return result;
				}

				@Override
				public MetricDescription getMetricDesciption() {
					return expectedInputDataMetric;
				}

				@Override
				public boolean isCompatibleWith(MetricDescription other) {
					return getMetricDesciption().equals(other);
				}

				@Override
				public void close() {
					MockDataSource.this.data.clear();
				}

				@Override
				public int size() {
					return MockDataSource.this.data.size();
				}
			};
		}

		@Override
		protected PropertyConfigurable createProperties() {
			return new PropertyConfigurable() {

				@Override
				public Class<?> getPropertyType(String key) {
					return null;
				}

				@Override
				public Set<String> getKeys() {
					return Collections.emptySet();
				}

				@Override
				public Map<String, Object> getDefaultConfiguration() {
					return Collections.emptyMap();
				}
			};
		}

		@Override
		public MeasuringPoint getMeasuringPoint() {
			return null;
		}

	}

}
