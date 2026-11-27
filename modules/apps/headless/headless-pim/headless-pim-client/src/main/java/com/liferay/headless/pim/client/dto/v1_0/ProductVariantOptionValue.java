/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.pim.client.dto.v1_0;

import com.liferay.headless.pim.client.function.UnsafeSupplier;
import com.liferay.headless.pim.client.serdes.v1_0.ProductVariantOptionValueSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Stefano Motta
 * @generated
 */
@Generated("")
public class ProductVariantOptionValue implements Cloneable, Serializable {

	public static ProductVariantOptionValue toDTO(String json) {
		return ProductVariantOptionValueSerDes.toDTO(json);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setKey(UnsafeSupplier<String, Exception> keyUnsafeSupplier) {
		try {
			key = keyUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String key;

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

	@Override
	public ProductVariantOptionValue clone() throws CloneNotSupportedException {
		return (ProductVariantOptionValue)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ProductVariantOptionValue)) {
			return false;
		}

		ProductVariantOptionValue productVariantOptionValue =
			(ProductVariantOptionValue)object;

		return Objects.equals(toString(), productVariantOptionValue.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return ProductVariantOptionValueSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:599557713