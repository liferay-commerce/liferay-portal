/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.servlet.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.test.util.BatchEngineTestUtil;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.service.CommerceOrderAttachmentLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.commerce.test.util.CommerceOrderAttachmentTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.digital.signature.constants.DigitalSignaturePortletKeys;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.digital.signature.url.SignDSURLProvider;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.servlet.HttpMethods;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.servlet.Servlet;

import java.io.ByteArrayInputStream;

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
 * @author Brian I. Kim
 */
@FeatureFlag("LPD-69290")
@RunWith(Arquillian.class)
public class DSCommerceOrderSignServletTest {

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
			DSCommerceOrderSignServletTest.class,
			new String[] {
				".com.liferay.digital.signature.internal.batch.01.object." +
					"definition"
			});

		CommerceOrderAttachmentTestUtil.initialize(getClass());

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
					"enabled", true
				).put(
					"environment", "sandbox"
				).put(
					"integrationKey", RandomTestUtil.randomString()
				).put(
					"rsaPrivateKey", RandomTestUtil.randomString()
				).put(
					"siteSettingsStrategy", "always-inherit"
				).build());

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

		CommerceOrder commerceOrder =
			_commerceOrderLocalService.addCommerceOrder(
				TestPropsValues.getUserId(), commerceChannel.getGroupId(),
				accountEntry.getAccountEntryId(), commerceCurrency.getCode(),
				0);

		_commerceOrderAttachment =
			_commerceOrderAttachmentLocalService.addCommerceOrderAttachment(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				commerceOrder.getCommerceOrderId(), RandomTestUtil.nextDouble(),
				false, RandomTestUtil.randomString(), "invoice",
				RandomTestUtil.randomString() + ".pdf",
				new ByteArrayInputStream(RandomTestUtil.randomBytes()));
	}

	@After
	public void tearDown() throws Exception {
		_companyConfigurationTemporarySwapper.close();
	}

	@Test
	public void testDoGet() throws Exception {
		String backURL = "/" + RandomTestUtil.randomString();
		long commerceOrderAttachmentId =
			_commerceOrderAttachment.getCommerceOrderAttachmentId();

		Assert.assertEquals(
			backURL,
			_getRedirectedURL(backURL, commerceOrderAttachmentId, null));

		User user = TestPropsValues.getUser();

		Assert.assertEquals(
			backURL,
			_getRedirectedURL(backURL, commerceOrderAttachmentId, user));

		DSRequest dsRequest = _addDSRequest(
			DSRequestConstants.STATUS_SENT, user);

		Assert.assertEquals(
			backURL,
			_getRedirectedURL(backURL, RandomTestUtil.randomLong(), user));

		Assert.assertEquals(
			backURL,
			_getRedirectedURL(
				backURL, commerceOrderAttachmentId, UserTestUtil.addUser()));

		String portletNamespace = _portal.getPortletNamespace(
			DigitalSignaturePortletKeys.SIGN_DIGITAL_SIGNATURE);

		Assert.assertEquals(
			HttpComponentsUtil.addParameter(
				HttpComponentsUtil.addParameter(
					_signDSURLProvider.getURL(
						_group.getCompanyId(), dsRequest.getSiteGroupId(),
						dsRequest.getDSRequestId()),
					"p_p_state", LiferayWindowState.POP_UP.toString()),
				portletNamespace + "backURL", backURL),
			_getRedirectedURL(backURL, commerceOrderAttachmentId, user));

		_addDSRequest(DSRequestConstants.STATUS_COMPLETED, user);

		Assert.assertEquals(
			backURL,
			_getRedirectedURL(backURL, commerceOrderAttachmentId, user));

		User recipientUser = UserTestUtil.addUser();

		_addDSRequest(DSRequestConstants.STATUS_SENT, recipientUser);

		Assert.assertEquals(
			backURL,
			_getRedirectedURL(
				backURL, commerceOrderAttachmentId, recipientUser));
	}

	private DSRequest _addDSRequest(String status, User user) throws Exception {
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
		dsEnvelope.setStatus(status);

		return ReflectionTestUtil.invoke(
			_dsRequestManager, "_addDSRequest",
			new Class<?>[] {
				long.class, long.class, long.class, DSEnvelope.class,
				long[].class
			},
			_group.getCompanyId(), _group.getGroupId(), user.getUserId(),
			dsEnvelope, new long[] {_commerceOrderAttachment.getFileEntryId()});
	}

	private String _getRedirectedURL(
			String backURL, long commerceOrderAttachmentId, User user)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.COMPANY_ID, _group.getCompanyId());

		if (user != null) {
			mockHttpServletRequest.setAttribute(WebKeys.USER, user);
		}

		mockHttpServletRequest.setMethod(HttpMethods.GET);
		mockHttpServletRequest.setParameter("backURL", backURL);
		mockHttpServletRequest.setParameter(
			"commerceOrderAttachmentId",
			String.valueOf(commerceOrderAttachmentId));

		MockHttpServletResponse mockHttpServletResponse =
			new MockHttpServletResponse();

		_servlet.service(mockHttpServletRequest, mockHttpServletResponse);

		return mockHttpServletResponse.getRedirectedUrl();
	}

	private CommerceOrderAttachment _commerceOrderAttachment;

	@Inject
	private CommerceOrderAttachmentLocalService
		_commerceOrderAttachmentLocalService;

	@Inject
	private CommerceOrderLocalService _commerceOrderLocalService;

	private CompanyConfigurationTemporarySwapper
		_companyConfigurationTemporarySwapper;

	@Inject
	private DSRequestManager _dsRequestManager;

	private Group _group;

	@Inject
	private Portal _portal;

	@Inject(
		filter = "osgi.http.whiteboard.servlet.name=com.liferay.commerce.digital.signature.internal.servlet.DSCommerceOrderSignServlet"
	)
	private Servlet _servlet;

	@Inject
	private SignDSURLProvider _signDSURLProvider;

}