/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.internal.validator.helper;

import com.liferay.commerce.discount.model.CommerceDiscountUsageEntry;
import com.liferay.commerce.discount.service.CommerceDiscountUsageEntryLocalService;
import com.liferay.commerce.discount.validator.helper.CommerceDiscountValidatorHelper;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Company;

import java.util.List;

/**
 * @author Marco Leo
 */
public class PortalInstanceLifecycleListenerImpl
	extends BasePortalInstanceLifecycleListener {

	public PortalInstanceLifecycleListenerImpl(
		CommerceDiscountUsageEntryLocalService
			commerceDiscountUsageEntryLocalService,
		CommerceDiscountValidatorHelper commerceDiscountValidatorHelper) {

		_commerceDiscountUsageEntryLocalService =
			commerceDiscountUsageEntryLocalService;
		_commerceDiscountValidatorHelper = commerceDiscountValidatorHelper;
	}

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		List<CommerceDiscountUsageEntry> commerceDiscountUsageEntries =
			_commerceDiscountUsageEntryLocalService.
				getCommerceDiscountUsageEntries(
					QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (CommerceDiscountUsageEntry commerceDiscountUsageEntry :
				commerceDiscountUsageEntries) {

			_commerceDiscountValidatorHelper.incrementUsage(
				commerceDiscountUsageEntry.getCommerceDiscountId());
		}
	}

	private final CommerceDiscountUsageEntryLocalService
		_commerceDiscountUsageEntryLocalService;
	private final CommerceDiscountValidatorHelper
		_commerceDiscountValidatorHelper;

}