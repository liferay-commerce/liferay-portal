/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.internal.upgrade.v6_7_0;

import com.liferay.portal.kernel.upgrade.BaseExternalReferenceCodeUpgradeProcess;

/**
 * @author Michele Vigilante
 */
public class CommerceProductExternalReferenceCodeUpgradeProcess
	extends BaseExternalReferenceCodeUpgradeProcess {

	@Override
	protected String[] getTableNames() {
		return new String[] {
			"CPDefinitionOptionRel", "CPDefinitionOptionValueRel"
		};
	}

}