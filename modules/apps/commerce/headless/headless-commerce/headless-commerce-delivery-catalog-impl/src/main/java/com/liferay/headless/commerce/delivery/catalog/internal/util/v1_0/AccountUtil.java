/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.internal.util.v1_0;

import com.liferay.account.exception.NoSuchEntryException;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountEntryService;
import com.liferay.commerce.helper.CommerceAccountHelper;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.permission.CommerceProductViewPermission;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import jakarta.ws.rs.core.MultivaluedMap;

/**
 * @author Brian I. Kim
 */
public class AccountUtil {

	public static void checkProductViewPermission(
			long companyId, long cpDefinitionId, long groupId, long userId,
			AccountEntryLocalService accountEntryLocalService,
			AccountEntryService accountEntryService,
			CommerceAccountHelper commerceAccountHelper,
			CommerceProductViewPermission commerceProductViewPermission,
			MultivaluedMap<String, String> queryParameters)
		throws PortalException {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if ((GetterUtil.getLong(queryParameters.getFirst("accountId")) > 0) ||
			(commerceAccountHelper.countUserCommerceAccounts(userId, groupId) <=
				1)) {

			commerceProductViewPermission.check(
				permissionChecker,
				getAccountId(
					companyId, groupId, userId, accountEntryLocalService,
					accountEntryService, null, commerceAccountHelper,
					queryParameters),
				groupId, cpDefinitionId);

			return;
		}

		boolean approved = false;

		for (long accountEntryId :
				commerceAccountHelper.getUserCommerceAccountIds(
					userId, groupId)) {

			AccountEntry accountEntry =
				accountEntryLocalService.getAccountEntry(accountEntryId);

			if (!accountEntry.isApproved()) {
				continue;
			}

			if (commerceProductViewPermission.contains(
					permissionChecker, accountEntryId, groupId,
					cpDefinitionId)) {

				return;
			}

			approved = true;
		}

		if (approved) {
			throw new PrincipalException.MustHavePermission(
				userId, CPDefinition.class.getName(), cpDefinitionId,
				ActionKeys.VIEW);
		}

		commerceProductViewPermission.check(
			permissionChecker,
			_getGuestAccountEntryId(companyId, accountEntryLocalService),
			groupId, cpDefinitionId);
	}

	public static long getAccountId(
			long companyId, long groupId, long userId,
			AccountEntryLocalService accountEntryLocalService,
			AccountEntryService accountEntryService, Long accountId,
			CommerceAccountHelper commerceAccountHelper,
			MultivaluedMap<String, String> queryParameters)
		throws PortalException {

		if ((accountId != null) && (accountId > 0)) {
			AccountEntry accountEntry = accountEntryService.fetchAccountEntry(
				accountId);

			if (accountEntry != null) {
				if (accountEntry.isApproved()) {
					return accountEntry.getAccountEntryId();
				}

				return _getGuestAccountEntryId(
					companyId, accountEntryLocalService);
			}
		}

		int count = commerceAccountHelper.countUserCommerceAccounts(
			userId, groupId);

		if ((count > 1) && (accountId == null)) {
			if (queryParameters != null) {
				String accountIdString = queryParameters.getFirst("accountId");

				if (Validator.isNotNull(accountIdString)) {
					long accountEntryId = GetterUtil.getLong(accountIdString);

					if (accountEntryId <= 0) {
						return accountEntryId;
					}

					AccountEntry accountEntry =
						accountEntryService.fetchAccountEntry(accountEntryId);

					if (accountEntry != null) {
						if (accountEntry.isApproved()) {
							return accountEntry.getAccountEntryId();
						}

						return _getGuestAccountEntryId(
							companyId, accountEntryLocalService);
					}
				}
			}

			throw new NoSuchEntryException();
		}

		for (long accountEntryId :
				commerceAccountHelper.getUserCommerceAccountIds(
					userId, groupId)) {

			AccountEntry accountEntry =
				accountEntryLocalService.getAccountEntry(accountEntryId);

			if (accountEntry.isApproved()) {
				return accountEntryId;
			}
		}

		return _getGuestAccountEntryId(companyId, accountEntryLocalService);
	}

	private static long _getGuestAccountEntryId(
			long companyId, AccountEntryLocalService accountEntryLocalService)
		throws PortalException {

		AccountEntry accountEntry =
			accountEntryLocalService.getGuestAccountEntry(companyId);

		return accountEntry.getAccountEntryId();
	}

}