/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.order.content.web.internal.frontend.taglib;

import com.liferay.commerce.configuration.CommerceAccountGroupServiceConfiguration;
import com.liferay.commerce.configuration.CommerceOrderCheckoutConfiguration;
import com.liferay.commerce.constants.CommerceConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.content.web.internal.constants.CommerceOrderScreenNavigationConstants;
import com.liferay.commerce.product.constants.CommerceChannelConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.frontend.taglib.servlet.taglib.ScreenNavigationEntry;
import com.liferay.frontend.taglib.servlet.taglib.util.JSPRenderer;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Danny Situ
 */
@Component(
	property = "screen.navigation.entry.order:Integer=10",
	service = ScreenNavigationEntry.class
)
public class CommerceOrderMultishippingScreenNavigationEntry
	extends CommerceOrderMultishippingScreenNavigationCategory
	implements ScreenNavigationEntry<CommerceOrder> {

	@Override
	public String getEntryKey() {
		return CommerceOrderScreenNavigationConstants.ENTRY_KEY_MULTISHIPPING;
	}

	@Override
	public boolean isVisible(User user, CommerceOrder commerceOrder) {
		try {
			CommerceChannel commerceChannel =
				_commerceChannelLocalService.getCommerceChannelByOrderGroupId(
					commerceOrder.getGroupId());

			CommerceAccountGroupServiceConfiguration
				commerceAccountGroupServiceConfiguration =
					_configurationProvider.getConfiguration(
						CommerceAccountGroupServiceConfiguration.class,
						new GroupServiceSettingsLocator(
							commerceChannel.getGroupId(),
							CommerceConstants.SERVICE_NAME_COMMERCE_ACCOUNT));

			int commerceSiteType =
				commerceAccountGroupServiceConfiguration.commerceSiteType();

			if (commerceSiteType == CommerceChannelConstants.SITE_TYPE_B2B) {
				CommerceOrderCheckoutConfiguration
					commerceOrderCheckoutConfiguration =
						_configurationProvider.getConfiguration(
							CommerceOrderCheckoutConfiguration.class,
							new GroupServiceSettingsLocator(
								commerceChannel.getGroupId(),
								CommerceConstants.SERVICE_NAME_COMMERCE_ORDER));

				return commerceOrderCheckoutConfiguration.
					multishippingEnabled();
			}
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return false;
	}

	@Override
	public void render(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		_jspRenderer.renderJSP(
			_servletContext, httpServletRequest, httpServletResponse,
			"/pending_commerce_orders/multishipping.jsp");
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceOrderMultishippingScreenNavigationEntry.class);

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private JSPRenderer _jspRenderer;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.commerce.order.content.web)"
	)
	private ServletContext _servletContext;

}