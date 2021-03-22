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

package com.liferay.commerce.machine.learning.internal.forecast;

import com.liferay.commerce.machine.learning.forecast.CPInstanceCommerceMLForecast;
import com.liferay.commerce.machine.learning.forecast.CPInstanceCommerceMLForecastManager;
import com.liferay.commerce.machine.learning.internal.forecast.constants.CommerceMLForecastField;
import com.liferay.commerce.machine.learning.internal.forecast.constants.CommerceMLForecastPeriod;
import com.liferay.commerce.machine.learning.internal.forecast.constants.CommerceMLForecastScope;
import com.liferay.commerce.machine.learning.internal.forecast.constants.CommerceMLForecastTarget;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.ParseException;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.TermsFilter;
import com.liferay.portal.search.engine.adapter.search.CountSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;

import java.util.Date;
import java.util.List;

import org.osgi.service.component.annotations.Component;

/**
 * @author Riccardo Ferrari
 */
@Component(
	enabled = false, immediate = true,
	service = CPInstanceCommerceMLForecastManager.class
)
public class CPInstanceCommerceMLForecastManagerImpl
	extends BaseCommerceMLForecastServiceImpl<CPInstanceCommerceMLForecast>
	implements CPInstanceCommerceMLForecastManager {

	@Override
	public CPInstanceCommerceMLForecast addCPInstanceCommerceMLForecast(
			CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast)
		throws PortalException {

		long commerceMLForecastId = getHash(
			cpInstanceCommerceMLForecast.getPeriod(),
			cpInstanceCommerceMLForecast.getScope(),
			cpInstanceCommerceMLForecast.getSku(),
			cpInstanceCommerceMLForecast.getTarget(),
			cpInstanceCommerceMLForecast.getTimestamp());

		cpInstanceCommerceMLForecast.setForecastId(commerceMLForecastId);

		return addCommerceMLForecast(cpInstanceCommerceMLForecast);
	}

	@Override
	public CPInstanceCommerceMLForecast create() {
		CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast =
			new CPInstanceCommerceMLForecastImpl();

		cpInstanceCommerceMLForecast.setScope(
			_commerceMLForecastScope.getLabel());

		return cpInstanceCommerceMLForecast;
	}

	@Override
	public CPInstanceCommerceMLForecast getCPInstanceCommerceMLForecast(
			long companyId, long forecastId)
		throws PortalException {

		return getCommerceMLForecast(companyId, forecastId);
	}

	@Override
	public List<CPInstanceCommerceMLForecast>
			getMonthlyQuantityCPInstanceCommerceMLForecasts(
				Date actualDate, long companyId, int end, int forecastLength,
				int historyLength, String sku, int start)
		throws PortalException {

		Query query = _getMonthlyQuantityQuery(
			actualDate, forecastLength, historyLength, sku);

		int size = end - start;

		SearchSearchRequest searchSearchRequest = getSearchSearchRequest(
			commerceMLIndexer.getIndexName(companyId), query, start, size,
			getDefaultSort(true));

		return getSearchResults(searchSearchRequest);
	}

	@Override
	public List<CPInstanceCommerceMLForecast>
			getMonthlyQuantityCPInstanceCommerceMLForecasts(
				Date actualDate, long companyId, int forecastLength,
				int historyLength, String sku)
		throws PortalException {

		int size = forecastLength + historyLength;

		return getMonthlyQuantityCPInstanceCommerceMLForecasts(
			actualDate, companyId, size, forecastLength, historyLength, sku, 0);
	}

	@Override
	public long getMonthlyQuantityCPInstanceCommerceMLForecastsCount(
			Date actualDate, long companyId, int forecastLength,
			int historyLength, String sku)
		throws PortalException {

		Query query = _getMonthlyQuantityQuery(
			actualDate, forecastLength, historyLength, sku);

		CountSearchRequest countSearchRequest = getCountSearchRequest(
			commerceMLIndexer.getIndexName(companyId), query);

		return getCountResult(countSearchRequest);
	}

	@Override
	protected Document toDocumentModel(
		CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast) {

		Document document = getBaseDocument(cpInstanceCommerceMLForecast);

		document.addText(
			CommerceMLForecastField.SKU, cpInstanceCommerceMLForecast.getSku());

		return document;
	}

	@Override
	protected CPInstanceCommerceMLForecast toForecastModel(Document document) {
		CPInstanceCommerceMLForecast cpInstanceCommerceMLForecast =
			getBaseCommerceMLForecastModel(
				new CPInstanceCommerceMLForecastImpl(), document);

		cpInstanceCommerceMLForecast.setSku(
			document.get(CommerceMLForecastField.SKU));

		return cpInstanceCommerceMLForecast;
	}

	private Query _getMonthlyQuantityQuery(
			Date actualDate, int forecastLength, int historyLength, String sku)
		throws ParseException {

		CommerceMLForecastPeriod commerceMLForecastPeriod =
			CommerceMLForecastPeriod.MONTH;

		CommerceMLForecastTarget commerceMLForecastTarget =
			CommerceMLForecastTarget.QUANTITY;

		Date endDate = getEndDate(
			actualDate, commerceMLForecastPeriod, forecastLength);

		Date startDate = getStartDate(
			actualDate, commerceMLForecastPeriod, historyLength);

		BooleanQuery baseQuery = getBaseQuery(
			_commerceMLForecastScope.getLabel(),
			commerceMLForecastPeriod.getLabel(),
			commerceMLForecastTarget.getLabel(), startDate, endDate);

		BooleanFilter preBooleanFilter = baseQuery.getPreBooleanFilter();

		TermsFilter termsFilter = new TermsFilter(CommerceMLForecastField.SKU);

		termsFilter.addValue(sku);

		preBooleanFilter.add(termsFilter, BooleanClauseOccur.MUST);

		return baseQuery;
	}

	private static final CommerceMLForecastScope _commerceMLForecastScope =
		CommerceMLForecastScope.CP_INSTANCE;

}