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

package com.liferay.headless.commerce.admin.account.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.commerce.admin.account.client.dto.v1_0.AccountGroup;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alessio Antonio Rendina
 */
@RunWith(Arquillian.class)
public class AccountGroupResourceTest extends BaseAccountGroupResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_accountEntry = _accountEntryLocalService.addAccountEntry(
			_user.getUserId(), AccountConstants.PARENT_ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null,
			RandomTestUtil.randomString(),
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS, 1,
			ServiceContextTestUtil.getServiceContext(
				testCompany.getCompanyId(), testGroup.getGroupId(),
				_user.getUserId()));
	}

	@Ignore
	@Override
	@Test
	public void testDeleteAccountGroup() throws Exception {
		super.testDeleteAccountGroup();
	}

	@Ignore
	@Override
	@Test
	public void testDeleteAccountGroupByExternalReferenceCode()
		throws Exception {

		super.testDeleteAccountGroupByExternalReferenceCode();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLDeleteAccountGroup() throws Exception {
		super.testGraphQLDeleteAccountGroup();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetAccountGroupNotFound() throws Exception {
		super.testGraphQLGetAccountGroupNotFound();
	}

	@Override
	@Test
	public void testPatchAccountGroup() throws Exception {
		AccountGroup postAccountGroup = _postAccountGroup(randomAccountGroup());

		AccountGroup randomPatchAccountGroup = randomPatchAccountGroup();

		accountGroupResource.patchAccountGroup(
			postAccountGroup.getId(), randomPatchAccountGroup);

		AccountGroup expectedPatchAccountGroup = postAccountGroup.clone();

		BeanTestUtil.copyProperties(
			randomPatchAccountGroup, expectedPatchAccountGroup);

		AccountGroup getAccountGroup = accountGroupResource.getAccountGroup(
			postAccountGroup.getId());

		assertEquals(expectedPatchAccountGroup, getAccountGroup);
		assertValid(getAccountGroup);
	}

	@Override
	@Test
	public void testPatchAccountGroupByExternalReferenceCode()
		throws Exception {

		AccountGroup postAccountGroup = _postAccountGroup(randomAccountGroup());

		AccountGroup randomPatchAccountGroup = randomPatchAccountGroup();

		accountGroupResource.patchAccountGroupByExternalReferenceCode(
			postAccountGroup.getExternalReferenceCode(),
			randomPatchAccountGroup);

		AccountGroup expectedPatchAccountGroup = postAccountGroup.clone();

		BeanTestUtil.copyProperties(
			randomPatchAccountGroup, expectedPatchAccountGroup);

		AccountGroup getAccountGroup =
			accountGroupResource.getAccountGroupByExternalReferenceCode(
				postAccountGroup.getExternalReferenceCode());

		assertEquals(expectedPatchAccountGroup, getAccountGroup);
		assertValid(getAccountGroup);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name"};
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
	protected AccountGroup
			testGetAccountByExternalReferenceCodeAccountGroupsPage_addAccountGroup(
				String externalReferenceCode, AccountGroup accountGroup)
		throws Exception {

		return _postAccountGroup(accountGroup);
	}

	@Override
	protected String
			testGetAccountByExternalReferenceCodeAccountGroupsPage_getExternalReferenceCode()
		throws Exception {

		return _accountEntry.getExternalReferenceCode();
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
			AccountGroup accountGroup)
		throws Exception {

		return _postAccountGroup(accountGroup);
	}

	@Override
	protected AccountGroup testGetAccountIdAccountGroupsPage_addAccountGroup(
			Long id, AccountGroup accountGroup)
		throws Exception {

		return _postAccountGroup(accountGroup);
	}

	protected Long testGetAccountIdAccountGroupsPage_getId() throws Exception {
		return _accountEntry.getAccountEntryId();
	}

	@Override
	protected AccountGroup testGraphQLAccountGroup_addAccountGroup()
		throws Exception {

		return _postAccountGroup(randomAccountGroup());
	}

	@Override
	protected AccountGroup testPostAccountGroup_addAccountGroup(
			AccountGroup accountGroup)
		throws Exception {

		return _postAccountGroup(accountGroup);
	}

	private AccountGroup _postAccountGroup(AccountGroup accountGroup)
		throws Exception {

		AccountGroup postAccountGroup = accountGroupResource.postAccountGroup(
			accountGroup);

		_accountGroups.add(
			_accountGroupLocalService.getAccountGroup(
				postAccountGroup.getId()));

		_accountGroupRelLocalService.addAccountGroupRel(
			postAccountGroup.getId(), AccountEntry.class.getName(),
			_accountEntry.getAccountEntryId());

		return postAccountGroup;
	}

	@Inject
	private static AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private static AccountGroupLocalService _accountGroupLocalService;

	@Inject
	private static AccountGroupRelLocalService _accountGroupRelLocalService;

	@DeleteAfterTestRun
	private AccountEntry _accountEntry;

	@DeleteAfterTestRun
	private final List<com.liferay.account.model.AccountGroup> _accountGroups =
		new ArrayList<>();

	@DeleteAfterTestRun
	private User _user;

}