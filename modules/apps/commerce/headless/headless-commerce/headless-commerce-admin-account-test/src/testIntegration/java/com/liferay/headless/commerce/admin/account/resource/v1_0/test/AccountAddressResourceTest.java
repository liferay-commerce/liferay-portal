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
import com.liferay.headless.commerce.admin.account.client.dto.v1_0.AccountAddress;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CountryLocalService;
import com.liferay.portal.kernel.service.RegionLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alessio Antonio Rendina
 */
@RunWith(Arquillian.class)
public class AccountAddressResourceTest
	extends BaseAccountAddressResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				testCompany.getCompanyId(), testGroup.getGroupId(),
				_user.getUserId());

		_accountEntry = _accountEntryLocalService.addAccountEntry(
			_user.getUserId(), AccountConstants.PARENT_ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null,
			RandomTestUtil.randomString(),
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS, 1, serviceContext);

		_country = _countryLocalService.addCountry(
			"XY", "XYZ", true, true, RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.nextDouble(), true, true, false, serviceContext);

		_region = _regionLocalService.addRegion(
			_country.getCountryId(), true, RandomTestUtil.randomString(),
			RandomTestUtil.nextDouble(), RandomTestUtil.randomString(),
			serviceContext);
	}

	@Ignore
	@Override
	@Test
	public void testDeleteAccountAddressByExternalReferenceCode()
		throws Exception {

		super.testDeleteAccountAddressByExternalReferenceCode();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLDeleteAccountAddress() throws Exception {
		super.testGraphQLDeleteAccountAddress();
	}

	@Override
	@Test
	public void testPatchAccountAddressByExternalReferenceCode()
		throws Exception {

		AccountAddress postAccountAddress =
			testPatchAccountAddress_addAccountAddress();

		AccountAddress randomPatchAccountAddress = randomPatchAccountAddress();

		accountAddressResource.patchAccountAddressByExternalReferenceCode(
			postAccountAddress.getExternalReferenceCode(),
			randomPatchAccountAddress);

		AccountAddress expectedPatchAccountAddress = postAccountAddress.clone();

		BeanTestUtil.copyProperties(
			randomPatchAccountAddress, expectedPatchAccountAddress);

		AccountAddress getAccountAddress =
			accountAddressResource.getAccountAddressByExternalReferenceCode(
				postAccountAddress.getExternalReferenceCode());

		assertEquals(expectedPatchAccountAddress, getAccountAddress);
		assertValid(getAccountAddress);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"city", "countryISOCode", "description", "name", "phoneNumber",
			"regionISOCode", "street1", "street2", "street3", "type", "zip"
		};
	}

	@Override
	protected AccountAddress randomAccountAddress() throws Exception {
		return new AccountAddress() {
			{
				city = StringUtil.toLowerCase(RandomTestUtil.randomString());
				countryISOCode = _country.getA2();
				defaultBilling = RandomTestUtil.randomBoolean();
				defaultShipping = RandomTestUtil.randomBoolean();
				description = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				id = RandomTestUtil.randomLong();
				latitude = RandomTestUtil.randomDouble();
				longitude = RandomTestUtil.randomDouble();
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				phoneNumber = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				regionISOCode = _region.getRegionCode();
				street1 = StringUtil.toLowerCase(RandomTestUtil.randomString());
				street2 = StringUtil.toLowerCase(RandomTestUtil.randomString());
				street3 = StringUtil.toLowerCase(RandomTestUtil.randomString());
				type = 2;
				zip = StringUtil.toLowerCase(RandomTestUtil.randomString());
			}
		};
	}

	@Override
	protected AccountAddress testDeleteAccountAddress_addAccountAddress()
		throws Exception {

		return accountAddressResource.postAccountIdAccountAddress(
			_accountEntry.getAccountEntryId(), randomAccountAddress());
	}

	@Override
	protected AccountAddress
			testDeleteAccountAddressByExternalReferenceCode_addAccountAddress()
		throws Exception {

		return accountAddressResource.
			postAccountByExternalReferenceCodeAccountAddress(
				_accountEntry.getExternalReferenceCode(),
				randomAccountAddress());
	}

	@Override
	protected AccountAddress testGetAccountAddress_addAccountAddress()
		throws Exception {

		return accountAddressResource.postAccountIdAccountAddress(
			_accountEntry.getAccountEntryId(), randomAccountAddress());
	}

	@Override
	protected AccountAddress
			testGetAccountAddressByExternalReferenceCode_addAccountAddress()
		throws Exception {

		return accountAddressResource.
			postAccountByExternalReferenceCodeAccountAddress(
				_accountEntry.getExternalReferenceCode(),
				randomAccountAddress());
	}

	@Override
	protected AccountAddress
			testGetAccountByExternalReferenceCodeAccountAddressesPage_addAccountAddress(
				String externalReferenceCode, AccountAddress accountAddress)
		throws Exception {

		return accountAddressResource.
			postAccountByExternalReferenceCodeAccountAddress(
				externalReferenceCode, accountAddress);
	}

	@Override
	protected String
			testGetAccountByExternalReferenceCodeAccountAddressesPage_getExternalReferenceCode()
		throws Exception {

		return _accountEntry.getExternalReferenceCode();
	}

	@Override
	protected AccountAddress
			testGetAccountIdAccountAddressesPage_addAccountAddress(
				Long id, AccountAddress accountAddress)
		throws Exception {

		return accountAddressResource.postAccountIdAccountAddress(
			id, accountAddress);
	}

	@Override
	protected Long testGetAccountIdAccountAddressesPage_getId()
		throws Exception {

		return _accountEntry.getAccountEntryId();
	}

	@Override
	protected AccountAddress testGraphQLAccountAddress_addAccountAddress()
		throws Exception {

		return accountAddressResource.postAccountIdAccountAddress(
			_accountEntry.getAccountEntryId(), randomAccountAddress());
	}

	@Override
	protected AccountAddress testPatchAccountAddress_addAccountAddress()
		throws Exception {

		return accountAddressResource.postAccountIdAccountAddress(
			_accountEntry.getAccountEntryId(), randomAccountAddress());
	}

	@Override
	protected AccountAddress
			testPostAccountByExternalReferenceCodeAccountAddress_addAccountAddress(
				AccountAddress accountAddress)
		throws Exception {

		return accountAddressResource.
			postAccountByExternalReferenceCodeAccountAddress(
				_accountEntry.getExternalReferenceCode(), accountAddress);
	}

	@Override
	protected AccountAddress testPostAccountIdAccountAddress_addAccountAddress(
			AccountAddress accountAddress)
		throws Exception {

		return accountAddressResource.postAccountIdAccountAddress(
			_accountEntry.getAccountEntryId(), accountAddress);
	}

	@Override
	protected AccountAddress testPutAccountAddress_addAccountAddress()
		throws Exception {

		return accountAddressResource.postAccountIdAccountAddress(
			_accountEntry.getAccountEntryId(), randomAccountAddress());
	}

	@Inject
	private static AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private static CountryLocalService _countryLocalService;

	@Inject
	private static RegionLocalService _regionLocalService;

	@DeleteAfterTestRun
	private AccountEntry _accountEntry;

	@DeleteAfterTestRun
	private Country _country;

	@DeleteAfterTestRun
	private Region _region;

	@DeleteAfterTestRun
	private User _user;

}