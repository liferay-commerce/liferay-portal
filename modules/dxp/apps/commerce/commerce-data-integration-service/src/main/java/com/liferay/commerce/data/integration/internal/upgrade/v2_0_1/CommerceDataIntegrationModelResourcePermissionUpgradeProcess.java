/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.commerce.data.integration.internal.upgrade.v2_0_1;

import com.liferay.portal.kernel.upgrade.UpgradeProcess;

/**
 * @author Alessio Antonio Rendina
 */
public class CommerceDataIntegrationModelResourcePermissionUpgradeProcess
	extends UpgradeProcess {

	@Override
	public void doUpgrade() throws Exception {
		runSQL(
			"delete from ResourceAction where name = '90' and actionId = " +
				"'ADD_COMMERCE_DATA_INTEGRATION_PROCESS'");
	}

}