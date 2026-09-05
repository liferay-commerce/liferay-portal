/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.util.v1_0;

import com.liferay.asset.kernel.exception.NoSuchCategoryException;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.Category;
import com.liferay.petra.function.UnsafeBiFunction;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import jakarta.validation.ValidationException;

/**
 * @author Lianne Louie
 */
public class CategoryUtil {

	public static Long getAssetCategoryId(
			Category category, long groupId,
			UnsafeBiFunction<String, Long, AssetCategory, Exception>
				unsafeBiFunction)
		throws Exception {

		String externalReferenceCode = category.getExternalReferenceCode();

		if (Validator.isNull(externalReferenceCode)) {
			if (category.getId() == null) {
				throw new ValidationException(
					"Category must have an external reference code or an ID");
			}

			return null;
		}

		AssetCategory assetCategory = unsafeBiFunction.apply(
			externalReferenceCode,
			GetterUtil.getLong(category.getSiteId(), groupId));

		if (assetCategory == null) {
			throw new NoSuchCategoryException(
				"Unable to find category with external reference code " +
					externalReferenceCode);
		}

		return assetCategory.getCategoryId();
	}

}