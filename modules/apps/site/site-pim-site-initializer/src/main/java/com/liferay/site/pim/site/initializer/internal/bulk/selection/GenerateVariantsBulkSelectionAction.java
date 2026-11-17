/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.bulk.selection;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.bulk.selection.BulkSelection;
import com.liferay.bulk.selection.BulkSelectionAction;
import com.liferay.bulk.selection.BulkSelectionFactory;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.object.exception.NoSuchObjectEntryException;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.site.cms.site.initializer.bulk.selection.BaseObjectBulkSelectionAction;
import com.liferay.site.pim.site.initializer.engine.PIMLinkEngine;
import com.liferay.site.pim.site.initializer.engine.PIMVariantEngine;
import com.liferay.site.pim.site.initializer.internal.link.VariantPIMLinkType;
import com.liferay.site.pim.site.initializer.internal.util.PIMVariantUtil;
import com.liferay.site.pim.site.initializer.link.PIMLinkRelatedEntry;
import com.liferay.site.pim.site.initializer.variant.PIMVariantOption;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Balazs Breier
 */
@Component(
	property = "bulk.selection.action.key=generate.variants",
	service = BulkSelectionAction.class
)
public class GenerateVariantsBulkSelectionAction
	extends BaseObjectBulkSelectionAction {

	@Override
	public void execute(
			User user, BulkSelection<Object> bulkSelection,
			Map<String, Serializable> inputMap)
		throws Exception {

		List<Object> objects = null;

		try {
			objects = _getObjects(bulkSelection, user.getCompanyId(), inputMap);

			partialUpdateObjectEntry(
				user.getUserId(),
				objectEntryLocalService.getObjectEntry(
					GetterUtil.getLong(inputMap.get("bulkActionTaskId"))),
				HashMapBuilder.<String, Serializable>put(
					"numberOfItems", objects.size()
				).build());
		}
		catch (Exception exception) {
			objects = Collections.singletonList(exception);
		}

		super.execute(user, new VariantBulkSelection(objects), inputMap);
	}

	@Override
	protected void doExecute(
			User user, Map<String, Serializable> inputMap, Object object)
		throws Exception {

		if (object instanceof Exception) {
			throw (Exception)object;
		}

		if (object instanceof ObjectEntry) {
			return;
		}

		Variant variant = (Variant)object;

		ObjectEntry objectEntry = variant._objectEntry;

		Map<String, Serializable> values = new HashMap<>(
			objectEntry.getValues());

		for (Map.Entry<String, String> entry : variant._values.entrySet()) {
			_putValue(entry.getKey(), entry.getValue(), values);
		}

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(user.getCompanyId());
		serviceContext.setScopeGroupId(objectEntry.getGroupId());
		serviceContext.setUserId(user.getUserId());

		_pimLinkEngine.addPIMLinks(
			objectEntry,
			Collections.singletonList(
				_objectEntryService.copyObjectEntry(
					objectEntry.getObjectEntryId(),
					objectEntry.getObjectEntryFolderId(), values,
					serviceContext)),
			VariantPIMLinkType.TYPE);
	}

	private List<String> _getKeys(
		List<PIMVariantOption> pimVariantOptions,
		Map<String, Serializable> values) {

		List<String> keys = new ArrayList<>();

		for (PIMVariantOption pimVariantOption : pimVariantOptions) {
			ObjectField objectField = pimVariantOption.getObjectField();

			String key = MapUtil.getString(values, objectField.getName());

			if (Validator.isNull(key)) {
				return null;
			}

			keys.add(key);
		}

		return keys;
	}

	private List<Object> _getObjects(
			BulkSelection<Object> bulkSelection, long companyId,
			Map<String, Serializable> inputMap)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-96666")) {
			throw new UnsupportedOperationException();
		}

		List<ObjectEntry> objectEntries = new ArrayList<>();

		bulkSelection.forEach(object -> objectEntries.add((ObjectEntry)object));

		ObjectEntry objectEntry = objectEntries.get(0);

		if (objectEntry.isInTrash()) {
			throw new NoSuchObjectEntryException(
				StringBundler.concat(
					"No ObjectEntry exists with the key {objectEntryId=",
					objectEntry.getObjectEntryId(), ", status=",
					WorkflowConstants.STATUS_IN_TRASH, "}"));
		}

		List<String> objectFieldNames = ListUtil.fromArray(
			GetterUtil.getStringValues(inputMap.get("objectFieldNames")));

		List<PIMVariantOption> pimVariantOptions = ListUtil.filter(
			_pimVariantEngine.getPIMVariantOptions(objectEntry),
			pimVariantOption -> {
				ObjectField objectField = pimVariantOption.getObjectField();

				return objectFieldNames.contains(objectField.getName());
			});

		if (objectFieldNames.isEmpty() ||
			(objectFieldNames.size() != pimVariantOptions.size())) {

			throw new IllegalArgumentException(
				"Invalid object field names " + objectFieldNames);
		}

		List<Object> objects = new ArrayList<>();

		Set<String> skus = new HashSet<>();
		Map<List<String>, ObjectEntry> variantObjectEntries = new HashMap<>();

		for (PIMLinkRelatedEntry pimLinkRelatedEntry :
				_pimLinkEngine.getPIMLinkRelatedEntries(
					StringBundler.concat(
						"type eq '", VariantPIMLinkType.TYPE, "'"),
					objectEntry)) {

			ObjectEntry variantObjectEntry =
				pimLinkRelatedEntry.getObjectEntry();

			Map<String, Serializable> values = variantObjectEntry.getValues();

			List<String> keys = _getKeys(pimVariantOptions, values);

			if (keys != null) {
				variantObjectEntries.put(keys, variantObjectEntry);
			}

			skus.add(MapUtil.getString(values, "sku"));
		}

		List<String> keys = _getKeys(
			pimVariantOptions, objectEntry.getValues());
		Locale locale = PortalUtil.getSiteDefaultLocale(
			objectEntry.getGroupId());

		for (List<ListTypeEntry> listTypeEntries :
				PIMVariantUtil.getCombinations(
					TransformUtil.transform(
						pimVariantOptions,
						PIMVariantOption::getListTypeEntries))) {

			List<String> combinationKeys = TransformUtil.transform(
				listTypeEntries, ListTypeEntry::getKey);

			if (combinationKeys.equals(keys)) {
				continue;
			}

			ObjectEntry variantObjectEntry = variantObjectEntries.get(
				combinationKeys);

			if (variantObjectEntry != null) {
				objects.add(variantObjectEntry);

				continue;
			}

			Map<String, String> values = new LinkedHashMap<>();

			for (int i = 0; i < pimVariantOptions.size(); i++) {
				PIMVariantOption pimVariantOption = pimVariantOptions.get(i);

				ObjectField objectField = pimVariantOption.getObjectField();

				values.put(objectField.getName(), combinationKeys.get(i));
			}

			String sku = PIMVariantUtil.getSKU(
				TransformUtil.transform(
					listTypeEntries,
					listTypeEntry -> listTypeEntry.getName(locale)),
				skus, objectEntry.getTitleValue());

			skus.add(sku);

			values.put("sku", sku);

			objects.add(new Variant(objectEntry, values));
		}

		return objects;
	}

	private void _putValue(
		String name, String value, Map<String, Serializable> values) {

		values.put(name, value);

		Map<String, Serializable> localizedValues =
			(Map<String, Serializable>)values.get(name + "_i18n");

		if (localizedValues == null) {
			return;
		}

		HashMap<String, Serializable> newLocalizedValues = new HashMap<>();

		for (String languageId : localizedValues.keySet()) {
			newLocalizedValues.put(languageId, value);
		}

		values.put(name + "_i18n", newLocalizedValues);
	}

	@Reference
	private ObjectEntryService _objectEntryService;

	@Reference
	private PIMLinkEngine _pimLinkEngine;

	@Reference
	private PIMVariantEngine _pimVariantEngine;

	private static class Variant {

		private Variant(ObjectEntry objectEntry, Map<String, String> values) {
			_objectEntry = objectEntry;
			_values = values;
		}

		private final ObjectEntry _objectEntry;
		private final Map<String, String> _values;

	}

	private static class VariantBulkSelection implements BulkSelection<Object> {

		@Override
		public <E extends PortalException> void forEach(
				UnsafeConsumer<Object, E> unsafeConsumer)
			throws PortalException {

			for (Object object : _objects) {
				unsafeConsumer.accept(object);
			}
		}

		@Override
		public Class<? extends BulkSelectionFactory>
			getBulkSelectionFactoryClass() {

			return null;
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return Collections.emptyMap();
		}

		@Override
		public long getSize() {
			return _objects.size();
		}

		@Override
		public Serializable serialize() {
			return null;
		}

		@Override
		public BulkSelection<AssetEntry> toAssetEntryBulkSelection() {
			return null;
		}

		private VariantBulkSelection(List<Object> objects) {
			_objects = objects;
		}

		private final List<Object> _objects;

	}

}