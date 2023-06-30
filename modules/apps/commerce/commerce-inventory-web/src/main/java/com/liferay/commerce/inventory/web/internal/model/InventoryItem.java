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

package com.liferay.commerce.inventory.web.internal.model;

import java.math.BigDecimal;

/**
 * @author Luca Pellizzon
 */
public class InventoryItem {

	public InventoryItem(
		String sku, String unitOfMeasureKey, BigDecimal stock,
		BigDecimal booked, BigDecimal incoming) {

		_sku = sku;
		_unitOfMeasureKey = unitOfMeasureKey;
		_stock = stock;

		if ((stock.compareTo(BigDecimal.ZERO) > 0) &&
			(booked.compareTo(BigDecimal.ZERO) >= 0)) {

			_available = stock.subtract(booked);
		}
		else {
			_available = BigDecimal.ZERO;
		}

		_booked = booked;
		_incoming = incoming;
	}

	public BigDecimal getAvailable() {
		return _available;
	}

	public BigDecimal getBooked() {
		return _booked;
	}

	public BigDecimal getIncoming() {
		return _incoming;
	}

	public String getSku() {
		return _sku;
	}

	public BigDecimal getStock() {
		return _stock;
	}

	public String getUnitOfMeasureKey() {
		return _unitOfMeasureKey;
	}

	private final BigDecimal _available;
	private final BigDecimal _booked;
	private final BigDecimal _incoming;
	private final String _sku;
	private final BigDecimal _stock;
	private final String _unitOfMeasureKey;

}