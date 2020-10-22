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

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used by SOAP remote services.
 *
 * @author Alessio Antonio Rendina
 * @deprecated As of Athanasius (7.3.x), with no direct replacement
 * @generated
 */
@Deprecated
public class CommerceRegionLocalizationSoap implements Serializable {

	public static CommerceRegionLocalizationSoap toSoapModel(
		CommerceRegionLocalization model) {

		CommerceRegionLocalizationSoap soapModel =
			new CommerceRegionLocalizationSoap();

		soapModel.setMvccVersion(model.getMvccVersion());
		soapModel.setCommerceRegionLocalizationId(
			model.getCommerceRegionLocalizationId());
		soapModel.setCompanyId(model.getCompanyId());
		soapModel.setCommerceRegionId(model.getCommerceRegionId());
		soapModel.setLanguageId(model.getLanguageId());
		soapModel.setName(model.getName());

		return soapModel;
	}

	public static CommerceRegionLocalizationSoap[] toSoapModels(
		CommerceRegionLocalization[] models) {

		CommerceRegionLocalizationSoap[] soapModels =
			new CommerceRegionLocalizationSoap[models.length];

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModel(models[i]);
		}

		return soapModels;
	}

	public static CommerceRegionLocalizationSoap[][] toSoapModels(
		CommerceRegionLocalization[][] models) {

		CommerceRegionLocalizationSoap[][] soapModels = null;

		if (models.length > 0) {
			soapModels =
				new CommerceRegionLocalizationSoap
					[models.length][models[0].length];
		}
		else {
			soapModels = new CommerceRegionLocalizationSoap[0][0];
		}

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModels(models[i]);
		}

		return soapModels;
	}

	public static CommerceRegionLocalizationSoap[] toSoapModels(
		List<CommerceRegionLocalization> models) {

		List<CommerceRegionLocalizationSoap> soapModels =
			new ArrayList<CommerceRegionLocalizationSoap>(models.size());

		for (CommerceRegionLocalization model : models) {
			soapModels.add(toSoapModel(model));
		}

		return soapModels.toArray(
			new CommerceRegionLocalizationSoap[soapModels.size()]);
	}

	public CommerceRegionLocalizationSoap() {
	}

	public long getPrimaryKey() {
		return _commerceRegionLocalizationId;
	}

	public void setPrimaryKey(long pk) {
		setCommerceRegionLocalizationId(pk);
	}

	public long getMvccVersion() {
		return _mvccVersion;
	}

	public void setMvccVersion(long mvccVersion) {
		_mvccVersion = mvccVersion;
	}

	public long getCommerceRegionLocalizationId() {
		return _commerceRegionLocalizationId;
	}

	public void setCommerceRegionLocalizationId(
		long commerceRegionLocalizationId) {

		_commerceRegionLocalizationId = commerceRegionLocalizationId;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	public long getCommerceRegionId() {
		return _commerceRegionId;
	}

	public void setCommerceRegionId(long commerceRegionId) {
		_commerceRegionId = commerceRegionId;
	}

	public String getLanguageId() {
		return _languageId;
	}

	public void setLanguageId(String languageId) {
		_languageId = languageId;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	private long _mvccVersion;
	private long _commerceRegionLocalizationId;
	private long _companyId;
	private long _commerceRegionId;
	private String _languageId;
	private String _name;

}