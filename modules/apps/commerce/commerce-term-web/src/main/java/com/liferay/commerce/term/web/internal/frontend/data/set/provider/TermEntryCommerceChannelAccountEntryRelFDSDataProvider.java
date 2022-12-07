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

package com.liferay.commerce.term.web.internal.frontend.data.set.provider;

import com.liferay.account.model.AccountEntry;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.model.CommerceChannelAccountEntryRel;
import com.liferay.commerce.product.service.CommerceChannelAccountEntryRelService;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.term.model.CommerceTermEntry;
import com.liferay.commerce.term.service.CommerceTermEntryLocalService;
import com.liferay.commerce.term.web.internal.entry.constants.CommerceTermEntryFDSNames;
import com.liferay.commerce.term.web.internal.model.TermEntry;
import com.liferay.frontend.data.set.provider.FDSDataProvider;
import com.liferay.frontend.data.set.provider.search.FDSKeywords;
import com.liferay.frontend.data.set.provider.search.FDSPagination;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.util.ParamUtil;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	property = {
		"fds.data.provider.key=" + CommerceTermEntryFDSNames.ACCOUNT_ENTRY_DELIVERY_TERM_ENTRIES,
		"fds.data.provider.key=" + CommerceTermEntryFDSNames.ACCOUNT_ENTRY_PAYMENT_TERM_ENTRIES
	},
	service = FDSDataProvider.class
)
public class TermEntryCommerceChannelAccountEntryRelFDSDataProvider
	implements FDSDataProvider<TermEntry> {

	@Override
	public List<TermEntry> getItems(
			FDSKeywords fdsKeywords, FDSPagination fdsPagination,
			HttpServletRequest httpServletRequest, Sort sort)
		throws PortalException {

		long accountEntryId = ParamUtil.getLong(
			httpServletRequest, "accountEntryId");

		if (_hasPermission(accountEntryId)) {
			int type = ParamUtil.getInteger(httpServletRequest, "type");

			return TransformUtil.transform(
				_commerceChannelAccountEntryRelService.
					getCommerceChannelAccountEntryRels(
						accountEntryId, type, fdsPagination.getStartPosition(),
						fdsPagination.getEndPosition(), null),
				commerceChannelAccountEntryRel -> {
					CommerceTermEntry commerceTermEntry =
						_commerceTermEntryLocalService.getCommerceTermEntry(
							commerceChannelAccountEntryRel.getClassPK());

					return new TermEntry(
						commerceChannelAccountEntryRel.getAccountEntryId(),
						commerceTermEntry.isActive(),
						_getChannelName(
							accountEntryId,
							commerceChannelAccountEntryRel.
								getCommerceChannelId(),
							httpServletRequest, type),
						commerceChannelAccountEntryRel.
							getCommerceChannelAccountEntryRelId(),
						commerceTermEntry.getLabel(
							_language.getLanguageId(httpServletRequest)),
						commerceChannelAccountEntryRel.isOverrideEligibility(),
						commerceChannelAccountEntryRel.getPriority(),
						commerceChannelAccountEntryRel.getType());
				});
		}

		return Collections.emptyList();
	}

	@Override
	public int getItemsCount(
			FDSKeywords fdsKeywords, HttpServletRequest httpServletRequest)
		throws PortalException {

		return _commerceChannelAccountEntryRelService.
			getCommerceChannelAccountEntryRelsCount(
				ParamUtil.getLong(httpServletRequest, "accountEntryId"),
				ParamUtil.getInteger(httpServletRequest, "type"));
	}

	private String _getChannelName(
			long accountEntryId, long commerceChannelId,
			HttpServletRequest httpServletRequest, int type)
		throws PortalException {

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.fetchCommerceChannel(
				commerceChannelId);

		if (commerceChannel == null) {
			List<CommerceChannelAccountEntryRel>
				commerceChannelAccountEntryRels =
					_commerceChannelAccountEntryRelService.
						getCommerceChannelAccountEntryRels(
							accountEntryId, type, QueryUtil.ALL_POS,
							QueryUtil.ALL_POS, null);

			if (commerceChannelAccountEntryRels.size() == 1) {
				CommerceChannelAccountEntryRel commerceChannelAccountEntryRel =
					commerceChannelAccountEntryRels.get(0);

				if (commerceChannelAccountEntryRel.getCommerceChannelId() ==
						0) {

					return _language.get(httpServletRequest, "all-channels");
				}
			}
			else if (!commerceChannelAccountEntryRels.isEmpty()) {
				return _language.get(httpServletRequest, "all-other-channels");
			}

			return _language.get(httpServletRequest, "all-channels");
		}

		return commerceChannel.getName();
	}

	private boolean _hasPermission(long accountEntryId) throws PortalException {
		return _accountEntryModelResourcePermission.contains(
			PermissionThreadLocal.getPermissionChecker(), accountEntryId,
			ActionKeys.UPDATE);
	}

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(model.class.name=com.liferay.account.model.AccountEntry)"
	)
	private volatile ModelResourcePermission<AccountEntry>
		_accountEntryModelResourcePermission;

	@Reference
	private CommerceChannelAccountEntryRelService
		_commerceChannelAccountEntryRelService;

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.commerce.product.model.CommerceChannel)"
	)
	private ModelResourcePermission<CommerceChannel>
		_commerceChannelModelResourcePermission;

	@Reference
	private CommerceTermEntryLocalService _commerceTermEntryLocalService;

	@Reference
	private Language _language;

}