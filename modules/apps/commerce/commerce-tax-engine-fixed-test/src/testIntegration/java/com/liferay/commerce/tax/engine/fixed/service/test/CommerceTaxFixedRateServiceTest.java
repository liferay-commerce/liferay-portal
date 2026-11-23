/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.tax.engine.fixed.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.product.model.CPTaxCategory;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPTaxCategoryLocalService;
import com.liferay.commerce.tax.engine.fixed.model.CommerceTaxFixedRate;
import com.liferay.commerce.tax.engine.fixed.service.CommerceTaxFixedRateService;
import com.liferay.commerce.tax.model.CommerceTaxMethod;
import com.liferay.commerce.test.util.CommerceTaxTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian Wulbern
 */
@RunWith(Arquillian.class)
public class CommerceTaxFixedRateServiceTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	@TestInfo("LPD-116184")
	public void testAddCommerceTaxFixedRate() throws Exception {
		CommerceChannel commerceChannel1 = _addCommerceChannel();
		CommerceChannel commerceChannel2 = _addCommerceChannel();

		CommerceTaxMethod commerceTaxMethod1 =
			CommerceTaxTestUtil.addFixedTaxCommerceTaxMethod(
				TestPropsValues.getUserId(), commerceChannel1.getGroupId(),
				false);
		CommerceTaxMethod commerceTaxMethod2 =
			CommerceTaxTestUtil.addFixedTaxCommerceTaxMethod(
				TestPropsValues.getUserId(), commerceChannel2.getGroupId(),
				false);

		CPTaxCategory cpTaxCategory =
			_cpTaxCategoryLocalService.addCPTaxCategory(
				StringPool.BLANK, RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(),
				ServiceContextTestUtil.getServiceContext());

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_resourcePermissionLocalService.setResourcePermissions(
			commerceChannel1.getCompanyId(), CommerceChannel.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(commerceChannel1.getCommerceChannelId()),
			role.getRoleId(), new String[] {ActionKeys.UPDATE});

		User user = UserTestUtil.addUser();

		_userLocalService.addRoleUser(role.getRoleId(), user);

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user, PermissionCheckerFactoryUtil.create(user))) {

			try {
				_commerceTaxFixedRateService.addCommerceTaxFixedRate(
					commerceChannel1.getGroupId(),
					commerceTaxMethod2.getCommerceTaxMethodId(),
					cpTaxCategory.getCPTaxCategoryId(),
					RandomTestUtil.randomDouble());

				Assert.fail();
			}
			catch (PrincipalException principalException) {
				Assert.assertNotNull(principalException);
			}

			CommerceTaxFixedRate commerceTaxFixedRate =
				_commerceTaxFixedRateService.addCommerceTaxFixedRate(
					commerceChannel1.getGroupId(),
					commerceTaxMethod1.getCommerceTaxMethodId(),
					cpTaxCategory.getCPTaxCategoryId(),
					RandomTestUtil.randomDouble());

			Assert.assertEquals(
				commerceTaxMethod1.getCommerceTaxMethodId(),
				commerceTaxFixedRate.getCommerceTaxMethodId());
		}
	}

	private CommerceChannel _addCommerceChannel() throws Exception {
		Group group = GroupTestUtil.addGroup();

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(group.getCompanyId());

		return CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), commerceCurrency.getCode());
	}

	@Inject
	private CommerceTaxFixedRateService _commerceTaxFixedRateService;

	@Inject
	private CPTaxCategoryLocalService _cpTaxCategoryLocalService;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject
	private UserLocalService _userLocalService;

}