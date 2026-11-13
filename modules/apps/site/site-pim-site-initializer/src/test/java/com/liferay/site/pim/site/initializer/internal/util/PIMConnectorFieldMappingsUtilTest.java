/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.util;

import com.liferay.object.model.ObjectEntry;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Tancredi Covioli
 */
public class PIMConnectorFieldMappingsUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetSourceFieldPath() {
		Assert.assertEquals(
			Collections.emptyList(),
			PIMConnectorFieldMappingsUtil.getSourceFieldPath(null));
		Assert.assertEquals(
			Collections.emptyList(),
			PIMConnectorFieldMappingsUtil.getSourceFieldPath(StringPool.BLANK));
		Assert.assertEquals(
			Collections.emptyList(),
			PIMConnectorFieldMappingsUtil.getSourceFieldPath("/title"));
		Assert.assertEquals(
			Collections.emptyList(),
			PIMConnectorFieldMappingsUtil.getSourceFieldPath(
				"r_images//title"));
		Assert.assertEquals(
			Collections.emptyList(),
			PIMConnectorFieldMappingsUtil.getSourceFieldPath("r_images/"));
		Assert.assertEquals(
			Arrays.asList("r_images", "r_sizes", "width"),
			PIMConnectorFieldMappingsUtil.getSourceFieldPath(
				"r_images/r_sizes/width"));
		Assert.assertEquals(
			Arrays.asList("r_images", "title"),
			PIMConnectorFieldMappingsUtil.getSourceFieldPath("r_images/title"));
		Assert.assertEquals(
			Collections.singletonList("title"),
			PIMConnectorFieldMappingsUtil.getSourceFieldPath("title"));
	}

	@Test
	public void testIsRepeatableGroupPath() {
		Assert.assertFalse(
			PIMConnectorFieldMappingsUtil.isRepeatableGroupPath(
				_mockFieldMapping(
					"r_images/title",
					PIMConnectorFieldMappingsUtil.TYPE_FIXED_VALUE)));
		Assert.assertFalse(
			PIMConnectorFieldMappingsUtil.isRepeatableGroupPath(
				_mockFieldMapping("title", "dynamicValue")));
		Assert.assertTrue(
			PIMConnectorFieldMappingsUtil.isRepeatableGroupPath(
				_mockFieldMapping("r_images/title", "dynamicValue")));
	}

	private ObjectEntry _mockFieldMapping(String sourceFieldName, String type) {
		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			objectEntry.getValues()
		).thenReturn(
			HashMapBuilder.<String, Serializable>put(
				"sourceFieldName", sourceFieldName
			).put(
				"type", type
			).build()
		);

		return objectEntry;
	}

}