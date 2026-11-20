/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.payment.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.constants.CommercePaymentEntryConstants;
import com.liferay.commerce.constants.CommercePaymentMethodConstants;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.service.CommerceCurrencyLocalService;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.payment.constants.CommercePaymentIntegrationConstants;
import com.liferay.commerce.payment.integration.CommercePaymentIntegration;
import com.liferay.commerce.payment.method.CommercePaymentMethod;
import com.liferay.commerce.payment.model.CommercePaymentEntry;
import com.liferay.commerce.payment.service.CommercePaymentEntryLocalService;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.headless.commerce.admin.payment.client.dto.v1_0.Payment;
import com.liferay.headless.commerce.admin.payment.client.http.HttpInvoker;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.rule.Inject;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Alessio Antonio Rendina
 */
@RunWith(Arquillian.class)
public class PaymentResourceTest extends BasePaymentResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		User user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			user.getUserId());

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			testCompany.getCompanyId());

		_commerceChannel = _commerceChannelLocalService.addCommerceChannel(
			RandomTestUtil.randomString(),
			AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT, testGroup.getGroupId(),
			RandomTestUtil.randomString(),
			CommerceChannelConstants.CHANNEL_TYPE_SITE, null,
			_commerceCurrency.getCode(), _serviceContext);
	}

	@Ignore
	@Override
	@Test
	public void testGetPaymentsPageWithSortInteger() throws Exception {
		super.testGetPaymentsPageWithSortInteger();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLDeletePayment() throws Exception {
		super.testGraphQLDeletePayment();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLDeletePaymentByExternalReferenceCode()
		throws Exception {

		super.testGraphQLDeletePaymentByExternalReferenceCode();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetPayment() throws Exception {
		super.testGraphQLGetPayment();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetPaymentByExternalReferenceCode()
		throws Exception {

		super.testGraphQLGetPaymentByExternalReferenceCode();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetPaymentByExternalReferenceCodeNotFound()
		throws Exception {

		super.testGraphQLGetPaymentByExternalReferenceCodeNotFound();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetPaymentNotFound() throws Exception {
		super.testGraphQLGetPaymentNotFound();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetPaymentsPage() throws Exception {
		super.testGraphQLGetPaymentsPage();
	}

	@Override
	@Test
	public void testPatchPayment() throws Exception {
		super.testPatchPayment();

		_testPatchPaymentWithPaymentIntegrationKey();
	}

	@Override
	@Test
	public void testPostPayment() throws Exception {
		super.testPostPayment();

		_testPostPayment(
			_commerceChannel.getCommerceChannelId(),
			CommercePaymentIntegrationConstants.TYPE_INTERNAL_ONLINE_STANDARD,
			null);
		_testPostPaymentWithCommercePaymentIntegration();
		_testPostPaymentWithCommercePaymentMethod();
	}

	@Override
	@Test
	public void testPostPaymentByExternalReferenceCodeRefund()
		throws Exception {

		super.testPostPaymentByExternalReferenceCodeRefund();

		_testPostPaymentByExternalReferenceCodeRefundWithoutCommercePaymentIntegration();
	}

	@Override
	@Test
	public void testPostPaymentRefund() throws Exception {
		super.testPostPaymentRefund();

		_testPostPaymentRefundWithoutCommercePaymentIntegration();
	}

	@Override
	protected Payment randomPayment() {
		return new Payment() {
			{
				amount = BigDecimal.valueOf(RandomTestUtil.randomDouble());
				callbackURL = RandomTestUtil.randomString();
				cancelURL = RandomTestUtil.randomString();
				channelId = _commerceChannel.getCommerceChannelId();
				comment = RandomTestUtil.randomString();
				currencyCode = _commerceCurrency.getCode();
				currencyExternalReferenceCode =
					_commerceCurrency.getExternalReferenceCode();
				currencyId = _commerceCurrency.getCommerceCurrencyId();
				externalReferenceCode = RandomTestUtil.randomString();
				languageId = RandomTestUtil.randomString();
				paymentIntegrationKey = RandomTestUtil.randomString();
				paymentIntegrationType = RandomTestUtil.randomInt();
				reasonKey = StringPool.BLANK;
				relatedItemId = RandomTestUtil.randomLong();
				relatedItemName = CommerceOrder.class.getName();
				transactionCode = RandomTestUtil.randomString();
				type = CommercePaymentEntryConstants.TYPE_PAYMENT;
			}
		};
	}

	@Override
	protected Payment testDeletePayment_addPayment() throws Exception {
		return _addPayment(randomPayment());
	}

	@Override
	protected Payment testDeletePaymentByExternalReferenceCode_addPayment()
		throws Exception {

		return _addPayment(randomPayment());
	}

	@Override
	protected Payment testGetPayment_addPayment() throws Exception {
		return _addPayment(randomPayment());
	}

	@Override
	protected Payment testGetPaymentByExternalReferenceCode_addPayment()
		throws Exception {

		return _addPayment(randomPayment());
	}

	@Override
	protected Payment testGetPaymentsPage_addPayment(Payment payment)
		throws Exception {

		return _addPayment(payment);
	}

	@Override
	protected Payment testGraphQLPayment_addPayment() throws Exception {
		return _addPayment(randomPayment());
	}

	@Override
	protected Payment testPatchPayment_addPayment() throws Exception {
		return _addPayment(randomPayment());
	}

	@Override
	protected Payment testPatchPaymentByExternalReferenceCode_addPayment()
		throws Exception {

		return _addPayment(randomPayment());
	}

	@Override
	protected Payment testPostPayment_addPayment(Payment payment)
		throws Exception {

		return _addPayment(payment);
	}

	@Override
	protected Payment testPostPaymentByExternalReferenceCodeRefund_addPayment(
			Payment payment)
		throws Exception {

		return _addPayment(payment);
	}

	@Override
	protected Payment testPostPaymentRefund_addPayment(Payment payment)
		throws Exception {

		return _addPayment(payment);
	}

	@Override
	protected Payment testPutPaymentByExternalReferenceCode_addPayment()
		throws Exception {

		return _addPayment(randomPayment());
	}

	private Payment _addPayment(Payment payment) throws Exception {
		CommercePaymentEntry commercePaymentEntry = _getCommercePaymentEntry(
			payment);

		return new Payment() {
			{
				amount = commercePaymentEntry.getAmount();
				callbackURL = commercePaymentEntry.getCallbackURL();
				cancelURL = commercePaymentEntry.getCancelURL();
				channelId = commercePaymentEntry.getCommerceChannelId();
				comment = commercePaymentEntry.getNote();
				createDate = commercePaymentEntry.getCreateDate();
				currencyCode = commercePaymentEntry.getCurrencyCode();
				externalReferenceCode = payment.getExternalReferenceCode();
				id = commercePaymentEntry.getCommercePaymentEntryId();
				languageId = commercePaymentEntry.getLanguageId();
				paymentIntegrationKey =
					commercePaymentEntry.getPaymentIntegrationKey();
				paymentIntegrationType =
					commercePaymentEntry.getPaymentIntegrationType();
				reasonKey = commercePaymentEntry.getReasonKey();
				relatedItemId = commercePaymentEntry.getClassPK();
				relatedItemName = commercePaymentEntry.getClassName();
				transactionCode = commercePaymentEntry.getTransactionCode();
				type = commercePaymentEntry.getType();
			}
		};
	}

	private CommercePaymentEntry _addRefundCommercePaymentEntry()
		throws Exception {

		CommercePaymentEntry commercePaymentEntry = _getCommercePaymentEntry(
			randomPayment());

		commercePaymentEntry.setPaymentStatus(
			CommercePaymentEntryConstants.STATUS_COMPLETED);

		commercePaymentEntry =
			_commercePaymentEntryLocalService.updateCommercePaymentEntry(
				commercePaymentEntry);

		Payment payment = randomPayment();

		payment.setPaymentIntegrationKey(RandomTestUtil.randomString());
		payment.setRelatedItemId(
			commercePaymentEntry.getCommercePaymentEntryId());
		payment.setRelatedItemName(CommercePaymentEntry.class.getName());
		payment.setType(CommercePaymentEntryConstants.TYPE_REFUND);

		return _getCommercePaymentEntry(payment);
	}

	private void _assertBadRequest(HttpInvoker.HttpResponse httpResponse)
		throws Exception {

		Assert.assertEquals(400, httpResponse.getStatusCode());

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			httpResponse.getContent());

		Assert.assertEquals(
			"The payment integration key is invalid.",
			jsonObject.getString("title"));
	}

	private CommercePaymentEntry _getCommercePaymentEntry(Payment payment)
		throws Exception {

		CommercePaymentEntry commercePaymentEntry =
			_commercePaymentEntryLocalService.addCommercePaymentEntry(
				_serviceContext.getUserId(),
				_classNameLocalService.getClassNameId(
					payment.getRelatedItemName()),
				GetterUtil.getLong(payment.getRelatedItemId()),
				GetterUtil.getLong(payment.getChannelId()),
				(BigDecimal)GetterUtil.getNumber(payment.getAmount()),
				payment.getCallbackURL(), payment.getCancelURL(),
				payment.getCurrencyCode(), payment.getLanguageId(),
				payment.getPayload(), payment.getComment(),
				payment.getPaymentIntegrationKey(),
				GetterUtil.getInteger(payment.getPaymentIntegrationType()),
				payment.getReasonKey(), payment.getTransactionCode(),
				GetterUtil.getInteger(payment.getType()), _serviceContext);

		commercePaymentEntry.setExternalReferenceCode(
			payment.getExternalReferenceCode());
		commercePaymentEntry.setCreateDate(
			new Date(System.currentTimeMillis() - Time.HOUR));

		commercePaymentEntry =
			_commercePaymentEntryLocalService.updateCommercePaymentEntry(
				commercePaymentEntry);

		_commercePaymentEntries.add(commercePaymentEntry);

		return commercePaymentEntry;
	}

	private ServiceRegistration<CommercePaymentMethod>
		_registerCommercePaymentMethod(String key) {

		Bundle bundle = FrameworkUtil.getBundle(PaymentResourceTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		return bundleContext.registerService(
			CommercePaymentMethod.class,
			new CommercePaymentMethod() {

				@Override
				public String getDescription(Locale locale) {
					return null;
				}

				@Override
				public String getKey() {
					return key;
				}

				@Override
				public String getName(Locale locale) {
					return null;
				}

				@Override
				public int getPaymentType() {
					return CommercePaymentMethodConstants.TYPE_OFFLINE;
				}

				@Override
				public String getServletPath() {
					return null;
				}

			},
			null);
	}

	private void _testPatchPaymentWithPaymentIntegrationKey() throws Exception {
		Payment payment = randomPayment();

		payment.setPaymentIntegrationKey((String)null);

		payment = paymentResource.postPayment(payment);

		_commercePaymentEntries.add(
			_commercePaymentEntryLocalService.getCommercePaymentEntry(
				payment.getId()));

		Assert.assertEquals(
			Integer.valueOf(
				CommercePaymentIntegrationConstants.
					TYPE_INTERNAL_ONLINE_STANDARD),
			payment.getPaymentIntegrationType());

		String key = RandomTestUtil.randomString();

		ServiceRegistration<CommercePaymentMethod> serviceRegistration =
			_registerCommercePaymentMethod(key);

		try {
			payment = paymentResource.patchPayment(
				payment.getId(),
				new Payment() {
					{
						paymentIntegrationKey = key;
					}
				});

			Assert.assertEquals(
				Integer.valueOf(
					CommercePaymentIntegrationConstants.TYPE_INTERNAL_OFFLINE),
				payment.getPaymentIntegrationType());

			payment = paymentResource.patchPayment(
				payment.getId(),
				new Payment() {
					{
						comment = RandomTestUtil.randomString();
					}
				});

			Assert.assertEquals(
				Integer.valueOf(
					CommercePaymentIntegrationConstants.TYPE_INTERNAL_OFFLINE),
				payment.getPaymentIntegrationType());
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	private void _testPostPayment(
			Long channelId, int expectedPaymentIntegrationType,
			String paymentIntegrationKey)
		throws Exception {

		Payment payment = randomPayment();

		payment.setChannelId(channelId);
		payment.setPaymentIntegrationKey(paymentIntegrationKey);
		payment.setPaymentStatus(CommercePaymentEntryConstants.STATUS_PENDING);

		payment = paymentResource.postPayment(payment);

		_commercePaymentEntries.add(
			_commercePaymentEntryLocalService.getCommercePaymentEntry(
				payment.getId()));

		Assert.assertEquals(
			Integer.valueOf(expectedPaymentIntegrationType),
			payment.getPaymentIntegrationType());
	}

	private void _testPostPaymentByExternalReferenceCodeRefundWithoutCommercePaymentIntegration()
		throws Exception {

		CommercePaymentEntry commercePaymentEntry =
			_addRefundCommercePaymentEntry();

		_assertBadRequest(
			paymentResource.
				postPaymentByExternalReferenceCodeRefundHttpResponse(
					commercePaymentEntry.getExternalReferenceCode()));
	}

	private void _testPostPaymentRefundWithoutCommercePaymentIntegration()
		throws Exception {

		CommercePaymentEntry commercePaymentEntry =
			_addRefundCommercePaymentEntry();

		_assertBadRequest(
			paymentResource.postPaymentRefundHttpResponse(
				commercePaymentEntry.getCommercePaymentEntryId()));
	}

	private void _testPostPaymentWithCommercePaymentIntegration()
		throws Exception {

		Bundle bundle = FrameworkUtil.getBundle(PaymentResourceTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		String key = RandomTestUtil.randomString();

		ServiceRegistration<CommercePaymentIntegration> serviceRegistration =
			bundleContext.registerService(
				CommercePaymentIntegration.class,
				new CommercePaymentIntegration() {

					@Override
					public CommercePaymentEntry authorize(
						HttpServletRequest httpServletRequest,
						CommercePaymentEntry commercePaymentEntry) {

						return commercePaymentEntry;
					}

					@Override
					public CommercePaymentEntry cancel(
						HttpServletRequest httpServletRequest,
						CommercePaymentEntry commercePaymentEntry) {

						return commercePaymentEntry;
					}

					@Override
					public CommercePaymentEntry capture(
						HttpServletRequest httpServletRequest,
						CommercePaymentEntry commercePaymentEntry) {

						return commercePaymentEntry;
					}

					@Override
					public String getDescription(Locale locale) {
						return null;
					}

					@Override
					public String getKey() {
						return key;
					}

					@Override
					public String getName(Locale locale) {
						return null;
					}

					@Override
					public int getPaymentIntegrationType() {
						return CommercePaymentIntegrationConstants.
							TYPE_FUNCTION_OFFLINE;
					}

					@Override
					public CommercePaymentEntry refund(
						HttpServletRequest httpServletRequest,
						CommercePaymentEntry commercePaymentEntry) {

						return commercePaymentEntry;
					}

				},
				null);

		try {
			_testPostPayment(
				_commerceChannel.getCommerceChannelId(),
				CommercePaymentIntegrationConstants.TYPE_FUNCTION_OFFLINE, key);
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	private void _testPostPaymentWithCommercePaymentMethod() throws Exception {
		String key = RandomTestUtil.randomString();

		ServiceRegistration<CommercePaymentMethod> serviceRegistration =
			_registerCommercePaymentMethod(key);

		try {
			_testPostPayment(
				null, CommercePaymentIntegrationConstants.TYPE_INTERNAL_OFFLINE,
				key);
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	@Inject
	private ClassNameLocalService _classNameLocalService;

	private CommerceChannel _commerceChannel;

	@Inject
	private CommerceChannelLocalService _commerceChannelLocalService;

	private CommerceCurrency _commerceCurrency;

	@Inject
	private CommerceCurrencyLocalService _commerceCurrencyLocalService;

	@DeleteAfterTestRun
	private final List<CommercePaymentEntry> _commercePaymentEntries =
		new ArrayList<>();

	@Inject
	private CommercePaymentEntryLocalService _commercePaymentEntryLocalService;

	private ServiceContext _serviceContext;

}