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

package com.liferay.headless.admin.user.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.account.service.AccountGroupRelLocalServiceUtil;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.admin.user.client.dto.v1_0.AccountGroup;
import com.liferay.headless.admin.user.client.pagination.Page;
import com.liferay.headless.admin.user.client.pagination.Pagination;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Javier Gamarra
 */
@Ignore
@RunWith(Arquillian.class)
public class AccountGroupResourceTest extends BaseAccountGroupResourceTestCase {

	@Ignore
	@Override
	@Test
	public void testDeleteAccountGroup() throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testDeleteAccountGroupByExternalReferenceCode()
		throws Exception {
	}

	@Override
	@Test
	public void testGetAccountByExternalReferenceCodeAccountGroupsPage()
		throws Exception {

		AccountEntry accountEntry = _accountEntryLocalService.addAccountEntry(
			_serviceContext.getUserId(),
			AccountConstants.PARENT_ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), null, null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			AccountConstants.ACCOUNT_ENTRY_TYPE_GUEST,
			WorkflowConstants.STATUS_APPROVED, _serviceContext);

		com.liferay.account.model.AccountGroup accountGroup1 = _accountGroupLocalService.addAccountGroup(
			_serviceContext.getUserId(), null, RandomTestUtil.randomString(),
			_serviceContext);

		accountGroup1.setExternalReferenceCode(null);
		accountGroup1.setDefaultAccountGroup(false);
		accountGroup1.setType(AccountConstants.ACCOUNT_GROUP_TYPE_STATIC);
		accountGroup1.setExpandoBridgeAttributes(_serviceContext);

		accountGroup1 = _accountGroupLocalService.updateAccountGroup(
			accountGroup1);

		AccountGroupRelLocalServiceUtil.addAccountGroupRel(
			accountGroup1.getAccountGroupId(), AccountEntry.class.getName(),
			accountEntry.getAccountEntryId());

		com.liferay.account.model.AccountGroup accountGroup2 = _accountGroupLocalService.addAccountGroup(
			_serviceContext.getUserId(), null, RandomTestUtil.randomString(),
			_serviceContext);

		accountGroup2.setExternalReferenceCode(null);
		accountGroup2.setDefaultAccountGroup(false);
		accountGroup2.setType(AccountConstants.ACCOUNT_GROUP_TYPE_STATIC);
		accountGroup2.setExpandoBridgeAttributes(_serviceContext);

		accountGroup2 = _accountGroupLocalService.updateAccountGroup(
			accountGroup2);

		AccountGroupRelLocalServiceUtil.addAccountGroupRel(
			accountGroup2.getAccountGroupId(), AccountEntry.class.getName(),
			accountEntry.getAccountEntryId());

		Page<AccountGroup> page =
			accountGroupResource.
				getAccountByExternalReferenceCodeAccountGroupsPage(
					accountEntry.getExternalReferenceCode(),
					Pagination.of(1, 20));

		Assert.assertEquals(2, page.getTotalCount());

		List<Long> accountGroupIds = new ArrayList<>();

		accountGroupIds.add(accountGroup1.getAccountGroupId());
		accountGroupIds.add(accountGroup2.getAccountGroupId());

