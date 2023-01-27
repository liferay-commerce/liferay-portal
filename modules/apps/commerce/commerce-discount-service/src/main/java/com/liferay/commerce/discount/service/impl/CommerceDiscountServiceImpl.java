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

package com.liferay.commerce.discount.service.impl;

import com.liferay.commerce.discount.constants.CommerceDiscountActionKeys;
import com.liferay.commerce.discount.exception.NoSuchDiscountException;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.service.base.CommerceDiscountServiceBaseImpl;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelService;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseModelSearchResult;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.Validator;

import java.math.BigDecimal;

import java.util.List;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 * @author Alessio Antonio Rendina
 */
@Component(
	property = {
		"json.web.service.context.name=commerce",
		"json.web.service.context.path=CommerceDiscount"
	},
	service = AopService.class
)
public class CommerceDiscountServiceImpl
	extends CommerceDiscountServiceBaseImpl {

	@Override
	public CommerceDiscount addCommerceDiscount(
			String externalReferenceCode, boolean active,
			String commerceCurrencyCode, String couponCode,
			int displayDateMonth, int displayDateDay, int displayDateYear,
			int displayDateHour, int displayDateMinute, int expirationDateMonth,
			int expirationDateDay, int expirationDateYear,
			int expirationDateHour, int expirationDateMinute, String level,
			BigDecimal level1, BigDecimal level2, BigDecimal level3,
			BigDecimal level4, int limitationTimes,
			int limitationTimesPerAccount, String limitationType,
			BigDecimal maximumDiscountAmount, boolean neverExpire,
			boolean rulesConjunction, String target, String title,
			boolean useCouponCode, boolean usePercentage,
			ServiceContext serviceContext)
		throws PortalException {

		PortletResourcePermission portletResourcePermission =
			_commerceDiscountResourcePermission.getPortletResourcePermission();

		portletResourcePermission.check(
			getPermissionChecker(), null,
			CommerceDiscountActionKeys.ADD_COMMERCE_DISCOUNT);

		return commerceDiscountLocalService.addCommerceDiscount(
			externalReferenceCode, getUserId(), active, commerceCurrencyCode,
			couponCode, displayDateMonth, displayDateDay, displayDateYear,
			displayDateHour, displayDateMinute, expirationDateMonth,
			expirationDateDay, expirationDateYear, expirationDateHour,
			expirationDateMinute, level, level1, level2, level3, level4,
			limitationTimes, limitationTimesPerAccount, limitationType,
			maximumDiscountAmount, neverExpire, rulesConjunction, target, title,
			useCouponCode, usePercentage, serviceContext);
	}

	@Override
	public CommerceDiscount addOrUpdateCommerceDiscount(
			String externalReferenceCode, boolean active,
			String commerceCurrencyCode, long commerceDiscountId,
			String couponCode, int displayDateMonth, int displayDateDay,
			int displayDateYear, int displayDateHour, int displayDateMinute,
			int expirationDateMonth, int expirationDateDay,
			int expirationDateYear, int expirationDateHour,
			int expirationDateMinute, String level, BigDecimal level1,
			BigDecimal level2, BigDecimal level3, BigDecimal level4,
			int limitationTimes, int limitationTimesPerAccount,
			String limitationType, BigDecimal maximumDiscountAmount,
			boolean neverExpire, boolean rulesConjunction, String target,
			String title, boolean useCouponCode, boolean usePercentage,
			ServiceContext serviceContext)
		throws PortalException {

		// Update

		if (commerceDiscountId > 0) {
			try {
				return updateCommerceDiscount(
					active, commerceCurrencyCode, commerceDiscountId,
					couponCode, displayDateMonth, displayDateDay,
					displayDateYear, displayDateHour, displayDateMinute,
					expirationDateMonth, expirationDateDay, expirationDateYear,
					expirationDateHour, expirationDateMinute, level, level1,
					level2, level3, level4, limitationTimes,
					limitationTimesPerAccount, limitationType,
					maximumDiscountAmount, neverExpire, rulesConjunction,
					target, title, useCouponCode, usePercentage,
					serviceContext);
			}
			catch (NoSuchDiscountException noSuchDiscountException) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"Unable to find discount with ID: " +
							commerceDiscountId,
						noSuchDiscountException);
				}
			}
		}

		if (!Validator.isBlank(externalReferenceCode)) {
			CommerceDiscount commerceDiscount =
				commerceDiscountPersistence.fetchByERC_C(
					externalReferenceCode, serviceContext.getCompanyId());

			if (commerceDiscount != null) {
				return updateCommerceDiscount(
					active, commerceCurrencyCode,
					commerceDiscount.getCommerceDiscountId(), couponCode,
					displayDateMonth, displayDateDay, displayDateYear,
					displayDateHour, displayDateMinute, expirationDateMonth,
					expirationDateDay, expirationDateYear, expirationDateHour,
					expirationDateMinute, level, level1, level2, level3, level4,
					limitationTimes, limitationTimesPerAccount, limitationType,
					maximumDiscountAmount, neverExpire, rulesConjunction,
					target, title, useCouponCode, usePercentage,
					serviceContext);
			}
		}

		// Add

		return addCommerceDiscount(
			externalReferenceCode, active, commerceCurrencyCode, couponCode,
			displayDateMonth, displayDateDay, displayDateYear, displayDateHour,
			displayDateMinute, expirationDateMonth, expirationDateDay,
			expirationDateYear, expirationDateHour, expirationDateMinute, level,
			level1, level2, level3, level4, limitationTimes,
			limitationTimesPerAccount, limitationType, maximumDiscountAmount,
			neverExpire, rulesConjunction, target, title, useCouponCode,
			usePercentage, serviceContext);
	}

	@Override
	public void deleteCommerceDiscount(long commerceDiscountId)
		throws PortalException {

		_commerceDiscountResourcePermission.check(
			getPermissionChecker(), commerceDiscountId, ActionKeys.DELETE);

		commerceDiscountLocalService.deleteCommerceDiscount(commerceDiscountId);
	}

	@Override
	public CommerceDiscount fetchByExternalReferenceCode(
			String externalReferenceCode, long companyId)
		throws PortalException {

		CommerceDiscount commerceDiscount =
			commerceDiscountLocalService.fetchByExternalReferenceCode(
				externalReferenceCode, companyId);

		if (commerceDiscount != null) {
			_commerceDiscountResourcePermission.check(
				getPermissionChecker(), commerceDiscount, ActionKeys.VIEW);
		}

		return commerceDiscount;
	}

	@Override
	public CommerceDiscount fetchCommerceDiscount(long commerceDiscountId)
		throws PortalException {

		CommerceDiscount commerceDiscount =
			commerceDiscountLocalService.fetchCommerceDiscount(
				commerceDiscountId);

		if (commerceDiscount != null) {
			_commerceDiscountResourcePermission.check(
				getPermissionChecker(), commerceDiscount, ActionKeys.VIEW);
		}

		return commerceDiscount;
	}

	@Override
	public CommerceDiscount getCommerceDiscount(long commerceDiscountId)
		throws PortalException {

		_commerceDiscountResourcePermission.check(
			getPermissionChecker(), commerceDiscountId, ActionKeys.VIEW);

		return commerceDiscountLocalService.getCommerceDiscount(
			commerceDiscountId);
	}

	@Override
	public List<CommerceDiscount> getCommerceDiscounts(
			long companyId, String level, boolean active, int status)
		throws PortalException {

		PortletResourcePermission portletResourcePermission =
			_commerceDiscountResourcePermission.getPortletResourcePermission();

		portletResourcePermission.check(
			getPermissionChecker(), null,
			CommerceDiscountActionKeys.VIEW_COMMERCE_DISCOUNTS);

		return commerceDiscountLocalService.getCommerceDiscounts(
			companyId, active, level, status);
	}

	@Override
	public int getCommerceDiscountsCountByPricingClassId(
			long commercePricingClassId, String title)
		throws PrincipalException {

		return commerceDiscountFinder.countByCommercePricingClassId(
			commercePricingClassId, title, true);
	}

	@Override
	public List<CommerceDiscount> searchByCommercePricingClassId(
			long commercePricingClassId, String title, int start, int end)
		throws PrincipalException {

		return commerceDiscountFinder.findByCommercePricingClassId(
			commercePricingClassId, title, start, end, true);
	}

	@Override
	public BaseModelSearchResult<CommerceDiscount> searchCommerceDiscounts(
			long companyId, String keywords, int status, int start, int end,
			Sort sort)
		throws PortalException {

		List<CommerceChannel> commerceChannels = _commerceChannelService.search(
			companyId);

		Stream<CommerceChannel> stream = commerceChannels.stream();

		long[] commerceChannelGroupIds = stream.mapToLong(
			CommerceChannel::getGroupId
		).toArray();

		return commerceDiscountLocalService.searchCommerceDiscounts(
			companyId, commerceChannelGroupIds, keywords, status, start, end,
			sort);
	}

	@Override
	public CommerceDiscount updateCommerceDiscount(
			boolean active, String commerceCurrencyCode,
			long commerceDiscountId, String couponCode, int displayDateMonth,
			int displayDateDay, int displayDateYear, int displayDateHour,
			int displayDateMinute, int expirationDateMonth,
			int expirationDateDay, int expirationDateYear,
			int expirationDateHour, int expirationDateMinute, String level,
			BigDecimal level1, BigDecimal level2, BigDecimal level3,
			BigDecimal level4, int limitationTimes,
			int limitationTimesPerAccount, String limitationType,
			BigDecimal maximumDiscountAmount, boolean neverExpire,
			boolean rulesConjunction, String target, String title,
			boolean useCouponCode, boolean usePercentage,
			ServiceContext serviceContext)
		throws PortalException {

		_commerceDiscountResourcePermission.check(
			getPermissionChecker(), commerceDiscountId, ActionKeys.UPDATE);

		return commerceDiscountLocalService.updateCommerceDiscount(
			active, commerceCurrencyCode, commerceDiscountId, couponCode,
			displayDateMonth, displayDateDay, displayDateYear, displayDateHour,
			displayDateMinute, expirationDateMonth, expirationDateDay,
			expirationDateYear, expirationDateHour, expirationDateMinute, level,
			level1, level2, level3, level4, limitationTimes,
			limitationTimesPerAccount, limitationType, maximumDiscountAmount,
			neverExpire, rulesConjunction, target, title, useCouponCode,
			usePercentage, serviceContext);
	}

	@Override
	public CommerceDiscount updateCommerceDiscountExternalReferenceCode(
			String externalReferenceCode, long commerceDiscountId)
		throws PortalException {

		_commerceDiscountResourcePermission.check(
			getPermissionChecker(), commerceDiscountId, ActionKeys.UPDATE);

		return commerceDiscountLocalService.
			updateCommerceDiscountExternalReferenceCode(
				externalReferenceCode, commerceDiscountId);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceDiscountServiceImpl.class);

	@Reference
	private CommerceChannelService _commerceChannelService;

	@Reference(
		target = "(model.class.name=com.liferay.commerce.discount.model.CommerceDiscount)"
	)
	private ModelResourcePermission<CommerceDiscount>
		_commerceDiscountResourcePermission;

}