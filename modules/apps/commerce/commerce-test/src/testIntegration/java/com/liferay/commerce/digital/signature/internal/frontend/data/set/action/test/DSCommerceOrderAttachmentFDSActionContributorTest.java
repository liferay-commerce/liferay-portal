/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.frontend.data.set.action.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.test.util.BatchEngineTestUtil;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.order.CommerceOrderAttachmentFDSActionContributor;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.service.CommerceOrderAttachmentLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.commerce.test.util.CommerceOrderAttachmentTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.ByteArrayInputStream;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Brian I. Kim
 */
@FeatureFlag("LPD-69290")
@RunWith(Arquillian.class)
public class DSCommerceOrderAttachmentFDSActionContributorTest {

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
			DSCommerceOrderAttachmentFDSActionContributorTest.class,
			new String[] {
				".com.liferay.digital.signature.internal.batch.01.object." +
					"definition"
			});

		CommerceOrderAttachmentTestUtil.initialize(getClass());

		_saveDigitalSignatureConfiguration(true);

		_group = GroupTestUtil.addGroup();

		AccountEntry accountEntry =
			CommerceAccountTestUtil.addPersonAccountEntry(
				TestPropsValues.getUserId(),
				ServiceContextTestUtil.getServiceContext(
					_group.getGroupId(), TestPropsValues.getUserId()));

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(_group.getCompanyId());

		CommerceChannel commerceChannel = CommerceTestUtil.addCommerceChannel(
			_group.getGroupId(), commerceCurrency.getCode());

		_commerceOrder = _commerceOrderLocalService.addCommerceOrder(
			TestPropsValues.getUserId(), commerceChannel.getGroupId(),
			accountEntry.getAccountEntryId(), commerceCurrency.getCode(), 0);

		_commerceOrderAttachment =
			_commerceOrderAttachmentLocalService.addCommerceOrderAttachment(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				_commerceOrder.getCommerceOrderId(),
				RandomTestUtil.nextDouble(), false,
				RandomTestUtil.randomString(), "invoice",
				RandomTestUtil.randomString() + ".pdf",
				new ByteArrayInputStream(RandomTestUtil.randomBytes()));
	}

	@After
	public void tearDown() throws Exception {
		_companyConfigurationTemporarySwapper.close();
	}

	@Test
	public void testGetAdditionalProps() throws Exception {
		_testGetAdditionalProps();
		_testGetAdditionalPropsWhenAttachmentIsRestricted();
		_testGetAdditionalPropsWhenDisabled();
	}

	@Test
	public void testGetFDSActionDropdownItems() throws Exception {
		User user = TestPropsValues.getUser();

		Assert.assertEquals(
			Collections.emptyList(), _getFDSActionDropdownItemIds(user));

		_addDSRequest(_commerceOrderAttachment, user);

		Assert.assertEquals(
			List.of("sign", "view-signature-status"),
			_getFDSActionDropdownItemIds(user));

		Assert.assertEquals(
			List.of("view-signature-status"),
			_getFDSActionDropdownItemIds(UserTestUtil.addUser()));

		_saveDigitalSignatureConfiguration(false);

		Assert.assertEquals(
			Collections.emptyList(), _getFDSActionDropdownItemIds(user));
	}

	private void _addDSRequest(
			CommerceOrderAttachment commerceOrderAttachment, User user)
		throws Exception {

		DSEnvelope dsEnvelope = new DSEnvelope();

		dsEnvelope.setDSEnvelopeId(RandomTestUtil.randomString());

		DSRecipient dsRecipient = new DSRecipient();

		dsRecipient.setDSRecipientId("1");
		dsRecipient.setEmailAddress(user.getEmailAddress());
		dsRecipient.setName(user.getFullName());
		dsRecipient.setRoutingOrder(1);
		dsRecipient.setStatus(DSRequestConstants.STATUS_SENT);

		dsEnvelope.setDSRecipients(ListUtil.fromArray(dsRecipient));

		dsEnvelope.setEmailSubject(RandomTestUtil.randomString());
		dsEnvelope.setStatus(DSRequestConstants.STATUS_SENT);

		ReflectionTestUtil.invoke(
			_dsRequestManager, "_addDSRequest",
			new Class<?>[] {
				long.class, long.class, long.class, DSEnvelope.class,
				long[].class
			},
			_group.getCompanyId(), _group.getGroupId(), user.getUserId(),
			dsEnvelope, new long[] {commerceOrderAttachment.getFileEntryId()});
	}

	private Map<String, Object> _getAdditionalProps(User user)
		throws Exception {

		return _commerceOrderAttachmentFDSActionContributor.getAdditionalProps(
			_commerceOrder, _getMockHttpServletRequest(user));
	}

	private List<String> _getFDSActionDropdownItemIds(User user)
		throws Exception {

		return TransformUtil.transform(
			_commerceOrderAttachmentFDSActionContributor.
				getFDSActionDropdownItems(
					_commerceOrder, _getMockHttpServletRequest(user)),
			fdsActionDropdownItem -> {
				Map<String, Object> data =
					(Map<String, Object>)fdsActionDropdownItem.get("data");

				return MapUtil.getString(data, "id");
			});
	}

	private MockHttpServletRequest _getMockHttpServletRequest(User user)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(_group.getCompanyId()));
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(user));
		themeDisplay.setPortalURL("http://" + RandomTestUtil.randomString());
		themeDisplay.setURLCurrent("/" + RandomTestUtil.randomString());
		themeDisplay.setUser(user);

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		return mockHttpServletRequest;
	}

	private void _saveDigitalSignatureConfiguration(boolean enabled)
		throws Exception {

		if (_companyConfigurationTemporarySwapper != null) {
			_companyConfigurationTemporarySwapper.close();
		}

		_companyConfigurationTemporarySwapper =
			new CompanyConfigurationTemporarySwapper(
				TestPropsValues.getCompanyId(),
				DigitalSignatureConfiguration.class.getName(),
				HashMapDictionaryBuilder.<String, Object>put(
					"accountBaseURI", "https://demo.docusign.net/restapi"
				).put(
					"apiAccountId", RandomTestUtil.randomString()
				).put(
					"apiUsername", RandomTestUtil.randomString()
				).put(
					"embeddedViewEnabled", true
				).put(
					"enabled", enabled
				).put(
					"environment", "sandbox"
				).put(
					"integrationKey", RandomTestUtil.randomString()
				).put(
					"rsaPrivateKey", RandomTestUtil.randomString()
				).put(
					"siteSettingsStrategy", "always-inherit"
				).build());
	}

	private void _testGetAdditionalProps() throws Exception {
		User user = TestPropsValues.getUser();

		Assert.assertEquals(Collections.emptyMap(), _getAdditionalProps(user));

		_addDSRequest(_commerceOrderAttachment, user);

		Map<String, Object> additionalProps = _getAdditionalProps(user);

		String commerceOrderAttachmentId = String.valueOf(
			_commerceOrderAttachment.getCommerceOrderAttachmentId());

		Assert.assertEquals(
			List.of(commerceOrderAttachmentId),
			additionalProps.get("signableIds"));
		Assert.assertEquals(
			HashMapBuilder.put(
				commerceOrderAttachmentId, DSRequestConstants.STATUS_SENT
			).build(),
			additionalProps.get("signatureStatuses"));

		additionalProps = _getAdditionalProps(UserTestUtil.addUser());

		Assert.assertEquals(
			Collections.emptyList(), additionalProps.get("signableIds"));
	}

	private void _testGetAdditionalPropsWhenAttachmentIsRestricted()
		throws Exception {

		CommerceOrderAttachment commerceOrderAttachment =
			_commerceOrderAttachmentLocalService.addCommerceOrderAttachment(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				_commerceOrder.getCommerceOrderId(),
				RandomTestUtil.nextDouble(), true,
				RandomTestUtil.randomString(), "invoice",
				RandomTestUtil.randomString() + ".pdf",
				new ByteArrayInputStream(RandomTestUtil.randomBytes()));

		User user = UserTestUtil.addUser();

		_addDSRequest(commerceOrderAttachment, user);

		Map<String, Object> additionalProps = _getAdditionalProps(user);

		Assert.assertEquals(
			HashMapBuilder.put(
				String.valueOf(
					_commerceOrderAttachment.getCommerceOrderAttachmentId()),
				DSRequestConstants.STATUS_SENT
			).build(),
			additionalProps.get("signatureStatuses"));

		additionalProps = _getAdditionalProps(TestPropsValues.getUser());

		Assert.assertEquals(
			HashMapBuilder.put(
				String.valueOf(
					_commerceOrderAttachment.getCommerceOrderAttachmentId()),
				DSRequestConstants.STATUS_SENT
			).put(
				String.valueOf(
					commerceOrderAttachment.getCommerceOrderAttachmentId()),
				DSRequestConstants.STATUS_SENT
			).build(),
			additionalProps.get("signatureStatuses"));
	}

	private void _testGetAdditionalPropsWhenDisabled() throws Exception {
		_saveDigitalSignatureConfiguration(false);

		Assert.assertEquals(
			Collections.emptyMap(),
			_getAdditionalProps(TestPropsValues.getUser()));
	}

	private CommerceOrder _commerceOrder;
	private CommerceOrderAttachment _commerceOrderAttachment;

	@Inject
	private CommerceOrderAttachmentFDSActionContributor
		_commerceOrderAttachmentFDSActionContributor;

	@Inject
	private CommerceOrderAttachmentLocalService
		_commerceOrderAttachmentLocalService;

	@Inject
	private CommerceOrderLocalService _commerceOrderLocalService;

	private CompanyConfigurationTemporarySwapper
		_companyConfigurationTemporarySwapper;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DSRequestManager _dsRequestManager;

	private Group _group;

}