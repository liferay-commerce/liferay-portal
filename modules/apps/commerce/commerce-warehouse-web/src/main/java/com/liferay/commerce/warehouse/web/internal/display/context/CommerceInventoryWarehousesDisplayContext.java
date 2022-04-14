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

package com.liferay.commerce.warehouse.web.internal.display.context;

import com.liferay.commerce.country.CommerceCountryManager;
import com.liferay.commerce.frontend.model.HeaderActionModel;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouse;
import com.liferay.commerce.inventory.service.CommerceInventoryWarehouseService;
import com.liferay.commerce.product.constants.CPPortletKeys;
import com.liferay.commerce.product.display.context.helper.CPRequestHelper;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.model.CommerceChannelRel;
import com.liferay.commerce.product.service.CommerceChannelRelService;
import com.liferay.commerce.product.service.CommerceChannelService;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.servlet.taglib.ManagementBarFilterItem;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.CountryService;
import com.liferay.portal.kernel.service.RegionService;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.portlet.RenderURL;
import javax.portlet.WindowStateException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Andrea Di Giorgi
 * @author Alessio Antonio Rendina
 */
public class CommerceInventoryWarehousesDisplayContext {

	public CommerceInventoryWarehousesDisplayContext(
		CommerceChannelRelService commerceChannelRelService,
		CommerceChannelService commerceChannelService,
		CommerceCountryManager commerceCountryManager,
		CommerceInventoryWarehouseService commerceInventoryWarehouseService,
		CountryService countryService, HttpServletRequest httpServletRequest,
		RegionService regionService, Portal portal,
		ModelResourcePermission<CommerceInventoryWarehouse>
			commerceInventoryWarehouseModelResourcePermission) {

		this.commerceChannelRelService = commerceChannelRelService;
		_commerceChannelService = commerceChannelService;
		_commerceCountryManager = commerceCountryManager;
		_commerceInventoryWarehouseService = commerceInventoryWarehouseService;
		_countryService = countryService;
		this.httpServletRequest = httpServletRequest;
		_regionService = regionService;

		cpRequestHelper = new CPRequestHelper(httpServletRequest);

		_portal = portal;
		_commerceInventoryWarehouseModelResourcePermission =
			commerceInventoryWarehouseModelResourcePermission;
	}

	public String getAddCommerceWarehouseRenderURL() throws Exception {
		return PortletURLBuilder.createRenderURL(
			cpRequestHelper.getLiferayPortletResponse()
		).setMVCRenderCommandName(
			"/commerce_inventory_warehouse/add_commerce_inventory_warehouse"
		).setWindowState(
			LiferayWindowState.POP_UP
		).buildString();
	}

