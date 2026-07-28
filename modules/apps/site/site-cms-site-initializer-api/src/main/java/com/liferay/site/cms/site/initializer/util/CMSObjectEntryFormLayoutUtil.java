/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.util;

import com.liferay.fragment.contributor.util.FragmentCollectionContributorRegistryUtil;
import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.fragment.listener.FragmentEntryLinkListener;
import com.liferay.fragment.listener.FragmentEntryLinkListenerRegistry;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.DefaultFragmentRendererContext;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.fragment.renderer.FragmentRendererRegistry;
import com.liferay.fragment.service.FragmentEntryLinkLocalServiceUtil;
import com.liferay.fragment.service.FragmentEntryLinkService;
import com.liferay.fragment.service.FragmentEntryLinkServiceUtil;
import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldSet;
import com.liferay.info.field.InfoFieldSetEntry;
import com.liferay.info.field.type.RelationshipInfoFieldType;
import com.liferay.info.localized.InfoLocalizedValue;
import com.liferay.layout.manager.FormManager;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalServiceUtil;
import com.liferay.layout.util.structure.FormRelationshipStyledLayoutStructureItem;
import com.liferay.layout.util.structure.FragmentStyledLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ScopeUtil;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author Stefano Motta
 */
public class CMSObjectEntryFormLayoutUtil {

	public static FragmentEntryLink addFragmentEntryLink(
			String editableValues,
			FragmentEntryLinkService fragmentEntryLinkService,
			FragmentRendererRegistry fragmentRendererRegistry,
			String fragmentEntryKey, Layout layout, long segmentsExperienceId,
			ServiceContext serviceContext)
		throws Exception {

		FragmentRenderer fragmentRenderer =
			fragmentRendererRegistry.getFragmentRenderer(fragmentEntryKey);

		if (fragmentRenderer != null) {
			DefaultFragmentRendererContext defaultFragmentRendererContext =
				new DefaultFragmentRendererContext(null);

			return fragmentEntryLinkService.addFragmentEntryLink(
				null, layout.getGroupId(), null, null, null,
				segmentsExperienceId, layout.getPlid(), StringPool.BLANK,
				StringPool.BLANK, StringPool.BLANK,
				JSONFactoryUtil.toString(
					fragmentRenderer.getConfigurationJSONObject(
						defaultFragmentRendererContext)),
				editableValues, StringPool.BLANK, 0, fragmentEntryKey,
				fragmentRenderer.getType(), serviceContext);
		}

		FragmentEntry fragmentEntry =
			FragmentCollectionContributorRegistryUtil.getFragmentEntry(
				fragmentEntryKey);

		if (fragmentEntry == null) {
			return null;
		}

		String contributedRendererKey = null;

		if (fragmentEntry.getFragmentEntryId() == 0) {
			contributedRendererKey = fragmentEntryKey;
		}

		return fragmentEntryLinkService.addFragmentEntryLink(
			null, layout.getGroupId(), null,
			fragmentEntry.getExternalReferenceCode(),
			ScopeUtil.getItemScopeExternalReferenceCode(
				fragmentEntry.getGroupId(), layout.getGroupId()),
			segmentsExperienceId, layout.getPlid(), fragmentEntry.getCss(),
			fragmentEntry.getHtml(), fragmentEntry.getJs(),
			fragmentEntry.getConfiguration(), editableValues, StringPool.BLANK,
			0, contributedRendererKey, fragmentEntry.getType(), serviceContext);
	}

	public static void addInfoFieldFragmentEntryLink(
			List<FragmentEntryLink> addedFragmentEntryLinks, boolean editMode,
			FormManager formManager, InfoField<?> infoField, Layout layout,
			LayoutStructure layoutStructure,
			LayoutStructureItem layoutStructureItem, boolean readOnly,
			long segmentsExperienceId, ServiceContext serviceContext,
			JSONObject stylesJSONObject)
		throws Exception {

		if (RelationshipInfoFieldType.INSTANCE ==
				infoField.getInfoFieldType()) {

			if (!editMode) {
				return;
			}

			InfoField<RelationshipInfoFieldType> relationshipInfoField =
				(InfoField<RelationshipInfoFieldType>)infoField;

			if (relationshipInfoField.getAttribute(
					RelationshipInfoFieldType.INHERITANCE)) {

				return;
			}

			if (relationshipInfoField.getAttribute(
					RelationshipInfoFieldType.MULTIPLE)) {

				addInputFragmentEntryLink(
					addedFragmentEntryLinks, null, formManager,
					"INPUTS-multiselector-dropdown", infoField, layout,
					layoutStructure, layoutStructureItem, readOnly,
					segmentsExperienceId, serviceContext, stylesJSONObject);

				return;
			}
		}

		addInputFragmentEntryLink(
			addedFragmentEntryLinks, null, formManager, null, infoField, layout,
			layoutStructure, layoutStructureItem, readOnly,
			segmentsExperienceId, serviceContext, stylesJSONObject);
	}

