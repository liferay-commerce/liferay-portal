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

package com.liferay.commerce.product.internal.search.spi.model.index.contributor;

import com.liferay.commerce.product.constants.CPField;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Localization;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(
	property = "indexer.class.name=com.liferay.commerce.product.model.CPInstance",
	service = ModelDocumentContributor.class
)
public class CPInstanceModelDocumentContributor
	implements ModelDocumentContributor<CPInstance> {

	@Override
	public void contribute(Document document, CPInstance cpInstance) {
		try {
			if (_log.isDebugEnabled()) {
				_log.debug("Indexing CPInstance " + cpInstance);
			}

			CPDefinition cpDefinition = cpInstance.getCPDefinition();

			document.addKeyword(
				CPField.CP_DEFINITION_ID, cpInstance.getCPDefinitionId());
			document.addKeyword(
				CPField.CP_DEFINITION_STATUS, cpDefinition.getStatus());
			document.addDateSortable(
				CPField.DISPLAY_DATE, cpInstance.getDisplayDate());
			document.addKeyword(
				CPField.EXTERNAL_REFERENCE_CODE,
				cpInstance.getExternalReferenceCode());
			document.addText(
				CPField.EXTERNAL_REFERENCE_CODE,
				cpInstance.getExternalReferenceCode());
			document.addKeyword(
				CPField.HAS_CHILD_CP_DEFINITIONS,
				_cpDefinitionLocalService.hasChildCPDefinitions(
					cpDefinition.getCPDefinitionId()));
			document.addKeyword(CPField.PUBLISHED, cpInstance.isPublished());
			document.addKeyword(
				CPField.PURCHASABLE, cpInstance.isPurchasable());
			document.addTextSortable(CPField.SKU, cpInstance.getSku());
			document.addKeyword(CPField.UNSPSC, cpInstance.getUnspsc());
			document.addText(Field.CONTENT, cpInstance.getSku());
			document.addText(Field.NAME, cpDefinition.getName());

			List<String> languageIds =
				_cpDefinitionLocalService.
					getCPDefinitionLocalizationLanguageIds(
						cpInstance.getCPDefinitionId());

			String cpDefinitionDefaultLanguageId =
				_localization.getDefaultLanguageId(cpDefinition.getName());

			for (String languageId : languageIds) {
				String name = cpDefinition.getName(languageId);

				if (languageId.equals(cpDefinitionDefaultLanguageId)) {
					document.addText(Field.NAME, name);
					document.addText("defaultLanguageId", languageId);
				}

				document.addText(
					_localization.getLocalizedName(Field.NAME, languageId),
					name);
			}

			CommerceCatalog commerceCatalog = cpDefinition.getCommerceCatalog();

			if (commerceCatalog != null) {
				document.addKeyword(
					"accountEntryId", commerceCatalog.getAccountEntryId());
				document.addKeyword(
					"commerceCatalogId",
					commerceCatalog.getCommerceCatalogId());
			}

			if (_log.isDebugEnabled()) {
				_log.debug("Document " + cpInstance + " indexed successfully");
			}
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to index document" + cpInstance.getCPInstanceId(),
					exception);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CPInstanceModelDocumentContributor.class);

	@Reference
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Reference
	private Localization _localization;

}