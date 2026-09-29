/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.internal.model.listener;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.site.configuration.manager.SitemapConfigurationManager;
import com.liferay.site.constants.SitemapConstants;
import com.liferay.site.service.SiteSitemapRegenerationEntryLocalService;

import org.osgi.service.component.annotations.Reference;

/**
 * @author Cheryl Tang
 */
public abstract class BaseCPDefinitionSitemapModelListener
	<T extends BaseModel<T>>
		extends BaseModelListener<T> {

	protected void addSiteSitemapRegenerationEntry(long companyId) {
		try {
			if (!sitemapConfigurationManager.isCachedGenerationCompanyEnabled(
					companyId) ||
				!sitemapConfigurationManager.isIndexModeAssetTypeCompanyEnabled(
					companyId)) {

				return;
			}

			siteSitemapRegenerationEntryLocalService.
				addSiteSitemapRegenerationEntry(
					SitemapConstants.ASSET_TYPE_KEY_COMMERCE_PRODUCTS,
					companyId, _COMPANY_SCOPED_GROUP_ID);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to add XML sitemap regeneration entry", exception);
		}
	}

	@Reference
	protected SiteSitemapRegenerationEntryLocalService
		siteSitemapRegenerationEntryLocalService;

	@Reference
	protected SitemapConfigurationManager sitemapConfigurationManager;

	private static final long _COMPANY_SCOPED_GROUP_ID = 0;

	private static final Log _log = LogFactoryUtil.getLog(
		BaseCPDefinitionSitemapModelListener.class);

}