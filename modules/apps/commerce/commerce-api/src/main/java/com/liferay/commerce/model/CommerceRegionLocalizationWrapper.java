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

package com.liferay.commerce.model;

import com.liferay.portal.kernel.model.ModelWrapper;
import com.liferay.portal.kernel.model.wrapper.BaseModelWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is a wrapper for {@link CommerceRegionLocalization}.
 * </p>
 *
 * @author Alessio Antonio Rendina
 * @see CommerceRegionLocalization
 * @generated
 */
public class CommerceRegionLocalizationWrapper
	extends BaseModelWrapper<CommerceRegionLocalization>
	implements CommerceRegionLocalization,
			   ModelWrapper<CommerceRegionLocalization> {

	public CommerceRegionLocalizationWrapper(
		CommerceRegionLocalization commerceRegionLocalization) {

		super(commerceRegionLocalization);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("mvccVersion", getMvccVersion());
		attributes.put(
			"commerceRegionLocalizationId", getCommerceRegionLocalizationId());
		attributes.put("companyId", getCompanyId());
		attributes.put("commerceRegionId", getCommerceRegionId());
		attributes.put("languageId", getLanguageId());
		attributes.put("name", getName());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long mvccVersion = (Long)attributes.get("mvccVersion");

		if (mvccVersion != null) {
			setMvccVersion(mvccVersion);
		}

		Long commerceRegionLocalizationId = (Long)attributes.get(
			"commerceRegionLocalizationId");

		if (commerceRegionLocalizationId != null) {
			setCommerceRegionLocalizationId(commerceRegionLocalizationId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Long commerceRegionId = (Long)attributes.get("commerceRegionId");

		if (commerceRegionId != null) {
			setCommerceRegionId(commerceRegionId);
		}

		String languageId = (String)attributes.get("languageId");

		if (languageId != null) {
			setLanguageId(languageId);
		}

		String name = (String)attributes.get("name");

		if (name != null) {
			setName(name);
		}
	}

	/**
	 * Returns the commerce region ID of this commerce region localization.
	 *
	 * @return the commerce region ID of this commerce region localization
	 */
	@Override
	public long getCommerceRegionId() {
		return model.getCommerceRegionId();
	}

	/**
	 * Returns the commerce region localization ID of this commerce region localization.
	 *
	 * @return the commerce region localization ID of this commerce region localization
	 */
	@Override
	public long getCommerceRegionLocalizationId() {
		return model.getCommerceRegionLocalizationId();
	}

	/**
	 * Returns the company ID of this commerce region localization.
	 *
	 * @return the company ID of this commerce region localization
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the language ID of this commerce region localization.
	 *
	 * @return the language ID of this commerce region localization
	 */
	@Override
	public String getLanguageId() {
		return model.getLanguageId();
	}

	/**
	 * Returns the mvcc version of this commerce region localization.
	 *
	 * @return the mvcc version of this commerce region localization
	 */
	@Override
	public long getMvccVersion() {
		return model.getMvccVersion();
	}

	/**
	 * Returns the name of this commerce region localization.
	 *
	 * @return the name of this commerce region localization
	 */
	@Override
	public String getName() {
		return model.getName();
	}

	/**
	 * Returns the primary key of this commerce region localization.
	 *
	 * @return the primary key of this commerce region localization
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	/**
	 * Sets the commerce region ID of this commerce region localization.
	 *
	 * @param commerceRegionId the commerce region ID of this commerce region localization
	 */
	@Override
	public void setCommerceRegionId(long commerceRegionId) {
		model.setCommerceRegionId(commerceRegionId);
	}

	/**
	 * Sets the commerce region localization ID of this commerce region localization.
	 *
	 * @param commerceRegionLocalizationId the commerce region localization ID of this commerce region localization
	 */
	@Override
	public void setCommerceRegionLocalizationId(
		long commerceRegionLocalizationId) {

		model.setCommerceRegionLocalizationId(commerceRegionLocalizationId);
	}

	/**
	 * Sets the company ID of this commerce region localization.
	 *
	 * @param companyId the company ID of this commerce region localization
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the language ID of this commerce region localization.
	 *
	 * @param languageId the language ID of this commerce region localization
	 */
	@Override
	public void setLanguageId(String languageId) {
		model.setLanguageId(languageId);
	}

	/**
	 * Sets the mvcc version of this commerce region localization.
	 *
	 * @param mvccVersion the mvcc version of this commerce region localization
	 */
	@Override
	public void setMvccVersion(long mvccVersion) {
		model.setMvccVersion(mvccVersion);
	}

	/**
	 * Sets the name of this commerce region localization.
	 *
	 * @param name the name of this commerce region localization
	 */
	@Override
	public void setName(String name) {
		model.setName(name);
	}

	/**
	 * Sets the primary key of this commerce region localization.
	 *
	 * @param primaryKey the primary key of this commerce region localization
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	@Override
	protected CommerceRegionLocalizationWrapper wrap(
		CommerceRegionLocalization commerceRegionLocalization) {

		return new CommerceRegionLocalizationWrapper(
			commerceRegionLocalization);
	}

}