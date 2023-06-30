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

package com.liferay.commerce.inventory.model;

import java.math.BigDecimal;

/**
 * @author Luca Pellizzon
 * @author Alessio Antonio Rendina
 */
public class CIWarehouseItem {

	public CIWarehouseItem(
		String skuCode, String unitOfMeasureKey, BigDecimal stockQuantity,
		BigDecimal bookedQuantity, BigDecimal replenishmentQuantity) {

		_skuCode = skuCode;
		_unitOfMeasureKey = unitOfMeasureKey;
		_stockQuantity = stockQuantity;
		_bookedQuantity = bookedQuantity;
		_replenishmentQuantity = replenishmentQuantity;
	}

	public BigDecimal getBookedQuantity() {
		return _bookedQuantity;
	}

	public BigDecimal getReplenishmentQuantity() {
		return _replenishmentQuantity;
	}

	public String getSkuCode() {
		return _skuCode;
	}

	public BigDecimal getStockQuantity() {
		return _stockQuantity;
	}

	public String getUnitOfMeasureKey() {
		return _unitOfMeasureKey;
	}

	public void setBookedQuantity(BigDecimal bookedQuantity) {
		_bookedQuantity = bookedQuantity;
	}

	public void setReplenishmentQuantity(BigDecimal replenishmentQuantity) {
		_replenishmentQuantity = replenishmentQuantity;
	}

	public void setSkuCode(String skuCode) {
		_skuCode = skuCode;
	}

	public void setStockQuantity(BigDecimal stockQuantity) {
		_stockQuantity = stockQuantity;
	}

	public void setUnitOfMeasureKey(String unitOfMeasureKey) {
		_unitOfMeasureKey = unitOfMeasureKey;
	}

	private BigDecimal _bookedQuantity;
	private BigDecimal _replenishmentQuantity;
	private String _skuCode;
	private BigDecimal _stockQuantity;
	private String _unitOfMeasureKey;

}