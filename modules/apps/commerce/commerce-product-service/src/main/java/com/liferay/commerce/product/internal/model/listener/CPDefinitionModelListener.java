/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.internal.model.listener;

import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.site.configuration.manager.SitemapConfigurationManager;
import com.liferay.site.constants.SitemapConstants;
import com.liferay.site.service.SiteSitemapRegenerationEntryLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Cheryl Tang
 */
@Component(service = ModelListener.class)
public class CPDefinitionModelListener extends BaseModelListener<CPDefinition> {

	@Override
	public void onAfterCreate(CPDefinition cpDefinition) {
		_addSiteSitemapRegenerationEntry(cpDefinition);
	}

	@Override
	public void onAfterRemove(CPDefinition cpDefinition) {
		_addSiteSitemapRegenerationEntry(cpDefinition);
	}

	@Override
	public void onAfterUpdate(
		CPDefinition originalCPDefinition, CPDefinition cpDefinition) {

		_addSiteSitemapRegenerationEntry(cpDefinition);
	}

	private void _addSiteSitemapRegenerationEntry(CPDefinition cpDefinition) {
		try {
			long companyId = cpDefinition.getCompanyId();

			if (!_sitemapConfigurationManager.isCachedGenerationCompanyEnabled(
					companyId) ||
				!_sitemapConfigurationManager.
					isIndexModeAssetTypeCompanyEnabled(companyId)) {

				return;
			}

			_siteSitemapRegenerationEntryLocalService.
				addSiteSitemapRegenerationEntry(
					SitemapConstants.ASSET_TYPE_KEY_COMMERCE_PRODUCTS,
					companyId, _COMPANY_SCOPED_GROUP_ID);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to add XML sitemap regeneration entry", exception);
		}
	}

	private static final long _COMPANY_SCOPED_GROUP_ID = 0;

	private static final Log _log = LogFactoryUtil.getLog(
		CPDefinitionModelListener.class);

	@Reference
	private SitemapConfigurationManager _sitemapConfigurationManager;

	@Reference
	private SiteSitemapRegenerationEntryLocalService
		_siteSitemapRegenerationEntryLocalService;

}