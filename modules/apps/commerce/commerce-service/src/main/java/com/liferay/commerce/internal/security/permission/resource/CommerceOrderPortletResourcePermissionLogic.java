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

package com.liferay.commerce.internal.security.permission.resource;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.constants.AccountRoleConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.commerce.context.CommerceContextThreadLocal;
import com.liferay.commerce.context.CommerceGroupThreadLocal;
import com.liferay.commerce.internal.util.AccountEntryUtil;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermissionLogic;
import com.liferay.portal.kernel.service.RoleLocalService;

import java.util.List;

/**
 * @author Riccardo Alberti
 */
public class CommerceOrderPortletResourcePermissionLogic
	implements PortletResourcePermissionLogic {

	public CommerceOrderPortletResourcePermissionLogic(
		AccountEntryLocalService accountEntryLocalService,
		CommerceChannelLocalService commerceChannelLocalService,
		RoleLocalService roleLocalService) {

		_accountEntryLocalService = accountEntryLocalService;
		_commerceChannelLocalService = commerceChannelLocalService;
		_roleLocalService = roleLocalService;
	}

	@Override
	public Boolean contains(
		PermissionChecker permissionChecker, String name, Group group,
		String actionId) {

		if (permissionChecker.hasPermission(group, name, 0, actionId)) {
			return true;
		}

		try {
			return _hasSupplierPermission(permissionChecker);
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		return false;
	}

	private boolean _hasSupplierAccount(
			PermissionChecker permissionChecker,
			CommerceChannel commerceChannel)
		throws PortalException {

		List<AccountEntry> accountEntries =
			_accountEntryLocalService.getUserAccountEntries(
				permissionChecker.getUserId(), 0L, StringPool.BLANK,
				new String[] {AccountConstants.ACCOUNT_ENTRY_TYPE_SUPPLIER},
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (AccountEntry accountEntry : accountEntries) {
			if (commerceChannel.getAccountEntryId() ==
					accountEntry.getAccountEntryId()) {

				return true;
			}
		}

		return false;
	}

	private boolean _hasSupplierPermission(PermissionChecker permissionChecker)
		throws PortalException {

		if (!_hasSupplierRole(permissionChecker)) {
			return false;
		}

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.fetchCommerceChannel(
				AccountEntryUtil.getCommerceChannelId(
					CommerceContextThreadLocal.get(),
					CommerceGroupThreadLocal.get()));

		if ((commerceChannel != null) &&
			(commerceChannel.getAccountEntryId() > 0) &&
			_hasSupplierAccount(permissionChecker, commerceChannel)) {

			return true;
		}

		return false;
	}

	private boolean _hasSupplierRole(PermissionChecker permissionChecker)
		throws PortalException {

		return _roleLocalService.hasUserRole(
			permissionChecker.getUserId(), permissionChecker.getCompanyId(),
			AccountRoleConstants.ROLE_NAME_SUPPLIER, true);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceOrderPortletResourcePermissionLogic.class);

	private final AccountEntryLocalService _accountEntryLocalService;
	private final CommerceChannelLocalService _commerceChannelLocalService;
	private final RoleLocalService _roleLocalService;

}