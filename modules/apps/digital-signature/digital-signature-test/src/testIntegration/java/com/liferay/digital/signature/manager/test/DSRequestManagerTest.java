/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.manager.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.test.util.BatchEngineTestUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.constants.DSObjectDefinitionConstants;
import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.digital.signature.model.DSRequestRecipient;
import com.liferay.digital.signature.url.SignDSURLProvider;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.model.DLVersionNumberIncrease;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.notification.constants.NotificationRecipientSettingConstants;
import com.liferay.notification.model.NotificationQueueEntry;
import com.liferay.notification.service.NotificationQueueEntryLocalService;
import com.liferay.notification.service.NotificationTemplateLocalService;
import com.liferay.notification.util.NotificationRecipientSettingUtil;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.exception.NoSuchObjectEntryException;
import com.liferay.object.exception.ObjectEntryValuesException;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.configuration.test.util.GroupConfigurationTemporarySwapper;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepository;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.constants.TestDataConstants;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.FeatureFlagTestUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.portlet.PortletException;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian I. Kim
 */
@FeatureFlag("LPD-69290")
@RunWith(Arquillian.class)
public class DSRequestManagerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		BatchEngineTestUtil.processBatchEngineUnits(
			"com.liferay.digital.signature.impl", DSRequestManagerTest.class,
			new String[] {
				".com.liferay.digital.signature.internal.batch.01.object." +
					"definition",
				".com.liferay.digital.signature.internal.batch.02.object." +
					"relationship",
				".com.liferay.digital.signature.internal.batch.03." +
					"notification.template"
			});

		_companyConfigurationTemporarySwapper =
			new CompanyConfigurationTemporarySwapper(
				TestPropsValues.getCompanyId(),
				DigitalSignatureConfiguration.class.getName(),
				HashMapDictionaryBuilder.<String, Object>put(
					"accountBaseURI", RandomTestUtil.randomString()
				).put(
					"apiAccountId", RandomTestUtil.randomString()
				).put(
					"apiUsername", RandomTestUtil.randomString()
				).put(
					"embeddedViewEnabled", true
				).put(
					"enabled", true
				).put(
					"environment", RandomTestUtil.randomString()
				).put(
					"integrationKey", RandomTestUtil.randomString()
				).put(
					"rsaPrivateKey", RandomTestUtil.randomString()
				).put(
					"signatureReminderEnabled", true
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
	}

	@After
	public void tearDown() throws Exception {
		_companyConfigurationTemporarySwapper.close();
	}

	@Test
	public void testAddDSRequest() throws Exception {
		DSEnvelope dsEnvelope1 = _createDSEnvelope();

		User user = TestPropsValues.getUser();

		DSRecipient dsRecipient1 = _createDSRecipient(user);

		dsEnvelope1.setDSRecipients(
			ListUtil.fromArray(
				dsRecipient1, _createDSRecipient(UserTestUtil.addUser())));

		FileEntry fileEntry1 = _addFileEntry();

		DSRequest dsRequest = _addDSRequest(dsEnvelope1, fileEntry1);

		Assert.assertEquals(
			dsEnvelope1.getDSEnvelopeId(),
			dsRequest.getExternalReferenceCode());
		Assert.assertEquals(
			Collections.singletonList(fileEntry1.getFileEntryId()),
			dsRequest.getFileEntryIds());
		Assert.assertEquals(
			TestPropsValues.getGroupId(), dsRequest.getSiteGroupId());
		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT, dsRequest.getStatus());

		Map<String, Serializable> dsRequestValues = _getDSRequestValues(
			dsEnvelope1);

		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT,
			MapUtil.getString(dsRequestValues, "requestStatus"));
		Assert.assertEquals(
			TestPropsValues.getGroupId(),
			MapUtil.getLong(dsRequestValues, "siteGroupId"));
		Assert.assertEquals(
			new HashSet<>(
				TransformUtil.transform(
					dsEnvelope1.getDSRecipients(),
					DSRecipient::getEmailAddress)),
			new HashSet<>(
				TransformUtil.transform(
					_getDSRequestRecipientValuesList(dsRequestValues),
					values -> MapUtil.getString(values, "emailAddress"))));

		List<Map<String, Serializable>> dsRequestDocumentValuesList =
			_getValuesList(
				"(fileEntryId eq " + fileEntry1.getFileEntryId() + ")",
				_dsRequestDocumentObjectDefinition);

		Assert.assertEquals(
			dsRequestDocumentValuesList.toString(), 1,
			dsRequestDocumentValuesList.size());

		List<Map<String, Serializable>> dsRecipientValuesList = ListUtil.filter(
			_getDSRequestRecipientValuesList(dsRequestValues),
			values -> Objects.equals(
				dsRecipient1.getEmailAddress(),
				MapUtil.getString(values, "emailAddress")));

		Assert.assertEquals(
			dsRecipientValuesList.toString(), 1, dsRecipientValuesList.size());
		Assert.assertEquals(
			user.getUserId(),
			MapUtil.getLong(
				dsRecipientValuesList.get(0),
				"r_userToDSRequestRecipients_userId"));

		DSEnvelope dsEnvelope2 = _createDSEnvelope();

		DSRecipient dsRecipient2 = _createDSRecipient();

		dsRecipient2.setName(RandomTestUtil.randomString(281));

		dsEnvelope2.setDSRecipients(ListUtil.fromArray(dsRecipient2));

		Group group = GroupTestUtil.addGroup();

		MockLiferayResourceRequest mockLiferayResourceRequest =
			new MockLiferayResourceRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setSiteGroupId(group.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		mockLiferayResourceRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		FileEntry fileEntry2 = _dlAppLocalService.addFileEntry(
			null, TestPropsValues.getUserId(), group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".txt", ContentTypes.TEXT_PLAIN,
			TestDataConstants.TEST_BYTE_ARRAY, null, null, null,
			ServiceContextTestUtil.getServiceContext(group.getGroupId()));

		mockLiferayResourceRequest.setParameter(
			"fileEntryIds", String.valueOf(fileEntry2.getFileEntryId()));

		mockLiferayResourceRequest.setParameter(
			"recipients",
			JSONUtil.putAll(
				JSONUtil.put(
					"email", user.getEmailAddress()
				).put(
					"fullName", user.getFullName()
				)
			).toString());

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> dsEnvelope2));

		try {
			PortletException portletException = Assert.assertThrows(
				PortletException.class,
				() -> _mvcResourceCommand.serveResource(
					mockLiferayResourceRequest,
					new MockLiferayResourceResponse()));

			Throwable throwable = portletException.getCause();

			Assert.assertSame(
				ObjectEntryValuesException.ExceedsTextMaxLength.class,
				throwable.getClass());
			Assert.assertEquals(
				"Object entry value exceeds the maximum length of 280 " +
					"characters for object field \"name\"",
				throwable.getMessage());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(externalReferenceCode eq '" + dsEnvelope2.getDSEnvelopeId() +
					"')",
				_dsRequestObjectDefinition));
		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(fileEntryId eq " + fileEntry2.getFileEntryId() + ")",
				_dsRequestDocumentObjectDefinition));
		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(emailAddress eq '" + dsRecipient2.getEmailAddress() + "')",
				_dsRequestRecipientObjectDefinition));

		_testAddDSRequestWhenDSEnvelopeIdIsNull();
		_testAddDSRequestWhenEmbeddedViewIsDisabled();
		_testAddDSRequestWhenExpirationIsInvalid();
		_testAddDSRequestWhenFeatureFlagIsDisabled();
		_testAddDSRequestWhenFileEntryIsInvalid();
		_testAddDSRequestWhenRecipientIsInvalid();
		_testAddDSRequestWhenDSRecipientUserIsDeleted();
	}

	@Test
	public void testFetchDSRequest() throws Exception {
		_testFetchDSRequest();
		_testFetchDSRequestWhenExpirationDateIsPast();
		_testFetchDSRequestWhenSiteOverridesInstanceSettings();
	}

	@Test
	public void testGetDSRequests() throws Exception {
		long fileEntryId1 = RandomTestUtil.randomInt();
		long fileEntryId2 = RandomTestUtil.randomInt();

		_addDSRequest(_createDSEnvelope(), fileEntryId1, fileEntryId2);

		Map<Long, DSRequest> dsRequests = _dsRequestManager.getDSRequests(
			TestPropsValues.getCompanyId(),
			new long[] {fileEntryId1, fileEntryId2});

		Assert.assertEquals(dsRequests.toString(), 2, dsRequests.size());

		DSRequest dsRequest1 = dsRequests.get(fileEntryId1);
		DSRequest dsRequest2 = dsRequests.get(fileEntryId2);

		Assert.assertEquals(
			dsRequest1.getExternalReferenceCode(),
			dsRequest2.getExternalReferenceCode());

		dsRequests = _dsRequestManager.getDSRequests(
			TestPropsValues.getCompanyId(),
			new long[] {RandomTestUtil.randomLong()});

		Assert.assertTrue(dsRequests.isEmpty());
	}

	@Test
	public void testHasPermission() throws Exception {
		User user1 = UserTestUtil.addUser();

		DSRequest dsRequest = _addDSRequest(
			user1.getEmailAddress(), DSRequestConstants.STATUS_SENT);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(TestPropsValues.getUser());

		Assert.assertTrue(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.UPDATE));
		Assert.assertTrue(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.VIEW));

		permissionChecker = PermissionCheckerFactoryUtil.create(user1);

		Assert.assertFalse(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.UPDATE));
		Assert.assertTrue(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.VIEW));

		User user2 = UserTestUtil.addUser();

		permissionChecker = PermissionCheckerFactoryUtil.create(user2);

		Assert.assertFalse(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.UPDATE));
		Assert.assertFalse(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.VIEW));
	}

	@Test
	public void testSendDSRequestNotifications() throws Exception {
		User user = UserTestUtil.addUser();

		DSRequest dsRequest = _addDSRequest(
			user.getEmailAddress(), DSRequestConstants.STATUS_VOIDED);

		_dsRequestManager.sendDSRequestNotifications(
			dsRequest.getDSRequestId());

		List<NotificationQueueEntry> notificationQueueEntries =
			_getNotificationQueueEntries(dsRequest);

		Assert.assertEquals(
			notificationQueueEntries.toString(), 0,
			notificationQueueEntries.size());

		dsRequest = _addDSRequest(
			user.getEmailAddress(), DSRequestConstants.STATUS_SENT);

		_dsRequestManager.sendDSRequestNotifications(
			dsRequest.getDSRequestId());

		notificationQueueEntries = _getNotificationQueueEntries(dsRequest);

		Assert.assertEquals(
			notificationQueueEntries.toString(), 1,
			notificationQueueEntries.size());

		NotificationQueueEntry notificationQueueEntry =
			notificationQueueEntries.get(0);

		Assert.assertEquals(
			dsRequest.getEmailSubject(), notificationQueueEntry.getSubject());

		String body = notificationQueueEntry.getBody();

		Assert.assertTrue(
			body,
			body.contains(
				_signDSURLProvider.getURL(
					TestPropsValues.getCompanyId(),
					TestPropsValues.getGroupId(), dsRequest.getDSRequestId())));

		Map<String, Object> notificationRecipientSettingsMap =
			NotificationRecipientSettingUtil.
				getNotificationRecipientSettingsMap(notificationQueueEntry);

		Assert.assertEquals(
			user.getEmailAddress(),
			notificationRecipientSettingsMap.get(
				NotificationRecipientSettingConstants.NAME_TO));

		dsRequest = _addDSRequest(
			user.getEmailAddress(), DSRequestConstants.STATUS_SENT);

		NotificationTemplateLocalService notificationTemplateLocalService =
			(NotificationTemplateLocalService)
				ReflectionTestUtil.getAndSetFieldValue(
					_dsRequestManager, "_notificationTemplateLocalService",
					ProxyUtil.newProxyInstance(
						NotificationTemplateLocalService.class.getClassLoader(),
						new Class<?>[] {NotificationTemplateLocalService.class},
						(proxy, method, arguments) -> null));

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.digital.signature.internal.manager." +
					"DSRequestManagerImpl",
				LoggerTestUtil.WARN)) {

			_dsRequestManager.sendDSRequestNotifications(
				dsRequest.getDSRequestId());

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_notificationTemplateLocalService",
				notificationTemplateLocalService);
		}

		notificationQueueEntries = _getNotificationQueueEntries(dsRequest);

		Assert.assertEquals(
			notificationQueueEntries.toString(), 0,
			notificationQueueEntries.size());
	}

	@Test
	public void testSendSignatureReminders() throws Exception {
		DSEnvelope dsEnvelope = _createDSEnvelope();

		DSRecipient dsRecipient1 = _createDSRecipient(UserTestUtil.addUser());

		dsRecipient1.setStatus(DSRequestConstants.STATUS_SENT);

		User user = UserTestUtil.addUser();

		DSRecipient dsRecipient2 = _createDSRecipient(user);

		dsRecipient2.setStatus(DSRequestConstants.STATUS_CREATED);

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(dsRecipient1, dsRecipient2));

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> dsEnvelope));

		try {
			DSRequest dsRequest1 = _addDSRequestObjectEntries(
				DSRequestConstants.STATUS_SENT);
			DSRequest dsRequest2 = _addDSRequestObjectEntries(
				DSRequestConstants.STATUS_VOIDED);
			DSRequest dsRequest3 = _addDSRequest(
				dsEnvelope, RandomTestUtil.randomInt());

			dsRecipient1.setStatus(DSRequestConstants.STATUS_COMPLETED);
			dsRecipient2.setStatus(DSRequestConstants.STATUS_SENT);

			_dsRequestManager.sendSignatureReminders(
				TestPropsValues.getCompanyId());

			List<NotificationQueueEntry> notificationQueueEntries =
				_getNotificationQueueEntries(dsRequest1);

			Assert.assertEquals(
				notificationQueueEntries.toString(), 1,
				notificationQueueEntries.size());

			notificationQueueEntries = _getNotificationQueueEntries(dsRequest2);

			Assert.assertEquals(
				notificationQueueEntries.toString(), 0,
				notificationQueueEntries.size());

			notificationQueueEntries = _getNotificationQueueEntries(dsRequest3);

			Assert.assertEquals(
				notificationQueueEntries.toString(), 1,
				notificationQueueEntries.size());

			Map<String, Object> notificationRecipientSettingsMap =
				NotificationRecipientSettingUtil.
					getNotificationRecipientSettingsMap(
						notificationQueueEntries.get(0));

			Assert.assertEquals(
				user.getEmailAddress(),
				notificationRecipientSettingsMap.get(
					NotificationRecipientSettingConstants.NAME_TO));

			try (CompanyConfigurationTemporarySwapper
					companyConfigurationTemporarySwapper =
						new CompanyConfigurationTemporarySwapper(
							TestPropsValues.getCompanyId(),
							DigitalSignatureConfiguration.class.getName(),
							HashMapDictionaryBuilder.<String, Object>put(
								"signatureReminderEnabled", false
							).build())) {

				_dsRequestManager.sendSignatureReminders(
					TestPropsValues.getCompanyId());
			}

			notificationQueueEntries = _getNotificationQueueEntries(dsRequest1);

			Assert.assertEquals(
				notificationQueueEntries.toString(), 1,
				notificationQueueEntries.size());

			dsEnvelope.setStatus(DSRequestConstants.STATUS_VOIDED);

			_dsRequestManager.sendSignatureReminders(
				TestPropsValues.getCompanyId());

			notificationQueueEntries = _getNotificationQueueEntries(dsRequest1);

			Assert.assertEquals(
				notificationQueueEntries.toString(), 1,
				notificationQueueEntries.size());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}
	}

	@Test
	public void testUpdateDSRequest() throws Exception {
		LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(
			System.currentTimeMillis() / 1000, 0, ZoneOffset.UTC);

		_testUpdateDSRequest(
			DSRequestConstants.STATUS_CREATED,
			DSRequestConstants.STATUS_CREATED, localDateTime.plusDays(1),
			"autoresponded", RandomTestUtil.randomString(), localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_DECLINED,
			DSRequestConstants.STATUS_DECLINED, localDateTime.plusDays(1),
			"declined", "declined", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_EXPIRED,
			DSRequestConstants.STATUS_EXPIRED, localDateTime, "sent", "voided",
			localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_SENT, DSRequestConstants.STATUS_SENT,
			localDateTime.plusDays(1), "delivered", "delivered", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_SIGNED, DSRequestConstants.STATUS_EXPIRED,
			localDateTime, "signed", "voided", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_SIGNED, DSRequestConstants.STATUS_SENT,
			localDateTime.plusDays(1), "signed", "signed", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_SIGNED, DSRequestConstants.STATUS_VOIDED,
			localDateTime.plusDays(1), "signed", "voided", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_VOIDED, DSRequestConstants.STATUS_VOIDED,
			localDateTime.plusDays(1), "sent", "voided", localDateTime);

		_testUpdateDSRequestWhenNextRecipientIsSent();
		_testUpdateDSRequestWhenObjectEntryIsNotDSRequest();
		_testUpdateDSRequestWhenRecipientStatusesDiffer();
		_testUpdateDSRequestWhenRequestIsCompleted();
		_testUpdateDSRequestWhenSignedDocumentIsNotPDF();
	}

	private DSRequest _addDSRequest(DSEnvelope dsEnvelope, FileEntry fileEntry)
		throws Exception {

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> {
						if (Objects.equals(method.getName(), "addDSEnvelope")) {
							return arguments[2];
						}

						return null;
					}));

		try {
			return _dsRequestManager.addDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				TestPropsValues.getUserId(), dsEnvelope,
				new long[] {fileEntry.getFileEntryId()});
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}
	}

	private DSRequest _addDSRequest(DSEnvelope dsEnvelope, long... fileEntryIds)
		throws Exception {

		return ReflectionTestUtil.invoke(
			_dsRequestManager, "_addDSRequest",
			new Class<?>[] {
				long.class, long.class, long.class, DSEnvelope.class,
				long[].class
			},
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), dsEnvelope, fileEntryIds);
	}

	private DSRequest _addDSRequest(String emailAddress, String status)
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		List<DSRecipient> dsRecipients = dsEnvelope.getDSRecipients();

		DSRecipient dsRecipient = dsRecipients.get(0);

		dsRecipient.setEmailAddress(emailAddress);

		dsEnvelope.setStatus(status);

		return _addDSRequest(dsEnvelope, RandomTestUtil.randomInt());
	}

	private DSRequest _addDSRequestObjectEntries(String requestStatus)
		throws Exception {

		String languageId = LocaleUtil.toLanguageId(
			LocaleUtil.getSiteDefault());
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId());

		ObjectEntry dsRequestObjectEntry =
			_objectEntryLocalService.addObjectEntry(
				0, TestPropsValues.getUserId(),
				_dsRequestObjectDefinition.getObjectDefinitionId(), 0,
				languageId,
				HashMapBuilder.<String, Serializable>put(
					"emailSubject", RandomTestUtil.randomString()
				).put(
					"requestStatus", requestStatus
				).put(
					"siteGroupId", TestPropsValues.getGroupId()
				).build(),
				serviceContext);

		_objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			_dsRequestDocumentObjectDefinition.getObjectDefinitionId(), 0,
			languageId,
			HashMapBuilder.<String, Serializable>put(
				"fileEntryId", RandomTestUtil.randomInt()
			).put(
				"r_dsRequestToDSRequestDocuments_l_dsRequestId",
				dsRequestObjectEntry.getObjectEntryId()
			).build(),
			serviceContext);

		_objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			_dsRequestRecipientObjectDefinition.getObjectDefinitionId(), 0,
			languageId,
			HashMapBuilder.<String, Serializable>put(
				"emailAddress", RandomTestUtil.randomString() + "@liferay.com"
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"r_dsRequestToDSRequestRecipients_l_dsRequestId",
				dsRequestObjectEntry.getObjectEntryId()
			).put(
				"r_userToDSRequestRecipients_userId",
				TestPropsValues.getUserId()
			).put(
				"requestRecipientStatus", DSRequestConstants.STATUS_SENT
			).build(),
			serviceContext);

		return _dsRequestManager.fetchDSRequest(
			TestPropsValues.getCompanyId(),
			dsRequestObjectEntry.getObjectEntryId());
	}

	private FileEntry _addFileEntry() throws Exception {
		return _dlAppLocalService.addFileEntry(
			null, TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".txt", ContentTypes.TEXT_PLAIN,
			TestDataConstants.TEST_BYTE_ARRAY, null, null, null,
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId()));
	}

	private DSEnvelope _createDSEnvelope() {
		DSEnvelope dsEnvelope = new DSEnvelope();

		dsEnvelope.setDSEnvelopeId(RandomTestUtil.randomString());
		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(_createDSRecipient(), _createDSRecipient()));
		dsEnvelope.setEmailSubject(RandomTestUtil.randomString());
		dsEnvelope.setStatus("sent");

		return dsEnvelope;
	}

	private DSEnvelope _createDSEnvelope(User user) {
		DSEnvelope dsEnvelope = _createDSEnvelope();

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(_createDSRecipient(user)));

		return dsEnvelope;
	}

	private DSRecipient _createDSRecipient() {
		DSRecipient dsRecipient = new DSRecipient();

		dsRecipient.setEmailAddress(
			RandomTestUtil.randomString() + "@liferay.com");
		dsRecipient.setName(RandomTestUtil.randomString());

		return dsRecipient;
	}

	private DSRecipient _createDSRecipient(User user) {
		DSRecipient dsRecipient = _createDSRecipient();

		dsRecipient.setDSClientUserId(String.valueOf(user.getUserId()));
		dsRecipient.setEmailAddress(user.getEmailAddress());

		return dsRecipient;
	}

	private List<Map<String, Serializable>> _getDSRequestRecipientValuesList(
			Map<String, Serializable> values)
		throws Exception {

		long dsRequestId = MapUtil.getLong(
			values, _dsRequestObjectDefinition.getPKObjectFieldName());

		return _getValuesList(
			"(r_dsRequestToDSRequestRecipients_l_dsRequestId eq '" +
				dsRequestId + "')",
			_dsRequestRecipientObjectDefinition);
	}

	private Map<String, Serializable> _getDSRequestValues(DSEnvelope dsEnvelope)
		throws Exception {

		List<Map<String, Serializable>> valuesList = _getValuesList(
			"(externalReferenceCode eq '" + dsEnvelope.getDSEnvelopeId() + "')",
			_dsRequestObjectDefinition);

		Assert.assertEquals(valuesList.toString(), 1, valuesList.size());

		return valuesList.get(0);
	}

	private List<NotificationQueueEntry> _getNotificationQueueEntries(
		DSRequest dsRequest) {

		return ListUtil.filter(
			_notificationQueueEntryLocalService.getNotificationQueueEntries(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS),
			notificationQueueEntry ->
				Objects.equals(
					notificationQueueEntry.getClassName(),
					DSRequest.class.getName()) &&
				(notificationQueueEntry.getClassPK() ==
					dsRequest.getDSRequestId()));
	}

	private List<Map<String, Serializable>> _getValuesList(
			String filterString, ObjectDefinition objectDefinition)
		throws Exception {

		return _objectEntryLocalService.getValuesList(
			0, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			_filterFactory.create(filterString, objectDefinition), null,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	private void _testAddDSRequestWhenDSEnvelopeIdIsNull() throws Exception {
		DSEnvelope dsEnvelope = _createDSEnvelope();

		dsEnvelope.setDSEnvelopeId(null);

		DSRecipient dsRecipient = _createDSRecipient();

		dsEnvelope.setDSRecipients(ListUtil.fromArray(dsRecipient));

		AssertUtils.assertFailure(
			PortalException.class,
			"A signature request requires a DocuSign envelope ID",
			() -> _addDSRequest(dsEnvelope, RandomTestUtil.randomInt()));

		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(emailAddress eq '" + dsRecipient.getEmailAddress() + "')",
				_dsRequestRecipientObjectDefinition));
	}

	private void _testAddDSRequestWhenDSRecipientUserIsDeleted()
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		User user = UserTestUtil.addUser();

		DSRecipient dsRecipient = _createDSRecipient(user);

		dsEnvelope.setDSRecipients(ListUtil.fromArray(dsRecipient));

		_addDSRequest(dsEnvelope, RandomTestUtil.randomInt());

		String filterString =
			"(emailAddress eq '" + dsRecipient.getEmailAddress() + "')";

		List<Map<String, Serializable>> dsRequestRecipientValuesList =
			_getValuesList(filterString, _dsRequestRecipientObjectDefinition);

		Assert.assertEquals(
			dsRequestRecipientValuesList.toString(), 1,
			dsRequestRecipientValuesList.size());
		Assert.assertEquals(
			user.getUserId(),
			MapUtil.getLong(
				dsRequestRecipientValuesList.get(0),
				"r_userToDSRequestRecipients_userId"));

		_userLocalService.deleteUser(user);

		dsRequestRecipientValuesList = _getValuesList(
			filterString, _dsRequestRecipientObjectDefinition);

		Assert.assertEquals(
			dsRequestRecipientValuesList.toString(), 1,
			dsRequestRecipientValuesList.size());

		Map<String, Serializable> dsRequestRecipientValues =
			dsRequestRecipientValuesList.get(0);

		Assert.assertEquals(
			user.getEmailAddress(),
			MapUtil.getString(dsRequestRecipientValues, "emailAddress"));
		Assert.assertEquals(
			0,
			MapUtil.getLong(
				dsRequestRecipientValues,
				"r_userToDSRequestRecipients_userId"));
	}

	private void _testAddDSRequestWhenEmbeddedViewIsDisabled()
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope(TestPropsValues.getUser());
		FileEntry fileEntry = _addFileEntry();

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						DigitalSignatureConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"embeddedViewEnabled", false
						).put(
							"enabled", true
						).put(
							"siteSettingsStrategy", "always-inherit"
						).build())) {

			AssertUtils.assertFailure(
				PortalException.class,
				"Digital signatures are not enabled for group " +
					TestPropsValues.getGroupId(),
				() -> _addDSRequest(dsEnvelope, fileEntry));
		}

		DSRequest dsRequest = _addDSRequest(dsEnvelope, fileEntry);

		Assert.assertEquals(
			dsEnvelope.getDSEnvelopeId(), dsRequest.getExternalReferenceCode());
	}

	private void _testAddDSRequestWhenExpirationIsInvalid() throws Exception {
		DSEnvelope dsEnvelope = _createDSEnvelope(TestPropsValues.getUser());

		dsEnvelope.setExpireAfter(5);
		dsEnvelope.setExpireWarn(-1);

		FileEntry fileEntry = _addFileEntry();

		AssertUtils.assertFailure(
			PortalException.class,
			"Days until expiration and days to warn signers must not be " +
				"negative",
			() -> _addDSRequest(dsEnvelope, fileEntry));

		dsEnvelope.setExpireWarn(5);

		AssertUtils.assertFailure(
			PortalException.class,
			"Days to warn signers must be fewer than days until expiration",
			() -> _addDSRequest(dsEnvelope, fileEntry));

		dsEnvelope.setExpireWarn(4);

		DSRequest dsRequest = _addDSRequest(dsEnvelope, fileEntry);

		Assert.assertEquals(
			dsEnvelope.getDSEnvelopeId(), dsRequest.getExternalReferenceCode());
	}

	private void _testAddDSRequestWhenFeatureFlagIsDisabled() throws Exception {
		DSEnvelope dsEnvelope = _createDSEnvelope(TestPropsValues.getUser());

		try (SafeCloseable safeCloseable =
				FeatureFlagTestUtil.setFeatureFlagsWithSafeCloseable(
					false, "LPD-69290")) {

			AssertUtils.assertFailure(
				PortalException.class,
				"Digital signatures are not enabled for group " +
					TestPropsValues.getGroupId(),
				() -> _dsRequestManager.addDSRequest(
					TestPropsValues.getCompanyId(),
					TestPropsValues.getGroupId(), TestPropsValues.getUserId(),
					dsEnvelope, new long[0]));
		}

		FileEntry fileEntry = _addFileEntry();

		DSRequest dsRequest = _addDSRequest(dsEnvelope, fileEntry);

		Assert.assertEquals(
			dsEnvelope.getDSEnvelopeId(), dsRequest.getExternalReferenceCode());
	}

	private void _testAddDSRequestWhenFileEntryIsInvalid() throws Exception {
		AssertUtils.assertFailure(
			PortalException.class,
			"A signature request must have at least one file entry",
			() -> _dsRequestManager.addDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				TestPropsValues.getUserId(), new DSEnvelope(), new long[0]));

		DSEnvelope dsEnvelope1 = _createDSEnvelope();
		FileEntry fileEntry = _addFileEntry();

		DSRequest dsRequest1 = _addDSRequest(
			dsEnvelope1, fileEntry.getFileEntryId());

		DSEnvelope dsEnvelope2 = _createDSEnvelope(TestPropsValues.getUser());

		AssertUtils.assertFailure(
			PortalException.class,
			StringBundler.concat(
				"File entry ", fileEntry.getFileEntryId(),
				" already has a signature request with status \"",
				DSRequestConstants.STATUS_SENT, "\""),
			() -> _addDSRequest(dsEnvelope2, fileEntry));

		dsEnvelope1.setStatus(DSRequestConstants.STATUS_VOIDED);

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> {
						if (Objects.equals(method.getName(), "addDSEnvelope")) {
							return arguments[2];
						}

						return dsEnvelope1;
					}));

		try {
			_dsRequestManager.updateDSRequest(dsRequest1.getDSRequestId());

			DSRequest dsRequest2 = _dsRequestManager.addDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				TestPropsValues.getUserId(), dsEnvelope2,
				new long[] {fileEntry.getFileEntryId()});

			Assert.assertEquals(
				dsEnvelope2.getDSEnvelopeId(),
				dsRequest2.getExternalReferenceCode());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}
	}

	private void _testAddDSRequestWhenRecipientIsInvalid() throws Exception {
		DSEnvelope dsEnvelope = _createDSEnvelope();

		User user = UserTestUtil.addUser();

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(
				_createDSRecipient(user), _createDSRecipient(user)));

		FileEntry fileEntry = _addFileEntry();

		AssertUtils.assertFailure(
			PortalException.class,
			"User " + user.getUserId() +
				" is already a signature request recipient",
			() -> _addDSRequest(dsEnvelope, fileEntry));

		User guestUser = _userLocalService.getGuestUser(
			TestPropsValues.getCompanyId());

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(_createDSRecipient(guestUser)));

		AssertUtils.assertFailure(
			PortalException.class,
			"User " + guestUser.getUserId() +
				" cannot be a signature request recipient",
			() -> _addDSRequest(dsEnvelope, fileEntry));

		User inactiveUser = UserTestUtil.addUser();

		inactiveUser = _userLocalService.updateStatus(
			inactiveUser.getUserId(), WorkflowConstants.STATUS_INACTIVE,
			ServiceContextTestUtil.getServiceContext());

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(_createDSRecipient(inactiveUser)));

		AssertUtils.assertFailure(
			PortalException.class,
			"User " + inactiveUser.getUserId() +
				" cannot be a signature request recipient",
			() -> _addDSRequest(dsEnvelope, fileEntry));

		DSRecipient dsRecipient = _createDSRecipient();

		dsEnvelope.setDSRecipients(ListUtil.fromArray(dsRecipient));

		AssertUtils.assertFailure(
			NoSuchUserException.class,
			StringBundler.concat(
				"No User exists with the key {companyId=",
				TestPropsValues.getCompanyId(), ", emailAddress=",
				StringUtil.toLowerCase(dsRecipient.getEmailAddress()), "}"),
			() -> _addDSRequest(dsEnvelope, fileEntry));

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(_createDSRecipient(user)));

		DSRequest dsRequest = _addDSRequest(dsEnvelope, fileEntry);

		Assert.assertEquals(
			dsEnvelope.getDSEnvelopeId(), dsRequest.getExternalReferenceCode());
	}

	private void _testFetchDSRequest() throws Exception {
		DSRequest dsRequest1 = _addDSRequest(
			_createDSEnvelope(), RandomTestUtil.randomInt());

		Assert.assertNull(
			_dsRequestManager.fetchDSRequest(
				RandomTestUtil.randomLong(), dsRequest1.getDSRequestId()));

		DSRequest dsRequest2 = _dsRequestManager.fetchDSRequest(
			TestPropsValues.getCompanyId(), dsRequest1.getDSRequestId());

		Assert.assertEquals(
			dsRequest1.getDSRequestId(), dsRequest2.getDSRequestId());
		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT, dsRequest2.getStatus());
	}

	private void _testFetchDSRequestWhenExpirationDateIsPast()
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		LocalDateTime localDateTime = LocalDateTime.now();

		dsEnvelope.setExpireLocalDateTime(localDateTime.minusDays(1));

		DSRequest dsRequest = _addDSRequest(
			dsEnvelope, RandomTestUtil.randomInt());

		dsRequest = _dsRequestManager.fetchDSRequest(
			TestPropsValues.getCompanyId(), dsRequest.getDSRequestId());

		Assert.assertTrue(dsRequest.isRequestable());
		Assert.assertTrue(dsRequest.isTerminal());
		Assert.assertEquals(
			DSRequestConstants.STATUS_EXPIRED, dsRequest.getStatus());
	}

	private void _testFetchDSRequestWhenSiteOverridesInstanceSettings()
		throws Exception {

		DSRequest dsRequest = _addDSRequest(
			_createDSEnvelope(), RandomTestUtil.randomInt());

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						DigitalSignatureConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"embeddedViewEnabled", false
						).put(
							"enabled", true
						).put(
							"siteSettingsStrategy", "always-override"
						).build())) {

			Assert.assertNull(
				_dsRequestManager.fetchDSRequest(
					TestPropsValues.getCompanyId(),
					dsRequest.getDSRequestId()));

			try (GroupConfigurationTemporarySwapper
					groupConfigurationTemporarySwapper =
						new GroupConfigurationTemporarySwapper(
							TestPropsValues.getGroupId(),
							DigitalSignatureConfiguration.class.getName(),
							HashMapDictionaryBuilder.<String, Object>put(
								"embeddedViewEnabled", true
							).put(
								"enabled", true
							).build())) {

				DSRequest fetchedDSRequest = _dsRequestManager.fetchDSRequest(
					TestPropsValues.getCompanyId(), dsRequest.getDSRequestId());

				Assert.assertEquals(
					dsRequest.getDSRequestId(),
					fetchedDSRequest.getDSRequestId());
			}
		}
	}

	private void _testUpdateDSRequest(
			String expectedRequestRecipientStatus, String expectedRequestStatus,
			LocalDateTime expireLocalDateTime, String requestRecipientStatus,
			String requestStatus, LocalDateTime statusChangedLocalDateTime)
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(
				_createDSRecipient(UserTestUtil.addUser()),
				_createDSRecipient(UserTestUtil.addUser())));
		dsEnvelope.setStatus(DSRequestConstants.STATUS_CREATED);

		for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
			dsRecipient.setStatus(DSRequestConstants.STATUS_CREATED);
		}

		DSRequest dsRequest = _addDSRequest(
			dsEnvelope, RandomTestUtil.randomInt());

		dsEnvelope.setExpireLocalDateTime(expireLocalDateTime);
		dsEnvelope.setStatus(requestStatus);
		dsEnvelope.setStatusChangedLocalDateTime(statusChangedLocalDateTime);

		for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
			dsRecipient.setStatus(requestRecipientStatus);
			dsRecipient.setStatusLocalDateTime(statusChangedLocalDateTime);
		}

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> {
						Assert.assertEquals(
							TestPropsValues.getCompanyId(), arguments[0]);
						Assert.assertEquals(
							TestPropsValues.getGroupId(), arguments[1]);
						Assert.assertEquals(
							dsEnvelope.getDSEnvelopeId(), arguments[2]);

						return dsEnvelope;
					}));

		User user = UserTestUtil.addUser();

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user, PermissionCheckerFactoryUtil.create(user))) {

			_dsRequestManager.updateDSRequest(dsRequest.getDSRequestId());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		Map<String, Serializable> dsRequestValues = _getDSRequestValues(
			dsEnvelope);

		Assert.assertEquals(
			Date.from(expireLocalDateTime.toInstant(ZoneOffset.UTC)),
			dsRequestValues.get("requestExpirationDate"));
		Assert.assertEquals(
			expectedRequestStatus,
			MapUtil.getString(dsRequestValues, "requestStatus"));
		Assert.assertEquals(
			Date.from(statusChangedLocalDateTime.toInstant(ZoneOffset.UTC)),
			dsRequestValues.get("requestStatusDate"));

		List<Map<String, Serializable>> dsRequestRecipientValuesList =
			_getDSRequestRecipientValuesList(dsRequestValues);

		Assert.assertEquals(
			dsRequestRecipientValuesList.toString(), 2,
			dsRequestRecipientValuesList.size());

		for (Map<String, Serializable> values : dsRequestRecipientValuesList) {
			Assert.assertEquals(
				expectedRequestRecipientStatus,
				MapUtil.getString(values, "requestRecipientStatus"));
			Assert.assertEquals(
				Date.from(statusChangedLocalDateTime.toInstant(ZoneOffset.UTC)),
				values.get("requestRecipientStatusDate"));
		}
	}

	private void _testUpdateDSRequestWhenNextRecipientIsSent()
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		DSRecipient dsRecipient1 = _createDSRecipient(UserTestUtil.addUser());

		dsRecipient1.setStatus(DSRequestConstants.STATUS_SENT);

		User user = UserTestUtil.addUser();

		DSRecipient dsRecipient2 = _createDSRecipient(user);

		dsRecipient2.setStatus(DSRequestConstants.STATUS_CREATED);

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(dsRecipient1, dsRecipient2));

		DSRequest dsRequest = _addDSRequest(
			dsEnvelope, RandomTestUtil.randomInt());

		dsRecipient1.setStatus(DSRequestConstants.STATUS_COMPLETED);
		dsRecipient2.setStatus(DSRequestConstants.STATUS_SENT);

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> dsEnvelope));

		try {
			_dsRequestManager.updateDSRequest(dsRequest.getDSRequestId());
			_dsRequestManager.updateDSRequest(dsRequest.getDSRequestId());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		List<NotificationQueueEntry> notificationQueueEntries =
			_getNotificationQueueEntries(dsRequest);

		Assert.assertEquals(
			notificationQueueEntries.toString(), 1,
			notificationQueueEntries.size());

		Map<String, Object> notificationRecipientSettingsMap =
			NotificationRecipientSettingUtil.
				getNotificationRecipientSettingsMap(
					notificationQueueEntries.get(0));

		Assert.assertEquals(
			user.getEmailAddress(),
			notificationRecipientSettingsMap.get(
				NotificationRecipientSettingConstants.NAME_TO));
	}

	private void _testUpdateDSRequestWhenObjectEntryIsNotDSRequest()
		throws Exception {

		long fileEntryId = RandomTestUtil.randomInt();

		_addDSRequest(_createDSEnvelope(), fileEntryId);

		List<Map<String, Serializable>> dsRequestDocumentValuesList =
			_getValuesList(
				"(fileEntryId eq " + fileEntryId + ")",
				_dsRequestDocumentObjectDefinition);

		long dsRequestDocumentId = MapUtil.getLong(
			dsRequestDocumentValuesList.get(0),
			_dsRequestDocumentObjectDefinition.getPKObjectFieldName());

		AssertUtils.assertFailure(
			NoSuchObjectEntryException.class,
			"No signature request exists with the primary key " +
				dsRequestDocumentId,
			() -> _dsRequestManager.updateDSRequest(dsRequestDocumentId));
	}

	private void _testUpdateDSRequestWhenRecipientStatusesDiffer()
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		User completedUser = UserTestUtil.addUser();

		DSRecipient completedDSRecipient = _createDSRecipient(completedUser);

		User deliveredUser = UserTestUtil.addUser();

		DSRecipient deliveredDSRecipient = _createDSRecipient(deliveredUser);

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(completedDSRecipient, deliveredDSRecipient));

		DSRequest dsRequest = _addDSRequest(
			dsEnvelope, RandomTestUtil.randomInt());

		LocalDateTime completedLocalDateTime = LocalDateTime.ofEpochSecond(
			System.currentTimeMillis() / 1000, 0, ZoneOffset.UTC);

		completedDSRecipient.setStatus(DSRequestConstants.STATUS_COMPLETED);
		completedDSRecipient.setStatusLocalDateTime(completedLocalDateTime);

		LocalDateTime deliveredLocalDateTime =
			completedLocalDateTime.minusHours(1);

		deliveredDSRecipient.setStatus("delivered");
		deliveredDSRecipient.setStatusLocalDateTime(deliveredLocalDateTime);

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(deliveredDSRecipient, completedDSRecipient));

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> dsEnvelope));

		try {
			_dsRequestManager.updateDSRequest(dsRequest.getDSRequestId());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		Map<Long, Map<String, Serializable>> recipientValuesMap =
			new HashMap<>();

		for (Map<String, Serializable> values :
				_getDSRequestRecipientValuesList(
					_getDSRequestValues(dsEnvelope))) {

			recipientValuesMap.put(
				MapUtil.getLong(values, "r_userToDSRequestRecipients_userId"),
				values);
		}

		Map<String, Serializable> dsRequestRecipientValues =
			recipientValuesMap.get(completedUser.getUserId());

		Assert.assertEquals(
			DSRequestConstants.STATUS_COMPLETED,
			MapUtil.getString(
				dsRequestRecipientValues, "requestRecipientStatus"));
		Assert.assertEquals(
			Date.from(completedLocalDateTime.toInstant(ZoneOffset.UTC)),
			dsRequestRecipientValues.get("requestRecipientStatusDate"));

		dsRequestRecipientValues = recipientValuesMap.get(
			deliveredUser.getUserId());

		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT,
			MapUtil.getString(
				dsRequestRecipientValues, "requestRecipientStatus"));
		Assert.assertEquals(
			Date.from(deliveredLocalDateTime.toInstant(ZoneOffset.UTC)),
			dsRequestRecipientValues.get("requestRecipientStatusDate"));
	}

	private void _testUpdateDSRequestWhenRequestIsCompleted() throws Exception {
		Group group = GroupTestUtil.addGroup();
		String portletId = RandomTestUtil.randomString();

		FileEntry fileEntry = _portletFileRepository.addPortletFileEntry(
			group.getGroupId(), TestPropsValues.getUserId(),
			Group.class.getName(), group.getGroupId(), portletId,
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			TestDataConstants.TEST_BYTE_ARRAY, "contract.txt",
			ContentTypes.TEXT_PLAIN, false);

		String description = RandomTestUtil.randomString();

		fileEntry = _dlAppLocalService.updateFileEntry(
			TestPropsValues.getUserId(), fileEntry.getFileEntryId(), null, null,
			fileEntry.getTitle(), null, description, null,
			DLVersionNumberIncrease.NONE, (byte[])null, null, null, null,
			ServiceContextTestUtil.getServiceContext(group.getGroupId()));

		_portletFileRepository.addPortletFileEntry(
			group.getGroupId(), TestPropsValues.getUserId(),
			Group.class.getName(), group.getGroupId(), portletId,
			fileEntry.getFolderId(), TestDataConstants.TEST_BYTE_ARRAY,
			"contract.pdf", ContentTypes.APPLICATION_PDF, false);

		DSEnvelope dsEnvelope = _createDSEnvelope();

		DSRequest dsRequest = _addDSRequest(
			dsEnvelope, fileEntry.getFileEntryId());

		dsEnvelope.setStatus(DSRequestConstants.STATUS_COMPLETED);

		byte[] bytes = ArrayUtil.append(
			"%PDF-".getBytes(), RandomTestUtil.randomBytes());

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> {
						if (Objects.equals(
								method.getName(), "getSignedDocument")) {

							return bytes;
						}

						return dsEnvelope;
					}));

		try {
			_dsRequestManager.updateDSRequest(dsRequest.getDSRequestId());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		Assert.assertEquals(
			DSRequestConstants.STATUS_COMPLETED,
			MapUtil.getString(
				_getDSRequestValues(dsEnvelope), "requestStatus"));

		fileEntry = _dlAppLocalService.getFileEntry(fileEntry.getFileEntryId());

		Assert.assertArrayEquals(
			bytes, FileUtil.getBytes(fileEntry.getContentStream()));
		Assert.assertEquals(description, fileEntry.getDescription());
		Assert.assertEquals("contract (signed).pdf", fileEntry.getFileName());
		Assert.assertEquals("2.0", fileEntry.getVersion());

		FileVersion fileVersion = fileEntry.getLatestFileVersion();

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, fileVersion.getStatus());
	}

	private void _testUpdateDSRequestWhenSignedDocumentIsNotPDF()
		throws Exception {

		FileEntry fileEntry = _dlAppLocalService.addFileEntry(
			null, TestPropsValues.getUserId(), TestPropsValues.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".pdf",
			ContentTypes.APPLICATION_PDF, TestDataConstants.TEST_BYTE_ARRAY,
			null, null, null,
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId()));

		DSEnvelope dsEnvelope = _createDSEnvelope();

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(
				_createDSRecipient(UserTestUtil.addUser()),
				_createDSRecipient(UserTestUtil.addUser())));

		DSRequest dsRequest = _addDSRequest(
			dsEnvelope, fileEntry.getFileEntryId());

		for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
			dsRecipient.setStatus(DSRequestConstants.STATUS_COMPLETED);
		}

		dsEnvelope.setStatus(DSRequestConstants.STATUS_COMPLETED);

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> {
						if (Objects.equals(
								method.getName(), "getSignedDocument")) {

							return "{\"errorCode\":\"NOT_FOUND\"}".getBytes();
						}

						return dsEnvelope;
					}));

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.digital.signature.internal.manager." +
					"DSRequestManagerImpl",
				LoggerTestUtil.ERROR)) {

			_dsRequestManager.updateDSRequest(dsRequest.getDSRequestId());

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		dsRequest = _dsRequestManager.fetchDSRequest(
			TestPropsValues.getCompanyId(), dsRequest.getDSRequestId());

		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT, dsRequest.getStatus());

		for (DSRequestRecipient dsRequestRecipient :
				dsRequest.getDSRequestRecipients()) {

			Assert.assertEquals(
				DSRequestConstants.STATUS_SENT, dsRequestRecipient.getStatus());
		}

		fileEntry = _dlAppLocalService.getFileEntry(fileEntry.getFileEntryId());

		Assert.assertEquals("1.0", fileEntry.getVersion());

		ReflectionTestUtil.setFieldValue(
			_dsRequestManager, "_dsEnvelopeManager",
			ProxyUtil.newProxyInstance(
				DSEnvelopeManager.class.getClassLoader(),
				new Class<?>[] {DSEnvelopeManager.class},
				(proxy, method, arguments) -> {
					if (Objects.equals(method.getName(), "getSignedDocument")) {
						return ArrayUtil.append(
							"%PDF-".getBytes(), RandomTestUtil.randomBytes());
					}

					return dsEnvelope;
				}));

		try {
			_dsRequestManager.updateDSRequest(dsRequest.getDSRequestId());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		dsRequest = _dsRequestManager.fetchDSRequest(
			TestPropsValues.getCompanyId(), dsRequest.getDSRequestId());

		Assert.assertEquals(
			DSRequestConstants.STATUS_COMPLETED, dsRequest.getStatus());

		for (DSRequestRecipient dsRequestRecipient :
				dsRequest.getDSRequestRecipients()) {

			Assert.assertEquals(
				DSRequestConstants.STATUS_COMPLETED,
				dsRequestRecipient.getStatus());
		}

		fileEntry = _dlAppLocalService.getFileEntry(fileEntry.getFileEntryId());

		Assert.assertEquals("2.0", fileEntry.getVersion());
	}

	private CompanyConfigurationTemporarySwapper
		_companyConfigurationTemporarySwapper;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	private ObjectDefinition _dsRequestDocumentObjectDefinition;

	@Inject
	private DSRequestManager _dsRequestManager;

	private ObjectDefinition _dsRequestObjectDefinition;
	private ObjectDefinition _dsRequestRecipientObjectDefinition;

	@Inject(
		filter = "filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT
	)
	private FilterFactory<Predicate> _filterFactory;

	@Inject(filter = "mvc.command.name=/digital_signature/add_ds_envelope")
	private MVCResourceCommand _mvcResourceCommand;

	@Inject
	private NotificationQueueEntryLocalService
		_notificationQueueEntryLocalService;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private PortletFileRepository _portletFileRepository;

	@Inject
	private SignDSURLProvider _signDSURLProvider;

	@Inject
	private UserLocalService _userLocalService;

}