/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.bulk.rest.client.dto.v1_0;

import com.liferay.bulk.rest.client.function.UnsafeSupplier;
import com.liferay.bulk.rest.client.serdes.v1_0.GeneratePIMProductVariantsBulkSelectionActionSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Alejandro Tardín
 * @generated
 */
@Generated("")
public class GeneratePIMProductVariantsBulkSelectionAction
	extends BulkAction implements Cloneable, Serializable {

	public static GeneratePIMProductVariantsBulkSelectionAction toDTO(
		String json) {

		return GeneratePIMProductVariantsBulkSelectionActionSerDes.toDTO(json);
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
	public GeneratePIMProductVariantsBulkSelectionAction clone()
		throws CloneNotSupportedException {

		return (GeneratePIMProductVariantsBulkSelectionAction)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof
				GeneratePIMProductVariantsBulkSelectionAction)) {

			return false;
		}

		GeneratePIMProductVariantsBulkSelectionAction
			generatePIMProductVariantsBulkSelectionAction =
				(GeneratePIMProductVariantsBulkSelectionAction)object;

		return Objects.equals(
			toString(),
			generatePIMProductVariantsBulkSelectionAction.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return GeneratePIMProductVariantsBulkSelectionActionSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:1987780070