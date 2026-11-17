/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.bulk.rest.client.dto.v1_0;

import com.liferay.bulk.rest.client.function.UnsafeSupplier;
import com.liferay.bulk.rest.client.serdes.v1_0.GenerateVariantsBulkSelectionActionSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Alejandro Tardín
 * @generated
 */
@Generated("")
public class GenerateVariantsBulkSelectionAction
	extends BulkAction implements Cloneable, Serializable {

	public static GenerateVariantsBulkSelectionAction toDTO(String json) {
		return GenerateVariantsBulkSelectionActionSerDes.toDTO(json);
	}

	public String[] getObjectFieldNames() {
		return objectFieldNames;
	}

	public void setObjectFieldNames(String[] objectFieldNames) {
		this.objectFieldNames = objectFieldNames;
	}

	public void setObjectFieldNames(
		UnsafeSupplier<String[], Exception> objectFieldNamesUnsafeSupplier) {

		try {
			objectFieldNames = objectFieldNamesUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String[] objectFieldNames;

	@Override
	public GenerateVariantsBulkSelectionAction clone()
		throws CloneNotSupportedException {

		return (GenerateVariantsBulkSelectionAction)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof GenerateVariantsBulkSelectionAction)) {
			return false;
		}

		GenerateVariantsBulkSelectionAction
			generateVariantsBulkSelectionAction =
				(GenerateVariantsBulkSelectionAction)object;

		return Objects.equals(
			toString(), generateVariantsBulkSelectionAction.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return GenerateVariantsBulkSelectionActionSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:-1123229771