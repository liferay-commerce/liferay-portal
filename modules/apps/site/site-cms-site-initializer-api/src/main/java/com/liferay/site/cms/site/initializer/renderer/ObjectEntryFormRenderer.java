/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.renderer;

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.info.field.InfoFieldSet;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.ServiceContext;

import java.util.List;

/**
 * @author Stefano Motta
 */
public interface ObjectEntryFormRenderer {

	public LayoutStructure render(
			List<FragmentEntryLink> fragmentEntryLinks,
			InfoFieldSet infoFieldSet, Layout layout,
			LayoutStructure layoutStructure,
			LayoutStructureItem layoutStructureItem,
			ObjectDefinition objectDefinition, long segmentsExperienceId,
			ServiceContext serviceContext)
		throws Exception;

}