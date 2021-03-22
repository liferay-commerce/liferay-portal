/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.commerce.machine.learning.forecast.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.machine.learning.forecast.CPInstanceCommerceMLForecast;
import com.liferay.commerce.machine.learning.forecast.CPInstanceCommerceMLForecastManager;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Riccardo Ferrari
 */
@RunWith(Arquillian.class)
public class CPInstanceCommerceMLForecastManagerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_actualDate = RandomTestUtil.nextDate();

		_company = CompanyTestUtil.addCompany();

		_cpInstanceCommerceMLForecasts = _populateEntries(
			4, _FORECAST_LENGTH + _HISTORY_LENGTH);
	}

	@Test
	public void testGetCPInstanceCommerceMLForecast() throws Exception {
		CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast =
			_cpInstanceCommerceMLForecasts.get(
				RandomTestUtil.randomInt(
					0, _cpInstanceCommerceMLForecasts.size() - 1));

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			() -> {
				_assertResultEquals(
					cpInstanceCommerceMLForecast.getForecastId(),
					cpInstanceCommerceMLForecast);

				return null;
			});
	}

	@Test
	public void testGetCPInstanceCommerceMLForecasts() throws Exception {
		CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast =
			_cpInstanceCommerceMLForecasts.get(
				RandomTestUtil.randomInt(
					0, _cpInstanceCommerceMLForecasts.size() - 1));

		Stream<CPInstanceCommerceMLForecast>
			cpInstanceCommerceMLForecastStream =
				_cpInstanceCommerceMLForecasts.stream();

		List<CPInstanceCommerceMLForecast> cpInstanceCommerceMLForecasts =
			cpInstanceCommerceMLForecastStream.filter(
				forecast -> Objects.equals(
					forecast.getSku(), cpInstanceCommerceMLForecast.getSku())
			).collect(
				Collectors.toList()
			);

		IdempotentRetryAssert.retryAssert(
			3, TimeUnit.SECONDS,
			() -> {
				_assertResultEquals(
					cpInstanceCommerceMLForecast.getSku(),
					cpInstanceCommerceMLForecasts);

				return null;
			});
	}

	private void _assertResultEquals(
			long forecastId,
			CPInstanceCommerceMLForecast expectedCPInstanceCommerceMLForecast)
		throws Exception {

		CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast =
			_cpInstanceCommerceMLForecastManager.
				getCPInstanceCommerceMLForecast(
					_company.getCompanyId(), forecastId);

		Assert.assertEquals(
			expectedCPInstanceCommerceMLForecast.getForecastId(),
			cpInstanceCommerceMLForecast.getForecastId());

		Assert.assertEquals(
			"Scope", expectedCPInstanceCommerceMLForecast.getScope(),
			cpInstanceCommerceMLForecast.getScope());
	}

	private void _assertResultEquals(
			String sku,
			List<CPInstanceCommerceMLForecast>
				expectedCPInstanceCommerceMLForecasts)
		throws Exception {

		List<CPInstanceCommerceMLForecast> cpInstanceCommerceMLForecasts =
			_cpInstanceCommerceMLForecastManager.
				getMonthlyQuantityCPInstanceCommerceMLForecasts(
					_actualDate, _company.getCompanyId(), _FORECAST_LENGTH,
					_HISTORY_LENGTH, sku);

		Assert.assertEquals(
			"Forecast list size", expectedCPInstanceCommerceMLForecasts.size(),
			cpInstanceCommerceMLForecasts.size());

		for (int i = 0; i < expectedCPInstanceCommerceMLForecasts.size(); i++) {
			CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast =
				cpInstanceCommerceMLForecasts.get(i);

			CPInstanceCommerceMLForecast expectedCPInstanceCommerceMLForecast =
				expectedCPInstanceCommerceMLForecasts.get(i);

			Assert.assertEquals(
				"Period", expectedCPInstanceCommerceMLForecast.getPeriod(),
				cpInstanceCommerceMLForecast.getPeriod());

			Assert.assertEquals(
				"Scope", expectedCPInstanceCommerceMLForecast.getScope(),
				cpInstanceCommerceMLForecast.getScope());

			Assert.assertEquals(
				"Sku", expectedCPInstanceCommerceMLForecast.getSku(),
				cpInstanceCommerceMLForecast.getSku());

			Assert.assertEquals(
				"Target", expectedCPInstanceCommerceMLForecast.getTarget(),
				cpInstanceCommerceMLForecast.getTarget());

			Assert.assertEquals(
				"Timestamp",
				expectedCPInstanceCommerceMLForecast.getTimestamp(),
				cpInstanceCommerceMLForecast.getTimestamp());
		}
	}

	private CPInstanceCommerceMLForecast _createCPInstanceCommerceMLForecast(
		String sku, Date timestamp) {

		CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast =
			_cpInstanceCommerceMLForecastManager.create();

		cpInstanceCommerceMLForecast.setActual(
			(float)RandomTestUtil.nextDouble());
		cpInstanceCommerceMLForecast.setCompanyId(_company.getCompanyId());
		cpInstanceCommerceMLForecast.setForecast(
			(float)RandomTestUtil.nextDouble());
		cpInstanceCommerceMLForecast.setForecastLowerBound(
			(float)RandomTestUtil.nextDouble());
		cpInstanceCommerceMLForecast.setForecastUpperBound(
			(float)RandomTestUtil.nextDouble());
		cpInstanceCommerceMLForecast.setSku(sku);
		cpInstanceCommerceMLForecast.setJobId(RandomTestUtil.randomString());
		cpInstanceCommerceMLForecast.setPeriod("month");
		cpInstanceCommerceMLForecast.setTarget("quantity");
		cpInstanceCommerceMLForecast.setTimestamp(timestamp);

		return cpInstanceCommerceMLForecast;
	}

	private List<CPInstanceCommerceMLForecast> _populateEntries(
			int forecastCount, int seriesLength)
		throws Exception {

		List<CPInstanceCommerceMLForecast> cpInstanceCommerceMLForecasts =
			new ArrayList<>();

		LocalDateTime endLocalDateTime = LocalDateTime.ofInstant(
			_actualDate.toInstant(), ZoneOffset.systemDefault());

		endLocalDateTime = endLocalDateTime.truncatedTo(ChronoUnit.DAYS);

		endLocalDateTime = endLocalDateTime.withDayOfMonth(1);

		endLocalDateTime = endLocalDateTime.plusMonths(_FORECAST_LENGTH);

		for (int i = 0; i < forecastCount; i++) {
			String sku = RandomTestUtil.randomString();

			for (int j = 0; j < seriesLength; j++) {
				CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast =
					_createCPInstanceCommerceMLForecast(
						sku, _toDate(endLocalDateTime.minusMonths(j)));

				cpInstanceCommerceMLForecast =
					_cpInstanceCommerceMLForecastManager.
						addCPInstanceCommerceMLForecast(
							cpInstanceCommerceMLForecast);

				cpInstanceCommerceMLForecasts.add(cpInstanceCommerceMLForecast);
			}
		}

		return cpInstanceCommerceMLForecasts;
	}

	private Date _toDate(LocalDateTime localDateTime) {
		ZonedDateTime zonedDateTime = localDateTime.atZone(
			ZoneOffset.systemDefault());

		return Date.from(zonedDateTime.toInstant());
	}

	private static final int _FORECAST_LENGTH = 2;

	private static final int _HISTORY_LENGTH = 9;

	private Date _actualDate;

	@DeleteAfterTestRun
	private Company _company;

	@Inject
	private CPInstanceCommerceMLForecastManager
		_cpInstanceCommerceMLForecastManager;

	private List<CPInstanceCommerceMLForecast> _cpInstanceCommerceMLForecasts;

}