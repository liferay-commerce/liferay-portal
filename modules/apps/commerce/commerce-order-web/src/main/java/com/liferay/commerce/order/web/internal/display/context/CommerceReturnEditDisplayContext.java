/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.order.web.internal.display.context;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.commerce.currency.util.CommercePriceFormatter;
import com.liferay.commerce.model.CommerceAddress;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.model.CommerceReturn;
import com.liferay.commerce.model.CommerceReturnItem;
import com.liferay.commerce.order.web.internal.display.context.helper.CommerceReturnRequestHelper;
import com.liferay.commerce.product.display.context.helper.CPRequestHelper;
import com.liferay.commerce.service.CommerceOrderItemLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeDefinitionService;
import com.liferay.list.type.service.ListTypeEntryService;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.webserver.WebServerServletTokenUtil;

import java.io.Serializable;

import java.math.BigDecimal;

import java.text.DateFormat;
import java.text.Format;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.portlet.RenderRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Stefano Motta
 */
public class CommerceReturnEditDisplayContext {

	public CommerceReturnEditDisplayContext(
			AccountEntryLocalService accountEntryLocalService,
			CommerceOrderLocalService commerceOrderLocalService,
			CommerceOrderItemLocalService commerceOrderItemLocalService,
			CommercePriceFormatter commercePriceFormatter, Language language,
			ListTypeDefinitionService listTypeDefinitionService,
			ListTypeEntryService listTypeEntryService,
			ObjectEntryLocalService objectEntryLocalService,
			HttpServletRequest httpServletRequest, RenderRequest renderRequest)
		throws PortalException {

		_accountEntryLocalService = accountEntryLocalService;
		_commerceOrderLocalService = commerceOrderLocalService;
		_commerceOrderItemLocalService = commerceOrderItemLocalService;
		_commercePriceFormatter = commercePriceFormatter;
		_language = language;
		_listTypeDefinitionService = listTypeDefinitionService;
		_listTypeEntryService = listTypeEntryService;
		_objectEntryLocalService = objectEntryLocalService;

		long commerceReturnId = ParamUtil.getLong(
			renderRequest, "commerceReturnId");

		if (commerceReturnId > 0) {
			_commerceReturn = new CommerceReturn(
				objectEntryLocalService.getObjectEntry(commerceReturnId));
		}
		else {
			_commerceReturn = null;
		}

		_commerceReturnRequestHelper = new CommerceReturnRequestHelper(
			renderRequest);

		ThemeDisplay themeDisplay =
			_commerceReturnRequestHelper.getThemeDisplay();

		_commerceDateTimeFormat = FastDateFormatFactoryUtil.getDateTime(
			DateFormat.SHORT, DateFormat.SHORT, themeDisplay.getLocale(),
			themeDisplay.getTimeZone());

		_cpRequestHelper = new CPRequestHelper(httpServletRequest);
	}

	public String getAmountFormatted(BigDecimal amount) throws PortalException {
		CommerceOrder commerceOrder = getCommerceReturnCommerceOrder();

		return _commercePriceFormatter.format(
			commerceOrder.getCommerceCurrency(), amount,
			_commerceReturnRequestHelper.getLocale());
	}

	public CommerceReturn getCommerceReturn() {
		return _commerceReturn;
	}

	public AccountEntry getCommerceReturnAccountEntry() throws PortalException {
		if (_commerceReturn == null) {
			return null;
		}

		if (_accountEntry != null) {
			return _accountEntry;
		}

		_accountEntry = _accountEntryLocalService.getAccountEntry(
			_commerceReturn.getAccountId());

		return _accountEntry;
	}

	public String getCommerceReturnAccountEntryThumbnailURL()
		throws PortalException {

		if (_commerceReturn == null) {
			return StringPool.BLANK;
		}

		AccountEntry accountEntry = getCommerceReturnAccountEntry();

		ThemeDisplay themeDisplay =
			_commerceReturnRequestHelper.getThemeDisplay();

		StringBundler sb = new StringBundler(5);

		sb.append(themeDisplay.getPathImage());
		sb.append("/organization_logo?img_id=");
		sb.append(accountEntry.getLogoId());

		if (accountEntry.getLogoId() > 0) {
			sb.append("&t=");
			sb.append(
				WebServerServletTokenUtil.getToken(accountEntry.getLogoId()));
		}

		return sb.toString();
	}

	public CommerceOrder getCommerceReturnCommerceOrder()
		throws PortalException {

		if (_commerceReturn == null) {
			return null;
		}

		if (_commerceOrder != null) {
			return _commerceOrder;
		}

		_commerceOrder = _commerceOrderLocalService.getCommerceOrder(
			_commerceReturn.getOrderId());

		return _commerceOrder;
	}

	public CommerceReturnItem getCommerceReturnItem() {
		if (_commerceReturnItem != null) {
			return _commerceReturnItem;
		}

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			getCommerceReturnItemId());

		if (objectEntry == null) {
			return _commerceReturnItem;
		}

		_commerceReturnItem = new CommerceReturnItem(objectEntry);