	public static void addInputFragmentEntryLink(
			List<FragmentEntryLink> addedFragmentEntryLinks,
			JSONObject configurationJSONObject, FormManager formManager,
			String fragmentEntryKey, InfoField<?> infoField, Layout layout,
			LayoutStructure layoutStructure,
			LayoutStructureItem layoutStructureItem, boolean readOnly,
			long segmentsExperienceId, ServiceContext serviceContext,
			JSONObject stylesJSONObject)
		throws Exception {

		if (infoField == null) {
			return;
		}

		FragmentStyledLayoutStructureItem fragmentStyledLayoutStructureItem =
			formManager.addFragmentEntryLinksLayoutStructureItem(
				fragmentEntryKey, infoField, layout, layoutStructure,
				layoutStructureItem, readOnly, segmentsExperienceId,
				serviceContext);

		if (fragmentStyledLayoutStructureItem == null) {
			return;
		}

		fragmentStyledLayoutStructureItem.updateItemConfig(
			JSONUtil.put("styles", stylesJSONObject));

		FragmentEntryLink fragmentEntryLink =
			FragmentEntryLinkLocalServiceUtil.fetchFragmentEntryLink(
				fragmentStyledLayoutStructureItem.getFragmentEntryLinkId());

		if (configurationJSONObject != null) {
			JSONObject editableValuesJSONObject =
				fragmentEntryLink.getEditableValuesJSONObject();

			JSONObject jsonObject = editableValuesJSONObject.getJSONObject(
				FragmentEntryProcessorConstants.
					KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR);

			for (String key : configurationJSONObject.keySet()) {
				jsonObject.put(key, configurationJSONObject.get(key));
			}

			fragmentEntryLink =
				FragmentEntryLinkServiceUtil.updateFragmentEntryLink(
					fragmentEntryLink.getFragmentEntryLinkId(),
					editableValuesJSONObject.toString());
		}

		if (fragmentEntryLink != null) {
			addedFragmentEntryLinks.add(fragmentEntryLink);
		}
	}

