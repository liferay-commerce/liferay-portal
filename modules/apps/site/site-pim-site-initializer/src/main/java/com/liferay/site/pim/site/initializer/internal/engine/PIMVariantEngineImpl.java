/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.engine;

import com.liferay.list.type.service.ListTypeEntryLocalService;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectLayout;
import com.liferay.object.model.ObjectLayoutBox;
import com.liferay.object.model.ObjectLayoutColumn;
import com.liferay.object.model.ObjectLayoutRow;
import com.liferay.object.model.ObjectLayoutTab;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectLayoutLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.site.pim.site.initializer.engine.PIMVariantEngine;
import com.liferay.site.pim.site.initializer.internal.util.PIMLinkUtil;
import com.liferay.site.pim.site.initializer.variant.PIMVariantOption;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Balazs Breier
 */
@Component(service = PIMVariantEngine.class)
public class PIMVariantEngineImpl implements PIMVariantEngine {

	@Override
	public List<PIMVariantOption> getPIMVariantOptions(ObjectEntry objectEntry)
		throws PortalException {

		PIMLinkUtil.checkPermission(objectEntry, ActionKeys.VIEW);

		Map<String, Serializable> values = objectEntry.getValues();

		return TransformUtil.transform(
			_getObjectFields(objectEntry.getObjectDefinitionId()),
			objectField -> new PIMVariantOption(
				ListUtil.filter(
					_listTypeEntryLocalService.getListTypeEntries(
						objectField.getListTypeDefinitionId()),
					listTypeEntry ->
						listTypeEntry.getStatus() ==
							WorkflowConstants.STATUS_APPROVED),
				objectField, MapUtil.getString(values, objectField.getName())));
	}

	private List<Long> _getObjectFieldIds(long objectDefinitionId) {
		List<Long> objectFieldIds = new ArrayList<>();

		List<ObjectLayout> objectLayouts =
			_objectLayoutLocalService.getObjectLayouts(objectDefinitionId);

		if (objectLayouts.isEmpty()) {
			return objectFieldIds;
		}

		ObjectLayout objectLayout = objectLayouts.get(0);

		for (ObjectLayoutTab objectLayoutTab :
				objectLayout.getObjectLayoutTabs()) {

			for (ObjectLayoutBox objectLayoutBox :
					objectLayoutTab.getObjectLayoutBoxes()) {

				for (ObjectLayoutRow objectLayoutRow :
						objectLayoutBox.getObjectLayoutRows()) {

					for (ObjectLayoutColumn objectLayoutColumn :
							objectLayoutRow.getObjectLayoutColumns()) {

						objectFieldIds.add(
							objectLayoutColumn.getObjectFieldId());
					}
				}
			}
		}

		return objectFieldIds;
	}

	private List<ObjectField> _getObjectFields(long objectDefinitionId) {
		List<Long> objectFieldIds = _getObjectFieldIds(objectDefinitionId);

		return ListUtil.sort(
			ListUtil.filter(
				_objectFieldLocalService.getObjectFields(objectDefinitionId),
				objectField -> Objects.equals(
					objectField.getBusinessType(),
					ObjectFieldConstants.BUSINESS_TYPE_PICKLIST)),
			Comparator.comparing(
				(ObjectField objectField) -> {
					int index = objectFieldIds.indexOf(
						objectField.getObjectFieldId());

					if (index < 0) {
						return Integer.MAX_VALUE;
					}

					return index;
				}
			).thenComparing(
				ObjectField::getName
			));
	}

	@Reference
	private ListTypeEntryLocalService _listTypeEntryLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private ObjectLayoutLocalService _objectLayoutLocalService;

}