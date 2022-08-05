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

package com.liferay.commerce.account.service.impl;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountEntryLocalServiceWrapper;
import com.liferay.commerce.product.constants.CommerceChannelAccountEntryRelConstants;
import com.liferay.commerce.product.model.CommerceChannelAccountEntryRel;
import com.liferay.commerce.product.service.CommerceChannelAccountEntryRelLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.service.ServiceWrapper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alessio Antonio Rendina
 */
@Component(enabled = false, immediate = true, service = ServiceWrapper.class)
public class AccountEntryLocalServiceImpl
	extends AccountEntryLocalServiceWrapper {

	@Override
	public AccountEntry updateDefaultBillingAddressId(
			long accountEntryId, long addressId)
		throws PortalException {

		AccountEntry accountEntry = super.updateDefaultBillingAddressId(
			accountEntryId, addressId);

		if (addressId == 0) {
			_commerceChannelAccountEntryRelLocalService.
				deleteCommerceChannelAccountEntryRelsByCommerceChannelId(0);
		}
		else {
			CommerceChannelAccountEntryRel commerceChannelAccountEntryRel =
				_commerceChannelAccountEntryRelLocalService.
					fetchCommerceChannelAccountEntryRel(
						accountEntry.getAccountEntryId(),
						Address.class.getName(), addressId, 0,
						CommerceChannelAccountEntryRelConstants.
							TYPE_BILLING_ADDRESS);

			if (commerceChannelAccountEntryRel != null) {
				_commerceChannelAccountEntryRelLocalService.
					deleteCommerceChannelAccountEntryRelsByCommerceChannelId(0);
			}

			_commerceChannelAccountEntryRelLocalService.
				addCommerceChannelAccountEntryRel(
					accountEntry.getUserId(), accountEntry.getAccountEntryId(),
					Address.class.getName(), addressId, 0, false, 0,
					CommerceChannelAccountEntryRelConstants.
						TYPE_BILLING_ADDRESS);
		}

		return accountEntry;
	}

	@Override
	public AccountEntry updateDefaultShippingAddressId(
			long accountEntryId, long addressId)
		throws PortalException {

		AccountEntry accountEntry = super.updateDefaultShippingAddressId(
			accountEntryId, addressId);

		if (addressId == 0) {
			_commerceChannelAccountEntryRelLocalService.
				deleteCommerceChannelAccountEntryRelsByCommerceChannelId(0);
		}
		else {
			CommerceChannelAccountEntryRel commerceChannelAccountEntryRel =
				_commerceChannelAccountEntryRelLocalService.
					fetchCommerceChannelAccountEntryRel(
						accountEntry.getAccountEntryId(),
						Address.class.getName(), addressId, 0,
						CommerceChannelAccountEntryRelConstants.
							TYPE_SHIPPING_ADDRESS);

			if (commerceChannelAccountEntryRel != null) {
				_commerceChannelAccountEntryRelLocalService.
					deleteCommerceChannelAccountEntryRelsByCommerceChannelId(0);
			}

			_commerceChannelAccountEntryRelLocalService.
				addCommerceChannelAccountEntryRel(
					accountEntry.getUserId(), accountEntry.getAccountEntryId(),
					Address.class.getName(), addressId, 0, false, 0,
					CommerceChannelAccountEntryRelConstants.
						TYPE_SHIPPING_ADDRESS);
		}

		return accountEntry;
	}

	@Reference(unbind = "-")
	private void _serviceSetter(
		AccountEntryLocalService accountEntryLocalService) {

		setWrappedService(accountEntryLocalService);
	}

	@Reference
	private CommerceChannelAccountEntryRelLocalService
		_commerceChannelAccountEntryRelLocalService;

}