/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.address.internal.odata.entity.v1_0;

import com.liferay.portal.kernel.model.CountryTable;
import com.liferay.portal.odata.entity.DateTimeEntityField;
import com.liferay.portal.odata.entity.DoubleEntityField;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.odata.entity.IntegerEntityField;
import com.liferay.portal.odata.entity.StringEntityField;

import java.util.Map;

/**
 * @author Balazs Breier
 */
public class CountryEntityModel implements EntityModel {

	public CountryEntityModel() {
		_entityFieldsMap = EntityModel.toEntityFieldsMap(
			new DateTimeEntityField(
				"dateCreated",
				locale -> CountryTable.INSTANCE.createDate.getName(),
				locale -> CountryTable.INSTANCE.createDate.getName()),
			new DateTimeEntityField(
				"dateModified",
				locale -> CountryTable.INSTANCE.modifiedDate.getName(),
				locale -> CountryTable.INSTANCE.modifiedDate.getName()),
			new DoubleEntityField(
				"position", locale -> CountryTable.INSTANCE.position.getName()),
			new IntegerEntityField(
				"id", locale -> CountryTable.INSTANCE.countryId.getName()),
			new StringEntityField(
				"name", locale -> CountryTable.INSTANCE.name.getName()));
	}

	@Override
	public Map<String, EntityField> getEntityFieldsMap() {
		return _entityFieldsMap;
	}

	private final Map<String, EntityField> _entityFieldsMap;

}