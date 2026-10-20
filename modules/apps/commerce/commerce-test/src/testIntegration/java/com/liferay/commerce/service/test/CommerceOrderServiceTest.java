/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.service.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.constants.CommerceActionKeys;
import com.liferay.commerce.constants.CommerceOrderActionKeys;
import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.exception.CommerceOrderPaymentMethodException;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelLocalService;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.BigDecimalUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Crescenzo Rega
 */
@RunWith(Arquillian.class)
public class CommerceOrderServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			TestPropsValues.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			TestPropsValues.getGroupId(), _commerceCurrency.getCode());

		_commerceOrder = _commerceOrderLocalService.addCommerceOrder(
			TestPropsValues.getUserId(), _commerceChannel.getGroupId(),
			AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
			_commerceCurrency.getCode(), 0);

		_company = CompanyTestUtil.addCompany(true);
	}

	@Test
	public void testAddCommerceOrder() throws Exception {
		Role role = RoleTestUtil.addRole(
			RandomTestUtil.randomString(), RoleConstants.TYPE_REGULAR);
		User user1 = UserTestUtil.addUser();

		_roleLocalService.addUserRole(user1.getUserId(), role);

		AccountEntry businessAccountEntry =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				TestPropsValues.getUserId(), RandomTestUtil.randomString(),
				null,
				ServiceContextTestUtil.getServiceContext(
					TestPropsValues.getGroupId()));

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user1, PermissionCheckerFactoryUtil.create(user1))) {

			_commerceOrderService.addCommerceOrder(
				_commerceChannel.getGroupId(),
				businessAccountEntry.getAccountEntryId(),
				_commerceCurrency.getCode(), 0);

			Assert.fail();
		}
		catch (Exception exception) {
			_assertMessage(
				CommerceOrderActionKeys.ADD_COMMERCE_ORDER,
				exception.getMessage(), user1.getUserId());
		}

		RoleTestUtil.addResourcePermission(
			role, CommerceOrderConstants.RESOURCE_NAME,
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(TestPropsValues.getCompanyId()),
			CommerceOrderActionKeys.ADD_COMMERCE_ORDER);

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user1, PermissionCheckerFactoryUtil.create(user1))) {

			_commerceOrderService.addCommerceOrder(
				_commerceChannel.getGroupId(),
				businessAccountEntry.getAccountEntryId(),
				_commerceCurrency.getCode(), 0);
		}

		User user2 = UserTestUtil.addUser();

		AccountEntry personAccountEntry =
			CommerceAccountTestUtil.getPersonAccountEntry(user2.getUserId());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user1, PermissionCheckerFactoryUtil.create(user1))) {

			_commerceOrderService.addCommerceOrder(
				_commerceChannel.getGroupId(),
				personAccountEntry.getAccountEntryId(),
				_commerceCurrency.getCode(), 0);

			Assert.fail();
		}
		catch (Exception exception) {
			_assertMessage(
				ActionKeys.UPDATE, exception.getMessage(), user1.getUserId());
		}

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user2, PermissionCheckerFactoryUtil.create(user2))) {

			_commerceOrderService.addCommerceOrder(
				_commerceChannel.getGroupId(),
				personAccountEntry.getAccountEntryId(),
				_commerceCurrency.getCode(), 0);
		}

		RoleTestUtil.addResourcePermission(
			role, CommerceOrderConstants.RESOURCE_NAME,
			ResourceConstants.SCOPE_COMPANY,
			String.valueOf(TestPropsValues.getCompanyId()),
			CommerceOrderActionKeys.MANAGE_ALL_ACCOUNTS);

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user1, PermissionCheckerFactoryUtil.create(user1))) {

			_commerceOrderService.addCommerceOrder(
				_commerceChannel.getGroupId(),
				personAccountEntry.getAccountEntryId(),
				_commerceCurrency.getCode(), 0);
		}

		int count =
			_commerceOrderLocalService.
				getCommerceOrdersCountByCommerceAccountId(
					businessAccountEntry.getAccountEntryId());

		User guestUser = _userLocalService.getGuestUser(
			TestPropsValues.getCompanyId());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				guestUser, PermissionCheckerFactoryUtil.create(guestUser))) {

			CommerceOrder commerceOrder =
				_commerceOrderService.addCommerceOrder(
					_commerceChannel.getGroupId(),
					businessAccountEntry.getAccountEntryId(),
					_commerceCurrency.getCode(), 0);

			Assert.assertEquals(
				AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
				commerceOrder.getCommerceAccountId());
		}

		Assert.assertEquals(
			count,
			_commerceOrderLocalService.
				getCommerceOrdersCountByCommerceAccountId(
					businessAccountEntry.getAccountEntryId()));
	}

	@Test
	public void testDeleteCommerceOrder() throws Exception {
		CommerceOrder commerceOrder =
			_commerceOrderLocalService.addCommerceOrder(
				TestPropsValues.getUserId(), _commerceChannel.getGroupId(),
				AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
				_commerceCurrency.getCode(), 0);

		User user = UserTestUtil.addCompanyAdminUser(_company);

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user, PermissionCheckerFactoryUtil.create(user))) {

			_commerceOrderService.deleteCommerceOrder(
				commerceOrder.getCommerceOrderId());

			Assert.fail();
		}
		catch (Exception exception) {
			_assertMessage(
				ActionKeys.DELETE, exception.getMessage(), user.getUserId());
		}

		_commerceOrderService.deleteCommerceOrder(
			commerceOrder.getCommerceOrderId());
	}

	@Test
	public void testGetCommerceOrder() throws Exception {
		User user = UserTestUtil.addCompanyAdminUser(_company);

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user, PermissionCheckerFactoryUtil.create(user))) {

			_commerceOrderService.getCommerceOrder(
				_commerceOrder.getCommerceOrderId());

			Assert.fail();
		}
		catch (Exception exception) {
			_assertMessage(
				ActionKeys.VIEW, exception.getMessage(), user.getUserId());
		}

		_commerceOrderService.getCommerceOrder(
			_commerceOrder.getCommerceOrderId());
	}

	@Test
	public void testUpdateCommerceOrder() throws Exception {
		User user = UserTestUtil.addUser();

		AccountEntry accountEntry =
			CommerceAccountTestUtil.getPersonAccountEntry(user.getUserId());

		CommerceOrder commerceOrder =
			_commerceOrderLocalService.addCommerceOrder(
				user.getUserId(), _commerceChannel.getGroupId(),
				accountEntry.getAccountEntryId(), _commerceCurrency.getCode(),
				0);

		BigDecimal total = commerceOrder.getTotal(
		).add(
			BigDecimal.ONE
		);

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user, PermissionCheckerFactoryUtil.create(user))) {

			_updateCommerceOrder(
				commerceOrder, commerceOrder.getCommercePaymentMethodKey(),
				commerceOrder.getTotal());

			try {
				_updateCommerceOrder(
					commerceOrder, commerceOrder.getCommercePaymentMethodKey(),
					total);

				Assert.fail();
			}
			catch (Exception exception) {
				_assertMessage(
					CommerceActionKeys.MANAGE_COMMERCE_ORDER_PRICES,
					exception.getMessage(), user.getUserId());
			}

			String commercePaymentMethodKey = RandomTestUtil.randomString();

			AssertUtils.assertFailure(
				CommerceOrderPaymentMethodException.class,
				"Invalid payment method key " + commercePaymentMethodKey,
				() -> _updateCommerceOrder(
					commerceOrder, commercePaymentMethodKey,
					commerceOrder.getTotal()));
		}

		CommerceOrder updatedCommerceOrder = _updateCommerceOrder(
			commerceOrder, commerceOrder.getCommercePaymentMethodKey(), total);

		Assert.assertTrue(
			BigDecimalUtil.eq(total, updatedCommerceOrder.getTotal()));
	}

	@Test
	public void testUpdateCommercePaymentMethodKey() throws Exception {
		CommerceOrder commerceOrder =
			_commerceOrderLocalService.addCommerceOrder(
				TestPropsValues.getUserId(), _commerceChannel.getGroupId(),
				AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
				_commerceCurrency.getCode(), 0);

		long commerceOrderId = commerceOrder.getCommerceOrderId();

		String commercePaymentMethodKey = RandomTestUtil.randomString();

		AssertUtils.assertFailure(
			CommerceOrderPaymentMethodException.class,
			"Invalid payment method key " + commercePaymentMethodKey,
			() -> _commerceOrderService.updateCommercePaymentMethodKey(
				commerceOrderId, commercePaymentMethodKey));

		CommercePaymentMethodGroupRel commercePaymentMethodGroupRel =
			_commercePaymentMethodGroupRelLocalService.
				addCommercePaymentMethodGroupRel(
					TestPropsValues.getUserId(), _commerceChannel.getGroupId(),
					RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.randomLocaleStringMap(), true, null,
					commercePaymentMethodKey, RandomTestUtil.randomDouble(),
					null);

		commerceOrder = _commerceOrderService.updateCommercePaymentMethodKey(
			commerceOrderId, commercePaymentMethodKey);

		Assert.assertEquals(
			commercePaymentMethodKey,
			commerceOrder.getCommercePaymentMethodKey());

		_commercePaymentMethodGroupRelLocalService.setActive(
			commercePaymentMethodGroupRel.getCommercePaymentMethodGroupRelId(),
			false);

		commerceOrder = _commerceOrderService.updateCommercePaymentMethodKey(
			commerceOrderId, StringPool.BLANK);

		Assert.assertEquals(
			StringPool.BLANK, commerceOrder.getCommercePaymentMethodKey());

		AssertUtils.assertFailure(
			CommerceOrderPaymentMethodException.class,
			"Invalid payment method key " + commercePaymentMethodKey,
			() -> _commerceOrderService.updateCommercePaymentMethodKey(
				commerceOrderId, commercePaymentMethodKey));
	}

	private void _assertMessage(String actionId, String message, long userId) {
		Assert.assertTrue(
			message.contains(
				StringBundler.concat(
					"User ", userId, " must have ", actionId,
					" permission for")));
	}

	private CommerceOrder _updateCommerceOrder(
			CommerceOrder commerceOrder, String commercePaymentMethodKey,
			BigDecimal total)
		throws Exception {

		return _commerceOrderService.updateCommerceOrder(
			commerceOrder.getExternalReferenceCode(),
			commerceOrder.getCommerceOrderId(),
			commerceOrder.getBillingAddressId(),
			commerceOrder.getCommerceShippingMethodId(),
			commerceOrder.getShippingAddressId(),
			commerceOrder.getAdvanceStatus(), commercePaymentMethodKey, null,
			commerceOrder.getPurchaseOrderNumber(),
			commerceOrder.getShippingAmount(),
			commerceOrder.getShippingOptionName(), commerceOrder.getSubtotal(),
			total);
	}

	private static CommerceChannel _commerceChannel;
	private static CommerceCurrency _commerceCurrency;
	private static CommerceOrder _commerceOrder;

	@Inject
	private static CommerceOrderLocalService _commerceOrderLocalService;

	private static Company _company;

	@Inject
	private CommerceOrderService _commerceOrderService;

	@Inject
	private CommercePaymentMethodGroupRelLocalService
		_commercePaymentMethodGroupRelLocalService;

	@Inject
	private RoleLocalService _roleLocalService;

	@Inject
	private UserLocalService _userLocalService;

}