/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.pim.client.dto.v1_0;

import com.liferay.headless.pim.client.function.UnsafeSupplier;
import com.liferay.headless.pim.client.serdes.v1_0.VariantOptionSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Stefano Motta
 * @generated
 */
@Generated("")
public class VariantOption implements Cloneable, Serializable {

	public static VariantOption toDTO(String json) {
		return VariantOptionSerDes.toDTO(json);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLabel(
		UnsafeSupplier<String, Exception> labelUnsafeSupplier) {

		try {
			label = labelUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String label;

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setValue(
		UnsafeSupplier<String, Exception> valueUnsafeSupplier) {

		try {
			value = valueUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String value;

	public VariantOptionValue[] getVariantOptionValues() {
		return variantOptionValues;
	}

	public void setVariantOptionValues(
		VariantOptionValue[] variantOptionValues) {

		this.variantOptionValues = variantOptionValues;
	}

	public void setVariantOptionValues(
		UnsafeSupplier<VariantOptionValue[], Exception>
			variantOptionValuesUnsafeSupplier) {

		try {
			variantOptionValues = variantOptionValuesUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected VariantOptionValue[] variantOptionValues;

	@Override
	public VariantOption clone() throws CloneNotSupportedException {
		return (VariantOption)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof VariantOption)) {
			return false;
		}

		VariantOption variantOption = (VariantOption)object;

		return Objects.equals(toString(), variantOption.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return VariantOptionSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:-992517392