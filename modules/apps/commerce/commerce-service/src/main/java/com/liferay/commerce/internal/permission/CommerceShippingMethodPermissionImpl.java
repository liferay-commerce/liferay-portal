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

package com.liferay.commerce.internal.permission;

import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.permission.CommerceShippingMethodPermission;
import com.liferay.commerce.service.CommerceShippingMethodLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.ArrayUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Crescenzo Rega
 */
@Component(service = CommerceShippingMethodPermission.class)
public class CommerceShippingMethodPermissionImpl
	implements CommerceShippingMethodPermission {

	@Override
	public void check(
			PermissionChecker permissionChecker,
			CommerceShippingMethod commerceShippingMethod, String actionId)
		throws PortalException {

		if (!contains(permissionChecker, commerceShippingMethod, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, CommerceShippingMethod.class.getName(),
				commerceShippingMethod.getCommerceShippingMethodId(), actionId);
		}
	}

	@Override
	public void check(
			PermissionChecker permissionChecker, long commerceShippingMethodId,
			String actionId)
		throws PortalException {

		if (!contains(permissionChecker, commerceShippingMethodId, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, CommerceShippingMethod.class.getName(),
				commerceShippingMethodId, actionId);
		}
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker,
			CommerceShippingMethod commerceShippingMethod, String actionId)
		throws PortalException {

		if (contains(
				permissionChecker,
				commerceShippingMethod.getCommerceShippingMethodId(),
				actionId)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, long commerceShippingMethodId,
			String actionId)
		throws PortalException {

		CommerceShippingMethod commerceShippingMethod =
			_commerceShippingMethodLocalService.fetchCommerceShippingMethod(
				commerceShippingMethodId);

		if (commerceShippingMethod == null) {
			return false;
		}

		return _contains(permissionChecker, commerceShippingMethod, actionId);
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker,
			long[] commerceShippingMethodIds, String actionId)
		throws PortalException {

		if (ArrayUtil.isEmpty(commerceShippingMethodIds)) {
			return false;
		}

		for (long commerceShippingMethodId : commerceShippingMethodIds) {
			if (!contains(
					permissionChecker, commerceShippingMethodId, actionId)) {

				return false;
			}
		}

		return true;
	}

	private boolean _contains(
		PermissionChecker permissionChecker,
		CommerceShippingMethod commerceShippingMethod, String actionId) {

		if (permissionChecker.isCompanyAdmin(
				commerceShippingMethod.getCompanyId()) ||
			permissionChecker.isOmniadmin() ||
			permissionChecker.hasOwnerPermission(
				commerceShippingMethod.getCompanyId(),
				CommerceShippingMethod.class.getName(),
				commerceShippingMethod.getCommerceShippingMethodId(),
				commerceShippingMethod.getUserId(), actionId)) {

			return true;
		}

		return permissionChecker.hasPermission(
			null, CommerceShippingMethod.class.getName(),
			commerceShippingMethod.getCommerceShippingMethodId(), actionId);
	}

	@Reference
	private CommerceShippingMethodLocalService
		_commerceShippingMethodLocalService;

}