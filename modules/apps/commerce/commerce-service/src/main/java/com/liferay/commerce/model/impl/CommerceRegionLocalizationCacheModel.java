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

package com.liferay.commerce.model.impl;

import com.liferay.commerce.model.CommerceRegionLocalization;
import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.MVCCModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The cache model class for representing CommerceRegionLocalization in entity cache.
 *
 * @author Alessio Antonio Rendina
 * @generated
 */
public class CommerceRegionLocalizationCacheModel
	implements CacheModel<CommerceRegionLocalization>, Externalizable,
			   MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof CommerceRegionLocalizationCacheModel)) {
			return false;
		}

		CommerceRegionLocalizationCacheModel
			commerceRegionLocalizationCacheModel =
				(CommerceRegionLocalizationCacheModel)object;

		if ((commerceRegionLocalizationId ==
				commerceRegionLocalizationCacheModel.
					commerceRegionLocalizationId) &&
			(mvccVersion == commerceRegionLocalizationCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, commerceRegionLocalizationId);

		return HashUtil.hash(hashCode, mvccVersion);
	}

	@Override
	public long getMvccVersion() {
		return mvccVersion;
	}

	@Override
	public void setMvccVersion(long mvccVersion) {
		this.mvccVersion = mvccVersion;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(13);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", commerceRegionLocalizationId=");
		sb.append(commerceRegionLocalizationId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", commerceRegionId=");
		sb.append(commerceRegionId);
		sb.append(", languageId=");
		sb.append(languageId);
		sb.append(", name=");
		sb.append(name);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public CommerceRegionLocalization toEntityModel() {
		CommerceRegionLocalizationImpl commerceRegionLocalizationImpl =
			new CommerceRegionLocalizationImpl();

		commerceRegionLocalizationImpl.setMvccVersion(mvccVersion);
		commerceRegionLocalizationImpl.setCommerceRegionLocalizationId(
			commerceRegionLocalizationId);
		commerceRegionLocalizationImpl.setCompanyId(companyId);
		commerceRegionLocalizationImpl.setCommerceRegionId(commerceRegionId);

		if (languageId == null) {
			commerceRegionLocalizationImpl.setLanguageId("");
		}
		else {
			commerceRegionLocalizationImpl.setLanguageId(languageId);
		}

		if (name == null) {
			commerceRegionLocalizationImpl.setName("");
		}
		else {
			commerceRegionLocalizationImpl.setName(name);
		}

		commerceRegionLocalizationImpl.resetOriginalValues();

		return commerceRegionLocalizationImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		mvccVersion = objectInput.readLong();

		commerceRegionLocalizationId = objectInput.readLong();

		companyId = objectInput.readLong();

		commerceRegionId = objectInput.readLong();
		languageId = objectInput.readUTF();
		name = objectInput.readUTF();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(mvccVersion);

		objectOutput.writeLong(commerceRegionLocalizationId);

		objectOutput.writeLong(companyId);

		objectOutput.writeLong(commerceRegionId);

		if (languageId == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(languageId);
		}

		if (name == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(name);
		}
	}

	public long mvccVersion;
	public long commerceRegionLocalizationId;
	public long companyId;
	public long commerceRegionId;
	public String languageId;
	public String name;

}