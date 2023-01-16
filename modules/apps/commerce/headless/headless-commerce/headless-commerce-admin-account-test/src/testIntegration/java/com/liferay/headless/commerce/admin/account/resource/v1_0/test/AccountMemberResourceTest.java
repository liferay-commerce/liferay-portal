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
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.commerce.admin.account.client.dto.v1_0.AccountMember;
import com.liferay.headless.commerce.admin.account.client.serdes.v1_0.AccountMemberSerDes;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alessio Antonio Rendina
 */
@RunWith(Arquillian.class)
public class AccountMemberResourceTest
	extends BaseAccountMemberResourceTestCase {

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

	@Override
	@Test
	public void testDeleteAccountByExternalReferenceCodeAccountMember()
		throws Exception {

		AccountMember accountMember = randomAccountMember();

		accountMemberResource.postAccountByExternalReferenceCodeAccountMember(
			_accountEntry.getExternalReferenceCode(), accountMember);

		assertHttpResponseStatusCode(
			204,
			accountMemberResource.
				deleteAccountByExternalReferenceCodeAccountMemberHttpResponse(
					_accountEntry.getExternalReferenceCode(),
					accountMember.getUserId()));

		assertHttpResponseStatusCode(
			404,
			accountMemberResource.
				getAccountByExternalReferenceCodeAccountMemberHttpResponse(
					_accountEntry.getExternalReferenceCode(),
					accountMember.getUserId()));
	}

	@Override
	@Test
	public void testDeleteAccountIdAccountMember() throws Exception {
		AccountMember accountMember = randomAccountMember();

		accountMemberResource.postAccountIdAccountMember(
			_accountEntry.getAccountEntryId(), accountMember);

		assertHttpResponseStatusCode(
			204,
			accountMemberResource.deleteAccountIdAccountMemberHttpResponse(
				_accountEntry.getAccountEntryId(), accountMember.getUserId()));

		assertHttpResponseStatusCode(
			404,
			accountMemberResource.getAccountIdAccountMemberHttpResponse(
				_accountEntry.getAccountEntryId(), accountMember.getUserId()));
	}

	@Override
	@Test
	public void testGetAccountByExternalReferenceCodeAccountMember()
		throws Exception {

		AccountMember accountMember1 = randomAccountMember();

		accountMemberResource.postAccountByExternalReferenceCodeAccountMember(
			_accountEntry.getExternalReferenceCode(), accountMember1);

		accountMember1.setAccountId(_accountEntry.getAccountEntryId());

		AccountMember accountMember2 =
			accountMemberResource.
				getAccountByExternalReferenceCodeAccountMember(
					_accountEntry.getExternalReferenceCode(),
					accountMember1.getUserId());

		assertEquals(accountMember1, accountMember2);
	}

	@Override
	@Test
	public void testGetAccountIdAccountMember() throws Exception {
		AccountMember accountMember1 = randomAccountMember();

		accountMemberResource.postAccountIdAccountMember(
			_accountEntry.getAccountEntryId(), accountMember1);

		accountMember1.setAccountId(_accountEntry.getAccountEntryId());

		AccountMember accountMember2 =
			accountMemberResource.getAccountIdAccountMember(
				_accountEntry.getAccountEntryId(), accountMember1.getUserId());

		assertEquals(accountMember1, accountMember2);
	}

	@Override
	@Test
	public void testGraphQLGetAccountByExternalReferenceCodeAccountMember()
		throws Exception {

		GraphQLField graphQLField = new GraphQLField(
			"accountByExternalReferenceCodeAccountMembers",
			HashMapBuilder.<String, Object>put(
				"externalReferenceCode",
				"\"" + _accountEntry.getExternalReferenceCode() + "\""
			).put(
				"page", 1
			).put(
				"pageSize", 10
			).build(),
			new GraphQLField("items", getGraphQLFields()),
			new GraphQLField("page"), new GraphQLField("totalCount"));

		JSONObject accountMembersJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/accountByExternalReferenceCodeAccountMembers");

		long totalCount = accountMembersJSONObject.getLong("totalCount");

		AccountMember accountMember1 =
			accountMemberResource.
				postAccountByExternalReferenceCodeAccountMember(
					_accountEntry.getExternalReferenceCode(),
					randomAccountMember());
		AccountMember accountMember2 =
			accountMemberResource.
				postAccountByExternalReferenceCodeAccountMember(
					_accountEntry.getExternalReferenceCode(),
					randomAccountMember());

		accountMembersJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/accountByExternalReferenceCodeAccountMembers");

		Assert.assertEquals(
			totalCount + 2, accountMembersJSONObject.getLong("totalCount"));

		assertContains(
			accountMember1,
			Arrays.asList(
				AccountMemberSerDes.toDTOs(
					accountMembersJSONObject.getString("items"))));
		assertContains(
			accountMember2,
			Arrays.asList(
				AccountMemberSerDes.toDTOs(
					accountMembersJSONObject.getString("items"))));
	}

	@Override
	@Test
	public void testGraphQLGetAccountByExternalReferenceCodeAccountMemberNotFound()
		throws Exception {

		GraphQLField graphQLField = new GraphQLField(
			"accountByExternalReferenceCodeAccountMembers",
			HashMapBuilder.<String, Object>put(
				"externalReferenceCode",
				"\"" + RandomTestUtil.randomString() + "\""
			).put(
				"page", 1
			).put(
				"pageSize", 10
			).build(),
			new GraphQLField("items", getGraphQLFields()),
			new GraphQLField("page"), new GraphQLField("totalCount"));

		Assert.assertEquals(
			"Not Found",
			JSONUtil.getValueAsString(
				invokeGraphQLQuery(graphQLField), "JSONArray/errors",
				"Object/0", "JSONObject/extensions", "Object/code"));
	}

	@Override
	@Test
	public void testGraphQLGetAccountIdAccountMember() throws Exception {
		GraphQLField graphQLField = new GraphQLField(
			"accountIdAccountMembers",
			HashMapBuilder.<String, Object>put(
				"id", _accountEntry.getAccountEntryId()
			).put(
				"page", 1
			).put(
				"pageSize", 10
			).build(),
			new GraphQLField("items", getGraphQLFields()),
			new GraphQLField("page"), new GraphQLField("totalCount"));

		JSONObject accountMembersJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/accountIdAccountMembers");

		long totalCount = accountMembersJSONObject.getLong("totalCount");

		AccountMember accountMember1 =
			accountMemberResource.
				postAccountByExternalReferenceCodeAccountMember(
					_accountEntry.getExternalReferenceCode(),
					randomAccountMember());
		AccountMember accountMember2 =
			accountMemberResource.
				postAccountByExternalReferenceCodeAccountMember(
					_accountEntry.getExternalReferenceCode(),
					randomAccountMember());

		accountMembersJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/accountIdAccountMembers");

		Assert.assertEquals(
			totalCount + 2, accountMembersJSONObject.getLong("totalCount"));

		assertContains(
			accountMember1,
			Arrays.asList(
				AccountMemberSerDes.toDTOs(
					accountMembersJSONObject.getString("items"))));
		assertContains(
			accountMember2,
			Arrays.asList(
				AccountMemberSerDes.toDTOs(
					accountMembersJSONObject.getString("items"))));
	}

	@Override
	@Test
	public void testGraphQLGetAccountIdAccountMemberNotFound()
		throws Exception {

		GraphQLField graphQLField = new GraphQLField(
			"accountIdAccountMembers",
			HashMapBuilder.<String, Object>put(
				"id", _accountEntry.getAccountEntryId()
			).put(
				"page", 1
			).put(
				"pageSize", 10
			).build(),
			new GraphQLField("items", getGraphQLFields()),
			new GraphQLField("page"), new GraphQLField("totalCount"));

		JSONObject accountMembersJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/accountIdAccountMembers");

		Assert.assertEquals(0, accountMembersJSONObject.getLong("totalCount"));
	}

	@Override
	@Test
	public void testPatchAccountByExternalReferenceCodeAccountMember()
		throws Exception {

		AccountMember accountMember1 = randomAccountMember();

		accountMemberResource.postAccountByExternalReferenceCodeAccountMember(
			_accountEntry.getExternalReferenceCode(), accountMember1);

		accountMember1.setAccountId(_accountEntry.getAccountEntryId());

		accountMemberResource.patchAccountByExternalReferenceCodeAccountMember(
			_accountEntry.getExternalReferenceCode(),
			accountMember1.getUserId(), accountMember1);

		AccountMember accountMember2 =
			accountMemberResource.
				getAccountByExternalReferenceCodeAccountMember(
					_accountEntry.getExternalReferenceCode(),
					accountMember1.getUserId());

		assertEquals(accountMember1, accountMember2);
	}

	@Override
	@Test
	public void testPatchAccountIdAccountMember() throws Exception {
		AccountMember accountMember1 = randomAccountMember();

		accountMemberResource.postAccountIdAccountMember(
			_accountEntry.getAccountEntryId(), accountMember1);

		accountMember1.setAccountId(_accountEntry.getAccountEntryId());

		accountMemberResource.patchAccountIdAccountMember(
			_accountEntry.getAccountEntryId(), accountMember1.getUserId(),
			accountMember1);

		AccountMember accountMember2 =
			accountMemberResource.getAccountIdAccountMember(
				_accountEntry.getAccountEntryId(), accountMember1.getUserId());

		assertEquals(accountMember1, accountMember2);
	}

	@Override
	@Test
	public void testPostAccountByExternalReferenceCodeAccountMember()
		throws Exception {

		super.testPostAccountByExternalReferenceCodeAccountMember();

		AccountMember accountMember1 = _randomAccountMember();

		accountMember1 =
			accountMemberResource.
				postAccountByExternalReferenceCodeAccountMember(
					_accountEntry.getExternalReferenceCode(), accountMember1);

		accountMember1.setAccountId(_accountEntry.getAccountEntryId());

		AccountMember accountMember2 =
			accountMemberResource.
				getAccountByExternalReferenceCodeAccountMember(
					_accountEntry.getExternalReferenceCode(),
					accountMember1.getUserId());

		assertEquals(accountMember1, accountMember2);
	}

	@Override
	@Test
	public void testPostAccountIdAccountMember() throws Exception {
		super.testPostAccountIdAccountMember();

		AccountMember accountMember1 = _randomAccountMember();

		accountMember1 = accountMemberResource.postAccountIdAccountMember(
			_accountEntry.getAccountEntryId(), accountMember1);

		accountMember1.setAccountId(_accountEntry.getAccountEntryId());

		AccountMember accountMember2 =
			accountMemberResource.getAccountIdAccountMember(
				_accountEntry.getAccountEntryId(), accountMember1.getUserId());

		assertEquals(accountMember1, accountMember2);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name", "email"};
	}

	@Override
	protected AccountMember randomAccountMember() throws Exception {
		User user = UserTestUtil.addUser(testCompany);

		return new AccountMember() {
			{
				email = user.getEmailAddress();
				externalReferenceCode = user.getExternalReferenceCode();
				name = user.getFullName();
				userId = user.getUserId();
			}
		};
	}

	@Override
	protected AccountMember
			testGetAccountByExternalReferenceCodeAccountMembersPage_addAccountMember(
				String externalReferenceCode, AccountMember accountMember)
		throws Exception {

		return accountMemberResource.
			postAccountByExternalReferenceCodeAccountMember(
				_accountEntry.getExternalReferenceCode(), accountMember);
	}

	@Override
	protected String
			testGetAccountByExternalReferenceCodeAccountMembersPage_getExternalReferenceCode()
		throws Exception {

		return _accountEntry.getExternalReferenceCode();
	}

	@Override
	protected AccountMember testGetAccountIdAccountMembersPage_addAccountMember(
			Long id, AccountMember accountMember)
		throws Exception {

		return accountMemberResource.postAccountIdAccountMember(
			id, accountMember);
	}

	@Override
	protected Long testGetAccountIdAccountMembersPage_getId() throws Exception {
		return _accountEntry.getAccountEntryId();
	}

	@Override
	protected AccountMember
			testPostAccountByExternalReferenceCodeAccountMember_addAccountMember(
				AccountMember accountMember)
		throws Exception {

		accountMemberResource.postAccountByExternalReferenceCodeAccountMember(
			_accountEntry.getExternalReferenceCode(), accountMember);

		accountMember.setAccountId(_accountEntry.getAccountEntryId());

		return accountMemberResource.
			getAccountByExternalReferenceCodeAccountMember(
				_accountEntry.getExternalReferenceCode(),
				accountMember.getUserId());
	}

	@Override
	protected AccountMember testPostAccountIdAccountMember_addAccountMember(
			AccountMember accountMember)
		throws Exception {

		accountMemberResource.postAccountIdAccountMember(
			_accountEntry.getAccountEntryId(), accountMember);

		accountMember.setAccountId(_accountEntry.getAccountEntryId());

		return accountMemberResource.getAccountIdAccountMember(
			_accountEntry.getAccountEntryId(), accountMember.getUserId());
	}

	private AccountMember _randomAccountMember() throws Exception {
		User user = UserTestUtil.addUser(testCompany);

		_users.add(user);

		return new AccountMember() {
			{
				email = user.getEmailAddress();
				name = user.getFullName();
			}
		};
	}

	@Inject
	private static AccountEntryLocalService _accountEntryLocalService;

	@DeleteAfterTestRun
	private AccountEntry _accountEntry;

	@DeleteAfterTestRun
	private User _user;

	@DeleteAfterTestRun
	private final List<User> _users = new ArrayList<>();

}