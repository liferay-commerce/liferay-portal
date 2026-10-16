/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.frontend.data.set.action;

import com.liferay.commerce.constants.CommerceOrderActionKeys;
import com.liferay.commerce.digital.signature.internal.util.DSCommerceOrderAttachmentUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.order.CommerceOrderAttachmentFDSActionContributor;
import com.liferay.commerce.service.CommerceOrderAttachmentLocalService;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemBuilder;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(service = CommerceOrderAttachmentFDSActionContributor.class)
public class DSCommerceOrderAttachmentFDSActionContributor
	implements CommerceOrderAttachmentFDSActionContributor {

	@Override
	public Map<String, Object> getAdditionalProps(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		Map<Long, DSRequest> dsRequests = _getDSRequests(
			commerceOrder, httpServletRequest);

		if (dsRequests.isEmpty()) {
			return Collections.emptyMap();
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return HashMapBuilder.<String, Object>put(
			"signableIds",
			TransformUtil.transform(
				dsRequests.entrySet(),
				entry -> {
					DSRequest dsRequest = entry.getValue();

					if (!dsRequest.isSignatureRequired(
							themeDisplay.getUserId())) {

						return null;
					}

					return String.valueOf(entry.getKey());
				})
		).put(
			"signatureStatuses",
			DSCommerceOrderAttachmentUtil.getSignatureStatuses(dsRequests)
		).build();
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		Map<Long, DSRequest> dsRequests = _getDSRequests(
			commerceOrder, httpServletRequest);

		if (dsRequests.isEmpty()) {
			return Collections.emptyList();
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		boolean signatureRequired = false;

		for (DSRequest dsRequest : dsRequests.values()) {
			if (dsRequest.isSignatureRequired(themeDisplay.getUserId())) {
				signatureRequired = true;

				break;
			}
		}

		List<FDSActionDropdownItem> fdsActionDropdownItems = new ArrayList<>();

		if (signatureRequired) {
			fdsActionDropdownItems.add(
				FDSActionDropdownItemBuilder.putData(
					"signURL",
					DSCommerceOrderAttachmentUtil.getActionURL(
						"/commerce-digital-signature/sign", themeDisplay)
				).setHref(
					StringPool.POUND
				).setIcon(
					"pencil"
				).setLabel(
					_language.get(httpServletRequest, "sign")
				).build(
					"sign"
				));
		}

		fdsActionDropdownItems.add(
			FDSActionDropdownItemBuilder.putData(
				"signatureStatusURL",
				DSCommerceOrderAttachmentUtil.getActionURL(
					"/commerce-digital-signature/signature-status",
					themeDisplay)
			).setHref(
				StringPool.POUND
			).setIcon(
				"list-ul"
			).setLabel(
				_language.get(httpServletRequest, "view-signature-status")
			).build(
				"view-signature-status"
			));

		return fdsActionDropdownItems;
	}

	private Map<Long, DSRequest> _getDSRequests(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		Map<Long, DSRequest> dsRequests =
			DSCommerceOrderAttachmentUtil.getDSRequests(
				commerceOrder, httpServletRequest);

		if (dsRequests.isEmpty()) {
			return dsRequests;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		try {
			if (_commerceOrderModelResourcePermission.contains(
					themeDisplay.getPermissionChecker(), commerceOrder,
					CommerceOrderActionKeys.
						VIEW_RESTRICTED_COMMERCE_ORDER_ATTACHMENTS)) {

				return dsRequests;
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException);

			return Collections.emptyMap();
		}

		Map<Long, DSRequest> unrestrictedDSRequests = new LinkedHashMap<>(
			dsRequests);

		for (CommerceOrderAttachment commerceOrderAttachment :
				_commerceOrderAttachmentLocalService.
					getCommerceOrderAttachments(
						commerceOrder.getCommerceOrderId(), true,
						QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			unrestrictedDSRequests.remove(
				commerceOrderAttachment.getCommerceOrderAttachmentId());
		}

		return unrestrictedDSRequests;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DSCommerceOrderAttachmentFDSActionContributor.class);

	@Reference
	private CommerceOrderAttachmentLocalService
		_commerceOrderAttachmentLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.commerce.model.CommerceOrder)"
	)
	private ModelResourcePermission<CommerceOrder>
		_commerceOrderModelResourcePermission;

	@Reference
	private Language _language;

}