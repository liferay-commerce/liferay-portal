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
import com.liferay.site.pim.site.initializer.internal.link.VariantPIMLinkType;
import com.liferay.site.pim.site.initializer.internal.util.PIMProductVariantUtil;
import com.liferay.site.pim.site.initializer.link.PIMLinkRelatedEntry;
import com.liferay.site.pim.site.initializer.variant.PIMProductVariantHelper;
import com.liferay.site.pim.site.initializer.variant.PIMProductVariantOption;

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
	property = "bulk.selection.action.key=generate.pim.product.variants",
	service = BulkSelectionAction.class
)
public class GeneratePIMProductVariantsBulkSelectionAction
	extends BaseObjectBulkSelectionAction {

	@Override
	public void execute(
			User user, BulkSelection<Object> bulkSelection,
			Map<String, Serializable> inputMap)
		throws Exception {

		List<PIMProductVariant> pimProductVariants = null;

		try {
			pimProductVariants = _getPIMProductVariants(
				bulkSelection, user.getCompanyId(), inputMap);
		}
		catch (Exception exception) {
			super.execute(
				user, bulkSelection,
				HashMapBuilder.<String, Serializable>putAll(
					inputMap
				).put(
					"exception", exception
				).build());

			return;
		}

		partialUpdateObjectEntry(
			user.getUserId(),
			objectEntryLocalService.getObjectEntry(
				GetterUtil.getLong(inputMap.get("bulkActionTaskId"))),
			HashMapBuilder.<String, Serializable>put(
				"numberOfItems", pimProductVariants.size()
			).build());

		super.execute(
			user, new PIMProductVariantBulkSelection(pimProductVariants),
			inputMap);
	}

	@Override
	protected void doExecute(
			User user, Map<String, Serializable> inputMap, Object object)
		throws Exception {

		Exception exception = (Exception)inputMap.get("exception");

		if (exception != null) {
			throw exception;
		}

		PIMProductVariant pimProductVariant = (PIMProductVariant)object;

		if (pimProductVariant.getPIMProductVariantObjectEntry() != null) {
			return;
		}

		ObjectEntry objectEntry = pimProductVariant.getObjectEntry();

		Map<String, Serializable> values = new HashMap<>(
			objectEntry.getValues());

		Map<String, String> pimProductVariantValues =
			pimProductVariant.getValues();

		for (Map.Entry<String, String> entry :
				pimProductVariantValues.entrySet()) {

			values.put(entry.getKey(), entry.getValue());

			Map<String, Serializable> sourceLocalizedValues =
				(Map<String, Serializable>)values.get(entry.getKey() + "_i18n");

			if (sourceLocalizedValues == null) {
				continue;
			}

			HashMap<String, Serializable> targetLocalizedValues =
				new HashMap<>();

			for (String languageId : sourceLocalizedValues.keySet()) {
				targetLocalizedValues.put(languageId, entry.getValue());
			}

			values.put(entry.getKey() + "_i18n", targetLocalizedValues);
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

	private List<String> _getListTypeEntryKeys(
		List<PIMProductVariantOption> pimProductVariantOptions,
		Map<String, Serializable> values) {

		List<String> listTypeEntryKeys = new ArrayList<>();

		for (PIMProductVariantOption pimProductVariantOption :
				pimProductVariantOptions) {

			ObjectField objectField = pimProductVariantOption.getObjectField();

			String listTypeEntryKey = MapUtil.getString(
				values, objectField.getName());

			if (Validator.isNull(listTypeEntryKey)) {
				return null;
			}

			listTypeEntryKeys.add(listTypeEntryKey);
		}

		return listTypeEntryKeys;
	}

	private List<PIMProductVariant> _getPIMProductVariants(
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

		List<PIMProductVariantOption> pimProductVariantOptions =
			ListUtil.filter(
				_pimProductVariantHelper.getPIMProductVariantOptions(
					objectEntry),
				pimProductVariantOption -> {
					ObjectField objectField =
						pimProductVariantOption.getObjectField();

					return objectFieldNames.contains(objectField.getName());
				});

		if (objectFieldNames.isEmpty() ||
			(objectFieldNames.size() != pimProductVariantOptions.size())) {

			throw new IllegalArgumentException(
				"Invalid object field names " + objectFieldNames);
		}

		List<PIMProductVariant> pimProductVariants = new ArrayList<>();

		Map<List<String>, ObjectEntry> pimProductVariantObjectEntries =
			new HashMap<>();
		Set<String> skus = new HashSet<>();

		for (PIMLinkRelatedEntry pimLinkRelatedEntry :
				_pimLinkEngine.getPIMLinkRelatedEntries(
					StringBundler.concat(
						"type eq '", VariantPIMLinkType.TYPE, "'"),
					objectEntry)) {

			ObjectEntry pimProductVariantObjectEntry =
				pimLinkRelatedEntry.getObjectEntry();

			Map<String, Serializable> values =
				pimProductVariantObjectEntry.getValues();

			List<String> listTypeEntryKeys = _getListTypeEntryKeys(
				pimProductVariantOptions, values);

			if (listTypeEntryKeys != null) {
				pimProductVariantObjectEntries.put(
					listTypeEntryKeys, pimProductVariantObjectEntry);
			}

			skus.add(MapUtil.getString(values, "sku"));
		}

		Locale locale = PortalUtil.getSiteDefaultLocale(
			objectEntry.getGroupId());
		List<String> sourceListTypeEntryKeys = _getListTypeEntryKeys(
			pimProductVariantOptions, objectEntry.getValues());

		for (List<ListTypeEntry> listTypeEntryCombination :
				PIMProductVariantUtil.getListTypeEntryCombinations(
					TransformUtil.transform(
						pimProductVariantOptions,
						PIMProductVariantOption::getListTypeEntries))) {

			List<String> targetListTypeEntryKeys = TransformUtil.transform(
				listTypeEntryCombination, ListTypeEntry::getKey);

			if (targetListTypeEntryKeys.equals(sourceListTypeEntryKeys)) {
				continue;
			}

			ObjectEntry pimProductVariantObjectEntry =
				pimProductVariantObjectEntries.get(targetListTypeEntryKeys);

			if (pimProductVariantObjectEntry != null) {
				pimProductVariants.add(
					new PIMProductVariant(
						objectEntry, pimProductVariantObjectEntry));

				continue;
			}

			Map<String, String> pimProductVariantValues = new LinkedHashMap<>();

			for (int i = 0; i < pimProductVariantOptions.size(); i++) {
				PIMProductVariantOption pimProductVariantOption =
					pimProductVariantOptions.get(i);

				ObjectField objectField =
					pimProductVariantOption.getObjectField();

				pimProductVariantValues.put(
					objectField.getName(), targetListTypeEntryKeys.get(i));
			}

			String sku = PIMProductVariantUtil.getSKU(
				TransformUtil.transform(
					listTypeEntryCombination,
					listTypeEntry -> listTypeEntry.getName(locale)),
				skus, objectEntry.getTitleValue());

			skus.add(sku);

			pimProductVariantValues.put("sku", sku);

			pimProductVariants.add(
				new PIMProductVariant(objectEntry, pimProductVariantValues));
		}

		return pimProductVariants;
	}

	@Reference
	private ObjectEntryService _objectEntryService;

	@Reference
	private PIMLinkEngine _pimLinkEngine;

	@Reference
	private PIMProductVariantHelper _pimProductVariantHelper;

	private static class PIMProductVariant {

		public ObjectEntry getObjectEntry() {
			return _objectEntry;
		}

		public ObjectEntry getPIMProductVariantObjectEntry() {
			return _pimProductVariantObjectEntry;
		}

		public Map<String, String> getValues() {
			return _values;
		}

		private PIMProductVariant(
			ObjectEntry objectEntry, Map<String, String> values) {

			this(objectEntry, null, values);
		}

		private PIMProductVariant(
			ObjectEntry objectEntry, ObjectEntry pimProductVariantObjectEntry) {

			this(objectEntry, pimProductVariantObjectEntry, null);
		}

		private PIMProductVariant(
			ObjectEntry objectEntry, ObjectEntry pimProductVariantObjectEntry,
			Map<String, String> values) {

			_objectEntry = objectEntry;
			_pimProductVariantObjectEntry = pimProductVariantObjectEntry;
			_values = values;
		}

		private final ObjectEntry _objectEntry;
		private final ObjectEntry _pimProductVariantObjectEntry;
		private final Map<String, String> _values;

	}

	private static class PIMProductVariantBulkSelection
		implements BulkSelection<Object> {

		@Override
		public <E extends PortalException> void forEach(
				UnsafeConsumer<Object, E> unsafeConsumer)
			throws PortalException {

			for (PIMProductVariant pimProductVariant : _pimProductVariants) {
				unsafeConsumer.accept(pimProductVariant);
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
			return _pimProductVariants.size();
		}

		@Override
		public Serializable serialize() {
			return null;
		}

		@Override
		public BulkSelection<AssetEntry> toAssetEntryBulkSelection() {
			return null;
		}

		private PIMProductVariantBulkSelection(
			List<PIMProductVariant> pimProductVariants) {

			_pimProductVariants = pimProductVariants;
		}

		private final List<PIMProductVariant> _pimProductVariants;

	}

}