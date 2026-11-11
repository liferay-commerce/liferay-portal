/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.persistence.impl;

import com.liferay.commerce.product.exception.DuplicateCPDefinitionVisibilityRelExternalReferenceCodeException;
import com.liferay.commerce.product.exception.NoSuchCPDefinitionVisibilityRelException;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRelTable;
import com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelImpl;
import com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl;
import com.liferay.commerce.product.service.persistence.CPDefinitionVisibilityRelPersistence;
import com.liferay.commerce.product.service.persistence.CPDefinitionVisibilityRelUtil;
import com.liferay.commerce.product.service.persistence.impl.constants.CommercePersistenceConstants;
import com.liferay.portal.kernel.change.tracking.CTColumnResolutionType;
import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.SessionFactory;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.sanitizer.Sanitizer;
import com.liferay.portal.kernel.sanitizer.SanitizerException;
import com.liferay.portal.kernel.sanitizer.SanitizerUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.persistence.change.tracking.helper.CTPersistenceHelper;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.service.persistence.impl.CollectionPersistenceFinder;
import com.liferay.portal.kernel.service.persistence.impl.FinderColumn;
import com.liferay.portal.kernel.service.persistence.impl.UniquePersistenceFinder;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The persistence implementation for the cp definition visibility rel service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Marco Leo
 * @generated
 */
