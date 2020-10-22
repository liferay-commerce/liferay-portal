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

package com.liferay.commerce.internal.upgrade.v5_0_0;

import com.liferay.commerce.internal.upgrade.base.BaseCommerceServiceUpgradeProcess;
import com.liferay.commerce.internal.upgrade.v5_0_0.util.CommerceRegionTable;
import com.liferay.commerce.model.CommerceRegion;
import com.liferay.commerce.model.CommerceRegionLocalization;
import com.liferay.commerce.service.CommerceRegionLocalService;
import com.liferay.commerce.service.persistence.CommerceRegionLocalizationPersistence;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.util.List;

/**
 * @author Luca Pellizzon
 */
public class CommerceRegionUpgradeProcess
	extends BaseCommerceServiceUpgradeProcess {

	public CommerceRegionUpgradeProcess(
		CommerceRegionLocalService commerceRegionLocalService,
		CommerceRegionLocalizationPersistence
			commerceRegionLocalizationPersistence,
		CounterLocalService counterLocalService) {

		_commerceRegionLocalService = commerceRegionLocalService;
		_commerceRegionLocalizationPersistence =
			commerceRegionLocalizationPersistence;
		_counterLocalService = counterLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		Language language = LanguageUtil.getLanguage();

		String languageId = language.getLanguageId(LocaleUtil.US);

		addColumn(
			CommerceRegionTable.class, CommerceRegionTable.TABLE_NAME,
			"defaultLanguageId", "VARCHAR(75) default " + languageId);

		List<CommerceRegion> commerceRegions =
			_commerceRegionLocalService.getCommerceRegions(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (CommerceRegion commerceRegion : commerceRegions) {
			long commerceRegionLocalizationId =
				_counterLocalService.increment();

			CommerceRegionLocalization commerceRegionLocalization =
				_commerceRegionLocalizationPersistence.create(
					commerceRegionLocalizationId);

			commerceRegionLocalization.setCompanyId(
				commerceRegion.getCompanyId());
			commerceRegionLocalization.setCommerceRegionId(
				commerceRegion.getCommerceRegionId());
			commerceRegionLocalization.setLanguageId(languageId);
			commerceRegionLocalization.setName(commerceRegion.getName());

			_commerceRegionLocalizationPersistence.update(
				commerceRegionLocalization);
		}

		dropColumn(CommerceRegionTable.TABLE_NAME, "name");
	}

	private final CommerceRegionLocalizationPersistence
		_commerceRegionLocalizationPersistence;
	private final CommerceRegionLocalService _commerceRegionLocalService;
	private final CounterLocalService _counterLocalService;

}