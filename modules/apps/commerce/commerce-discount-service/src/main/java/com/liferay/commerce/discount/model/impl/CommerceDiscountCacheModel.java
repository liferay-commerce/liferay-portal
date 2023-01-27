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

package com.liferay.commerce.discount.model.impl;

import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.MVCCModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.math.BigDecimal;

import java.util.Date;

/**
 * The cache model class for representing CommerceDiscount in entity cache.
 *
 * @author Marco Leo
 * @generated
 */
public class CommerceDiscountCacheModel
	implements CacheModel<CommerceDiscount>, Externalizable, MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof CommerceDiscountCacheModel)) {
			return false;
		}

		CommerceDiscountCacheModel commerceDiscountCacheModel =
			(CommerceDiscountCacheModel)object;

		if ((commerceDiscountId ==
				commerceDiscountCacheModel.commerceDiscountId) &&
			(mvccVersion == commerceDiscountCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, commerceDiscountId);

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
		StringBundler sb = new StringBundler(69);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", uuid=");
		sb.append(uuid);
		sb.append(", externalReferenceCode=");
		sb.append(externalReferenceCode);
		sb.append(", commerceDiscountId=");
		sb.append(commerceDiscountId);
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
		sb.append(", active=");
		sb.append(active);
		sb.append(", commerceCurrencyCode=");
		sb.append(commerceCurrencyCode);
		sb.append(", couponCode=");
		sb.append(couponCode);
		sb.append(", displayDate=");
		sb.append(displayDate);
		sb.append(", expirationDate=");
		sb.append(expirationDate);
		sb.append(", level=");
		sb.append(level);
		sb.append(", level1=");
		sb.append(level1);
		sb.append(", level2=");
		sb.append(level2);
		sb.append(", level3=");
		sb.append(level3);
		sb.append(", level4=");
		sb.append(level4);
		sb.append(", limitationTimes=");
		sb.append(limitationTimes);
		sb.append(", limitationTimesPerAccount=");
		sb.append(limitationTimesPerAccount);
		sb.append(", limitationType=");
		sb.append(limitationType);
		sb.append(", maximumDiscountAmount=");
		sb.append(maximumDiscountAmount);
		sb.append(", numberOfUse=");
		sb.append(numberOfUse);
		sb.append(", rulesConjunction=");
		sb.append(rulesConjunction);
		sb.append(", target=");
		sb.append(target);
		sb.append(", title=");
		sb.append(title);
		sb.append(", useCouponCode=");
		sb.append(useCouponCode);
		sb.append(", usePercentage=");
		sb.append(usePercentage);
		sb.append(", lastPublishDate=");
		sb.append(lastPublishDate);
		sb.append(", status=");
		sb.append(status);
		sb.append(", statusByUserId=");
		sb.append(statusByUserId);
		sb.append(", statusByUserName=");
		sb.append(statusByUserName);
		sb.append(", statusDate=");
		sb.append(statusDate);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public CommerceDiscount toEntityModel() {
		CommerceDiscountImpl commerceDiscountImpl = new CommerceDiscountImpl();

		commerceDiscountImpl.setMvccVersion(mvccVersion);

		if (uuid == null) {
			commerceDiscountImpl.setUuid("");
		}
		else {
			commerceDiscountImpl.setUuid(uuid);
		}

		if (externalReferenceCode == null) {
			commerceDiscountImpl.setExternalReferenceCode("");
		}
		else {
			commerceDiscountImpl.setExternalReferenceCode(
				externalReferenceCode);
		}

		commerceDiscountImpl.setCommerceDiscountId(commerceDiscountId);
		commerceDiscountImpl.setCompanyId(companyId);
		commerceDiscountImpl.setUserId(userId);

		if (userName == null) {
			commerceDiscountImpl.setUserName("");
		}
		else {
			commerceDiscountImpl.setUserName(userName);
		}

		if (createDate == Long.MIN_VALUE) {
			commerceDiscountImpl.setCreateDate(null);
		}
		else {
			commerceDiscountImpl.setCreateDate(new Date(createDate));
		}

		if (modifiedDate == Long.MIN_VALUE) {
			commerceDiscountImpl.setModifiedDate(null);
		}
		else {
			commerceDiscountImpl.setModifiedDate(new Date(modifiedDate));
		}

		commerceDiscountImpl.setActive(active);

		if (commerceCurrencyCode == null) {
			commerceDiscountImpl.setCommerceCurrencyCode("");
		}
		else {
			commerceDiscountImpl.setCommerceCurrencyCode(commerceCurrencyCode);
		}

		if (couponCode == null) {
			commerceDiscountImpl.setCouponCode("");
		}
		else {
			commerceDiscountImpl.setCouponCode(couponCode);
		}

		if (displayDate == Long.MIN_VALUE) {
			commerceDiscountImpl.setDisplayDate(null);
		}
		else {
			commerceDiscountImpl.setDisplayDate(new Date(displayDate));
		}

		if (expirationDate == Long.MIN_VALUE) {
			commerceDiscountImpl.setExpirationDate(null);
		}
		else {
			commerceDiscountImpl.setExpirationDate(new Date(expirationDate));
		}

		if (level == null) {
			commerceDiscountImpl.setLevel("");
		}
		else {
			commerceDiscountImpl.setLevel(level);
		}

		commerceDiscountImpl.setLevel1(level1);
		commerceDiscountImpl.setLevel2(level2);
		commerceDiscountImpl.setLevel3(level3);
		commerceDiscountImpl.setLevel4(level4);
		commerceDiscountImpl.setLimitationTimes(limitationTimes);
		commerceDiscountImpl.setLimitationTimesPerAccount(
			limitationTimesPerAccount);

		if (limitationType == null) {
			commerceDiscountImpl.setLimitationType("");
		}
		else {
			commerceDiscountImpl.setLimitationType(limitationType);
		}

		commerceDiscountImpl.setMaximumDiscountAmount(maximumDiscountAmount);
		commerceDiscountImpl.setNumberOfUse(numberOfUse);
		commerceDiscountImpl.setRulesConjunction(rulesConjunction);

		if (target == null) {
			commerceDiscountImpl.setTarget("");
		}
		else {
			commerceDiscountImpl.setTarget(target);
		}

		if (title == null) {
			commerceDiscountImpl.setTitle("");
		}
		else {
			commerceDiscountImpl.setTitle(title);
		}

		commerceDiscountImpl.setUseCouponCode(useCouponCode);
		commerceDiscountImpl.setUsePercentage(usePercentage);

		if (lastPublishDate == Long.MIN_VALUE) {
			commerceDiscountImpl.setLastPublishDate(null);
		}
		else {
			commerceDiscountImpl.setLastPublishDate(new Date(lastPublishDate));
		}

		commerceDiscountImpl.setStatus(status);
		commerceDiscountImpl.setStatusByUserId(statusByUserId);

		if (statusByUserName == null) {
			commerceDiscountImpl.setStatusByUserName("");
		}
		else {
			commerceDiscountImpl.setStatusByUserName(statusByUserName);
		}

		if (statusDate == Long.MIN_VALUE) {
			commerceDiscountImpl.setStatusDate(null);
		}
		else {
			commerceDiscountImpl.setStatusDate(new Date(statusDate));
		}

		commerceDiscountImpl.resetOriginalValues();

		return commerceDiscountImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput)
		throws ClassNotFoundException, IOException {

		mvccVersion = objectInput.readLong();
		uuid = objectInput.readUTF();
		externalReferenceCode = objectInput.readUTF();

		commerceDiscountId = objectInput.readLong();

		companyId = objectInput.readLong();

		userId = objectInput.readLong();
		userName = objectInput.readUTF();
		createDate = objectInput.readLong();
		modifiedDate = objectInput.readLong();

		active = objectInput.readBoolean();
		commerceCurrencyCode = objectInput.readUTF();
		couponCode = objectInput.readUTF();
		displayDate = objectInput.readLong();
		expirationDate = objectInput.readLong();
		level = objectInput.readUTF();
		level1 = (BigDecimal)objectInput.readObject();
		level2 = (BigDecimal)objectInput.readObject();
		level3 = (BigDecimal)objectInput.readObject();
		level4 = (BigDecimal)objectInput.readObject();

		limitationTimes = objectInput.readInt();

		limitationTimesPerAccount = objectInput.readInt();
		limitationType = objectInput.readUTF();
		maximumDiscountAmount = (BigDecimal)objectInput.readObject();

		numberOfUse = objectInput.readInt();

		rulesConjunction = objectInput.readBoolean();
		target = objectInput.readUTF();
		title = objectInput.readUTF();

		useCouponCode = objectInput.readBoolean();

		usePercentage = objectInput.readBoolean();
		lastPublishDate = objectInput.readLong();

		status = objectInput.readInt();

		statusByUserId = objectInput.readLong();
		statusByUserName = objectInput.readUTF();
		statusDate = objectInput.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(mvccVersion);

		if (uuid == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(uuid);
		}

		if (externalReferenceCode == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(externalReferenceCode);
		}

		objectOutput.writeLong(commerceDiscountId);

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

		objectOutput.writeBoolean(active);

		if (commerceCurrencyCode == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(commerceCurrencyCode);
		}

		if (couponCode == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(couponCode);
		}

		objectOutput.writeLong(displayDate);
		objectOutput.writeLong(expirationDate);

		if (level == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(level);
		}

		objectOutput.writeObject(level1);
		objectOutput.writeObject(level2);
		objectOutput.writeObject(level3);
		objectOutput.writeObject(level4);

		objectOutput.writeInt(limitationTimes);

		objectOutput.writeInt(limitationTimesPerAccount);

		if (limitationType == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(limitationType);
		}

		objectOutput.writeObject(maximumDiscountAmount);

		objectOutput.writeInt(numberOfUse);

		objectOutput.writeBoolean(rulesConjunction);

		if (target == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(target);
		}

		if (title == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(title);
		}

		objectOutput.writeBoolean(useCouponCode);

		objectOutput.writeBoolean(usePercentage);
		objectOutput.writeLong(lastPublishDate);

		objectOutput.writeInt(status);

		objectOutput.writeLong(statusByUserId);

		if (statusByUserName == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(statusByUserName);
		}

		objectOutput.writeLong(statusDate);
	}

	public long mvccVersion;
	public String uuid;
	public String externalReferenceCode;
	public long commerceDiscountId;
	public long companyId;
	public long userId;
	public String userName;
	public long createDate;
	public long modifiedDate;
	public boolean active;
	public String commerceCurrencyCode;
	public String couponCode;
	public long displayDate;
	public long expirationDate;
	public String level;
	public BigDecimal level1;
	public BigDecimal level2;
	public BigDecimal level3;
	public BigDecimal level4;
	public int limitationTimes;
	public int limitationTimesPerAccount;
	public String limitationType;
	public BigDecimal maximumDiscountAmount;
	public int numberOfUse;
	public boolean rulesConjunction;
	public String target;
	public String title;
	public boolean useCouponCode;
	public boolean usePercentage;
	public long lastPublishDate;
	public int status;
	public long statusByUserId;
	public String statusByUserName;
	public long statusDate;

}