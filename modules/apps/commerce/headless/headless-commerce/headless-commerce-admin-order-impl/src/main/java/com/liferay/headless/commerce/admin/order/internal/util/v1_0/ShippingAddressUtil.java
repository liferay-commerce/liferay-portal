/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.order.internal.util.v1_0;

import com.liferay.account.constants.AccountListTypeConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.headless.commerce.admin.order.dto.v1_0.ShippingAddress;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.AddressService;
import com.liferay.portal.kernel.service.CountryService;
import com.liferay.portal.kernel.service.ListTypeLocalService;
import com.liferay.portal.kernel.service.RegionLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Alessio Antonio Rendina
 */
public class ShippingAddressUtil {

	public static CommerceOrder addOrUpdateShippingAddress(
			AddressService addressService, CommerceOrder commerceOrder,
			CommerceOrderService commerceOrderService,
			CountryService countryService,
			ListTypeLocalService listTypeLocalService,
			ShippingAddress shippingAddress, ServiceContext serviceContext)
		throws Exception {

		if (commerceOrder.getShippingAddressId() > 0) {
			return _updateCommerceOrderShippingAddress(
				addressService, commerceOrder, commerceOrderService,
				countryService, shippingAddress, serviceContext);
		}

		Address address = _addAddress(
			addressService, countryService, commerceOrder, listTypeLocalService,
			shippingAddress, serviceContext);

		return commerceOrderService.updateShippingAddress(
			commerceOrder.getCommerceOrderId(), address.getName(),
			address.getDescription(), address.getStreet1(),
			address.getStreet2(), address.getStreet3(), address.getCity(),
			address.getZip(), address.getRegionId(), address.getCountryId(),
			address.getPhoneNumber(), serviceContext);
	}

	private static Address _addAddress(
			AddressService addressService, CountryService countryService,
			CommerceOrder commerceOrder,
			ListTypeLocalService listTypeLocalService,
			ShippingAddress shippingAddress, ServiceContext serviceContext)
		throws Exception {

		Country country = countryService.getCountryByA2(
			commerceOrder.getCompanyId(), shippingAddress.getCountryISOCode());
		ListType listType = listTypeLocalService.getListType(
			CompanyThreadLocal.getCompanyId(),
			AccountListTypeConstants.
				ACCOUNT_ENTRY_ADDRESS_TYPE_BILLING_AND_SHIPPING,
			AccountListTypeConstants.ACCOUNT_ENTRY_ADDRESS);

		return addressService.addAddress(
			StringPool.BLANK, commerceOrder.getModelClassName(),
			commerceOrder.getCommerceOrderId(), country.getCountryId(),
			listType.getListTypeId(),
			_getRegionId(null, country, shippingAddress),
			shippingAddress.getCity(), shippingAddress.getDescription(), false,
			shippingAddress.getName(), false, shippingAddress.getStreet1(),
			shippingAddress.getStreet2(), shippingAddress.getStreet3(),
			StringPool.BLANK, shippingAddress.getZip(),
			shippingAddress.getPhoneNumber(), serviceContext);
	}

	private static long _getCountryId(
		Address address, Country country, ShippingAddress shippingAddress) {

		if (Validator.isNull(shippingAddress.getCountryISOCode()) &&
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
			Address address, Country country, ShippingAddress shippingAddress)
		throws Exception {

		if ((country != null) &&
			Validator.isNotNull(shippingAddress.getRegionISOCode())) {

			Region region = RegionLocalServiceUtil.getRegion(
				country.getCountryId(), shippingAddress.getRegionISOCode());

			return region.getRegionId();
		}

		if (address != null) {
			return address.getRegionId();
		}

		return 0;
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

	private static CommerceOrder _updateCommerceOrderShippingAddress(
			AddressService addressService, CommerceOrder commerceOrder,
			CommerceOrderService commerceOrderService,
			CountryService countryService, ShippingAddress shippingAddress,
			ServiceContext serviceContext)
		throws Exception {

		Address address = addressService.fetchAddress(
			commerceOrder.getShippingAddressId());
		Country country = countryService.fetchCountryByA2(
			commerceOrder.getCompanyId(), shippingAddress.getCountryISOCode());

		return commerceOrderService.updateShippingAddress(
			commerceOrder.getCommerceOrderId(), shippingAddress.getName(),
			GetterUtil.get(
				shippingAddress.getDescription(), _getDescription(address)),
			shippingAddress.getStreet1(),
			GetterUtil.get(shippingAddress.getStreet2(), _getStreet2(address)),
			GetterUtil.get(shippingAddress.getStreet3(), _getStreet3(address)),
			shippingAddress.getCity(),
			GetterUtil.get(shippingAddress.getZip(), _getZip(address)),
			_getRegionId(address, country, shippingAddress),
			_getCountryId(address, country, shippingAddress),
			GetterUtil.get(
				shippingAddress.getPhoneNumber(), _getPhoneNumber(address)),
			serviceContext);
	}

}