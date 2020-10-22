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

package com.liferay.commerce.service.persistence;

import com.liferay.commerce.model.CommerceRegionLocalization;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The persistence utility for the commerce region localization service. This utility wraps <code>com.liferay.commerce.service.persistence.impl.CommerceRegionLocalizationPersistenceImpl</code> and provides direct access to the database for CRUD operations. This utility should only be used by the service layer, as it must operate within a transaction. Never access this utility in a JSP, controller, model, or other front-end class.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Alessio Antonio Rendina
 * @see CommerceRegionLocalizationPersistence
 * @generated
 */
public class CommerceRegionLocalizationUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */

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
		CommerceRegionLocalization commerceRegionLocalization) {

		getPersistence().clearCache(commerceRegionLocalization);
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
	public static Map<Serializable, CommerceRegionLocalization>
		fetchByPrimaryKeys(Set<Serializable> primaryKeys) {

		return getPersistence().fetchByPrimaryKeys(primaryKeys);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery)
	 */
	public static List<CommerceRegionLocalization> findWithDynamicQuery(
		DynamicQuery dynamicQuery) {

		return getPersistence().findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int)
	 */
	public static List<CommerceRegionLocalization> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getPersistence().findWithDynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int, OrderByComparator)
	 */
	public static List<CommerceRegionLocalization> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<CommerceRegionLocalization> orderByComparator) {

		return getPersistence().findWithDynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static CommerceRegionLocalization update(
		CommerceRegionLocalization commerceRegionLocalization) {

		return getPersistence().update(commerceRegionLocalization);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel, ServiceContext)
	 */
	public static CommerceRegionLocalization update(
		CommerceRegionLocalization commerceRegionLocalization,
		ServiceContext serviceContext) {

		return getPersistence().update(
			commerceRegionLocalization, serviceContext);
	}

	/**
	 * Returns all the commerce region localizations where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @return the matching commerce region localizations
	 */
	public static List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId) {

		return getPersistence().findByCommerceRegionId(commerceRegionId);
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
	public static List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId, int start, int end) {

		return getPersistence().findByCommerceRegionId(
			commerceRegionId, start, end);
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
	public static List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId, int start, int end,
		OrderByComparator<CommerceRegionLocalization> orderByComparator) {

		return getPersistence().findByCommerceRegionId(
			commerceRegionId, start, end, orderByComparator);
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
	public static List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId, int start, int end,
		OrderByComparator<CommerceRegionLocalization> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByCommerceRegionId(
			commerceRegionId, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce region localization
	 * @throws NoSuchRegionLocalizationException if a matching commerce region localization could not be found
	 */
	public static CommerceRegionLocalization findByCommerceRegionId_First(
			long commerceRegionId,
			OrderByComparator<CommerceRegionLocalization> orderByComparator)
		throws com.liferay.commerce.exception.
			NoSuchRegionLocalizationException {

		return getPersistence().findByCommerceRegionId_First(
			commerceRegionId, orderByComparator);
	}

	/**
	 * Returns the first commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	public static CommerceRegionLocalization fetchByCommerceRegionId_First(
		long commerceRegionId,
		OrderByComparator<CommerceRegionLocalization> orderByComparator) {

		return getPersistence().fetchByCommerceRegionId_First(
			commerceRegionId, orderByComparator);
	}

	/**
	 * Returns the last commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce region localization
	 * @throws NoSuchRegionLocalizationException if a matching commerce region localization could not be found
	 */
	public static CommerceRegionLocalization findByCommerceRegionId_Last(
			long commerceRegionId,
			OrderByComparator<CommerceRegionLocalization> orderByComparator)
		throws com.liferay.commerce.exception.
			NoSuchRegionLocalizationException {

		return getPersistence().findByCommerceRegionId_Last(
			commerceRegionId, orderByComparator);
	}

	/**
	 * Returns the last commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	public static CommerceRegionLocalization fetchByCommerceRegionId_Last(
		long commerceRegionId,
		OrderByComparator<CommerceRegionLocalization> orderByComparator) {

		return getPersistence().fetchByCommerceRegionId_Last(
			commerceRegionId, orderByComparator);
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
	public static CommerceRegionLocalization[]
			findByCommerceRegionId_PrevAndNext(
				long commerceRegionLocalizationId, long commerceRegionId,
				OrderByComparator<CommerceRegionLocalization> orderByComparator)
		throws com.liferay.commerce.exception.
			NoSuchRegionLocalizationException {

		return getPersistence().findByCommerceRegionId_PrevAndNext(
			commerceRegionLocalizationId, commerceRegionId, orderByComparator);
	}

	/**
	 * Removes all the commerce region localizations where commerceRegionId = &#63; from the database.
	 *
	 * @param commerceRegionId the commerce region ID
	 */
	public static void removeByCommerceRegionId(long commerceRegionId) {
		getPersistence().removeByCommerceRegionId(commerceRegionId);
	}

	/**
	 * Returns the number of commerce region localizations where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @return the number of matching commerce region localizations
	 */
	public static int countByCommerceRegionId(long commerceRegionId) {
		return getPersistence().countByCommerceRegionId(commerceRegionId);
	}

	/**
	 * Returns the commerce region localization where commerceRegionId = &#63; and languageId = &#63; or throws a <code>NoSuchRegionLocalizationException</code> if it could not be found.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the matching commerce region localization
	 * @throws NoSuchRegionLocalizationException if a matching commerce region localization could not be found
	 */
	public static CommerceRegionLocalization findByCommerceRegionId_LanguageId(
			long commerceRegionId, String languageId)
		throws com.liferay.commerce.exception.
			NoSuchRegionLocalizationException {

		return getPersistence().findByCommerceRegionId_LanguageId(
			commerceRegionId, languageId);
	}

	/**
	 * Returns the commerce region localization where commerceRegionId = &#63; and languageId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	public static CommerceRegionLocalization fetchByCommerceRegionId_LanguageId(
		long commerceRegionId, String languageId) {

		return getPersistence().fetchByCommerceRegionId_LanguageId(
			commerceRegionId, languageId);
	}

	/**
	 * Returns the commerce region localization where commerceRegionId = &#63; and languageId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	public static CommerceRegionLocalization fetchByCommerceRegionId_LanguageId(
		long commerceRegionId, String languageId, boolean useFinderCache) {

		return getPersistence().fetchByCommerceRegionId_LanguageId(
			commerceRegionId, languageId, useFinderCache);
	}

	/**
	 * Removes the commerce region localization where commerceRegionId = &#63; and languageId = &#63; from the database.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the commerce region localization that was removed
	 */
	public static CommerceRegionLocalization
			removeByCommerceRegionId_LanguageId(
				long commerceRegionId, String languageId)
		throws com.liferay.commerce.exception.
			NoSuchRegionLocalizationException {

		return getPersistence().removeByCommerceRegionId_LanguageId(
			commerceRegionId, languageId);
	}

	/**
	 * Returns the number of commerce region localizations where commerceRegionId = &#63; and languageId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the number of matching commerce region localizations
	 */
	public static int countByCommerceRegionId_LanguageId(
		long commerceRegionId, String languageId) {

		return getPersistence().countByCommerceRegionId_LanguageId(
			commerceRegionId, languageId);
	}

	/**
	 * Caches the commerce region localization in the entity cache if it is enabled.
	 *
	 * @param commerceRegionLocalization the commerce region localization
	 */
	public static void cacheResult(
		CommerceRegionLocalization commerceRegionLocalization) {

		getPersistence().cacheResult(commerceRegionLocalization);
	}

	/**
	 * Caches the commerce region localizations in the entity cache if it is enabled.
	 *
	 * @param commerceRegionLocalizations the commerce region localizations
	 */
	public static void cacheResult(
		List<CommerceRegionLocalization> commerceRegionLocalizations) {

		getPersistence().cacheResult(commerceRegionLocalizations);
	}

	/**
	 * Creates a new commerce region localization with the primary key. Does not add the commerce region localization to the database.
	 *
	 * @param commerceRegionLocalizationId the primary key for the new commerce region localization
	 * @return the new commerce region localization
	 */
	public static CommerceRegionLocalization create(
		long commerceRegionLocalizationId) {

		return getPersistence().create(commerceRegionLocalizationId);
	}

	/**
	 * Removes the commerce region localization with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param commerceRegionLocalizationId the primary key of the commerce region localization
	 * @return the commerce region localization that was removed
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	public static CommerceRegionLocalization remove(
			long commerceRegionLocalizationId)
		throws com.liferay.commerce.exception.
			NoSuchRegionLocalizationException {

		return getPersistence().remove(commerceRegionLocalizationId);
	}

	public static CommerceRegionLocalization updateImpl(
		CommerceRegionLocalization commerceRegionLocalization) {

		return getPersistence().updateImpl(commerceRegionLocalization);
	}

	/**
	 * Returns the commerce region localization with the primary key or throws a <code>NoSuchRegionLocalizationException</code> if it could not be found.
	 *
	 * @param commerceRegionLocalizationId the primary key of the commerce region localization
	 * @return the commerce region localization
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	public static CommerceRegionLocalization findByPrimaryKey(
			long commerceRegionLocalizationId)
		throws com.liferay.commerce.exception.
			NoSuchRegionLocalizationException {

		return getPersistence().findByPrimaryKey(commerceRegionLocalizationId);
	}

	/**
	 * Returns the commerce region localization with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param commerceRegionLocalizationId the primary key of the commerce region localization
	 * @return the commerce region localization, or <code>null</code> if a commerce region localization with the primary key could not be found
	 */
	public static CommerceRegionLocalization fetchByPrimaryKey(
		long commerceRegionLocalizationId) {

		return getPersistence().fetchByPrimaryKey(commerceRegionLocalizationId);
	}

	/**
	 * Returns all the commerce region localizations.
	 *
	 * @return the commerce region localizations
	 */
	public static List<CommerceRegionLocalization> findAll() {
		return getPersistence().findAll();
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
	public static List<CommerceRegionLocalization> findAll(int start, int end) {
		return getPersistence().findAll(start, end);
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
	public static List<CommerceRegionLocalization> findAll(
		int start, int end,
		OrderByComparator<CommerceRegionLocalization> orderByComparator) {

		return getPersistence().findAll(start, end, orderByComparator);
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
	public static List<CommerceRegionLocalization> findAll(
		int start, int end,
		OrderByComparator<CommerceRegionLocalization> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findAll(
			start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Removes all the commerce region localizations from the database.
	 */
	public static void removeAll() {
		getPersistence().removeAll();
	}

	/**
	 * Returns the number of commerce region localizations.
	 *
	 * @return the number of commerce region localizations
	 */
	public static int countAll() {
		return getPersistence().countAll();
	}

	public static CommerceRegionLocalizationPersistence getPersistence() {
		return _serviceTracker.getService();
	}

	private static ServiceTracker
		<CommerceRegionLocalizationPersistence,
		 CommerceRegionLocalizationPersistence> _serviceTracker;

	static {
		Bundle bundle = FrameworkUtil.getBundle(
			CommerceRegionLocalizationPersistence.class);

		ServiceTracker
			<CommerceRegionLocalizationPersistence,
			 CommerceRegionLocalizationPersistence> serviceTracker =
				new ServiceTracker
					<CommerceRegionLocalizationPersistence,
					 CommerceRegionLocalizationPersistence>(
						 bundle.getBundleContext(),
						 CommerceRegionLocalizationPersistence.class, null);

		serviceTracker.open();

		_serviceTracker = serviceTracker;
	}

}