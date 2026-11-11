/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.content.web.internal.info.collection.provider;

import com.liferay.account.model.AccountEntry;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.product.catalog.CPQuery;
import com.liferay.commerce.product.helper.CPDefinitionHelper;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.service.CPDefinitionService;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.util.CommerceContextThreadLocal;
import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.filter.KeywordsInfoFilter;
import com.liferay.info.pagination.InfoPage;
import com.liferay.info.pagination.Pagination;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseModelSearchResult;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.io.Serializable;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alec Sloan
 */
@Component(service = InfoCollectionProvider.class)
public class CPDefinitionInfoCollectionProvider
	implements InfoCollectionProvider<CPDefinition> {

	@Override
	public InfoPage<CPDefinition> getCollectionInfoPage(
		CollectionQuery collectionQuery) {

		try {
			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			long commerceChannelGroupId =
				_commerceChannelLocalService.
					getCommerceChannelGroupIdBySiteGroupId(
						serviceContext.getScopeGroupId());

			String keywords = null;

			KeywordsInfoFilter keywordsInfoFilter =
				collectionQuery.getInfoFilter(KeywordsInfoFilter.class);

			if (keywordsInfoFilter != null) {
				keywords = keywordsInfoFilter.getKeywords();
			}

			Pagination pagination = collectionQuery.getPagination();

			com.liferay.info.sort.Sort infoSort = collectionQuery.getSort();

			if (commerceChannelGroupId != 0) {
				CPQuery cpQuery = _getCPQuery(infoSort);
				SearchContext searchContext = _getSearchContext(
					serviceContext.getCompanyId(), keywords);

				return InfoPage.of(
					_cpDefinitionHelper.searchCPDefinitions(
						commerceChannelGroupId, searchContext, cpQuery,
						pagination.getStart(), pagination.getEnd()),
					pagination,
					(int)_cpDefinitionHelper.searchCount(
						commerceChannelGroupId, searchContext, cpQuery));
			}

			Sort sort = null;

			if (infoSort != null) {
				sort = new Sort(
					infoSort.getFieldName(), Sort.LONG_TYPE,
					infoSort.isReverse());
			}

			BaseModelSearchResult<CPDefinition>
				cpDefinitionBaseModelSearchResult =
					_cpDefinitionService.searchCPDefinitions(
						serviceContext.getCompanyId(), keywords,
						WorkflowConstants.STATUS_APPROVED, false,
						pagination.getStart(), pagination.getEnd(), sort);

			return InfoPage.of(
				cpDefinitionBaseModelSearchResult.getBaseModels(),
				collectionQuery.getPagination(),
				cpDefinitionBaseModelSearchResult.getLength());
		}
		catch (Exception exception) {
			_log.error("Unable to get cpDefinitions", exception);
		}

		return InfoPage.of(
			Collections.emptyList(), collectionQuery.getPagination(), 0);
	}

	@Override
	public String getLabel(Locale locale) {
		return _language.get(locale, "products");
	}

	@Override
	public boolean isAvailable() {
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		return !Objects.equals(
			portletDisplay.getPortletName(), PortletKeys.ITEM_SELECTOR);
	}

	private CPQuery _getCPQuery(com.liferay.info.sort.Sort infoSort) {
		CPQuery cpQuery = new CPQuery();

		if (infoSort == null) {
			cpQuery.setOrderByCol1(Field.CREATE_DATE);
			cpQuery.setOrderByType1("ASC");

			return cpQuery;
		}

		cpQuery.setOrderByCol1(infoSort.getFieldName());

		if (infoSort.isReverse()) {
			cpQuery.setOrderByType1("DESC");
		}
		else {
			cpQuery.setOrderByType1("ASC");
		}

		return cpQuery;
	}

	private SearchContext _getSearchContext(long companyId, String keywords)
		throws PortalException {

		SearchContext searchContext = new SearchContext();

		long accountEntryId = 0;
		long[] commerceAccountGroupIds = null;

		CommerceContext commerceContext = CommerceContextThreadLocal.get();

		if (commerceContext != null) {
			AccountEntry accountEntry = commerceContext.getAccountEntry();

			if (accountEntry != null) {
				accountEntryId = accountEntry.getAccountEntryId();
				commerceAccountGroupIds =
					commerceContext.getCommerceAccountGroupIds();
			}
		}

		searchContext.setAttributes(
			HashMapBuilder.<String, Serializable>put(
				Field.STATUS, WorkflowConstants.STATUS_APPROVED
			).put(
				"accountEntryId", accountEntryId
			).put(
				"commerceAccountGroupIds", commerceAccountGroupIds
			).build());
		searchContext.setCompanyId(companyId);

		if (Validator.isNotNull(keywords)) {
			searchContext.setKeywords(keywords);
		}

		return searchContext;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CPDefinitionInfoCollectionProvider.class);

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference
	private CPDefinitionHelper _cpDefinitionHelper;

	@Reference
	private CPDefinitionService _cpDefinitionService;

	@Reference
	private Language _language;

}