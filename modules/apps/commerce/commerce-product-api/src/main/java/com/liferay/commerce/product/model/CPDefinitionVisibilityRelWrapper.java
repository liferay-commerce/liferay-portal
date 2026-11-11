/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.model;

import com.liferay.portal.kernel.model.ModelWrapper;
import com.liferay.portal.kernel.model.wrapper.BaseModelWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * <p>
 * This class is a wrapper for {@link CPDefinitionVisibilityRel}.
 * </p>
 *
 * @author Marco Leo
 * @see CPDefinitionVisibilityRel
 * @generated
 */
public class CPDefinitionVisibilityRelWrapper
	extends BaseModelWrapper<CPDefinitionVisibilityRel>
	implements CPDefinitionVisibilityRel,
			   ModelWrapper<CPDefinitionVisibilityRel> {

	public CPDefinitionVisibilityRelWrapper(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		super(cpDefinitionVisibilityRel);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("mvccVersion", getMvccVersion());
		attributes.put("ctCollectionId", getCtCollectionId());
		attributes.put("externalReferenceCode", getExternalReferenceCode());
		attributes.put(
			"CPDefinitionVisibilityRelId", getCPDefinitionVisibilityRelId());
		attributes.put("companyId", getCompanyId());
		attributes.put("userId", getUserId());
		attributes.put("userName", getUserName());
		attributes.put("createDate", getCreateDate());
		attributes.put("modifiedDate", getModifiedDate());
		attributes.put("CPDefinitionId", getCPDefinitionId());
		attributes.put("classNameId", getClassNameId());
		attributes.put("classPK", getClassPK());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long mvccVersion = (Long)attributes.get("mvccVersion");

		if (mvccVersion != null) {
			setMvccVersion(mvccVersion);
		}

		Long ctCollectionId = (Long)attributes.get("ctCollectionId");

		if (ctCollectionId != null) {
			setCtCollectionId(ctCollectionId);
		}

		String externalReferenceCode = (String)attributes.get(
			"externalReferenceCode");

		if (externalReferenceCode != null) {
			setExternalReferenceCode(externalReferenceCode);
		}

		Long CPDefinitionVisibilityRelId = (Long)attributes.get(
			"CPDefinitionVisibilityRelId");

		if (CPDefinitionVisibilityRelId != null) {
			setCPDefinitionVisibilityRelId(CPDefinitionVisibilityRelId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Long userId = (Long)attributes.get("userId");

		if (userId != null) {
			setUserId(userId);
		}

		String userName = (String)attributes.get("userName");

		if (userName != null) {
			setUserName(userName);
		}

		Date createDate = (Date)attributes.get("createDate");

		if (createDate != null) {
			setCreateDate(createDate);
		}

		Date modifiedDate = (Date)attributes.get("modifiedDate");

		if (modifiedDate != null) {
			setModifiedDate(modifiedDate);
		}

		Long CPDefinitionId = (Long)attributes.get("CPDefinitionId");

		if (CPDefinitionId != null) {
			setCPDefinitionId(CPDefinitionId);
		}

		Long classNameId = (Long)attributes.get("classNameId");

		if (classNameId != null) {
			setClassNameId(classNameId);
		}

		Long classPK = (Long)attributes.get("classPK");

		if (classPK != null) {
			setClassPK(classPK);
		}
	}

	@Override
	public CPDefinitionVisibilityRel cloneWithOriginalValues() {
		return wrap(model.cloneWithOriginalValues());
	}

	/**
	 * Returns the fully qualified class name of this cp definition visibility rel.
	 *
	 * @return the fully qualified class name of this cp definition visibility rel
	 */
	@Override
	public String getClassName() {
		return model.getClassName();
	}

	/**
	 * Returns the class name ID of this cp definition visibility rel.
	 *
	 * @return the class name ID of this cp definition visibility rel
	 */
	@Override
	public long getClassNameId() {
		return model.getClassNameId();
	}

	/**
	 * Returns the class pk of this cp definition visibility rel.
	 *
	 * @return the class pk of this cp definition visibility rel
	 */
	@Override
	public long getClassPK() {
		return model.getClassPK();
	}

	/**
	 * Returns the company ID of this cp definition visibility rel.
	 *
	 * @return the company ID of this cp definition visibility rel
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the cp definition ID of this cp definition visibility rel.
	 *
	 * @return the cp definition ID of this cp definition visibility rel
	 */
	@Override
	public long getCPDefinitionId() {
		return model.getCPDefinitionId();
	}

	/**
	 * Returns the cp definition visibility rel ID of this cp definition visibility rel.
	 *
	 * @return the cp definition visibility rel ID of this cp definition visibility rel
	 */
	@Override
	public long getCPDefinitionVisibilityRelId() {
		return model.getCPDefinitionVisibilityRelId();
	}

	/**
	 * Returns the create date of this cp definition visibility rel.
	 *
	 * @return the create date of this cp definition visibility rel
	 */
	@Override
	public Date getCreateDate() {
		return model.getCreateDate();
	}

	/**
	 * Returns the ct collection ID of this cp definition visibility rel.
	 *
	 * @return the ct collection ID of this cp definition visibility rel
	 */
	@Override
	public long getCtCollectionId() {
		return model.getCtCollectionId();
	}

	/**
	 * Returns the external reference code of this cp definition visibility rel.
	 *
	 * @return the external reference code of this cp definition visibility rel
	 */
	@Override
	public String getExternalReferenceCode() {
		return model.getExternalReferenceCode();
	}

	/**
	 * Returns the modified date of this cp definition visibility rel.
	 *
	 * @return the modified date of this cp definition visibility rel
	 */
	@Override
	public Date getModifiedDate() {
		return model.getModifiedDate();
	}

	/**
	 * Returns the mvcc version of this cp definition visibility rel.
	 *
	 * @return the mvcc version of this cp definition visibility rel
	 */
	@Override
	public long getMvccVersion() {
		return model.getMvccVersion();
	}

	/**
	 * Returns the primary key of this cp definition visibility rel.
	 *
	 * @return the primary key of this cp definition visibility rel
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	/**
	 * Returns the user ID of this cp definition visibility rel.
	 *
	 * @return the user ID of this cp definition visibility rel
	 */
	@Override
	public long getUserId() {
		return model.getUserId();
	}

	/**
	 * Returns the user name of this cp definition visibility rel.
	 *
	 * @return the user name of this cp definition visibility rel
	 */
	@Override
	public String getUserName() {
		return model.getUserName();
	}

	/**
	 * Returns the user uuid of this cp definition visibility rel.
	 *
	 * @return the user uuid of this cp definition visibility rel
	 */
	@Override
	public String getUserUuid() {
		return model.getUserUuid();
	}

	@Override
	public void persist() {
		model.persist();
	}

	@Override
	public void setClassName(String className) {
		model.setClassName(className);
	}

	/**
	 * Sets the class name ID of this cp definition visibility rel.
	 *
	 * @param classNameId the class name ID of this cp definition visibility rel
	 */
	@Override
	public void setClassNameId(long classNameId) {
		model.setClassNameId(classNameId);
	}

	/**
	 * Sets the class pk of this cp definition visibility rel.
	 *
	 * @param classPK the class pk of this cp definition visibility rel
	 */
	@Override
	public void setClassPK(long classPK) {
		model.setClassPK(classPK);
	}

	/**
	 * Sets the company ID of this cp definition visibility rel.
	 *
	 * @param companyId the company ID of this cp definition visibility rel
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the cp definition ID of this cp definition visibility rel.
	 *
	 * @param CPDefinitionId the cp definition ID of this cp definition visibility rel
	 */
	@Override
	public void setCPDefinitionId(long CPDefinitionId) {
		model.setCPDefinitionId(CPDefinitionId);
	}

	/**
	 * Sets the cp definition visibility rel ID of this cp definition visibility rel.
	 *
	 * @param CPDefinitionVisibilityRelId the cp definition visibility rel ID of this cp definition visibility rel
	 */
	@Override
	public void setCPDefinitionVisibilityRelId(
		long CPDefinitionVisibilityRelId) {

		model.setCPDefinitionVisibilityRelId(CPDefinitionVisibilityRelId);
	}

	/**
	 * Sets the create date of this cp definition visibility rel.
	 *
	 * @param createDate the create date of this cp definition visibility rel
	 */
	@Override
	public void setCreateDate(Date createDate) {
		model.setCreateDate(createDate);
	}

	/**
	 * Sets the ct collection ID of this cp definition visibility rel.
	 *
	 * @param ctCollectionId the ct collection ID of this cp definition visibility rel
	 */
	@Override
	public void setCtCollectionId(long ctCollectionId) {
		model.setCtCollectionId(ctCollectionId);
	}

	/**
	 * Sets the external reference code of this cp definition visibility rel.
	 *
	 * @param externalReferenceCode the external reference code of this cp definition visibility rel
	 */
	@Override
	public void setExternalReferenceCode(String externalReferenceCode) {
		model.setExternalReferenceCode(externalReferenceCode);
	}

	/**
	 * Sets the modified date of this cp definition visibility rel.
	 *
	 * @param modifiedDate the modified date of this cp definition visibility rel
	 */
	@Override
	public void setModifiedDate(Date modifiedDate) {
		model.setModifiedDate(modifiedDate);
	}

	/**
	 * Sets the mvcc version of this cp definition visibility rel.
	 *
	 * @param mvccVersion the mvcc version of this cp definition visibility rel
	 */
	@Override
	public void setMvccVersion(long mvccVersion) {
		model.setMvccVersion(mvccVersion);
	}

	/**
	 * Sets the primary key of this cp definition visibility rel.
	 *
	 * @param primaryKey the primary key of this cp definition visibility rel
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	/**
	 * Sets the user ID of this cp definition visibility rel.
	 *
	 * @param userId the user ID of this cp definition visibility rel
	 */
	@Override
	public void setUserId(long userId) {
		model.setUserId(userId);
	}

	/**
	 * Sets the user name of this cp definition visibility rel.
	 *
	 * @param userName the user name of this cp definition visibility rel
	 */
	@Override
	public void setUserName(String userName) {
		model.setUserName(userName);
	}

	/**
	 * Sets the user uuid of this cp definition visibility rel.
	 *
	 * @param userUuid the user uuid of this cp definition visibility rel
	 */
	@Override
	public void setUserUuid(String userUuid) {
		model.setUserUuid(userUuid);
	}

	@Override
	public String toXmlString() {
		return model.toXmlString();
	}

	@Override
	public Map<String, Function<CPDefinitionVisibilityRel, Object>>
		getAttributeGetterFunctions() {

		return model.getAttributeGetterFunctions();
	}

	@Override
	public Map<String, BiConsumer<CPDefinitionVisibilityRel, Object>>
		getAttributeSetterBiConsumers() {

		return model.getAttributeSetterBiConsumers();
	}

	@Override
	protected CPDefinitionVisibilityRelWrapper wrap(
		CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

		return new CPDefinitionVisibilityRelWrapper(cpDefinitionVisibilityRel);
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:-1050454234