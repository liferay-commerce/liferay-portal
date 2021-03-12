/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.shipping.engine.remote.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.commerce.account.model.CommerceAccount;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.exception.CommerceShippingEngineException;
import com.liferay.commerce.model.CommerceAddress;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceShippingEngine;
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.model.CommerceShippingOption;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelService;
import com.liferay.commerce.shipping.engine.remote.internal.configuration.RemoteCommerceShippingEngineConfiguration;
import com.liferay.petra.apache.http.components.URIBuilder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;

import java.math.BigDecimal;

import java.net.URISyntaxException;

import java.nio.charset.StandardCharsets;

import java.text.Format;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Ivica Cardic
 */
@Component(
	enabled = false, immediate = true,
	property = "commerce.shipping.engine.key=" + RemoteCommerceShippingEngine.KEY,
	service = CommerceShippingEngine.class
)
public class RemoteCommerceShippingEngine implements CommerceShippingEngine {

	public static final String KEY = "remote";

	@Override
	public String getCommerceShippingOptionLabel(String name, Locale locale) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<CommerceShippingOption> getCommerceShippingOptions(
			CommerceContext commerceContext, CommerceOrder commerceOrder,
			Locale locale)
		throws CommerceShippingEngineException {

		try (CloseableHttpResponse closeableHttpResponse =
				_closeableHttpClient.execute(
					_getHttpGet(commerceContext, commerceOrder, locale))) {

			if (_log.isTraceEnabled()) {
				StatusLine statusLine = closeableHttpResponse.getStatusLine();

				int statusCode = statusLine.getStatusCode();

				_log.trace("Server returned status " + statusCode);
			}

			return _getCommerceShippingOptions(
				EntityUtils.toString(
					closeableHttpResponse.getEntity(), StandardCharsets.UTF_8));
		}
		catch (Exception exception) {
			throw new CommerceShippingEngineException(exception);
		}
	}

	@Override
	public String getDescription(Locale locale) {
		return LanguageUtil.get(
			_getResourceBundle(locale), "remote-description");
	}

	@Override
	public String getName(Locale locale) {
		return LanguageUtil.get(_getResourceBundle(locale), "remote");
	}

	@Activate
	protected void activate() {
		HttpClientBuilder httpClientBuilder = HttpClients.custom();

		_poolingHttpClientConnectionManager =
			new PoolingHttpClientConnectionManager();

		httpClientBuilder.setConnectionManager(
			_poolingHttpClientConnectionManager);

		_poolingHttpClientConnectionManager.setMaxTotal(20);
		_poolingHttpClientConnectionManager.setValidateAfterInactivity(30000);

		httpClientBuilder.useSystemProperties();

		_closeableHttpClient = httpClientBuilder.build();
	}

	@Deactivate
	protected void deactivate() {
		if (_closeableHttpClient != null) {
			try {
				_closeableHttpClient.close();
			}
			catch (IOException ioException) {
				_log.error("Unable to close client", ioException);
			}

			_closeableHttpClient = null;
		}

		if (_poolingHttpClientConnectionManager != null) {
			_poolingHttpClientConnectionManager.close();

			_poolingHttpClientConnectionManager = null;
		}
	}

	protected CommerceChannel getCommerceChannel(long commerceChannelId)
		throws PortalException {

		return _commerceChannelService.getCommerceChannel(commerceChannelId);
	}

	protected RemoteCommerceShippingEngineConfiguration
			getRemoteCommerceShippingEngineConfiguration(long channelGroupId)
		throws CommerceShippingEngineException {

		try {
			return _configurationProvider.getConfiguration(
				RemoteCommerceShippingEngineConfiguration.class,
				new GroupServiceSettingsLocator(
					channelGroupId,
					RemoteCommerceShippingEngineConfiguration.class.getName()));
		}
		catch (ConfigurationException configurationException) {
			throw new CommerceShippingEngineException(configurationException);
		}
	}

	private Map<String, String> _getCommerceAddressParameters(
			CommerceAddress commerceAddress, String prefix)
		throws PortalException {

		return HashMapBuilder.put(
			prefix + "AddressCity", commerceAddress.getCity()
		).put(
			prefix + "AddressCountryISOCode",
			() -> {
				Country country = commerceAddress.getCountry();

				return country.getA3();
			}
		).put(
			prefix + "AddressExternalReferenceCode",
			commerceAddress.getExternalReferenceCode()
		).put(
			prefix + "AddressId",
			String.valueOf(commerceAddress.getCommerceAddressId())
		).put(
			prefix + "AddressLatitude",
			String.valueOf(commerceAddress.getLatitude())
		).put(
			prefix + "AddressLongitude",
			String.valueOf(commerceAddress.getLongitude())
		).put(
			prefix + "AddressPhoneNumber", commerceAddress.getPhoneNumber()
		).put(
			prefix + "AddressRegionISOCode",
			() -> {
				Region region = commerceAddress.getRegion();

				if (region == null) {
					return null;
				}

				return region.getRegionCode();
			}
		).put(
			prefix + "AddressStreet1", commerceAddress.getStreet1()
		).put(
			prefix + "AddressStreet2", commerceAddress.getStreet2()
		).put(
			prefix + "AddressStreet3", commerceAddress.getStreet3()
		).put(
			prefix + "AddressZip", commerceAddress.getZip()
		).build();
	}

