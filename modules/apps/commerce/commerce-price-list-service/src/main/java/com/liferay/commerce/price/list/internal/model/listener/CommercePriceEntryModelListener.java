/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.price.list.internal.model.listener;

import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.context.CommerceContextFactory;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.price.list.model.CommercePriceEntry;
import com.liferay.commerce.price.list.service.CommerceTierPriceEntryLocalService;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.service.CPInstanceLocalService;
import com.liferay.commerce.service.CommerceOrderItemLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Dante Wang
 */
@Component(service = ModelListener.class)
public class CommercePriceEntryModelListener
	extends BaseModelListener<CommercePriceEntry> {

	@Override
	public void onAfterUpdate(
		CommercePriceEntry originalCommercePriceEntry,
		CommercePriceEntry commercePriceEntry) {

		try {
			CPInstance cpInstance = _cpInstanceLocalService.fetchCPInstance(
				commercePriceEntry.getCProductId(),
				commercePriceEntry.getCPInstanceUuid());

			if (cpInstance == null) {
				return;
			}

			List<CommerceOrderItem> commerceOrderItems =
				_commerceOrderItemLocalService.getCommerceOrderItems(
					cpInstance.getCPInstanceId(),
					new int[] {CommerceOrderConstants.ORDER_STATUS_OPEN},
					commercePriceEntry.getUnitOfMeasureKey(), QueryUtil.ALL_POS,
					QueryUtil.ALL_POS);

			List<Long> commerceOrderIds = new ArrayList<>();

			for (CommerceOrderItem commerceOrderItem : commerceOrderItems) {
				CommerceOrder commerceOrder =
					commerceOrderItem.getCommerceOrder();

				if (!commerceOrderIds.isEmpty() &&
					commerceOrderIds.contains(
						commerceOrder.getCommerceOrderId())) {

					continue;
				}

				CommerceContext commerceContext =
					_commerceContextFactory.create(
						commerceOrder.getCommerceAccountId(),
						commerceOrder.getGroupId(),
						commerceOrder.getCommerceCurrencyCode(),
						commerceOrder.getCommerceOrderId(),
						commerceOrder.getCompanyId());

				_commerceOrderLocalService.recalculatePrice(
					commerceOrder.getCommerceOrderId(), commerceContext);

				commerceOrderIds.add(commerceOrder.getCommerceOrderId());
			}
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}
		}
	}

	@Override
	public void onBeforeRemove(CommercePriceEntry commercePriceEntry) {
		try {
			_commerceTierPriceEntryLocalService.deleteCommerceTierPriceEntries(
				commercePriceEntry.getCommercePriceEntryId());
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommercePriceEntryModelListener.class);

	@Reference
	private CommerceContextFactory _commerceContextFactory;

	@Reference
	private CommerceOrderItemLocalService _commerceOrderItemLocalService;

	@Reference
	private CommerceOrderLocalService _commerceOrderLocalService;

	@Reference
	private CommerceTierPriceEntryLocalService
		_commerceTierPriceEntryLocalService;

	@Reference
	private CPInstanceLocalService _cpInstanceLocalService;

}