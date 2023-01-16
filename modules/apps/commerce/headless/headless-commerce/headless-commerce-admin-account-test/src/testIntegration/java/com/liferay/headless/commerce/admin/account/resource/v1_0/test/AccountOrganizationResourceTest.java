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
import com.liferay.headless.commerce.admin.account.client.dto.v1_0.AccountOrganization;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
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
public class AccountOrganizationResourceTest
	extends BaseAccountOrganizationResourceTestCase {

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
	public void testDeleteAccountByExternalReferenceCodeAccountOrganization()
		throws Exception {

		AccountOrganization accountOrganization =
			accountOrganizationResource.
				postAccountByExternalReferenceCodeAccountOrganization(
					_accountEntry.getExternalReferenceCode(),
					randomAccountOrganization());

		assertHttpResponseStatusCode(
			204,
			accountOrganizationResource.
				deleteAccountByExternalReferenceCodeAccountOrganizationHttpResponse(
					_accountEntry.getExternalReferenceCode(),
					accountOrganization.getOrganizationId()));

		assertHttpResponseStatusCode(
			404,
			accountOrganizationResource.
				getAccountByExternalReferenceCodeAccountOrganizationHttpResponse(
					_accountEntry.getExternalReferenceCode(),
					accountOrganization.getOrganizationId()));

		assertHttpResponseStatusCode(
			404,
			accountOrganizationResource.
				getAccountByExternalReferenceCodeAccountOrganizationHttpResponse(
					_accountEntry.getExternalReferenceCode(),
					accountOrganization.getOrganizationId()));
	}

	@Ignore
	@Override
	@Test
	public void testDeleteAccountIdAccountOrganization() throws Exception {
		AccountOrganization accountOrganization =
			accountOrganizationResource.postAccountIdAccountOrganization(
				_accountEntry.getAccountEntryId(), randomAccountOrganization());

		assertHttpResponseStatusCode(
			204,
			accountOrganizationResource.
				deleteAccountIdAccountOrganizationHttpResponse(
					_accountEntry.getAccountEntryId(),
					accountOrganization.getOrganizationId()));

		assertHttpResponseStatusCode(
			404,
			accountOrganizationResource.
				getAccountIdAccountOrganizationHttpResponse(
					_accountEntry.getAccountEntryId(),
					accountOrganization.getOrganizationId()));

		assertHttpResponseStatusCode(
			404,
			accountOrganizationResource.
				getAccountIdAccountOrganizationHttpResponse(
					_accountEntry.getAccountEntryId(),
					accountOrganization.getOrganizationId()));
	}

	@Override
	@Test
	public void testGetAccountByExternalReferenceCodeAccountOrganization()
		throws Exception {

		AccountOrganization accountOrganization1 = randomAccountOrganization();

		accountOrganizationResource.
			postAccountByExternalReferenceCodeAccountOrganization(
				_accountEntry.getExternalReferenceCode(), accountOrganization1);

		accountOrganization1.setAccountId(_accountEntry.getAccountEntryId());

		AccountOrganization accountOrganization2 =
			accountOrganizationResource.
				getAccountByExternalReferenceCodeAccountOrganization(
					_accountEntry.getExternalReferenceCode(),
					accountOrganization1.getOrganizationId());

		assertEquals(accountOrganization1, accountOrganization2);
	}

	@Override
	@Test
	public void testGetAccountIdAccountOrganization() throws Exception {
		AccountOrganization accountOrganization1 = randomAccountOrganization();

		accountOrganizationResource.postAccountIdAccountOrganization(
			_accountEntry.getAccountEntryId(), accountOrganization1);

		accountOrganization1.setAccountId(_accountEntry.getAccountEntryId());

		AccountOrganization accountOrganization2 =
			accountOrganizationResource.getAccountIdAccountOrganization(
				_accountEntry.getAccountEntryId(),
				accountOrganization1.getOrganizationId());

		assertEquals(accountOrganization1, accountOrganization2);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name"};
	}

	protected AccountOrganization randomAccountOrganization() throws Exception {
		Organization localOrganization =
			_organizationLocalService.addOrganization(
				_user.getUserId(), 0, RandomTestUtil.randomString(), false);

		_organizations.add(localOrganization);

		return new AccountOrganization() {
			{
				accountId = _accountEntry.getAccountEntryId();
				name = localOrganization.getName();
				organizationExternalReferenceCode =
					localOrganization.getExternalReferenceCode();
				organizationId = localOrganization.getOrganizationId();
				treePath = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
			}
		};
	}

	@Override
	protected AccountOrganization
			testGetAccountByExternalReferenceCodeAccountOrganizationsPage_addAccountOrganization(
				String externalReferenceCode,
				AccountOrganization accountOrganization)
		throws Exception {

		return accountOrganizationResource.
			postAccountByExternalReferenceCodeAccountOrganization(
				externalReferenceCode, accountOrganization);
	}

	@Override
	protected String
			testGetAccountByExternalReferenceCodeAccountOrganizationsPage_getExternalReferenceCode()
		throws Exception {

		return _accountEntry.getExternalReferenceCode();
	}

	@Override
	protected AccountOrganization
			testGetAccountIdAccountOrganizationsPage_addAccountOrganization(
				Long id, AccountOrganization accountOrganization)
		throws Exception {

		return accountOrganizationResource.postAccountIdAccountOrganization(
			id, accountOrganization);
	}

	protected Long testGetAccountIdAccountOrganizationsPage_getId()
		throws Exception {

		return _accountEntry.getAccountEntryId();
	}

	@Override
	protected AccountOrganization
			testPostAccountByExternalReferenceCodeAccountOrganization_addAccountOrganization(
				AccountOrganization accountOrganization)
		throws Exception {

		return accountOrganizationResource.
			postAccountByExternalReferenceCodeAccountOrganization(
				_accountEntry.getExternalReferenceCode(), accountOrganization);
	}

	@Override
	protected AccountOrganization
			testPostAccountIdAccountOrganization_addAccountOrganization(
				AccountOrganization accountOrganization)
		throws Exception {

		return accountOrganizationResource.postAccountIdAccountOrganization(
			_accountEntry.getAccountEntryId(), accountOrganization);
	}

	@Inject
	private static AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private static OrganizationLocalService _organizationLocalService;

	@DeleteAfterTestRun
	private AccountEntry _accountEntry;

	@DeleteAfterTestRun
	private final List<Organization> _organizations = new ArrayList<>();

	@DeleteAfterTestRun
	private User _user;

}