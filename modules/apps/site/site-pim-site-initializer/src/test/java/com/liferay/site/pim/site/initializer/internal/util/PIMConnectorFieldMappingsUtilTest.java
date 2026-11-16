/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.util;

import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectEntryLocalServiceUtil;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
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
	public void testGetObjectEntryValues() throws Exception {
		long objectDefinitionId = RandomTestUtil.randomLong();
		long objectEntryId = RandomTestUtil.randomLong();

		ObjectEntry objectEntry = Mockito.mock(ObjectEntry.class);

		Mockito.when(
			objectEntry.getModelAttributes()
		).thenReturn(
			HashMapBuilder.<String, Object>put(
				"objectEntryId", objectEntryId
			).put(
				"status", WorkflowConstants.STATUS_APPROVED
			).put(
				"userName", RandomTestUtil.randomString()
			).build()
		);

		Mockito.when(
			objectEntry.getObjectDefinitionId()
		).thenReturn(
			objectDefinitionId
		);

		try (MockedStatic<ObjectEntryLocalServiceUtil>
				objectEntryLocalServiceUtilMockedStatic = Mockito.mockStatic(
					ObjectEntryLocalServiceUtil.class);
			MockedStatic<ObjectFieldLocalServiceUtil>
				objectFieldLocalServiceUtilMockedStatic = Mockito.mockStatic(
					ObjectFieldLocalServiceUtil.class)) {

			String title = RandomTestUtil.randomString();

			objectEntryLocalServiceUtilMockedStatic.when(
				() -> ObjectEntryLocalServiceUtil.getValues(objectEntry)
			).thenReturn(
				HashMapBuilder.<String, Serializable>put(
					"title", title
				).build()
			);

			List<ObjectField> objectFields = Arrays.asList(
				_mockObjectField("expirationDate", "expirationDate", true),
				_mockObjectField("objectEntryId", "id", true),
				_mockObjectField("status", "status", true),
				_mockObjectField("userName", "title", false));

			objectFieldLocalServiceUtilMockedStatic.when(
				() -> ObjectFieldLocalServiceUtil.getObjectFields(
					objectDefinitionId)
			).thenReturn(
				objectFields
			);

			Assert.assertEquals(
				HashMapBuilder.<String, Serializable>put(
					"id", objectEntryId
				).put(
					"status", WorkflowConstants.LABEL_APPROVED
				).put(
					"title", title
				).build(),
				PIMConnectorFieldMappingsUtil.getObjectEntryValues(
					objectEntry));
		}
	}

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
			PIMConnectorFieldMappingsUtil.getSourceFieldPath("r_images/"));
		Assert.assertEquals(
			Collections.emptyList(),
			PIMConnectorFieldMappingsUtil.getSourceFieldPath(
				"r_images//title"));
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
		Assert.assertTrue(
			PIMConnectorFieldMappingsUtil.isRepeatableGroupPath(
				_mockFieldMapping("r_images/title", "dynamicValue")));
		Assert.assertFalse(
			PIMConnectorFieldMappingsUtil.isRepeatableGroupPath(
				_mockFieldMapping(
					"r_images/title",
					PIMConnectorFieldMappingsUtil.TYPE_FIXED_VALUE)));
		Assert.assertFalse(
			PIMConnectorFieldMappingsUtil.isRepeatableGroupPath(
				_mockFieldMapping("title", "dynamicValue")));
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

	private ObjectField _mockObjectField(
		String dbColumnName, String name, boolean system) {

		ObjectField objectField = Mockito.mock(ObjectField.class);

		Mockito.when(
			objectField.getDBColumnName()
		).thenReturn(
			dbColumnName
		);

		Mockito.when(
			objectField.getName()
		).thenReturn(
			name
		);

		Mockito.when(
			objectField.isSystem()
		).thenReturn(
			system
		);

		return objectField;
	}

}