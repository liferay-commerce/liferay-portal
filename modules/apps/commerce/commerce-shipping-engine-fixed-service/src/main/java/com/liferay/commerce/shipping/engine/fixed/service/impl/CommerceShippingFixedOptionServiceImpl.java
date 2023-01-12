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

package com.liferay.commerce.shipping.engine.fixed.service.impl;

import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOption;
import com.liferay.commerce.shipping.engine.fixed.service.base.CommerceShippingFixedOptionServiceBaseImpl;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.math.BigDecimal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	property = {
		"json.web.service.context.name=commerce",
		"json.web.service.context.path=CommerceShippingFixedOption"
	},
	service = AopService.class
)
public class CommerceShippingFixedOptionServiceImpl
	extends CommerceShippingFixedOptionServiceBaseImpl {

	@Override
	public CommerceShippingFixedOption addCommerceShippingFixedOption(
			long groupId, long commerceShippingMethodId, BigDecimal amount,
			Map<Locale, String> descriptionMap, String key,
			Map<Locale, String> nameMap, double priority)
		throws PortalException {

		_commerceShippingMethodModelResourcePermission.check(
			getPermissionChecker(), commerceShippingMethodId,
			ActionKeys.UPDATE);

		return commerceShippingFixedOptionLocalService.
			addCommerceShippingFixedOption(
				getUserId(), groupId, commerceShippingMethodId, amount,
				descriptionMap, key, nameMap, priority);
	}

	@Override
	public void deleteCommerceShippingFixedOption(
			long commerceShippingFixedOptionId)
		throws PortalException {

		CommerceShippingFixedOption commerceShippingFixedOption =
			commerceShippingFixedOptionLocalService.
				getCommerceShippingFixedOption(commerceShippingFixedOptionId);

		_commerceShippingMethodModelResourcePermission.check(
			getPermissionChecker(),
			commerceShippingFixedOption.getCommerceShippingMethodId(),
			ActionKeys.DELETE);

		commerceShippingFixedOptionLocalService.
			deleteCommerceShippingFixedOption(commerceShippingFixedOption);
	}

	@Override
	public CommerceShippingFixedOption fetchCommerceShippingFixedOption(
			long commerceShippingFixedOptionId)
		throws PortalException {

		CommerceShippingFixedOption commerceShippingFixedOption =
			commerceShippingFixedOptionLocalService.
				fetchCommerceShippingFixedOption(commerceShippingFixedOptionId);

		if (commerceShippingFixedOption != null) {
			_commerceShippingMethodModelResourcePermission.check(
				getPermissionChecker(),
				commerceShippingFixedOption.getCommerceShippingMethodId(),
				ActionKeys.VIEW);
		}

		return commerceShippingFixedOption;
	}

	@Override
	public CommerceShippingFixedOption fetchCommerceShippingFixedOption(
			long companyId, String key)
		throws PortalException {

		CommerceShippingFixedOption commerceShippingFixedOption =
			commerceShippingFixedOptionLocalService.
				fetchCommerceShippingFixedOption(companyId, key);

		if (commerceShippingFixedOption != null) {
			_commerceShippingMethodModelResourcePermission.check(
				getPermissionChecker(),
				commerceShippingFixedOption.getCommerceShippingMethodId(),
				ActionKeys.VIEW);
		}

		return commerceShippingFixedOption;
	}

	@Override
	public List<CommerceShippingFixedOption> getCommerceShippingFixedOptions(
			long companyId, long groupId, long commerceShippingMethodId,
			String keywords, int start, int end,
			OrderByComparator<CommerceShippingFixedOption> orderByComparator)
		throws PortalException {

		return commerceShippingFixedOptionLocalService.
			getCommerceShippingFixedOptions(
				companyId, groupId, commerceShippingMethodId, keywords, start,
				end, orderByComparator);
	}

	@Override
	public long getCommerceShippingFixedOptionsCount(
			long companyId, long groupId, long commerceShippingMethodId,
			String keywords)
		throws PortalException {

		return commerceShippingFixedOptionLocalService.
			getCommerceShippingFixedOptionsCount(
				companyId, groupId, commerceShippingMethodId, keywords);
	}

	@Override
	public CommerceShippingFixedOption updateCommerceShippingFixedOption(
			long commerceShippingFixedOptionId, BigDecimal amount,
			Map<Locale, String> descriptionMap, String key,
			Map<Locale, String> nameMap, double priority)
		throws PortalException {

		CommerceShippingFixedOption commerceShippingFixedOption =
			commerceShippingFixedOptionLocalService.
				fetchCommerceShippingFixedOption(commerceShippingFixedOptionId);

		_commerceShippingMethodModelResourcePermission.check(
			getPermissionChecker(),
			commerceShippingFixedOption.getCommerceShippingMethodId(),
			ActionKeys.UPDATE);

		return commerceShippingFixedOptionLocalService.
			updateCommerceShippingFixedOption(
				commerceShippingFixedOptionId, amount, descriptionMap, key,
				nameMap, priority);
	}

	@Reference(
		target = "(model.class.name=com.liferay.commerce.model.CommerceShippingMethod)"
	)
	private ModelResourcePermission<CommerceShippingMethod>
		_commerceShippingMethodModelResourcePermission;

}