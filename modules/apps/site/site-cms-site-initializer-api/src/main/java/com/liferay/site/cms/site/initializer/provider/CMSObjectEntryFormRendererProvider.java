/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.provider;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.site.cms.site.initializer.renderer.ObjectEntryFormRenderer;

/**
 * @author Stefano Motta
 */
public interface CMSObjectEntryFormRendererProvider {

	public ObjectEntryFormRenderer getObjectEntryFormRenderer(
		ObjectDefinition objectDefinition);

}