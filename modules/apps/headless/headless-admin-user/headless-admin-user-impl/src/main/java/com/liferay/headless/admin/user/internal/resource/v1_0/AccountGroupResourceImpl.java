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

package com.liferay.headless.admin.user.internal.resource.v1_0;

import com.liferay.account.constants.AccountActionKeys;
import com.liferay.account.exception.NoSuchEntryException;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryService;
import com.liferay.account.service.AccountGroupService;
import com.liferay.headless.admin.user.dto.v1_0.AccountGroup;
import com.liferay.headless.admin.user.internal.dto.v1_0.converter.AccountGroupResourceDTOConverter;
import com.liferay.headless.admin.user.internal.dto.v1_0.util.CustomFieldsUtil;
import com.liferay.headless.admin.user.resource.v1_0.AccountGroupResource;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.SearchUtil;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Javier Gamarra
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/account-group.properties",
	scope = ServiceScope.PROTOTYPE, service = AccountGroupResource.class
)
public class AccountGroupResourceImpl extends BaseAccountGroupResourceImpl {

	@Override
	public Response deleteAccountGroup(Long id) throws Exception {
		_accountGroupService.deleteAccountGroup(id);

		Response.ResponseBuilder responseBuilder = Response.ok();

		return responseBuilder.build();
	}

	@Override
	public Response deleteAccountGroupByExternalReferenceCode(
			String externalReferenceCode)
		throws Exception {
	}

	@Override
	public Page<AccountGroup>
			getAccountByExternalReferenceCodeAccountGroupsPage(
				String externalReferenceCode, Pagination pagination)
		throws Exception {

		AccountEntry accountEntry =
			_accountEntryService.fetchAccountEntryByExternalReferenceCode(
				contextCompany.getCompanyId(), externalReferenceCode);

		if (accountEntry == null) {
			throw new NoSuchEntryException(
				"Unable to find account with external reference code " +
					externalReferenceCode);
		}

		return _getAdminAccountGroups(
			accountEntry.getAccountEntryId(), pagination);
	}

	@Override
	public AccountGroup getAccountGroup(Long id) throws Exception {
		return _accountGroupResourceDTOConverter.toDTO(
			_getDTOConverterContext(id));
	}

	@Override
	public AccountGroup getAccountGroupByExternalReferenceCode(
			String externalReferenceCode)
		throws Exception {

		return _toAccountGroup(
			_accountGroupResourceDTOConverter.getObject(externalReferenceCode));
	}

