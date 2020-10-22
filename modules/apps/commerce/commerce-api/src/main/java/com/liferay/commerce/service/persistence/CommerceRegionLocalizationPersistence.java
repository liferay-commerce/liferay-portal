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

import com.liferay.commerce.exception.NoSuchRegionLocalizationException;
import com.liferay.commerce.model.CommerceRegionLocalization;
import com.liferay.portal.kernel.service.persistence.BasePersistence;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the commerce region localization service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Alessio Antonio Rendina
 * @see CommerceRegionLocalizationUtil
 * @generated
 */
@ProviderType
public interface CommerceRegionLocalizationPersistence
	extends BasePersistence<CommerceRegionLocalization> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link CommerceRegionLocalizationUtil} to access the commerce region localization persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	 * Returns all the commerce region localizations where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @return the matching commerce region localizations
	 */
	public java.util.List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId);

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
	public java.util.List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId, int start, int end);

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
	public java.util.List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceRegionLocalization> orderByComparator);

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
	public java.util.List<CommerceRegionLocalization> findByCommerceRegionId(
		long commerceRegionId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceRegionLocalization> orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce region localization
	 * @throws NoSuchRegionLocalizationException if a matching commerce region localization could not be found
	 */
	public CommerceRegionLocalization findByCommerceRegionId_First(
			long commerceRegionId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CommerceRegionLocalization> orderByComparator)
		throws NoSuchRegionLocalizationException;

	/**
	 * Returns the first commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	public CommerceRegionLocalization fetchByCommerceRegionId_First(
		long commerceRegionId,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceRegionLocalization> orderByComparator);

	/**
	 * Returns the last commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce region localization
	 * @throws NoSuchRegionLocalizationException if a matching commerce region localization could not be found
	 */
	public CommerceRegionLocalization findByCommerceRegionId_Last(
			long commerceRegionId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CommerceRegionLocalization> orderByComparator)
		throws NoSuchRegionLocalizationException;

	/**
	 * Returns the last commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	public CommerceRegionLocalization fetchByCommerceRegionId_Last(
		long commerceRegionId,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceRegionLocalization> orderByComparator);

	/**
	 * Returns the commerce region localizations before and after the current commerce region localization in the ordered set where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionLocalizationId the primary key of the current commerce region localization
	 * @param commerceRegionId the commerce region ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next commerce region localization
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	public CommerceRegionLocalization[] findByCommerceRegionId_PrevAndNext(
			long commerceRegionLocalizationId, long commerceRegionId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CommerceRegionLocalization> orderByComparator)
		throws NoSuchRegionLocalizationException;

	/**
	 * Removes all the commerce region localizations where commerceRegionId = &#63; from the database.
	 *
	 * @param commerceRegionId the commerce region ID
	 */
	public void removeByCommerceRegionId(long commerceRegionId);

	/**
	 * Returns the number of commerce region localizations where commerceRegionId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @return the number of matching commerce region localizations
	 */
	public int countByCommerceRegionId(long commerceRegionId);

	/**
	 * Returns the commerce region localization where commerceRegionId = &#63; and languageId = &#63; or throws a <code>NoSuchRegionLocalizationException</code> if it could not be found.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the matching commerce region localization
	 * @throws NoSuchRegionLocalizationException if a matching commerce region localization could not be found
	 */
	public CommerceRegionLocalization findByCommerceRegionId_LanguageId(
			long commerceRegionId, String languageId)
		throws NoSuchRegionLocalizationException;

	/**
	 * Returns the commerce region localization where commerceRegionId = &#63; and languageId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	public CommerceRegionLocalization fetchByCommerceRegionId_LanguageId(
		long commerceRegionId, String languageId);

	/**
	 * Returns the commerce region localization where commerceRegionId = &#63; and languageId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching commerce region localization, or <code>null</code> if a matching commerce region localization could not be found
	 */
	public CommerceRegionLocalization fetchByCommerceRegionId_LanguageId(
		long commerceRegionId, String languageId, boolean useFinderCache);

	/**
	 * Removes the commerce region localization where commerceRegionId = &#63; and languageId = &#63; from the database.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the commerce region localization that was removed
	 */
	public CommerceRegionLocalization removeByCommerceRegionId_LanguageId(
			long commerceRegionId, String languageId)
		throws NoSuchRegionLocalizationException;

	/**
	 * Returns the number of commerce region localizations where commerceRegionId = &#63; and languageId = &#63;.
	 *
	 * @param commerceRegionId the commerce region ID
	 * @param languageId the language ID
	 * @return the number of matching commerce region localizations
	 */
	public int countByCommerceRegionId_LanguageId(
		long commerceRegionId, String languageId);

	/**
	 * Caches the commerce region localization in the entity cache if it is enabled.
	 *
	 * @param commerceRegionLocalization the commerce region localization
	 */
	public void cacheResult(
		CommerceRegionLocalization commerceRegionLocalization);

	/**
	 * Caches the commerce region localizations in the entity cache if it is enabled.
	 *
	 * @param commerceRegionLocalizations the commerce region localizations
	 */
	public void cacheResult(
		java.util.List<CommerceRegionLocalization> commerceRegionLocalizations);

	/**
	 * Creates a new commerce region localization with the primary key. Does not add the commerce region localization to the database.
	 *
	 * @param commerceRegionLocalizationId the primary key for the new commerce region localization
	 * @return the new commerce region localization
	 */
	public CommerceRegionLocalization create(long commerceRegionLocalizationId);

	/**
	 * Removes the commerce region localization with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param commerceRegionLocalizationId the primary key of the commerce region localization
	 * @return the commerce region localization that was removed
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	public CommerceRegionLocalization remove(long commerceRegionLocalizationId)
		throws NoSuchRegionLocalizationException;

	public CommerceRegionLocalization updateImpl(
		CommerceRegionLocalization commerceRegionLocalization);

	/**
	 * Returns the commerce region localization with the primary key or throws a <code>NoSuchRegionLocalizationException</code> if it could not be found.
	 *
	 * @param commerceRegionLocalizationId the primary key of the commerce region localization
	 * @return the commerce region localization
	 * @throws NoSuchRegionLocalizationException if a commerce region localization with the primary key could not be found
	 */
	public CommerceRegionLocalization findByPrimaryKey(
			long commerceRegionLocalizationId)
		throws NoSuchRegionLocalizationException;

	/**
	 * Returns the commerce region localization with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param commerceRegionLocalizationId the primary key of the commerce region localization
	 * @return the commerce region localization, or <code>null</code> if a commerce region localization with the primary key could not be found
	 */
	public CommerceRegionLocalization fetchByPrimaryKey(
		long commerceRegionLocalizationId);

	/**
	 * Returns all the commerce region localizations.
	 *
	 * @return the commerce region localizations
	 */
	public java.util.List<CommerceRegionLocalization> findAll();

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
	public java.util.List<CommerceRegionLocalization> findAll(
		int start, int end);

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
	public java.util.List<CommerceRegionLocalization> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceRegionLocalization> orderByComparator);

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
	public java.util.List<CommerceRegionLocalization> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceRegionLocalization> orderByComparator,
		boolean useFinderCache);

	/**
	 * Removes all the commerce region localizations from the database.
	 */
	public void removeAll();

	/**
	 * Returns the number of commerce region localizations.
	 *
	 * @return the number of commerce region localizations
	 */
	public int countAll();

}