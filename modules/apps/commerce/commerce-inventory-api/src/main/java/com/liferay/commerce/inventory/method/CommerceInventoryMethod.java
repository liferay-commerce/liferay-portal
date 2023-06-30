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

package com.liferay.commerce.inventory.method;

import com.liferay.portal.kernel.exception.PortalException;

import java.math.BigDecimal;

import java.util.Locale;
import java.util.Map;

/**
 * @author Alessio Antonio Rendina
 */
public interface CommerceInventoryMethod {

	public void consumeQuantity(
			long userId, long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey, BigDecimal quantity, long bookedQuantityId,
			Map<String, String> context)
		throws PortalException;

	public void decreaseStockQuantity(
			long userId, long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey, BigDecimal quantity)
		throws PortalException;

	public String getAvailabilityStatus(
		long companyId, long commerceChannelGroupId,
		BigDecimal minStockQuantity, String sku, String unitOfMeasureKey);

	public String getKey();

	public String getLabel(Locale locale);

	public BigDecimal getStockQuantity(
			long companyId, long commerceChannelGroupId, String sku,
			String unitOfMeasureKey)
		throws PortalException;

	public BigDecimal getStockQuantity(
			long companyId, String sku, String unitOfMeasureKey)
		throws PortalException;

	public boolean hasStockQuantity(
		long companyId, String sku, String unitOfMeasureKey,
		BigDecimal quantity);

	public void increaseStockQuantity(
			long userId, long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey, BigDecimal quantity)
		throws PortalException;

}