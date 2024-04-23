/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.defaultpermissions.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.portal.defaultpermissions.web.internal.search.PortalDefaultPermissionsSearch;
import com.liferay.portal.defaultpermissions.web.internal.search.PortalDefaultPermissionsSearchEntry;
import com.liferay.portal.defaultpermissions.web.internal.search.PortalDefaultPermissionsSearchEntryClassNamePredicate;
import com.liferay.portal.defaultpermissions.web.internal.search.PortalDefaultPermissionsSearchEntryLabelPredicate;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.List;
import java.util.function.Predicate;

import javax.portlet.PortletURL;

/**
 * @author Stefano Motta
 */
public interface PortalDefaultPermissionsConfigurationDisplayContext {

	public default Predicate<PortalDefaultPermissionsSearchEntry>
		createPredicate(String className, String label) {

		Predicate<PortalDefaultPermissionsSearchEntry> predicate =
			new PortalDefaultPermissionsSearchEntryClassNamePredicate(
				className);

		return predicate.or(
			new PortalDefaultPermissionsSearchEntryLabelPredicate(label));
	}

	public default List<PortalDefaultPermissionsSearchEntry> filter(
		List<PortalDefaultPermissionsSearchEntry>
			portalDefaultPermissionSearchEntries,
		String className, String label) {

		if (Validator.isNull(className) && Validator.isNull(label)) {
			return portalDefaultPermissionSearchEntries;
		}

		Predicate<PortalDefaultPermissionsSearchEntry> predicate =
			createPredicate(className, label);

		return ListUtil.filter(
			portalDefaultPermissionSearchEntries, predicate::test);
	}

	public List<DropdownItem> getActionDropdownItems(
		PortalDefaultPermissionsSearchEntry
			portalDefaultPermissionsSearchEntry);

	public String getEditURL(String className);

	public PortletURL getPortletURL();

	public PortalDefaultPermissionsSearch getSearchContainer();

}