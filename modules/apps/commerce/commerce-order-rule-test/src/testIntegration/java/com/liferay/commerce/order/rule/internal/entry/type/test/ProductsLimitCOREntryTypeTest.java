/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.order.rule.internal.entry.type.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.constants.CommerceConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CPDefinitionInventory;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.order.CommerceOrderValidator;
import com.liferay.commerce.order.CommerceOrderValidatorResult;
import com.liferay.commerce.order.rule.constants.COREntryConstants;
import com.liferay.commerce.order.rule.entry.type.COREntryType;
import com.liferay.commerce.order.rule.entry.type.COREntryTypeItem;
import com.liferay.commerce.order.rule.entry.type.COREntryTypeRegistry;
import com.liferay.commerce.order.rule.model.COREntry;
import com.liferay.commerce.order.rule.service.COREntryLocalService;
import com.liferay.commerce.order.rule.service.COREntryRelLocalService;
import com.liferay.commerce.product.model.CPConfigurationEntry;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CProduct;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPConfigurationEntryLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.service.CPDefinitionInventoryLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.commerce.service.CommerceOrderTypeLocalService;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.settings.ModifiableSettings;
import com.liferay.portal.kernel.settings.Settings;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.frutilla.FrutillaRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Michele Vigilante
 */
@RunWith(Arquillian.class)
@Sync
public class ProductsLimitCOREntryTypeTest {

