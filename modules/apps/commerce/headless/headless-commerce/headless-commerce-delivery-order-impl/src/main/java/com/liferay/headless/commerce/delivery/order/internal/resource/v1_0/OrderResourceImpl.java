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

package com.liferay.headless.commerce.delivery.order.internal.resource.v1_0;

import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.product.service.CommerceChannelService;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Order;
import com.liferay.headless.commerce.delivery.order.internal.dto.v1_0.OrderDTOConverter;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.OrderResource;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false, properties = "OSGI-INF/liferay/rest/v1_0/order.properties",
	scope = ServiceScope.PROTOTYPE, service = OrderResource.class
)
public class OrderResourceImpl extends BaseOrderResourceImpl {

	@Override
	public Page<Order> getChannelAccountOrdersPage(
			Long accountId, Long channelId, Pagination pagination)
		throws Exception {

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.getCommerceChannel(channelId);

		List<CommerceOrder> commerceOrders =
			_commerceOrderService.getPlacedCommerceOrders(
				commerceChannel.getGroupId(), accountId, null,
				pagination.getStartPosition(), pagination.getEndPosition());

		return Page.of(
			_toOrders(commerceOrders), pagination,
			_commerceOrderService.getPlacedCommerceOrdersCount(
				commerceChannel.getGroupId(), accountId, null));
	}

	private Order _toOrder(CommerceOrder commerceOrder) throws Exception {
		return _orderDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				commerceOrder.getCommerceOrderId(),
				contextAcceptLanguage.getPreferredLocale()));
	}

	private List<Order> _toOrders(List<CommerceOrder> commerceOrders)
		throws Exception {

		List<Order> orders = new ArrayList<>();

		for (CommerceOrder commerceOrder : commerceOrders) {
			orders.add(_toOrder(commerceOrder));
		}

		return orders;
	}

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference
	private CommerceChannelService _commerceChannelService;

	@Reference
	private CommerceOrderService _commerceOrderService;

	@Reference
	private OrderDTOConverter _orderDTOConverter;

}