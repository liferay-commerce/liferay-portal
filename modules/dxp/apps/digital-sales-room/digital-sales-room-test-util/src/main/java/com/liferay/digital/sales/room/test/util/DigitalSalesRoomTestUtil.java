/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.sales.room.test.util;

import com.liferay.batch.engine.test.util.BatchEngineTestUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalServiceUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;

/**
 * @author Stefano Motta
 */
public class DigitalSalesRoomTestUtil {

	public static ObjectDefinition getObjectDefinition() throws Exception {
		ObjectDefinition objectDefinition =
			ObjectDefinitionLocalServiceUtil.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DSR_ROOM", TestPropsValues.getCompanyId());

		if (objectDefinition != null) {
			return objectDefinition;
		}

		BatchEngineTestUtil.processBatchEngineUnits(
			"com.liferay.digital.sales.room.impl");

		return ObjectDefinitionLocalServiceUtil.
			getObjectDefinitionByExternalReferenceCode(
				"L_DSR_ROOM", TestPropsValues.getCompanyId());
	}

}