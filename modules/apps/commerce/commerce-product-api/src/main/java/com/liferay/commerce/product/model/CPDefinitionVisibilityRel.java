/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.model;

import com.liferay.portal.kernel.annotation.ImplementationClassName;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.util.Accessor;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The extended model interface for the CPDefinitionVisibilityRel service. Represents a row in the &quot;CPDefinitionVisibilityRel&quot; database table, with each column mapped to a property of this class.
 *
 * @author Marco Leo
 * @see CPDefinitionVisibilityRelModel
 * @generated
 */
@ImplementationClassName(
	"com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelImpl"
)
@ProviderType
public interface CPDefinitionVisibilityRel
	extends CPDefinitionVisibilityRelModel, PersistedModel {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this interface directly. Add methods to <code>com.liferay.commerce.product.model.impl.CPDefinitionVisibilityRelImpl</code> and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */
	public static final Accessor<CPDefinitionVisibilityRel, Long>
		CP_DEFINITION_VISIBILITY_REL_ID_ACCESSOR =
			new Accessor<CPDefinitionVisibilityRel, Long>() {

				@Override
				public Long get(
					CPDefinitionVisibilityRel cpDefinitionVisibilityRel) {

					return cpDefinitionVisibilityRel.
						getCPDefinitionVisibilityRelId();
				}

				@Override
				public Class<Long> getAttributeClass() {
					return Long.class;
				}

				@Override
				public Class<CPDefinitionVisibilityRel> getTypeClass() {
					return CPDefinitionVisibilityRel.class;
				}

			};

}
// LIFERAY-SERVICE-BUILDER-HASH:413988821