	public long[] getCommerceChannelRelCommerceChannelIds()
		throws PortalException {

		CommerceInventoryWarehouse commerceInventoryWarehouse =
			getCommerceInventoryWarehouse();

		if (commerceInventoryWarehouse == null) {
			return new long[0];
		}

		List<CommerceChannelRel> commerceChannelRels =
			commerceChannelRelService.getCommerceChannelRels(
				CommerceInventoryWarehouse.class.getName(),
				commerceInventoryWarehouse.getCommerceInventoryWarehouseId(),
				null, QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Stream<CommerceChannelRel> stream = commerceChannelRels.stream();

		return stream.mapToLong(
			CommerceChannelRel::getCommerceChannelId
		).toArray();
	}

	public List<CommerceChannel> getCommerceChannels() throws PortalException {
		return _commerceChannelService.getCommerceChannels(
			cpRequestHelper.getCompanyId());
	}

	public CommerceInventoryWarehouse getCommerceInventoryWarehouse()
		throws PortalException {

		if (_commerceInventoryWarehouse != null) {
			return _commerceInventoryWarehouse;
		}

		long commerceInventoryWarehouseId = ParamUtil.getLong(
			cpRequestHelper.getRenderRequest(), "commerceInventoryWarehouseId");

		if (commerceInventoryWarehouseId > 0) {
			_commerceInventoryWarehouse =
				_commerceInventoryWarehouseService.
					getCommerceInventoryWarehouse(commerceInventoryWarehouseId);
		}

		return _commerceInventoryWarehouse;
	}

	public long getCommerceInventoryWarehouseId() throws PortalException {
		CommerceInventoryWarehouse commerceInventoryWarehouse =
			getCommerceInventoryWarehouse();

		if (commerceInventoryWarehouse == null) {
			return 0;
		}

		return commerceInventoryWarehouse.getCommerceInventoryWarehouseId();
	}

	public Country getCountry(long countryId) throws PortalException {
		return _countryService.getCountry(countryId);
	}

	public Country getCountry(String countryTwoLettersIsoCode)
		throws PortalException {

		return _countryService.getCountryByA2(
			cpRequestHelper.getCompanyId(), countryTwoLettersIsoCode);
	}

	public String getCountryTwoLettersIsoCode() {
		return ParamUtil.getString(
			cpRequestHelper.getRenderRequest(), "countryTwoLettersISOCode",
			null);
	}

	public PortletURL getEditCommerceWarehouseRenderURL() {
		return PortletURLBuilder.create(
			_portal.getControlPanelPortletURL(
				cpRequestHelper.getRequest(),
				CPPortletKeys.COMMERCE_INVENTORY_WAREHOUSE,
				PortletRequest.RENDER_PHASE)
		).setMVCRenderCommandName(
			"/commerce_inventory_warehouse/edit_commerce_inventory_warehouse"
		).buildPortletURL();
	}

	public List<HeaderActionModel> getHeaderActionModels() throws Exception {
		List<HeaderActionModel> headerActionModels = new ArrayList<>();

		LiferayPortletResponse liferayPortletResponse =
			cpRequestHelper.getLiferayPortletResponse();

		RenderResponse renderResponse = cpRequestHelper.getRenderResponse();

		RenderURL cancelURL = renderResponse.createRenderURL();

		HeaderActionModel cancelHeaderActionModel = new HeaderActionModel(
			null, cancelURL.toString(), null, "cancel");

		headerActionModels.add(cancelHeaderActionModel);

		HeaderActionModel saveHeaderActionModel = new HeaderActionModel(
			"btn-primary", liferayPortletResponse.getNamespace() + "fm",
			PortletURLBuilder.createActionURL(
				liferayPortletResponse
			).setActionName(
				"/commerce_inventory_warehouse" +
					"/edit_commerce_inventory_warehouse"
			).buildString(),
			null, "save");

		headerActionModels.add(saveHeaderActionModel);

		return headerActionModels;
	}

	public List<ManagementBarFilterItem> getManagementBarFilterItems()
		throws PortalException, PortletException {

		List<Country> countries = _commerceCountryManager.getWarehouseCountries(
			cpRequestHelper.getCompanyId(), true);

		countries = ListUtil.unique(countries);

		List<ManagementBarFilterItem> managementBarFilterItems =
			new ArrayList<>(countries.size() + 1);

		managementBarFilterItems.add(_getManagementBarFilterItem(-1, "all"));

		for (Country country : countries) {
			managementBarFilterItems.add(
				_getManagementBarFilterItem(
					country.getCountryId(),
					country.getName(cpRequestHelper.getLocale())));
		}

		return managementBarFilterItems;
	}

	public PortletURL getPortletCommerceInventoryWarehouseURL() {
		LiferayPortletResponse liferayPortletResponse =
			cpRequestHelper.getLiferayPortletResponse();

		PortletURL portletURL = liferayPortletResponse.createRenderURL();

		String redirect = ParamUtil.getString(httpServletRequest, "redirect");

		if (Validator.isNotNull(redirect)) {
			portletURL.setParameter("redirect", redirect);
		}

		long commerceInventoryWarehouseId = ParamUtil.getLong(
			httpServletRequest, "commerceInventoryWarehouseId");

		if (commerceInventoryWarehouseId > 0) {
			portletURL.setParameter(
				"commerceInventoryWarehouseId",
				String.valueOf(commerceInventoryWarehouseId));
		}

		return portletURL;
	}

	public PortletURL getPortletURL() {
		return PortletURLBuilder.createRenderURL(
			cpRequestHelper.getRenderResponse()
		).setKeywords(
			_getKeywords()
		).setNavigation(
			_getNavigation()
		).setParameter(
			"countryTwoLettersISOCode", getCountryTwoLettersIsoCode()
		).setParameter(
			"delta",
			() -> {
				String delta = ParamUtil.getString(
					cpRequestHelper.getRenderRequest(), "delta");

				if (Validator.isNotNull(delta)) {
					return delta;
				}

				return null;
			}
		).buildPortletURL();
	}

	public List<Region> getRegions() throws PortalException {
		Country countryByA2 = _countryService.getCountryByA2(
			cpRequestHelper.getCompanyId(),
			_commerceInventoryWarehouse.getCountryTwoLettersISOCode());

		return _regionService.getRegions(countryByA2.getCountryId(), true);
	}

	public CreationMenu getWarehouseCreationMenu() {
		CreationMenu creationMenu = new CreationMenu();

		if (hasManageCommerceInventoryWarehousePermission()) {
			creationMenu.addDropdownItem(
				dropdownItem -> {
					dropdownItem.setHref(getAddCommerceWarehouseRenderURL());
					dropdownItem.setLabel(
						LanguageUtil.get(
							cpRequestHelper.getRequest(), "add-warehouse"));
					dropdownItem.setTarget("modal");
				});
		}

		return creationMenu;
	}

	public List<FDSActionDropdownItem> getWarehouseFDSActionDropdownItems()
		throws PortalException {

		return ListUtil.fromArray(
			new FDSActionDropdownItem(
				PortletURLBuilder.create(
					PortletProviderUtil.getPortletURL(
						cpRequestHelper.getRequest(),
						CommerceInventoryWarehouse.class.getName(),
						PortletProvider.Action.MANAGE)
				).setMVCRenderCommandName(
					"/commerce_inventory_warehouse" +
						"/edit_commerce_inventory_warehouse"
				).setRedirect(
					cpRequestHelper.getCurrentURL()
				).setParameter(
					"commerceInventoryWarehouseId", "{id}"
				).setParameter(
					"screenNavigationCategoryKey", "details"
				).buildString(),
				"pencil", "edit",
				LanguageUtil.get(cpRequestHelper.getRequest(), "edit"), "get",
				null, null),
			new FDSActionDropdownItem(
				null, "trash", "delete",
				LanguageUtil.get(cpRequestHelper.getRequest(), "delete"),
				"delete", "delete", "headless"),
			new FDSActionDropdownItem(
				_getManageWarehousePermissionsURL(), null, "permissions",
				LanguageUtil.get(cpRequestHelper.getRequest(), "permissions"),
				"get", "permissions", "modal-permissions"));
	}

	public boolean hasManageCommerceInventoryWarehousePermission() {
		return true;
	}

	public boolean hasPermission(String actionId) throws PortalException {
		CommerceInventoryWarehouse commerceInventoryWarehouse =
			getCommerceInventoryWarehouse();

		return _commerceInventoryWarehouseModelResourcePermission.contains(
			cpRequestHelper.getPermissionChecker(),
			commerceInventoryWarehouse.getCommerceInventoryWarehouseId(),
			actionId);
	}

	protected CommerceChannelRelService commerceChannelRelService;
	protected final CPRequestHelper cpRequestHelper;
	protected HttpServletRequest httpServletRequest;

	private String _getKeywords() {
		if (_keywords != null) {
			return _keywords;
		}

		_keywords = ParamUtil.getString(
			cpRequestHelper.getRenderRequest(), "keywords");

		return _keywords;
	}

	private ManagementBarFilterItem _getManagementBarFilterItem(
			long countryId, String label)
		throws PortalException, PortletException {

		boolean active = false;

		PortletURL portletURL = PortletURLUtil.clone(
			getPortletURL(), cpRequestHelper.getRenderResponse());

		if (countryId > 0) {
			String countryTwoLettersIsoCode = getCountryTwoLettersIsoCode();
			Country country = getCountry(countryId);

			if (Validator.isNotNull(countryTwoLettersIsoCode) &&
				countryTwoLettersIsoCode.equals(country.getA2())) {

				active = true;
			}

			portletURL.setParameter(
				"countryTwoLettersISOCode", country.getA2());
		}
		else {
			portletURL.setParameter(
				"countryTwoLettersISOCode", StringPool.BLANK);
		}

		return new ManagementBarFilterItem(
			active, String.valueOf(countryId), label, portletURL.toString());
	}

	private String _getManageWarehousePermissionsURL() throws PortalException {
		PortletURL portletURL = PortletURLBuilder.create(
			_portal.getControlPanelPortletURL(
				cpRequestHelper.getRequest(),
				"com_liferay_portlet_configuration_web_portlet_" +
					"PortletConfigurationPortlet",
				ActionRequest.RENDER_PHASE)
		).setMVCPath(
			"/edit_permissions.jsp"
		).setRedirect(
			cpRequestHelper.getCurrentURL()
		).setParameter(
			"modelResource", CommerceInventoryWarehouse.class.getName()
		).setParameter(
			"modelResourceDescription", "{name}"
		).setParameter(
			"resourcePrimKey", "{id}"
		).buildPortletURL();

		try {
			portletURL.setWindowState(LiferayWindowState.POP_UP);
		}
		catch (WindowStateException windowStateException) {
			throw new PortalException(windowStateException);
		}

		return portletURL.toString();
	}

	private String _getNavigation() {
		return ParamUtil.getString(
			cpRequestHelper.getRenderRequest(), "navigation");
	}

	private final CommerceChannelService _commerceChannelService;
	private final CommerceCountryManager _commerceCountryManager;
	private CommerceInventoryWarehouse _commerceInventoryWarehouse;
	private final ModelResourcePermission<CommerceInventoryWarehouse>
		_commerceInventoryWarehouseModelResourcePermission;
	private final CommerceInventoryWarehouseService
		_commerceInventoryWarehouseService;
	private final CountryService _countryService;
	private String _keywords;
	private final Portal _portal;
	private final RegionService _regionService;

}