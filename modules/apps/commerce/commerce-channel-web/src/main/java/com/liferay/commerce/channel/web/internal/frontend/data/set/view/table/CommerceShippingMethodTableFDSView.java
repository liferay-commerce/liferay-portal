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

package com.liferay.commerce.channel.web.internal.frontend.data.set.view.table;

import com.liferay.commerce.channel.web.internal.constants.CommerceChannelFDSNames;
import com.liferay.commerce.channel.web.internal.frontend.util.CommerceChannelClayTableUtil;
import com.liferay.commerce.channel.web.internal.model.ShippingMethod;
import com.liferay.commerce.constants.CommerceActionKeys;
import com.liferay.commerce.model.CommerceShippingEngine;
import com.liferay.commerce.model.CommerceShippingMethod;
import com.liferay.commerce.permission.CommerceShippingMethodPermission;
import com.liferay.commerce.product.constants.CPConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelService;
import com.liferay.commerce.service.CommerceShippingMethodService;
import com.liferay.commerce.util.CommerceShippingEngineRegistry;
import com.liferay.frontend.data.set.provider.FDSActionProvider;
import com.liferay.frontend.data.set.provider.FDSDataProvider;
import com.liferay.frontend.data.set.provider.search.FDSKeywords;
import com.liferay.frontend.data.set.provider.search.FDSPagination;
import com.liferay.frontend.data.set.view.FDSView;
import com.liferay.frontend.data.set.view.table.BaseTableFDSView;
import com.liferay.frontend.data.set.view.table.FDSTableSchema;
import com.liferay.frontend.data.set.view.table.FDSTableSchemaBuilder;
import com.liferay.frontend.data.set.view.table.FDSTableSchemaBuilderFactory;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.portlet.PortletQName;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(
	property = {
		"fds.data.provider.key=" + CommerceChannelFDSNames.SHIPPING_METHOD,
		"frontend.data.set.name=" + CommerceChannelFDSNames.SHIPPING_METHOD
	},
	service = {FDSActionProvider.class, FDSDataProvider.class, FDSView.class}
)
public class CommerceShippingMethodTableFDSView
	extends BaseTableFDSView
	implements FDSActionProvider, FDSDataProvider<ShippingMethod> {

	@Override
	public List<DropdownItem> getDropdownItems(
			long groupId, HttpServletRequest httpServletRequest, Object model)
		throws PortalException {

		long commerceChannelId = ParamUtil.getLong(
			httpServletRequest, "commerceChannelId");
		ShippingMethod shippingMethod = (ShippingMethod)model;

		CommerceChannel commerceChannel =
			_commerceChannelService.getCommerceChannel(commerceChannelId);

		CommerceShippingMethod commerceShippingMethod =
			_commerceShippingMethodService.fetchCommerceShippingMethod(
				commerceChannel.getGroupId(), shippingMethod.getKey());

		return DropdownItemListBuilder.add(
			() -> _portletResourcePermission.contains(
				PermissionThreadLocal.getPermissionChecker(),
				commerceChannel.getGroupId(),
				CommerceActionKeys.MANAGE_COMMERCE_SHIPPING_METHOD),
			dropdownItem -> {
				dropdownItem.setHref(
					PortletURLBuilder.create(
						PortletProviderUtil.getPortletURL(
							httpServletRequest,
							CommerceShippingMethod.class.getName(),
							PortletProvider.Action.EDIT)
					).setParameter(
						"commerceChannelId", commerceChannelId
					).setParameter(
						"commerceShippingMethodEngineKey",
						shippingMethod.getKey()
					).setWindowState(
						LiferayWindowState.POP_UP
					).buildString());
				dropdownItem.setLabel(
					_language.get(httpServletRequest, "edit"));
				dropdownItem.setTarget("sidePanel");
			}
		).add(
			() ->
				(commerceShippingMethod != null) &&
				commerceShippingMethod.isActive() &&
				_commerceShippingMethodPermission.contains(
					PermissionThreadLocal.getPermissionChecker(),
					commerceShippingMethod.getCommerceShippingMethodId(),
					ActionKeys.PERMISSIONS),
			dropdownItem -> {
				dropdownItem.setHref(
					_getShippingMethodPermissionURL(
						commerceShippingMethod, httpServletRequest));
				dropdownItem.setLabel(
					_language.get(httpServletRequest, "permissions"));
				dropdownItem.setTarget("modal-permissions");
			}
		).build();
	}

	@Override
	public FDSTableSchema getFDSTableSchema(Locale locale) {
		FDSTableSchemaBuilder fdsTableSchemaBuilder =
			_fdsTableSchemaBuilderFactory.create();

		return fdsTableSchemaBuilder.add(
			"name", "name",
			fdsTableSchemaField -> fdsTableSchemaField.setContentRenderer(
				"actionLink")
		).add(
			"description", "description"
		).add(
			"shippingEngine", "shipping-engine"
		).add(
			"status", "status",
			fdsTableSchemaField -> fdsTableSchemaField.setContentRenderer(
				"label")
		).build();
	}

	@Override
	public List<ShippingMethod> getItems(
			FDSKeywords fdsKeywords, FDSPagination fdsPagination,
			HttpServletRequest httpServletRequest, Sort sort)
		throws PortalException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		long commerceChannelId = ParamUtil.getLong(
			httpServletRequest, "commerceChannelId");

		CommerceChannel commerceChannel =
			_commerceChannelService.getCommerceChannel(commerceChannelId);

		Map<String, CommerceShippingEngine> commerceShippingEngines =
			_commerceShippingEngineRegistry.getCommerceShippingEngines();

		List<ShippingMethod> shippingMethods = new ArrayList<>();

		for (Map.Entry<String, CommerceShippingEngine> entry :
				commerceShippingEngines.entrySet()) {

			CommerceShippingEngine commerceShippingEngine = entry.getValue();

			try {
				CommerceShippingMethod commerceShippingMethod =
					_commerceShippingMethodService.fetchCommerceShippingMethod(
						commerceChannel.getGroupId(), entry.getKey());

				String commerceShippingDescription =
					commerceShippingEngine.getDescription(
						themeDisplay.getLocale());
				String commerceShippingName = commerceShippingEngine.getName(
					themeDisplay.getLocale());

				if (commerceShippingMethod != null) {
					commerceShippingDescription =
						commerceShippingMethod.getDescription(
							themeDisplay.getLocale());
					commerceShippingName = commerceShippingMethod.getName(
						themeDisplay.getLocale());
				}

				shippingMethods.add(
					new ShippingMethod(
						commerceShippingDescription, entry.getKey(),
						commerceShippingName,
						commerceShippingEngine.getName(
							themeDisplay.getLocale()),
						CommerceChannelClayTableUtil.getLabelField(
							_isActive(commerceShippingMethod),
							themeDisplay.getLocale())));
			}
			catch (PortalException portalException) {
				_log.error(portalException);
			}
		}

		return shippingMethods;
	}

	@Override
	public int getItemsCount(
			FDSKeywords fdsKeywords, HttpServletRequest httpServletRequest)
		throws PortalException {

		Map<String, CommerceShippingEngine> commerceShippingEngines =
			_commerceShippingEngineRegistry.getCommerceShippingEngines();

		return commerceShippingEngines.size();
	}

	private PortletURL _getShippingMethodPermissionURL(
			CommerceShippingMethod commerceShippingMethod,
			HttpServletRequest httpServletRequest)
		throws PortalException {

		return PortletURLBuilder.create(
			_portal.getControlPanelPortletURL(
				httpServletRequest,
				"com_liferay_portlet_configuration_web_portlet_" +
					"PortletConfigurationPortlet",
				ActionRequest.RENDER_PHASE)
		).setMVCPath(
			"/edit_permissions.jsp"
		).setParameter(
			PortletQName.PUBLIC_RENDER_PARAMETER_NAMESPACE + "backURL",
			ParamUtil.getString(
				httpServletRequest, "currentUrl",
				_portal.getCurrentURL(httpServletRequest))
		).setParameter(
			"modelResource", CommerceShippingMethod.class.getName()
		).setParameter(
			"modelResourceDescription", commerceShippingMethod.getDescription()
		).setParameter(
			"resourcePrimKey",
			commerceShippingMethod.getCommerceShippingMethodId()
		).setWindowState(
			LiferayWindowState.POP_UP
		).buildPortletURL();
	}

	private boolean _isActive(CommerceShippingMethod commerceShippingMethod) {
		if (commerceShippingMethod == null) {
			return false;
		}

		return commerceShippingMethod.isActive();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceShippingMethodTableFDSView.class);

	@Reference
	private CommerceChannelService _commerceChannelService;

	@Reference
	private CommerceShippingEngineRegistry _commerceShippingEngineRegistry;

	@Reference
	private CommerceShippingMethodPermission _commerceShippingMethodPermission;

	@Reference
	private CommerceShippingMethodService _commerceShippingMethodService;

	@Reference
	private FDSTableSchemaBuilderFactory _fdsTableSchemaBuilderFactory;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

	@Reference(
		target = "(resource.name=" + CPConstants.RESOURCE_NAME_CHANNEL + ")"
	)
	private PortletResourcePermission _portletResourcePermission;

}