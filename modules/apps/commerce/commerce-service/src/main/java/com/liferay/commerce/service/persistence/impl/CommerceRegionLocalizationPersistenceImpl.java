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

package com.liferay.commerce.service.persistence.impl;

import com.liferay.commerce.exception.NoSuchRegionLocalizationException;
import com.liferay.commerce.model.CommerceRegionLocalization;
import com.liferay.commerce.model.CommerceRegionLocalizationTable;
import com.liferay.commerce.model.impl.CommerceRegionLocalizationImpl;
import com.liferay.commerce.model.impl.CommerceRegionLocalizationModelImpl;
import com.liferay.commerce.service.persistence.CommerceRegionLocalizationPersistence;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.ArgumentsResolver;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Query;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.spring.extender.service.ServiceReference;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * The persistence implementation for the commerce region localization service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Alessio Antonio Rendina
 * @generated
 */
public class CommerceRegionLocalizationPersistenceImpl
	extends BasePersistenceImpl<CommerceRegionLocalization>
	implements CommerceRegionLocalizationPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>CommerceRegionLocalizationUtil</code> to access the commerce region localization persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		CommerceRegionLocalizationImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private FinderPath _finderPathWithPaginationFindAll;
	private FinderPath _finderPathWithoutPaginationFindAll;
	private FinderPath _finderPathCountAll;
	private FinderPath _finderPathWithPaginationFindByCommerceRegionId;
	private FinderPath _finderPathWithoutPaginationFindByCommerceRegionId;
	private FinderPath _finderPathCountByCommerceRegionId;

	/**
	 * Returns all the commerce region localizations where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @return the matching commerce region localizations
	 */
	@Override
	public List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId) {

		return findByCommerceRegionId(
			commerceRegionId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the commerce region localizations where commerceRegionId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceRegionLocalizationModelImpl</code>.
	 * </p>
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param start the lower bound of the range of commerce region localizations
	 * @param end the upper bound of the range of commerce region localizations (not inclusive)
	 * @return the range of matching commerce region localizations
	 */
	@Override
	public List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId, int start, int end) {

		return findByCommerceRegionId(commerceRegionId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the commerce region localizations where commerceRegionId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceRegionLocalizationModelImpl</code>.
	 * </p>
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param start the lower bound of the range of commerce region localizations
	 * @param end the upper bound of the range of commerce region localizations (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching commerce region localizations
	 */
	@Override
	public List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId, int start, int end,
		OrderByComparator<CommerceRegionLocalization> orderByComparator) {

		return findByCommerceRegionId(
			commerceRegionId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the commerce region localizations where commerceRegionId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceRegionLocalizationModelImpl</code>.
	 * </p>
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param start the lower bound of the range of commerce region localizations
	 * @param end the upper bound of the range of commerce region localizations (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching commerce region localizations
	 */
	@Override
	public List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId, int start, int end,
		OrderByComparator<CommerceRegionLocalization> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByCommerceRegionId;
				finderArgs = new Object[] {commerceRegionId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByCommerceRegionId;
			finderArgs = new Object[] {
				commerceRegionId, start, end, orderByComparator
			};
		}

		List<CommerceRegionLocalization> list = null;

		if (useFinderCache) {
			list = (List<CommerceRegionLocalization>)finderCache.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (CommerceRegionLocalization commerceRegionLocalization :
						list) {

					if (commerceRegionId !=
							commerceRegionLocalization.getCommerceRegionId()) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler sb = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					3 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				sb = new StringBundler(3);
			}

			sb.append(_SQL_SELECT_COMMERCEREGIONLOCALIZATION_WHERE);

			sb.append(_FINDER_COLUMN_COMMERCEREGIONID_COMMERCEREGIONID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(CommerceRegionLocalizationModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(commerceRegionId);

				list = (List<CommerceRegionLocalization>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce region localization
	 * @throws NoSuchRegionLocalizationException if a matching commerce region localization could not be found
	 */
	@Override
	public CommerceRegionLocalization findByCommerceRegionId_First(
			long commerceRegionId,
			OrderByComparator<CommerceRegionLocalization> orderByComparator)
		throws NoSuchRegionLocalizationException {

		CommerceRegionLocalization commerceRegionLocalization =
			fetchByCommerceRegionId_First(commerceRegionId, orderByComparator);

		if (commerceRegionLocalization != null) {
			return commerceRegionLocalization;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("commerceRegionId=");
		sb.append(commerceRegionId);

		sb.append("}");

		throw new NoSuchRegionLocalizationException(sb.toString());
	}

	/**
	 * Returns the first commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	@Override
	public CommerceRegionLocalization fetchByCommerceRegionId_First(
		long commerceRegionId,
		OrderByComparator<CommerceRegionLocalization> orderByComparator) {

		List<CommerceRegionLocalization> list = findByCommerceRegionId(
			commerceRegionId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce region localization
	 * @throws NoSuchRegionLocalizationException if a matching commerce region localization could not be found
	 */
	@Override
	public CommerceRegionLocalization findByCommerceRegionId_Last(
			long commerceRegionId,
			OrderByComparator<CommerceRegionLocalization> orderByComparator)
		throws NoSuchRegionLocalizationException {

		CommerceRegionLocalization commerceRegionLocalization =
			fetchByCommerceRegionId_Last(commerceRegionId, orderByComparator);

		if (commerceRegionLocalization != null) {
			return commerceRegionLocalization;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("commerceRegionId=");
		sb.append(commerceRegionId);

		sb.append("}");

		throw new NoSuchRegionLocalizationException(sb.toString());
	}

	/**
	 * Returns the last commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	@Override
	public CommerceRegionLocalization fetchByCommerceRegionId_Last(
		long commerceRegionId,
		OrderByComparator<CommerceRegionLocalization> orderByComparator) {

		int count = countByCommerceRegionId(commerceRegionId);

		if (count == 0) {
			return null;
		}

		List<CommerceRegionLocalization> list = findByCommerceRegionId(
			commerceRegionId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the commerce region localizations before and after the current commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionLocalizationId the primary key of the current commerce region localization
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next commerce region localization
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	@Override
	public CommerceRegionLocalization[] findByCommerceRegionId_PrevAndNext(
			long commerceRegionLocalizationId, long commerceRegionId,
			OrderByComparator<CommerceRegionLocalization> orderByComparator)
		throws NoSuchRegionLocalizationException {

		CommerceRegionLocalization commerceRegionLocalization =
			findByPrimaryKey(commerceRegionLocalizationId);

		Session session = null;

		try {
			session = openSession();

			CommerceRegionLocalization[] array =
				new CommerceRegionLocalizationImpl[3];

			array[0] = getByCommerceRegionId_PrevAndNext(
				session, commerceRegionLocalization, commerceRegionId,
				orderByComparator, true);

			array[1] = commerceRegionLocalization;

			array[2] = getByCommerceRegionId_PrevAndNext(
				session, commerceRegionLocalization, commerceRegionId,
				orderByComparator, false);

			return array;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected CommerceRegionLocalization getByCommerceRegionId_PrevAndNext(
		Session session, CommerceRegionLocalization commerceRegionLocalization,
		long commerceRegionId,
		OrderByComparator<CommerceRegionLocalization> orderByComparator,
		boolean previous) {

		StringBundler sb = null;

		if (orderByComparator != null) {
			sb = new StringBundler(
				4 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			sb = new StringBundler(3);
		}

		sb.append(_SQL_SELECT_COMMERCEREGIONLOCALIZATION_WHERE);

		sb.append(_FINDER_COLUMN_COMMERCEREGIONID_COMMERCEREGIONID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				sb.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						sb.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN);
					}
					else {
						sb.append(WHERE_LESSER_THAN);
					}
				}
			}

			sb.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						sb.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC);
					}
					else {
						sb.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			sb.append(CommerceRegionLocalizationModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		queryPos.add(commerceRegionId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						commerceRegionLocalization)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<CommerceRegionLocalization> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the commerce region localizations where commerceRegionId = &#63; from the database.
	 *
	 * @param commerceRegionId the commerce region ID
	 */
	@Override
	public void removeByCommerceRegionId(long commerceRegionId) {
		for (CommerceRegionLocalization commerceRegionLocalization :
				findByCommerceRegionId(
					commerceRegionId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null)) {

			remove(commerceRegionLocalization);
		}
	}

	/**
	 * Returns the number of commerce region localizations where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @return the number of matching commerce region localizations
	 */
	@Override
	public int countByCommerceRegionId(long commerceRegionId) {
		FinderPath finderPath = _finderPathCountByCommerceRegionId;

		Object[] finderArgs = new Object[] {commerceRegionId};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs, this);

		if (count == null) {
			StringBundler sb = new StringBundler(2);

			sb.append(_SQL_COUNT_COMMERCEREGIONLOCALIZATION_WHERE);

			sb.append(_FINDER_COLUMN_COMMERCEREGIONID_COMMERCEREGIONID_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(commerceRegionId);

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String
		_FINDER_COLUMN_COMMERCEREGIONID_COMMERCEREGIONID_2 =
			"commerceRegionLocalization.commerceRegionId = ?";

	private FinderPath _finderPathFetchByCommerceRegionId_LanguageId;
	private FinderPath _finderPathCountByCommerceRegionId_LanguageId;

	/**
	 * Returns the commerce region localization where commerceRegionId = &#63; and languageId = &#63; or throws a <code>NoSuchRegionLocalizationException</code> if it could not be found.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the matching commerce region localization
	 * @throws NoSuchRegionLocalizationException if a matching commerce region localization could not be found
	 */
	@Override
	public CommerceRegionLocalization findByCommerceRegionId_LanguageId(
			long commerceRegionId, String languageId)
		throws NoSuchRegionLocalizationException {

		CommerceRegionLocalization commerceRegionLocalization =
			fetchByCommerceRegionId_LanguageId(commerceRegionId, languageId);

		if (commerceRegionLocalization == null) {
			StringBundler sb = new StringBundler(6);

			sb.append(_NO_SUCH_ENTITY_WITH_KEY);

			sb.append("commerceRegionId=");
			sb.append(commerceRegionId);

			sb.append(", languageId=");
			sb.append(languageId);

			sb.append("}");

			if (_log.isDebugEnabled()) {
				_log.debug(sb.toString());
			}

			throw new NoSuchRegionLocalizationException(sb.toString());
		}

		return commerceRegionLocalization;
	}

	/**
	 * Returns the commerce region localization where commerceRegionId = &#63; and languageId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	@Override
	public CommerceRegionLocalization fetchByCommerceRegionId_LanguageId(
		long commerceRegionId, String languageId) {

		return fetchByCommerceRegionId_LanguageId(
			commerceRegionId, languageId, true);
	}

	/**
	 * Returns the commerce region localization where commerceRegionId = &#63; and languageId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	@Override
	public CommerceRegionLocalization fetchByCommerceRegionId_LanguageId(
		long commerceRegionId, String languageId, boolean useFinderCache) {

		languageId = Objects.toString(languageId, "");

		Object[] finderArgs = null;

		if (useFinderCache) {
			finderArgs = new Object[] {commerceRegionId, languageId};
		}

		Object result = null;

		if (useFinderCache) {
			result = finderCache.getResult(
				_finderPathFetchByCommerceRegionId_LanguageId, finderArgs,
				this);
		}

		if (result instanceof CommerceRegionLocalization) {
			CommerceRegionLocalization commerceRegionLocalization =
				(CommerceRegionLocalization)result;

			if ((commerceRegionId !=
					commerceRegionLocalization.getCommerceRegionId()) ||
				!Objects.equals(
					languageId, commerceRegionLocalization.getLanguageId())) {

				result = null;
			}
		}

		if (result == null) {
			StringBundler sb = new StringBundler(4);

			sb.append(_SQL_SELECT_COMMERCEREGIONLOCALIZATION_WHERE);

			sb.append(
				_FINDER_COLUMN_COMMERCEREGIONID_LANGUAGEID_COMMERCEREGIONID_2);

			boolean bindLanguageId = false;

			if (languageId.isEmpty()) {
				sb.append(
					_FINDER_COLUMN_COMMERCEREGIONID_LANGUAGEID_LANGUAGEID_3);
			}
			else {
				bindLanguageId = true;

				sb.append(
					_FINDER_COLUMN_COMMERCEREGIONID_LANGUAGEID_LANGUAGEID_2);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(commerceRegionId);

				if (bindLanguageId) {
					queryPos.add(languageId);
				}

				List<CommerceRegionLocalization> list = query.list();

				if (list.isEmpty()) {
					if (useFinderCache) {
						finderCache.putResult(
							_finderPathFetchByCommerceRegionId_LanguageId,
							finderArgs, list);
					}
				}
				else {
					CommerceRegionLocalization commerceRegionLocalization =
						list.get(0);

					result = commerceRegionLocalization;

					cacheResult(commerceRegionLocalization);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		if (result instanceof List<?>) {
			return null;
		}
		else {
			return (CommerceRegionLocalization)result;
		}
	}

	/**
	 * Removes the commerce region localization where commerceRegionId = &#63; and languageId = &#63; from the database.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the commerce region localization that was removed
	 */
	@Override
	public CommerceRegionLocalization removeByCommerceRegionId_LanguageId(
			long commerceRegionId, String languageId)
		throws NoSuchRegionLocalizationException {

		CommerceRegionLocalization commerceRegionLocalization =
			findByCommerceRegionId_LanguageId(commerceRegionId, languageId);

		return remove(commerceRegionLocalization);
	}

	/**
	 * Returns the number of commerce region localizations where commerceRegionId = &#63; and languageId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the number of matching commerce region localizations
	 */
	@Override
	public int countByCommerceRegionId_LanguageId(
		long commerceRegionId, String languageId) {

		languageId = Objects.toString(languageId, "");

		FinderPath finderPath = _finderPathCountByCommerceRegionId_LanguageId;

		Object[] finderArgs = new Object[] {commerceRegionId, languageId};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs, this);

		if (count == null) {
			StringBundler sb = new StringBundler(3);

			sb.append(_SQL_COUNT_COMMERCEREGIONLOCALIZATION_WHERE);

			sb.append(
				_FINDER_COLUMN_COMMERCEREGIONID_LANGUAGEID_COMMERCEREGIONID_2);

			boolean bindLanguageId = false;

			if (languageId.isEmpty()) {
				sb.append(
					_FINDER_COLUMN_COMMERCEREGIONID_LANGUAGEID_LANGUAGEID_3);
			}
			else {
				bindLanguageId = true;

				sb.append(
					_FINDER_COLUMN_COMMERCEREGIONID_LANGUAGEID_LANGUAGEID_2);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(commerceRegionId);

				if (bindLanguageId) {
					queryPos.add(languageId);
				}

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String
		_FINDER_COLUMN_COMMERCEREGIONID_LANGUAGEID_COMMERCEREGIONID_2 =
			"commerceRegionLocalization.commerceRegionId = ? AND ";

	private static final String
		_FINDER_COLUMN_COMMERCEREGIONID_LANGUAGEID_LANGUAGEID_2 =
			"commerceRegionLocalization.languageId = ?";

	private static final String
		_FINDER_COLUMN_COMMERCEREGIONID_LANGUAGEID_LANGUAGEID_3 =
			"(commerceRegionLocalization.languageId IS NULL OR commerceRegionLocalization.languageId = '')";

	public CommerceRegionLocalizationPersistenceImpl() {
		setModelClass(CommerceRegionLocalization.class);

		setModelImplClass(CommerceRegionLocalizationImpl.class);
		setModelPKClass(long.class);

		setTable(CommerceRegionLocalizationTable.INSTANCE);
	}

	/**
	 * Caches the commerce region localization in the entity cache if it is enabled.
	 *
	 * @param commerceRegionLocalization the commerce region localization
	 */
	@Override
	public void cacheResult(
		CommerceRegionLocalization commerceRegionLocalization) {

		entityCache.putResult(
			CommerceRegionLocalizationImpl.class,
			commerceRegionLocalization.getPrimaryKey(),
			commerceRegionLocalization);

		finderCache.putResult(
			_finderPathFetchByCommerceRegionId_LanguageId,
			new Object[] {
				commerceRegionLocalization.getCommerceRegionId(),
				commerceRegionLocalization.getLanguageId()
			},
			commerceRegionLocalization);
	}

	/**
	 * Caches the commerce region localizations in the entity cache if it is enabled.
	 *
	 * @param commerceRegionLocalizations the commerce region localizations
	 */
	@Override
	public void cacheResult(
		List<CommerceRegionLocalization> commerceRegionLocalizations) {

		for (CommerceRegionLocalization commerceRegionLocalization :
				commerceRegionLocalizations) {

			if (entityCache.getResult(
					CommerceRegionLocalizationImpl.class,
					commerceRegionLocalization.getPrimaryKey()) == null) {

				cacheResult(commerceRegionLocalization);
			}
		}
	}

	/**
	 * Clears the cache for all commerce region localizations.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		entityCache.clearCache(CommerceRegionLocalizationImpl.class);

		finderCache.clearCache(CommerceRegionLocalizationImpl.class);
	}

	/**
	 * Clears the cache for the commerce region localization.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(
		CommerceRegionLocalization commerceRegionLocalization) {

		entityCache.removeResult(
			CommerceRegionLocalizationImpl.class, commerceRegionLocalization);
	}

	@Override
	public void clearCache(
		List<CommerceRegionLocalization> commerceRegionLocalizations) {

		for (CommerceRegionLocalization commerceRegionLocalization :
				commerceRegionLocalizations) {

			entityCache.removeResult(
				CommerceRegionLocalizationImpl.class,
				commerceRegionLocalization);
		}
	}

	@Override
	public void clearCache(Set<Serializable> primaryKeys) {
		finderCache.clearCache(CommerceRegionLocalizationImpl.class);

		for (Serializable primaryKey : primaryKeys) {
			entityCache.removeResult(
				CommerceRegionLocalizationImpl.class, primaryKey);
		}
	}

	protected void cacheUniqueFindersCache(
		CommerceRegionLocalizationModelImpl
			commerceRegionLocalizationModelImpl) {

		Object[] args = new Object[] {
			commerceRegionLocalizationModelImpl.getCommerceRegionId(),
			commerceRegionLocalizationModelImpl.getLanguageId()
		};

		finderCache.putResult(
			_finderPathCountByCommerceRegionId_LanguageId, args,
			Long.valueOf(1));
		finderCache.putResult(
			_finderPathFetchByCommerceRegionId_LanguageId, args,
			commerceRegionLocalizationModelImpl);
	}

	/**
	 * Creates a new commerce region localization with the primary key. Does not add the commerce region localization to the database.
	 *
	 * @param commerceRegionLocalizationId the primary key for the new commerce region localization
	 * @return the new commerce region localization
	 */
	@Override
	public CommerceRegionLocalization create(
		long commerceRegionLocalizationId) {

		CommerceRegionLocalization commerceRegionLocalization =
			new CommerceRegionLocalizationImpl();

		commerceRegionLocalization.setNew(true);
		commerceRegionLocalization.setPrimaryKey(commerceRegionLocalizationId);

		commerceRegionLocalization.setCompanyId(
			CompanyThreadLocal.getCompanyId());

		return commerceRegionLocalization;
	}

	/**
	 * Removes the commerce region localization with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param commerceRegionLocalizationId the primary key of the commerce region localization
	 * @return the commerce region localization that was removed
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	@Override
	public CommerceRegionLocalization remove(long commerceRegionLocalizationId)
		throws NoSuchRegionLocalizationException {

		return remove((Serializable)commerceRegionLocalizationId);
	}

	/**
	 * Removes the commerce region localization with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the commerce region localization
	 * @return the commerce region localization that was removed
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	@Override
	public CommerceRegionLocalization remove(Serializable primaryKey)
		throws NoSuchRegionLocalizationException {

		Session session = null;

		try {
			session = openSession();

			CommerceRegionLocalization commerceRegionLocalization =
				(CommerceRegionLocalization)session.get(
					CommerceRegionLocalizationImpl.class, primaryKey);

			if (commerceRegionLocalization == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchRegionLocalizationException(
					_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			return remove(commerceRegionLocalization);
		}
		catch (NoSuchRegionLocalizationException noSuchEntityException) {
			throw noSuchEntityException;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	protected CommerceRegionLocalization removeImpl(
		CommerceRegionLocalization commerceRegionLocalization) {

		Session session = null;

		try {
			session = openSession();

			if (!session.contains(commerceRegionLocalization)) {
				commerceRegionLocalization =
					(CommerceRegionLocalization)session.get(
						CommerceRegionLocalizationImpl.class,
						commerceRegionLocalization.getPrimaryKeyObj());
			}

			if (commerceRegionLocalization != null) {
				session.delete(commerceRegionLocalization);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (commerceRegionLocalization != null) {
			clearCache(commerceRegionLocalization);
		}

		return commerceRegionLocalization;
	}

	@Override
	public CommerceRegionLocalization updateImpl(
		CommerceRegionLocalization commerceRegionLocalization) {

		boolean isNew = commerceRegionLocalization.isNew();

		if (!(commerceRegionLocalization instanceof
				CommerceRegionLocalizationModelImpl)) {

			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(commerceRegionLocalization.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					commerceRegionLocalization);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in commerceRegionLocalization proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom CommerceRegionLocalization implementation " +
					commerceRegionLocalization.getClass());
		}

		CommerceRegionLocalizationModelImpl
			commerceRegionLocalizationModelImpl =
				(CommerceRegionLocalizationModelImpl)commerceRegionLocalization;

		Session session = null;

		try {
			session = openSession();

			if (isNew) {
				session.save(commerceRegionLocalization);
			}
			else {
				commerceRegionLocalization =
					(CommerceRegionLocalization)session.merge(
						commerceRegionLocalization);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		entityCache.putResult(
			CommerceRegionLocalizationImpl.class,
			commerceRegionLocalizationModelImpl, false, true);

		cacheUniqueFindersCache(commerceRegionLocalizationModelImpl);

		if (isNew) {
			commerceRegionLocalization.setNew(false);
		}

		commerceRegionLocalization.resetOriginalValues();

		return commerceRegionLocalization;
	}

	/**
	 * Returns the commerce region localization with the primary key or throws a <code>com.liferay.portal.kernel.exception.NoSuchModelException</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the commerce region localization
	 * @return the commerce region localization
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	@Override
	public CommerceRegionLocalization findByPrimaryKey(Serializable primaryKey)
		throws NoSuchRegionLocalizationException {

		CommerceRegionLocalization commerceRegionLocalization =
			fetchByPrimaryKey(primaryKey);

		if (commerceRegionLocalization == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			throw new NoSuchRegionLocalizationException(
				_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
		}

		return commerceRegionLocalization;
	}

	/**
	 * Returns the commerce region localization with the primary key or throws a <code>NoSuchRegionLocalizationException</code> if it could not be found.
	 *
	 * @param commerceRegionLocalizationId the primary key of the commerce region localization
	 * @return the commerce region localization
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	@Override
	public CommerceRegionLocalization findByPrimaryKey(
			long commerceRegionLocalizationId)
		throws NoSuchRegionLocalizationException {

		return findByPrimaryKey((Serializable)commerceRegionLocalizationId);
	}

	/**
	 * Returns the commerce region localization with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param commerceRegionLocalizationId the primary key of the commerce region localization
	 * @return the commerce region localization, or <code>null</code> if a commerce region localization with the primary key could not be found
	 */
	@Override
	public CommerceRegionLocalization fetchByPrimaryKey(
		long commerceRegionLocalizationId) {

		return fetchByPrimaryKey((Serializable)commerceRegionLocalizationId);
	}

	/**
	 * Returns all the commerce region localizations.
	 *
	 * @return the commerce region localizations
	 */
	@Override
	public List<CommerceRegionLocalization> findAll() {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the commerce region localizations.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceRegionLocalizationModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of commerce region localizations
	 * @param end the upper bound of the range of commerce region localizations (not inclusive)
	 * @return the range of commerce region localizations
	 */
	@Override
	public List<CommerceRegionLocalization> findAll(int start, int end) {
		return findAll(start, end, null);
	}

	/**
	 * Returns an ordered range of all the commerce region localizations.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceRegionLocalizationModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of commerce region localizations
	 * @param end the upper bound of the range of commerce region localizations (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of commerce region localizations
	 */
	@Override
	public List<CommerceRegionLocalization> findAll(
		int start, int end,
		OrderByComparator<CommerceRegionLocalization> orderByComparator) {

		return findAll(start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the commerce region localizations.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceRegionLocalizationModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of commerce region localizations
	 * @param end the upper bound of the range of commerce region localizations (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of commerce region localizations
	 */
	@Override
	public List<CommerceRegionLocalization> findAll(
		int start, int end,
		OrderByComparator<CommerceRegionLocalization> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindAll;
				finderArgs = FINDER_ARGS_EMPTY;
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindAll;
			finderArgs = new Object[] {start, end, orderByComparator};
		}

		List<CommerceRegionLocalization> list = null;

		if (useFinderCache) {
			list = (List<CommerceRegionLocalization>)finderCache.getResult(
				finderPath, finderArgs, this);
		}

		if (list == null) {
			StringBundler sb = null;
			String sql = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					2 + (orderByComparator.getOrderByFields().length * 2));

				sb.append(_SQL_SELECT_COMMERCEREGIONLOCALIZATION);

				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);

				sql = sb.toString();
			}
			else {
				sql = _SQL_SELECT_COMMERCEREGIONLOCALIZATION;

				sql = sql.concat(
					CommerceRegionLocalizationModelImpl.ORDER_BY_JPQL);
			}

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				list = (List<CommerceRegionLocalization>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Removes all the commerce region localizations from the database.
	 *
	 */
	@Override
	public void removeAll() {
		for (CommerceRegionLocalization commerceRegionLocalization :
				findAll()) {

			remove(commerceRegionLocalization);
		}
	}

	/**
	 * Returns the number of commerce region localizations.
	 *
	 * @return the number of commerce region localizations
	 */
	@Override
	public int countAll() {
		Long count = (Long)finderCache.getResult(
			_finderPathCountAll, FINDER_ARGS_EMPTY, this);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(
					_SQL_COUNT_COMMERCEREGIONLOCALIZATION);

				count = (Long)query.uniqueResult();

				finderCache.putResult(
					_finderPathCountAll, FINDER_ARGS_EMPTY, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	@Override
	protected EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	protected String getPKDBName() {
		return "commerceRegionLocalizationId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_COMMERCEREGIONLOCALIZATION;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return CommerceRegionLocalizationModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the commerce region localization persistence.
	 */
	public void afterPropertiesSet() {
		Bundle bundle = FrameworkUtil.getBundle(
			CommerceRegionLocalizationPersistenceImpl.class);

		_bundleContext = bundle.getBundleContext();

		_argumentsResolverServiceRegistration = _bundleContext.registerService(
			ArgumentsResolver.class,
			new CommerceRegionLocalizationModelArgumentsResolver(),
			new HashMapDictionary<>());

		_finderPathWithPaginationFindAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findAll", new String[0],
			new String[0], true);

		_finderPathWithoutPaginationFindAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findAll", new String[0],
			new String[0], true);

		_finderPathCountAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countAll",
			new String[0], new String[0], false);

		_finderPathWithPaginationFindByCommerceRegionId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByCommerceRegionId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			},
			new String[] {"commerceRegionId"}, true);

		_finderPathWithoutPaginationFindByCommerceRegionId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByCommerceRegionId",
			new String[] {Long.class.getName()},
			new String[] {"commerceRegionId"}, true);

		_finderPathCountByCommerceRegionId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"countByCommerceRegionId", new String[] {Long.class.getName()},
			new String[] {"commerceRegionId"}, false);

		_finderPathFetchByCommerceRegionId_LanguageId = new FinderPath(
			FINDER_CLASS_NAME_ENTITY, "fetchByCommerceRegionId_LanguageId",
			new String[] {Long.class.getName(), String.class.getName()},
			new String[] {"commerceRegionId", "languageId"}, true);

		_finderPathCountByCommerceRegionId_LanguageId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"countByCommerceRegionId_LanguageId",
			new String[] {Long.class.getName(), String.class.getName()},
			new String[] {"commerceRegionId", "languageId"}, false);
	}

	public void destroy() {
		entityCache.removeCache(CommerceRegionLocalizationImpl.class.getName());

		_argumentsResolverServiceRegistration.unregister();
	}

	private BundleContext _bundleContext;

	@ServiceReference(type = EntityCache.class)
	protected EntityCache entityCache;

	@ServiceReference(type = FinderCache.class)
	protected FinderCache finderCache;

	private static final String _SQL_SELECT_COMMERCEREGIONLOCALIZATION =
		"SELECT commerceRegionLocalization FROM CommerceRegionLocalization commerceRegionLocalization";

	private static final String _SQL_SELECT_COMMERCEREGIONLOCALIZATION_WHERE =
		"SELECT commerceRegionLocalization FROM CommerceRegionLocalization commerceRegionLocalization WHERE ";

	private static final String _SQL_COUNT_COMMERCEREGIONLOCALIZATION =
		"SELECT COUNT(commerceRegionLocalization) FROM CommerceRegionLocalization commerceRegionLocalization";

	private static final String _SQL_COUNT_COMMERCEREGIONLOCALIZATION_WHERE =
		"SELECT COUNT(commerceRegionLocalization) FROM CommerceRegionLocalization commerceRegionLocalization WHERE ";

	private static final String _ORDER_BY_ENTITY_ALIAS =
		"commerceRegionLocalization.";

	private static final String _NO_SUCH_ENTITY_WITH_PRIMARY_KEY =
		"No CommerceRegionLocalization exists with the primary key ";

	private static final String _NO_SUCH_ENTITY_WITH_KEY =
		"No CommerceRegionLocalization exists with the key {";

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceRegionLocalizationPersistenceImpl.class);

	@Override
	protected FinderCache getFinderCache() {
		return finderCache;
	}

	private ServiceRegistration<ArgumentsResolver>
		_argumentsResolverServiceRegistration;

	private static class CommerceRegionLocalizationModelArgumentsResolver
		implements ArgumentsResolver {

		@Override
		public Object[] getArguments(
			FinderPath finderPath, BaseModel<?> baseModel, boolean checkColumn,
			boolean original) {

			String[] columnNames = finderPath.getColumnNames();

			if ((columnNames == null) || (columnNames.length == 0)) {
				if (baseModel.isNew()) {
					return FINDER_ARGS_EMPTY;
				}

				return null;
			}

			CommerceRegionLocalizationModelImpl
				commerceRegionLocalizationModelImpl =
					(CommerceRegionLocalizationModelImpl)baseModel;

			long columnBitmask =
				commerceRegionLocalizationModelImpl.getColumnBitmask();

			if (!checkColumn || (columnBitmask == 0)) {
				return _getValue(
					commerceRegionLocalizationModelImpl, columnNames, original);
			}

			Long finderPathColumnBitmask = _finderPathColumnBitmasksCache.get(
				finderPath);

			if (finderPathColumnBitmask == null) {
				finderPathColumnBitmask = 0L;

				for (String columnName : columnNames) {
					finderPathColumnBitmask |=
						commerceRegionLocalizationModelImpl.getColumnBitmask(
							columnName);
				}

				_finderPathColumnBitmasksCache.put(
					finderPath, finderPathColumnBitmask);
			}

			if ((columnBitmask & finderPathColumnBitmask) != 0) {
				return _getValue(
					commerceRegionLocalizationModelImpl, columnNames, original);
			}

			return null;
		}

		@Override
		public String getClassName() {
			return CommerceRegionLocalizationImpl.class.getName();
		}

		@Override
		public String getTableName() {
			return CommerceRegionLocalizationTable.INSTANCE.getTableName();
		}

		private Object[] _getValue(
			CommerceRegionLocalizationModelImpl
				commerceRegionLocalizationModelImpl,
			String[] columnNames, boolean original) {

			Object[] arguments = new Object[columnNames.length];

			for (int i = 0; i < arguments.length; i++) {
				String columnName = columnNames[i];

				if (original) {
					arguments[i] =
						commerceRegionLocalizationModelImpl.
							getColumnOriginalValue(columnName);
				}
				else {
					arguments[i] =
						commerceRegionLocalizationModelImpl.getColumnValue(
							columnName);
				}
			}

			return arguments;
		}

		private static Map<FinderPath, Long> _finderPathColumnBitmasksCache =
			new ConcurrentHashMap<>();

	}

}