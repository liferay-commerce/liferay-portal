/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.engine;

import com.liferay.object.model.ObjectEntry;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.site.pim.site.initializer.variant.PIMVariantOption;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Balazs Breier
 */
@ProviderType
public interface PIMVariantEngine {

	public List<PIMVariantOption> getPIMVariantOptions(ObjectEntry objectEntry)
		throws PortalException;

}