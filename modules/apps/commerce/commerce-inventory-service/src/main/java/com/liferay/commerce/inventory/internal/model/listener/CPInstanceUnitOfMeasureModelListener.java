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

package com.liferay.commerce.inventory.internal.model.listener;

import com.liferay.commerce.inventory.model.CommerceInventoryBookedQuantity;
import com.liferay.commerce.inventory.model.CommerceInventoryReplenishmentItem;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouse;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouseItem;
import com.liferay.commerce.inventory.service.CommerceInventoryBookedQuantityLocalService;
import com.liferay.commerce.inventory.service.CommerceInventoryReplenishmentItemLocalService;
import com.liferay.commerce.inventory.service.CommerceInventoryWarehouseItemLocalService;
import com.liferay.commerce.inventory.service.CommerceInventoryWarehouseLocalService;
import com.liferay.commerce.product.model.CPInstanceUnitOfMeasure;
import com.liferay.commerce.product.service.CPInstanceUnitOfMeasureLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import java.math.BigDecimal;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Stefano Motta
 */
@Component(service = ModelListener.class)
public class CPInstanceUnitOfMeasureModelListener
	extends BaseModelListener<CPInstanceUnitOfMeasure> {

	@Override
	public void onAfterCreate(CPInstanceUnitOfMeasure cpInstanceUnitOfMeasure) {
		List<CPInstanceUnitOfMeasure> cpInstanceUnitOfMeasures =
			_cpInstanceUnitOfMeasureLocalService.getCPInstanceUnitOfMeasures(
				cpInstanceUnitOfMeasure.getCPInstanceId(), 0, 2, null);

		if (cpInstanceUnitOfMeasures.size() == 1) {
			_updateUnitOfMeasureKey(
				cpInstanceUnitOfMeasure.getCompanyId(),
				cpInstanceUnitOfMeasure.getKey(), StringPool.BLANK,
				cpInstanceUnitOfMeasure.getSku());
		}
		else {
			for (CommerceInventoryWarehouse commerceInventoryWarehouse :
					_commerceInventoryWarehouseLocalService.
						getCommerceInventoryWarehouses(
							cpInstanceUnitOfMeasure.getCompanyId())) {

				try {
					_commerceInventoryWarehouseItemLocalService.
						addCommerceInventoryWarehouseItem(
							null, cpInstanceUnitOfMeasure.getUserId(),
							commerceInventoryWarehouse.
								getCommerceInventoryWarehouseId(),
							cpInstanceUnitOfMeasure.getSku(),
							cpInstanceUnitOfMeasure.getKey(), BigDecimal.ZERO);
				}
				catch (PortalException portalException) {
					_log.error(portalException);
				}
			}
		}
	}

	@Override
	public void onAfterRemove(CPInstanceUnitOfMeasure cpInstanceUnitOfMeasure) {
		List<CPInstanceUnitOfMeasure> cpInstanceUnitOfMeasures =
			_cpInstanceUnitOfMeasureLocalService.getCPInstanceUnitOfMeasures(
				cpInstanceUnitOfMeasure.getCPInstanceId(), 0, 1, null);

		if (cpInstanceUnitOfMeasures.isEmpty()) {
			_updateUnitOfMeasureKey(
				cpInstanceUnitOfMeasure.getCompanyId(), StringPool.BLANK,
				cpInstanceUnitOfMeasure.getKey(),
				cpInstanceUnitOfMeasure.getSku());
		}
		else {
			for (CommerceInventoryWarehouse commerceInventoryWarehouse :
					_commerceInventoryWarehouseLocalService.
						getCommerceInventoryWarehouses(
							cpInstanceUnitOfMeasure.getCompanyId())) {

				_commerceInventoryWarehouseItemLocalService.
					deleteCommerceInventoryWarehouseItems(
						commerceInventoryWarehouse.getCompanyId(),
						cpInstanceUnitOfMeasure.getSku(),
						cpInstanceUnitOfMeasure.getKey());
			}
		}
	}

	@Override
	public void onAfterUpdate(
			CPInstanceUnitOfMeasure originalCPInstanceUnitOfMeasure,
			CPInstanceUnitOfMeasure cpInstanceUnitOfMeasure)
		throws ModelListenerException {

		String key = cpInstanceUnitOfMeasure.getKey();

		if (key.equals(originalCPInstanceUnitOfMeasure.getKey())) {
			return;
		}

		_updateUnitOfMeasureKey(
			originalCPInstanceUnitOfMeasure.getCompanyId(),
			cpInstanceUnitOfMeasure.getKey(),
			originalCPInstanceUnitOfMeasure.getKey(),
			originalCPInstanceUnitOfMeasure.getSku());
	}

	private void _updateUnitOfMeasureKey(
		long companyId, String key, String originalKey, String sku) {

		for (CommerceInventoryBookedQuantity commerceInventoryBookedQuantity :
				_commerceInventoryBookedQuantityLocalService.
					getCommerceInventoryBookedQuantities(
						companyId, sku, originalKey, QueryUtil.ALL_POS,
						QueryUtil.ALL_POS)) {

			commerceInventoryBookedQuantity.setUnitOfMeasureKey(key);

			_commerceInventoryBookedQuantityLocalService.
				updateCommerceInventoryBookedQuantity(
					commerceInventoryBookedQuantity);
		}

		for (CommerceInventoryReplenishmentItem
				commerceInventoryReplenishmentItem :
					_commerceInventoryReplenishmentItemLocalService.
						getCommerceInventoryReplenishmentItemsByCompanyIdAndSku(
							companyId, sku, originalKey, QueryUtil.ALL_POS,
							QueryUtil.ALL_POS)) {

			commerceInventoryReplenishmentItem.setUnitOfMeasureKey(key);

			_commerceInventoryReplenishmentItemLocalService.
				updateCommerceInventoryReplenishmentItem(
					commerceInventoryReplenishmentItem);
		}

		for (CommerceInventoryWarehouseItem commerceInventoryWarehouseItem :
				_commerceInventoryWarehouseItemLocalService.
					getCommerceInventoryWarehouseItems(
						companyId, sku, originalKey, QueryUtil.ALL_POS,
						QueryUtil.ALL_POS)) {

			commerceInventoryWarehouseItem.setUnitOfMeasureKey(key);

			_commerceInventoryWarehouseItemLocalService.
				updateCommerceInventoryWarehouseItem(
					commerceInventoryWarehouseItem);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CPInstanceUnitOfMeasureModelListener.class);

	@Reference
	private CommerceInventoryBookedQuantityLocalService
		_commerceInventoryBookedQuantityLocalService;

	@Reference
	private CommerceInventoryReplenishmentItemLocalService
		_commerceInventoryReplenishmentItemLocalService;

	@Reference
	private CommerceInventoryWarehouseItemLocalService
		_commerceInventoryWarehouseItemLocalService;

	@Reference
	private CommerceInventoryWarehouseLocalService
		_commerceInventoryWarehouseLocalService;

	@Reference
	private CPInstanceUnitOfMeasureLocalService
		_cpInstanceUnitOfMeasureLocalService;

}