/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.persistence;

import com.liferay.commerce.product.exception.NoSuchCPDefinitionVisibilityRelException;
import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.portal.kernel.service.persistence.BasePersistence;
import com.liferay.portal.kernel.service.persistence.change.tracking.CTPersistence;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the cp definition visibility rel service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Marco Leo
 * @see CPDefinitionVisibilityRelUtil
 * @generated
 */
@ProviderType
public interface CPDefinitionVisibilityRelPersistence
	extends BasePersistence<CPDefinitionVisibilityRel>,
			CTPersistence<CPDefinitionVisibilityRel> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link CPDefinitionVisibilityRelUtil} to access the cp definition visibility rel persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

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
	public java.util.List<CPDefinitionVisibilityRel> findByCPDefinitionId(
		long CPDefinitionId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CPDefinitionVisibilityRel> orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel findByCPDefinitionId_First(
			long CPDefinitionId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CPDefinitionVisibilityRel> orderByComparator)
		throws NoSuchCPDefinitionVisibilityRelException;

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel fetchByCPDefinitionId_First(
		long CPDefinitionId,
		com.liferay.portal.kernel.util.OrderByComparator
			<CPDefinitionVisibilityRel> orderByComparator);

	/**
	 * Removes all the cp definition visibility rels where CPDefinitionId = &#63; from the database.
	 *
	 * @param CPDefinitionId the cp definition ID
	 */
	public void removeByCPDefinitionId(long CPDefinitionId);

	/**
	 * Returns the number of cp definition visibility rels where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @return the number of matching cp definition visibility rels
	 */
	public int countByCPDefinitionId(long CPDefinitionId);

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
	public java.util.List<CPDefinitionVisibilityRel> findByCPD_C(
		long CPDefinitionId, long classNameId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CPDefinitionVisibilityRel> orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel findByCPD_C_First(
			long CPDefinitionId, long classNameId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CPDefinitionVisibilityRel> orderByComparator)
		throws NoSuchCPDefinitionVisibilityRelException;

	/**
	 * Returns the first cp definition visibility rel in the ordered set where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel fetchByCPD_C_First(
		long CPDefinitionId, long classNameId,
		com.liferay.portal.kernel.util.OrderByComparator
			<CPDefinitionVisibilityRel> orderByComparator);

	/**
	 * Removes all the cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63; from the database.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 */
	public void removeByCPD_C(long CPDefinitionId, long classNameId);

	/**
	 * Returns the number of cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @return the number of matching cp definition visibility rels
	 */
	public int countByCPD_C(long CPDefinitionId, long classNameId);

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
	public java.util.List<CPDefinitionVisibilityRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CPDefinitionVisibilityRel> orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first cp definition visibility rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel findByC_C_First(
			long classNameId, long classPK,
			com.liferay.portal.kernel.util.OrderByComparator
				<CPDefinitionVisibilityRel> orderByComparator)
		throws NoSuchCPDefinitionVisibilityRelException;

	/**
	 * Returns the first cp definition visibility rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel fetchByC_C_First(
		long classNameId, long classPK,
		com.liferay.portal.kernel.util.OrderByComparator
			<CPDefinitionVisibilityRel> orderByComparator);

	/**
	 * Removes all the cp definition visibility rels where classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 */
	public void removeByC_C(long classNameId, long classPK);

	/**
	 * Returns the number of cp definition visibility rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching cp definition visibility rels
	 */
	public int countByC_C(long classNameId, long classPK);

	/**
	 * Returns the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; or throws a <code>NoSuchCPDefinitionVisibilityRelException</code> if it could not be found.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel findByCPD_C_C(
			long CPDefinitionId, long classNameId, long classPK)
		throws NoSuchCPDefinitionVisibilityRelException;

	/**
	 * Returns the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel fetchByCPD_C_C(
		long CPDefinitionId, long classNameId, long classPK,
		boolean useFinderCache);

	/**
	 * Removes the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the cp definition visibility rel that was removed
	 */
	public CPDefinitionVisibilityRel removeByCPD_C_C(
			long CPDefinitionId, long classNameId, long classPK)
		throws NoSuchCPDefinitionVisibilityRelException;

	/**
	 * Returns the number of cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching cp definition visibility rels
	 */
	public int countByCPD_C_C(
		long CPDefinitionId, long classNameId, long classPK);

	/**
	 * Returns the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; or throws a <code>NoSuchCPDefinitionVisibilityRelException</code> if it could not be found.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the matching cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel findByERC_C(
			String externalReferenceCode, long companyId)
		throws NoSuchCPDefinitionVisibilityRelException;

	/**
	 * Returns the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public CPDefinitionVisibilityRel fetchByERC_C(
		String externalReferenceCode, long companyId, boolean useFinderCache);

	/**
	 * Removes the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; from the database.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the cp definition visibility rel that was removed
	 */
	public CPDefinitionVisibilityRel removeByERC_C(
			String externalReferenceCode, long companyId)
		throws NoSuchCPDefinitionVisibilityRelException;

	/**
	 * Returns the number of cp definition visibility rels where externalReferenceCode = &#63; and companyId = &#63;.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the number of matching cp definition visibility rels
	 */
	public int countByERC_C(String externalReferenceCode, long companyId);

	/**
	 * Creates a new cp definition visibility rel with the primary key. Does not add the cp definition visibility rel to the database.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key for the new cp definition visibility rel
	 * @return the new cp definition visibility rel
	 */
	public CPDefinitionVisibilityRel create(long CPDefinitionVisibilityRelId);

	/**
	 * Removes the cp definition visibility rel with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel that was removed
	 * @throws NoSuchCPDefinitionVisibilityRelException if a cp definition visibility rel with the primary key could not be found
	 */
	public CPDefinitionVisibilityRel remove(long CPDefinitionVisibilityRelId)
		throws NoSuchCPDefinitionVisibilityRelException;

	public CPDefinitionVisibilityRel updateImpl(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel);

	/**
	 * Returns the cp definition visibility rel with the primary key or throws a <code>NoSuchCPDefinitionVisibilityRelException</code> if it could not be found.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel
	 * @throws NoSuchCPDefinitionVisibilityRelException if a cp definition visibility rel with the primary key could not be found
	 */
	public CPDefinitionVisibilityRel findByPrimaryKey(
			long CPDefinitionVisibilityRelId)
		throws NoSuchCPDefinitionVisibilityRelException;

	/**
	 * Returns the cp definition visibility rel with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel, or <code>null</code> if a cp definition visibility rel with the primary key could not be found
	 */
	public CPDefinitionVisibilityRel fetchByPrimaryKey(
		long CPDefinitionVisibilityRelId);

	/**
	 * Returns the cp definition visibility rel where CPDefinitionId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public default CPDefinitionVisibilityRel fetchByCPD_C_C(
		long CPDefinitionId, long classNameId, long classPK) {

		return fetchByCPD_C_C(CPDefinitionId, classNameId, classPK, true);
	}

	/**
	 * Returns the cp definition visibility rel where externalReferenceCode = &#63; and companyId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param externalReferenceCode the external reference code
	 * @param companyId the company ID
	 * @return the matching cp definition visibility rel, or <code>null</code> if a matching cp definition visibility rel could not be found
	 */
	public default CPDefinitionVisibilityRel fetchByERC_C(
		String externalReferenceCode, long companyId) {

		return fetchByERC_C(externalReferenceCode, companyId, true);
	}

	/**
	 * Returns all the cp definition visibility rels where CPDefinitionId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @return the matching cp definition visibility rels
	 */
	public default java.util.List<CPDefinitionVisibilityRel>
		findByCPDefinitionId(long CPDefinitionId) {

		return findByCPDefinitionId(
			CPDefinitionId, com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS,
			com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS, null, true);
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
	public default java.util.List<CPDefinitionVisibilityRel>
		findByCPDefinitionId(long CPDefinitionId, int start, int end) {

		return findByCPDefinitionId(CPDefinitionId, start, end, null, true);
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
	public default java.util.List<CPDefinitionVisibilityRel>
		findByCPDefinitionId(
			long CPDefinitionId, int start, int end,
			com.liferay.portal.kernel.util.OrderByComparator
				<CPDefinitionVisibilityRel> orderByComparator) {

		return findByCPDefinitionId(
			CPDefinitionId, start, end, orderByComparator, true);
	}

	/**
	 * Returns all the cp definition visibility rels where CPDefinitionId = &#63; and classNameId = &#63;.
	 *
	 * @param CPDefinitionId the cp definition ID
	 * @param classNameId the class name ID
	 * @return the matching cp definition visibility rels
	 */
	public default java.util.List<CPDefinitionVisibilityRel> findByCPD_C(
		long CPDefinitionId, long classNameId) {

		return findByCPD_C(
			CPDefinitionId, classNameId,
			com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS,
			com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS, null, true);
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
	public default java.util.List<CPDefinitionVisibilityRel> findByCPD_C(
		long CPDefinitionId, long classNameId, int start, int end) {

		return findByCPD_C(CPDefinitionId, classNameId, start, end, null, true);
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
	public default java.util.List<CPDefinitionVisibilityRel> findByCPD_C(
		long CPDefinitionId, long classNameId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CPDefinitionVisibilityRel> orderByComparator) {

		return findByCPD_C(
			CPDefinitionId, classNameId, start, end, orderByComparator, true);
	}

	/**
	 * Returns all the cp definition visibility rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching cp definition visibility rels
	 */
	public default java.util.List<CPDefinitionVisibilityRel> findByC_C(
		long classNameId, long classPK) {

		return findByC_C(
			classNameId, classPK,
			com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS,
			com.liferay.portal.kernel.dao.orm.QueryUtil.ALL_POS, null, true);
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
	public default java.util.List<CPDefinitionVisibilityRel> findByC_C(
		long classNameId, long classPK, int start, int end) {

		return findByC_C(classNameId, classPK, start, end, null, true);
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
	public default java.util.List<CPDefinitionVisibilityRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CPDefinitionVisibilityRel> orderByComparator) {

		return findByC_C(
			classNameId, classPK, start, end, orderByComparator, true);
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:-1901837606