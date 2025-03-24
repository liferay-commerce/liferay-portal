/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.checkout.web.internal.display.context;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.constants.AccountListTypeConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.commerce.constants.CommerceCheckoutWebKeys;
import com.liferay.commerce.constants.CommerceOrderActionKeys;
import com.liferay.commerce.constants.CommerceWebKeys;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.exception.CommerceOrderBillingAddressException;
import com.liferay.commerce.exception.CommerceOrderShippingAddressException;
import com.liferay.commerce.exception.CommerceOrderShippingAndBillingException;
import com.liferay.commerce.model.CommerceAddress;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.AddressService;
import com.liferay.portal.kernel.service.CountryLocalService;
import com.liferay.portal.kernel.service.ListTypeLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.Objects;

import javax.portlet.ActionRequest;

/**
 * @author Luca Pellizzon
 */
public class AddressCommerceCheckoutStepDisplayContext {

	public AddressCommerceCheckoutStepDisplayContext(
		AccountEntryLocalService accountEntryLocalService,
		AddressService addressService, String commerceAddressType,
		CommerceOrderService commerceOrderService,
		CountryLocalService countryLocalService,
		ModelResourcePermission<CommerceOrder>
			commerceOrderModelResourcePermission,
		ListTypeLocalService listTypeLocalService) {

		_accountEntryLocalService = accountEntryLocalService;
		_addressService = addressService;
		_commerceAddressType = commerceAddressType;
		_commerceOrderService = commerceOrderService;
		_countryLocalService = countryLocalService;
		_commerceOrderModelResourcePermission =
			commerceOrderModelResourcePermission;
		_listTypeLocalService = listTypeLocalService;
	}

	public CommerceOrder updateCommerceOrderAddress(
			ActionRequest actionRequest, String paramName)
		throws Exception {

		CommerceContext commerceContext =
			(CommerceContext)actionRequest.getAttribute(
				CommerceWebKeys.COMMERCE_CONTEXT);

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		CommerceOrder commerceOrder = _getCommerceOrder(
			actionRequest, commerceContext.getCommerceChannelGroupId());

		boolean newAddress = ParamUtil.getBoolean(actionRequest, "newAddress");

		long commerceAddressId = ParamUtil.getLong(actionRequest, paramName);

		if (newAddress) {
			Address address = _addAddress(commerceOrder, actionRequest);

			commerceAddressId = address.getAddressId();
		}

		_commerceOrderModelResourcePermission.check(
			themeDisplay.getPermissionChecker(), commerceOrder,
			CommerceOrderActionKeys.CHECKOUT_COMMERCE_ORDER);

		boolean useAsBilling = ParamUtil.getBoolean(
			actionRequest, "use-as-billing");

		Address address = _addressService.getAddress(commerceAddressId);

		if (useAsBilling) {
			Country country = address.getCountry();

			if (!country.isBillingAllowed()) {
				throw new CommerceOrderShippingAndBillingException();
			}

			_commerceAddressType =
				AccountListTypeConstants.
					ACCOUNT_ENTRY_ADDRESS_TYPE_BILLING_AND_SHIPPING;
		}

		if (Objects.equals(
				CommerceCheckoutWebKeys.SHIPPING_ADDRESS_PARAM_NAME,
				paramName)) {

			if (commerceAddressId < 1) {
				throw new CommerceOrderShippingAddressException();
			}

			if (useAsBilling) {
				_addressService.updateAddress(
					address.getExternalReferenceCode(), commerceAddressId,
					address.getCountryId(),
					_getListTypeId(_commerceAddressType), address.getRegionId(),
					address.getCity(), address.getDescription(),
					address.isMailing(), address.getName(), address.isPrimary(),
					address.getStreet1(), address.getStreet2(),
					address.getStreet3(), address.getSubtype(),
					address.getZip(), address.getPhoneNumber());

				commerceOrder.setBillingAddressId(commerceAddressId);

				commerceOrder = updateCommerceOrderAddress(
					commerceOrder, commerceAddressId, commerceAddressId,
					commerceContext);
			}
			else {
				if (Objects.equals(
						commerceOrder.getShippingAddressId(),
						commerceOrder.getBillingAddressId())) {

					commerceOrder = updateCommerceOrderAddress(
						commerceOrder, 0, commerceAddressId, commerceContext);
				}
				else {
					commerceOrder = updateCommerceOrderAddress(
						commerceOrder, commerceOrder.getBillingAddressId(),
						commerceAddressId, commerceContext);
				}
			}

			actionRequest.setAttribute(
				CommerceCheckoutWebKeys.COMMERCE_ORDER, commerceOrder);

			return commerceOrder;
		}

		if (Objects.equals(
				CommerceCheckoutWebKeys.BILLING_ADDRESS_PARAM_NAME,
				paramName)) {

			if (commerceAddressId < 1) {
				throw new CommerceOrderBillingAddressException();
			}

			return updateCommerceOrderAddress(
				commerceOrder, commerceAddressId,
				commerceOrder.getShippingAddressId(), commerceContext);
		}

		return commerceOrder;
	}

