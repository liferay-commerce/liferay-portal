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

package com.liferay.headless.commerce.delivery.catalog.internal.util.v1_0;

import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.SkuOption;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.SkuOptionValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Crescenzo Rega
 */
public class SkuOptionUtil {

	public static SkuOption[] getSkuOptions(
			Map<CPDefinitionOptionRel, List<CPDefinitionOptionValueRel>>
				cpDefinitionOptionRelsMap,
			String languageId)
		throws Exception {

		List<SkuOption> skuOptions = new ArrayList<>();

		for (Map.Entry<CPDefinitionOptionRel, List<CPDefinitionOptionValueRel>>
				entry : cpDefinitionOptionRelsMap.entrySet()) {

			CPDefinitionOptionRel cpDefinitionOptionRel = entry.getKey();

			SkuOption skuOption = new SkuOption() {
				{
					id = cpDefinitionOptionRel.getCPDefinitionOptionRelId();
					key = cpDefinitionOptionRel.getKey();
					name = cpDefinitionOptionRel.getName(languageId);
					priceContributor =
						cpDefinitionOptionRel.isPriceContributor();
					required = cpDefinitionOptionRel.isRequired();
					skuContributor = cpDefinitionOptionRel.isSkuContributor();
					skuOptionValues = _toSkuOptionValues(
						entry.getValue(), languageId);
					value = cpDefinitionOptionRel.getKey();
				}
			};

			skuOptions.add(skuOption);
		}

		return skuOptions.toArray(new SkuOption[0]);
	}

	private static SkuOptionValue[] _toSkuOptionValues(
		List<CPDefinitionOptionValueRel> cpDefinitionOptionValueRels,
		String languageId) {

		List<SkuOptionValue> skuOptionValues = new ArrayList<>();

		for (CPDefinitionOptionValueRel cpDefinitionOptionValueRel :
				cpDefinitionOptionValueRels) {

			SkuOptionValue skuOptionValue = new SkuOptionValue() {
				{
					id =
						cpDefinitionOptionValueRel.
							getCPDefinitionOptionValueRelId();
					key = cpDefinitionOptionValueRel.getKey();
					name = cpDefinitionOptionValueRel.getName(languageId);
					preselected = cpDefinitionOptionValueRel.isPreselected();
				}
			};

			skuOptionValues.add(skuOptionValue);
		}

		return skuOptionValues.toArray(new SkuOptionValue[0]);
	}

}