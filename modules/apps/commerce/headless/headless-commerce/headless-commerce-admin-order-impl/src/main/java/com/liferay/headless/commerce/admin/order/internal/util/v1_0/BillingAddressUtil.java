/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.order.internal.util.v1_0;

import com.liferay.account.constants.AccountListTypeConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.headless.commerce.admin.order.dto.v1_0.BillingAddress;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.AddressService;
import com.liferay.portal.kernel.service.CountryLocalServiceUtil;
import com.liferay.portal.kernel.service.CountryService;
import com.liferay.portal.kernel.service.ListTypeLocalService;
import com.liferay.portal.kernel.service.RegionLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Alessio Antonio Rendina
 */
public class BillingAddressUtil {

	public static CommerceOrder addOrUpdateBillingAddress(
			AddressService addressService, BillingAddress billingAddress,
			CommerceOrder commerceOrder,
			CommerceOrderService commerceOrderService,
			CountryService countryService,
			ListTypeLocalService listTypeLocalService,
			ServiceContext serviceContext)
		throws Exception {

		if (commerceOrder.getBillingAddressId() > 0) {
			return _updateCommerceOrderBillingAddress(
				addressService, billingAddress, commerceOrder,
				commerceOrderService, countryService, serviceContext);
		}

		Address address = _addAddress(
			addressService, commerceOrder, billingAddress, listTypeLocalService,
			serviceContext);

		return commerceOrderService.updateBillingAddress(
			commerceOrder.getCommerceOrderId(), address.getName(),
			address.getDescription(), address.getStreet1(),
			address.getStreet2(), address.getStreet3(), address.getCity(),
			address.getZip(), address.getRegionId(), address.getCountryId(),
			address.getPhoneNumber(), serviceContext);
	}

	private static Address _addAddress(
			AddressService addressService, CommerceOrder commerceOrder,
			BillingAddress billingAddress,
			ListTypeLocalService listTypeLocalService,
			ServiceContext serviceContext)
		throws Exception {

		Country country = CountryLocalServiceUtil.getCountryByA2(
			commerceOrder.getCompanyId(), billingAddress.getCountryISOCode());
		ListType listType = listTypeLocalService.getListType(
			CompanyThreadLocal.getCompanyId(),
			AccountListTypeConstants.
				ACCOUNT_ENTRY_ADDRESS_TYPE_BILLING_AND_SHIPPING,
			AccountListTypeConstants.ACCOUNT_ENTRY_ADDRESS);

		return addressService.addAddress(
			StringPool.BLANK, commerceOrder.getModelClassName(),
			commerceOrder.getCommerceOrderId(), country.getCountryId(),
			listType.getListTypeId(),
			_getRegionId(null, billingAddress, country),
			billingAddress.getCity(), billingAddress.getDescription(), false,
			billingAddress.getName(), false, billingAddress.getStreet1(),
			billingAddress.getStreet2(), billingAddress.getStreet3(),
			StringPool.BLANK, billingAddress.getZip(),
			billingAddress.getPhoneNumber(), serviceContext);
	}

	private static long _getCountryId(
		Address address, BillingAddress billingAddress, Country country) {

		if (Validator.isNull(billingAddress.getCountryISOCode()) &&
			(address != null)) {

			return address.getCountryId();
		}

		if (country == null) {
			return 0;
		}

		return country.getCountryId();
	}

	private static String _getDescription(Address address) {
		if (address == null) {
			return null;
		}

		return address.getDescription();
	}

	private static String _getPhoneNumber(Address address) {
		if (address == null) {
			return null;
		}

		return address.getPhoneNumber();
	}

	private static long _getRegionId(
			Address address, BillingAddress billingAddress, Country country)
		throws Exception {

		if (Validator.isNull(billingAddress.getRegionISOCode()) &&
			(address != null)) {

			return address.getRegionId();
		}

		if (Validator.isNull(billingAddress.getRegionISOCode()) ||
			(country == null)) {

			return 0;
		}

		Region region = RegionLocalServiceUtil.getRegion(
			country.getCountryId(), billingAddress.getRegionISOCode());

		return region.getRegionId();
	}

	private static String _getStreet2(Address address) {
		if (address == null) {
			return null;
		}

		return address.getStreet2();
	}

	private static String _getStreet3(Address address) {
		if (address == null) {
			return null;
		}

		return address.getStreet3();
	}

	private static String _getZip(Address address) {
		if (address == null) {
			return null;
		}

		return address.getZip();
	}

	private static CommerceOrder _updateCommerceOrderBillingAddress(
			AddressService addressService, BillingAddress billingAddress,
			CommerceOrder commerceOrder,
			CommerceOrderService commerceOrderService,
			CountryService countryService, ServiceContext serviceContext)
		throws Exception {

		Address address = addressService.fetchAddress(
			commerceOrder.getBillingAddressId());
		Country country = countryService.fetchCountryByA2(
			commerceOrder.getCompanyId(), billingAddress.getCountryISOCode());

		return commerceOrderService.updateBillingAddress(
			commerceOrder.getCommerceOrderId(), billingAddress.getName(),
			GetterUtil.get(
				billingAddress.getDescription(), _getDescription(address)),
			billingAddress.getStreet1(),
			GetterUtil.get(billingAddress.getStreet2(), _getStreet2(address)),
			GetterUtil.get(billingAddress.getStreet3(), _getStreet3(address)),
			billingAddress.getCity(),
			GetterUtil.get(billingAddress.getZip(), _getZip(address)),
			_getRegionId(address, billingAddress, country),
			_getCountryId(address, billingAddress, country),
			GetterUtil.get(
				billingAddress.getPhoneNumber(), _getPhoneNumber(address)),
			serviceContext);
	}

}