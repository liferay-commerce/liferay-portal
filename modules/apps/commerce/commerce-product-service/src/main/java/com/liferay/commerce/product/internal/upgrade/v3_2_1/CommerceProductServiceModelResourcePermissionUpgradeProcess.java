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

package com.liferay.commerce.product.internal.upgrade.v3_2_1;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Alessio Antonio Rendina
 */
public class CommerceProductServiceModelResourcePermissionUpgradeProcess
	extends UpgradeProcess {

	@Override
	public void doUpgrade() throws Exception {
		runSQL(
			StringBundler.concat(
				"delete from ResourceAction where name = '90' and actionId in ",
				"('ADD_COMMERCE_CATALOG', 'ADD_COMMERCE_CHANNEL', ",
				"'ADD_COMMERCE_PRODUCT_OPTION', ",
				"'ADD_COMMERCE_PRODUCT_OPTION_CATEGORY', ",
				"'ADD_COMMERCE_PRODUCT_SPECIFICATION_OPTION', ",
				"'MANAGE_COMMERCE_PRODUCT_MEASUREMENT_UNITS', ",
				"'MANAGE_COMMERCE_PRODUCT_TAX_CATEGORIES', ",
				"'VIEW_COMMERCE_CATALOGS', 'VIEW_COMMERCE_CHANNELS')"));
	}

}