	private Map<String, String> _getCommerceOrderParameters(
			CommerceOrder commerceOrder)
		throws PortalException {

		Format dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		return HashMapBuilder.put(
			"orderAccountExternalReferenceCode",
			() -> {
				CommerceAccount commerceAccount =
					commerceOrder.getCommerceAccount();

				return commerceAccount.getExternalReferenceCode();
			}
		).put(
			"orderAccountId",
			() -> {
				CommerceAccount commerceAccount =
					commerceOrder.getCommerceAccount();

				return String.valueOf(commerceAccount.getCommerceAccountId());
			}
		).put(
			"orderAdvanceStatus", commerceOrder.getAdvanceStatus()
		).putAll(
			_getCommerceAddressParameters(
				commerceOrder.getBillingAddress(), "orderBilling")
		).put(
			"orderCouponCode", commerceOrder.getCouponCode()
		).put(
			"orderExternalReferenceCode",
			commerceOrder.getExternalReferenceCode()
		).put(
			"orderId", String.valueOf(commerceOrder.getCommerceOrderId())
		).put(
			"orderLastPriceUpdateDate",
			() -> {
				if (commerceOrder.getLastPriceUpdateDate() == null) {
					return null;
				}

				return dateFormat.format(
					commerceOrder.getLastPriceUpdateDate());
			}
		).put(
			"orderDate",
			() -> {
				if (commerceOrder.getOrderDate() == null) {
					return null;
				}

				return dateFormat.format(commerceOrder.getOrderDate());
			}
		).put(
			"orderStatus", String.valueOf(commerceOrder.getOrderStatus())
		).put(
			"orderPaymentMethod", commerceOrder.getCommercePaymentMethodKey()
		).put(
			"orderPaymentStatus",
			String.valueOf(commerceOrder.getPaymentStatus())
		).put(
			"orderPurchaseOrderNumber", commerceOrder.getPurchaseOrderNumber()
		).put(
			"orderRequestedDeliveryDate",
			() -> {
				if (commerceOrder.getRequestedDeliveryDate() == null) {
					return null;
				}

				return dateFormat.format(
					commerceOrder.getRequestedDeliveryDate());
			}
		).putAll(
			_getCommerceAddressParameters(
				commerceOrder.getShippingAddress(), "orderShipping")
		).put(
			"orderShippingAmount",
			String.valueOf(commerceOrder.getShippingAmount())
		).put(
			"orderShippingDiscountAmount",
			String.valueOf(commerceOrder.getShippingDiscountAmount())
		).put(
			"orderShippingDiscountPercentageLevel1",
			String.valueOf(commerceOrder.getShippingDiscountPercentageLevel1())
		).put(
			"orderShippingDiscountPercentageLevel2",
			String.valueOf(commerceOrder.getShippingDiscountPercentageLevel2())
		).put(
			"orderShippingDiscountPercentageLevel3",
			String.valueOf(commerceOrder.getShippingDiscountPercentageLevel3())
		).put(
			"orderShippingDiscountPercentageLevel4",
			String.valueOf(commerceOrder.getShippingDiscountPercentageLevel4())
		).put(
			"orderShippingMethod",
			() -> {
				CommerceShippingMethod commerceShippingMethod =
					commerceOrder.getCommerceShippingMethod();

				return commerceShippingMethod.getEngineKey();
			}
		).put(
			"orderSubtotal", String.valueOf(commerceOrder.getSubtotal())
		).put(
			"orderSubtotalDiscountAmount",
			String.valueOf(commerceOrder.getSubtotalDiscountAmount())
		).put(
			"orderSubtotalDiscountPercentageLevel1",
			String.valueOf(commerceOrder.getSubtotalDiscountPercentageLevel1())
		).put(
			"orderSubtotalDiscountPercentageLevel2",
			String.valueOf(commerceOrder.getSubtotalDiscountPercentageLevel2())
		).put(
			"orderSubtotalDiscountPercentageLevel3",
			String.valueOf(commerceOrder.getSubtotalDiscountPercentageLevel3())
		).put(
			"orderSubtotalDiscountPercentageLevel4",
			String.valueOf(commerceOrder.getSubtotalDiscountPercentageLevel4())
		).put(
			"orderTaxAmount", String.valueOf(commerceOrder.getTaxAmount())
		).put(
			"orderTotal", String.valueOf(commerceOrder.getTotal())
		).put(
			"orderTotalDiscountAmount",
			String.valueOf(commerceOrder.getTotalDiscountAmount())
		).put(
			"orderTotalDiscountPercentageLevel1",
			String.valueOf(commerceOrder.getTotalDiscountPercentageLevel1())
		).put(
			"orderTotalDiscountPercentageLevel2",
			String.valueOf(commerceOrder.getTotalDiscountPercentageLevel2())
		).put(
			"orderTotalDiscountPercentageLevel3",
			String.valueOf(commerceOrder.getTotalDiscountPercentageLevel3())
		).put(
			"orderTotalDiscountPercentageLevel4",
			String.valueOf(commerceOrder.getTotalDiscountPercentageLevel4())
		).put(
			"orderTransactionId", commerceOrder.getTransactionId()
		).build();
	}