@Component(service = CPDefinitionVisibilityRelPersistence.class)
public class CPDefinitionVisibilityRelPersistenceImpl
	extends BasePersistenceImpl
		<CPDefinitionVisibilityRel, NoSuchCPDefinitionVisibilityRelException>
	implements CPDefinitionVisibilityRelPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>CPDefinitionVisibilityRelUtil</code> to access the cp definition visibility rel persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		CPDefinitionVisibilityRelImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private CollectionPersistenceFinder
		<CPDefinitionVisibilityRel, NoSuchCPDefinitionVisibilityRelException>
			_collectionPersistenceFinderByCPDefinitionId;

	/**
	 * Returns an ordered range of all the cp definition visibility rels where CPDefinitionId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching cp definition visibility rels
	 */
	@Override
	public List<CPDefinitionVisibilityRel> findByCPDefinitionId(
		long CPDefinitionId, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByCPDefinitionId.find(
			finderCache, new Object[] {CPDefinitionId}, start, end,
			orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel findByCPDefinitionId_First(
			long CPDefinitionId,
			OrderByComparator<CPDefinitionVisibilityRel> orderByComparator)
		throws NoSuchCPDefinitionVisibilityRelException {

		return _collectionPersistenceFinderByCPDefinitionId.findFirst(
			finderCache, new Object[] {CPDefinitionId}, orderByComparator);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel fetchByCPDefinitionId_First(
		long CPDefinitionId,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return _collectionPersistenceFinderByCPDefinitionId.fetchFirst(
			finderCache, new Object[] {CPDefinitionId}, orderByComparator);
	}

	/**
	 * Removes all the cp definition visibility rels where CPDefinitionId = &#63; from the database.
	 *
	 * @param CPDefinitionId the cp definition ID
	 */
	@Override
	public void removeByCPDefinitionId(long CPDefinitionId) {
		_collectionPersistenceFinderByCPDefinitionId.remove(
			finderCache, new Object[] {CPDefinitionId});
	}

	/**
	 * Returns the number of cp definition visibility rels where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @return the number of matching cp definition visibility rels
	 */
	@Override
	public int countByCPDefinitionId(long CPDefinitionId) {
		return _collectionPersistenceFinderByCPDefinitionId.count(
			finderCache, new Object[] {CPDefinitionId});
	}

	private CollectionPersistenceFinder
		<CPDefinitionVisibilityRel, NoSuchCPDefinitionVisibilityRelException>
			_collectionPersistenceFinderByCPD_C;

	/**
	 * Returns an ordered range of all the cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching cp definition visibility rels
	 */
	@Override
	public List<CPDefinitionVisibilityRel> findByCPD_C(
		long CPDefinitionId, long classNameId, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByCPD_C.find(
			finderCache, new Object[] {CPDefinitionId, classNameId}, start, end,
			orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel findByCPD_C_First(
			long CPDefinitionId, long classNameId,
			OrderByComparator<CPDefinitionVisibilityRel> orderByComparator)
		throws NoSuchCPDefinitionVisibilityRelException {

		return _collectionPersistenceFinderByCPD_C.findFirst(
			finderCache, new Object[] {CPDefinitionId, classNameId},
			orderByComparator);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel fetchByCPD_C_First(
		long CPDefinitionId, long classNameId,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return _collectionPersistenceFinderByCPD_C.fetchFirst(
			finderCache, new Object[] {CPDefinitionId, classNameId},
			orderByComparator);
	}

	/**
	 * Removes all the cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63; from the database.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 */
	@Override
	public void removeByCPD_C(long CPDefinitionId, long classNameId) {
		_collectionPersistenceFinderByCPD_C.remove(
			finderCache, new Object[] {CPDefinitionId, classNameId});
	}

	/**
	 * Returns the number of cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @return the number of matching cp definition visibility rels
	 */
	@Override
	public int countByCPD_C(long CPDefinitionId, long classNameId) {
		return _collectionPersistenceFinderByCPD_C.count(
			finderCache, new Object[] {CPDefinitionId, classNameId});
	}

	private CollectionPersistenceFinder
		<CPDefinitionVisibilityRel, NoSuchCPDefinitionVisibilityRelException>
			_collectionPersistenceFinderByC_C;

	/**
	 * Returns an ordered range of all the cp definition visibility rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching cp definition visibility rels
	 */
	@Override
	public List<CPDefinitionVisibilityRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator,
		boolean useFinderCache) {

		return _collectionPersistenceFinderByC_C.find(
			finderCache, new Object[] {classNameId, classPK}, start, end,
			orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel findByC_C_First(
			long classNameId, long classPK,
			OrderByComparator<CPDefinitionVisibilityRel> orderByComparator)
		throws NoSuchCPDefinitionVisibilityRelException {

		return _collectionPersistenceFinderByC_C.findFirst(
			finderCache, new Object[] {classNameId, classPK},
			orderByComparator);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel fetchByC_C_First(
		long classNameId, long classPK,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return _collectionPersistenceFinderByC_C.fetchFirst(
			finderCache, new Object[] {classNameId, classPK},
			orderByComparator);
	}

	/**
	 * Removes all the cp definition visibility rels where classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 */
	@Override
	public void removeByC_C(long classNameId, long classPK) {
		_collectionPersistenceFinderByC_C.remove(
			finderCache, new Object[] {classNameId, classPK});
	}

	/**
	 * Returns the number of cp definition visibility rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching cp definition visibility rels
	 */
	@Override
	public int countByC_C(long classNameId, long classPK) {
		return _collectionPersistenceFinderByC_C.count(
			finderCache, new Object[] {classNameId, classPK});
	}

	private UniquePersistenceFinder
		<CPDefinitionVisibilityRel, NoSuchCPDefinitionVisibilityRelException>
			_uniquePersistenceFinderByCPD_C_C;

	/**
	 * Returns the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; or throws a <code>NoSuchCPDefinitionVisibilityRelException</code> if it could not be found.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel findByCPD_C_C(
			long CPDefinitionId, long classNameId, long classPK)
		throws NoSuchCPDefinitionVisibilityRelException {

		return _uniquePersistenceFinderByCPD_C_C.find(
			finderCache, new Object[] {CPDefinitionId, classNameId, classPK});
	}

	/**
	 * Returns the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel fetchByCPD_C_C(
		long CPDefinitionId, long classNameId, long classPK,
		boolean useFinderCache) {

		return _uniquePersistenceFinderByCPD_C_C.fetch(
			finderCache, new Object[] {CPDefinitionId, classNameId, classPK},
			useFinderCache);
	}

	/**
	 * Removes the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the cp definition visibility rel that was removed
	 */
	@Override
	public CPDefinitionVisibilityRel removeByCPD_C_C(
			long CPDefinitionId, long classNameId, long classPK)
		throws NoSuchCPDefinitionVisibilityRelException {

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel = findByCPD_C_C(
			CPDefinitionId, classNameId, classPK);

		return remove(cpDefinitionVisibilityRel);
	}

	/**
	 * Returns the number of cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching cp definition visibility rels
	 */
	@Override
	public int countByCPD_C_C(
		long CPDefinitionId, long classNameId, long classPK) {

		return _uniquePersistenceFinderByCPD_C_C.count(
			finderCache, new Object[] {CPDefinitionId, classNameId, classPK});
	}

	private UniquePersistenceFinder
		<CPDefinitionVisibilityRel, NoSuchCPDefinitionVisibilityRelException>
			_uniquePersistenceFinderByERC_C;

	/**
	 * Returns the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; or throws a <code>NoSuchCPDefinitionVisibilityRelException</code> if it could not be found.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel findByERC_C(
			String externalReferenceCode, long companyId)
		throws NoSuchCPDefinitionVisibilityRelException {

		return _uniquePersistenceFinderByERC_C.find(
			finderCache, new Object[] {externalReferenceCode, companyId});
	}

	/**
	 * Returns the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel fetchByERC_C(
		String externalReferenceCode, long companyId, boolean useFinderCache) {

		return _uniquePersistenceFinderByERC_C.fetch(
			finderCache, new Object[] {externalReferenceCode, companyId},
			useFinderCache);
	}

	/**
	 * Removes the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; from the database.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the cp definition visibility rel that was removed
	 */
	@Override
	public CPDefinitionVisibilityRel removeByERC_C(
			String externalReferenceCode, long companyId)
		throws NoSuchCPDefinitionVisibilityRelException {

		CPDefinitionVisibilityRel cpDefinitionVisibilityRel = findByERC_C(
			externalReferenceCode, companyId);

		return remove(cpDefinitionVisibilityRel);
	}

	/**
	 * Returns the number of cp definition visibility rels where externalReferenceCode = &#63; and companyId = &#63;.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the number of matching cp definition visibility rels
	 */
	@Override
	public int countByERC_C(String externalReferenceCode, long companyId) {
		return _uniquePersistenceFinderByERC_C.count(
			finderCache, new Object[] {externalReferenceCode, companyId});
	}

	public CPDefinitionVisibilityRelPersistenceImpl() {
		setModelClass(CPDefinitionVisibilityRel.class);

		setModelImplClass(CPDefinitionVisibilityRelImpl.class);
		setModelPKClass(long.class);

		setTable(CPDefinitionVisibilityRelTable.INSTANCE);
	}

	/**
	 * Creates a new cp definition visibility rel with the primary key. Does not add the cp definition visibility rel to the database.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key for the new cp definition visibility rel
	 * @return the new cp definition visibility rel
	 */
	@Override
	public CPDefinitionVisibilityRel create(long CPDefinitionVisibilityRelId) {
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel =
			new CPDefinitionVisibilityRelImpl();

		cpDefinitionVisibilityRel.setNew(true);
		cpDefinitionVisibilityRel.setPrimaryKey(CPDefinitionVisibilityRelId);

		cpDefinitionVisibilityRel.setCompanyId(
			CompanyThreadLocal.getCompanyId());

		return cpDefinitionVisibilityRel;
	}

	/**
	 * Removes the cp definition visibility rel with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel that was removed
	 * @throws NoSuchCPDefinitionVisibilityRelException if a cp definition visibility rel with the primary key could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel remove(long CPDefinitionVisibilityRelId)
		throws NoSuchCPDefinitionVisibilityRelException {

		return remove((Serializable)CPDefinitionVisibilityRelId);
	}

	@Override
	protected CPDefinitionVisibilityRel removeImpl(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		Session session = null;

		try {
			session = openSession();

			if (!session.contains(cpDefinitionVisibilityRel)) {
				cpDefinitionVisibilityRel =
					(CPDefinitionVisibilityRel)session.get(
						CPDefinitionVisibilityRelImpl.class,
						cpDefinitionVisibilityRel.getPrimaryKeyObj());
			}

			if ((cpDefinitionVisibilityRel != null) &&
				ctPersistenceHelper.isRemove(cpDefinitionVisibilityRel)) {

				session.delete(cpDefinitionVisibilityRel);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (cpDefinitionVisibilityRel != null) {
			clearCache(cpDefinitionVisibilityRel);
		}

		return cpDefinitionVisibilityRel;
	}

	@Override
	public CPDefinitionVisibilityRel updateImpl(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		boolean isNew = cpDefinitionVisibilityRel.isNew();

		if (!(cpDefinitionVisibilityRel instanceof
				CPDefinitionVisibilityRelModelImpl)) {

			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(cpDefinitionVisibilityRel.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					cpDefinitionVisibilityRel);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in cpDefinitionVisibilityRel proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom CPDefinitionVisibilityRel implementation " +
					cpDefinitionVisibilityRel.getClass());
		}

		CPDefinitionVisibilityRelModelImpl cpDefinitionVisibilityRelModelImpl =
			(CPDefinitionVisibilityRelModelImpl)cpDefinitionVisibilityRel;

		if (Validator.isNull(
				cpDefinitionVisibilityRel.getExternalReferenceCode())) {

			cpDefinitionVisibilityRel.setExternalReferenceCode(
				String.valueOf(cpDefinitionVisibilityRel.getPrimaryKey()));
		}
		else {
			if (!Objects.equals(
					cpDefinitionVisibilityRelModelImpl.getColumnOriginalValue(
						"externalReferenceCode"),
					cpDefinitionVisibilityRel.getExternalReferenceCode())) {

				long userId = GetterUtil.getLong(
					PrincipalThreadLocal.getName());

				if (userId > 0) {
					long companyId = cpDefinitionVisibilityRel.getCompanyId();

					long groupId = 0;

					long classPK = 0;

					if (!isNew) {
						classPK = cpDefinitionVisibilityRel.getPrimaryKey();
					}

					try {
						cpDefinitionVisibilityRel.setExternalReferenceCode(
							SanitizerUtil.sanitize(
								companyId, groupId, userId,
								CPDefinitionVisibilityRel.class.getName(),
								classPK, ContentTypes.TEXT_HTML,
								Sanitizer.MODE_ALL,
								cpDefinitionVisibilityRel.
									getExternalReferenceCode(),
								null));
					}
					catch (SanitizerException sanitizerException) {
						throw new SystemException(sanitizerException);
					}
				}
			}

			CPDefinitionVisibilityRel ercCPDefinitionVisibilityRel =
				fetchByERC_C(
					cpDefinitionVisibilityRel.getExternalReferenceCode(),
					cpDefinitionVisibilityRel.getCompanyId());

			if (isNew) {
				if (ercCPDefinitionVisibilityRel != null) {
					throw new DuplicateCPDefinitionVisibilityRelExternalReferenceCodeException(
						"Duplicate cp definition visibility rel with external reference code " +
							cpDefinitionVisibilityRel.
								getExternalReferenceCode() + " and company " +
									cpDefinitionVisibilityRel.getCompanyId());
				}
			}
			else {
				if ((ercCPDefinitionVisibilityRel != null) &&
					(cpDefinitionVisibilityRel.
						getCPDefinitionVisibilityRelId() !=
							ercCPDefinitionVisibilityRel.
								getCPDefinitionVisibilityRelId())) {

					throw new DuplicateCPDefinitionVisibilityRelExternalReferenceCodeException(
						"Duplicate cp definition visibility rel with external reference code " +
							cpDefinitionVisibilityRel.
								getExternalReferenceCode() + " and company " +
									cpDefinitionVisibilityRel.getCompanyId());
				}
			}
		}

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		Date date = new Date();

		if (isNew && (cpDefinitionVisibilityRel.getCreateDate() == null)) {
			if (serviceContext == null) {
				cpDefinitionVisibilityRel.setCreateDate(date);
			}
			else {
				cpDefinitionVisibilityRel.setCreateDate(
					serviceContext.getCreateDate(date));
			}
		}

		if (!cpDefinitionVisibilityRelModelImpl.hasSetModifiedDate()) {
			if (serviceContext == null) {
				cpDefinitionVisibilityRel.setModifiedDate(date);
			}
			else {
				cpDefinitionVisibilityRel.setModifiedDate(
					serviceContext.getModifiedDate(date));
			}
		}

		Session session = null;

		try {
			session = openSession();

			if (ctPersistenceHelper.isInsert(cpDefinitionVisibilityRel)) {
				if (!isNew) {
					session.evict(
						CPDefinitionVisibilityRelImpl.class,
						cpDefinitionVisibilityRel.getPrimaryKeyObj());
				}

				session.save(cpDefinitionVisibilityRel);
			}
			else {
				cpDefinitionVisibilityRel =
					(CPDefinitionVisibilityRel)session.merge(
						cpDefinitionVisibilityRel);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		cacheUniqueFindersResult(cpDefinitionVisibilityRel, false);

		if (isNew) {
			cpDefinitionVisibilityRel.setNew(false);
		}

		cpDefinitionVisibilityRel.resetOriginalValues();

		return cpDefinitionVisibilityRel;
	}

	/**
	 * Returns the cp definition visibility rel with the primary key or throws a <code>NoSuchCPDefinitionVisibilityRelException</code> if it could not be found.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a cp definition visibility rel with the primary key could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel findByPrimaryKey(
			long CPDefinitionVisibilityRelId)
		throws NoSuchCPDefinitionVisibilityRelException {

		return findByPrimaryKey((Serializable)CPDefinitionVisibilityRelId);
	}

	@Override
	protected CTPersistenceHelper getCTPersistenceHelper() {
		return ctPersistenceHelper;
	}

	/**
	 * Returns the cp definition visibility rel with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel, or <code>null</code> if a cp definition visibility rel with the primary key could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel fetchByPrimaryKey(
		long CPDefinitionVisibilityRelId) {

		return fetchByPrimaryKey((Serializable)CPDefinitionVisibilityRelId);
	}

	@Override
	protected EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	protected String getPKDBName() {
		return "CPDefinitionVisibilityRelId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_CPDEFINITIONVISIBILITYREL;
	}

	@Override
	public Set<String> getCTColumnNames(
		CTColumnResolutionType ctColumnResolutionType) {

		return _ctColumnNamesMap.getOrDefault(
			ctColumnResolutionType, Collections.emptySet());
	}

	@Override
	public List<String> getMappingTableNames() {
		return _mappingTableNames;
	}

	@Override
	public Map<String, Integer> getTableColumnsMap() {
		return CPDefinitionVisibilityRelModelImpl.TABLE_COLUMNS_MAP;
	}

	@Override
	public String getTableName() {
		return "CPDefinitionVisibilityRel";
	}

	@Override
	public List<String[]> getUniqueIndexColumnNames() {
		return _uniqueIndexColumnNames;
	}

	private static final Map<CTColumnResolutionType, Set<String>>
		_ctColumnNamesMap = new EnumMap<CTColumnResolutionType, Set<String>>(
			CTColumnResolutionType.class);
	private static final List<String> _mappingTableNames =
		new ArrayList<String>();
	private static final List<String[]> _uniqueIndexColumnNames =
		new ArrayList<String[]>();

	static {
		Set<String> ctControlColumnNames = new HashSet<String>();
		Set<String> ctIgnoreColumnNames = new HashSet<String>();
		Set<String> ctMergeColumnNames = new HashSet<String>();
		Set<String> ctStrictColumnNames = new HashSet<String>();

		ctControlColumnNames.add("mvccVersion");
		ctControlColumnNames.add("ctCollectionId");
		ctStrictColumnNames.add("externalReferenceCode");
		ctStrictColumnNames.add("companyId");
		ctStrictColumnNames.add("userId");
		ctStrictColumnNames.add("userName");
		ctStrictColumnNames.add("createDate");
		ctIgnoreColumnNames.add("modifiedDate");
		ctMergeColumnNames.add("CPDefinitionId");
		ctStrictColumnNames.add("classNameId");
		ctStrictColumnNames.add("classPK");

		_ctColumnNamesMap.put(
			CTColumnResolutionType.CONTROL, ctControlColumnNames);
		_ctColumnNamesMap.put(
			CTColumnResolutionType.IGNORE, ctIgnoreColumnNames);
		_ctColumnNamesMap.put(CTColumnResolutionType.MERGE, ctMergeColumnNames);
		_ctColumnNamesMap.put(
			CTColumnResolutionType.PK,
			Collections.singleton("CPDefinitionVisibilityRelId"));
		_ctColumnNamesMap.put(
			CTColumnResolutionType.STRICT, ctStrictColumnNames);

		_uniqueIndexColumnNames.add(
			new String[] {"CPDefinitionId", "classNameId", "classPK"});

		_uniqueIndexColumnNames.add(
			new String[] {"externalReferenceCode", "companyId"});
	}

	/**
	 * Initializes the cp definition visibility rel persistence.
	 */
	@Activate
	public void activate() {
		_collectionPersistenceFinderByCPDefinitionId =
			new CollectionPersistenceFinder<>(
				this,
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
					"findByCPDefinitionId",
					new String[] {
						Long.class.getName(), Integer.class.getName(),
						Integer.class.getName(),
						OrderByComparator.class.getName()
					},
					new String[] {"CPDefinitionId"}, true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
					"findByCPDefinitionId", new String[] {Long.class.getName()},
					new String[] {"CPDefinitionId"}, true),
				new FinderPath(
					FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
					"countByCPDefinitionId",
					new String[] {Long.class.getName()},
					new String[] {"CPDefinitionId"}, false),
				_SQL_SELECT_CPDEFINITIONVISIBILITYREL_WHERE,
				_SQL_COUNT_CPDEFINITIONVISIBILITYREL_WHERE,
				CPDefinitionVisibilityRelModelImpl.ORDER_BY_JPQL,
				_ENTITY_ALIAS_PREFIX, "", "", null,
				new FinderColumn<>(
					"cpDefinitionVisibilityRel.", "CPDefinitionId",
					FinderColumn.Type.LONG, "=", true, true,
					CPDefinitionVisibilityRel::getCPDefinitionId));

		_collectionPersistenceFinderByCPD_C = new CollectionPersistenceFinder<>(
			this,
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByCPD_C",
				new String[] {
					Long.class.getName(), Long.class.getName(),
					Integer.class.getName(), Integer.class.getName(),
					OrderByComparator.class.getName()
				},
				new String[] {"CPDefinitionId", "classNameId"}, true),
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByCPD_C",
				new String[] {Long.class.getName(), Long.class.getName()},
				new String[] {"CPDefinitionId", "classNameId"}, true),
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByCPD_C",
				new String[] {Long.class.getName(), Long.class.getName()},
				new String[] {"CPDefinitionId", "classNameId"}, false),
			_SQL_SELECT_CPDEFINITIONVISIBILITYREL_WHERE,
			_SQL_COUNT_CPDEFINITIONVISIBILITYREL_WHERE,
			CPDefinitionVisibilityRelModelImpl.ORDER_BY_JPQL,
			_ENTITY_ALIAS_PREFIX, "", "", null,
			new FinderColumn<>(
				"cpDefinitionVisibilityRel.", "CPDefinitionId",
				FinderColumn.Type.LONG, "=", true, true,
				CPDefinitionVisibilityRel::getCPDefinitionId),
			new FinderColumn<>(
				"cpDefinitionVisibilityRel.", "classNameId",
				FinderColumn.Type.LONG, "=", true, true,
				CPDefinitionVisibilityRel::getClassNameId));

		_collectionPersistenceFinderByC_C = new CollectionPersistenceFinder<>(
			this,
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByC_C",
				new String[] {
					Long.class.getName(), Long.class.getName(),
					Integer.class.getName(), Integer.class.getName(),
					OrderByComparator.class.getName()
				},
				new String[] {"classNameId", "classPK"}, true),
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByC_C",
				new String[] {Long.class.getName(), Long.class.getName()},
				new String[] {"classNameId", "classPK"}, true),
			new FinderPath(
				FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByC_C",
				new String[] {Long.class.getName(), Long.class.getName()},
				new String[] {"classNameId", "classPK"}, false),
			_SQL_SELECT_CPDEFINITIONVISIBILITYREL_WHERE,
			_SQL_COUNT_CPDEFINITIONVISIBILITYREL_WHERE,
			CPDefinitionVisibilityRelModelImpl.ORDER_BY_JPQL,
			_ENTITY_ALIAS_PREFIX, "", "", null,
			new FinderColumn<>(
				"cpDefinitionVisibilityRel.", "classNameId",
				FinderColumn.Type.LONG, "=", true, true,
				CPDefinitionVisibilityRel::getClassNameId),
			new FinderColumn<>(
				"cpDefinitionVisibilityRel.", "classPK", FinderColumn.Type.LONG,
				"=", true, true, CPDefinitionVisibilityRel::getClassPK));

		_uniquePersistenceFinderByCPD_C_C = new UniquePersistenceFinder<>(
			this,
			createUniqueFinderPath(
				FINDER_CLASS_NAME_ENTITY, "fetchByCPD_C_C",
				new String[] {
					Long.class.getName(), Long.class.getName(),
					Long.class.getName()
				},
				new String[] {"CPDefinitionId", "classNameId", "classPK"}, 0, 0,
				false, CPDefinitionVisibilityRel::getCPDefinitionId,
				CPDefinitionVisibilityRel::getClassNameId,
				CPDefinitionVisibilityRel::getClassPK),
			_SQL_SELECT_CPDEFINITIONVISIBILITYREL_WHERE, "",
			new FinderColumn<>(
				"cpDefinitionVisibilityRel.", "CPDefinitionId",
				FinderColumn.Type.LONG, "=", true, true,
				CPDefinitionVisibilityRel::getCPDefinitionId),
			new FinderColumn<>(
				"cpDefinitionVisibilityRel.", "classNameId",
				FinderColumn.Type.LONG, "=", true, true,
				CPDefinitionVisibilityRel::getClassNameId),
			new FinderColumn<>(
				"cpDefinitionVisibilityRel.", "classPK", FinderColumn.Type.LONG,
				"=", true, true, CPDefinitionVisibilityRel::getClassPK));

		_uniquePersistenceFinderByERC_C = new UniquePersistenceFinder<>(
			this,
			createUniqueFinderPath(
				FINDER_CLASS_NAME_ENTITY, "fetchByERC_C",
				new String[] {String.class.getName(), Long.class.getName()},
				new String[] {"externalReferenceCode", "companyId"}, 0, 1,
				false,
				convertNullFunction(
					CPDefinitionVisibilityRel::getExternalReferenceCode),
				CPDefinitionVisibilityRel::getCompanyId),
			_SQL_SELECT_CPDEFINITIONVISIBILITYREL_WHERE, "",
			new FinderColumn<>(
				"cpDefinitionVisibilityRel.", "externalReferenceCode",
				FinderColumn.Type.STRING, "=", true, true,
				CPDefinitionVisibilityRel::getExternalReferenceCode),
			new FinderColumn<>(
				"cpDefinitionVisibilityRel.", "companyId",
				FinderColumn.Type.LONG, "=", true, true,
				CPDefinitionVisibilityRel::getCompanyId));

		CPDefinitionVisibilityRelUtil.setPersistence(this);
	}

	@Deactivate
	public void deactivate() {
		CPDefinitionVisibilityRelUtil.setPersistence(null);

		entityCache.removeCache(CPDefinitionVisibilityRelImpl.class.getName());
	}

	@Override
	@Reference(
		target = CommercePersistenceConstants.SERVICE_CONFIGURATION_FILTER,
		unbind = "-"
	)
	public void setConfiguration(Configuration configuration) {
	}

	@Override
	@Reference(
		target = CommercePersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	@Reference(
		target = CommercePersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@Reference
	protected CTPersistenceHelper ctPersistenceHelper;

	@Reference
	protected EntityCache entityCache;

	@Reference
	protected FinderCache finderCache;

	private static final String _ENTITY_ALIAS_PREFIX =
		CPDefinitionVisibilityRelModelImpl.ENTITY_ALIAS + ".";

	private static final String _SQL_SELECT_CPDEFINITIONVISIBILITYREL =
		"SELECT cpDefinitionVisibilityRel FROM CPDefinitionVisibilityRel cpDefinitionVisibilityRel";

	private static final String _SQL_SELECT_CPDEFINITIONVISIBILITYREL_WHERE =
		"SELECT cpDefinitionVisibilityRel FROM CPDefinitionVisibilityRel cpDefinitionVisibilityRel WHERE ";

	private static final String _SQL_COUNT_CPDEFINITIONVISIBILITYREL_WHERE =
		"SELECT COUNT(cpDefinitionVisibilityRel) FROM CPDefinitionVisibilityRel cpDefinitionVisibilityRel WHERE ";

	@Override
	protected FinderCache getFinderCache() {
		return finderCache;
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:-1861875721