		for (AccountGroup AccountGroup : page.getItems()) {
			Assert.assertTrue(
				accountGroupIds.contains(AccountGroup.getId()));
		}
	}

	@Ignore
	@Override
	@Test
	public void testGetAccountByExternalReferenceCodeAccountGroupsPageWithPagination()
		throws Exception {
	}

	@Override
	@Test
	public void testGetAccountAccountGroupsPage() throws Exception {
		AccountEntry accountEntry = _accountEntryLocalService.addAccountEntry(
			_serviceContext.getUserId(),
			AccountConstants.PARENT_ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), null, null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			AccountConstants.ACCOUNT_ENTRY_TYPE_GUEST,
			WorkflowConstants.STATUS_APPROVED, _serviceContext);

		com.liferay.account.model.AccountGroup accountGroup1 = _accountGroupLocalService.addAccountGroup(
			_serviceContext.getUserId(), null, RandomTestUtil.randomString(),
			_serviceContext);

		accountGroup1.setExternalReferenceCode(null);
		accountGroup1.setDefaultAccountGroup(false);
		accountGroup1.setType(AccountConstants.ACCOUNT_GROUP_TYPE_STATIC);
		accountGroup1.setExpandoBridgeAttributes(_serviceContext);

		accountGroup1 = _accountGroupLocalService.updateAccountGroup(
			accountGroup1);

		AccountGroupRelLocalServiceUtil.addAccountGroupRel(
			accountGroup1.getAccountGroupId(), AccountEntry.class.getName(),
			accountEntry.getAccountEntryId());

		com.liferay.account.model.AccountGroup accountGroup2 = _accountGroupLocalService.addAccountGroup(
			_serviceContext.getUserId(), null, RandomTestUtil.randomString(),
			_serviceContext);

		accountGroup2.setExternalReferenceCode(null);
		accountGroup2.setDefaultAccountGroup(false);
		accountGroup2.setType(AccountConstants.ACCOUNT_GROUP_TYPE_STATIC);
		accountGroup2.setExpandoBridgeAttributes(_serviceContext);

		accountGroup2 = _accountGroupLocalService.updateAccountGroup(
			accountGroup2);

		AccountGroupRelLocalServiceUtil.addAccountGroupRel(
			accountGroup2.getAccountGroupId(), AccountEntry.class.getName(),
			accountEntry.getAccountEntryId());

		Page<AccountGroup> page =
			accountGroupResource.getAccountAccountGroupsPage(
				accountEntry.getAccountEntryId(), Pagination.of(1, 20));

		Assert.assertEquals(2, page.getTotalCount());

		List<Long> accountGroupsIds = new ArrayList<>();

		accountGroupsIds.add(accountGroup1.getAccountGroupId());
		accountGroupsIds.add(accountGroup2.getAccountGroupId());

		for (AccountGroup AccountGroup : page.getItems()) {
			Assert.assertTrue(
				accountGroupsIds.contains(AccountGroup.getId()));
		}
	}

	@Ignore
	@Override
	@Test
	public void testGetAccountAccountGroupsPageWithPagination()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetAccountGroup() throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetAccountGroupByExternalReferenceCode()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetAccountGroupByExternalReferenceCodeNotFound()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetAccountGroupNotFound() throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testPatchAccountGroup() throws Exception {
		Assert.assertTrue(false);
	}

	@Ignore
	@Override
	@Test
	public void testPatchAccountGroupByExternalReferenceCode()
		throws Exception {

		Assert.assertTrue(false);
	}

	@Override
	protected AccountGroup testDeleteAccountGroup_addAccountGroup()
		throws Exception {

		return _postAccountGroup(randomAccountGroup());
	}

	@Override
	protected AccountGroup
	testDeleteAccountGroupByExternalReferenceCode_addAccountGroup()
		throws Exception {

		return _postAccountGroup(randomAccountGroup());
	}

	@Override
	protected AccountGroup testGetAccountGroup_addAccountGroup()
		throws Exception {

		return _postAccountGroup(randomAccountGroup());
	}

	@Override
	protected AccountGroup
	testGetAccountGroupByExternalReferenceCode_addAccountGroup()
		throws Exception {

		return _postAccountGroup(randomAccountGroup());
	}

	@Override
	protected AccountGroup testGetAccountGroupsPage_addAccountGroup(
		AccountGroup AccountGroup)
		throws Exception {

		return _postAccountGroup(AccountGroup);
	}

	@Override
	protected AccountGroup
	testGraphQLAccountGroup_addAccountGroup()
		throws Exception {

		return _postAccountGroup(randomAccountGroup());
	}

	@Override
	protected AccountGroup testPostAccountGroup_addAccountGroup(
		AccountGroup AccountGroup)
		throws Exception {

		return _postAccountGroup(AccountGroup);
	}

	private AccountGroup _postAccountGroup(
		AccountGroup AccountGroup)
		throws Exception {

		return accountGroupResource.postAccountGroup(AccountGroup);
	}

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private AccountGroupLocalService _accountGroupLocalService;

	private ServiceContext _serviceContext;
	private User _user;
}