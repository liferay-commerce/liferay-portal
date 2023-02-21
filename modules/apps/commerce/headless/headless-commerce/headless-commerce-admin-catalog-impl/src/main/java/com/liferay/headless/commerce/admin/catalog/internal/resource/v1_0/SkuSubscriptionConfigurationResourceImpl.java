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

package com.liferay.headless.commerce.admin.catalog.internal.resource.v1_0;

import com.liferay.commerce.product.exception.NoSuchCPInstanceException;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.service.CPInstanceService;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.Sku;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.SkuSubscriptionConfiguration;
import com.liferay.headless.commerce.admin.catalog.internal.dto.v1_0.converter.SkuSubscriptionConfigurationDTOConverter;
import com.liferay.headless.commerce.admin.catalog.resource.v1_0.SkuSubscriptionConfigurationResource;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.fields.NestedField;
import com.liferay.portal.vulcan.fields.NestedFieldId;
import com.liferay.portal.vulcan.fields.NestedFieldSupport;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Zoltán Takács
 * @author Crescenzo Rega
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/sku-subscription-configuration.properties",
	scope = ServiceScope.PROTOTYPE,
	service = {
		NestedFieldSupport.class, SkuSubscriptionConfigurationResource.class
	}
)
public class SkuSubscriptionConfigurationResourceImpl
	extends BaseSkuSubscriptionConfigurationResourceImpl
	implements NestedFieldSupport {

	@Override
	public SkuSubscriptionConfiguration
			getSkuByExternalReferenceCodeSubscriptionConfiguration(
				String externalReferenceCode)
		throws Exception {

		CPInstance cpInstance = _cpInstanceService.fetchByExternalReferenceCode(
			externalReferenceCode, contextCompany.getCompanyId());

		if (cpInstance == null) {
			throw new NoSuchCPInstanceException(
				"Unable to find Sku with external reference code: " +
					externalReferenceCode);
		}

		return _toSkuSubscriptionConfiguration(cpInstance.getCPInstanceId());
	}

	@NestedField(parentClass = Sku.class, value = "subscriptionConfiguration")
	@Override
	public SkuSubscriptionConfiguration getSkuIdSubscriptionConfiguration(
			@NestedFieldId(value = "id") Long id)
		throws Exception {

		CPInstance cpInstance = _cpInstanceService.fetchCPInstance(id);

		if (cpInstance == null) {
			throw new NoSuchCPInstanceException(
				"Unable to find Sku with ID: " + id);
		}

		return _toSkuSubscriptionConfiguration(cpInstance.getCPInstanceId());
	}

	@Override
	public SkuSubscriptionConfiguration
			patchSkuByExternalReferenceCodeSubscriptionConfiguration(
				String externalReferenceCode,
				SkuSubscriptionConfiguration skuSubscriptionConfiguration)
		throws Exception {

		CPInstance cpInstance = _cpInstanceService.fetchByExternalReferenceCode(
			externalReferenceCode, contextCompany.getCompanyId());

		if (cpInstance == null) {
			throw new NoSuchCPInstanceException(
				"Unable to find Sku with external reference code: " +
					externalReferenceCode);
		}

		return _updateSkuSubscriptionConfiguration(
			cpInstance, skuSubscriptionConfiguration);
	}

	@Override
	public SkuSubscriptionConfiguration patchSkuIdSubscriptionConfiguration(
			Long id, SkuSubscriptionConfiguration skuSubscriptionConfiguration)
		throws Exception {

		CPInstance cpInstance = _cpInstanceService.fetchCPInstance(id);

		if (cpInstance == null) {
			throw new NoSuchCPInstanceException(
				"Unable to find Sku with ID: " + id);
		}

		return _updateSkuSubscriptionConfiguration(
			cpInstance, skuSubscriptionConfiguration);
	}

	private SkuSubscriptionConfiguration _toSkuSubscriptionConfiguration(
			Long cpInstanceId)
		throws Exception {

		return _skuSubscriptionConfigurationDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				cpInstanceId, contextAcceptLanguage.getPreferredLocale()));
	}

	private SkuSubscriptionConfiguration _updateSkuSubscriptionConfiguration(
			CPInstance cpInstance,
			SkuSubscriptionConfiguration skuSubscriptionConfiguration)
		throws Exception {

		String subscriptionTypeValue = null;

		SkuSubscriptionConfiguration.SubscriptionType subscriptionType =
			skuSubscriptionConfiguration.getSubscriptionType();

		if (subscriptionType != null) {
			subscriptionTypeValue = subscriptionType.getValue();
		}

		cpInstance = _cpInstanceService.updateSubscriptionInfo(
			cpInstance.getCPInstanceId(),
			GetterUtil.get(
				skuSubscriptionConfiguration.getOverrideSubscriptionInfo(),
				cpInstance.isOverrideSubscriptionInfo()),
			GetterUtil.get(
				skuSubscriptionConfiguration.getEnable(),
				cpInstance.isSubscriptionEnabled()),
			GetterUtil.get(
				skuSubscriptionConfiguration.getLength(),
				cpInstance.getSubscriptionLength()),
			subscriptionTypeValue,
			UnicodePropertiesBuilder.create(
				skuSubscriptionConfiguration.getSubscriptionTypeSettings(), true
			).build(),
			GetterUtil.get(
				skuSubscriptionConfiguration.getNumberOfLength(),
				cpInstance.getMaxSubscriptionCycles()),
			cpInstance.isDeliverySubscriptionEnabled(),
			cpInstance.getDeliverySubscriptionLength(),
			cpInstance.getDeliverySubscriptionType(),
			cpInstance.getDeliverySubscriptionTypeSettingsProperties(),
			cpInstance.getDeliveryMaxSubscriptionCycles());

		return _toSkuSubscriptionConfiguration(cpInstance.getCPInstanceId());
	}

	@Reference
	private CPInstanceService _cpInstanceService;

	@Reference
	private SkuSubscriptionConfigurationDTOConverter
		_skuSubscriptionConfigurationDTOConverter;

}