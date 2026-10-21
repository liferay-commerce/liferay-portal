/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.frontend.data.set.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dispatch.executor.DispatchTaskStatus;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.dispatch.service.DispatchLogLocalService;
import com.liferay.dispatch.service.DispatchTriggerLocalService;
import com.liferay.frontend.data.set.provider.FDSDataProvider;
import com.liferay.frontend.data.set.provider.FDSDataProviderRegistry;
import com.liferay.frontend.data.set.provider.search.FDSPagination;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.bean.BeanPropertiesUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.site.pim.site.initializer.constants.PIMObjectDefinitionConstants;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Tancredi Covioli
 */
@FeatureFlag("LPD-96666")
@RunWith(Arquillian.class)
public class PIMConnectorFDSDataProviderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_fdsDataProvider = _fdsDataProviderRegistry.getFDSDataProvider(
			"com.liferay.site.pim.site.initializer-connectors");

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setLocale(LocaleUtil.US);
		themeDisplay.setUser(TestPropsValues.getUser());

		_mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		_objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					PIMObjectDefinitionConstants.
						EXTERNAL_REFERENCE_CODE_CONNECTOR,
					TestPropsValues.getCompanyId());
	}

	@After
	public void tearDown() throws Exception {
		for (ObjectEntry objectEntry : _objectEntries) {
			_objectEntryLocalService.deleteObjectEntry(objectEntry);
		}
	}

	@Test
	public void testGetItems() throws Exception {
		String name = RandomTestUtil.randomString();

		ObjectEntry objectEntry = _addObjectEntry(name);

		_addObjectEntry(RandomTestUtil.randomString());

		_mockHttpServletRequest.setParameter(
			"filter", "name eq '" + name + "'");

		Assert.assertEquals(
			1,
			_fdsDataProvider.getItemsCount(
				() -> null, _mockHttpServletRequest));

		Object item = _getItem();

		Assert.assertEquals(
			objectEntry.getObjectEntryId(),
			BeanPropertiesUtil.getLong(item, "id"));
		Assert.assertEquals(
			"liferay-commerce", BeanPropertiesUtil.getString(item, "key"));
		Assert.assertEquals(
			"never-executed",
			BeanPropertiesUtil.getString(item, "lastExecution"));
		Assert.assertNull(
			BeanPropertiesUtil.getObject(item, "lastExecutionDate"));
		Assert.assertEquals(name, BeanPropertiesUtil.getString(item, "name"));
		Assert.assertTrue(BeanPropertiesUtil.getBoolean(item, "active"));

		DispatchTrigger dispatchTrigger =
			_dispatchTriggerLocalService.
				getDispatchTriggerByExternalReferenceCode(
					"L_PIM_CONNECTOR_" + objectEntry.getExternalReferenceCode(),
					TestPropsValues.getCompanyId());

		Date startDate = new Date(System.currentTimeMillis() - 3000);

		_addDispatchLog(
			dispatchTrigger, startDate, DispatchTaskStatus.SUCCESSFUL);

		_assertLastExecution("success", startDate);

		startDate = new Date(System.currentTimeMillis() - 2000);

		_addDispatchLog(dispatchTrigger, startDate, DispatchTaskStatus.FAILED);

		_assertLastExecution("failed", startDate);

		_addDispatchLog(
			dispatchTrigger, new Date(System.currentTimeMillis() - 1000),
			DispatchTaskStatus.IN_PROGRESS);

		_assertLastExecution("failed", startDate);

		startDate = new Date();

		_addDispatchLog(
			dispatchTrigger, startDate, DispatchTaskStatus.SUCCESSFUL);

		_assertLastExecution("success", startDate);
	}

	private void _addDispatchLog(
			DispatchTrigger dispatchTrigger, Date startDate,
			DispatchTaskStatus dispatchTaskStatus)
		throws Exception {

		_dispatchLogLocalService.addDispatchLog(
			TestPropsValues.getUserId(), dispatchTrigger.getDispatchTriggerId(),
			null, null, null, startDate, dispatchTaskStatus);
	}

	private ObjectEntry _addObjectEntry(String name) throws Exception {
		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			_objectDefinition.getObjectDefinitionId(), 0, null,
			HashMapBuilder.<String, Serializable>put(
				"active", true
			).put(
				"key", "liferay-commerce"
			).put(
				"name", name
			).build(),
			ServiceContextTestUtil.getServiceContext());

		_objectEntries.add(objectEntry);

		return objectEntry;
	}

	private void _assertLastExecution(
			String expectedLastExecution, Date expectedLastExecutionDate)
		throws Exception {

		Object item = _getItem();

		Assert.assertEquals(
			expectedLastExecution,
			BeanPropertiesUtil.getString(item, "lastExecution"));

		Date lastExecutionDate = (Date)BeanPropertiesUtil.getObject(
			item, "lastExecutionDate");

		Assert.assertEquals(
			expectedLastExecutionDate.getTime(), lastExecutionDate.getTime(),
			1000);
	}

	private Object _getItem() throws Exception {
		List<?> items = _fdsDataProvider.getItems(
			() -> null,
			new FDSPagination() {

				@Override
				public int getEndPosition() {
					return 20;
				}

				@Override
				public int getPage() {
					return 1;
				}

				@Override
				public int getPageSize() {
					return 20;
				}

				@Override
				public int getStartPosition() {
					return 0;
				}

			},
			_mockHttpServletRequest, null);

		Assert.assertEquals(items.toString(), 1, items.size());

		return items.get(0);
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DispatchLogLocalService _dispatchLogLocalService;

	@Inject
	private DispatchTriggerLocalService _dispatchTriggerLocalService;

	private FDSDataProvider<?> _fdsDataProvider;

	@Inject
	private FDSDataProviderRegistry _fdsDataProviderRegistry;

	private final MockHttpServletRequest _mockHttpServletRequest =
		new MockHttpServletRequest();
	private ObjectDefinition _objectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private final List<ObjectEntry> _objectEntries = new ArrayList<>();

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}