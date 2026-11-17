/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.test.util;

import com.liferay.list.type.entry.util.ListTypeEntryUtil;
import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.service.ListTypeDefinitionLocalServiceUtil;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectDefinitionSettingConstants;
import com.liferay.object.constants.ObjectLayoutBoxConstants;
import com.liferay.object.field.builder.PicklistObjectFieldBuilder;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.model.ObjectLayoutBox;
import com.liferay.object.model.ObjectLayoutColumn;
import com.liferay.object.model.ObjectLayoutRow;
import com.liferay.object.model.ObjectLayoutTab;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.object.service.ObjectDefinitionSettingLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.object.service.ObjectFolderLocalServiceUtil;
import com.liferay.object.service.ObjectLayoutLocalServiceUtil;
import com.liferay.object.service.ObjectLayoutTabLocalServiceUtil;
import com.liferay.object.service.persistence.ObjectLayoutBoxUtil;
import com.liferay.object.service.persistence.ObjectLayoutColumnUtil;
import com.liferay.object.service.persistence.ObjectLayoutRowUtil;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.site.pim.site.initializer.constants.PIMObjectFolderConstants;

import java.util.Collections;
import java.util.List;

/**
 * @author Balazs Breier
 */
public class PIMProductTypeTestUtil {

	public static void addObjectLayout(
			ObjectDefinition objectDefinition, String... objectFieldNames)
		throws Exception {

		ObjectLayoutRow objectLayoutRow = ObjectLayoutRowUtil.create(0);

		objectLayoutRow.setObjectLayoutColumns(
			TransformUtil.transformToList(
				objectFieldNames,
				objectFieldName -> {
					ObjectLayoutColumn objectLayoutColumn =
						ObjectLayoutColumnUtil.create(0);

					ObjectField objectField =
						ObjectFieldLocalServiceUtil.getObjectField(
							objectDefinition.getObjectDefinitionId(),
							objectFieldName);

					objectLayoutColumn.setObjectFieldId(
						objectField.getObjectFieldId());

					objectLayoutColumn.setSize(12);

					return objectLayoutColumn;
				}));

		ObjectLayoutBox objectLayoutBox = ObjectLayoutBoxUtil.create(0);

		objectLayoutBox.setNameMap(RandomTestUtil.randomLocaleStringMap());
		objectLayoutBox.setType(ObjectLayoutBoxConstants.TYPE_REGULAR);
		objectLayoutBox.setObjectLayoutRows(
			Collections.singletonList(objectLayoutRow));

		ObjectLayoutTab objectLayoutTab =
			ObjectLayoutTabLocalServiceUtil.createObjectLayoutTab(0);

		objectLayoutTab.setNameMap(RandomTestUtil.randomLocaleStringMap());
		objectLayoutTab.setObjectLayoutBoxes(
			Collections.singletonList(objectLayoutBox));

		ObjectLayoutLocalServiceUtil.addObjectLayout(
			TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(), true,
			RandomTestUtil.randomLocaleStringMap(),
			Collections.singletonList(objectLayoutTab));
	}

	public static ObjectDefinition addProductTypeObjectDefinition(
			List<ObjectField> objectFields)
		throws Exception {

		ObjectFolder objectFolder =
			ObjectFolderLocalServiceUtil.getObjectFolderByExternalReferenceCode(
				PIMObjectFolderConstants.EXTERNAL_REFERENCE_CODE_PRODUCT_TYPES,
				TestPropsValues.getCompanyId());

		ObjectDefinition objectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition(
				true, false, false, ObjectDefinitionTestUtil.getRandomName(),
				objectFields, objectFolder.getObjectFolderId(),
				ObjectDefinitionConstants.SCOPE_DEPOT,
				TestPropsValues.getUserId());

		ObjectDefinitionSettingLocalServiceUtil.addObjectDefinitionSetting(
			TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			ObjectDefinitionSettingConstants.NAME_ACCEPT_ALL_GROUPS,
			Boolean.TRUE.toString());

		ObjectField objectField = ObjectFieldLocalServiceUtil.fetchObjectField(
			objectDefinition.getObjectDefinitionId(), "title");

		if (objectField == null) {
			return objectDefinition;
		}

		return ObjectDefinitionLocalServiceUtil.updateTitleObjectFieldId(
			objectDefinition.getObjectDefinitionId(),
			objectField.getObjectFieldId());
	}

	public static ObjectField createPicklistObjectField(
			String name, String... keys)
		throws Exception {

		ListTypeDefinition listTypeDefinition =
			ListTypeDefinitionLocalServiceUtil.addListTypeDefinition(
				null, TestPropsValues.getUserId(),
				RandomTestUtil.randomLocaleStringMap(), false,
				TransformUtil.transformToList(
					keys, ListTypeEntryUtil::createListTypeEntry),
				new ServiceContext());

		return new PicklistObjectFieldBuilder(
		).labelMap(
			Collections.singletonMap(LocaleUtil.getDefault(), name)
		).listTypeDefinitionId(
			listTypeDefinition.getListTypeDefinitionId()
		).name(
			name
		).build();
	}

}