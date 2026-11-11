/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.persistence;

import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistence utility for the cp definition visibility rel service. This utility wraps <code>com.liferay.commerce.product.service.persistence.impl.CPDefinitionVisibilityRelPersistenceImpl</code> and provides direct access to the database for CRUD operations. This utility should only be used by the service layer, as it must operate within a transaction. Never access this utility in a JSP, controller, model, or other front-end class.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Marco Leo
 * @see CPDefinitionVisibilityRelPersistence
 * @generated
 */
public class CPDefinitionVisibilityRelUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#cacheResult(List)
	 */
	public static void cacheResult(
		List<CPDefinitionVisibilityRel> cpDefinitionVisibilityRels) {

		getPersistence().cacheResult(cpDefinitionVisibilityRels);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#cacheResult(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static void cacheResult(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		getPersistence().cacheResult(cpDefinitionVisibilityRel);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache()
	 */
	public static void clearCache() {
		getPersistence().clearCache();
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static void clearCache(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		getPersistence().clearCache(cpDefinitionVisibilityRel);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#countWithDynamicQuery(DynamicQuery)
	 */
	public static long countWithDynamicQuery(DynamicQuery dynamicQuery) {
		return getPersistence().countWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#fetchByPrimaryKeys(Set)
	 */
	public static Map<Serializable, CPDefinitionVisibilityRel>
		fetchByPrimaryKeys(Set<Serializable> primaryKeys) {

		return getPersistence().fetchByPrimaryKeys(primaryKeys);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery)
	 */
	public static List<CPDefinitionVisibilityRel> findWithDynamicQuery(
		DynamicQuery dynamicQuery) {

		return getPersistence().findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int)
	 */
	public static List<CPDefinitionVisibilityRel> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getPersistence().findWithDynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int, OrderByComparator)
	 */
	public static List<CPDefinitionVisibilityRel> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return getPersistence().findWithDynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static CPDefinitionVisibilityRel update(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		return getPersistence().update(cpDefinitionVisibilityRel);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel, ServiceContext)
	 */
	public static CPDefinitionVisibilityRel update(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel,
		ServiceContext serviceContext) {

		return getPersistence().update(
			cpDefinitionVisibilityRel, serviceContext);
	}

	/**
	 * Returns an ordered range of all the cp definition visibility rels where CPDefinitionId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByCPDefinitionId(
		long CPDefinitionId, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByCPDefinitionId(
			CPDefinitionId, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	public static CPDefinitionVisibilityRel findByCPDefinitionId_First(
			long CPDefinitionId,
			OrderByComparator<CPDefinitionVisibilityRel> orderByComparator)
		throws com.liferay.commerce.product.exception.
			NoSuchCPDefinitionVisibilityRelException {

		return getPersistence().findByCPDefinitionId_First(
			CPDefinitionId, orderByComparator);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public static CPDefinitionVisibilityRel fetchByCPDefinitionId_First(
		long CPDefinitionId,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return getPersistence().fetchByCPDefinitionId_First(
			CPDefinitionId, orderByComparator);
	}

	/**
	 * Removes all the cp definition visibility rels where CPDefinitionId = &#63; from the database.
	 *
	 * @param CPDefinitionId the cp definition ID
	 */
	public static void removeByCPDefinitionId(long CPDefinitionId) {
		getPersistence().removeByCPDefinitionId(CPDefinitionId);
	}

	/**
	 * Returns the number of cp definition visibility rels where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @return the number of matching cp definition visibility rels
	 */
	public static int countByCPDefinitionId(long CPDefinitionId) {
		return getPersistence().countByCPDefinitionId(CPDefinitionId);
	}

	/**
	 * Returns an ordered range of all the cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
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
	public static List<CPDefinitionVisibilityRel> findByCPD_C(
		long CPDefinitionId, long classNameId, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByCPD_C(
			CPDefinitionId, classNameId, start, end, orderByComparator,
			useFinderCache);
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
	public static CPDefinitionVisibilityRel findByCPD_C_First(
			long CPDefinitionId, long classNameId,
			OrderByComparator<CPDefinitionVisibilityRel> orderByComparator)
		throws com.liferay.commerce.product.exception.
			NoSuchCPDefinitionVisibilityRelException {

		return getPersistence().findByCPD_C_First(
			CPDefinitionId, classNameId, orderByComparator);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public static CPDefinitionVisibilityRel fetchByCPD_C_First(
		long CPDefinitionId, long classNameId,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return getPersistence().fetchByCPD_C_First(
			CPDefinitionId, classNameId, orderByComparator);
	}

	/**
	 * Removes all the cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63; from the database.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 */
	public static void removeByCPD_C(long CPDefinitionId, long classNameId) {
		getPersistence().removeByCPD_C(CPDefinitionId, classNameId);
	}

	/**
	 * Returns the number of cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @return the number of matching cp definition visibility rels
	 */
	public static int countByCPD_C(long CPDefinitionId, long classNameId) {
		return getPersistence().countByCPD_C(CPDefinitionId, classNameId);
	}

	/**
	 * Returns an ordered range of all the cp definition visibility rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
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
	public static List<CPDefinitionVisibilityRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByC_C(
			classNameId, classPK, start, end, orderByComparator,
			useFinderCache);
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
	public static CPDefinitionVisibilityRel findByC_C_First(
			long classNameId, long classPK,
			OrderByComparator<CPDefinitionVisibilityRel> orderByComparator)
		throws com.liferay.commerce.product.exception.
			NoSuchCPDefinitionVisibilityRelException {

		return getPersistence().findByC_C_First(
			classNameId, classPK, orderByComparator);
	}

	/**
	 * Returns the first cp definition visibility rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public static CPDefinitionVisibilityRel fetchByC_C_First(
		long classNameId, long classPK,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return getPersistence().fetchByC_C_First(
			classNameId, classPK, orderByComparator);
	}

	/**
	 * Removes all the cp definition visibility rels where classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 */
	public static void removeByC_C(long classNameId, long classPK) {
		getPersistence().removeByC_C(classNameId, classPK);
	}

	/**
	 * Returns the number of cp definition visibility rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching cp definition visibility rels
	 */
	public static int countByC_C(long classNameId, long classPK) {
		return getPersistence().countByC_C(classNameId, classPK);
	}

	/**
	 * Returns the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; or throws a <code>NoSuchCPDefinitionVisibilityRelException</code> if it could not be found.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	public static CPDefinitionVisibilityRel findByCPD_C_C(
			long CPDefinitionId, long classNameId, long classPK)
		throws com.liferay.commerce.product.exception.
			NoSuchCPDefinitionVisibilityRelException {

		return getPersistence().findByCPD_C_C(
			CPDefinitionId, classNameId, classPK);
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
	public static CPDefinitionVisibilityRel fetchByCPD_C_C(
		long CPDefinitionId, long classNameId, long classPK,
		boolean useFinderCache) {

		return getPersistence().fetchByCPD_C_C(
			CPDefinitionId, classNameId, classPK, useFinderCache);
	}

	/**
	 * Removes the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the cp definition visibility rel that was removed
	 */
	public static CPDefinitionVisibilityRel removeByCPD_C_C(
			long CPDefinitionId, long classNameId, long classPK)
		throws com.liferay.commerce.product.exception.
			NoSuchCPDefinitionVisibilityRelException {

		return getPersistence().removeByCPD_C_C(
			CPDefinitionId, classNameId, classPK);
	}

	/**
	 * Returns the number of cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching cp definition visibility rels
	 */
	public static int countByCPD_C_C(
		long CPDefinitionId, long classNameId, long classPK) {

		return getPersistence().countByCPD_C_C(
			CPDefinitionId, classNameId, classPK);
	}

	/**
	 * Returns the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; or throws a <code>NoSuchCPDefinitionVisibilityRelException</code> if it could not be found.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	public static CPDefinitionVisibilityRel findByERC_C(
			String externalReferenceCode, long companyId)
		throws com.liferay.commerce.product.exception.
			NoSuchCPDefinitionVisibilityRelException {

		return getPersistence().findByERC_C(externalReferenceCode, companyId);
	}

	/**
	 * Returns the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public static CPDefinitionVisibilityRel fetchByERC_C(
		String externalReferenceCode, long companyId, boolean useFinderCache) {

		return getPersistence().fetchByERC_C(
			externalReferenceCode, companyId, useFinderCache);
	}

	/**
	 * Removes the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; from the database.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the cp definition visibility rel that was removed
	 */
	public static CPDefinitionVisibilityRel removeByERC_C(
			String externalReferenceCode, long companyId)
		throws com.liferay.commerce.product.exception.
			NoSuchCPDefinitionVisibilityRelException {

		return getPersistence().removeByERC_C(externalReferenceCode, companyId);
	}

	/**
	 * Returns the number of cp definition visibility rels where externalReferenceCode = &#63; and companyId = &#63;.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the number of matching cp definition visibility rels
	 */
	public static int countByERC_C(
		String externalReferenceCode, long companyId) {

		return getPersistence().countByERC_C(externalReferenceCode, companyId);
	}

	/**
	 * Creates a new cp definition visibility rel with the primary key. Does not add the cp definition visibility rel to the database.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key for the new cp definition visibility rel
	 * @return the new cp definition visibility rel
	 */
	public static CPDefinitionVisibilityRel create(
		long CPDefinitionVisibilityRelId) {

		return getPersistence().create(CPDefinitionVisibilityRelId);
	}

	/**
	 * Removes the cp definition visibility rel with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel that was removed
	 * @throws NoSuchCPDefinitionVisibilityRelException if a cp definition visibility rel with the primary key could not be found
	 */
	public static CPDefinitionVisibilityRel remove(
			long CPDefinitionVisibilityRelId)
		throws com.liferay.commerce.product.exception.
			NoSuchCPDefinitionVisibilityRelException {

		return getPersistence().remove(CPDefinitionVisibilityRelId);
	}

	public static CPDefinitionVisibilityRel updateImpl(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		return getPersistence().updateImpl(cpDefinitionVisibilityRel);
	}

	/**
	 * Returns the cp definition visibility rel with the primary key or throws a <code>NoSuchCPDefinitionVisibilityRelException</code> if it could not be found.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a cp definition visibility rel with the primary key could not be found
	 */
	public static CPDefinitionVisibilityRel findByPrimaryKey(
			long CPDefinitionVisibilityRelId)
		throws com.liferay.commerce.product.exception.
			NoSuchCPDefinitionVisibilityRelException {

		return getPersistence().findByPrimaryKey(CPDefinitionVisibilityRelId);
	}

	/**
	 * Returns the cp definition visibility rel with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel, or <code>null</code> if a cp definition visibility rel with the primary key could not be found
	 */
	public static CPDefinitionVisibilityRel fetchByPrimaryKey(
		long CPDefinitionVisibilityRelId) {

		return getPersistence().fetchByPrimaryKey(CPDefinitionVisibilityRelId);
	}

	/**
	 * Returns the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public static CPDefinitionVisibilityRel fetchByCPD_C_C(
		long CPDefinitionId, long classNameId, long classPK) {

		return getPersistence().fetchByCPD_C_C(
			CPDefinitionId, classNameId, classPK);
	}

	/**
	 * Returns the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public static CPDefinitionVisibilityRel fetchByERC_C(
		String externalReferenceCode, long companyId) {

		return getPersistence().fetchByERC_C(externalReferenceCode, companyId);
	}

	/**
	 * Returns all the cp definition visibility rels where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @return the matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByCPDefinitionId(
		long CPDefinitionId) {

		return getPersistence().findByCPDefinitionId(CPDefinitionId);
	}

	/**
	 * Returns a range of all the cp definition visibility rels where CPDefinitionId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @return the range of matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByCPDefinitionId(
		long CPDefinitionId, int start, int end) {

		return getPersistence().findByCPDefinitionId(
			CPDefinitionId, start, end);
	}

	/**
	 * Returns an ordered range of all the cp definition visibility rels where CPDefinitionId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByCPDefinitionId(
		long CPDefinitionId, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return getPersistence().findByCPDefinitionId(
			CPDefinitionId, start, end, orderByComparator);
	}

	/**
	 * Returns all the cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @return the matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByCPD_C(
		long CPDefinitionId, long classNameId) {

		return getPersistence().findByCPD_C(CPDefinitionId, classNameId);
	}

	/**
	 * Returns a range of all the cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @return the range of matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByCPD_C(
		long CPDefinitionId, long classNameId, int start, int end) {

		return getPersistence().findByCPD_C(
			CPDefinitionId, classNameId, start, end);
	}

	/**
	 * Returns an ordered range of all the cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByCPD_C(
		long CPDefinitionId, long classNameId, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return getPersistence().findByCPD_C(
			CPDefinitionId, classNameId, start, end, orderByComparator);
	}

	/**
	 * Returns all the cp definition visibility rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByC_C(
		long classNameId, long classPK) {

		return getPersistence().findByC_C(classNameId, classPK);
	}

	/**
	 * Returns a range of all the cp definition visibility rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @return the range of matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByC_C(
		long classNameId, long classPK, int start, int end) {

		return getPersistence().findByC_C(classNameId, classPK, start, end);
	}

	/**
	 * Returns an ordered range of all the cp definition visibility rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return getPersistence().findByC_C(
			classNameId, classPK, start, end, orderByComparator);
	}

	public static CPDefinitionVisibilityRelPersistence getPersistence() {
		return _persistence;
	}

	public static void setPersistence(
		CPDefinitionVisibilityRelPersistence persistence) {

		_persistence = persistence;
	}

	private static volatile CPDefinitionVisibilityRelPersistence _persistence;

}
// LIFERAY-SERVICE-BUILDER-HASH:-1823176244