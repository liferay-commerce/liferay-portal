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

package com.liferay.commerce.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.exception.NoSuchRegionLocalizationException;
import com.liferay.commerce.model.CommerceRegionLocalization;
import com.liferay.commerce.service.persistence.CommerceRegionLocalizationPersistence;
import com.liferay.commerce.service.persistence.CommerceRegionLocalizationUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @generated
 */
@RunWith(Arquillian.class)
public class CommerceRegionLocalizationPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED, "com.liferay.commerce.service"));

	@Before
	public void setUp() {
		_persistence = CommerceRegionLocalizationUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<CommerceRegionLocalization> iterator =
			_commerceRegionLocalizations.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		CommerceRegionLocalization commerceRegionLocalization =
			_persistence.create(pk);

		Assert.assertNotNull(commerceRegionLocalization);

		Assert.assertEquals(commerceRegionLocalization.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		CommerceRegionLocalization newCommerceRegionLocalization =
			addCommerceRegionLocalization();

		_persistence.remove(newCommerceRegionLocalization);

		CommerceRegionLocalization existingCommerceRegionLocalization =
			_persistence.fetchByPrimaryKey(
				newCommerceRegionLocalization.getPrimaryKey());

		Assert.assertNull(existingCommerceRegionLocalization);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addCommerceRegionLocalization();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		CommerceRegionLocalization newCommerceRegionLocalization =
			_persistence.create(pk);

		newCommerceRegionLocalization.setMvccVersion(RandomTestUtil.nextLong());

		newCommerceRegionLocalization.setCompanyId(RandomTestUtil.nextLong());

		newCommerceRegionLocalization.setCommerceRegionId(
			RandomTestUtil.nextLong());

		newCommerceRegionLocalization.setLanguageId(
			RandomTestUtil.randomString());

		newCommerceRegionLocalization.setName(RandomTestUtil.randomString());

		_commerceRegionLocalizations.add(
			_persistence.update(newCommerceRegionLocalization));

		CommerceRegionLocalization existingCommerceRegionLocalization =
			_persistence.findByPrimaryKey(
				newCommerceRegionLocalization.getPrimaryKey());

		Assert.assertEquals(
			existingCommerceRegionLocalization.getMvccVersion(),
			newCommerceRegionLocalization.getMvccVersion());
		Assert.assertEquals(
			existingCommerceRegionLocalization.
				getCommerceRegionLocalizationId(),
			newCommerceRegionLocalization.getCommerceRegionLocalizationId());
		Assert.assertEquals(
			existingCommerceRegionLocalization.getCompanyId(),
			newCommerceRegionLocalization.getCompanyId());
		Assert.assertEquals(
			existingCommerceRegionLocalization.getCommerceRegionId(),
			newCommerceRegionLocalization.getCommerceRegionId());
		Assert.assertEquals(
			existingCommerceRegionLocalization.getLanguageId(),
			newCommerceRegionLocalization.getLanguageId());
		Assert.assertEquals(
			existingCommerceRegionLocalization.getName(),
			newCommerceRegionLocalization.getName());
	}

	@Test
	public void testCountByCommerceRegionId() throws Exception {
		_persistence.countByCommerceRegionId(RandomTestUtil.nextLong());

		_persistence.countByCommerceRegionId(0L);
	}

	@Test
	public void testCountByCommerceRegionId_LanguageId() throws Exception {
		_persistence.countByCommerceRegionId_LanguageId(
			RandomTestUtil.nextLong(), "");

		_persistence.countByCommerceRegionId_LanguageId(0L, "null");

		_persistence.countByCommerceRegionId_LanguageId(0L, (String)null);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		CommerceRegionLocalization newCommerceRegionLocalization =
			addCommerceRegionLocalization();

		CommerceRegionLocalization existingCommerceRegionLocalization =
			_persistence.findByPrimaryKey(
				newCommerceRegionLocalization.getPrimaryKey());

		Assert.assertEquals(
			existingCommerceRegionLocalization, newCommerceRegionLocalization);
	}

	@Test(expected = NoSuchRegionLocalizationException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<CommerceRegionLocalization>
		getOrderByComparator() {

		return OrderByComparatorFactoryUtil.create(
			"CommerceRegionLocalization", "mvccVersion", true,
			"commerceRegionLocalizationId", true, "companyId", true,
			"commerceRegionId", true, "languageId", true, "name", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		CommerceRegionLocalization newCommerceRegionLocalization =
			addCommerceRegionLocalization();

		CommerceRegionLocalization existingCommerceRegionLocalization =
			_persistence.fetchByPrimaryKey(
				newCommerceRegionLocalization.getPrimaryKey());

		Assert.assertEquals(
			existingCommerceRegionLocalization, newCommerceRegionLocalization);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		CommerceRegionLocalization missingCommerceRegionLocalization =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingCommerceRegionLocalization);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		CommerceRegionLocalization newCommerceRegionLocalization1 =
			addCommerceRegionLocalization();
		CommerceRegionLocalization newCommerceRegionLocalization2 =
			addCommerceRegionLocalization();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newCommerceRegionLocalization1.getPrimaryKey());
		primaryKeys.add(newCommerceRegionLocalization2.getPrimaryKey());

		Map<Serializable, CommerceRegionLocalization>
			commerceRegionLocalizations = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(2, commerceRegionLocalizations.size());
		Assert.assertEquals(
			newCommerceRegionLocalization1,
			commerceRegionLocalizations.get(
				newCommerceRegionLocalization1.getPrimaryKey()));
		Assert.assertEquals(
			newCommerceRegionLocalization2,
			commerceRegionLocalizations.get(
				newCommerceRegionLocalization2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, CommerceRegionLocalization>
			commerceRegionLocalizations = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(commerceRegionLocalizations.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		CommerceRegionLocalization newCommerceRegionLocalization =
			addCommerceRegionLocalization();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newCommerceRegionLocalization.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, CommerceRegionLocalization>
			commerceRegionLocalizations = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, commerceRegionLocalizations.size());
		Assert.assertEquals(
			newCommerceRegionLocalization,
			commerceRegionLocalizations.get(
				newCommerceRegionLocalization.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, CommerceRegionLocalization>
			commerceRegionLocalizations = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(commerceRegionLocalizations.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		CommerceRegionLocalization newCommerceRegionLocalization =
			addCommerceRegionLocalization();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newCommerceRegionLocalization.getPrimaryKey());

		Map<Serializable, CommerceRegionLocalization>
			commerceRegionLocalizations = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, commerceRegionLocalizations.size());
		Assert.assertEquals(
			newCommerceRegionLocalization,
			commerceRegionLocalizations.get(
				newCommerceRegionLocalization.getPrimaryKey()));
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		CommerceRegionLocalization newCommerceRegionLocalization =
			addCommerceRegionLocalization();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CommerceRegionLocalization.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"commerceRegionLocalizationId",
				newCommerceRegionLocalization.
					getCommerceRegionLocalizationId()));

		List<CommerceRegionLocalization> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		CommerceRegionLocalization existingCommerceRegionLocalization =
			result.get(0);

		Assert.assertEquals(
			existingCommerceRegionLocalization, newCommerceRegionLocalization);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CommerceRegionLocalization.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"commerceRegionLocalizationId", RandomTestUtil.nextLong()));

		List<CommerceRegionLocalization> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		CommerceRegionLocalization newCommerceRegionLocalization =
			addCommerceRegionLocalization();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CommerceRegionLocalization.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("commerceRegionLocalizationId"));

		Object newCommerceRegionLocalizationId =
			newCommerceRegionLocalization.getCommerceRegionLocalizationId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"commerceRegionLocalizationId",
				new Object[] {newCommerceRegionLocalizationId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingCommerceRegionLocalizationId = result.get(0);

		Assert.assertEquals(
			existingCommerceRegionLocalizationId,
			newCommerceRegionLocalizationId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CommerceRegionLocalization.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("commerceRegionLocalizationId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"commerceRegionLocalizationId",
				new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		CommerceRegionLocalization newCommerceRegionLocalization =
			addCommerceRegionLocalization();

		_persistence.clearCache();

		_assertOriginalValues(
			_persistence.findByPrimaryKey(
				newCommerceRegionLocalization.getPrimaryKey()));
	}

	@Test
	public void testResetOriginalValuesWithDynamicQueryLoadFromDatabase()
		throws Exception {

		_testResetOriginalValuesWithDynamicQuery(true);
	}

	@Test
	public void testResetOriginalValuesWithDynamicQueryLoadFromSession()
		throws Exception {

		_testResetOriginalValuesWithDynamicQuery(false);
	}

	private void _testResetOriginalValuesWithDynamicQuery(boolean clearSession)
		throws Exception {

		CommerceRegionLocalization newCommerceRegionLocalization =
			addCommerceRegionLocalization();

		if (clearSession) {
			Session session = _persistence.openSession();

			session.flush();

			session.clear();
		}

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CommerceRegionLocalization.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"commerceRegionLocalizationId",
				newCommerceRegionLocalization.
					getCommerceRegionLocalizationId()));

		List<CommerceRegionLocalization> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		_assertOriginalValues(result.get(0));
	}

	private void _assertOriginalValues(
		CommerceRegionLocalization commerceRegionLocalization) {

		Assert.assertEquals(
			Long.valueOf(commerceRegionLocalization.getCommerceRegionId()),
			ReflectionTestUtil.<Long>invoke(
				commerceRegionLocalization, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "commerceRegionId"));
		Assert.assertEquals(
			commerceRegionLocalization.getLanguageId(),
			ReflectionTestUtil.invoke(
				commerceRegionLocalization, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "languageId"));
	}

	protected CommerceRegionLocalization addCommerceRegionLocalization()
		throws Exception {

		long pk = RandomTestUtil.nextLong();

		CommerceRegionLocalization commerceRegionLocalization =
			_persistence.create(pk);

		commerceRegionLocalization.setMvccVersion(RandomTestUtil.nextLong());

		commerceRegionLocalization.setCompanyId(RandomTestUtil.nextLong());

		commerceRegionLocalization.setCommerceRegionId(
			RandomTestUtil.nextLong());

		commerceRegionLocalization.setLanguageId(RandomTestUtil.randomString());

		commerceRegionLocalization.setName(RandomTestUtil.randomString());

		_commerceRegionLocalizations.add(
			_persistence.update(commerceRegionLocalization));

		return commerceRegionLocalization;
	}

	private List<CommerceRegionLocalization> _commerceRegionLocalizations =
		new ArrayList<CommerceRegionLocalization>();
	private CommerceRegionLocalizationPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}