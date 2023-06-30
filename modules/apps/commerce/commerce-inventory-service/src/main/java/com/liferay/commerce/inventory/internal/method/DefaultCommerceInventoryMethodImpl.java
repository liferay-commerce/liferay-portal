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

package com.liferay.commerce.inventory.internal.method;

import com.liferay.commerce.inventory.constants.CommerceInventoryAvailabilityConstants;
import com.liferay.commerce.inventory.constants.CommerceInventoryConstants;
import com.liferay.commerce.inventory.engine.contributor.CommerceInventoryEngineContributor;
import com.liferay.commerce.inventory.engine.contributor.CommerceInventoryEngineContributorRegistry;
import com.liferay.commerce.inventory.exception.MVCCException;
import com.liferay.commerce.inventory.method.CommerceInventoryMethod;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouseItem;
import com.liferay.commerce.inventory.service.CommerceInventoryAuditLocalService;
import com.liferay.commerce.inventory.service.CommerceInventoryBookedQuantityLocalService;
import com.liferay.commerce.inventory.service.CommerceInventoryWarehouseItemLocalService;
import com.liferay.commerce.inventory.service.CommerceInventoryWarehouseItemService;
import com.liferay.commerce.inventory.type.CommerceInventoryAuditType;
import com.liferay.commerce.inventory.type.CommerceInventoryAuditTypeRegistry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.math.BigDecimal;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	property = {
		"commerce.inventory.method.key=" + CommerceInventoryConstants.DEFAULT_METHOD_KEY,
		"commerce.inventory.method.order:Integer=100"
	},
	service = CommerceInventoryMethod.class
)
public class DefaultCommerceInventoryMethodImpl
	implements CommerceInventoryMethod {

	@Override
	@Transactional(
		propagation = Propagation.REQUIRED, rollbackFor = Exception.class
	)
	public void consumeQuantity(
			long userId, long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey, BigDecimal quantity, long bookedQuantityId,
			Map<String, String> context)
		throws PortalException {

		if (bookedQuantityId > 0) {
			_commerceBookedQuantityLocalService.consumeCommerceBookedQuantity(
				bookedQuantityId, quantity);
		}

		decreaseStockQuantity(
			userId, commerceInventoryWarehouseId, sku, unitOfMeasureKey,
			quantity);

		CommerceInventoryAuditType commerceInventoryAuditType =
			_commerceInventoryAuditTypeRegistry.getCommerceInventoryAuditType(
				CommerceInventoryConstants.AUDIT_TYPE_CONSUME_QUANTITY);

		_commerceInventoryAuditLocalService.addCommerceInventoryAudit(
			userId, sku, unitOfMeasureKey, commerceInventoryAuditType.getType(),
			commerceInventoryAuditType.getLog(context), quantity);

		for (CommerceInventoryEngineContributor
				commerceInventoryEngineContributor :
					_commerceInventoryEngineContributorRegistry.
						getCommerceInventoryEngineContributors()) {

			commerceInventoryEngineContributor.consumeQuantityContribute(
				userId, commerceInventoryWarehouseId, sku, unitOfMeasureKey,
				quantity, bookedQuantityId, context);
		}
	}

	@Override
	@Transactional(
		propagation = Propagation.REQUIRED, rollbackFor = Exception.class
	)
	public void decreaseStockQuantity(
			long userId, long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey, BigDecimal quantity)
		throws PortalException {

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				fetchCommerceInventoryWarehouseItem(
					commerceInventoryWarehouseId, sku, unitOfMeasureKey);

		BigDecimal itemQuantity = commerceInventoryWarehouseItem.getQuantity();

		_commerceInventoryWarehouseItemLocalService.
			updateCommerceInventoryWarehouseItem(
				userId,
				commerceInventoryWarehouseItem.
					getCommerceInventoryWarehouseItemId(),
				itemQuantity.subtract(quantity),
				commerceInventoryWarehouseItem.getMvccVersion());

		for (CommerceInventoryEngineContributor
				commerceInventoryEngineContributor :
					_commerceInventoryEngineContributorRegistry.
						getCommerceInventoryEngineContributors()) {

			commerceInventoryEngineContributor.decreaseStockQuantityContribute(
				userId, commerceInventoryWarehouseId, sku, unitOfMeasureKey,
				quantity);
		}
	}

	@Override
	public String getAvailabilityStatus(
		long companyId, long commerceChannelGroupId,
		BigDecimal minStockQuantity, String sku, String unitOfMeasureKey) {

		return _getAvailabilityStatus(
			minStockQuantity,
			getStockQuantity(
				companyId, commerceChannelGroupId, sku, unitOfMeasureKey));
	}

	@Override
	public String getKey() {
		return CommerceInventoryConstants.DEFAULT_METHOD_KEY;
	}

	@Override
	public String getLabel(Locale locale) {
		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());

		return _language.get(resourceBundle, getKey());
	}

	@Override
	public BigDecimal getStockQuantity(
		long companyId, long commerceChannelGroupId, String sku,
		String unitOfMeasureKey) {

		BigDecimal stockQuantity =
			_commerceInventoryWarehouseItemService.getStockQuantity(
				companyId, commerceChannelGroupId, sku, unitOfMeasureKey);

		return stockQuantity.subtract(
			_commerceBookedQuantityLocalService.getCommerceBookedQuantity(
				companyId, commerceChannelGroupId, sku, unitOfMeasureKey));
	}

	@Override
	public BigDecimal getStockQuantity(
		long companyId, String sku, String unitOfMeasureKey) {

		BigDecimal stockQuantity =
			_commerceInventoryWarehouseItemService.getStockQuantity(
				companyId, sku, unitOfMeasureKey);

		return stockQuantity.subtract(
			_commerceBookedQuantityLocalService.getCommerceBookedQuantity(
				companyId, sku, unitOfMeasureKey));
	}

	@Override
	public boolean hasStockQuantity(
		long companyId, String sku, String unitOfMeasureKey,
		BigDecimal quantity) {

		int compareTo = quantity.compareTo(
			getStockQuantity(companyId, sku, unitOfMeasureKey));

		if (compareTo <= 0) {
			return true;
		}

		return false;
	}

	@Override
	@Transactional(
		propagation = Propagation.REQUIRED, rollbackFor = Exception.class
	)
	public void increaseStockQuantity(
			long userId, long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey, BigDecimal quantity)
		throws PortalException {

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				fetchCommerceInventoryWarehouseItem(
					commerceInventoryWarehouseId, sku, unitOfMeasureKey);

		BigDecimal itemQuantity = commerceInventoryWarehouseItem.getQuantity();

		try {
			_commerceInventoryWarehouseItemLocalService.
				updateCommerceInventoryWarehouseItem(
					userId,
					commerceInventoryWarehouseItem.
						getCommerceInventoryWarehouseItemId(),
					itemQuantity.add(quantity),
					commerceInventoryWarehouseItem.getMvccVersion());
		}
		catch (MVCCException mvccException) {
			_log.error(mvccException);

			throw mvccException;
		}

		CommerceInventoryAuditType commerceInventoryAuditType =
			_commerceInventoryAuditTypeRegistry.getCommerceInventoryAuditType(
				CommerceInventoryConstants.AUDIT_TYPE_INCREASE_QUANTITY);

		_commerceInventoryAuditLocalService.addCommerceInventoryAudit(
			userId, sku, unitOfMeasureKey, commerceInventoryAuditType.getType(),
			commerceInventoryAuditType.getLog(null), quantity);

		for (CommerceInventoryEngineContributor
				commerceInventoryEngineContributor :
					_commerceInventoryEngineContributorRegistry.
						getCommerceInventoryEngineContributors()) {

			commerceInventoryEngineContributor.increaseStockQuantityContribute(
				userId, commerceInventoryWarehouseId, sku, unitOfMeasureKey,
				quantity);
		}
	}

	private String _getAvailabilityStatus(
		BigDecimal minStockQuantity, BigDecimal stockQuantity) {

		String availabilityStatus =
			CommerceInventoryAvailabilityConstants.UNAVAILABLE;

		boolean available = false;

		if (stockQuantity.compareTo(minStockQuantity) > 0) {
			available = true;
		}

		if (available) {
			availabilityStatus =
				CommerceInventoryAvailabilityConstants.AVAILABLE;
		}

		return availabilityStatus;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DefaultCommerceInventoryMethodImpl.class);

	@Reference
	private CommerceInventoryBookedQuantityLocalService
		_commerceBookedQuantityLocalService;

	@Reference
	private CommerceInventoryAuditLocalService
		_commerceInventoryAuditLocalService;

	@Reference
	private CommerceInventoryAuditTypeRegistry
		_commerceInventoryAuditTypeRegistry;

	@Reference
	private CommerceInventoryEngineContributorRegistry
		_commerceInventoryEngineContributorRegistry;

	@Reference
	private CommerceInventoryWarehouseItemLocalService
		_commerceInventoryWarehouseItemLocalService;

	@Reference
	private CommerceInventoryWarehouseItemService
		_commerceInventoryWarehouseItemService;

	@Reference
	private Language _language;

}