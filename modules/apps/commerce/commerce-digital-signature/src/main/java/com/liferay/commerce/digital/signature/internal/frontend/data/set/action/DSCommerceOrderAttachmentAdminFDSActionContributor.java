/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.frontend.data.set.action;

import com.liferay.commerce.constants.CommercePortletKeys;
import com.liferay.commerce.digital.signature.internal.util.DSCommerceOrderAttachmentUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.CommerceOrderAttachmentAdminFDSActionContributor;
import com.liferay.commerce.service.CommerceOrderAttachmentLocalService;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemBuilder;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemList;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.portlet.PortletURLFactory;
import com.liferay.portal.kernel.portlet.url.builder.ResourceURLBuilder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.PortletRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(service = CommerceOrderAttachmentAdminFDSActionContributor.class)
public class DSCommerceOrderAttachmentAdminFDSActionContributor
	implements CommerceOrderAttachmentAdminFDSActionContributor {

	@Override
	public Map<String, Object> getAdditionalProps(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		if (!DSCommerceOrderAttachmentUtil.isEnabled(commerceOrder)) {
			return Collections.emptyMap();
		}

		Map<Long, DSRequest> dsRequests =
			DSCommerceOrderAttachmentUtil.getDSRequests(
				commerceOrder, httpServletRequest);

		return HashMapBuilder.<String, Object>put(
			"signatureRequest",
			() -> {
				ThemeDisplay themeDisplay =
					(ThemeDisplay)httpServletRequest.getAttribute(
						WebKeys.THEME_DISPLAY);

				if (!_commerceOrderModelResourcePermission.contains(
						themeDisplay.getPermissionChecker(), commerceOrder,
						ActionKeys.UPDATE)) {

					return null;
				}

				return HashMapBuilder.<String, Object>put(
					"accountUsers",
					JSONUtil.toJSONArray(
						DSCommerceOrderAttachmentUtil.getAccountUsers(
							commerceOrder),
						DSCommerceOrderAttachmentUtil::getUserJSONObject)
				).put(
					"buyer",
					() -> {
						User user = _userLocalService.fetchUser(
							commerceOrder.getUserId());

						if ((user == null) || !user.isActive() ||
							(user.getType() != UserConstants.TYPE_REGULAR)) {

							return null;
						}

						return DSCommerceOrderAttachmentUtil.getUserJSONObject(
							user);
					}
				).put(
					"commerceOrderId", commerceOrder.getCommerceOrderId()
				).put(
					"nonrequestableIds",
					_getNonrequestableIds(commerceOrder, dsRequests)
				).put(
					"portletNamespace",
					_portal.getPortletNamespace(
						CommercePortletKeys.COMMERCE_ORDER)
				).put(
					"searchUsersURL",
					ResourceURLBuilder.createResourceURL(
						_portletURLFactory.create(
							httpServletRequest,
							CommercePortletKeys.COMMERCE_ORDER,
							PortletRequest.RESOURCE_PHASE)
					).setResourceID(
						"/commerce_order/search_ds_request_users"
					).buildString()
				).build();
			}
		).put(
			"signatureStatuses",
			DSCommerceOrderAttachmentUtil.getSignatureStatuses(dsRequests)
		).build();
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		if (!DSCommerceOrderAttachmentUtil.isEnabled(commerceOrder)) {
			return Collections.emptyList();
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return FDSActionDropdownItemList.of(
			FDSActionDropdownItemBuilder.putData(
				"addDSRequestURL",
				_getResourceURL(
					httpServletRequest, "/commerce_order/add_ds_request")
			).setHref(
				StringPool.POUND
			).setIcon(
				"signature"
			).setLabel(
				_language.get(httpServletRequest, "request-signature")
			).setPermissionKey(
				"update"
			).build(
				"request-signature"
			),
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
			),
			FDSActionDropdownItemBuilder.putData(
				"resendURL",
				_getResourceURL(
					httpServletRequest, "/commerce_order/resend_ds_request")
			).setHref(
				StringPool.POUND
			).setIcon(
				"envelope-closed"
			).setLabel(
				_language.get(httpServletRequest, "resend")
			).setPermissionKey(
				"update"
			).build(
				"resend-signature-request"
			),
			FDSActionDropdownItemBuilder.putData(
				"voidURL",
				_getResourceURL(
					httpServletRequest, "/commerce_order/void_ds_request")
			).setHref(
				StringPool.POUND
			).setIcon(
				"times-circle"
			).setLabel(
				_language.get(httpServletRequest, "void")
			).setPermissionKey(
				"update"
			).build(
				"void-signature-request"
			));
	}

	private List<String> _getNonrequestableIds(
			CommerceOrder commerceOrder, Map<Long, DSRequest> dsRequests)
		throws PortalException {

		return TransformUtil.transform(
			_commerceOrderAttachmentLocalService.getCommerceOrderAttachments(
				commerceOrder.getCommerceOrderId(), QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null),
			commerceOrderAttachment -> {
				DSRequest dsRequest = dsRequests.get(
					commerceOrderAttachment.getCommerceOrderAttachmentId());

				if (((dsRequest == null) || dsRequest.isRequestable()) &&
					DSCommerceOrderAttachmentUtil.hasAllowedFileExtension(
						commerceOrderAttachment)) {

					return null;
				}

				return String.valueOf(
					commerceOrderAttachment.getCommerceOrderAttachmentId());
			});
	}

	private String _getResourceURL(
		HttpServletRequest httpServletRequest, String resourceID) {

		return ResourceURLBuilder.createResourceURL(
			_portletURLFactory.create(
				httpServletRequest, CommercePortletKeys.COMMERCE_ORDER,
				PortletRequest.RESOURCE_PHASE)
		).setParameter(
			"commerceOrderAttachmentId", "{id}"
		).setResourceID(
			resourceID
		).buildString();
	}

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

	@Reference
	private Portal _portal;

	@Reference
	private PortletURLFactory _portletURLFactory;

	@Reference
	private UserLocalService _userLocalService;

}