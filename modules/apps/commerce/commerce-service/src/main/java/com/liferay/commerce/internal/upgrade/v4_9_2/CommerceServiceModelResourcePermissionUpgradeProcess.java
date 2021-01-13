/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.internal.upgrade.v4_9_2;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Alessio Antonio Rendina
 */
public class CommerceServiceModelResourcePermissionUpgradeProcess
	extends UpgradeProcess {

	@Override
	public void doUpgrade() throws Exception {
		runSQL(
			StringBundler.concat(
				"delete from ResourceAction where name = '90' and actionId in ",
				"('MANAGE_COMMERCE_AVAILABILITY_ESTIMATES', ",
				"'MANAGE_COMMERCE_COUNTRIES', ",
				"'MANAGE_COMMERCE_HEALTH_STATUS', ",
				"'MANAGE_COMMERCE_ORDER_PRICES', 'MANAGE_COMMERCE_SHIPMENTS', ",
				"'MANAGE_COMMERCE_SUBSCRIPTIONS')"));
	}

}