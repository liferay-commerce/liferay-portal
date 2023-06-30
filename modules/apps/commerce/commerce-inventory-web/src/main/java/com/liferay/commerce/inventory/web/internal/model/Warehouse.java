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
 * @author Alessio Antonio Rendina
 */
public class Warehouse {

	public Warehouse(
		long commerceInventoryWarehouseItemId, String warehouse,
		BigDecimal quantity, BigDecimal reserved, long incoming) {

		_commerceInventoryWarehouseItemId = commerceInventoryWarehouseItemId;
		_warehouse = warehouse;
		_quantity = quantity;
		_reserved = reserved;

		if ((quantity.compareTo(BigDecimal.ZERO) > 0) &&
			(reserved.compareTo(BigDecimal.ZERO) >= 0)) {

			_available = quantity.subtract(reserved);
		}
		else {
			_available = BigDecimal.ZERO;
		}

		_incoming = incoming;
	}

	public BigDecimal getAvailable() {
		return _available;
	}

	public long getCommerceInventoryWarehouseItemId() {
		return _commerceInventoryWarehouseItemId;
	}

	public long getIncoming() {
		return _incoming;
	}

	public BigDecimal getQuantity() {
		return _quantity;
	}

	public BigDecimal getReserved() {
		return _reserved;
	}

	public String getWarehouse() {
		return _warehouse;
	}

	private final BigDecimal _available;
	private final long _commerceInventoryWarehouseItemId;
	private final long _incoming;
	private final BigDecimal _quantity;
	private final BigDecimal _reserved;
	private final String _warehouse;

}