	public static LayoutStructure addInputFragmentEntryLinks(
			List<FragmentEntryLink> addedFragmentEntryLinks, boolean editMode,
			FragmentEntryLinkListenerRegistry fragmentEntryLinkListenerRegistry,
			FragmentEntryLinkService fragmentEntryLinkService,
			FormManager formManager,
			FragmentRendererRegistry fragmentRendererRegistry,
			InfoFieldSet infoFieldSet, Layout layout,
			LayoutStructure layoutStructure,
			LayoutStructureItem layoutStructureItem,
			String objectDefinitionName, boolean readOnly, boolean repeatable,
			long segmentsExperienceId, ServiceContext serviceContext,
			JSONObject stylesJSONObject)
		throws Exception {

		if (infoFieldSet.isRelationship()) {
			FragmentEntryLink fragmentEntryLink = addFragmentEntryLink(
				JSONUtil.toString(
					JSONUtil.put(
						FragmentEntryProcessorConstants.
							KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR,
						JSONUtil.put(
							"accordion-title",
							() -> {
								InfoLocalizedValue<String>
									labelInfoLocalizedValue =
										infoFieldSet.
											getLabelInfoLocalizedValue();

								JSONObject jsonObject =
									JSONFactoryUtil.createJSONObject();

								for (Locale availableLocale :
										labelInfoLocalizedValue.
											getAvailableLocales()) {

									jsonObject.put(
										LocaleUtil.toLanguageId(
											availableLocale),
										labelInfoLocalizedValue.getValue(
											availableLocale));
								}

								return jsonObject;
							}))),
				fragmentEntryLinkService, fragmentRendererRegistry,
				"BASIC_COMPONENT-accordion", layout, segmentsExperienceId,
				serviceContext);

			if (fragmentEntryLink != null) {
				LayoutStructureItem fragmentStyledLayoutStructureItem =
					layoutStructure.addFragmentStyledLayoutStructureItem(
						fragmentEntryLink.getFragmentEntryLinkId(),
						layoutStructureItem.getItemId(), -1);

				fragmentStyledLayoutStructureItem.updateItemConfig(
					JSONUtil.put("styles", stylesJSONObject));

				LayoutPageTemplateStructureLocalServiceUtil.
					updateLayoutPageTemplateStructureData(
						serviceContext.getUserId(), layout.getGroupId(),
						layout.getPlid(), segmentsExperienceId,
						layoutStructure.toString());

				for (FragmentEntryLinkListener fragmentEntryLinkListener :
						fragmentEntryLinkListenerRegistry.
							getFragmentEntryLinkListeners()) {

					fragmentEntryLinkListener.onAddFragmentEntryLink(
						fragmentEntryLink);
				}

				LayoutPageTemplateStructure layoutPageTemplateStructure =
					LayoutPageTemplateStructureLocalServiceUtil.
						fetchLayoutPageTemplateStructure(
							layout.getGroupId(), layout.getPlid());

				layoutStructure = LayoutStructure.of(
					layoutPageTemplateStructure.getData(segmentsExperienceId));

				fragmentStyledLayoutStructureItem =
					layoutStructure.getLayoutStructureItem(
						fragmentStyledLayoutStructureItem.getItemId());

				String childrenItemId =
					fragmentStyledLayoutStructureItem.getChildrenItemId(0);

				if (childrenItemId != null) {
					layoutStructureItem =
						layoutStructure.getLayoutStructureItem(childrenItemId);
				}
			}

			FormRelationshipStyledLayoutStructureItem
				formRelationshipStyledLayoutStructureItem =
					(FormRelationshipStyledLayoutStructureItem)
						layoutStructure.
							addFormRelationshipStyledLayoutStructureItem(
								PortalUUIDUtil.generate(),
								layoutStructureItem.getItemId(), -1);

			formRelationshipStyledLayoutStructureItem.setContentType(
				infoFieldSet.getName());
			formRelationshipStyledLayoutStructureItem.setRepeatable(repeatable);

			layoutStructureItem = formRelationshipStyledLayoutStructureItem;
		}

		for (InfoFieldSetEntry infoFieldSetEntry :
				infoFieldSet.getInfoFieldSetEntries()) {

			if (Objects.equals(infoFieldSet.getName(), objectDefinitionName) &&
				ArrayUtil.contains(
					_HIDDEN_INFO_FIELDS, infoFieldSetEntry.getName())) {

				continue;
			}

			if (infoFieldSetEntry instanceof InfoField) {
				addInfoFieldFragmentEntryLink(
					addedFragmentEntryLinks, editMode, formManager,
					(InfoField<?>)infoFieldSetEntry, layout, layoutStructure,
					layoutStructureItem, readOnly, segmentsExperienceId,
					serviceContext, stylesJSONObject);
			}
			else if (infoFieldSetEntry instanceof InfoFieldSet) {
				layoutStructure = addInputFragmentEntryLinks(
					addedFragmentEntryLinks, editMode,
					fragmentEntryLinkListenerRegistry, fragmentEntryLinkService,
					formManager, fragmentRendererRegistry,
					(InfoFieldSet)infoFieldSetEntry, layout, layoutStructure,
					layoutStructureItem, objectDefinitionName, readOnly,
					repeatable, segmentsExperienceId, serviceContext,
					stylesJSONObject);
			}
		}

		return layoutStructure;
	}

	public static JSONObject getLocalizedNameJSONObject(
		Map<Locale, String> nameMap) {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		for (Map.Entry<Locale, String> entry : nameMap.entrySet()) {
			jsonObject.put(
				LocaleUtil.toLanguageId(entry.getKey()), entry.getValue());
		}

		return jsonObject;
	}

	public static LayoutStructure persistAndRefetchLayoutStructure(
			FragmentEntryLinkListenerRegistry fragmentEntryLinkListenerRegistry,
			Layout layout, LayoutStructure layoutStructure,
			long segmentsExperienceId, ServiceContext serviceContext,
			FragmentEntryLink fragmentEntryLink)
		throws Exception {

		LayoutPageTemplateStructureLocalServiceUtil.
			updateLayoutPageTemplateStructureData(
				serviceContext.getUserId(), layout.getGroupId(),
				layout.getPlid(), segmentsExperienceId,
				layoutStructure.toString());

		for (FragmentEntryLinkListener fragmentEntryLinkListener :
				fragmentEntryLinkListenerRegistry.
					getFragmentEntryLinkListeners()) {

			fragmentEntryLinkListener.onAddFragmentEntryLink(fragmentEntryLink);
		}

		LayoutPageTemplateStructure layoutPageTemplateStructure =
			LayoutPageTemplateStructureLocalServiceUtil.
				fetchLayoutPageTemplateStructure(
					layout.getGroupId(), layout.getPlid());

		return LayoutStructure.of(
			layoutPageTemplateStructure.getData(segmentsExperienceId));
	}

	private static final String[] _HIDDEN_INFO_FIELDS = {
		"displayDate", "expirationDate", "externalReferenceCode",
		"objectEntryFriendlyURL", "reviewDate", "title"
	};

}