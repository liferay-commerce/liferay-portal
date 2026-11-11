/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service;

import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.persistence.BasePersistence;
import com.liferay.portal.kernel.service.persistence.change.tracking.CTPersistence;

/**
 * Provides a wrapper for {@link CPDefinitionVisibilityRelLocalService}.
 *
 * @author Marco Leo
 * @see CPDefinitionVisibilityRelLocalService
 * @generated
 */
public class CPDefinitionVisibilityRelLocalServiceWrapper
	implements CPDefinitionVisibilityRelLocalService,
			   ServiceWrapper<CPDefinitionVisibilityRelLocalService> {

	public CPDefinitionVisibilityRelLocalServiceWrapper() {
		this(null);
	}

	public CPDefinitionVisibilityRelLocalServiceWrapper(
		CPDefinitionVisibilityRelLocalService
			cpDefinitionVisibilityRelLocalService) {

		_cpDefinitionVisibilityRelLocalService =
			cpDefinitionVisibilityRelLocalService;
	}

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
	@Override
	public CPDefinitionVisibilityRel addCPDefinitionVisibilityRel(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		return _cpDefinitionVisibilityRelLocalService.
			addCPDefinitionVisibilityRel(cpDefinitionVisibilityRel);
	}

	/**
	 * Creates a new cp definition visibility rel with the primary key. Does not add the cp definition visibility rel to the database.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key for the new cp definition visibility rel
	 * @return the new cp definition visibility rel
	 */
	@Override
	public CPDefinitionVisibilityRel createCPDefinitionVisibilityRel(
		long CPDefinitionVisibilityRelId) {

		return _cpDefinitionVisibilityRelLocalService.
			createCPDefinitionVisibilityRel(CPDefinitionVisibilityRelId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel createPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelLocalService.createPersistedModel(
			primaryKeyObj);
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
	 */
	@Override
	public CPDefinitionVisibilityRel deleteCPDefinitionVisibilityRel(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		return _cpDefinitionVisibilityRelLocalService.
			deleteCPDefinitionVisibilityRel(cpDefinitionVisibilityRel);
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
	@Override
	public CPDefinitionVisibilityRel deleteCPDefinitionVisibilityRel(
			long CPDefinitionVisibilityRelId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelLocalService.
			deleteCPDefinitionVisibilityRel(CPDefinitionVisibilityRelId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel deletePersistedModel(
			com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelLocalService.deletePersistedModel(
			persistedModel);
	}

	@Override
	public <T> T dslQuery(com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {
		return _cpDefinitionVisibilityRelLocalService.dslQuery(dslQuery);
	}

	@Override
	public int dslQueryCount(
		com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {

		return _cpDefinitionVisibilityRelLocalService.dslQueryCount(dslQuery);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery() {
		return _cpDefinitionVisibilityRelLocalService.dynamicQuery();
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return _cpDefinitionVisibilityRelLocalService.dynamicQuery(
			dynamicQuery);
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
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) {

		return _cpDefinitionVisibilityRelLocalService.dynamicQuery(
			dynamicQuery, start, end);
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
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator<T> orderByComparator) {

		return _cpDefinitionVisibilityRelLocalService.dynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return _cpDefinitionVisibilityRelLocalService.dynamicQueryCount(
			dynamicQuery);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @param projection the projection to apply to the query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {

		return _cpDefinitionVisibilityRelLocalService.dynamicQueryCount(
			dynamicQuery, projection);
	}

	@Override
	public CPDefinitionVisibilityRel fetchCPDefinitionVisibilityRel(
		long CPDefinitionVisibilityRelId) {

		return _cpDefinitionVisibilityRelLocalService.
			fetchCPDefinitionVisibilityRel(CPDefinitionVisibilityRelId);
	}

	@Override
	public CPDefinitionVisibilityRel
		fetchCPDefinitionVisibilityRelByExternalReferenceCode(
			String externalReferenceCode, long companyId) {

		return _cpDefinitionVisibilityRelLocalService.
			fetchCPDefinitionVisibilityRelByExternalReferenceCode(
				externalReferenceCode, companyId);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return _cpDefinitionVisibilityRelLocalService.
			getActionableDynamicQuery();
	}

	/**
	 * Returns the cp definition visibility rel with the primary key.
	 *
	 * @param CPDefinitionVisibilityRelId the primary key of the cp definition visibility rel
	 * @return the cp definition visibility rel
	 * @throws PortalException if a cp definition visibility rel with the primary key could not be found
	 */
	@Override
	public CPDefinitionVisibilityRel getCPDefinitionVisibilityRel(
			long CPDefinitionVisibilityRelId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelLocalService.
			getCPDefinitionVisibilityRel(CPDefinitionVisibilityRelId);
	}

	@Override
	public CPDefinitionVisibilityRel
			getCPDefinitionVisibilityRelByExternalReferenceCode(
				String externalReferenceCode, long companyId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelLocalService.
			getCPDefinitionVisibilityRelByExternalReferenceCode(
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
	@Override
	public java.util.List<CPDefinitionVisibilityRel>
		getCPDefinitionVisibilityRels(int start, int end) {

		return _cpDefinitionVisibilityRelLocalService.
			getCPDefinitionVisibilityRels(start, end);
	}

	/**
	 * Returns the number of cp definition visibility rels.
	 *
	 * @return the number of cp definition visibility rels
	 */
	@Override
	public int getCPDefinitionVisibilityRelsCount() {
		return _cpDefinitionVisibilityRelLocalService.
			getCPDefinitionVisibilityRelsCount();
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
		getIndexableActionableDynamicQuery() {

		return _cpDefinitionVisibilityRelLocalService.
			getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _cpDefinitionVisibilityRelLocalService.
			getOSGiServiceIdentifier();
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel getPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _cpDefinitionVisibilityRelLocalService.getPersistedModel(
			primaryKeyObj);
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
	@Override
	public CPDefinitionVisibilityRel updateCPDefinitionVisibilityRel(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		return _cpDefinitionVisibilityRelLocalService.
			updateCPDefinitionVisibilityRel(cpDefinitionVisibilityRel);
	}

	@Override
	public BasePersistence<?> getBasePersistence() {
		return _cpDefinitionVisibilityRelLocalService.getBasePersistence();
	}

	@Override
	public CTPersistence<CPDefinitionVisibilityRel> getCTPersistence() {
		return _cpDefinitionVisibilityRelLocalService.getCTPersistence();
	}

	@Override
	public Class<CPDefinitionVisibilityRel> getModelClass() {
		return _cpDefinitionVisibilityRelLocalService.getModelClass();
	}

	@Override
	public <R, E extends Throwable> R updateWithUnsafeFunction(
			UnsafeFunction<CTPersistence<CPDefinitionVisibilityRel>, R, E>
				updateUnsafeFunction)
		throws E {

		return _cpDefinitionVisibilityRelLocalService.updateWithUnsafeFunction(
			updateUnsafeFunction);
	}

	@Override
	public CPDefinitionVisibilityRelLocalService getWrappedService() {
		return _cpDefinitionVisibilityRelLocalService;
	}

	@Override
	public void setWrappedService(
		CPDefinitionVisibilityRelLocalService
			cpDefinitionVisibilityRelLocalService) {

		_cpDefinitionVisibilityRelLocalService =
			cpDefinitionVisibilityRelLocalService;
	}

	private CPDefinitionVisibilityRelLocalService
		_cpDefinitionVisibilityRelLocalService;

}
// LIFERAY-SERVICE-BUILDER-HASH:719252777