/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.variant;

import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.object.model.ObjectField;

import java.util.List;

/**
 * @author Balazs Breier
 */
public class PIMVariantOption {

	public PIMVariantOption(
		List<ListTypeEntry> listTypeEntries, ObjectField objectField,
		String value) {

		_listTypeEntries = listTypeEntries;
		_objectField = objectField;
		_value = value;
	}

	public List<ListTypeEntry> getListTypeEntries() {
		return _listTypeEntries;
	}

	public ObjectField getObjectField() {
		return _objectField;
	}

	public String getValue() {
		return _value;
	}

	private final List<ListTypeEntry> _listTypeEntries;
	private final ObjectField _objectField;
	private final String _value;

}