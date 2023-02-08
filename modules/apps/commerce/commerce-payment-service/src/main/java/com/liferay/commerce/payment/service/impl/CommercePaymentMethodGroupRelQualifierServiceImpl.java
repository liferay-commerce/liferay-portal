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

package com.liferay.commerce.payment.service.impl;

import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRelQualifier;
import com.liferay.commerce.payment.service.base.CommercePaymentMethodGroupRelQualifierServiceBaseImpl;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luca Pellizzon
 */
@Component(
	property = {
		"json.web.service.context.name=commerce",
		"json.web.service.context.path=CommercePaymentMethodGroupRelQualifier"
	},
	service = AopService.class
)
public class CommercePaymentMethodGroupRelQualifierServiceImpl
	extends CommercePaymentMethodGroupRelQualifierServiceBaseImpl {

	@Override
	public CommercePaymentMethodGroupRelQualifier
			addCommercePaymentMethodGroupRelQualifier(
				String className, long classPK,
				long commercePaymentMethodGroupRelId)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.UPDATE);

		return commercePaymentMethodGroupRelQualifierLocalService.
			addCommercePaymentMethodGroupRelQualifier(
				getUserId(), className, classPK,
				commercePaymentMethodGroupRelId);
	}

	@Override
	public void deleteCommercePaymentMethodGroupRelQualifier(
			long commercePaymentMethodGroupRelQualifierId)
		throws PortalException {

		CommercePaymentMethodGroupRelQualifier
			commercePaymentMethodGroupRelQualifier =
				commercePaymentMethodGroupRelQualifierLocalService.
					getCommercePaymentMethodGroupRelQualifier(
						commercePaymentMethodGroupRelQualifierId);

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelQualifier.
				getCommercePaymentMethodGroupRelId(),
			ActionKeys.DELETE);

		commercePaymentMethodGroupRelQualifierLocalService.
			deleteCommercePaymentMethodGroupRelQualifier(
				commercePaymentMethodGroupRelQualifier);
	}

	@Override
	public void deleteCommercePaymentMethodGroupRelQualifiers(
			String className, long commercePaymentMethodGroupRelId)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.DELETE);

		commercePaymentMethodGroupRelQualifierLocalService.
			deleteCommercePaymentMethodGroupRelQualifiers(
				className, commercePaymentMethodGroupRelId);
	}

	@Override
	public void
			deleteCommercePaymentMethodGroupRelQualifiersByCommercePaymentMethodGroupRelId(
				long commercePaymentMethodGroupRelId)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.DELETE);

		commercePaymentMethodGroupRelQualifierLocalService.
			deleteCommercePaymentMethodGroupRelQualifiers(
				commercePaymentMethodGroupRelId);
	}

	@Override
	public CommercePaymentMethodGroupRelQualifier
			fetchCommercePaymentMethodGroupRelQualifier(
				String className, long classPK,
				long commercePaymentMethodGroupRelId)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.VIEW);

		return commercePaymentMethodGroupRelQualifierLocalService.
			fetchCommercePaymentMethodGroupRelQualifier(
				className, classPK, commercePaymentMethodGroupRelId);
	}

	@Override
	public List<CommercePaymentMethodGroupRelQualifier>
			getCommerceOrderTypeCommercePaymentMethodGroupRelQualifiers(
				long commercePaymentMethodGroupRelId, String keywords,
				int start, int end)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.VIEW);

		return commercePaymentMethodGroupRelQualifierLocalService.
			getCommerceOrderTypeCommercePaymentMethodGroupRelQualifiers(
				commercePaymentMethodGroupRelId, keywords, start, end);
	}

	@Override
	public int getCommerceOrderTypeCommercePaymentMethodGroupRelQualifiersCount(
			long commercePaymentMethodGroupRelId, String keywords)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.VIEW);

		return commercePaymentMethodGroupRelQualifierLocalService.
			getCommerceOrderTypeCommercePaymentMethodGroupRelQualifiersCount(
				commercePaymentMethodGroupRelId, keywords);
	}

	@Override
	public CommercePaymentMethodGroupRelQualifier
			getCommercePaymentMethodGroupRelQualifier(
				long commercePaymentMethodGroupRelQualifierId)
		throws PortalException {

		CommercePaymentMethodGroupRelQualifier
			commercePaymentMethodGroupRelQualifier =
				commercePaymentMethodGroupRelQualifierLocalService.
					getCommercePaymentMethodGroupRelQualifier(
						commercePaymentMethodGroupRelQualifierId);

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelQualifier.
				getCommercePaymentMethodGroupRelId(),
			ActionKeys.VIEW);

		return commercePaymentMethodGroupRelQualifier;
	}

	@Override
	public List<CommercePaymentMethodGroupRelQualifier>
			getCommercePaymentMethodGroupRelQualifiers(
				long commercePaymentMethodGroupRelId, int start, int end,
				OrderByComparator<CommercePaymentMethodGroupRelQualifier>
					orderByComparator)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.VIEW);

		return commercePaymentMethodGroupRelQualifierLocalService.
			getCommercePaymentMethodGroupRelQualifiers(
				commercePaymentMethodGroupRelId, start, end, orderByComparator);
	}

	@Override
	public List<CommercePaymentMethodGroupRelQualifier>
			getCommercePaymentMethodGroupRelQualifiers(
				String className, long commercePaymentMethodGroupRelId)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.VIEW);

		return commercePaymentMethodGroupRelQualifierLocalService.
			getCommercePaymentMethodGroupRelQualifiers(
				className, commercePaymentMethodGroupRelId);
	}

	@Override
	public int getCommercePaymentMethodGroupRelQualifiersCount(
			long commercePaymentMethodGroupRelId)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.VIEW);

		return commercePaymentMethodGroupRelQualifierLocalService.
			getCommercePaymentMethodGroupRelQualifiersCount(
				commercePaymentMethodGroupRelId);
	}

	@Override
	public List<CommercePaymentMethodGroupRelQualifier>
			getCommerceTermEntryCommercePaymentMethodGroupRelQualifiers(
				long commercePaymentMethodGroupRelId, String keywords,
				int start, int end)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.VIEW);

		return commercePaymentMethodGroupRelQualifierLocalService.
			getCommerceTermEntryCommercePaymentMethodGroupRelQualifiers(
				commercePaymentMethodGroupRelId, keywords, start, end);
	}

	@Override
	public int getCommerceTermEntryCommercePaymentMethodGroupRelQualifiersCount(
			long commercePaymentMethodGroupRelId, String keywords)
		throws PortalException {

		_checkCommercePaymentMethodGroupRel(
			commercePaymentMethodGroupRelId, ActionKeys.VIEW);

		return commercePaymentMethodGroupRelQualifierLocalService.
			getCommerceTermEntryCommercePaymentMethodGroupRelQualifiersCount(
				commercePaymentMethodGroupRelId, keywords);
	}

	private void _checkCommercePaymentMethodGroupRel(
			long commercePaymentMethodGroupRelId, String actionId)
		throws PortalException {

		_commercePaymentMethodGroupRelModelResourcePermission.check(
			getPermissionChecker(), commercePaymentMethodGroupRelId, actionId);
	}

	@Reference(
		target = "(model.class.name=com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel)"
	)
	private ModelResourcePermission<CommercePaymentMethodGroupRel>
		_commercePaymentMethodGroupRelModelResourcePermission;

}