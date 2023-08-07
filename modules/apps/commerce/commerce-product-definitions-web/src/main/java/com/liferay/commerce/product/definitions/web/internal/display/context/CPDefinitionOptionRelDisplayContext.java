/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.definitions.web.internal.display.context;

import com.liferay.commerce.product.configuration.CPOptionConfiguration;
import com.liferay.commerce.product.constants.CPConstants;
import com.liferay.commerce.product.display.context.BaseCPDefinitionsDisplayContext;
import com.liferay.commerce.product.item.selector.criterion.CPOptionItemSelectorCriterion;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.option.CommerceOptionType;
import com.liferay.commerce.product.option.CommerceOptionTypeRegistry;
import com.liferay.commerce.product.portlet.action.ActionHelper;
import com.liferay.commerce.product.servlet.taglib.ui.constants.CPDefinitionScreenNavigationConstants;
import com.liferay.commerce.product.util.CommerceOptionTypeUtil;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.criteria.UUIDItemSelectorReturnType;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.settings.SystemSettingsLocator;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.taglib.util.CustomAttributesUtil;

import java.util.Collections;
import java.util.List;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Alessio Antonio Rendina
 * @author Marco Leo
 */
public class CPDefinitionOptionRelDisplayContext
	extends BaseCPDefinitionsDisplayContext {

	public CPDefinitionOptionRelDisplayContext(
		ActionHelper actionHelper, HttpServletRequest httpServletRequest,
		CommerceOptionTypeRegistry commerceOptionTypeRegistry,
		ConfigurationProvider configurationProvider,
		ItemSelector itemSelector) {

		super(actionHelper, httpServletRequest);

		_commerceOptionTypeRegistry = commerceOptionTypeRegistry;
		_configurationProvider = configurationProvider;
		_itemSelector = itemSelector;
	}

	public String getCommerceOptionTypeKeys() throws PortalException {
		CPOptionConfiguration cpOptionConfiguration =
			_configurationProvider.getConfiguration(
				CPOptionConfiguration.class,
				new SystemSettingsLocator(CPConstants.SERVICE_NAME_CP_OPTION));

		return StringUtil.merge(
			cpOptionConfiguration.commerceOptionTypesAllowed(),
			StringPool.COMMA);
	}

	public List<CommerceOptionType> getCommerceOptionTypes()
		throws PortalException {

		List<CommerceOptionType> commerceOptionTypes =
			_commerceOptionTypeRegistry.getCommerceOptionTypes();

		CPOptionConfiguration cpOptionConfiguration =
			_configurationProvider.getConfiguration(
				CPOptionConfiguration.class,
				new SystemSettingsLocator(CPConstants.SERVICE_NAME_CP_OPTION));

		String[] commerceOptionTypesAllowed =
			cpOptionConfiguration.commerceOptionTypesAllowed();

		return CommerceOptionTypeUtil.getCommerceOptionTypesAllowed(
			commerceOptionTypes, commerceOptionTypesAllowed);
	}

	public CPDefinitionOptionRel getCPDefinitionOptionRel()
		throws PortalException {

		if (_cpDefinitionOptionRel != null) {
			return _cpDefinitionOptionRel;
		}

		_cpDefinitionOptionRel = actionHelper.getCPDefinitionOptionRel(
			cpRequestHelper.getRenderRequest());

		return _cpDefinitionOptionRel;
	}

	public long getCPDefinitionOptionRelId() throws PortalException {
		CPDefinitionOptionRel cpDefinitionOptionRel =
			getCPDefinitionOptionRel();

		if (cpDefinitionOptionRel == null) {
			return 0;
		}

		return cpDefinitionOptionRel.getCPDefinitionOptionRelId();
	}

	public CreationMenu getCreationMenu() throws Exception {
		return CreationMenuBuilder.addDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					PortletURLBuilder.createRenderURL(
						liferayPortletResponse
					).setMVCRenderCommandName(
						"/cp_definitions/edit_cp_definition_option_value_rel"
					).setParameter(
						"cpDefinitionId", getCPDefinitionId()
					).setParameter(
						"cpDefinitionOptionRelId", getCPDefinitionOptionRelId()
					).setWindowState(
						LiferayWindowState.POP_UP
					).buildString());
				dropdownItem.setLabel(
					LanguageUtil.get(
						cpRequestHelper.getRequest(), "add-value"));
				dropdownItem.setTarget("modal-lg");
			}
		).build();
	}

	public String getItemSelectorUrl() {
		RequestBackedPortletURLFactory requestBackedPortletURLFactory =
			RequestBackedPortletURLFactoryUtil.create(
				cpRequestHelper.getRenderRequest());

		CPOptionItemSelectorCriterion cpOptionItemSelectorCriterion =
			new CPOptionItemSelectorCriterion();

		cpOptionItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			Collections.<ItemSelectorReturnType>singletonList(
				new UUIDItemSelectorReturnType()));

		return String.valueOf(
			_itemSelector.getItemSelectorURL(
				requestBackedPortletURLFactory, "productOptionsSelectItem",
				cpOptionItemSelectorCriterion));
	}

	@Override
	public PortletURL getPortletURL() throws PortalException {
		return PortletURLBuilder.create(
			super.getPortletURL()
		).setMVCRenderCommandName(
			"/cp_definitions/edit_cp_definition"
		).setParameter(
			"cpDefinitionId", getCPDefinitionId()
		).setParameter(
			"screenNavigationCategoryKey", getScreenNavigationCategoryKey()
		).buildPortletURL();
	}

	@Override
	public String getScreenNavigationCategoryKey() {
		return CPDefinitionScreenNavigationConstants.CATEGORY_KEY_OPTIONS;
	}

	public boolean hasCustomAttributesAvailable() throws Exception {
		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return CustomAttributesUtil.hasCustomAttributes(
			themeDisplay.getCompanyId(), CPDefinitionOptionRel.class.getName(),
			getCPDefinitionOptionRelId(), null);
	}

	private final CommerceOptionTypeRegistry _commerceOptionTypeRegistry;
	private final ConfigurationProvider _configurationProvider;
	private CPDefinitionOptionRel _cpDefinitionOptionRel;
	private final ItemSelector _itemSelector;

}