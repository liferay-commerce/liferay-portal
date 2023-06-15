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

package com.liferay.commerce.service.impl;

import com.liferay.commerce.constants.CommerceOrderActionKeys;
import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.context.CommerceGroupThreadLocal;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderNote;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.commerce.service.base.CommerceOrderNoteServiceBaseImpl;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.service.ServiceContext;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Di Giorgi
 * @author Alessio Antonio Rendina
 */
@Component(
	property = {
		"json.web.service.context.name=commerce",
		"json.web.service.context.path=CommerceOrderNote"
	},
	service = AopService.class
)
public class CommerceOrderNoteServiceImpl
	extends CommerceOrderNoteServiceBaseImpl {

	@Override
	public CommerceOrderNote addCommerceOrderNote(
			long commerceOrderId, String content, boolean restricted,
			ServiceContext serviceContext)
		throws PortalException {

		_checkPortletResourcePermission(
			commerceOrderId,
			CommerceOrderActionKeys.MANAGE_COMMERCE_ORDER_NOTES, restricted);

		return commerceOrderNoteLocalService.addCommerceOrderNote(
			commerceOrderId, content, restricted, serviceContext);
	}

	@Override
	public CommerceOrderNote addOrUpdateCommerceOrderNote(
			String externalReferenceCode, long commerceOrderNoteId,
			long commerceOrderId, String content, boolean restricted,
			ServiceContext serviceContext)
		throws PortalException {

		_checkPortletResourcePermission(
			commerceOrderId,
			CommerceOrderActionKeys.MANAGE_COMMERCE_ORDER_NOTES, restricted);

		return commerceOrderNoteLocalService.addOrUpdateCommerceOrderNote(
			externalReferenceCode, commerceOrderNoteId, commerceOrderId,
			content, restricted, serviceContext);
	}

	@Override
	public void deleteCommerceOrderNote(long commerceOrderNoteId)
		throws PortalException {

		CommerceOrderNote commerceOrderNote =
			commerceOrderNoteLocalService.getCommerceOrderNote(
				commerceOrderNoteId);

		_checkPortletResourcePermission(
			commerceOrderNote.getCommerceOrderId(),
			CommerceOrderActionKeys.MANAGE_COMMERCE_ORDER_NOTES,
			commerceOrderNote.isRestricted());

		commerceOrderNoteLocalService.deleteCommerceOrderNote(
			commerceOrderNote);
	}

	@Override
	public CommerceOrderNote fetchByExternalReferenceCode(
			String externalReferenceCode, long companyId)
		throws PortalException {

		CommerceOrderNote commerceOrderNote =
			commerceOrderNoteLocalService.fetchByExternalReferenceCode(
				externalReferenceCode, companyId);

		if (commerceOrderNote != null) {
			_checkPortletResourcePermission(
				commerceOrderNote.getCommerceOrderId(),
				CommerceOrderActionKeys.MANAGE_COMMERCE_ORDER_NOTES,
				commerceOrderNote.isRestricted());
		}

		return commerceOrderNote;
	}

	@Override
	public CommerceOrderNote fetchCommerceOrderNote(long commerceOrderNoteId)
		throws PortalException {

		CommerceOrderNote commerceOrderNote =
			commerceOrderNoteLocalService.fetchCommerceOrderNote(
				commerceOrderNoteId);

		if (commerceOrderNote != null) {
			_checkCommerceOrderNoteViewPermission(
				commerceOrderNote.getCommerceOrderId(), ActionKeys.VIEW,
				commerceOrderNote.isRestricted());
		}

		return commerceOrderNote;
	}

	@Override
	public CommerceOrderNote getCommerceOrderNote(long commerceOrderNoteId)
		throws PortalException {

		CommerceOrderNote commerceOrderNote =
			commerceOrderNoteLocalService.getCommerceOrderNote(
				commerceOrderNoteId);

		if (commerceOrderNote != null) {
			_checkCommerceOrderNoteViewPermission(
				commerceOrderNote.getCommerceOrderId(), ActionKeys.VIEW,
				commerceOrderNote.isRestricted());
		}

		return commerceOrderNote;
	}

	@Override
	public List<CommerceOrderNote> getCommerceOrderNotes(
			long commerceOrderId, boolean restricted)
		throws PortalException {

		_checkCommerceOrderNoteViewPermission(
			commerceOrderId, ActionKeys.VIEW, restricted);

		return commerceOrderNoteLocalService.getCommerceOrderNotes(
			commerceOrderId, restricted);
	}

	@Override
	public List<CommerceOrderNote> getCommerceOrderNotes(
			long commerceOrderId, boolean restricted, int start, int end)
		throws PortalException {

		_checkCommerceOrderNoteViewPermission(
			commerceOrderId, ActionKeys.VIEW, restricted);

		return commerceOrderNoteLocalService.getCommerceOrderNotes(
			commerceOrderId, restricted, start, end);
	}

	@Override
	public List<CommerceOrderNote> getCommerceOrderNotes(
			long commerceOrderId, int start, int end)
		throws PortalException {

		_checkPortletResourcePermission(
			commerceOrderId,
			CommerceOrderActionKeys.MANAGE_COMMERCE_ORDER_NOTES, true);

		return commerceOrderNoteLocalService.getCommerceOrderNotes(
			commerceOrderId, start, end);
	}

	@Override
	public int getCommerceOrderNotesCount(long commerceOrderId)
		throws PortalException {

		_checkPortletResourcePermission(
			commerceOrderId,
			CommerceOrderActionKeys.MANAGE_COMMERCE_ORDER_NOTES, true);

		return commerceOrderNoteLocalService.getCommerceOrderNotesCount(
			commerceOrderId);
	}

	@Override
	public int getCommerceOrderNotesCount(
			long commerceOrderId, boolean restricted)
		throws PortalException {

		_checkCommerceOrderNoteViewPermission(
			commerceOrderId, ActionKeys.VIEW, restricted);

		return commerceOrderNoteLocalService.getCommerceOrderNotesCount(
			commerceOrderId, restricted);
	}

	@Override
	public CommerceOrderNote updateCommerceOrderNote(
			long commerceOrderNoteId, String content, boolean restricted)
		throws PortalException {

		CommerceOrderNote commerceOrderNote =
			commerceOrderNoteLocalService.getCommerceOrderNote(
				commerceOrderNoteId);

		_checkPortletResourcePermission(
			commerceOrderNote.getCommerceOrderId(),
			CommerceOrderActionKeys.MANAGE_COMMERCE_ORDER_NOTES, restricted);

		return commerceOrderNoteLocalService.updateCommerceOrderNote(
			commerceOrderNote.getCommerceOrderNoteId(), content, restricted);
	}

	private void _checkCommerceOrderNoteViewPermission(
			long commerceOrderId, String actionId, boolean restricted)
		throws PortalException {

		CommerceOrder commerceOrder = _commerceOrderService.fetchCommerceOrder(
			commerceOrderId);

		if (commerceOrder != null) {
			CommerceGroupThreadLocal.setWithSafeCloseable(
				commerceOrder.getGroupId());
		}

		if (restricted) {
			actionId =
				CommerceOrderActionKeys.MANAGE_COMMERCE_ORDER_RESTRICTED_NOTES;

			_portletResourcePermission.check(
				getPermissionChecker(), commerceOrderId, actionId);
		}
		else {
			_commerceOrderModelResourcePermission.check(
				getPermissionChecker(), commerceOrderId, actionId);
		}
	}

	private void _checkPortletResourcePermission(
			long commerceOrderId, String actionId, boolean restricted)
		throws PortalException {

		CommerceOrder commerceOrder = _commerceOrderService.fetchCommerceOrder(
			commerceOrderId);

		if (commerceOrder != null) {
			CommerceGroupThreadLocal.setWithSafeCloseable(
				commerceOrder.getGroupId());
		}

		if (restricted) {
			actionId =
				CommerceOrderActionKeys.MANAGE_COMMERCE_ORDER_RESTRICTED_NOTES;
		}

		_portletResourcePermission.check(
			getPermissionChecker(), commerceOrderId, actionId);
	}

	@Reference(
		target = "(model.class.name=com.liferay.commerce.model.CommerceOrder)"
	)
	private ModelResourcePermission<CommerceOrder>
		_commerceOrderModelResourcePermission;

	@Reference
	private CommerceOrderService _commerceOrderService;

	@Reference(
		target = "(resource.name=" + CommerceOrderConstants.RESOURCE_NAME + ")"
	)
	private PortletResourcePermission _portletResourcePermission;

}