	@ClassRule
	@Rule
	public static AggregateTestRule aggregateTestRule = new AggregateTestRule(
		new LiferayIntegrationTestRule(),
		PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_user = UserTestUtil.addUser();

		_group = GroupTestUtil.addGroup();

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			_group.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			_group.getGroupId(), _commerceCurrency.getCode());

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getCompanyId(), _group.getGroupId(), _user.getUserId());

		CommerceCatalog commerceCatalog =
			_commerceCatalogLocalService.addCommerceCatalog(
				null, RandomTestUtil.randomString(),
				_commerceCurrency.getCode(), LocaleUtil.US.getDisplayLanguage(),
				_serviceContext);

		_cpInstance = CPTestUtil.addCPInstanceFromCatalog(
			commerceCatalog.getGroupId());

		CPDefinitionInventory cpDefinitionInventory =
			_cpDefinitionInventoryLocalService.
				fetchCPDefinitionInventoryByCPDefinitionId(
					_cpInstance.getCPDefinitionId());

		cpDefinitionInventory.setBackOrders(true);

		_cpDefinitionInventoryLocalService.updateCPDefinitionInventory(
			cpDefinitionInventory);

		_accountEntry = CommerceAccountTestUtil.getPersonAccountEntry(
			_user.getUserId());
	}

	@Test
	public void testValidateWithAccountEntryEligibility() throws Exception {
		frutillaRule.scenario(
			"Validate an order against a products limit rule eligible for " +
				"one account"
		).given(
			"A products limit rule of 1 eligible for an account"
		).when(
			"Orders of that account and of another account exceed the limit"
		).then(
			"Only the order of the eligible account is invalid"
		);

		COREntry corEntry = _addCOREntry(0, "1");

		_corEntryRelLocalService.addCOREntryRel(
			_user.getUserId(), AccountEntry.class.getName(),
			_accountEntry.getAccountEntryId(), corEntry.getCOREntryId());

		CommerceOrder commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			BigDecimal.valueOf(2));

		_assertInvalid(
			commerceOrder,
			"No more than 1 products in this product range can be purchased " +
				"together.");

		User user = UserTestUtil.addUser();

		AccountEntry accountEntry =
			CommerceAccountTestUtil.getPersonAccountEntry(user.getUserId());

		commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			BigDecimal.valueOf(2));

		CommerceOrderValidatorResult commerceOrderValidatorResult =
			_commerceOrderValidator.validate(LocaleUtil.US, commerceOrder);

		Assert.assertTrue(commerceOrderValidatorResult.isValid());
	}

	@Test
	public void testValidateWithCommerceChannelEligibility() throws Exception {
		frutillaRule.scenario(
			"Validate an order against a products limit rule eligible for " +
				"one channel"
		).given(
			"A products limit rule of 1 eligible for a channel"
		).when(
			"Orders of that channel and of another channel exceed the limit"
		).then(
			"Only the order of the eligible channel is invalid"
		);

		COREntry corEntry = _addCOREntry(0, "1");

		_corEntryRelLocalService.addCOREntryRel(
			_user.getUserId(), CommerceChannel.class.getName(),
			_commerceChannel.getCommerceChannelId(), corEntry.getCOREntryId());

		CommerceOrder commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			BigDecimal.valueOf(2));

		_assertInvalid(
			commerceOrder,
			"No more than 1 products in this product range can be purchased " +
				"together.");

		Group group = GroupTestUtil.addGroup();

		CommerceTestUtil.addCommerceChannel(
			group.getGroupId(), _commerceCurrency.getCode());

		commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			BigDecimal.valueOf(2));

		CommerceOrderValidatorResult commerceOrderValidatorResult =
			_commerceOrderValidator.validate(LocaleUtil.US, commerceOrder);

		Assert.assertTrue(commerceOrderValidatorResult.isValid());
	}

	@Test
	public void testValidateWithCommerceOrderItemsOfTheSameProduct()
		throws Exception {

		frutillaRule.scenario(
			"Validate an order whose product is split into separate order items"
		).given(
			"A products limit rule of 1.9 and a channel showing separate " +
				"order items"
		).when(
			"The product is added twice to the order with quantity 1"
		).then(
			"The quantities of the order items are summed and the order is " +
				"invalid"
		);

		_addCOREntry(0, "1.9");

		Settings settings = FallbackKeysSettingsUtil.getSettings(
			new GroupServiceSettingsLocator(
				_commerceChannel.getGroupId(),
				CommerceConstants.SERVICE_NAME_COMMERCE_ORDER));

		ModifiableSettings modifiableSettings =
			settings.getModifiableSettings();

		modifiableSettings.setValue(
			"showSeparateOrderItems", Boolean.TRUE.toString());

		modifiableSettings.store();

		CommerceOrder commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			BigDecimal.ONE);

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			BigDecimal.ONE);

		List<CommerceOrderItem> commerceOrderItems =
			commerceOrder.getCommerceOrderItems();

		Assert.assertEquals(
			commerceOrderItems.toString(), 2, commerceOrderItems.size());

		_assertInvalid(
			commerceOrder,
			"No more than 1.9 products in this product range can be " +
				"purchased together.");
	}

	@Test
	public void testValidateWithCommerceOrderTypeEligibility()
		throws Exception {

		frutillaRule.scenario(
			"Validate an order against a products limit rule eligible for " +
				"one order type"
		).given(
			"A products limit rule of 1 eligible for an order type"
		).when(
			"Orders of that order type and of another order type exceed the " +
				"limit"
		).then(
			"Only the order of the eligible order type is invalid"
		);

		COREntry corEntry = _addCOREntry(0, "1");

		Calendar calendar = Calendar.getInstance();

		CommerceOrderType commerceOrderType1 =
			_commerceOrderTypeLocalService.addCommerceOrderType(
				RandomTestUtil.randomString(), _user.getUserId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(), true,
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), 1, calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), true, _serviceContext);

		_corEntryRelLocalService.addCOREntryRel(
			_user.getUserId(), CommerceOrderType.class.getName(),
			commerceOrderType1.getCommerceOrderTypeId(),
			corEntry.getCOREntryId());

		CommerceOrder commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		commerceOrder.setCommerceOrderTypeId(
			commerceOrderType1.getCommerceOrderTypeId());

		commerceOrder = _commerceOrderLocalService.updateCommerceOrder(
			commerceOrder);

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			BigDecimal.valueOf(2));

		_assertInvalid(
			commerceOrder,
			"No more than 1 products in this product range can be purchased " +
				"together.");

		CommerceOrderType commerceOrderType2 =
			_commerceOrderTypeLocalService.addCommerceOrderType(
				RandomTestUtil.randomString(), _user.getUserId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(), true,
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), 1, calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), true, _serviceContext);

		commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		commerceOrder.setCommerceOrderTypeId(
			commerceOrderType2.getCommerceOrderTypeId());

		commerceOrder = _commerceOrderLocalService.updateCommerceOrder(
			commerceOrder);

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			BigDecimal.valueOf(2));

		CommerceOrderValidatorResult commerceOrderValidatorResult =
			_commerceOrderValidator.validate(LocaleUtil.US, commerceOrder);

		Assert.assertTrue(commerceOrderValidatorResult.isValid());
	}

	@Test
	public void testValidateWithDecimalQuantity() throws Exception {
		frutillaRule.scenario(
			"Validate an order with a decimal quantity"
		).given(
			"A products limit rule of 1.1 and a unit of measure with an " +
				"incremental quantity of 1.2"
		).when(
			"The product is added to the order with quantity 1.2"
		).then(
			"The order is invalid and the rule evaluates the decimal quantities"
		);

		COREntry corEntry = _addCOREntry(0, "1.1");

		CPDefinition cpDefinition = _cpInstance.getCPDefinition();

		CPConfigurationEntry cpConfigurationEntry =
			cpDefinition.fetchMasterCPConfigurationEntry();

		cpConfigurationEntry.setMultipleOrderQuantity(new BigDecimal("1.2"));

		_cpConfigurationEntryLocalService.updateCPConfigurationEntry(
			cpConfigurationEntry);

		String unitOfMeasureKey = RandomTestUtil.randomString();

		CPTestUtil.addCPInstanceUnitOfMeasure(
			_group.getGroupId(), _cpInstance.getCPInstanceId(),
			unitOfMeasureKey, new BigDecimal("1.2"), _cpInstance.getSku());

		CommerceOrder commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			new BigDecimal("1.2"), unitOfMeasureKey);

		_assertInvalid(
			commerceOrder,
			"No more than 1.1 products in this product range can be " +
				"purchased together.");

		COREntryType corEntryType = _corEntryTypeRegistry.getCOREntryType(
			COREntryConstants.TYPE_PRODUCTS_LIMIT);

		Assert.assertFalse(
			corEntryType.evaluate(
				corEntry,
				Collections.singletonList(
					new COREntryTypeItem(
						_cpInstance.getCPDefinitionId(),
						_cpInstance.getCPInstanceId(),
						new BigDecimal("1.2")))));
		Assert.assertTrue(
			corEntryType.evaluate(
				corEntry,
				Collections.singletonList(
					new COREntryTypeItem(
						_cpInstance.getCPDefinitionId(),
						_cpInstance.getCPInstanceId(), BigDecimal.ONE))));
	}

	@Test
	public void testValidateWithPriority() throws Exception {
		frutillaRule.scenario(
			"Validate an order against products limit rules with different " +
				"priorities"
		).given(
			"Products limit rules of 1 and 1.1 for the same product"
		).when(
			"A rule of 3 with a higher priority is added"
		).then(
			"Only the rule with the highest priority is evaluated"
		);

		_addCOREntry(0, "1");
		_addCOREntry(1, "1.1");

		CommerceOrder commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), _cpInstance.getCPInstanceId(),
			BigDecimal.valueOf(2));

		_assertInvalid(
			commerceOrder,
			"No more than 1.1 products in this product range can be " +
				"purchased together.");

		_addCOREntry(2, "3");

		CommerceOrderValidatorResult commerceOrderValidatorResult =
			_commerceOrderValidator.validate(LocaleUtil.US, commerceOrder);

		Assert.assertTrue(commerceOrderValidatorResult.isValid());
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private COREntry _addCOREntry(int priority, String quantity)
		throws Exception {

		Calendar calendar = Calendar.getInstance();

		COREntry corEntry = _corEntryLocalService.addCOREntry(
			RandomTestUtil.randomString(), _user.getUserId(), true,
			RandomTestUtil.randomString(), calendar.get(Calendar.MONTH),
			calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR),
			calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
			calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
			calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY),
			calendar.get(Calendar.MINUTE), true, RandomTestUtil.randomString(),
			priority, COREntryConstants.TYPE_PRODUCTS_LIMIT,
			UnicodePropertiesBuilder.put(
				COREntryConstants.
					TYPE_PRODUCTS_LIMIT_FIELD_PRODUCT_EXTERNAL_REFERENCE_CODES,
				() -> {
					CPDefinition cpDefinition = _cpInstance.getCPDefinition();

					CProduct cProduct = cpDefinition.getCProduct();

					return cProduct.getExternalReferenceCode();
				}
			).put(
				COREntryConstants.TYPE_PRODUCTS_LIMIT_FIELD_PRODUCT_QUANTITY,
				quantity
			).buildString(),
			_serviceContext);

		_corEntries.add(corEntry);

		return corEntry;
	}

	private void _assertInvalid(CommerceOrder commerceOrder, String message)
		throws Exception {

		CommerceOrderValidatorResult commerceOrderValidatorResult =
			_commerceOrderValidator.validate(LocaleUtil.US, commerceOrder);

		Assert.assertFalse(commerceOrderValidatorResult.isValid());
		Assert.assertEquals(
			message, commerceOrderValidatorResult.getLocalizedMessage());
	}

	private AccountEntry _accountEntry;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	private CommerceChannel _commerceChannel;
	private CommerceCurrency _commerceCurrency;

	@Inject
	private CommerceOrderLocalService _commerceOrderLocalService;

	@Inject
	private CommerceOrderTypeLocalService _commerceOrderTypeLocalService;

	@Inject(filter = "commerce.order.validator.key=order-rules")
	private CommerceOrderValidator _commerceOrderValidator;

	@DeleteAfterTestRun
	private final List<COREntry> _corEntries = new ArrayList<>();

	@Inject
	private COREntryLocalService _corEntryLocalService;

	@Inject
	private COREntryRelLocalService _corEntryRelLocalService;

	@Inject
	private COREntryTypeRegistry _corEntryTypeRegistry;

	@Inject
	private CPConfigurationEntryLocalService _cpConfigurationEntryLocalService;

	@Inject
	private CPDefinitionInventoryLocalService
		_cpDefinitionInventoryLocalService;

	private CPInstance _cpInstance;
	private Group _group;
	private ServiceContext _serviceContext;
	private User _user;

}