	private List<CommerceShippingOption> _getCommerceShippingOptions(
			String result)
		throws IOException {

		List<CommerceShippingOption> commerceShippingOptions =
			new ArrayList<>();

		JsonNode rootJsonNode = _objectMapper.readTree(result);

		for (JsonNode jsonNode : rootJsonNode) {
			JsonNode nameJsonNode = jsonNode.get("name");
			JsonNode labelJsonNode = jsonNode.get("label");
			JsonNode amountJsonNode = jsonNode.get("amount");

			CommerceShippingOption commerceShippingOption =
				new CommerceShippingOption(
					nameJsonNode.textValue(), labelJsonNode.textValue(),
					BigDecimal.valueOf(amountJsonNode.doubleValue()));

			commerceShippingOptions.add(commerceShippingOption);
		}

		return commerceShippingOptions;
	}

	private HttpGet _getHttpGet(
			CommerceContext commerceContext, CommerceOrder commerceOrder,
			Locale locale)
		throws PortalException, URISyntaxException {

		RemoteCommerceShippingEngineConfiguration
			commerceShippingEngineConfiguration =
				getRemoteCommerceShippingEngineConfiguration(
					commerceContext.getCommerceChannelGroupId());

		HttpGet httpGet = new HttpGet(
			URIBuilder.create(
				commerceShippingEngineConfiguration.shippingOptionsEndpointURL()
			).addParameter(
				"accountExternalReferenceCode",
				() -> {
					CommerceAccount commerceAccount =
						commerceContext.getCommerceAccount();

					return commerceAccount.getExternalReferenceCode();
				}
			).addParameter(
				"accountId",
				() -> {
					CommerceAccount commerceAccount =
						commerceContext.getCommerceAccount();

					return String.valueOf(
						commerceAccount.getCommerceAccountId());
				}
			).addParameter(
				"channelCurrencyCode",
				() -> {
					CommerceChannel commerceChannel = getCommerceChannel(
						commerceContext.getCommerceChannelId());

					return commerceChannel.getCommerceCurrencyCode();
				}
			).addParameter(
				"channelExternalReferenceCode",
				() -> {
					CommerceChannel commerceChannel = getCommerceChannel(
						commerceContext.getCommerceChannelId());

					return commerceChannel.getExternalReferenceCode();
				}
			).addParameter(
				"channelId",
				() -> {
					CommerceChannel commerceChannel = getCommerceChannel(
						commerceContext.getCommerceChannelId());

					return String.valueOf(
						commerceChannel.getCommerceChannelId());
				}
			).addParameter(
				"channelType",
				() -> {
					CommerceChannel commerceChannel = getCommerceChannel(
						commerceContext.getCommerceChannelId());

					return String.valueOf(commerceChannel.getType());
				}
			).addParameter(
				"currencyCode",
				() -> {
					CommerceCurrency commerceCurrency =
						commerceContext.getCommerceCurrency();

					return commerceCurrency.getCode();
				}
			).addParameter(
				"siteType",
				String.valueOf(commerceContext.getCommerceSiteType())
			).addParameters(
				_getCommerceOrderParameters(commerceOrder)
			).addParameter(
				"locale", locale.toString()
			).build());

		if (Validator.isNotNull(
				commerceShippingEngineConfiguration.
					shippingOptionsEndpointAuthorizationToken())) {

			String shippingOptionsEndpointAuthorizationToken =
				commerceShippingEngineConfiguration.
					shippingOptionsEndpointAuthorizationToken();

			httpGet.addHeader(
				"Authorization",
				"token " + shippingOptionsEndpointAuthorizationToken);
		}

		return httpGet;
	}

	private ResourceBundle _getResourceBundle(Locale locale) {
		return ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RemoteCommerceShippingEngine.class);

	private CloseableHttpClient _closeableHttpClient;

	@Reference
	private CommerceChannelService _commerceChannelService;

	@Reference
	private ConfigurationProvider _configurationProvider;

	private final ObjectMapper _objectMapper = new ObjectMapper();
	private PoolingHttpClientConnectionManager
		_poolingHttpClientConnectionManager;

}