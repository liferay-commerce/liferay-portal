/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service;

import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;

/**
 * Provides the local service utility for CPDefinitionVisibilityRel. This utility wraps
 * <code>com.liferay.commerce.product.service.impl.CPDefinitionVisibilityRelLocalServiceImpl</code> and
 * is an access point for service operations in application layer code running
 * on the local server. Methods of this service will not have security checks
 * based on the propagated JAAS credentials because this service can only be
 * accessed from within the same VM.
 *
 * @author Marco Leo
 * @see CPDefinitionVisibilityRelLocalService
 * @generated
 */
public class CPDefinitionVisibilityRelLocalServiceUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to <code>com.liferay.commerce.product.service.impl.CPDefinitionVisibilityRelLocalServiceImpl</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * Adds the cp definition visibility rel to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect CPDefinitionVisibilityRelLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param cpDefinitionVisibilityRel the cp definition visibility rel
	 * @return the cp definition visibility rel that was added
	 */
	public static CPDefinitionVisibilityRel addCPDefinitionVisibilityRel(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		return getService().addCPDefinitionVisibilityRel(
			cpDefinitionVisibilityRel);
	}

	public static CPDefinitionVisibilityRel addCPDefinitionVisibilityRel(
			String externalReferenceCode, long userId, long cpDefinitionId,
			String className, long classPK)
		throws PortalException {

		return getService().addCPDefinitionVisibilityRel(
			externalReferenceCode, userId, cpDefinitionId, className, classPK);
	}

	/**
	 * Creates a new cp definition visibility rel with the primary key. Does not add the cp definition visibility rel to the database.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key for the new cp definition visibility rel
	 * @return the new cp definition visibility rel
	 */
	public static CPDefinitionVisibilityRel createCPDefinitionVisibilityRel(
		long CPDefinitionVisibilityRelId) {

		return getService().createCPDefinitionVisibilityRel(
			CPDefinitionVisibilityRelId);
	}

	/**
	 * @throws PortalException
	 */
	public static PersistedModel createPersistedModel(
			Serializable primaryKeyObj)
		throws PortalException {

		return getService().createPersistedModel(primaryKeyObj);
	}

	/**
	 * Deletes the cp definition visibility rel from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect CPDefinitionVisibilityRelLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param cpDefinitionVisibilityRel the cp definition visibility rel
	 * @return the cp definition visibility rel that was removed
	 * @throws PortalException
	 */
	public static CPDefinitionVisibilityRel deleteCPDefinitionVisibilityRel(
			CPDefinitionVisibilityRel cpDefinitionVisibilityRel)
		throws PortalException {

		return getService().deleteCPDefinitionVisibilityRel(
			cpDefinitionVisibilityRel);
	}

	/**
	 * Deletes the cp definition visibility rel with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect CPDefinitionVisibilityRelLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel that was removed
	 * @throws PortalException if a cp definition visibility rel with the primary key could not be found
	 */
	public static CPDefinitionVisibilityRel deleteCPDefinitionVisibilityRel(
			long CPDefinitionVisibilityRelId)
		throws PortalException {

		return getService().deleteCPDefinitionVisibilityRel(
			CPDefinitionVisibilityRelId);
	}

	public static void deleteCPDefinitionVisibilityRels(
			String className, long classPK)
		throws PortalException {

		getService().deleteCPDefinitionVisibilityRels(className, classPK);
	}

	/**
	 * @throws PortalException
	 */
	public static PersistedModel deletePersistedModel(
			PersistedModel persistedModel)
		throws PortalException {

		return getService().deletePersistedModel(persistedModel);
	}

	public static <T> T dslQuery(DSLQuery dslQuery) {
		return getService().dslQuery(dslQuery);
	}

	public static int dslQueryCount(DSLQuery dslQuery) {
		return getService().dslQueryCount(dslQuery);
	}

	public static DynamicQuery dynamicQuery() {
		return getService().dynamicQuery();
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	public static <T> List<T> dynamicQuery(DynamicQuery dynamicQuery) {
		return getService().dynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @return the range of matching rows
	 */
	public static <T> List<T> dynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getService().dynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching rows
	 */
	public static <T> List<T> dynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<T> orderByComparator) {

		return getService().dynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	public static long dynamicQueryCount(DynamicQuery dynamicQuery) {
		return getService().dynamicQueryCount(dynamicQuery);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @param projection the projection to apply to the query
	 * @return the number of rows matching the dynamic query
	 */
	public static long dynamicQueryCount(
		DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {

		return getService().dynamicQueryCount(dynamicQuery, projection);
	}

	public static CPDefinitionVisibilityRel fetchCPDefinitionVisibilityRel(
		long CPDefinitionVisibilityRelId) {

		return getService().fetchCPDefinitionVisibilityRel(
			CPDefinitionVisibilityRelId);
	}

	public static CPDefinitionVisibilityRel fetchCPDefinitionVisibilityRel(
		long cpDefinitionId, String className, long classPK) {

		return getService().fetchCPDefinitionVisibilityRel(
			cpDefinitionId, className, classPK);
	}

	public static CPDefinitionVisibilityRel
		fetchCPDefinitionVisibilityRelByExternalReferenceCode(
			String externalReferenceCode, long companyId) {

		return getService().
			fetchCPDefinitionVisibilityRelByExternalReferenceCode(
				externalReferenceCode, companyId);
	}

	public static com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return getService().getActionableDynamicQuery();
	}

	/**
	 * Returns the cp definition visibility rel with the primary key.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel
	 * @throws PortalException if a cp definition visibility rel with the primary key could not be found
	 */
	public static CPDefinitionVisibilityRel getCPDefinitionVisibilityRel(
			long CPDefinitionVisibilityRelId)
		throws PortalException {

		return getService().getCPDefinitionVisibilityRel(
			CPDefinitionVisibilityRelId);
	}

	public static CPDefinitionVisibilityRel
			getCPDefinitionVisibilityRelByExternalReferenceCode(
				String externalReferenceCode, long companyId)
		throws PortalException {

		return getService().getCPDefinitionVisibilityRelByExternalReferenceCode(
			externalReferenceCode, companyId);
	}

	/**
	 * Returns a range of all the cp definition visibility rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of cp definition visibility rels
	 * @param end the upper bound of the range of cp definition visibility rels (not inclusive)
	 * @return the range of cp definition visibility rels
	 */
	public static List<CPDefinitionVisibilityRel> getCPDefinitionVisibilityRels(
		int start, int end) {

		return getService().getCPDefinitionVisibilityRels(start, end);
	}

	public static List<CPDefinitionVisibilityRel> getCPDefinitionVisibilityRels(
		long cpDefinitionId, String className, int start, int end,
		OrderByComparator<CPDefinitionVisibilityRel> orderByComparator) {

		return getService().getCPDefinitionVisibilityRels(
			cpDefinitionId, className, start, end, orderByComparator);
	}

	/**
	 * Returns the number of cp definition visibility rels.
	 *
	 * @return the number of cp definition visibility rels
	 */
	public static int getCPDefinitionVisibilityRelsCount() {
		return getService().getCPDefinitionVisibilityRelsCount();
	}

	public static
		com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
			getIndexableActionableDynamicQuery() {

		return getService().getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	public static String getOSGiServiceIdentifier() {
		return getService().getOSGiServiceIdentifier();
	}

	/**
	 * @throws PortalException
	 */
	public static PersistedModel getPersistedModel(Serializable primaryKeyObj)
		throws PortalException {

		return getService().getPersistedModel(primaryKeyObj);
	}

	/**
	 * Updates the cp definition visibility rel in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect CPDefinitionVisibilityRelLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param cpDefinitionVisibilityRel the cp definition visibility rel
	 * @return the cp definition visibility rel that was updated
	 */
	public static CPDefinitionVisibilityRel updateCPDefinitionVisibilityRel(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		return getService().updateCPDefinitionVisibilityRel(
			cpDefinitionVisibilityRel);
	}

	public static CPDefinitionVisibilityRelLocalService getService() {
		return _serviceSnapshot.get();
	}

	private static final Snapshot<CPDefinitionVisibilityRelLocalService>
		_serviceSnapshot = new Snapshot<>(
			CPDefinitionVisibilityRelLocalServiceUtil.class,
			CPDefinitionVisibilityRelLocalService.class);

}
// LIFERAY-SERVICE-BUILDER-HASH:-1441384480