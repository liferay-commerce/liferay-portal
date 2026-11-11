/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.exception.DuplicateCPDefinitionVisibilityRelExternalReferenceCodeException;
import com.liferay.commerce.product.exception.NoSuchCPDefinitionVisibilityRelException;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.commerce.product.service.CPDefinitionVisibilityRelLocalServiceUtil;
import com.liferay.commerce.product.service.persistence.CPDefinitionVisibilityRelPersistence;
import com.liferay.commerce.product.service.persistence.CPDefinitionVisibilityRelUtil;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
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
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Time;
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
public class CPDefinitionVisibilityRelPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED, "com.liferay.commerce.product.service"));

	@Before
	public void setUp() {
		_persistence = CPDefinitionVisibilityRelUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<CPDefinitionVisibilityRel> iterator =
			_cpDefinitionVisibilityRels.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_persistence.create(pk);

		Assert.assertNotNull(cpDefinitionVisibilityRel);

		Assert.assertEquals(cpDefinitionVisibilityRel.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		_persistence.remove(newCPDefinitionVisibilityRel);

		CPDefinitionVisibilityRel existingCPDefinitionVisibilityRel =
			_persistence.fetchByPrimaryKey(
				newCPDefinitionVisibilityRel.getPrimaryKey());

		Assert.assertNull(existingCPDefinitionVisibilityRel);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addCPDefinitionVisibilityRel();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		newCPDefinitionVisibilityRel.setCtCollectionId(
			RandomTestUtil.nextLong());

		newCPDefinitionVisibilityRel.setExternalReferenceCode(
			RandomTestUtil.randomString());

		newCPDefinitionVisibilityRel.setCompanyId(RandomTestUtil.nextLong());

		newCPDefinitionVisibilityRel.setUserId(RandomTestUtil.nextLong());

		newCPDefinitionVisibilityRel.setUserName(RandomTestUtil.randomString());

		newCPDefinitionVisibilityRel.setCreateDate(RandomTestUtil.nextDate());

		newCPDefinitionVisibilityRel.setModifiedDate(RandomTestUtil.nextDate());

		newCPDefinitionVisibilityRel.setCPDefinitionId(
			RandomTestUtil.nextLong());

		newCPDefinitionVisibilityRel.setClassNameId(RandomTestUtil.nextLong());

		newCPDefinitionVisibilityRel.setClassPK(RandomTestUtil.nextLong());

		newCPDefinitionVisibilityRel = _persistence.update(
			newCPDefinitionVisibilityRel);

		_cpDefinitionVisibilityRels.add(newCPDefinitionVisibilityRel);

		CPDefinitionVisibilityRel existingCPDefinitionVisibilityRel =
			_persistence.findByPrimaryKey(
				newCPDefinitionVisibilityRel.getPrimaryKey());

		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getMvccVersion(),
			newCPDefinitionVisibilityRel.getMvccVersion());
		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getCtCollectionId(),
			newCPDefinitionVisibilityRel.getCtCollectionId());
		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getExternalReferenceCode(),
			newCPDefinitionVisibilityRel.getExternalReferenceCode());
		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getCPDefinitionVisibilityRelId(),
			newCPDefinitionVisibilityRel.getCPDefinitionVisibilityRelId());
		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getCompanyId(),
			newCPDefinitionVisibilityRel.getCompanyId());
		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getUserId(),
			newCPDefinitionVisibilityRel.getUserId());
		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getUserName(),
			newCPDefinitionVisibilityRel.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingCPDefinitionVisibilityRel.getCreateDate()),
			Time.getShortTimestamp(
				newCPDefinitionVisibilityRel.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingCPDefinitionVisibilityRel.getModifiedDate()),
			Time.getShortTimestamp(
				newCPDefinitionVisibilityRel.getModifiedDate()));
		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getCPDefinitionId(),
			newCPDefinitionVisibilityRel.getCPDefinitionId());
		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getClassNameId(),
			newCPDefinitionVisibilityRel.getClassNameId());
		Assert.assertEquals(
			existingCPDefinitionVisibilityRel.getClassPK(),
			newCPDefinitionVisibilityRel.getClassPK());
	}

	@Test(
		expected = DuplicateCPDefinitionVisibilityRelExternalReferenceCodeException.class
	)
	public void testUpdateWithExistingExternalReferenceCode() throws Exception {
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		newCPDefinitionVisibilityRel.setCompanyId(
			cpDefinitionVisibilityRel.getCompanyId());

		newCPDefinitionVisibilityRel = _persistence.update(
			newCPDefinitionVisibilityRel);

		Session session = _persistence.getCurrentSession();

		session.evict(newCPDefinitionVisibilityRel);

		newCPDefinitionVisibilityRel.setExternalReferenceCode(
			cpDefinitionVisibilityRel.getExternalReferenceCode());

		_persistence.update(newCPDefinitionVisibilityRel);
	}

	@Test
	public void testCountByCPDefinitionId() throws Exception {
		_persistence.countByCPDefinitionId(RandomTestUtil.nextLong());

		_persistence.countByCPDefinitionId(0L);
	}

	@Test
	public void testCountByCPD_C() throws Exception {
		_persistence.countByCPD_C(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong());

		_persistence.countByCPD_C(0L, 0L);
	}

	@Test
	public void testCountByC_C() throws Exception {
		_persistence.countByC_C(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong());

		_persistence.countByC_C(0L, 0L);
	}

	@Test
	public void testCountByCPD_C_C() throws Exception {
		_persistence.countByCPD_C_C(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong(),
			RandomTestUtil.nextLong());

		_persistence.countByCPD_C_C(0L, 0L, 0L);
	}

	@Test
	public void testCountByERC_C() throws Exception {
		_persistence.countByERC_C("", RandomTestUtil.nextLong());

		_persistence.countByERC_C("null", 0L);

		_persistence.countByERC_C((String)null, 0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		CPDefinitionVisibilityRel existingCPDefinitionVisibilityRel =
			_persistence.findByPrimaryKey(
				newCPDefinitionVisibilityRel.getPrimaryKey());

		Assert.assertEquals(
			existingCPDefinitionVisibilityRel, newCPDefinitionVisibilityRel);
	}

	@Test(expected = NoSuchCPDefinitionVisibilityRelException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<CPDefinitionVisibilityRel>
		getOrderByComparator() {

		return OrderByComparatorFactoryUtil.create(
			"CPDefinitionVisibilityRel", "mvccVersion", true, "ctCollectionId",
			true, "externalReferenceCode", true, "CPDefinitionVisibilityRelId",
			true, "companyId", true, "userId", true, "userName", true,
			"createDate", true, "modifiedDate", true, "CPDefinitionId", true,
			"classNameId", true, "classPK", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		CPDefinitionVisibilityRel existingCPDefinitionVisibilityRel =
			_persistence.fetchByPrimaryKey(
				newCPDefinitionVisibilityRel.getPrimaryKey());

		Assert.assertEquals(
			existingCPDefinitionVisibilityRel, newCPDefinitionVisibilityRel);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		CPDefinitionVisibilityRel missingCPDefinitionVisibilityRel =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingCPDefinitionVisibilityRel);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel1 =
			addCPDefinitionVisibilityRel();
		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel2 =
			addCPDefinitionVisibilityRel();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newCPDefinitionVisibilityRel1.getPrimaryKey());
		primaryKeys.add(newCPDefinitionVisibilityRel2.getPrimaryKey());

		Map<Serializable, CPDefinitionVisibilityRel>
			cpDefinitionVisibilityRels = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(2, cpDefinitionVisibilityRels.size());
		Assert.assertEquals(
			newCPDefinitionVisibilityRel1,
			cpDefinitionVisibilityRels.get(
				newCPDefinitionVisibilityRel1.getPrimaryKey()));
		Assert.assertEquals(
			newCPDefinitionVisibilityRel2,
			cpDefinitionVisibilityRels.get(
				newCPDefinitionVisibilityRel2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, CPDefinitionVisibilityRel>
			cpDefinitionVisibilityRels = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(cpDefinitionVisibilityRels.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newCPDefinitionVisibilityRel.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, CPDefinitionVisibilityRel>
			cpDefinitionVisibilityRels = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, cpDefinitionVisibilityRels.size());
		Assert.assertEquals(
			newCPDefinitionVisibilityRel,
			cpDefinitionVisibilityRels.get(
				newCPDefinitionVisibilityRel.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, CPDefinitionVisibilityRel>
			cpDefinitionVisibilityRels = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(cpDefinitionVisibilityRels.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newCPDefinitionVisibilityRel.getPrimaryKey());

		Map<Serializable, CPDefinitionVisibilityRel>
			cpDefinitionVisibilityRels = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, cpDefinitionVisibilityRels.size());
		Assert.assertEquals(
			newCPDefinitionVisibilityRel,
			cpDefinitionVisibilityRels.get(
				newCPDefinitionVisibilityRel.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			CPDefinitionVisibilityRelLocalServiceUtil.
				getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod
				<CPDefinitionVisibilityRel>() {

				@Override
				public void performAction(
					CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

					Assert.assertNotNull(cpDefinitionVisibilityRel);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CPDefinitionVisibilityRel.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"CPDefinitionVisibilityRelId",
				newCPDefinitionVisibilityRel.getCPDefinitionVisibilityRelId()));

		List<CPDefinitionVisibilityRel> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		CPDefinitionVisibilityRel existingCPDefinitionVisibilityRel =
			result.get(0);

		Assert.assertEquals(
			existingCPDefinitionVisibilityRel, newCPDefinitionVisibilityRel);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CPDefinitionVisibilityRel.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"CPDefinitionVisibilityRelId", RandomTestUtil.nextLong()));

		List<CPDefinitionVisibilityRel> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CPDefinitionVisibilityRel.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("CPDefinitionVisibilityRelId"));

		Object newCPDefinitionVisibilityRelId =
			newCPDefinitionVisibilityRel.getCPDefinitionVisibilityRelId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"CPDefinitionVisibilityRelId",
				new Object[] {newCPDefinitionVisibilityRelId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingCPDefinitionVisibilityRelId = result.get(0);

		Assert.assertEquals(
			existingCPDefinitionVisibilityRelId,
			newCPDefinitionVisibilityRelId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CPDefinitionVisibilityRel.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("CPDefinitionVisibilityRelId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"CPDefinitionVisibilityRelId",
				new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		_persistence.clearCache();

		_assertOriginalValues(
			_persistence.findByPrimaryKey(
				newCPDefinitionVisibilityRel.getPrimaryKey()));
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

		CPDefinitionVisibilityRel newCPDefinitionVisibilityRel =
			addCPDefinitionVisibilityRel();

		if (clearSession) {
			Session session = _persistence.openSession();

			session.flush();

			session.clear();
		}

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CPDefinitionVisibilityRel.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"CPDefinitionVisibilityRelId",
				newCPDefinitionVisibilityRel.getCPDefinitionVisibilityRelId()));

		List<CPDefinitionVisibilityRel> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		_assertOriginalValues(result.get(0));
	}

	private void _assertOriginalValues(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		Assert.assertEquals(
			Long.valueOf(cpDefinitionVisibilityRel.getCPDefinitionId()),
			ReflectionTestUtil.<Long>invoke(
				cpDefinitionVisibilityRel, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "CPDefinitionId"));
		Assert.assertEquals(
			Long.valueOf(cpDefinitionVisibilityRel.getClassNameId()),
			ReflectionTestUtil.<Long>invoke(
				cpDefinitionVisibilityRel, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "classNameId"));
		Assert.assertEquals(
			Long.valueOf(cpDefinitionVisibilityRel.getClassPK()),
			ReflectionTestUtil.<Long>invoke(
				cpDefinitionVisibilityRel, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "classPK"));

		Assert.assertEquals(
			cpDefinitionVisibilityRel.getExternalReferenceCode(),
			ReflectionTestUtil.invoke(
				cpDefinitionVisibilityRel, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "externalReferenceCode"));
		Assert.assertEquals(
			Long.valueOf(cpDefinitionVisibilityRel.getCompanyId()),
			ReflectionTestUtil.<Long>invoke(
				cpDefinitionVisibilityRel, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "companyId"));
	}

	protected CPDefinitionVisibilityRel addCPDefinitionVisibilityRel()
		throws Exception {

		long pk = RandomTestUtil.nextLong();

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			_persistence.create(pk);

		cpDefinitionVisibilityRel.setCtCollectionId(RandomTestUtil.nextLong());

		cpDefinitionVisibilityRel.setExternalReferenceCode(
			RandomTestUtil.randomString());

		cpDefinitionVisibilityRel.setCompanyId(RandomTestUtil.nextLong());

		cpDefinitionVisibilityRel.setUserId(RandomTestUtil.nextLong());

		cpDefinitionVisibilityRel.setUserName(RandomTestUtil.randomString());

		cpDefinitionVisibilityRel.setCreateDate(RandomTestUtil.nextDate());

		cpDefinitionVisibilityRel.setModifiedDate(RandomTestUtil.nextDate());

		cpDefinitionVisibilityRel.setCPDefinitionId(RandomTestUtil.nextLong());

		cpDefinitionVisibilityRel.setClassNameId(RandomTestUtil.nextLong());

		cpDefinitionVisibilityRel.setClassPK(RandomTestUtil.nextLong());

		_cpDefinitionVisibilityRels.add(
			_persistence.update(cpDefinitionVisibilityRel));

		return cpDefinitionVisibilityRel;
	}

	private List<CPDefinitionVisibilityRel> _cpDefinitionVisibilityRels =
		new ArrayList<CPDefinitionVisibilityRel>();
	private CPDefinitionVisibilityRelPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}
// LIFERAY-SERVICE-BUILDER-HASH:874926434