	protected CommerceOrder updateCommerceOrderAddress(
			CommerceOrder commerceOrder, long billingAddressId,
			long shippingAddressId, CommerceContext commerceContext)
		throws Exception {

		return _commerceOrderService.updateCommerceOrder(
			commerceOrder.getExternalReferenceCode(),
			commerceOrder.getCommerceOrderId(), billingAddressId,
			commerceOrder.getCommerceShippingMethodId(), shippingAddressId,
			commerceOrder.getAdvanceStatus(),
			commerceOrder.getCommercePaymentMethodKey(), null,
			commerceOrder.getPurchaseOrderNumber(),
			commerceOrder.getShippingAmount(),
			commerceOrder.getShippingOptionName(), commerceOrder.getSubtotal(),
			commerceOrder.getTotal());
	}

	private Address _addAddress(
			CommerceOrder commerceOrder, ActionRequest actionRequest)
		throws Exception {

		long countryId = ParamUtil.getLong(actionRequest, "countryId");

		boolean useAsBilling = ParamUtil.getBoolean(
			actionRequest, "use-as-billing");

		if (useAsBilling) {
			Country country = _countryLocalService.getCountry(countryId);

			if (!country.isBillingAllowed()) {
				throw new CommerceOrderShippingAndBillingException();
			}

			_commerceAddressType =
				AccountListTypeConstants.
					ACCOUNT_ENTRY_ADDRESS_TYPE_BILLING_AND_SHIPPING;
		}

		String name = ParamUtil.getString(actionRequest, "name");

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			CommerceAddress.class.getName(), actionRequest);

		serviceContext.setScopeGroupId(commerceOrder.getGroupId());

		if (commerceOrder.isGuestOrder()) {
			String email = ParamUtil.getString(actionRequest, "email");

			AccountEntry accountEntry =
				_accountEntryLocalService.addAccountEntry(
					serviceContext.getUserId(),
					AccountConstants.PARENT_ACCOUNT_ENTRY_ID_DEFAULT, name,
					null, null, email, null, null,
					AccountConstants.ACCOUNT_ENTRY_TYPE_GUEST,
					WorkflowConstants.STATUS_APPROVED, serviceContext);

			commerceOrder.setCommerceAccountId(
				accountEntry.getAccountEntryId());

			commerceOrder = _commerceOrderService.updateCommerceOrder(
				commerceOrder);
		}

		return _addressService.addAddress(
			StringPool.BLANK, AccountEntry.class.getName(),
			commerceOrder.getCommerceAccountId(), countryId,
			_getListTypeId(_commerceAddressType),
			ParamUtil.getLong(actionRequest, "regionId"),
			ParamUtil.getString(actionRequest, "city"),
			ParamUtil.getString(actionRequest, "description"), false, name,
			false, ParamUtil.getString(actionRequest, "street1"),
			ParamUtil.getString(actionRequest, "street2"),
			ParamUtil.getString(actionRequest, "street3"), StringPool.BLANK,
			ParamUtil.getString(actionRequest, "zip"),
			ParamUtil.getString(actionRequest, "phoneNumber"), serviceContext);
	}

	private CommerceOrder _getCommerceOrder(
			ActionRequest actionRequest, long groupId)
		throws Exception {

		CommerceOrder commerceOrder = (CommerceOrder)actionRequest.getAttribute(
			CommerceCheckoutWebKeys.COMMERCE_ORDER);

		if (commerceOrder != null) {
			return commerceOrder;
		}

		commerceOrder = _commerceOrderService.getCommerceOrderByUuidAndGroupId(
			ParamUtil.getString(actionRequest, "commerceOrderUuid"), groupId);

		actionRequest.setAttribute(
			CommerceCheckoutWebKeys.COMMERCE_ORDER, commerceOrder);

		return commerceOrder;
	}

	private long _getListTypeId(String name) {
		ListType listType = _listTypeLocalService.getListType(
			CompanyThreadLocal.getCompanyId(), name,
			AccountListTypeConstants.ACCOUNT_ENTRY_ADDRESS);

		return listType.getListTypeId();
	}

	private final AccountEntryLocalService _accountEntryLocalService;
	private final AddressService _addressService;
	private String _commerceAddressType;
	private final ModelResourcePermission<CommerceOrder>
		_commerceOrderModelResourcePermission;
	private final CommerceOrderService _commerceOrderService;
	private final CountryLocalService _countryLocalService;
	private final ListTypeLocalService _listTypeLocalService;

}