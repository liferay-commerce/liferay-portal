/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.order.web.internal.frontend;

import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.frontend.taglib.clay.data.set.filter.BaseDateRangeClayDataSetFilter;
import com.liferay.frontend.taglib.clay.data.set.filter.ClayDataSetFilter;
import com.liferay.frontend.taglib.clay.data.set.filter.DateClayDataSetFilterItem;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.BaseModelSearchResult;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.SortFactoryUtil;
import com.liferay.portal.kernel.service.UserService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luca Pellizzon
 */
@Component(
	enabled = false, immediate = true,
	property = "clay.data.set.display.name=" + CommerceOrderDataSetConstants.COMMERCE_DATA_SET_KEY_ALL_ORDERS,
	service = ClayDataSetFilter.class
)
public class CommerceOrderCreateDateClayTableDataSetFilter
	extends BaseDateRangeClayDataSetFilter {

	@Override
	public String getId() {
		return "createDate";
	}

	@Override
	public String getLabel() {
		return "order-create-date-range";
	}

	public DateClayDataSetFilterItem getMax() {
		Calendar calendar = Calendar.getInstance();

		return new DateClayDataSetFilterItem(
			calendar.get(Calendar.DAY_OF_MONTH),
			calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
	}

	public DateClayDataSetFilterItem getMin() {
		Calendar calendar = Calendar.getInstance();

		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR) - 1;

		try {
			User currentUser = _userService.getCurrentUser();

			SearchContext searchContext = _buildSearchContext(
				currentUser.getCompanyId(), 0, 1);

			BaseModelSearchResult<CommerceOrder>
				commerceOrderBaseModelSearchResult =
					_commerceOrderLocalService.searchCommerceOrders(
						searchContext);

			List<CommerceOrder> commerceOrders =
				commerceOrderBaseModelSearchResult.getBaseModels();

			if (!commerceOrders.isEmpty()) {
				CommerceOrder commerceOrder = commerceOrders.get(0);

				Date createDate = commerceOrder.getCreateDate();

				calendar.setTime(createDate);

				day = calendar.get(Calendar.DAY_OF_MONTH);
				month = calendar.get(Calendar.MONTH) + 1;
				year = calendar.get(Calendar.YEAR);
			}
		}
		catch (PortalException portalException) {
			if (_log.isInfoEnabled()) {
				_log.info("No order found", portalException);
			}
		}

		return new DateClayDataSetFilterItem(day, month, year);
	}

	private SearchContext _buildSearchContext(
			long companyId, int start, int end)
		throws PortalException {

		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(companyId);
		searchContext.setEnd(end);
		searchContext.setGroupIds(_getCommerceChannelGroupIds(companyId));

		Sort sort = SortFactoryUtil.getSort(
			CommerceOrder.class, Sort.LONG_TYPE, Field.CREATE_DATE, "ASC");

		searchContext.setSorts(sort);

		searchContext.setStart(start);

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setHighlightEnabled(false);
		queryConfig.setScoreEnabled(false);

		return searchContext;
	}

	private long[] _getCommerceChannelGroupIds(long companyId)
		throws PortalException {

		List<CommerceChannel> commerceChannels =
			_commerceChannelLocalService.searchCommerceChannels(companyId);

		Stream<CommerceChannel> stream = commerceChannels.stream();

		return stream.mapToLong(
			CommerceChannel::getGroupId
		).toArray();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceOrderCreateDateClayTableDataSetFilter.class);

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference
	private CommerceOrderLocalService _commerceOrderLocalService;

	@Reference
	private UserService _userService;

}