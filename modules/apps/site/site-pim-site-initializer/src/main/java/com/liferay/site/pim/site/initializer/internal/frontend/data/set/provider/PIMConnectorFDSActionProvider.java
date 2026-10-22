/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.frontend.data.set.provider;

import com.liferay.frontend.data.set.provider.FDSActionProvider;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.site.pim.site.initializer.internal.constants.PIMFDSNames;
import com.liferay.site.pim.site.initializer.internal.frontend.data.set.model.PIMConnectorDisplay;
import com.liferay.site.pim.site.initializer.internal.util.PIMURLUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tancredi Covioli
 */
@Component(
	property = "fds.data.provider.key=" + PIMFDSNames.CONNECTORS,
	service = FDSActionProvider.class
)
public class PIMConnectorFDSActionProvider implements FDSActionProvider {

	@Override
	public List<DropdownItem> getDropdownItems(
		long groupId, HttpServletRequest httpServletRequest, Object model) {

		PIMConnectorDisplay pimConnectorDisplay = (PIMConnectorDisplay)model;

		Map<String, Map<String, String>> actions =
			pimConnectorDisplay.getActions();
		String objectEntryId = String.valueOf(pimConnectorDisplay.getId());

		return DropdownItemListBuilder.add(
			() -> actions.containsKey("update"),
			dropdownItem -> {
				dropdownItem.putData("id", "fieldMappings");
				dropdownItem.setHref(
					_getURL(
						httpServletRequest, objectEntryId, "fieldMappingsURL"));
				dropdownItem.setIcon("sheets");
				dropdownItem.setLabel(
					_language.get(httpServletRequest, "map-fields"));
			}
		).add(
			() -> actions.containsKey("update"),
			dropdownItem -> {
				dropdownItem.putData("id", "edit");
				dropdownItem.setHref(
					_getURL(
						httpServletRequest, objectEntryId, "editConnectorURL"));
				dropdownItem.setIcon("pencil");
				dropdownItem.setLabel(
					_language.get(httpServletRequest, "edit"));
			}
		).add(
			() ->
				pimConnectorDisplay.isActive() &&
				pimConnectorDisplay.isMappingComplete(),
			dropdownItem -> {
				dropdownItem.putData("id", "execute");
				dropdownItem.putData("method", "post");
				dropdownItem.putData(
					"successMessage",
					_language.get(
						httpServletRequest,
						"execution-has-started-successfully-and-will-" +
							"continue-in-the-background"));
				dropdownItem.setHref(
					PIMURLUtil.getConnectorExecuteURL(objectEntryId));
				dropdownItem.setIcon("play");
				dropdownItem.setLabel(
					_language.get(httpServletRequest, "execute"));
				dropdownItem.setTarget("async");
			}
		).add(
			dropdownItem -> {
				dropdownItem.putData("id", "schedule");
				dropdownItem.setHref(
					_getURL(httpServletRequest, objectEntryId, "scheduleURL"));
				dropdownItem.setIcon("date-time");
				dropdownItem.setLabel(
					_language.get(httpServletRequest, "schedule"));
			}
		).add(
			() -> actions.containsKey("delete"),
			dropdownItem -> {
				dropdownItem.put("className", "text-danger");
				dropdownItem.putData("id", "delete");
				dropdownItem.setIcon("trash");
				dropdownItem.setLabel(
					_language.get(httpServletRequest, "delete"));
			}
		).build();
	}

	private String _getURL(
		HttpServletRequest httpServletRequest, String objectEntryId,
		String param) {

		return StringUtil.replace(
			ParamUtil.getString(httpServletRequest, param), "{id}",
			objectEntryId);
	}

	@Reference
	private Language _language;

}