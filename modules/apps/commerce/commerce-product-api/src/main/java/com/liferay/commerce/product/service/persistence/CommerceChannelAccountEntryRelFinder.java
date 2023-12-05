/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.persistence;

import org.osgi.annotation.versioning.ProviderType;

/**
 * @author Marco Leo
 * @generated
 */
@ProviderType
public interface CommerceChannelAccountEntryRelFinder {

	public int countByC_T(long commerceChannelId, String name, int type);

	public int countByC_T(
		long commerceChannelId, String name, int type, boolean inlineSQLHelper);

	public java.util.List
		<com.liferay.commerce.product.model.CommerceChannelAccountEntryRel>
			findByC_T(
				long commerceChannelId, String name, int type, int start,
				int end);

	public java.util.List
		<com.liferay.commerce.product.model.CommerceChannelAccountEntryRel>
			findByC_T(
				long commerceChannelId, String name, int type, int start,
				int end, boolean inlineSQLHelper);

}