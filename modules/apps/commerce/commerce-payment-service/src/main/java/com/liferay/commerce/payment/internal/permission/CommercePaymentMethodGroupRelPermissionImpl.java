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

package com.liferay.commerce.payment.internal.permission;

import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.permission.CommercePaymentMethodGroupRelPermission;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.ArrayUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Crescenzo Rega
 */
@Component(service = CommercePaymentMethodGroupRelPermission.class)
public class CommercePaymentMethodGroupRelPermissionImpl
	implements CommercePaymentMethodGroupRelPermission {

	@Override
	public void check(
			PermissionChecker permissionChecker,
			CommercePaymentMethodGroupRel commercePaymentMethodGroupRel,
			String actionId)
		throws PortalException {

		if (!contains(
				permissionChecker, commercePaymentMethodGroupRel, actionId)) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker,
				CommercePaymentMethodGroupRel.class.getName(),
				commercePaymentMethodGroupRel.
					getCommercePaymentMethodGroupRelId(),
				actionId);
		}
	}

	@Override
	public void check(
			PermissionChecker permissionChecker,
			long commercePaymentMethodGroupRelId, String actionId)
		throws PortalException {

		if (!contains(
				permissionChecker, commercePaymentMethodGroupRelId, actionId)) {

			throw new PrincipalException.MustHavePermission(
				permissionChecker,
				CommercePaymentMethodGroupRel.class.getName(),
				commercePaymentMethodGroupRelId, actionId);
		}
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker,
			CommercePaymentMethodGroupRel commercePaymentMethodGroupRel,
			String actionId)
		throws PortalException {

		if (contains(
				permissionChecker,
				commercePaymentMethodGroupRel.
					getCommercePaymentMethodGroupRelId(),
				actionId)) {

			return true;
		}

		return false;
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker,
			long commercePaymentMethodGroupRelId, String actionId)
		throws PortalException {

		CommercePaymentMethodGroupRel commercePaymentMethodGroupRel =
			_commercePaymentMethodGroupRelLocalService.
				fetchCommercePaymentMethodGroupRel(
					commercePaymentMethodGroupRelId);

		if (commercePaymentMethodGroupRel == null) {
			return false;
		}

		return _contains(
			permissionChecker, commercePaymentMethodGroupRel, actionId);
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker,
			long[] commercePaymentMethodGroupRelIds, String actionId)
		throws PortalException {

		if (ArrayUtil.isEmpty(commercePaymentMethodGroupRelIds)) {
			return false;
		}

		for (long commercePaymentMethodGroupRelId :
				commercePaymentMethodGroupRelIds) {

			if (!contains(
					permissionChecker, commercePaymentMethodGroupRelId,
					actionId)) {

				return false;
			}
		}

		return true;
	}

	private boolean _contains(
			PermissionChecker permissionChecker,
			CommercePaymentMethodGroupRel commercePaymentMethodGroupRel,
			String actionId)
		throws PortalException {

		if (permissionChecker.isCompanyAdmin(
				commercePaymentMethodGroupRel.getCompanyId()) ||
			permissionChecker.hasOwnerPermission(
				commercePaymentMethodGroupRel.getCompanyId(),
				CommercePaymentMethodGroupRel.class.getName(),
				commercePaymentMethodGroupRel.
					getCommercePaymentMethodGroupRelId(),
				commercePaymentMethodGroupRel.getUserId(), actionId)) {

			return true;
		}

		return permissionChecker.hasPermission(
			commercePaymentMethodGroupRel.getGroupId(),
			CommercePaymentMethodGroupRel.class.getName(),
			commercePaymentMethodGroupRel.getCommercePaymentMethodGroupRelId(),
			actionId);
	}

	@Reference
	private CommercePaymentMethodGroupRelLocalService
		_commercePaymentMethodGroupRelLocalService;

}