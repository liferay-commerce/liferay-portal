/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.order.internal.resource.v1_0;

import com.liferay.commerce.exception.CommerceOrderStatusException;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Term;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.TermResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Andrea Sbarra
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/term.properties",
	scope = ServiceScope.PROTOTYPE, service = TermResource.class
)
public class TermResourceImpl extends BaseTermResourceImpl {

	@Override
	public Term getPlacedOrderDeliveryTerm(Long placedOrderId)
		throws Exception {

		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			placedOrderId);

		if (commerceOrder.isOpen()) {
			throw new CommerceOrderStatusException(
				"Unable to get delivery term of an open order");
		}

		return new Term() {
			{
				setDescription(
					commerceOrder::getDeliveryCommerceTermEntryDescription);
				setId(commerceOrder::getDeliveryCommerceTermEntryId);
				setName(commerceOrder::getDeliveryCommerceTermEntryName);
			}
		};
	}

	@Override
	public Term getPlacedOrderPaymentTerm(Long placedOrderId) throws Exception {
		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			placedOrderId);

		if (commerceOrder.isOpen()) {
			throw new CommerceOrderStatusException(
				"Unable to get payment term of an open order");
		}

		return new Term() {
			{
				setDescription(
					commerceOrder::getPaymentCommerceTermEntryDescription);
				setId(commerceOrder::getPaymentCommerceTermEntryId);
				setName(commerceOrder::getPaymentCommerceTermEntryName);
			}
		};
	}

	@Reference
	private CommerceOrderService _commerceOrderService;

}