	@Override
	public Page<AccountGroup> getAccountGroupsPage(
			Filter filter, Pagination pagination, Sort[] sorts)
		throws Exception {

		return SearchUtil.search(
			HashMapBuilder.<String, Map<String, String>>put(
				"create",
				addAction(
					AccountActionKeys.ADD_ACCOUNT_GROUP, "postAccountGroup",
					PortletKeys.PORTAL, 0L)
			).put(
				"get",
				addAction(
					ActionKeys.VIEW, 0L, "getAccountGroupsPage",
					_accountGroupModelResourcePermission)
			).build(),
			booleanQuery -> booleanQuery.getPreBooleanFilter(), filter,
			com.liferay.account.model.AccountGroup.class.getName(),
			StringPool.BLANK, pagination,
			queryConfig -> queryConfig.setSelectedFieldNames(
				Field.ENTRY_CLASS_PK),
			searchContext -> searchContext.setCompanyId(
				contextCompany.getCompanyId()),
			sorts,
			document -> _toAccountGroup(
				_accountGroupService.getAccountGroup(
					GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK)))));
	}

	@Override
	public Page<AccountGroup> getAccountIdAccountGroupsPage(
			Long id, Pagination pagination)
		throws Exception {

		return _getAdminAccountGroups(id, pagination);
	}

	@Override
	public Response patchAccountGroup(Long id, AccountGroup accountGroup)
		throws Exception {

		_accountGroupService.updateAccountGroup(
			id, accountGroup.getDescription(), accountGroup.getName(),
			ServiceContextFactory.getInstance(contextHttpServletRequest));

		Response.ResponseBuilder responseBuilder = Response.ok();

		return responseBuilder.build();
	}

	@Override
	public void patchAccountGroupByExternalReferenceCode(
			String externalReferenceCode, AccountGroup accountGroup)
		throws Exception {

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			contextHttpServletRequest);

		// Expando

		serviceContext.setExpandoBridgeAttributes(
			CustomFieldsUtil.toMap(
				com.liferay.account.model.AccountGroup.class.getName(),
				contextCompany.getCompanyId(), accountGroup.getCustomFields(),
				contextAcceptLanguage.getPreferredLocale()));

		_accountGroupService.updateAccountGroup(
			_accountGroupResourceDTOConverter.getAccountGroupId(
				externalReferenceCode),
			accountGroup.getDescription(), accountGroup.getName(),
			serviceContext);
	}

	@Override
	public AccountGroup postAccountGroup(AccountGroup accountGroup)
		throws Exception {

		com.liferay.account.model.AccountGroup existingAccountGroup = null;

		if (Validator.isNotNull(accountGroup.getExternalReferenceCode())) {
			existingAccountGroup =
				_accountGroupService.fetchAccountGroupByExternalReferenceCode(
					accountGroup.getExternalReferenceCode(),
					contextCompany.getCompanyId());
		}

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			contextHttpServletRequest);

		if (existingAccountGroup == null) {
			existingAccountGroup = _accountGroupService.addAccountGroup(
				contextCompany.getCompanyId(), accountGroup.getDescription(),
				accountGroup.getName(), serviceContext);

			existingAccountGroup =
				_accountGroupService.updateExternalReferenceCode(
					existingAccountGroup.getAccountGroupId(),
					accountGroup.getExternalReferenceCode());
		}
		else {
			existingAccountGroup = _accountGroupService.updateAccountGroup(
				existingAccountGroup.getAccountGroupId(),
				accountGroup.getDescription(), accountGroup.getName(),
				serviceContext);
		}

		// Expando

		serviceContext.setExpandoBridgeAttributes(
			CustomFieldsUtil.toMap(
				com.liferay.account.model.AccountGroup.class.getName(),
				contextCompany.getCompanyId(), accountGroup.getCustomFields(),
				contextAcceptLanguage.getPreferredLocale()));

		_accountGroupService.updateAccountGroup(
			_accountGroupResourceDTOConverter.getAccountGroupId(
				existingAccountGroup.getExternalReferenceCode()),
			accountGroup.getDescription(), accountGroup.getName(),
			serviceContext);

		return _accountGroupResourceDTOConverter.toDTO(
			_getDTOConverterContext(existingAccountGroup.getAccountGroupId()));
	}

	private Page<AccountGroup> _getAdminAccountGroups(
			long accountEntryId, Pagination pagination)
		throws Exception {

		return Page.of(
			transform(
				_accountGroupService.getAccountGroupsByAccountEntryId(
					accountEntryId, pagination.getStartPosition(),
					pagination.getEndPosition()),
				accountGroup -> _toAccountGroup(accountGroup)),
			pagination,
			_accountGroupService.getAccountGroupsCountByAccountEntryId(
				accountEntryId));
	}

	private DTOConverterContext _getDTOConverterContext(long accountGroupId) {
		return new DefaultDTOConverterContext(
			contextAcceptLanguage.isAcceptAllLanguages(),
			HashMapBuilder.<String, Map<String, String>>put(
				"delete",
				addAction(
					ActionKeys.DELETE, accountGroupId, "deleteAccountGroup",
					_accountGroupModelResourcePermission)
			).put(
				"delete-by-external-reference-code",
				addAction(
					ActionKeys.DELETE, accountGroupId,
					"deleteAccountGroupByExternalReferenceCode",
					_accountGroupModelResourcePermission)
			).put(
				"get",
				addAction(
					ActionKeys.VIEW, accountGroupId, "getAccountGroup",
					_accountGroupModelResourcePermission)
			).put(
				"get-by-external-reference-code",
				addAction(
					ActionKeys.VIEW, accountGroupId,
					"getAccountGroupByExternalReferenceCode",
					_accountGroupModelResourcePermission)
			).put(
				"update",
				addAction(
					ActionKeys.UPDATE, accountGroupId, "patchAccountGroup",
					_accountGroupModelResourcePermission)
			).put(
				"update-by-external-reference-code",
				addAction(
					ActionKeys.UPDATE, accountGroupId,
					"patchAccountGroupByExternalReferenceCode",
					_accountGroupModelResourcePermission)
			).build(),
			null, contextHttpServletRequest, accountGroupId,
			contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
			contextUser);
	}

	private AccountGroup _toAccountGroup(
			com.liferay.account.model.AccountGroup accountGroup)
		throws Exception {

		return _accountGroupResourceDTOConverter.toDTO(
			_getDTOConverterContext(accountGroup.getAccountGroupId()));
	}

	@Reference
	private AccountEntryService _accountEntryService;

	@Reference(
		target = "(model.class.name=com.liferay.account.model.AccountGroup)"
	)
	private ModelResourcePermission<com.liferay.account.model.AccountGroup>
		_accountGroupModelResourcePermission;

	@Reference
	private AccountGroupResourceDTOConverter _accountGroupResourceDTOConverter;

	@Reference
	private AccountGroupService _accountGroupService;

}