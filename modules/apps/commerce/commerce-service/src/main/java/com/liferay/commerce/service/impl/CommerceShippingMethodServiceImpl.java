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

package com.liferay.commerce.service.impl;

import com.liferay.commerce.constants.CommerceActionKeys;
import com.liferay.commerce.model.CommerceAddressRestriction;
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.service.CommerceAddressRestrictionLocalService;
import com.liferay.commerce.service.base.CommerceShippingMethodServiceBaseImpl;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.File;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Di Giorgi
 * @author Alessio Antonio Rendina
 */
@Component(
	property = {
		"json.web.service.context.name=commerce",
		"json.web.service.context.path=CommerceShippingMethod"
	},
	service = AopService.class
)
public class CommerceShippingMethodServiceImpl
	extends CommerceShippingMethodServiceBaseImpl {

	@Override
	public CommerceAddressRestriction addCommerceAddressRestriction(
			long groupId, long commerceShippingMethodId, long countryId)
		throws PortalException {

		_checkCommerceChannel(groupId, ActionKeys.UPDATE);

		return commerceShippingMethodLocalService.addCommerceAddressRestriction(
			getUserId(), groupId, commerceShippingMethodId, countryId);
	}

	@Override
	public CommerceShippingMethod addCommerceShippingMethod(
			long groupId, Map<Locale, String> nameMap,
			Map<Locale, String> descriptionMap, boolean active,
			String engineKey, File imageFile, double priority,
			String trackingURL)
		throws PortalException {

		PortletResourcePermission portletResourcePermission =
			_commerceChannelModelResourcePermission.
				getPortletResourcePermission();

		portletResourcePermission.check(
			getPermissionChecker(), groupId,
			CommerceActionKeys.MANAGE_COMMERCE_SHIPPING_METHOD);

		return commerceShippingMethodLocalService.addCommerceShippingMethod(
			getUserId(), groupId, nameMap, descriptionMap, active, engineKey,
			imageFile, priority, trackingURL);
	}

	@Override
	public void deleteCommerceAddressRestriction(
			long commerceAddressRestrictionId)
		throws PortalException {

		CommerceAddressRestriction commerceAddressRestriction =
			_commerceAddressRestrictionLocalService.
				getCommerceAddressRestriction(commerceAddressRestrictionId);

		_checkCommerceChannel(
			commerceAddressRestriction.getGroupId(), ActionKeys.UPDATE);

		commerceShippingMethodLocalService.deleteCommerceAddressRestriction(
			commerceAddressRestrictionId);
	}

	@Override
	public void deleteCommerceAddressRestrictions(long commerceShippingMethodId)
		throws PortalException {

		CommerceShippingMethod commerceShippingMethod =
			commerceShippingMethodLocalService.getCommerceShippingMethod(
				commerceShippingMethodId);

		_checkCommerceChannel(
			commerceShippingMethod.getGroupId(), ActionKeys.UPDATE);

		_commerceAddressRestrictionLocalService.
			deleteCommerceAddressRestrictions(
				CommerceShippingMethod.class.getName(),
				commerceShippingMethodId);
	}

	@Override
	public void deleteCommerceShippingMethod(long commerceShippingMethodId)
		throws PortalException {

		_commerceShippingMethodModelResourcePermission.check(
			getPermissionChecker(), commerceShippingMethodId,
			ActionKeys.DELETE);

		commerceShippingMethodLocalService.deleteCommerceShippingMethod(
			commerceShippingMethodId);
	}

	@Override
	public CommerceShippingMethod fetchCommerceShippingMethod(
			long groupId, String engineKey)
		throws PortalException {

		CommerceShippingMethod commerceShippingMethod =
			commerceShippingMethodLocalService.fetchCommerceShippingMethod(
				groupId, engineKey);

		if (commerceShippingMethod != null) {
			_commerceShippingMethodModelResourcePermission.check(
				getPermissionChecker(), commerceShippingMethod,
				ActionKeys.VIEW);
		}

		return commerceShippingMethod;
	}

	@Override
	public List<CommerceAddressRestriction> getCommerceAddressRestrictions(
			long commerceShippingMethodId, int start, int end,
			OrderByComparator<CommerceAddressRestriction> orderByComparator)
		throws PortalException {

		CommerceShippingMethod commerceShippingMethod =
			commerceShippingMethodLocalService.getCommerceShippingMethod(
				commerceShippingMethodId);

		_checkCommerceChannel(
			commerceShippingMethod.getGroupId(), ActionKeys.UPDATE);

		return commerceShippingMethodLocalService.
			getCommerceAddressRestrictions(
				commerceShippingMethodId, start, end, orderByComparator);
	}

	@Override
	public int getCommerceAddressRestrictionsCount(
			long commerceShippingMethodId)
		throws PortalException {

		CommerceShippingMethod commerceShippingMethod =
			commerceShippingMethodLocalService.getCommerceShippingMethod(
				commerceShippingMethodId);

		_checkCommerceChannel(
			commerceShippingMethod.getGroupId(), ActionKeys.UPDATE);

		return commerceShippingMethodLocalService.
			getCommerceAddressRestrictionsCount(commerceShippingMethodId);
	}

	@Override
	public CommerceShippingMethod getCommerceShippingMethod(
			long commerceShippingMethodId)
		throws PortalException {

		_commerceShippingMethodModelResourcePermission.check(
			getPermissionChecker(), commerceShippingMethodId, ActionKeys.VIEW);

		return commerceShippingMethodLocalService.getCommerceShippingMethod(
			commerceShippingMethodId);
	}

	@Override
	public List<CommerceShippingMethod> getCommerceShippingMethods(
			long groupId, boolean active, int start, int end,
			OrderByComparator<CommerceShippingMethod> orderByComparator)
		throws PortalException {

		return commerceShippingMethodPersistence.filterFindByG_A(
			groupId, active, start, end, orderByComparator);
	}

	@Override
	public List<CommerceShippingMethod> getCommerceShippingMethods(
			long groupId, int start, int end,
			OrderByComparator<CommerceShippingMethod> orderByComparator)
		throws PortalException {

		return commerceShippingMethodPersistence.filterFindByGroupId(
			groupId, start, end, orderByComparator);
	}

	@Override
	public List<CommerceShippingMethod> getCommerceShippingMethods(
			long groupId, long countryId, boolean active)
		throws PortalException {

		return ListUtil.filter(
			commerceShippingMethodPersistence.filterFindByG_A(groupId, active),
			commerceShippingMethod ->
				!_commerceAddressRestrictionLocalService.
					isCommerceAddressRestricted(
						CommerceShippingMethod.class.getName(),
						commerceShippingMethod.getCommerceShippingMethodId(),
						countryId));
	}

	@Override
	public int getCommerceShippingMethodsCount(long groupId)
		throws PortalException {

		return commerceShippingMethodPersistence.filterCountByGroupId(groupId);
	}

	@Override
	public int getCommerceShippingMethodsCount(long groupId, boolean active)
		throws PortalException {

		return commerceShippingMethodPersistence.filterCountByG_A(
			groupId, active);
	}

	@Override
	public CommerceShippingMethod setActive(
			long commerceShippingMethodId, boolean active)
		throws PortalException {

		_commerceShippingMethodModelResourcePermission.check(
			getPermissionChecker(), commerceShippingMethodId,
			ActionKeys.UPDATE);

		return commerceShippingMethodLocalService.setActive(
			commerceShippingMethodId, active);
	}

	@Override
	public CommerceShippingMethod updateCommerceShippingMethod(
			long commerceShippingMethodId, Map<Locale, String> nameMap,
			Map<Locale, String> descriptionMap, boolean active, File imageFile,
			double priority, String trackingURL)
		throws PortalException {

		_commerceShippingMethodModelResourcePermission.check(
			getPermissionChecker(), commerceShippingMethodId,
			ActionKeys.UPDATE);

		return commerceShippingMethodLocalService.updateCommerceShippingMethod(
			commerceShippingMethodId, nameMap, descriptionMap, active,
			imageFile, priority, trackingURL);
	}

	private void _checkCommerceChannel(long groupId, String actionId)
		throws PortalException {

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.getCommerceChannelByGroupId(groupId);

		_commerceChannelModelResourcePermission.check(
			getPermissionChecker(), commerceChannel, actionId);
	}

	@Reference
	private CommerceAddressRestrictionLocalService
		_commerceAddressRestrictionLocalService;

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.commerce.product.model.CommerceChannel)"
	)
	private ModelResourcePermission<CommerceChannel>
		_commerceChannelModelResourcePermission;

	@Reference(
		target = "(model.class.name=com.liferay.commerce.model.CommerceShippingMethod)"
	)
	private ModelResourcePermission<CommerceShippingMethod>
		_commerceShippingMethodModelResourcePermission;

}