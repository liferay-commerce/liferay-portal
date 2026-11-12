/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.permission.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.permission.CommerceProductViewPermission;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelLocalService;
import com.liferay.commerce.product.service.CommerceChannelRelLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Gianmarco Brunialti Masera
 */
@FeatureFlag("LPD-109588")
@RunWith(Arquillian.class)
public class CommerceProductViewPermissionTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(_group.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			_group.getGroupId(), commerceCurrency.getCode());

		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			_group.getCompanyId(), _group.getGroupId(),
			TestPropsValues.getUserId(), commerceCurrency.getCode());

		_cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		_commerceChannelRelLocalService.addCommerceChannelRel(
			CPDefinition.class.getName(), _cpDefinition.getCPDefinitionId(),
			_commerceChannel.getCommerceChannelId(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		_cpDefinitionLocalService.updateCPDefinitionChannelFilter(
			_cpDefinition.getCPDefinitionId(), true);

		_guestPermissionChecker = PermissionCheckerFactoryUtil.create(
			_userLocalService.getGuestUser(_group.getCompanyId()));
	}

	@Test
	public void testContains() throws Exception {
		Assert.assertTrue(
			_commerceProductViewPermission.contains(
				_guestPermissionChecker,
				AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
				_cpDefinition.getCPDefinitionId()));
		Assert.assertTrue(
			_commerceProductViewPermission.contains(
				_guestPermissionChecker,
				AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
				_commerceChannel.getGroupId(),
				_cpDefinition.getCPDefinitionId()));

		CommerceTestUtil.runWithGuestBrowsingDisabled(
			_commerceChannel.getGroupId(),
			() -> {
				Assert.assertFalse(
					_commerceProductViewPermission.contains(
						_guestPermissionChecker,
						AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
						_cpDefinition.getCPDefinitionId()));
				Assert.assertFalse(
					_commerceProductViewPermission.contains(
						_guestPermissionChecker,
						AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
						_commerceChannel.getGroupId(),
						_cpDefinition.getCPDefinitionId()));
				Assert.assertFalse(
					_commerceProductViewPermission.contains(
						_guestPermissionChecker,
						AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
						_group.getGroupId(),
						_cpDefinition.getCPDefinitionId()));

				PermissionChecker permissionChecker =
					PermissionCheckerFactoryUtil.create(UserTestUtil.addUser());

				Assert.assertTrue(
					_commerceProductViewPermission.contains(
						permissionChecker,
						AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
						_cpDefinition.getCPDefinitionId()));
				Assert.assertTrue(
					_commerceProductViewPermission.contains(
						permissionChecker,
						AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
						_commerceChannel.getGroupId(),
						_cpDefinition.getCPDefinitionId()));
			});
	}

	@FeatureFlag("LPD-112683")
	@Test
	public void testContainsWithCPDefinitionVisibilityRel() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(_group.getGroupId());

		AccountEntry accountEntry1 =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(),
				new long[] {TestPropsValues.getUserId()}, null, serviceContext);

		AccountGroup accountGroup1 =
			CommerceAccountTestUtil.addAccountGroupAndAccountRel(
				RandomTestUtil.randomString(),
				AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
				accountEntry1.getAccountEntryId(), serviceContext);

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_cpDefinition.getGroupId(), SimpleCPTypeConstants.NAME, true, true);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(), cpDefinition.getCPDefinitionId(),
			AccountEntry.class.getName(), accountEntry1.getAccountEntryId());

		AccountEntry accountEntry2 =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(),
				new long[] {TestPropsValues.getUserId()}, null, serviceContext);

		AccountGroup accountGroup2 =
			CommerceAccountTestUtil.addAccountGroupAndAccountRel(
				RandomTestUtil.randomString(),
				AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
				accountEntry2.getAccountEntryId(), serviceContext);

		_accountGroupRelLocalService.addAccountGroupRel(
			accountGroup2.getAccountGroupId(), CPDefinition.class.getName(),
			cpDefinition.getCPDefinitionId());

		cpDefinition =
			_cpDefinitionLocalService.updateCPDefinitionAccountGroupFilter(
				cpDefinition.getCPDefinitionId(), true);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(UserTestUtil.addUser());

		Assert.assertFalse(
			_commerceProductViewPermission.contains(
				_guestPermissionChecker,
				AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
				cpDefinition.getCPDefinitionId()));
		Assert.assertTrue(
			_commerceProductViewPermission.contains(
				permissionChecker, accountEntry1.getAccountEntryId(),
				cpDefinition.getCPDefinitionId()));
		Assert.assertTrue(
			_commerceProductViewPermission.contains(
				permissionChecker, accountEntry2.getAccountEntryId(),
				cpDefinition.getCPDefinitionId()));

		AccountEntry accountEntry3 =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(),
				new long[] {TestPropsValues.getUserId()}, null, serviceContext);

		_accountGroupRelLocalService.addAccountGroupRel(
			accountGroup1.getAccountGroupId(), AccountEntry.class.getName(),
			accountEntry3.getAccountEntryId());

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(),
			_cpDefinition.getCPDefinitionId(), AccountEntry.class.getName(),
			accountEntry3.getAccountEntryId());

		Assert.assertFalse(
			_commerceProductViewPermission.contains(
				permissionChecker, accountEntry3.getAccountEntryId(),
				cpDefinition.getCPDefinitionId()));

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-112683")) {

			Assert.assertFalse(
				_commerceProductViewPermission.contains(
					permissionChecker, accountEntry1.getAccountEntryId(),
					cpDefinition.getCPDefinitionId()));
			Assert.assertTrue(
				_commerceProductViewPermission.contains(
					permissionChecker, accountEntry2.getAccountEntryId(),
					cpDefinition.getCPDefinitionId()));
		}
	}

	@Inject
	private AccountGroupRelLocalService _accountGroupRelLocalService;

	private CommerceChannel _commerceChannel;

	@Inject
	private CommerceChannelRelLocalService _commerceChannelRelLocalService;

	@Inject
	private CommerceProductViewPermission _commerceProductViewPermission;

	private CPDefinition _cpDefinition;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private CPDefinitionVisibilityRelLocalService
		_cpDefinitionVisibilityRelLocalService;

	private Group _group;
	private PermissionChecker _guestPermissionChecker;

	@Inject
	private UserLocalService _userLocalService;

}