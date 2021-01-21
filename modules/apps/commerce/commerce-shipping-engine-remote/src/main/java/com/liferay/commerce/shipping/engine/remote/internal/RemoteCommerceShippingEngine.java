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
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.math.BigDecimal;

import java.net.URI;

import java.text.Format;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
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

		try {
			Http.Options options = _getHttpOptions(
				commerceContext, commerceOrder, locale);

			String json = _http.URLtoString(options);

			if (_log.isDebugEnabled()) {
				Http.Response response = options.getResponse();

				_log.debug("Response code " + response.getResponseCode());
			}

			return _getCommerceShippingOptions(json);
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

	private Map<String, String> _getCommerceAddressParameters(
		CommerceAddress commerceAddress, String prefix) {

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
		throws Exception {

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
			"orderDate",
			() -> {
				if (commerceOrder.getOrderDate() == null) {
					return null;
				}

				return dateFormat.format(commerceOrder.getOrderDate());
			}
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
		).put(
			"orderStatus", String.valueOf(commerceOrder.getOrderStatus())
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
			String json)
		throws Exception {

		List<CommerceShippingOption> commerceShippingOptions =
			new ArrayList<>();

		JSONArray jsonArray = _jsonFactory.createJSONArray(json);

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			CommerceShippingOption commerceShippingOption =
				new CommerceShippingOption(
					jsonObject.getString("name"), jsonObject.getString("label"),
					BigDecimal.valueOf(jsonObject.getDouble("amount")));

			commerceShippingOptions.add(commerceShippingOption);
		}

		return commerceShippingOptions;
	}

	private Http.Options _getHttpOptions(
			CommerceContext commerceContext, CommerceOrder commerceOrder,
			Locale locale)
		throws Exception {

		Http.Options options = new Http.Options();

		RemoteCommerceShippingEngineConfiguration
			commerceShippingEngineConfiguration =
				_configurationProvider.getConfiguration(
					RemoteCommerceShippingEngineConfiguration.class,
					new GroupServiceSettingsLocator(
						commerceContext.getCommerceChannelGroupId(),
						RemoteCommerceShippingEngineConfiguration.class.
							getName()));

		if (Validator.isNotNull(
				commerceShippingEngineConfiguration.
					shippingOptionsEndpointAuthorizationToken())) {

			String shippingOptionsEndpointAuthorizationToken =
				commerceShippingEngineConfiguration.
					shippingOptionsEndpointAuthorizationToken();

			options.addHeader(
				"Authorization",
				"token " + shippingOptionsEndpointAuthorizationToken);
		}

		URI uri = URIBuilder.create(
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

				return String.valueOf(commerceAccount.getCommerceAccountId());
			}
		).addParameter(
			"channelCurrencyCode",
			() -> {
				CommerceChannel commerceChannel =
					_commerceChannelService.getCommerceChannel(
						commerceContext.getCommerceChannelId());

				return commerceChannel.getCommerceCurrencyCode();
			}
		).addParameter(
			"channelExternalReferenceCode",
			() -> {
				CommerceChannel commerceChannel =
					_commerceChannelService.getCommerceChannel(
						commerceContext.getCommerceChannelId());

				return commerceChannel.getExternalReferenceCode();
			}
		).addParameter(
			"channelId",
			() -> {
				CommerceChannel commerceChannel =
					_commerceChannelService.getCommerceChannel(
						commerceContext.getCommerceChannelId());

				return String.valueOf(commerceChannel.getCommerceChannelId());
			}
		).addParameter(
			"channelType",
			() -> {
				CommerceChannel commerceChannel =
					_commerceChannelService.getCommerceChannel(
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
			"siteType", String.valueOf(commerceContext.getCommerceSiteType())
		).addParameters(
			_getCommerceOrderParameters(commerceOrder)
		).addParameter(
			"locale", locale.toString()
		).build();

		options.setLocation(uri.toString());

		return options;
	}

	private ResourceBundle _getResourceBundle(Locale locale) {
		return ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RemoteCommerceShippingEngine.class);

	@Reference
	private CommerceChannelService _commerceChannelService;

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private Http _http;

	@Reference
	private JSONFactory _jsonFactory;

}