		return _commerceReturnItem;
	}

	public CommerceOrderItem getCommerceReturnItemCommerceOrderItem()
		throws PortalException {

		if (_commerceReturnItem == null) {
			return null;
		}

		if (_commerceOrderItem != null) {
			return _commerceOrderItem;
		}

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			getCommerceReturnItemId());

		Map<String, Serializable> values = objectEntry.getValues();

		_commerceOrderItem =
			_commerceOrderItemLocalService.getCommerceOrderItem(
				GetterUtil.getLong(
					values.get(
						"r_commerceOrderItemToCommerceReturnItems_" +
							"commerceOrderItemId")));

		return _commerceOrderItem;
	}

	public List<FDSActionDropdownItem>
			getCommerceReturnItemFDSActionDropdownItems()
		throws PortalException {

		HttpServletRequest httpServletRequest = _cpRequestHelper.getRequest();

		return ListUtil.fromArray(
			new FDSActionDropdownItem(
				PortletURLBuilder.create(
					PortletProviderUtil.getPortletURL(
						_commerceReturnRequestHelper.getRequest(),
						CommerceReturn.class.getName(),
						PortletProvider.Action.MANAGE)
				).setMVCRenderCommandName(
					"/commerce_return_item/edit_commerce_return_item"
				).setParameter(
					"commerceReturnItemId", "{id}"
				).setWindowState(
					LiferayWindowState.POP_UP
				).buildString(),
				null, "edit", _language.get(httpServletRequest, "edit"), "get",
				"get", "sidePanel"),
			new FDSActionDropdownItem(
				null, null, "delete",
				_language.get(httpServletRequest, "delete"), "delete", "delete",
				"headless"));
	}

	public long getCommerceReturnItemId() {
		if (_commerceReturnItemId > 0) {
			return _commerceReturnItemId;
		}

		_commerceReturnItemId = ParamUtil.getLong(
			_cpRequestHelper.getRequest(), "commerceReturnItemId");

		return _commerceReturnItemId;
	}

	public String getDateTimeFormatted(Date date) {
		if (date == null) {
			return StringPool.BLANK;
		}

		return _commerceDateTimeFormat.format(date);
	}

	public String getDescriptiveAddress(CommerceAddress commerceAddress) {
		StringBundler sb = new StringBundler(5);

		sb.append(HtmlUtil.escape(commerceAddress.getCity()));
		sb.append(StringPool.COMMA_AND_SPACE);

		try {
			Region region = commerceAddress.getRegion();

			if (region != null) {
				sb.append(HtmlUtil.escape(region.getName()));
				sb.append(StringPool.COMMA_AND_SPACE);
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		sb.append(HtmlUtil.escape(commerceAddress.getZip()));

		return sb.toString();
	}

	public String getListTypeEntriesByExternalReferenceCodeURL() {
		return StringBundler.concat(
			"/o/headless-admin-list-type/v1.0/list-type-definitions",
			"/by-external-reference-code/L_COMMERCE_RETURN_RESOLUTION_METHODS",
			"/list-type-entries");
	}

	public String getResolutionMethodName() throws PortalException {
		ListTypeDefinition listTypeDefinition =
			_listTypeDefinitionService.
				fetchListTypeDefinitionByExternalReferenceCode(
					"L_COMMERCE_RETURN_RESOLUTION_METHODS",
					_cpRequestHelper.getCompanyId());

		if (listTypeDefinition == null) {
			return StringPool.BLANK;
		}

		CommerceReturnItem commerceReturnItem = getCommerceReturnItem();

		if (commerceReturnItem == null) {
			return StringPool.BLANK;
		}

		for (ListTypeEntry listTypeEntry :
				_listTypeEntryService.getListTypeEntries(
					listTypeDefinition.getListTypeDefinitionId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			if (Objects.equals(
					listTypeEntry.getKey(),
					commerceReturnItem.getReturnResolutionMethod())) {

				return listTypeEntry.getName(_cpRequestHelper.getLocale());
			}
		}

		return StringPool.BLANK;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceReturnEditDisplayContext.class);

	private AccountEntry _accountEntry;
	private final AccountEntryLocalService _accountEntryLocalService;
	private final Format _commerceDateTimeFormat;
	private CommerceOrder _commerceOrder;
	private CommerceOrderItem _commerceOrderItem;
	private final CommerceOrderItemLocalService _commerceOrderItemLocalService;
	private final CommerceOrderLocalService _commerceOrderLocalService;
	private final CommercePriceFormatter _commercePriceFormatter;
	private final CommerceReturn _commerceReturn;
	private CommerceReturnItem _commerceReturnItem;
	private long _commerceReturnItemId;
	private final CommerceReturnRequestHelper _commerceReturnRequestHelper;
	private final CPRequestHelper _cpRequestHelper;
	private final Language _language;
	private final ListTypeDefinitionService _listTypeDefinitionService;
	private final ListTypeEntryService _listTypeEntryService;
	private final ObjectEntryLocalService _objectEntryLocalService;

}