/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.util;

import com.liferay.dispatch.executor.DispatchTaskClusterMode;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.dispatch.service.DispatchTriggerLocalServiceUtil;
import com.liferay.object.model.ObjectEntry;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.site.pim.site.initializer.internal.dispatch.executor.PIMConnectorDispatchTaskExecutor;

import java.io.Serializable;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Stefano Motta
 */
public class PIMConnectorDispatchTriggerUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		Company company = Mockito.mock(Company.class);

		Mockito.when(
			company.getTimeZone()
		).thenReturn(
			TimeZone.getTimeZone("Europe/Rome")
		);

		_companyLocalServiceUtilMockedStatic.when(
			() -> CompanyLocalServiceUtil.getCompany(_COMPANY_ID)
		).thenReturn(
			company
		);

		Mockito.when(
			_dispatchTrigger.getDispatchTaskSettingsUnicodeProperties()
		).thenReturn(
			_unicodeProperties
		);

		Mockito.when(
			_dispatchTrigger.getDispatchTriggerId()
		).thenReturn(
			_DISPATCH_TRIGGER_ID
		);

		_dispatchTriggerLocalServiceUtilMockedStatic.when(
			() -> DispatchTriggerLocalServiceUtil.addDispatchTrigger(
				Mockito.anyString(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(UnicodeProperties.class), Mockito.anyString(),
				Mockito.anyBoolean())
		).thenReturn(
			_dispatchTrigger
		);

		_dispatchTriggerLocalServiceUtilMockedStatic.when(
			() ->
				DispatchTriggerLocalServiceUtil.
					getDispatchTriggerByExternalReferenceCode(
						"L_PIM_CONNECTOR_" + _EXTERNAL_REFERENCE_CODE,
						_COMPANY_ID)
		).thenReturn(
			_dispatchTrigger
		);

		_dispatchTriggerLocalServiceUtilMockedStatic.when(
			() -> DispatchTriggerLocalServiceUtil.updateDispatchTrigger(
				Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString(),
				Mockito.any(DispatchTaskClusterMode.class), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyBoolean(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())
		).thenReturn(
			_dispatchTrigger
		);

		Mockito.when(
			_objectEntry.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		Mockito.when(
			_objectEntry.getExternalReferenceCode()
		).thenReturn(
			_EXTERNAL_REFERENCE_CODE
		);

		Mockito.when(
			_objectEntry.getObjectEntryId()
		).thenReturn(
			_OBJECT_ENTRY_ID
		);

		Mockito.when(
			_objectEntry.getUserId()
		).thenReturn(
			_USER_ID
		);

		_mockObjectEntryValues(true, "Connector");
	}

	@After
	public void tearDown() {
		_companyLocalServiceUtilMockedStatic.close();
		_dispatchTriggerLocalServiceUtilMockedStatic.close();
	}

	@Test
	public void testAddDispatchTrigger() throws Exception {
		Assert.assertEquals(
			_dispatchTrigger,
			PIMConnectorDispatchTriggerUtil.addDispatchTrigger(_objectEntry));

		_dispatchTriggerLocalServiceUtilMockedStatic.verify(
			() -> DispatchTriggerLocalServiceUtil.addDispatchTrigger(
				"L_PIM_CONNECTOR_" + _EXTERNAL_REFERENCE_CODE, _USER_ID,
				PIMConnectorDispatchTaskExecutor.KEY,
				UnicodePropertiesBuilder.put(
					"pimConnectorObjectEntryId", _OBJECT_ENTRY_ID
				).build(),
				"Connector (" + _OBJECT_ENTRY_ID + ")", false));
		_dispatchTriggerLocalServiceUtilMockedStatic.verify(
			() -> DispatchTriggerLocalServiceUtil.updateDispatchTrigger(
				Mockito.eq(_DISPATCH_TRIGGER_ID), Mockito.eq(true),
				Mockito.eq("0 0 0 * * ?"),
				Mockito.eq(DispatchTaskClusterMode.SINGLE_NODE_PERSISTED),
				Mockito.eq(0), Mockito.eq(0), Mockito.eq(0), Mockito.eq(0),
				Mockito.eq(0), Mockito.eq(true), Mockito.eq(false),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.eq("Europe/Rome")));
	}

	@Test
	public void testDeleteDispatchTrigger() throws Exception {
		PIMConnectorDispatchTriggerUtil.deleteDispatchTrigger(_objectEntry);

		_dispatchTriggerLocalServiceUtilMockedStatic.verify(
			() -> DispatchTriggerLocalServiceUtil.deleteDispatchTrigger(
				Mockito.any(DispatchTrigger.class)),
			Mockito.never());

		_dispatchTriggerLocalServiceUtilMockedStatic.when(
			() ->
				DispatchTriggerLocalServiceUtil.
					fetchDispatchTriggerByExternalReferenceCode(
						"L_PIM_CONNECTOR_" + _EXTERNAL_REFERENCE_CODE,
						_COMPANY_ID)
		).thenReturn(
			_dispatchTrigger
		);

		PIMConnectorDispatchTriggerUtil.deleteDispatchTrigger(_objectEntry);

		_dispatchTriggerLocalServiceUtilMockedStatic.verify(
			() -> DispatchTriggerLocalServiceUtil.deleteDispatchTrigger(
				_dispatchTrigger));
	}

	@Test
	public void testGetDispatchTrigger() throws Exception {
		Assert.assertEquals(
			_dispatchTrigger,
			PIMConnectorDispatchTriggerUtil.getDispatchTrigger(_objectEntry));
	}

	@Test
	public void testUpdateDispatchTrigger() throws Exception {
		_testUpdateDispatchTrigger();
		_testUpdateDispatchTriggerWithActiveChange();
		_testUpdateDispatchTriggerWithNameChange();
	}

	private void _mockObjectEntryValues(boolean active, String name) {
		Mockito.when(
			_objectEntry.getValues()
		).thenReturn(
			HashMapBuilder.<String, Serializable>put(
				"active", active
			).put(
				"name", name
			).build()
		);
	}

	private void _testUpdateDispatchTrigger() throws Exception {
		Mockito.when(
			_dispatchTrigger.getName()
		).thenReturn(
			"Connector (" + _OBJECT_ENTRY_ID + ")"
		);

		Mockito.when(
			_dispatchTrigger.isActive()
		).thenReturn(
			true
		);

		_dispatchTriggerLocalServiceUtilMockedStatic.clearInvocations();

		PIMConnectorDispatchTriggerUtil.updateDispatchTrigger(_objectEntry);

		_dispatchTriggerLocalServiceUtilMockedStatic.verify(
			() -> DispatchTriggerLocalServiceUtil.updateDispatchTrigger(
				Mockito.anyLong(), Mockito.any(UnicodeProperties.class),
				Mockito.anyString()),
			Mockito.never());
		_dispatchTriggerLocalServiceUtilMockedStatic.verify(
			() -> DispatchTriggerLocalServiceUtil.updateDispatchTrigger(
				Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString(),
				Mockito.any(DispatchTaskClusterMode.class), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyBoolean(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString()),
			Mockito.never());
	}

	private void _testUpdateDispatchTriggerWithActiveChange() throws Exception {
		_mockObjectEntryValues(false, "Connector");

		Mockito.when(
			_dispatchTrigger.getCronExpression()
		).thenReturn(
			"0 0 12 * * ?"
		);

		Mockito.when(
			_dispatchTrigger.getDispatchTaskClusterMode()
		).thenReturn(
			DispatchTaskClusterMode.SINGLE_NODE_PERSISTED.getMode()
		);

		Mockito.when(
			_dispatchTrigger.getTimeZoneId()
		).thenReturn(
			"Europe/Rome"
		);

		Mockito.when(
			_dispatchTrigger.isOverlapAllowed()
		).thenReturn(
			false
		);

		_dispatchTriggerLocalServiceUtilMockedStatic.clearInvocations();

		PIMConnectorDispatchTriggerUtil.updateDispatchTrigger(_objectEntry);

		_dispatchTriggerLocalServiceUtilMockedStatic.verify(
			() -> DispatchTriggerLocalServiceUtil.updateDispatchTrigger(
				Mockito.eq(_DISPATCH_TRIGGER_ID), Mockito.eq(false),
				Mockito.eq("0 0 12 * * ?"),
				Mockito.eq(DispatchTaskClusterMode.SINGLE_NODE_PERSISTED),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.eq(true),
				Mockito.eq(false), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
				Mockito.eq("Europe/Rome")));

		_mockObjectEntryValues(true, "Connector");
	}

	private void _testUpdateDispatchTriggerWithNameChange() throws Exception {
		_mockObjectEntryValues(true, "Renamed Connector");

		_dispatchTriggerLocalServiceUtilMockedStatic.when(
			() -> DispatchTriggerLocalServiceUtil.updateDispatchTrigger(
				Mockito.anyLong(), Mockito.any(UnicodeProperties.class),
				Mockito.anyString())
		).thenReturn(
			_dispatchTrigger
		);

		_dispatchTriggerLocalServiceUtilMockedStatic.clearInvocations();

		PIMConnectorDispatchTriggerUtil.updateDispatchTrigger(_objectEntry);

		_dispatchTriggerLocalServiceUtilMockedStatic.verify(
			() -> DispatchTriggerLocalServiceUtil.updateDispatchTrigger(
				_DISPATCH_TRIGGER_ID, _unicodeProperties,
				"Renamed Connector (" + _OBJECT_ENTRY_ID + ")"));

		_mockObjectEntryValues(true, "Connector");
	}

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _DISPATCH_TRIGGER_ID =
		RandomTestUtil.randomLong();

	private static final String _EXTERNAL_REFERENCE_CODE =
		RandomTestUtil.randomString();

	private static final long _OBJECT_ENTRY_ID = RandomTestUtil.randomLong();

	private static final long _USER_ID = RandomTestUtil.randomLong();

	private final MockedStatic<CompanyLocalServiceUtil>
		_companyLocalServiceUtilMockedStatic = Mockito.mockStatic(
			CompanyLocalServiceUtil.class);
	private final DispatchTrigger _dispatchTrigger = Mockito.mock(
		DispatchTrigger.class);
	private final MockedStatic<DispatchTriggerLocalServiceUtil>
		_dispatchTriggerLocalServiceUtilMockedStatic = Mockito.mockStatic(
			DispatchTriggerLocalServiceUtil.class);
	private final ObjectEntry _objectEntry = Mockito.mock(ObjectEntry.class);
	private final UnicodeProperties _unicodeProperties =
		new UnicodeProperties();

}