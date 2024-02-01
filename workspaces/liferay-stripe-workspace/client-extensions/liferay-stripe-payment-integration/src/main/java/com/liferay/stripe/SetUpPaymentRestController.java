/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.stripe;

import com.liferay.petra.string.StringBundler;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Crescenzo Rega
 */
@RequestMapping("/set-up-payment")
@RestController
public class SetUpPaymentRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
		@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {

		log(jwt, _log, json);

		String errorMessages = null;
		String paymentStatus = "4";
		String redirectURL = null;
		String transactionCode = null;

		try {
			JSONObject jsonObject = new JSONObject(json);

			Stripe.apiKey = apiKey;

			JSONObject commercePaymentEntryJSONObject =
				jsonObject.getJSONObject("commercePaymentEntry");

			Session session = Session.create(
				_getSessionCreateParams(
					commercePaymentEntryJSONObject,
					_getOrderJSONObject(
						commercePaymentEntryJSONObject, jwt.getTokenValue())));

			if (Objects.equals(session.getStatus(), "open")) {
				redirectURL = session.getUrl();
				paymentStatus = "18";
				transactionCode = session.getId();
			}
		}
		catch (Exception exception) {
			errorMessages = ExceptionUtils.getStackTrace(exception);

			_log.error(errorMessages);
		}

		return new ResponseEntity<>(
			new JSONObject(
			).put(
				"errorMessages", errorMessages
			).put(
				"redirectURL", redirectURL
			).put(
				"paymentStatus", paymentStatus
			).put(
				"transactionCode", transactionCode
			).toString(),
			HttpStatus.OK);
	}

	private List<SessionCreateParams.LineItem> _getLineItems(
		String currencyCode, String languageId, JSONArray orderItemsJSONArray) {

		List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

		for (int i = 0; i < orderItemsJSONArray.length(); i++) {
			JSONObject orderItemJSONObject = orderItemsJSONArray.getJSONObject(
				i);

			SessionCreateParams.LineItem lineItem =
				SessionCreateParams.LineItem.builder(
				).setQuantity(
					orderItemJSONObject.getLong("quantity")
				).setPriceData(
					SessionCreateParams.LineItem.PriceData.builder(
					).setCurrency(
						currencyCode
					).setUnitAmount(
						BigDecimal.valueOf(
							orderItemJSONObject.getLong("unitPrice")
						).multiply(
							BigDecimal.valueOf(100)
						).longValue()
					).setProductData(
						SessionCreateParams.LineItem.PriceData.ProductData.
							builder(
							).setName(
								orderItemJSONObject.getJSONObject(
									"name"
								).getString(
									languageId
								)
							).build()
					).build()
				).build();

			lineItems.add(lineItem);
		}

		return lineItems;
	}

	private JSONObject _getOrderJSONObject(
		JSONObject commercePaymentEntryJSONObject, String tokenValue) {

		return new JSONObject(
			Objects.requireNonNull(
				WebClient.create(
				).get(
				).uri(
					StringBundler.concat(
						lxcDXPServerProtocol, "://", lxcDXPMainDomain,
						"/o/headless-commerce-admin-order/v1.0/orders/",
						commercePaymentEntryJSONObject.getLong("classPK"),
						"?nestedFields=orderItems,shippingAddress,account")
				).accept(
					MediaType.APPLICATION_JSON
				).header(
					HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue
				).retrieve(
				).bodyToMono(
					String.class
				).block()));
	}

	private SessionCreateParams.PaymentIntentData _getPaymentIntentData(
		JSONObject accountJSONObject, JSONObject shippingAddressJSONObject) {

		return SessionCreateParams.PaymentIntentData.builder(
		).setShipping(
			SessionCreateParams.PaymentIntentData.Shipping.builder(
			).setAddress(
				SessionCreateParams.PaymentIntentData.Shipping.Address.builder(
				).setCity(
					shippingAddressJSONObject.getString("city")
				).setCountry(
					shippingAddressJSONObject.getString("countryISOCode")
				).setLine1(
					shippingAddressJSONObject.getString("street1")
				).setLine2(
					shippingAddressJSONObject.getString("street2")
				).setPostalCode(
					shippingAddressJSONObject.getString("zip")
				).setState(
					shippingAddressJSONObject.getString("regionISOCode")
				).build()
			).setName(
				accountJSONObject.getString("name")
			).build()
		).build();
	}

	private SessionCreateParams _getSessionCreateParams(
		JSONObject commercePaymentEntryJSONObject, JSONObject orderJSONObject) {

		return SessionCreateParams.builder(
		).setPaymentIntentData(
			_getPaymentIntentData(
				orderJSONObject.getJSONObject("account"),
				orderJSONObject.getJSONObject("shippingAddress"))
		).addPaymentMethodType(
			SessionCreateParams.PaymentMethodType.CARD
		).setCurrency(
			orderJSONObject.getString("currencyCode")
		).setMode(
			SessionCreateParams.Mode.PAYMENT
		).setSuccessUrl(
			commercePaymentEntryJSONObject.getString("callbackURL")
		).setCancelUrl(
			commercePaymentEntryJSONObject.getString("cancelURL")
		).addAllLineItem(
			_getLineItems(
				orderJSONObject.getString("currencyCode"),
				commercePaymentEntryJSONObject.getString("languageId"),
				orderJSONObject.getJSONArray("orderItems"))
		).addShippingOption(
			_getShippingOption(
				orderJSONObject.getString("currencyCode"),
				orderJSONObject.getLong("shippingAmountValue"),
				orderJSONObject.getString("shippingOption"))
		).build();
	}

	private SessionCreateParams.ShippingOption _getShippingOption(
		String currencyCode, Long shippingAmountValue, String shippingOption) {

		return SessionCreateParams.ShippingOption.builder(
		).setShippingRateData(
			SessionCreateParams.ShippingOption.ShippingRateData.builder(
			).setDisplayName(
				shippingOption
			).setFixedAmount(
				SessionCreateParams.ShippingOption.ShippingRateData.FixedAmount.
					builder(
					).setCurrency(
						currencyCode
					).setAmount(
						BigDecimal.valueOf(
							shippingAmountValue
						).multiply(
							BigDecimal.valueOf(100)
						).longValue()
					).build()
			).setType(
				SessionCreateParams.ShippingOption.ShippingRateData.Type.
					FIXED_AMOUNT
			).build()
		).build();
	}

	private static final Log _log = LogFactory.getLog(
		SetUpPaymentRestController.class);

}