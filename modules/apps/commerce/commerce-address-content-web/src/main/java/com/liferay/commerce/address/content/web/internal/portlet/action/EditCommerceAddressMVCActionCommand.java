/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.address.content.web.internal.portlet.action;

import com.liferay.account.constants.AccountListTypeConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.commerce.constants.CommercePortletKeys;
import com.liferay.commerce.exception.NoSuchAddressException;
import com.liferay.commerce.model.CommerceAddress;
import com.liferay.commerce.service.CommerceAddressService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.AddressCityException;
import com.liferay.portal.kernel.exception.AddressStreetException;
import com.liferay.portal.kernel.exception.AddressZipException;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.service.AddressService;
import com.liferay.portal.kernel.service.ListTypeLocalService;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	property = {
		"javax.portlet.name=" + CommercePortletKeys.COMMERCE_ADDRESS_CONTENT,
		"mvc.command.name=/commerce_address_content/edit_commerce_address"
	},
	service = MVCActionCommand.class
)
public class EditCommerceAddressMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		try {
			if (cmd.equals(Constants.DELETE)) {
				_deleteCommerceAddress(actionRequest);
			}
			else if (cmd.equals(Constants.ADD) ||
					 cmd.equals(Constants.UPDATE)) {

				_updateCommerceAddress(actionRequest);
			}
		}
		catch (Exception exception) {
			if (exception instanceof NoSuchAddressException ||
				exception instanceof PrincipalException) {

				SessionErrors.add(actionRequest, exception.getClass());

				actionResponse.setRenderParameter("mvcPath", "/error.jsp");
			}
			else if (exception instanceof AddressCityException ||
					 exception instanceof AddressStreetException ||
					 exception instanceof AddressZipException) {

				hideDefaultErrorMessage(actionRequest);

				SessionErrors.add(actionRequest, exception.getClass());

				String redirect = _portal.getCurrentURL(actionRequest);

				sendRedirect(actionRequest, actionResponse, redirect);
			}
			else {
				throw exception;
			}
		}

		hideDefaultSuccessMessage(actionRequest);
	}

	private void _deleteCommerceAddress(ActionRequest actionRequest)
		throws Exception {

		long commerceAddressId = ParamUtil.getLong(
			actionRequest, "commerceAddressId");

		if (commerceAddressId > 0) {
			_commerceAddressService.deleteCommerceAddress(commerceAddressId);
		}
	}

	private long _getListTypeId(
		boolean defaultBilling, boolean defaultShipping) {

		String name =
			AccountListTypeConstants.
				ACCOUNT_ENTRY_ADDRESS_TYPE_BILLING_AND_SHIPPING;

		if (defaultBilling && !defaultShipping) {
			name =
				AccountListTypeConstants.ACCOUNT_ENTRY_ADDRESS_TYPE_BILLING;
		}
		else if (!defaultBilling && defaultShipping) {
			name =
				AccountListTypeConstants.ACCOUNT_ENTRY_ADDRESS_TYPE_SHIPPING;
		}

		ListType listType = _listTypeLocalService.getListType(
			CompanyThreadLocal.getCompanyId(), name,
			AccountListTypeConstants.ACCOUNT_ENTRY_ADDRESS);

		return listType.getListTypeId();
	}

	private void _updateCommerceAddress(ActionRequest actionRequest)
		throws Exception {

		long commerceAddressId = ParamUtil.getLong(
			actionRequest, "commerceAddressId");

		if (commerceAddressId <= 0) {
			_addressService.addAddress(
				StringPool.BLANK, AccountEntry.class.getName(),
				ParamUtil.getLong(actionRequest, "commerceAccountId"),
				ParamUtil.getLong(actionRequest, "countryId"),
				_getListTypeId(
					ParamUtil.getBoolean(actionRequest, "defaultBilling"),
					ParamUtil.getBoolean(actionRequest, "defaultShipping")),
				ParamUtil.getLong(actionRequest, "regionId"),
				ParamUtil.getString(actionRequest, "city"),
				ParamUtil.getString(actionRequest, "description"), false,
				ParamUtil.getString(actionRequest, "name"), false,
				ParamUtil.getString(actionRequest, "street1"),
				ParamUtil.getString(actionRequest, "street2"),
				ParamUtil.getString(actionRequest, "street3"), StringPool.BLANK,
				ParamUtil.getString(actionRequest, "zip"),
				ParamUtil.getString(actionRequest, "phoneNumber"),
				ServiceContextFactory.getInstance(
					CommerceAddress.class.getName(), actionRequest));
		}
		else {
			Address address = _addressService.getAddress(commerceAddressId);

			_addressService.updateAddress(
				address.getExternalReferenceCode(), commerceAddressId,
				ParamUtil.getLong(actionRequest, "countryId"),
				_getListTypeId(
					ParamUtil.getBoolean(actionRequest, "defaultBilling"),
					ParamUtil.getBoolean(actionRequest, "defaultShipping")),
				ParamUtil.getLong(actionRequest, "regionId"),
				ParamUtil.getString(actionRequest, "city"),
				ParamUtil.getString(actionRequest, "description"),
				address.isMailing(), ParamUtil.getString(actionRequest, "name"),
				address.isPrimary(),
				ParamUtil.getString(actionRequest, "street1"),
				ParamUtil.getString(actionRequest, "street2"),
				ParamUtil.getString(actionRequest, "street3"),
				address.getSubtype(), ParamUtil.getString(actionRequest, "zip"),
				ParamUtil.getString(actionRequest, "phoneNumber"));
		}
	}

	@Reference
	private AddressService _addressService;

	@Reference
	private CommerceAddressService _commerceAddressService;

	@Reference
	private ListTypeLocalService _listTypeLocalService;

	@Reference
	private Portal _portal;

}