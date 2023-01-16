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
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.service.CommerceShippingMethodLocalService;
import com.liferay.commerce.shipping.engine.fixed.model.CommerceShippingFixedOption;
import com.liferay.commerce.shipping.engine.fixed.service.CommerceShippingFixedOptionLocalService;
import com.liferay.headless.commerce.admin.account.client.dto.v1_0.AccountChannelShippingOption;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alessio Antonio Rendina
 */
@RunWith(Arquillian.class)
public class AccountChannelShippingOptionResourceTest
	extends BaseAccountChannelShippingOptionResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_accountEntry = _accountEntryLocalService.addAccountEntry(
			_user.getUserId(), AccountConstants.PARENT_ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null,
			RandomTestUtil.randomString(),
			AccountConstants.ACCOUNT_ENTRY_TYPE_BUSINESS, 1, _serviceContext);

		_commerceCurrency = _commerceCurrencyLocalService.addCommerceCurrency(
			_user.getUserId(), RandomTestUtil.randomString(),
			Collections.singletonMap(
				LocaleUtil.getSiteDefault(), RandomTestUtil.randomString()),
			RandomTestUtil.randomString(), BigDecimal.ONE, new HashMap<>(), 2,
			2, "HALF_EVEN", false, 0, true);

		_commerceShippingMethod =
			_commerceShippingMethodLocalService.addCommerceShippingMethod(
				_user.getUserId(), testGroup.getGroupId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(), true,
				RandomTestUtil.randomString(), null,
				RandomTestUtil.nextDouble(), null);
	}

	@Ignore
	@Override
	@Test
	public void testDeleteAccountChannelShippingOption() throws Exception {
		super.testDeleteAccountChannelShippingOption();
	}

	@Ignore
	@Override
	@Test
	public void testGetAccountByExternalReferenceCodeAccountChannelShippingOptionPageWithPagination()
		throws Exception {

		super.
			testGetAccountByExternalReferenceCodeAccountChannelShippingOptionPageWithPagination();
	}

	@Ignore
	@Override
	@Test
	public void testGetAccountIdAccountChannelShippingOptionPageWithPagination()
		throws Exception {

		super.testGetAccountIdAccountChannelShippingOptionPageWithPagination();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"accountId", "shippingMethodKey", "shippingOptionId"
		};
	}

	@Override
	protected AccountChannelShippingOption randomAccountChannelShippingOption()
		throws Exception {

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.addCommerceChannel(
				RandomTestUtil.randomString(), testGroup.getGroupId(),
				RandomTestUtil.randomString(),
				CommerceChannelConstants.CHANNEL_TYPE_SITE, null,
				_commerceCurrency.getCode(), _serviceContext);

		_commerceChannels.add(commerceChannel);

		CommerceShippingFixedOption shippingFixedOption =
			_commerceShippingFixedOptionLocalService.
				addCommerceShippingFixedOption(
					_user.getUserId(), testGroup.getGroupId(),
					_commerceShippingMethod.getCommerceShippingMethodId(),
					BigDecimal.TEN, RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.randomString(),
					RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.randomDouble());

		_commerceShippingOptions.add(shippingFixedOption);

		return new AccountChannelShippingOption() {
			{
				accountExternalReferenceCode =
					_accountEntry.getExternalReferenceCode();
				accountId = _accountEntry.getAccountEntryId();
				channelExternalReferenceCode =
					commerceChannel.getExternalReferenceCode();
				channelId = commerceChannel.getCommerceChannelId();
				id = RandomTestUtil.randomLong();
				shippingMethodId =
					_commerceShippingMethod.getCommerceShippingMethodId();
				shippingMethodKey = _commerceShippingMethod.getEngineKey();
				shippingOptionId =
					shippingFixedOption.getCommerceShippingFixedOptionId();
				shippingOptionKey = shippingFixedOption.getKey();
			}
		};
	}

	@Override
	protected AccountChannelShippingOption
			testDeleteAccountChannelShippingOption_addAccountChannelShippingOption()
		throws Exception {

		return accountChannelShippingOptionResource.
			postAccountIdAccountChannelShippingOption(
				_accountEntry.getAccountEntryId(),
				randomAccountChannelShippingOption());
	}

	@Override
	protected AccountChannelShippingOption
			testGetAccountByExternalReferenceCodeAccountChannelShippingOptionPage_addAccountChannelShippingOption(
				String externalReferenceCode,
				AccountChannelShippingOption accountChannelShippingOption)
		throws Exception {

		return accountChannelShippingOptionResource.
			postAccountByExternalReferenceCodeAccountChannelShippingOption(
				externalReferenceCode, accountChannelShippingOption);
	}

	@Override
	protected String
			testGetAccountByExternalReferenceCodeAccountChannelShippingOptionPage_getExternalReferenceCode()
		throws Exception {

		return _accountEntry.getExternalReferenceCode();
	}

	@Override
	protected AccountChannelShippingOption
			testGetAccountChannelShippingOption_addAccountChannelShippingOption()
		throws Exception {

		return accountChannelShippingOptionResource.
			postAccountIdAccountChannelShippingOption(
				_accountEntry.getAccountEntryId(),
				randomAccountChannelShippingOption());
	}

	@Override
	protected AccountChannelShippingOption
			testGetAccountIdAccountChannelShippingOptionPage_addAccountChannelShippingOption(
				Long id,
				AccountChannelShippingOption accountChannelShippingOption)
		throws Exception {

		return accountChannelShippingOptionResource.
			postAccountIdAccountChannelShippingOption(
				id, accountChannelShippingOption);
	}

	@Override
	protected Long testGetAccountIdAccountChannelShippingOptionPage_getId()
		throws Exception {

		return _accountEntry.getAccountEntryId();
	}

	@Override
	protected AccountChannelShippingOption
			testGraphQLAccountChannelShippingOption_addAccountChannelShippingOption()
		throws Exception {

		return accountChannelShippingOptionResource.
			postAccountIdAccountChannelShippingOption(
				_accountEntry.getAccountEntryId(),
				randomAccountChannelShippingOption());
	}

	@Override
	protected AccountChannelShippingOption
			testPatchAccountChannelShippingOption_addAccountChannelShippingOption()
		throws Exception {

		return accountChannelShippingOptionResource.
			postAccountIdAccountChannelShippingOption(
				_accountEntry.getAccountEntryId(),
				randomAccountChannelShippingOption());
	}

	@Override
	protected AccountChannelShippingOption
			testPostAccountByExternalReferenceCodeAccountChannelShippingOption_addAccountChannelShippingOption(
				AccountChannelShippingOption accountChannelShippingOption)
		throws Exception {

		return accountChannelShippingOptionResource.
			postAccountByExternalReferenceCodeAccountChannelShippingOption(
				_accountEntry.getExternalReferenceCode(),
				accountChannelShippingOption);
	}

	@Override
	protected AccountChannelShippingOption
			testPostAccountIdAccountChannelShippingOption_addAccountChannelShippingOption(
				AccountChannelShippingOption accountChannelShippingOption)
		throws Exception {

		return accountChannelShippingOptionResource.
			postAccountIdAccountChannelShippingOption(
				_accountEntry.getAccountEntryId(),
				accountChannelShippingOption);
	}

	@Inject
	private static AccountEntryLocalService _accountEntryLocalService;

	@Inject
	private static CommerceChannelLocalService _commerceChannelLocalService;

	@Inject
	private static CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@Inject
	private static CommerceShippingFixedOptionLocalService
		_commerceShippingFixedOptionLocalService;

	@Inject
	private static CommerceShippingMethodLocalService
		_commerceShippingMethodLocalService;

	@DeleteAfterTestRun
	private AccountEntry _accountEntry;

	@DeleteAfterTestRun
	private final List<CommerceChannel> _commerceChannels = new ArrayList<>();

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private CommerceShippingMethod _commerceShippingMethod;

	@DeleteAfterTestRun
	private final List<CommerceShippingFixedOption> _commerceShippingOptions =
		new ArrayList<>();

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}