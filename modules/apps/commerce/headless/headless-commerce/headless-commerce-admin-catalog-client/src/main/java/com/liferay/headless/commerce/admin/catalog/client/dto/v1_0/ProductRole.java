/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.client.dto.v1_0;

import com.liferay.headless.commerce.admin.catalog.client.function.UnsafeSupplier;
import com.liferay.headless.commerce.admin.catalog.client.serdes.v1_0.ProductRoleSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Zoltán Takács
 * @generated
 */
@Generated("")
public class ProductRole implements Cloneable, Serializable {

	public static ProductRole toDTO(String json) {
		return ProductRoleSerDes.toDTO(json);
	}

	public String getExternalReferenceCode() {
		return externalReferenceCode;
	}

	public void setExternalReferenceCode(String externalReferenceCode) {
		this.externalReferenceCode = externalReferenceCode;
	}

	public void setExternalReferenceCode(
		UnsafeSupplier<String, Exception> externalReferenceCodeUnsafeSupplier) {

		try {
			externalReferenceCode = externalReferenceCodeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String externalReferenceCode;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(UnsafeSupplier<Long, Exception> idUnsafeSupplier) {
		try {
			id = idUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setName(UnsafeSupplier<String, Exception> nameUnsafeSupplier) {
		try {
			name = nameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String name;

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public void setRoleId(
		UnsafeSupplier<Long, Exception> roleIdUnsafeSupplier) {

		try {
			roleId = roleIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long roleId;

	public RoleType getRoleType() {
		return roleType;
	}

	public String getRoleTypeAsString() {
		if (roleType == null) {
			return null;
		}

		return roleType.toString();
	}

	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}

	public void setRoleType(
		UnsafeSupplier<RoleType, Exception> roleTypeUnsafeSupplier) {

		try {
			roleType = roleTypeUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected RoleType roleType;

	@Override
	public ProductRole clone() throws CloneNotSupportedException {
		return (ProductRole)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ProductRole)) {
			return false;
		}

		ProductRole productRole = (ProductRole)object;

		return Objects.equals(toString(), productRole.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return ProductRoleSerDes.toJSON(this);
	}

	public static enum RoleType {

		ACCOUNT("account"), REGULAR("regular");

		public static RoleType create(String value) {
			for (RoleType roleType : values()) {
				if (Objects.equals(roleType.getValue(), value) ||
					Objects.equals(roleType.name(), value)) {

					return roleType;
				}
			}

			return null;
		}

		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return _value;
		}

		private RoleType(String value) {
			_value = value;
		}

		private final String _value;

	}

}
// LIFERAY-REST-BUILDER-HASH:-1773607649