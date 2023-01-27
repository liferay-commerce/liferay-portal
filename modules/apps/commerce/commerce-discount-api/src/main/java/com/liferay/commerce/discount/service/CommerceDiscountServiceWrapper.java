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

package com.liferay.commerce.discount.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link CommerceDiscountService}.
 *
 * @author Marco Leo
 * @see CommerceDiscountService
 * @generated
 */
public class CommerceDiscountServiceWrapper
	implements CommerceDiscountService,
			   ServiceWrapper<CommerceDiscountService> {

	public CommerceDiscountServiceWrapper() {
		this(null);
	}

	public CommerceDiscountServiceWrapper(
		CommerceDiscountService commerceDiscountService) {

		_commerceDiscountService = commerceDiscountService;
	}

	@Override
	public com.liferay.commerce.discount.model.CommerceDiscount
			addCommerceDiscount(
				String externalReferenceCode, boolean active,
				String commerceCurrencyCode, String couponCode,
				int displayDateMonth, int displayDateDay, int displayDateYear,
				int displayDateHour, int displayDateMinute,
				int expirationDateMonth, int expirationDateDay,
				int expirationDateYear, int expirationDateHour,
				int expirationDateMinute, String level,
				java.math.BigDecimal level1, java.math.BigDecimal level2,
				java.math.BigDecimal level3, java.math.BigDecimal level4,
				int limitationTimes, int limitationTimesPerAccount,
				String limitationType,
				java.math.BigDecimal maximumDiscountAmount, boolean neverExpire,
				boolean rulesConjunction, String target, String title,
				boolean useCouponCode, boolean usePercentage,
				com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountService.addCommerceDiscount(
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
	public com.liferay.commerce.discount.model.CommerceDiscount
			addOrUpdateCommerceDiscount(
				String externalReferenceCode, boolean active,
				String commerceCurrencyCode, long commerceDiscountId,
				String couponCode, int displayDateMonth, int displayDateDay,
				int displayDateYear, int displayDateHour, int displayDateMinute,
				int expirationDateMonth, int expirationDateDay,
				int expirationDateYear, int expirationDateHour,
				int expirationDateMinute, String level,
				java.math.BigDecimal level1, java.math.BigDecimal level2,
				java.math.BigDecimal level3, java.math.BigDecimal level4,
				int limitationTimes, int limitationTimesPerAccount,
				String limitationType,
				java.math.BigDecimal maximumDiscountAmount, boolean neverExpire,
				boolean rulesConjunction, String target, String title,
				boolean useCouponCode, boolean usePercentage,
				com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountService.addOrUpdateCommerceDiscount(
			externalReferenceCode, active, commerceCurrencyCode,
			commerceDiscountId, couponCode, displayDateMonth, displayDateDay,
			displayDateYear, displayDateHour, displayDateMinute,
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, level, level1, level2,
			level3, level4, limitationTimes, limitationTimesPerAccount,
			limitationType, maximumDiscountAmount, neverExpire,
			rulesConjunction, target, title, useCouponCode, usePercentage,
			serviceContext);
	}

	@Override
	public void deleteCommerceDiscount(long commerceDiscountId)
		throws com.liferay.portal.kernel.exception.PortalException {

		_commerceDiscountService.deleteCommerceDiscount(commerceDiscountId);
	}

	@Override
	public com.liferay.commerce.discount.model.CommerceDiscount
			fetchByExternalReferenceCode(
				String externalReferenceCode, long companyId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountService.fetchByExternalReferenceCode(
			externalReferenceCode, companyId);
	}

	@Override
	public com.liferay.commerce.discount.model.CommerceDiscount
			fetchCommerceDiscount(long commerceDiscountId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountService.fetchCommerceDiscount(
			commerceDiscountId);
	}

	@Override
	public com.liferay.commerce.discount.model.CommerceDiscount
			getCommerceDiscount(long commerceDiscountId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountService.getCommerceDiscount(commerceDiscountId);
	}

	@Override
	public java.util.List<com.liferay.commerce.discount.model.CommerceDiscount>
			getCommerceDiscounts(
				long companyId, String level, boolean active, int status)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountService.getCommerceDiscounts(
			companyId, level, active, status);
	}

	@Override
	public int getCommerceDiscountsCountByPricingClassId(
			long commercePricingClassId, String title)
		throws com.liferay.portal.kernel.security.auth.PrincipalException {

		return _commerceDiscountService.
			getCommerceDiscountsCountByPricingClassId(
				commercePricingClassId, title);
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _commerceDiscountService.getOSGiServiceIdentifier();
	}

	@Override
	public java.util.List<com.liferay.commerce.discount.model.CommerceDiscount>
			searchByCommercePricingClassId(
				long commercePricingClassId, String title, int start, int end)
		throws com.liferay.portal.kernel.security.auth.PrincipalException {

		return _commerceDiscountService.searchByCommercePricingClassId(
			commercePricingClassId, title, start, end);
	}

	@Override
	public com.liferay.portal.kernel.search.BaseModelSearchResult
		<com.liferay.commerce.discount.model.CommerceDiscount>
				searchCommerceDiscounts(
					long companyId, String keywords, int status, int start,
					int end, com.liferay.portal.kernel.search.Sort sort)
			throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountService.searchCommerceDiscounts(
			companyId, keywords, status, start, end, sort);
	}

	@Override
	public com.liferay.commerce.discount.model.CommerceDiscount
			updateCommerceDiscount(
				boolean active, String commerceCurrencyCode,
				long commerceDiscountId, String couponCode,
				int displayDateMonth, int displayDateDay, int displayDateYear,
				int displayDateHour, int displayDateMinute,
				int expirationDateMonth, int expirationDateDay,
				int expirationDateYear, int expirationDateHour,
				int expirationDateMinute, String level,
				java.math.BigDecimal level1, java.math.BigDecimal level2,
				java.math.BigDecimal level3, java.math.BigDecimal level4,
				int limitationTimes, int limitationTimesPerAccount,
				String limitationType,
				java.math.BigDecimal maximumDiscountAmount, boolean neverExpire,
				boolean rulesConjunction, String target, String title,
				boolean useCouponCode, boolean usePercentage,
				com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountService.updateCommerceDiscount(
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
	public com.liferay.commerce.discount.model.CommerceDiscount
			updateCommerceDiscountExternalReferenceCode(
				String externalReferenceCode, long commerceDiscountId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountService.
			updateCommerceDiscountExternalReferenceCode(
				externalReferenceCode, commerceDiscountId);
	}

	@Override
	public CommerceDiscountService getWrappedService() {
		return _commerceDiscountService;
	}

	@Override
	public void setWrappedService(
		CommerceDiscountService commerceDiscountService) {

		_commerceDiscountService = commerceDiscountService;
	}

	private CommerceDiscountService _commerceDiscountService;

}