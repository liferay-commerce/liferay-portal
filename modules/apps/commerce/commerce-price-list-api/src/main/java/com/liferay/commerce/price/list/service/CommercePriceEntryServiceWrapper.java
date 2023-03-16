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

package com.liferay.commerce.price.list.service;

import com.liferay.commerce.price.list.model.CommercePriceEntry;
import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link CommercePriceEntryService}.
 *
 * @author Alessio Antonio Rendina
 * @see CommercePriceEntryService
 * @generated
 */
public class CommercePriceEntryServiceWrapper
	implements CommercePriceEntryService,
			   ServiceWrapper<CommercePriceEntryService> {

	public CommercePriceEntryServiceWrapper() {
		this(null);
	}

	public CommercePriceEntryServiceWrapper(
		CommercePriceEntryService commercePriceEntryService) {

		_commercePriceEntryService = commercePriceEntryService;
	}

	@Override
	public CommercePriceEntry addCommercePriceEntry(
			String externalReferenceCode, long commercePriceListId,
			long cProductId, String cpInstanceUuid, java.math.BigDecimal price,
			boolean priceOnApplication,
			com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.addCommercePriceEntry(
			externalReferenceCode, commercePriceListId, cProductId,
			cpInstanceUuid, price, priceOnApplication, serviceContext);
	}

	@Override
	public CommercePriceEntry addOrUpdateCommercePriceEntry(
			String externalReferenceCode, long commercePriceEntryId,
			long cProductId, String cpInstanceUuid, long commercePriceListId,
			java.math.BigDecimal price, boolean priceOnApplication,
			boolean discountDiscovery, java.math.BigDecimal discountLevel1,
			java.math.BigDecimal discountLevel2,
			java.math.BigDecimal discountLevel3,
			java.math.BigDecimal discountLevel4, int displayDateMonth,
			int displayDateDay, int displayDateYear, int displayDateHour,
			int displayDateMinute, int expirationDateMonth,
			int expirationDateDay, int expirationDateYear,
			int expirationDateHour, int expirationDateMinute,
			boolean neverExpire, String skuExternalReferenceCode,
			com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.addOrUpdateCommercePriceEntry(
			externalReferenceCode, commercePriceEntryId, cProductId,
			cpInstanceUuid, commercePriceListId, price, priceOnApplication,
			discountDiscovery, discountLevel1, discountLevel2, discountLevel3,
			discountLevel4, displayDateMonth, displayDateDay, displayDateYear,
			displayDateHour, displayDateMinute, expirationDateMonth,
			expirationDateDay, expirationDateYear, expirationDateHour,
			expirationDateMinute, neverExpire, skuExternalReferenceCode,
			serviceContext);
	}

	@Override
	public CommercePriceEntry addOrUpdateCommercePriceEntry(
			String externalReferenceCode, long commercePriceEntryId,
			long cProductId, String cpInstanceUuid, long commercePriceListId,
			java.math.BigDecimal price, boolean priceOnApplication,
			String skuExternalReferenceCode,
			com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.addOrUpdateCommercePriceEntry(
			externalReferenceCode, commercePriceEntryId, cProductId,
			cpInstanceUuid, commercePriceListId, price, priceOnApplication,
			skuExternalReferenceCode, serviceContext);
	}

	@Override
	public void deleteCommercePriceEntry(long commercePriceEntryId)
		throws com.liferay.portal.kernel.exception.PortalException {

		_commercePriceEntryService.deleteCommercePriceEntry(
			commercePriceEntryId);
	}

	@Override
	public CommercePriceEntry fetchByExternalReferenceCode(
			String externalReferenceCode, long companyId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.fetchByExternalReferenceCode(
			externalReferenceCode, companyId);
	}

	@Override
	public CommercePriceEntry fetchCommercePriceEntry(long commercePriceEntryId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.fetchCommercePriceEntry(
			commercePriceEntryId);
	}

	@Override
	public java.util.List<CommercePriceEntry> getCommercePriceEntries(
			long commercePriceListId, int start, int end)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.getCommercePriceEntries(
			commercePriceListId, start, end);
	}

	@Override
	public int getCommercePriceEntriesCount(long commercePriceListId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.getCommercePriceEntriesCount(
			commercePriceListId);
	}

	@Override
	public CommercePriceEntry getCommercePriceEntry(long commercePriceEntryId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.getCommercePriceEntry(
			commercePriceEntryId);
	}

	@Override
	public CommercePriceEntry getInstanceBaseCommercePriceEntry(
		String cpInstanceUuid, String priceListType) {

		return _commercePriceEntryService.getInstanceBaseCommercePriceEntry(
			cpInstanceUuid, priceListType);
	}

	@Override
	public java.util.List<CommercePriceEntry> getInstanceCommercePriceEntries(
			long cpInstanceId, int start, int end)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.getInstanceCommercePriceEntries(
			cpInstanceId, start, end);
	}

	@Override
	public int getInstanceCommercePriceEntriesCount(long cpInstanceId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.getInstanceCommercePriceEntriesCount(
			cpInstanceId);
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _commercePriceEntryService.getOSGiServiceIdentifier();
	}

	@Override
	public CommercePriceEntry updateCommercePriceEntry(
			long commercePriceEntryId, java.math.BigDecimal price,
			boolean priceOnApplication, boolean discountDiscovery,
			java.math.BigDecimal discountLevel1,
			java.math.BigDecimal discountLevel2,
			java.math.BigDecimal discountLevel3,
			java.math.BigDecimal discountLevel4, boolean bulkPricing,
			int displayDateMonth, int displayDateDay, int displayDateYear,
			int displayDateHour, int displayDateMinute, int expirationDateMonth,
			int expirationDateDay, int expirationDateYear,
			int expirationDateHour, int expirationDateMinute,
			boolean neverExpire,
			com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.updateCommercePriceEntry(
			commercePriceEntryId, price, priceOnApplication, discountDiscovery,
			discountLevel1, discountLevel2, discountLevel3, discountLevel4,
			bulkPricing, displayDateMonth, displayDateDay, displayDateYear,
			displayDateHour, displayDateMinute, expirationDateMonth,
			expirationDateDay, expirationDateYear, expirationDateHour,
			expirationDateMinute, neverExpire, serviceContext);
	}

	@Override
	public CommercePriceEntry updateCommercePriceEntry(
			long commercePriceEntryId, java.math.BigDecimal price,
			boolean priceOnApplication,
			com.liferay.portal.kernel.service.ServiceContext serviceContext)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commercePriceEntryService.updateCommercePriceEntry(
			commercePriceEntryId, price, priceOnApplication, serviceContext);
	}

	@Override
	public CommercePriceEntryService getWrappedService() {
		return _commercePriceEntryService;
	}

	@Override
	public void setWrappedService(
		CommercePriceEntryService commercePriceEntryService) {

		_commercePriceEntryService = commercePriceEntryService;
	}

	private CommercePriceEntryService _commercePriceEntryService;

}