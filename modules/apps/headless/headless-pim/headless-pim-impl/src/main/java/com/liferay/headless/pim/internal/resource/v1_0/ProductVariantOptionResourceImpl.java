/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.pim.internal.resource.v1_0;

import com.liferay.headless.pim.resource.v1_0.ProductVariantOptionResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Stefano Motta
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/product-variant-option.properties",
	scope = ServiceScope.PROTOTYPE,
	service = ProductVariantOptionResource.class
)
public class ProductVariantOptionResourceImpl
	extends BaseProductVariantOptionResourceImpl {
}