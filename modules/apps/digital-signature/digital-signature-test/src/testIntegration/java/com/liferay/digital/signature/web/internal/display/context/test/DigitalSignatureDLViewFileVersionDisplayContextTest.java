/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.web.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.test.util.BatchEngineTestUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.constants.DSObjectDefinitionConstants;
import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.document.library.display.context.DLDisplayContextFactory;
import com.liferay.document.library.display.context.DLUIItemKeys;
import com.liferay.document.library.display.context.DLViewFileVersionDisplayContext;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Danny Situ
 */
@FeatureFlag("LPD-69290")
@RunWith(Arquillian.class)
public class DigitalSignatureDLViewFileVersionDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		BatchEngineTestUtil.processBatchEngineUnits(
			"com.liferay.digital.signature.impl",
			DigitalSignatureDLViewFileVersionDisplayContextTest.class,
			new String[] {
				".com.liferay.digital.signature.internal.batch.01.object." +
					"definition",
				".com.liferay.digital.signature.internal.batch.02.object." +
					"relationship"
			});

		_companyConfigurationTemporarySwapper =
			new CompanyConfigurationTemporarySwapper(
				TestPropsValues.getCompanyId(),
				DigitalSignatureConfiguration.class.getName(),
				HashMapDictionaryBuilder.<String, Object>put(
					"embeddedViewEnabled", true
				).put(
					"enabled", true
				).put(
					"siteSettingsStrategy", "always-inherit"
				).build());
		_dsRequestDocumentObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					DSObjectDefinitionConstants.
						EXTERNAL_REFERENCE_CODE_REQUEST_DOCUMENT,
					TestPropsValues.getCompanyId());
		_dsRequestObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					DSObjectDefinitionConstants.EXTERNAL_REFERENCE_CODE_REQUEST,
					TestPropsValues.getCompanyId());
		_dsRequestRecipientObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					DSObjectDefinitionConstants.
						EXTERNAL_REFERENCE_CODE_REQUEST_RECIPIENT,
					TestPropsValues.getCompanyId());

		_group = GroupTestUtil.addGroup();
	}

	@After
	public void tearDown() throws Exception {
		_companyConfigurationTemporarySwapper.close();
	}

	@Test
	public void testGetActionDropdownItems() throws Exception {
		_testGetActionDropdownItemsWithDigitalSignatureDisabled();
		_testGetActionDropdownItemsWithDigitalSignatureEnabled();
		_testGetActionDropdownItemsWithSignedDSRequestRecipient();
		_testGetActionDropdownItemsWithTerminalDSRequest();
	}

	private ObjectEntry _addDSRequest(
			long fileEntryId, String requestRecipientStatus,
			String requestStatus)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId());

		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			_dsRequestObjectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()),
			HashMapBuilder.<String, Serializable>put(
				"emailSubject", RandomTestUtil.randomString()
			).put(
				"externalReferenceCode", RandomTestUtil.randomString()
			).put(
				"requestStatus", requestStatus
			).put(
				"siteGroupId", _group.getGroupId()
			).build(),
			serviceContext);

		_objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			_dsRequestDocumentObjectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()),
			HashMapBuilder.<String, Serializable>put(
				"fileEntryId", fileEntryId
			).put(
				"r_dsRequestToDSRequestDocuments_l_dsRequestId",
				objectEntry.getObjectEntryId()
			).build(),
			serviceContext);

		_objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			_dsRequestRecipientObjectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()),
			HashMapBuilder.<String, Serializable>put(
				"emailAddress",
				() -> {
					User user = TestPropsValues.getUser();

					return user.getEmailAddress();
				}
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"r_dsRequestToDSRequestRecipients_l_dsRequestId",
				objectEntry.getObjectEntryId()
			).put(
				"r_userToDSRequestRecipients_userId",
				TestPropsValues.getUserId()
			).put(
				"requestRecipientStatus", requestRecipientStatus
			).build(),
			serviceContext);

		return objectEntry;
	}

	private FileEntry _addFileEntry() throws Exception {
		return _dlAppLocalService.addFileEntry(
			null, TestPropsValues.getUserId(), _group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), "text/plain",
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), StringPool.BLANK,
			RandomTestUtil.randomBytes(), null, null, null,
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));
	}

	private List<DropdownItem> _getDropdownItems(
			List<DropdownItem> dropdownItems, FileEntry fileEntry, User user)
		throws Exception {

		HttpServletRequest httpServletRequest = new MockHttpServletRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(_group.getCompanyId()));
		themeDisplay.setLocale(LocaleUtil.getDefault());
		themeDisplay.setSignedIn(true);
		themeDisplay.setURLCurrent(RandomTestUtil.randomString());
		themeDisplay.setUser(user);

		httpServletRequest.setAttribute(WebKeys.THEME_DISPLAY, themeDisplay);

		DLViewFileVersionDisplayContext dlViewFileVersionDisplayContext =
			_dlDisplayContextFactory.getDLViewFileVersionDisplayContext(
				new TestParentDLViewFileVersionDisplayContext(dropdownItems),
				httpServletRequest, new MockHttpServletResponse(),
				fileEntry.getFileVersion());

		return dlViewFileVersionDisplayContext.getActionDropdownItems();
	}

	private List<Object> _getGroupKeys(List<DropdownItem> dropdownItems) {
		Assert.assertEquals(dropdownItems.toString(), 1, dropdownItems.size());

		DropdownItem dropdownItem = dropdownItems.get(0);

		return TransformUtil.transform(
			(List<DropdownItem>)dropdownItem.get("items"),
			groupDropdownItem -> groupDropdownItem.get("key"));
	}

	private List<DropdownItem> _getParentDropdownItems(String... keys) {
		return DropdownItemListBuilder.addGroup(
			dropdownGroupItem -> dropdownGroupItem.setDropdownItems(
				TransformUtil.transformToList(
					keys,
					key -> {
						DropdownItem dropdownItem = new DropdownItem();

						dropdownItem.setKey(key);

						return dropdownItem;
					}))
		).build();
	}

	private void _testGetActionDropdownItemsWithDigitalSignatureDisabled()
		throws Exception {

		FileEntry fileEntry = _addFileEntry();

		_addDSRequest(
			fileEntry.getFileEntryId(), DSRequestConstants.STATUS_SENT,
			DSRequestConstants.STATUS_SENT);

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						DigitalSignatureConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"embeddedViewEnabled", true
						).put(
							"enabled", false
						).put(
							"siteSettingsStrategy", "always-inherit"
						).build())) {

			Assert.assertEquals(
				Arrays.asList(
					DLUIItemKeys.DOWNLOAD,
					DLUIItemKeys.COLLECT_DIGITAL_SIGNATURE,
					DLUIItemKeys.VIEW_HISTORY),
				_getGroupKeys(
					_getDropdownItems(
						_getParentDropdownItems(
							DLUIItemKeys.DOWNLOAD,
							DLUIItemKeys.COLLECT_DIGITAL_SIGNATURE,
							DLUIItemKeys.VIEW_HISTORY),
						fileEntry, TestPropsValues.getUser())));
		}
	}

	private void _testGetActionDropdownItemsWithDigitalSignatureEnabled()
		throws Exception {

		FileEntry fileEntry = _addFileEntry();

		ObjectEntry objectEntry = _addDSRequest(
			fileEntry.getFileEntryId(), DSRequestConstants.STATUS_SENT,
			DSRequestConstants.STATUS_SENT);

		List<DropdownItem> dropdownItems = _getDropdownItems(
			_getParentDropdownItems(
				DLUIItemKeys.DOWNLOAD, DLUIItemKeys.COLLECT_DIGITAL_SIGNATURE,
				DLUIItemKeys.VIEW_HISTORY),
			fileEntry, TestPropsValues.getUser());

		Assert.assertEquals(
			Arrays.asList(
				DLUIItemKeys.DOWNLOAD, DLUIItemKeys.COLLECT_DIGITAL_SIGNATURE,
				"sign", DLUIItemKeys.VIEW_HISTORY),
			_getGroupKeys(dropdownItems));

		dropdownItems = _getDropdownItems(
			_getParentDropdownItems(
				DLUIItemKeys.DOWNLOAD, DLUIItemKeys.VIEW_HISTORY),
			fileEntry, TestPropsValues.getUser());

		Assert.assertEquals(
			Arrays.asList(
				DLUIItemKeys.DOWNLOAD, "sign", DLUIItemKeys.VIEW_HISTORY),
			_getGroupKeys(dropdownItems));

		dropdownItems = _getDropdownItems(
			new ArrayList<>(), fileEntry, TestPropsValues.getUser());

		Assert.assertEquals(dropdownItems.toString(), 1, dropdownItems.size());

		DropdownItem dropdownItem = dropdownItems.get(0);

		Assert.assertEquals(
			String.valueOf(objectEntry.getObjectEntryId()),
			HttpComponentsUtil.getParameter(
				String.valueOf(dropdownItem.get("href")), "dsRequestId",
				false));
		Assert.assertEquals("sign", dropdownItem.get("key"));

		dropdownItems = _getDropdownItems(
			_getParentDropdownItems(
				DLUIItemKeys.DOWNLOAD, DLUIItemKeys.VIEW_HISTORY),
			fileEntry, UserTestUtil.addUser());

		Assert.assertEquals(
			Arrays.asList(DLUIItemKeys.DOWNLOAD, DLUIItemKeys.VIEW_HISTORY),
			_getGroupKeys(dropdownItems));
	}

	private void _testGetActionDropdownItemsWithSignedDSRequestRecipient()
		throws Exception {

		FileEntry fileEntry = _addFileEntry();

		_addDSRequest(
			fileEntry.getFileEntryId(), DSRequestConstants.STATUS_SIGNED,
			DSRequestConstants.STATUS_SENT);

		Assert.assertEquals(
			Arrays.asList(DLUIItemKeys.DOWNLOAD, DLUIItemKeys.VIEW_HISTORY),
			_getGroupKeys(
				_getDropdownItems(
					_getParentDropdownItems(
						DLUIItemKeys.DOWNLOAD, DLUIItemKeys.VIEW_HISTORY),
					fileEntry, TestPropsValues.getUser())));
	}

	private void _testGetActionDropdownItemsWithTerminalDSRequest()
		throws Exception {

		for (String status : DSRequestConstants.STATUSES_REQUEST_TERMINAL) {
			FileEntry fileEntry = _addFileEntry();

			_addDSRequest(
				fileEntry.getFileEntryId(), DSRequestConstants.STATUS_SENT,
				status);

			Assert.assertEquals(
				status,
				Arrays.asList(DLUIItemKeys.DOWNLOAD, DLUIItemKeys.VIEW_HISTORY),
				_getGroupKeys(
					_getDropdownItems(
						_getParentDropdownItems(
							DLUIItemKeys.DOWNLOAD, DLUIItemKeys.VIEW_HISTORY),
						fileEntry, TestPropsValues.getUser())));
		}
	}

	private CompanyConfigurationTemporarySwapper
		_companyConfigurationTemporarySwapper;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	@Inject(
		filter = "component.name=com.liferay.digital.signature.web.internal.display.context.DigitalSignatureDLDisplayContextFactory"
	)
	private DLDisplayContextFactory _dlDisplayContextFactory;

	private ObjectDefinition _dsRequestDocumentObjectDefinition;
	private ObjectDefinition _dsRequestObjectDefinition;
	private ObjectDefinition _dsRequestRecipientObjectDefinition;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	private class TestParentDLViewFileVersionDisplayContext
		implements DLViewFileVersionDisplayContext {

		public TestParentDLViewFileVersionDisplayContext(
			List<DropdownItem> dropdownItems) {

			_dropdownItems = dropdownItems;
		}

		@Override
		public List<DropdownItem> getActionDropdownItems()
			throws PortalException {

			return _dropdownItems;
		}

		@Override
		public String getCssClassFileMimeType() {
			return null;
		}

		@Override
		public DDMFormValues getDDMFormValues(DDMStructure ddmStructure)
			throws PortalException {

			return null;
		}

		@Override
		public DDMFormValues getDDMFormValues(long ddmStorageId)
			throws PortalException {

			return null;
		}

		@Override
		public List<DDMStructure> getDDMStructures() throws PortalException {
			return null;
		}

		@Override
		public int getDDMStructuresCount() throws PortalException {
			return 0;
		}

		@Override
		public String getDiscussionClassName() {
			return null;
		}

		@Override
		public long getDiscussionClassPK() {
			return 0;
		}

		@Override
		public String getDiscussionLabel(Locale locale) {
			return null;
		}

		@Override
		public UUID getUuid() {
			return null;
		}

		@Override
		public boolean hasApprovedVersion() {
			return false;
		}

		@Override
		public boolean hasPreview() {
			return false;
		}

		@Override
		public boolean isDownloadLinkVisible() throws PortalException {
			return false;
		}

		@Override
		public boolean isVersionInfoVisible() throws PortalException {
			return false;
		}

		@Override
		public void renderPreview(
				HttpServletRequest httpServletRequest,
				HttpServletResponse httpServletResponse)
			throws IOException, ServletException {
		}

		private final List<DropdownItem> _dropdownItems;

	}

}