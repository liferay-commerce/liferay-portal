/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.object.notification.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.constants.CPDefinitionInventoryConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.inventory.engine.CommerceInventoryEngine;
import com.liferay.commerce.model.CPDefinitionInventory;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.service.CPDefinitionInventoryLocalService;
import com.liferay.commerce.service.CommerceOrderItemLocalService;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.notification.context.NotificationContextBuilder;
import com.liferay.notification.term.evaluator.NotificationTermEvaluatorTracker;
import com.liferay.notification.type.util.NotificationTypeUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;

import org.frutilla.FrutillaRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Danny Situ
 */
@RunWith(Arquillian.class)
@Sync
public class CommerceOrderItemsNotificationTermEvaluatorTest {

	@ClassRule
	@Rule
	public static AggregateTestRule aggregateTestRule = new AggregateTestRule(
		new LiferayIntegrationTestRule(),
		PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_company = CompanyLocalServiceUtil.getCompany(_group.getCompanyId());

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			_group.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			_group.getGroupId(), _commerceCurrency.getCode());

		_user = UserTestUtil.addUser(_company);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId());

		_accountEntry = CommerceAccountTestUtil.addBusinessAccountEntry(
			_user.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString() + "@liferay.com",
			RandomTestUtil.randomString(), new long[] {_user.getUserId()},
			new long[0], _serviceContext);
	}

	@Test
	public void testCommerceOrderItemsNotificationTermEvaluator()
		throws Exception {

		frutillaRule.scenario(
			"Order notifications can provide a list of order items"
		).given(
			"A notification template has the [%COMMERCEORDER_ORDER_ITEMS%] term"
		).and(
			"The template is linked to an CommerceOrder object action"
		).when(
			"The term [%COMMERCEORDER_ORDER_ITEMS%] is used"
		).then(
			"A list of commerce order items is returned"
		);

		CommerceOrder commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		commerceOrder = CommerceTestUtil.addCheckoutDetailsToCommerceOrder(
			commerceOrder, _user.getUserId(), false);

		List<CommerceOrderItem> commerceOrderItems =
			commerceOrder.getCommerceOrderItems();

		CommerceOrderItem commerceOrderItem = commerceOrderItems.get(0);

		CPDefinition cpDefinition = _cpDefinitionLocalService.getCPDefinition(
			commerceOrderItem.getCPDefinitionId());

		CPDefinitionInventory cpDefinitionInventory =
			_cpDefinitionInventoryLocalService.
				fetchCPDefinitionInventoryByCPDefinitionId(
					cpDefinition.getCPDefinitionId());

		if (cpDefinitionInventory == null) {
			_cpDefinitionInventoryLocalService.addCPDefinitionInventory(
				_user.getUserId(), cpDefinition.getCPDefinitionId(), "default",
				"default", false, false, BigDecimal.ONE, false,
				CPDefinitionInventoryConstants.DEFAULT_MIN_ORDER_QUANTITY,
				CPDefinitionInventoryConstants.DEFAULT_MAX_ORDER_QUANTITY, null,
				BigDecimal.ONE);
		}
		else {
			_cpDefinitionInventoryLocalService.updateCPDefinitionInventory(
				cpDefinitionInventory.getCPDefinitionInventoryId(), "default",
				"default", false, false, BigDecimal.ONE, false,
				CPDefinitionInventoryConstants.DEFAULT_MIN_ORDER_QUANTITY,
				CPDefinitionInventoryConstants.DEFAULT_MAX_ORDER_QUANTITY, null,
				BigDecimal.ONE);
		}

		BigDecimal stockQuantity = _commerceInventoryEngine.getStockQuantity(
			_company.getCompanyId(), cpDefinition.getGroupId(),
			commerceOrderItem.getSku(), StringPool.BLANK);

		commerceOrderItem.setQuantity(stockQuantity);

		_commerceOrderItemLocalService.updateCommerceOrderItem(
			commerceOrderItem);

		String content = "[%COMMERCEORDER_ORDER_ITEMS%]";

		Map<String, Object> termValues = HashMapBuilder.<String, Object>put(
			"externalReferenceCode", commerceOrder.getExternalReferenceCode()
		).put(
			"groupId", commerceOrder.getGroupId()
		).put(
			"id", commerceOrder.getCommerceOrderId()
		).build();

		content = NotificationTypeUtil.evaluateTerms(
			content,
			new NotificationContextBuilder(
			).className(
				CommerceOrder.class.getName()
			).classPK(
				GetterUtil.getLong(termValues.get("id"))
			).externalReferenceCode(
				GetterUtil.getString(termValues.get("externalReferenceCode"))
			).groupId(
				GetterUtil.getLong(termValues.get("groupId"))
			).termValues(
				termValues
			).build(),
			_notificationTermEvaluatorTracker);

		Assert.assertTrue(
			"Commerce order items table is missing.",
			content.contains("<table>"));
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private AccountEntry _accountEntry;
	private CommerceChannel _commerceChannel;
	private CommerceCurrency _commerceCurrency;

	@Inject
	private CommerceInventoryEngine _commerceInventoryEngine;

	@Inject
	private CommerceOrderItemLocalService _commerceOrderItemLocalService;

	private Company _company;

	@Inject
	private CPDefinitionInventoryLocalService
		_cpDefinitionInventoryLocalService;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	private Group _group;

	@Inject
	private NotificationTermEvaluatorTracker _notificationTermEvaluatorTracker;

	private ServiceContext _serviceContext;
	private User _user;

}