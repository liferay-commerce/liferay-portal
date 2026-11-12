/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.constants;

/**
 * @author Balazs Breier
 */

// PR-NOTE: The values of the "type" field of a PIM connector field mapping.
// Renamed from PIMConnectorFieldMappingConstants (review), so the TYPE_ prefix
// was dropped. FIXED_VALUE used to live in the internal
// PIMConnectorFieldMappingsUtil, but the API class PIMConnectorChannelField
// callers need these values for allowedFieldMappingTypes, and an API class
// cannot depend on an internal one. DYNAMIC_VALUE was a "dynamicValue" string
// literal before. CATEGORIES and TAGS are the two new types. The frontend
// mirrors these values in edit_field_mappings/types.ts.

public class PIMConnectorFieldMappingTypeConstants {

	public static final String CATEGORIES = "categories";

	public static final String DYNAMIC_VALUE = "dynamicValue";

	public static final String FIXED_VALUE = "fixedValue";

	public static final String TAGS = "tags";

}