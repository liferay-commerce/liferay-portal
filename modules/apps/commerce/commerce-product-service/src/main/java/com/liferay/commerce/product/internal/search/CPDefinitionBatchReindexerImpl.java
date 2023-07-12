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

package com.liferay.commerce.product.internal.search;

import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.search.batch.BatchIndexingActionable;
import com.liferay.portal.search.indexer.IndexerDocumentBuilder;
import com.liferay.portal.search.indexer.IndexerWriter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(service = CPDefinitionBatchReindexer.class)
public class CPDefinitionBatchReindexerImpl
	implements CPDefinitionBatchReindexer {

	@Override
	public void reindex(long cpDefinitionId, long companyId) {
		BatchIndexingActionable batchIndexingActionable =
			indexerWriter.getBatchIndexingActionable();

		batchIndexingActionable.setAddCriteriaMethod(
			dynamicQuery -> {
				Property cpDefinitionIdProperty = PropertyFactoryUtil.forName(
					"cpDefinitionId");

				dynamicQuery.add(cpDefinitionIdProperty.eq(cpDefinitionId));
			});
		batchIndexingActionable.setCompanyId(companyId);
		batchIndexingActionable.setPerformActionMethod(
			(CPDefinition cpDefinition) -> batchIndexingActionable.addDocuments(
				indexerDocumentBuilder.getDocument(cpDefinition)));

		batchIndexingActionable.performActions();
	}

	@Reference
	protected CPDefinitionLocalService cpDefinitionLocalService;

	@Reference(
		target = "(indexer.class.name=com.liferay.commerce.product.model.CPDefinition)"
	)
	protected IndexerDocumentBuilder indexerDocumentBuilder;

	@Reference(
		target = "(indexer.class.name=com.liferay.commerce.product.model.CPDefinition)"
	)
	protected IndexerWriter<CPDefinition> indexerWriter;

}