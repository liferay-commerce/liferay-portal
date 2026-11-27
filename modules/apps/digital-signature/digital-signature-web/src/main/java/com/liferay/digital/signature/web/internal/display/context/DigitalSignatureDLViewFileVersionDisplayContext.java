/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.web.internal.display.context;

import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.document.library.display.context.BaseDLViewFileVersionDisplayContext;
import com.liferay.document.library.display.context.DLUIItemKeys;
import com.liferay.document.library.display.context.DLViewFileVersionDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownGroupItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemBuilder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Danny Situ
 */
public class DigitalSignatureDLViewFileVersionDisplayContext
	extends BaseDLViewFileVersionDisplayContext {

	public DigitalSignatureDLViewFileVersionDisplayContext(
		DSRequestManager dsRequestManager, FileVersion fileVersion,
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse,
		DLViewFileVersionDisplayContext parentDLViewFileVersionDisplayContext) {

		super(
			_UUID, parentDLViewFileVersionDisplayContext, httpServletRequest,
			httpServletResponse, fileVersion);

		_dsRequestManager = dsRequestManager;
		_httpServletRequest = httpServletRequest;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() throws PortalException {
		List<DropdownItem> dropdownItems = super.getActionDropdownItems();

		if (dropdownItems == null) {
			return null;
		}

		DSRequest dsRequest = _dsRequestManager.fetchLatestFileEntryDSRequest(
			_themeDisplay.getCompanyId(), fileVersion.getFileEntryId());

		if ((dsRequest == null) ||
			!dsRequest.isSignatureRequired(_themeDisplay.getUserId())) {

			return dropdownItems;
		}

		DropdownItem dropdownItem = DropdownItemBuilder.setHref(
			HttpComponentsUtil.setParameter(
				_themeDisplay.getURLCurrent(), "dsRequestId",
				dsRequest.getDSRequestId())
		).setIcon(
			"signature"
		).setKey(
			"sign"
		).setLabel(
			LanguageUtil.get(_httpServletRequest, "sign")
		).build();

		if (!_addDropdownItem(
				dropdownItem, dropdownItems,
				DLUIItemKeys.COLLECT_DIGITAL_SIGNATURE, 1) &&
			!_addDropdownItem(
				dropdownItem, dropdownItems, DLUIItemKeys.VIEW_HISTORY, 0)) {

			dropdownItems.add(dropdownItem);
		}

		return dropdownItems;
	}

	private boolean _addDropdownItem(
		DropdownItem dropdownItem, List<DropdownItem> dropdownItems, String key,
		int offset) {

		for (int i = 0; i < dropdownItems.size(); i++) {
			DropdownItem curDropdownItem = dropdownItems.get(i);

			if (curDropdownItem instanceof DropdownGroupItem) {
				List<DropdownItem> groupDropdownItems =
					(List<DropdownItem>)curDropdownItem.get("items");

				if (ListUtil.isNotEmpty(groupDropdownItems) &&
					_addDropdownItem(
						dropdownItem, groupDropdownItems, key, offset)) {

					return true;
				}
			}
			else if (Objects.equals(key, curDropdownItem.get("key"))) {
				dropdownItems.add(i + offset, dropdownItem);

				return true;
			}
		}

		return false;
	}

	private static final UUID _UUID = UUID.fromString(
		"d69b1c19-234b-43ce-a8c4-e701a6264f9e");

	private final DSRequestManager _dsRequestManager;
	private final HttpServletRequest _httpServletRequest;
	private final ThemeDisplay _themeDisplay;

}