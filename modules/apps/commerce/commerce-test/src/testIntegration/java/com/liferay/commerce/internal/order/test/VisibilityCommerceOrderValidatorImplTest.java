/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.internal.order.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.order.CommerceOrderValidator;
import com.liferay.commerce.order.CommerceOrderValidatorResult;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Fabio Monaco
 */
@RunWith(Arquillian.class)
public class VisibilityCommerceOrderValidatorImplTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			_group.getCompanyId());

		CommerceTestUtil.addCommerceChannel(
			_group.getGroupId(), _commerceCurrency.getCode());

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId());
	}

	@FeatureFlag("LPD-112683")
	@Test
	public void testValidate() throws Exception {
		AccountEntry accountEntry1 =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(),
				new long[] {TestPropsValues.getUserId()}, null,
				_serviceContext);

		AccountGroup accountGroup1 =
			CommerceAccountTestUtil.addAccountGroupAndAccountRel(
				RandomTestUtil.randomString(),
				AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
				accountEntry1.getAccountEntryId(), _serviceContext);

		CommerceCatalog commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			_group.getCompanyId(), _group.getGroupId(),
			TestPropsValues.getUserId(), _commerceCurrency.getCode());

		CPInstance cpInstance = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId());

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(), cpInstance.getCPDefinitionId(),
			AccountEntry.class.getName(), accountEntry1.getAccountEntryId());

		AccountEntry accountEntry2 =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(),
				new long[] {TestPropsValues.getUserId()}, null,
				_serviceContext);

		AccountGroup accountGroup2 =
			CommerceAccountTestUtil.addAccountGroupAndAccountRel(
				RandomTestUtil.randomString(),
				AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
				accountEntry2.getAccountEntryId(), _serviceContext);

		_accountGroupRelLocalService.addAccountGroupRel(
			accountGroup2.getAccountGroupId(), CPDefinition.class.getName(),
			cpInstance.getCPDefinitionId());

		_cpDefinitionLocalService.updateCPDefinitionAccountGroupFilter(
			cpInstance.getCPDefinitionId(), true);

		CommerceOrderValidatorResult commerceOrderValidatorResult = _validate(
			accountEntry1.getAccountEntryId(), cpInstance);

		Assert.assertTrue(commerceOrderValidatorResult.isValid());

		commerceOrderValidatorResult = _validate(
			accountEntry2.getAccountEntryId(), cpInstance);

		Assert.assertTrue(commerceOrderValidatorResult.isValid());

		AccountEntry accountEntry3 =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomString(),
				new long[] {TestPropsValues.getUserId()}, null,
				_serviceContext);

		_accountGroupRelLocalService.addAccountGroupRel(
			accountGroup1.getAccountGroupId(), AccountEntry.class.getName(),
			accountEntry3.getAccountEntryId());

		CPDefinition cpDefinition = CPTestUtil.addCPDefinitionFromCatalog(
			commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, true,
			true);

		_cpDefinitionVisibilityRelLocalService.addCPDefinitionVisibilityRel(
			null, TestPropsValues.getUserId(), cpDefinition.getCPDefinitionId(),
			AccountEntry.class.getName(), accountEntry3.getAccountEntryId());

		commerceOrderValidatorResult = _validate(
			accountEntry3.getAccountEntryId(), cpInstance);

		Assert.assertFalse(commerceOrderValidatorResult.isValid());

		commerceOrderValidatorResult = _validate(
			AccountConstants.ACCOUNT_ENTRY_ID_GUEST, cpInstance);

		Assert.assertFalse(commerceOrderValidatorResult.isValid());

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-112683")) {

			commerceOrderValidatorResult = _validate(
				accountEntry1.getAccountEntryId(), cpInstance);

			Assert.assertFalse(commerceOrderValidatorResult.isValid());

			commerceOrderValidatorResult = _validate(
				accountEntry2.getAccountEntryId(), cpInstance);

			Assert.assertTrue(commerceOrderValidatorResult.isValid());
		}
	}

	private CommerceOrderValidatorResult _validate(
			long accountEntryId, CPInstance cpInstance)
		throws Exception {

		return _commerceOrderValidator.validate(
			LocaleUtil.US,
			CommerceTestUtil.addB2BCommerceOrder(
				_group.getGroupId(), TestPropsValues.getUserId(),
				accountEntryId, _commerceCurrency.getCommerceCurrencyId()),
			cpInstance, null, BigDecimal.ONE, false);
	}

	@Inject
	private AccountGroupRelLocalService _accountGroupRelLocalService;

	private CommerceCurrency _commerceCurrency;

	@Inject(filter = "commerce.order.validator.key=visibility")
	private CommerceOrderValidator _commerceOrderValidator;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private CPDefinitionVisibilityRelLocalService
		_cpDefinitionVisibilityRelLocalService;

	private Group _group;
	private ServiceContext _serviceContext;

}