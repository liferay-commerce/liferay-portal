/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.provider;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectFolder;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.site.cms.site.initializer.contributor.CMSStructureObjectFolderContributor;
import com.liferay.site.cms.site.initializer.provider.CMSObjectEntryFormRendererProvider;
import com.liferay.site.cms.site.initializer.renderer.ObjectEntryFormRenderer;

import java.util.List;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Stefano Motta
 */
@Component(service = CMSObjectEntryFormRendererProvider.class)
public class CMSObjectEntryFormRendererProviderImpl
	implements CMSObjectEntryFormRendererProvider {

	@Override
	public ObjectEntryFormRenderer getObjectEntryFormRenderer(
		ObjectDefinition objectDefinition) {

		ObjectFolder objectFolder = _objectFolderLocalService.fetchObjectFolder(
			objectDefinition.getObjectFolderId());

		if (objectFolder == null) {
			return null;
		}

		for (CMSStructureObjectFolderContributor
				cmsStructureObjectFolderContributor :
					_cmsStructureObjectFolderContributors) {

			if (Objects.equals(
					objectFolder.getExternalReferenceCode(),
					cmsStructureObjectFolderContributor.
						getObjectFolderExternalReferenceCode())) {

				return cmsStructureObjectFolderContributor.
					getObjectEntryFormRenderer();
			}
		}

		return null;
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private volatile List<CMSStructureObjectFolderContributor>
		_cmsStructureObjectFolderContributors;

	@Reference
	private ObjectFolderLocalService _objectFolderLocalService;

}