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

package com.liferay.commerce.payment.web.internal.frontend.data.set.provider;

import com.liferay.account.constants.AccountPortletKeys;
import com.liferay.account.model.AccountEntry;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelService;
import com.liferay.commerce.payment.web.internal.constants.CommercePaymentMethodGroupRelFDSNames;
import com.liferay.commerce.payment.web.internal.model.PaymentMethod;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.frontend.data.set.provider.FDSActionProvider;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Crescenzo Rega
 */
@Component(
	property = "fds.data.provider.key=" + CommercePaymentMethodGroupRelFDSNames.ACCOUNT_ENTRY_DEFAULT_PAYMENTS,
	service = FDSActionProvider.class
)
public class AccountEntryDefaultCommercePaymentsFDSActionProvider
	implements FDSActionProvider {

	@Override
	public List<DropdownItem> getDropdownItems(
			long groupId, HttpServletRequest httpServletRequest, Object model)
		throws PortalException {

		PaymentMethod paymentMethod = (PaymentMethod)model;

		return DropdownItemListBuilder.add(
			() -> _hasPermission(paymentMethod),
			dropdownItem -> {
				dropdownItem.setHref(
					_getAccountEntryDefaultCommercePaymentMethodEditURL(
						paymentMethod.getAccountEntryId(),
						paymentMethod.getCommerceChannelId(),
						httpServletRequest));
				dropdownItem.setLabel(
					_language.get(httpServletRequest, Constants.EDIT));
				dropdownItem.setTarget("modal-lg");
			}
		).build();
	}

	private String _getAccountEntryDefaultCommercePaymentMethodEditURL(
		long accountEntryId, long commerceChannelId,
		HttpServletRequest httpServletRequest) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		PortletURL portletURL = null;

		if (AccountPortletKeys.ACCOUNT_ENTRIES_ADMIN.equals(
				portletDisplay.getId())) {

			portletURL = _portal.getControlPanelPortletURL(
				httpServletRequest, AccountPortletKeys.ACCOUNT_ENTRIES_ADMIN,
				PortletRequest.RENDER_PHASE);
		}
		else if (AccountPortletKeys.ACCOUNT_ENTRIES_MANAGEMENT.equals(
					portletDisplay.getId())) {

			portletURL = PortletURLFactoryUtil.create(
				httpServletRequest,
				AccountPortletKeys.ACCOUNT_ENTRIES_MANAGEMENT,
				themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
		}

		return PortletURLBuilder.create(
			portletURL
		).setMVCRenderCommandName(
			"/commerce_payment" +
				"/edit_account_entry_default_commerce_payment_method"
		).setParameter(
			"accountEntryId", accountEntryId
		).setParameter(
			"commerceChannelId", commerceChannelId
		).setWindowState(
			LiferayWindowState.POP_UP
		).buildString();
	}

	private boolean _hasCommercePaymentMethodGroupRels(long commerceChannelId)
		throws PortalException {

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.getCommerceChannel(commerceChannelId);

		try {
			List<CommercePaymentMethodGroupRel> commercePaymentMethodGroupRels =
				_commercePaymentMethodGroupRelService.
					getCommercePaymentMethodGroupRels(
						commerceChannel.getGroupId(), true, QueryUtil.ALL_POS,
						QueryUtil.ALL_POS, null);

			return !commercePaymentMethodGroupRels.isEmpty();
		}
		catch (PrincipalException principalException) {
			_log.error(principalException);
		}

		return false;
	}

	private boolean _hasPermission(PaymentMethod paymentMethod)
		throws PortalException {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (_accountEntryModelResourcePermission.contains(
				permissionChecker, paymentMethod.getAccountEntryId(),
				ActionKeys.UPDATE) &&
			permissionChecker.hasPermission(
				null, CommerceChannel.class.getName(),
				CompanyThreadLocal.getCompanyId(), ActionKeys.VIEW) &&
			_hasCommercePaymentMethodGroupRels(
				paymentMethod.getCommerceChannelId())) {

			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AccountEntryDefaultCommercePaymentsFDSActionProvider.class);

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(model.class.name=com.liferay.account.model.AccountEntry)"
	)
	private volatile ModelResourcePermission<AccountEntry>
		_accountEntryModelResourcePermission;

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference
	private CommercePaymentMethodGroupRelService
		_commercePaymentMethodGroupRelService;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

}