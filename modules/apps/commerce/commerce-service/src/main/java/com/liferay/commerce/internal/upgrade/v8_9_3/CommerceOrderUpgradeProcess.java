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

package com.liferay.commerce.internal.upgrade.v8_9_3;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.order.status.CommerceOrderStatusRegistry;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Brian I. Kim
 */
public class CommerceOrderUpgradeProcess extends UpgradeProcess {

	public CommerceOrderUpgradeProcess(
		CommerceOrderLocalService commerceOrderLocalService,
		CommerceOrderStatusRegistry commerceOrderStatusRegistry) {

		_commerceOrderLocalService = commerceOrderLocalService;
		_commerceOrderStatusRegistry = commerceOrderStatusRegistry;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_updateCommerceOrder();
	}

	private void _updateCommerceOrder() throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select commerceOrderId, orderStatus from " +
				"CommerceOrder where orderStatus = 14 or orderStatus = 15 ");

			 ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				int orderStatus = resultSet.getInt(2);

				if (orderStatus ==
						CommerceOrderConstants.ORDER_STATUS_PARTIALLY_SHIPPED) {

					CommerceOrderStatus shippedCommerceOrderStatus =
						_commerceOrderStatusRegistry.getCommerceOrderStatus(
							CommerceOrderConstants.ORDER_STATUS_SHIPPED);

					CommerceOrder commerceOrder =
						_commerceOrderLocalService.getCommerceOrder(
							resultSet.getLong(1));

					if ((shippedCommerceOrderStatus != null) &&
						shippedCommerceOrderStatus.isTransitionCriteriaMet(
							commerceOrder)) {

						commerceOrder.setOrderStatus(
							CommerceOrderConstants.ORDER_STATUS_COMPLETED);

						_commerceOrderLocalService.updateCommerceOrder(
							commerceOrder);
					}
				}
				else if (orderStatus ==
							CommerceOrderConstants.ORDER_STATUS_SHIPPED) {

					CommerceOrder commerceOrder =
						_commerceOrderLocalService.getCommerceOrder(
							resultSet.getLong(1));

					commerceOrder.setOrderStatus(
						CommerceOrderConstants.ORDER_STATUS_COMPLETED);

					_commerceOrderLocalService.updateCommerceOrder(
						commerceOrder);
				}
			}
		}
	}

	private final CommerceOrderLocalService _commerceOrderLocalService;
	private final CommerceOrderStatusRegistry _commerceOrderStatusRegistry;

}