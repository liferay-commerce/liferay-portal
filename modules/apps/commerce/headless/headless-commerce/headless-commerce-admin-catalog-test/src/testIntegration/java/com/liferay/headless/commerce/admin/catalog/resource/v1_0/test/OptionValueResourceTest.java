/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPOption;
import com.liferay.commerce.product.model.CPOptionValue;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.model.ExpandoTableConstants;
import com.liferay.expando.test.util.ExpandoTestUtil;
import com.liferay.headless.commerce.admin.catalog.client.custom.field.CustomField;
import com.liferay.headless.commerce.admin.catalog.client.custom.field.CustomValue;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.OptionValue;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Page;
import com.liferay.headless.commerce.admin.catalog.client.pagination.Pagination;
import com.liferay.headless.commerce.core.util.LanguageUtils;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.Inject;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Zoltán Takács
 * @author Michele Vigilante
 */
@RunWith(Arquillian.class)
public class OptionValueResourceTest extends BaseOptionValueResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_cpOption = CPTestUtil.addCPOption(testGroup.getGroupId(), false);
	}

	@Ignore
	@Override
	@Test
	public void testDeleteOptionValue() throws Exception {
		super.testDeleteOptionValue();
	}

	@Ignore
	@Override
	@Test
	public void testDeleteOptionValueByExternalReferenceCode()
		throws Exception {

		super.testDeleteOptionValueByExternalReferenceCode();
	}

	@Override
	@Test
	public void testGetOptionIdOptionValuesPage() throws Exception {
		super.testGetOptionIdOptionValuesPage();

		_testGetOptionIdOptionValuesPageWithCustomFields();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLDeleteOptionValue() throws Exception {
		super.testGraphQLDeleteOptionValue();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLDeleteOptionValueByExternalReferenceCode()
		throws Exception {

		super.testGraphQLDeleteOptionValueByExternalReferenceCode();
	}

	@Ignore
	@Override
	@Test
	public void testPatchOptionValue() throws Exception {
		super.testPatchOptionValue();
	}

	@Ignore
	@Override
	@Test
	public void testPatchOptionValueByExternalReferenceCode() throws Exception {
		super.testPatchOptionValueByExternalReferenceCode();
	}

	@Override
	protected OptionValue randomOptionValue() throws Exception {
		return new OptionValue() {
			{
				externalReferenceCode = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				key = StringUtil.toLowerCase(RandomTestUtil.randomString());
				name = LanguageUtils.getLanguageIdMap(
					RandomTestUtil.randomLocaleStringMap());
				priority = RandomTestUtil.randomDouble();
			}
		};
	}

	@Override
	protected OptionValue testDeleteOptionValue_addOptionValue()
		throws Exception {

		return optionValueResource.postOptionIdOptionValue(
			_cpOption.getCPOptionId(), randomOptionValue());
	}

	@Override
	protected OptionValue
			testDeleteOptionValueByExternalReferenceCode_addOptionValue()
		throws Exception {

		return optionValueResource.postOptionIdOptionValue(
			_cpOption.getCPOptionId(), randomOptionValue());
	}

	@Override
	protected OptionValue
			testGetOptionByExternalReferenceCodeOptionValuesPage_addOptionValue(
				String externalReferenceCode, OptionValue optionValue)
		throws Exception {

		return optionValueResource.postOptionByExternalReferenceCodeOptionValue(
			externalReferenceCode, optionValue);
	}

	@Override
	protected String
			testGetOptionByExternalReferenceCodeOptionValuesPage_getExternalReferenceCode()
		throws Exception {

		return _cpOption.getExternalReferenceCode();
	}

	@Override
	protected OptionValue testGetOptionIdOptionValuesPage_addOptionValue(
			Long id, OptionValue optionValue)
		throws Exception {

		return optionValueResource.postOptionIdOptionValue(id, optionValue);
	}

	@Override
	protected Long testGetOptionIdOptionValuesPage_getId() throws Exception {
		return _cpOption.getCPOptionId();
	}

	@Override
	protected OptionValue testGetOptionValue_addOptionValue() throws Exception {
		return optionValueResource.postOptionIdOptionValue(
			_cpOption.getCPOptionId(), randomOptionValue());
	}

	@Override
	protected OptionValue
			testGetOptionValueByExternalReferenceCode_addOptionValue()
		throws Exception {

		return optionValueResource.postOptionIdOptionValue(
			_cpOption.getCPOptionId(), randomOptionValue());
	}

	@Override
	protected OptionValue testGraphQLOptionValue_addOptionValue()
		throws Exception {

		return optionValueResource.postOptionIdOptionValue(
			_cpOption.getCPOptionId(), randomOptionValue());
	}

	@Override
	protected OptionValue
			testPostOptionByExternalReferenceCodeOptionValue_addOptionValue(
				OptionValue optionValue)
		throws Exception {

		return optionValueResource.postOptionByExternalReferenceCodeOptionValue(
			_cpOption.getExternalReferenceCode(), optionValue);
	}

	@Override
	protected OptionValue testPostOptionIdOptionValue_addOptionValue(
			OptionValue optionValue)
		throws Exception {

		return optionValueResource.postOptionIdOptionValue(
			_cpOption.getCPOptionId(), optionValue);
	}

	private void _testGetOptionIdOptionValuesPageWithCustomFields()
		throws Exception {

		ExpandoTable expandoTable = ExpandoTestUtil.addTable(
			_classNameLocalService.getClassNameId(CPOptionValue.class),
			ExpandoTableConstants.DEFAULT_TABLE_NAME);

		ExpandoColumn expandoColumn = ExpandoTestUtil.addColumn(
			expandoTable, RandomTestUtil.randomString(),
			ExpandoColumnConstants.STRING);

		CPOption cpOption = CPTestUtil.addCPOption(
			testGroup.getGroupId(), false);

		OptionValue optionValue = optionValueResource.postOptionIdOptionValue(
			cpOption.getCPOptionId(), randomOptionValue());

		String customFieldValue = RandomTestUtil.randomString();

		ExpandoTestUtil.addValue(
			expandoTable, expandoColumn, optionValue.getId(), customFieldValue);

		Page<OptionValue> optionValuesPage =
			optionValueResource.getOptionIdOptionValuesPage(
				cpOption.getCPOptionId(), null, Pagination.of(1, 10), null);

		List<OptionValue> optionValues =
			(List<OptionValue>)optionValuesPage.getItems();

		Assert.assertEquals(optionValues.toString(), 1, optionValues.size());

		optionValue = optionValues.get(0);

		CustomField[] customFields = optionValue.getCustomFields();

		Assert.assertEquals(
			Arrays.toString(customFields), 1, customFields.length);

		CustomField customField = customFields[0];

		Assert.assertEquals(expandoColumn.getName(), customField.getName());

		CustomValue customValue = customField.getCustomValue();

		Assert.assertEquals(customFieldValue, customValue.getData());
	}

	@Inject
	private ClassNameLocalService _classNameLocalService;

	private CPOption _cpOption;

}