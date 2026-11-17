/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.pim.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.depot.model.DepotEntry;
import com.liferay.headless.pim.client.dto.v1_0.VariantOption;
import com.liferay.headless.pim.client.dto.v1_0.VariantOptionValue;
import com.liferay.headless.pim.client.pagination.Page;
import com.liferay.headless.pim.client.pagination.Pagination;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeEntryLocalService;
import com.liferay.object.field.builder.MultiselectPicklistObjectFieldBuilder;
import com.liferay.object.field.builder.TextObjectFieldBuilder;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.pim.site.initializer.test.util.PIMProductTypeTestUtil;
import com.liferay.site.pim.site.initializer.test.util.PIMTestUtil;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Balazs Breier
 */
@FeatureFlag("LPD-96666")
@RunWith(Arquillian.class)
public class VariantOptionResourceTest
	extends BaseVariantOptionResourceTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		PIMTestUtil.getOrAddGroup();
	}

	@Override
	@Test
	public void testGetScopeScopeKeyVariantOptionsPage() throws Exception {
		ObjectEntry objectEntry = _addObjectEntry();

		Page<VariantOption> page = _getVariantOptionsPage(
			objectEntry, Pagination.of(1, 20), null);

		Assert.assertEquals(3, page.getTotalCount());

		List<VariantOption> variantOptions =
			(List<VariantOption>)page.getItems();

		Assert.assertEquals(
			Arrays.asList("size", "color", "pattern"),
			TransformUtil.transform(variantOptions, VariantOption::getName));

		VariantOption variantOption = variantOptions.get(0);

		Assert.assertEquals("Dimension", variantOption.getLabel());
		Assert.assertEquals("XL", variantOption.getValue());
		Assert.assertEquals(
			Arrays.asList("M", "XL"),
			TransformUtil.transformToList(
				variantOption.getVariantOptionValues(),
				VariantOptionValue::getKey));

		variantOption = variantOptions.get(1);

		Assert.assertEquals(
			Arrays.asList("Black", "White"),
			TransformUtil.transformToList(
				variantOption.getVariantOptionValues(),
				VariantOptionValue::getLabel));

		variantOption = variantOptions.get(2);

		Assert.assertNull(variantOption.getValue());

		page = _getVariantOptionsPage(objectEntry, Pagination.of(1, 20), "COL");

		Assert.assertEquals(
			Collections.singletonList("color"),
			TransformUtil.transform(
				(List<VariantOption>)page.getItems(), VariantOption::getName));

		page = _getVariantOptionsPage(
			objectEntry, Pagination.of(1, 20), "dimen");

		Assert.assertEquals(
			Collections.singletonList("size"),
			TransformUtil.transform(
				(List<VariantOption>)page.getItems(), VariantOption::getName));

		page = _getVariantOptionsPage(
			objectEntry, Pagination.of(1, 20), RandomTestUtil.randomString());

		Assert.assertEquals(0, page.getTotalCount());
	}

	@Override
	@Test
	public void testGetScopeScopeKeyVariantOptionsPageWithPagination()
		throws Exception {

		Page<VariantOption> page = _getVariantOptionsPage(
			_addObjectEntry(), Pagination.of(1, 2), null);

		Assert.assertEquals(3, page.getTotalCount());

		List<VariantOption> variantOptions =
			(List<VariantOption>)page.getItems();

		Assert.assertEquals(
			variantOptions.toString(), 2, variantOptions.size());
	}

	private ObjectEntry _addObjectEntry() throws Exception {
		ObjectField colorObjectField =
			PIMProductTypeTestUtil.createPicklistObjectField(
				"color", "Black", "Red", "White");

		ObjectField sizeObjectField =
			PIMProductTypeTestUtil.createPicklistObjectField("size", "M", "XL");

		sizeObjectField.setLabelMap(
			Collections.singletonMap(LocaleUtil.getDefault(), "Dimension"));

		ObjectDefinition objectDefinition =
			PIMProductTypeTestUtil.addProductTypeObjectDefinition(
				Arrays.asList(
					colorObjectField,
					new MultiselectPicklistObjectFieldBuilder(
					).labelMap(
						RandomTestUtil.randomLocaleStringMap()
					).listTypeDefinitionId(
						colorObjectField.getListTypeDefinitionId()
					).name(
						"material"
					).build(),
					PIMProductTypeTestUtil.createPicklistObjectField(
						"pattern", RandomTestUtil.randomString(),
						RandomTestUtil.randomString()),
					sizeObjectField,
					new TextObjectFieldBuilder(
					).labelMap(
						RandomTestUtil.randomLocaleStringMap()
					).name(
						"title"
					).build()));

		PIMProductTypeTestUtil.addObjectLayout(
			objectDefinition, "title", "size", "material", "color");

		ListTypeEntry listTypeEntry =
			_listTypeEntryLocalService.getListTypeEntry(
				colorObjectField.getListTypeDefinitionId(), "Red");

		listTypeEntry.setStatus(WorkflowConstants.STATUS_INACTIVE);

		_listTypeEntryLocalService.updateListTypeEntry(listTypeEntry);

		DepotEntry depotEntry = PIMTestUtil.addSpaceDepotEntry();

		return _objectEntryLocalService.addObjectEntry(
			depotEntry.getGroupId(), TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(), 0, null,
			HashMapBuilder.<String, Serializable>put(
				"color", "White"
			).put(
				"size", "XL"
			).put(
				"title", RandomTestUtil.randomString()
			).build(),
			ServiceContextTestUtil.getServiceContext(depotEntry.getGroupId()));
	}

	private Page<VariantOption> _getVariantOptionsPage(
			ObjectEntry objectEntry, Pagination pagination, String search)
		throws Exception {

		return variantOptionResource.getScopeScopeKeyVariantOptionsPage(
			String.valueOf(objectEntry.getGroupId()),
			objectEntry.getModelClassName(),
			objectEntry.getExternalReferenceCode(), search, pagination);
	}

	@Inject
	private ListTypeEntryLocalService _listTypeEntryLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}