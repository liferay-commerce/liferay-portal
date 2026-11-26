/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.permission.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.constants.AccountRoleConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountGroup;
import com.liferay.account.model.AccountRole;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.account.service.AccountRoleLocalService;
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
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserGroupTestUtil;
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

	@FeatureFlag("LPD-112683")
	@Test
	public void testContainsWithRoleFilterDisabled() throws Exception {
		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_cpDefinition.getGroupId(), SimpleCPTypeConstants.NAME, true, true);

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(), cpDefinition.getCPDefinitionId(),
			Role.class.getName(), role.getRoleId());

		Assert.assertTrue(
			_commerceProductViewPermission.contains(
				PermissionCheckerFactoryUtil.create(UserTestUtil.addUser()), 0,
				cpDefinition.getCPDefinitionId()));
	}

	@FeatureFlag("LPD-112683")
	@Test
	public void testContainsWithRoleFilterEnabled() throws Exception {
		User user1 = UserTestUtil.addUser();
		User user2 = UserTestUtil.addUser();

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(_group.getGroupId());

		AccountEntry accountEntry1 =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(),
				new long[] {user1.getUserId(), user2.getUserId()}, null,
				serviceContext);

		AccountRole accountRole1 = _accountRoleLocalService.addAccountRole(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), null, null);

		_accountRoleLocalService.associateUser(
			accountEntry1.getAccountEntryId(), accountRole1.getAccountRoleId(),
			user1.getUserId());

		Role role1 = _roleLocalService.getRole(
			_group.getCompanyId(),
			AccountRoleConstants.REQUIRED_ROLE_NAME_ACCOUNT_ADMINISTRATOR);

		AccountRole accountRole2 =
			_accountRoleLocalService.getAccountRoleByRoleId(role1.getRoleId());

		_accountRoleLocalService.associateUser(
			accountEntry1.getAccountEntryId(), accountRole2.getAccountRoleId(),
			user2.getUserId());

		AccountEntry accountEntry2 =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(), new long[] {user1.getUserId()},
				null, serviceContext);

		Role role2 = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		User user3 = UserTestUtil.addUser();
		UserGroup userGroup = UserGroupTestUtil.addUserGroup();

		_userLocalService.addUserGroupUser(userGroup.getUserGroupId(), user3);

		_roleLocalService.addGroupRole(userGroup.getGroupId(), role2);

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_cpDefinition.getGroupId(), SimpleCPTypeConstants.NAME, true, true);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(), cpDefinition.getCPDefinitionId(),
			Role.class.getName(), accountRole1.getRoleId());
		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(), cpDefinition.getCPDefinitionId(),
			Role.class.getName(), role2.getRoleId());

		_cpDefinitionLocalService.updateCPDefinitionRoleFilter(
			cpDefinition.getCPDefinitionId(), true);

		PermissionChecker permissionChecker1 =
			PermissionCheckerFactoryUtil.create(user1);
		PermissionChecker permissionChecker2 =
			PermissionCheckerFactoryUtil.create(user2);

		Assert.assertFalse(
			_commerceProductViewPermission.contains(
				_guestPermissionChecker,
				AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
				cpDefinition.getCPDefinitionId()));
		Assert.assertTrue(
			_commerceProductViewPermission.contains(
				permissionChecker1, accountEntry1.getAccountEntryId(),
				cpDefinition.getCPDefinitionId()));
		Assert.assertFalse(
			_commerceProductViewPermission.contains(
				permissionChecker1, accountEntry2.getAccountEntryId(),
				cpDefinition.getCPDefinitionId()));
		Assert.assertFalse(
			_commerceProductViewPermission.contains(
				permissionChecker2, 0, cpDefinition.getCPDefinitionId()));
		Assert.assertFalse(
			_commerceProductViewPermission.contains(
				permissionChecker2, accountEntry1.getAccountEntryId(),
				cpDefinition.getCPDefinitionId()));
		Assert.assertTrue(
			_commerceProductViewPermission.contains(
				PermissionCheckerFactoryUtil.create(user3), 0,
				cpDefinition.getCPDefinitionId()));
	}

	@Test
	public void testContainsWithRoleFilterEnabledWhenFeatureFlagIsDisabled()
		throws Exception {

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			_cpDefinition.getGroupId(), SimpleCPTypeConstants.NAME, true, true);

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(), cpDefinition.getCPDefinitionId(),
			Role.class.getName(), role.getRoleId());

		_cpDefinitionLocalService.updateCPDefinitionRoleFilter(
			cpDefinition.getCPDefinitionId(), true);

		Assert.assertTrue(
			_commerceProductViewPermission.contains(
				PermissionCheckerFactoryUtil.create(UserTestUtil.addUser()), 0,
				cpDefinition.getCPDefinitionId()));
	}

	@Inject
	private AccountGroupRelLocalService _accountGroupRelLocalService;

	@Inject
	private AccountRoleLocalService _accountRoleLocalService;

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
	private RoleLocalService _roleLocalService;

	@Inject
	private UserLocalService _userLocalService;

}