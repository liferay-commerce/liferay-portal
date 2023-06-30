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

package com.liferay.commerce.inventory.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.inventory.exception.CommerceInventoryWarehouseItemSkuException;
import com.liferay.commerce.inventory.exception.CommerceInventoryWarehouseItemUnitOfMeasureKeyException;
import com.liferay.commerce.inventory.exception.NoSuchInventoryWarehouseItemException;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouse;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouseItem;
import com.liferay.commerce.inventory.service.CommerceInventoryWarehouseItemLocalService;
import com.liferay.commerce.inventory.service.CommerceInventoryWarehouseItemService;
import com.liferay.commerce.inventory.service.CommerceInventoryWarehouseLocalService;
import com.liferay.commerce.product.exception.NoSuchCPInstanceException;
import com.liferay.commerce.product.exception.NoSuchCPInstanceUnitOfMeasureException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CPInstanceUnitOfMeasure;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPInstanceUnitOfMeasureLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.util.CommerceBigDecimalUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

import org.frutilla.FrutillaRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stefano Motta
 */
@RunWith(Arquillian.class)
public class CommerceInventoryWarehouseItemLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();

		_user = UserTestUtil.addUser(_company);
	}

	@Before
	public void setUp() throws Exception {
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_company.getCompanyId(), _company.getGroupId(),
				_user.getUserId());

		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			null, RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			LocaleUtil.US.getDisplayLanguage(), serviceContext);

		_cpInstance = CPTestUtil.addCPInstanceFromCatalog(
			_commerceCatalog.getGroupId());

		_cpDefinitions.add(_cpInstance.getCPDefinition());

		_commerceInventoryWarehouse =
			_commerceInventoryWarehouseLocalService.
				addCommerceInventoryWarehouse(
					RandomTestUtil.randomString(),
					RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.randomLocaleStringMap(), true,
					RandomTestUtil.randomString(), null, null,
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString(), 10, 10, serviceContext);

		for (CPInstanceUnitOfMeasure cpInstanceUnitOfMeasure :
				_cpInstanceUnitOfMeasureLocalService.
					getCPInstanceUnitOfMeasures(
						QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			_cpInstanceUnitOfMeasureLocalService.deleteCPInstanceUnitOfMeasure(
				cpInstanceUnitOfMeasure.getCPInstanceUnitOfMeasureId());
		}
	}

	@Test
	public void testAddCommerceInventoryWarehouseItem() throws PortalException {
		frutillaRule.scenario(
			"Create a new Commerce Inventory Warehouse Item for a CPInstance"
		).given(
			"I have a product definition with a default SKU and UnitOfMeasure"
		).when(
			"Commerce Inventory Warehouse Item is added"
		).then(
			"Commerce Inventory Warehouse Item is actually created."
		);

		String key = RandomTestUtil.randomString();

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, key, RandomTestUtil.randomLocaleStringMap(), 2,
				true, 0.0, BigDecimal.ONE, _cpInstance.getSku());

		BigDecimal quantity = BigDecimal.TEN;

		_commerceInventoryWarehouseItemLocalService.
			addCommerceInventoryWarehouseItem(
				RandomTestUtil.randomString(), _user.getUserId(),
				_commerceInventoryWarehouse.getCommerceInventoryWarehouseId(),
				_cpInstance.getSku(), _cpInstanceUnitOfMeasure.getKey(),
				quantity);

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				getCommerceInventoryWarehouseItem(
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					_cpInstance.getSku(), _cpInstanceUnitOfMeasure.getKey());

		Assert.assertNotNull(commerceInventoryWarehouseItem);

		Assert.assertEquals(
			commerceInventoryWarehouseItem.toString(),
			commerceInventoryWarehouseItem.getUnitOfMeasureKey(), key);
		Assert.assertEquals(
			commerceInventoryWarehouseItem.toString(),
			commerceInventoryWarehouseItem.getQuantity(), quantity);
	}

	@Test
	public void testAddCommerceInventoryWarehouseItemMultipleSku()
		throws PortalException {

		frutillaRule.scenario(
			"Create a new Commerce Inventory Warehouse Item for multiple " +
				"CPInstance with same sku"
		).given(
			"I have two product definitions with a default SKU without " +
				"UnitOfMeasure and a Commerce Inventory Warehouse Item"
		).when(
			"A commerce unit of measure is added to the first sku"
		).then(
			"The first sku should have inventory set and the second one not."
		);

		CPInstance cpInstance = CPTestUtil.addCPInstanceFromCatalog(
			_commerceCatalog.getGroupId());

		_cpDefinitions.add(cpInstance.getCPDefinition());

		BigDecimal quantity = BigDecimal.TEN;

		_commerceInventoryWarehouseItemLocalService.
			addCommerceInventoryWarehouseItem(
				RandomTestUtil.randomString(), _user.getUserId(),
				_commerceInventoryWarehouse.getCommerceInventoryWarehouseId(),
				_cpInstance.getSku(), null, quantity);

		Assert.assertTrue(
			CommerceBigDecimalUtil.eq(
				quantity,
				_commerceInventoryWarehouseItemService.getStockQuantity(
					_company.getCompanyId(), _cpInstance.getSku(),
					StringPool.BLANK)));
		Assert.assertTrue(
			CommerceBigDecimalUtil.eq(
				quantity,
				_commerceInventoryWarehouseItemService.getStockQuantity(
					_company.getCompanyId(), cpInstance.getSku(),
					StringPool.BLANK)));

		String key = RandomTestUtil.randomString();

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, key, RandomTestUtil.randomLocaleStringMap(), 2,
				true, 0.0, BigDecimal.ONE, _cpInstance.getSku());

		Assert.assertTrue(
			CommerceBigDecimalUtil.eq(
				quantity,
				_commerceInventoryWarehouseItemService.getStockQuantity(
					_company.getCompanyId(), _cpInstance.getSku(), key)));

		Assert.assertTrue(
			CommerceBigDecimalUtil.eq(
				BigDecimal.ZERO,
				_commerceInventoryWarehouseItemService.getStockQuantity(
					_company.getCompanyId(), cpInstance.getSku(),
					StringPool.BLANK)));
	}

	@Test(expected = NoSuchCPInstanceException.class)
	public void testAddCommerceInventoryWarehouseItemWithInvalidSku()
		throws PortalException {

		frutillaRule.scenario(
			"Create a new Commerce Inventory Warehouse Item for a CPInstance"
		).given(
			"I have a product definition with a default SKU and UnitOfMeasure"
		).when(
			"Commerce Inventory Warehouse Item is added with a not valid Sku"
		).then(
			"Commerce Inventory Warehouse Item is not created"
		).and(
			"An exception is thrown."
		);

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, RandomTestUtil.randomString(),
				RandomTestUtil.randomLocaleStringMap(), 2, true, 0.0,
				BigDecimal.ONE, _cpInstance.getSku());

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				addCommerceInventoryWarehouseItem(
					RandomTestUtil.randomString(), _user.getUserId(),
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					RandomTestUtil.randomString(),
					_cpInstanceUnitOfMeasure.getKey(), BigDecimal.TEN);

		Assert.assertNull(commerceInventoryWarehouseItem);

		List<CommerceInventoryWarehouseItem> commerceInventoryWarehouseItems =
			_commerceInventoryWarehouseItemLocalService.
				getCommerceInventoryWarehouseItems(
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertTrue(commerceInventoryWarehouseItems.isEmpty());
	}

	@Test(expected = NoSuchCPInstanceUnitOfMeasureException.class)
	public void testAddCommerceInventoryWarehouseItemWithInvalidUnitOfMeasureKey()
		throws PortalException {

		frutillaRule.scenario(
			"Create a new Commerce Inventory Warehouse Item for a CPInstance"
		).given(
			"I have a product definition with a default SKU and UnitOfMeasure"
		).when(
			"Commerce Inventory Warehouse Item is added with a not valid " +
				"unit of measure key"
		).then(
			"Commerce Inventory Warehouse Item is not created"
		).and(
			"An exception is thrown."
		);

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				addCommerceInventoryWarehouseItem(
					RandomTestUtil.randomString(), _user.getUserId(),
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					_cpInstance.getSku(), RandomTestUtil.randomString(),
					BigDecimal.TEN);

		Assert.assertNull(commerceInventoryWarehouseItem);

		List<CommerceInventoryWarehouseItem> commerceInventoryWarehouseItems =
			_commerceInventoryWarehouseItemLocalService.
				getCommerceInventoryWarehouseItems(
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertTrue(commerceInventoryWarehouseItems.isEmpty());
	}

	@Test(expected = CommerceInventoryWarehouseItemSkuException.class)
	public void testAddCommerceInventoryWarehouseItemWithoutSku()
		throws PortalException {

		frutillaRule.scenario(
			"Create a new Commerce Inventory Warehouse Item for a CPInstance"
		).given(
			"I have a product definition with a default SKU and UnitOfMeasure"
		).when(
			"Commerce Inventory Warehouse Item is added without Sku"
		).then(
			"Commerce Inventory Warehouse Item is not created"
		).and(
			"An exception is thrown."
		);

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, RandomTestUtil.randomString(),
				RandomTestUtil.randomLocaleStringMap(), 2, true, 0.0,
				BigDecimal.ONE, _cpInstance.getSku());

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				addCommerceInventoryWarehouseItem(
					RandomTestUtil.randomString(), _user.getUserId(),
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					null, _cpInstanceUnitOfMeasure.getKey(), BigDecimal.TEN);

		Assert.assertNull(commerceInventoryWarehouseItem);

		List<CommerceInventoryWarehouseItem> commerceInventoryWarehouseItems =
			_commerceInventoryWarehouseItemLocalService.
				getCommerceInventoryWarehouseItems(
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertTrue(commerceInventoryWarehouseItems.isEmpty());
	}

	@Test
	public void testAddCommerceInventoryWarehouseItemWithoutUnitOfMeasureKeyWithoutUOM()
		throws PortalException {

		frutillaRule.scenario(
			"Create a new Commerce Inventory Warehouse Item for a CPInstance"
		).given(
			"I have a product definition with a default SKU without " +
				"UnitOfMeasure"
		).when(
			"Commerce Inventory Warehouse Item is added without a unit of " +
				"measure key"
		).then(
			"Commerce Inventory Warehouse Item is created."
		);

		BigDecimal quantity = BigDecimal.TEN;

		_commerceInventoryWarehouseItemLocalService.
			addCommerceInventoryWarehouseItem(
				RandomTestUtil.randomString(), _user.getUserId(),
				_commerceInventoryWarehouse.getCommerceInventoryWarehouseId(),
				_cpInstance.getSku(), null, quantity);

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				getCommerceInventoryWarehouseItem(
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					_cpInstance.getSku(), null);

		Assert.assertNotNull(commerceInventoryWarehouseItem);

		Assert.assertEquals(
			commerceInventoryWarehouseItem.toString(), quantity,
			commerceInventoryWarehouseItem.getQuantity());
	}

	@Test(
		expected = CommerceInventoryWarehouseItemUnitOfMeasureKeyException.class
	)
	public void testAddCommerceInventoryWarehouseItemWithoutUnitOfMeasureKeyWithUOM()
		throws PortalException {

		frutillaRule.scenario(
			"Create a new Commerce Inventory Warehouse Item for a CPInstance"
		).given(
			"I have a product definition with a default SKU and UnitOfMeasure"
		).when(
			"Commerce Inventory Warehouse Item is added without a unit of " +
				"measure key"
		).then(
			"Commerce Inventory Warehouse Item is not created"
		).and(
			"An exception is thrown."
		);

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, RandomTestUtil.randomString(),
				RandomTestUtil.randomLocaleStringMap(), 2, true, 0.0,
				BigDecimal.ONE, _cpInstance.getSku());

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				addCommerceInventoryWarehouseItem(
					RandomTestUtil.randomString(), _user.getUserId(),
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					_cpInstance.getSku(), null, BigDecimal.TEN);

		Assert.assertNull(commerceInventoryWarehouseItem);

		List<CommerceInventoryWarehouseItem> commerceInventoryWarehouseItems =
			_commerceInventoryWarehouseItemLocalService.
				getCommerceInventoryWarehouseItems(
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertTrue(commerceInventoryWarehouseItems.isEmpty());
	}

	@Test(expected = NoSuchInventoryWarehouseItemException.class)
	public void testGetCommerceInventoryWarehouseItemWithoutUnitOfMeasureKey()
		throws PortalException {

		frutillaRule.scenario(
			"Get a commerce inventory warehouse item by sky and unit of " +
				"measure key"
		).given(
			"I have multiple commerce inventory warehouse item for the same sku"
		).when(
			"Search by only sku"
		).then(
			"An exception is thrown."
		);

		String key = RandomTestUtil.randomString();
		BigDecimal quantity = BigDecimal.TEN;

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, key, RandomTestUtil.randomLocaleStringMap(), 2,
				true, 0.0, BigDecimal.ONE, _cpInstance.getSku());

		_commerceInventoryWarehouseItemLocalService.
			addCommerceInventoryWarehouseItem(
				RandomTestUtil.randomString(), _user.getUserId(),
				_commerceInventoryWarehouse.getCommerceInventoryWarehouseId(),
				_cpInstance.getSku(), _cpInstanceUnitOfMeasure.getKey(),
				quantity);

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, RandomTestUtil.randomString(),
				RandomTestUtil.randomLocaleStringMap(), 2, false, 0.0,
				BigDecimal.ONE, _cpInstance.getSku());

		Assert.assertNull(
			_commerceInventoryWarehouseItemLocalService.
				getCommerceInventoryWarehouseItem(
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					_cpInstance.getSku(), null));
	}

	@Test
	public void testGetCommerceInventoryWarehouseItemWithUnitOfMeasureKey()
		throws PortalException {

		frutillaRule.scenario(
			"Get a commerce inventory warehouse item by sky and unit of " +
				"measure key"
		).given(
			"I have multiple commerce inventory warehouse item for the same sku"
		).when(
			"Search by sku and unit of measure key"
		).then(
			"The correct commerce inventory warehouse item is returned."
		);

		String key = RandomTestUtil.randomString();
		BigDecimal quantity = BigDecimal.TEN;

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, key, RandomTestUtil.randomLocaleStringMap(), 2,
				true, 0.0, BigDecimal.ONE, _cpInstance.getSku());

		_commerceInventoryWarehouseItemLocalService.
			addCommerceInventoryWarehouseItem(
				RandomTestUtil.randomString(), _user.getUserId(),
				_commerceInventoryWarehouse.getCommerceInventoryWarehouseId(),
				_cpInstance.getSku(), _cpInstanceUnitOfMeasure.getKey(),
				quantity);

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, RandomTestUtil.randomString(),
				RandomTestUtil.randomLocaleStringMap(), 2, false, 0.0,
				BigDecimal.ONE, _cpInstance.getSku());

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				getCommerceInventoryWarehouseItem(
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					_cpInstance.getSku(), key);

		Assert.assertNotNull(commerceInventoryWarehouseItem);

		Assert.assertEquals(
			commerceInventoryWarehouseItem.toString(), key,
			commerceInventoryWarehouseItem.getUnitOfMeasureKey());
		Assert.assertEquals(
			commerceInventoryWarehouseItem.toString(), quantity,
			commerceInventoryWarehouseItem.getQuantity());
	}

	@Test
	public void testUpdateCommerceInventoryWarehouseItem()
		throws PortalException {

		frutillaRule.scenario(
			"Update a commerce inventory warehouse item for a CPInstance"
		).given(
			"I have a commerce inventory warehouse item with a unit of " +
				"measure key"
		).when(
			"Quantity and reserved quantity fields are updated"
		).then(
			"Quantity and reserved quantity fields are saved with the new " +
				"value."
		);

		_cpInstanceUnitOfMeasure =
			_cpInstanceUnitOfMeasureLocalService.addCPInstanceUnitOfMeasure(
				_user.getUserId(), _cpInstance.getCPInstanceId(), true,
				BigDecimal.ONE, RandomTestUtil.randomString(),
				RandomTestUtil.randomLocaleStringMap(), 2, true, 0.0,
				BigDecimal.ONE, _cpInstance.getSku());

		BigDecimal quantity = BigDecimal.TEN;

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				addCommerceInventoryWarehouseItem(
					RandomTestUtil.randomString(), _user.getUserId(),
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					_cpInstance.getSku(), _cpInstanceUnitOfMeasure.getKey(),
					quantity);

		quantity = BigDecimal.ONE;

		BigDecimal reservedQuantity = BigDecimal.TEN;

		_commerceInventoryWarehouseItemLocalService.
			updateCommerceInventoryWarehouseItem(
				_user.getUserId(),
				commerceInventoryWarehouseItem.
					getCommerceInventoryWarehouseItemId(),
				quantity, reservedQuantity,
				commerceInventoryWarehouseItem.getMvccVersion());

		commerceInventoryWarehouseItem =
			_commerceInventoryWarehouseItemLocalService.
				getCommerceInventoryWarehouseItem(
					_commerceInventoryWarehouse.
						getCommerceInventoryWarehouseId(),
					_cpInstance.getSku(), _cpInstanceUnitOfMeasure.getKey());

		Assert.assertNotNull(commerceInventoryWarehouseItem);

		Assert.assertEquals(
			commerceInventoryWarehouseItem.toString(), quantity,
			commerceInventoryWarehouseItem.getQuantity());
		Assert.assertEquals(
			commerceInventoryWarehouseItem.toString(), reservedQuantity,
			commerceInventoryWarehouseItem.getReservedQuantity());
	}

	@Rule
	public final FrutillaRule frutillaRule = new FrutillaRule();

	private static Company _company;
	private static User _user;

	@DeleteAfterTestRun
	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@DeleteAfterTestRun
	private CommerceInventoryWarehouse _commerceInventoryWarehouse;

	@Inject
	private CommerceInventoryWarehouseItemLocalService
		_commerceInventoryWarehouseItemLocalService;

	@Inject
	private CommerceInventoryWarehouseItemService
		_commerceInventoryWarehouseItemService;

	@Inject
	private CommerceInventoryWarehouseLocalService
		_commerceInventoryWarehouseLocalService;

	@DeleteAfterTestRun
	private final List<CPDefinition> _cpDefinitions = new ArrayList<>();

	@DeleteAfterTestRun
	private CPInstance _cpInstance;

	@DeleteAfterTestRun
	private CPInstanceUnitOfMeasure _cpInstanceUnitOfMeasure;

	@Inject
	private CPInstanceUnitOfMeasureLocalService
		_cpInstanceUnitOfMeasureLocalService;

}