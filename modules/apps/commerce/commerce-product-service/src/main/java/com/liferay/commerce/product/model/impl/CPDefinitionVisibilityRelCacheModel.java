/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.model.impl;

import com.liferay.commerce.product.model.CPDefinitionVisibilityRel;
import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.MVCCModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Date;

/**
 * The cache model class for representing CPDefinitionVisibilityRel in entity cache.
 *
 * @author Marco Leo
 * @generated
 */
public class CPDefinitionVisibilityRelCacheModel
	implements CacheModel<CPDefinitionVisibilityRel>, Externalizable,
			   MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof CPDefinitionVisibilityRelCacheModel)) {
			return false;
		}

		CPDefinitionVisibilityRelCacheModel
			cpDefinitionVisibilityRelCacheModel =
				(CPDefinitionVisibilityRelCacheModel)object;

		if ((CPDefinitionVisibilityRelId ==
				cpDefinitionVisibilityRelCacheModel.
					CPDefinitionVisibilityRelId) &&
			(mvccVersion == cpDefinitionVisibilityRelCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, CPDefinitionVisibilityRelId);

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
		StringBundler sb = new StringBundler(25);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", ctCollectionId=");
		sb.append(ctCollectionId);
		sb.append(", externalReferenceCode=");
		sb.append(externalReferenceCode);
		sb.append(", CPDefinitionVisibilityRelId=");
		sb.append(CPDefinitionVisibilityRelId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", userId=");
		sb.append(userId);
		sb.append(", userName=");
		sb.append(userName);
		sb.append(", createDate=");
		sb.append(createDate);
		sb.append(", modifiedDate=");
		sb.append(modifiedDate);
		sb.append(", CPDefinitionId=");
		sb.append(CPDefinitionId);
		sb.append(", classNameId=");
		sb.append(classNameId);
		sb.append(", classPK=");
		sb.append(classPK);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public CPDefinitionVisibilityRel toEntityModel() {
		CPDefinitionVisibilityRelImpl cpDefinitionVisibilityRelImpl =
			new CPDefinitionVisibilityRelImpl();

		cpDefinitionVisibilityRelImpl.setMvccVersion(mvccVersion);
		cpDefinitionVisibilityRelImpl.setCtCollectionId(ctCollectionId);

		if (externalReferenceCode == null) {
			cpDefinitionVisibilityRelImpl.setExternalReferenceCode("");
		}
		else {
			cpDefinitionVisibilityRelImpl.setExternalReferenceCode(
				externalReferenceCode);
		}

		cpDefinitionVisibilityRelImpl.setCPDefinitionVisibilityRelId(
			CPDefinitionVisibilityRelId);
		cpDefinitionVisibilityRelImpl.setCompanyId(companyId);
		cpDefinitionVisibilityRelImpl.setUserId(userId);

		if (userName == null) {
			cpDefinitionVisibilityRelImpl.setUserName("");
		}
		else {
			cpDefinitionVisibilityRelImpl.setUserName(userName);
		}

		if (createDate == Long.MIN_VALUE) {
			cpDefinitionVisibilityRelImpl.setCreateDate(null);
		}
		else {
			cpDefinitionVisibilityRelImpl.setCreateDate(new Date(createDate));
		}

		if (modifiedDate == Long.MIN_VALUE) {
			cpDefinitionVisibilityRelImpl.setModifiedDate(null);
		}
		else {
			cpDefinitionVisibilityRelImpl.setModifiedDate(
				new Date(modifiedDate));
		}

		cpDefinitionVisibilityRelImpl.setCPDefinitionId(CPDefinitionId);
		cpDefinitionVisibilityRelImpl.setClassNameId(classNameId);
		cpDefinitionVisibilityRelImpl.setClassPK(classPK);

		cpDefinitionVisibilityRelImpl.resetOriginalValues();

		return cpDefinitionVisibilityRelImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		mvccVersion = objectInput.readLong();

		ctCollectionId = objectInput.readLong();
		externalReferenceCode = objectInput.readUTF();

		CPDefinitionVisibilityRelId = objectInput.readLong();

		companyId = objectInput.readLong();

		userId = objectInput.readLong();
		userName = objectInput.readUTF();
		createDate = objectInput.readLong();
		modifiedDate = objectInput.readLong();

		CPDefinitionId = objectInput.readLong();

		classNameId = objectInput.readLong();

		classPK = objectInput.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(mvccVersion);

		objectOutput.writeLong(ctCollectionId);

		if (externalReferenceCode == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(externalReferenceCode);
		}

		objectOutput.writeLong(CPDefinitionVisibilityRelId);

		objectOutput.writeLong(companyId);

		objectOutput.writeLong(userId);

		if (userName == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(userName);
		}

		objectOutput.writeLong(createDate);
		objectOutput.writeLong(modifiedDate);

		objectOutput.writeLong(CPDefinitionId);

		objectOutput.writeLong(classNameId);

		objectOutput.writeLong(classPK);
	}

	public long mvccVersion;
	public long ctCollectionId;
	public String externalReferenceCode;
	public long CPDefinitionVisibilityRelId;
	public long companyId;
	public long userId;
	public String userName;
	public long createDate;
	public long modifiedDate;
	public long CPDefinitionId;
	public long classNameId;
	public long classPK;

}
// LIFERAY-SERVICE-BUILDER-HASH:430320228