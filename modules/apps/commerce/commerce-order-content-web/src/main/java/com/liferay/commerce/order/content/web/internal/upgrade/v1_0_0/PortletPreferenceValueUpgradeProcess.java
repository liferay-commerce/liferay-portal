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

package com.liferay.commerce.order.content.web.internal.upgrade.v1_0_0;

import com.liferay.commerce.constants.CommercePortletKeys;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.PortletPreferenceValue;
import com.liferay.portal.kernel.model.PortletPreferences;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.PortletPreferencesLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.PortletKeys;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Danny Situ
 */
public class PortletPreferenceValueUpgradeProcess extends UpgradeProcess {

	public PortletPreferenceValueUpgradeProcess(
		LayoutLocalService layoutLocalService,
		PortletPreferencesLocalService portletPreferencesLocalService) {

		_layoutLocalService = layoutLocalService;
		_portletPreferencesLocalService = portletPreferencesLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		if (!hasTable("PortletPreferenceValue")) {
			return;
		}

		_upgradePortletPreferences(CommercePortletKeys.COMMERCE_ORDER_CONTENT);
		_upgradePortletPreferences(
			CommercePortletKeys.COMMERCE_OPEN_ORDER_CONTENT);
	}

	private void _upgradePortletPreferences(String commercePortletKey)
		throws Exception {

		List<Long> plids = new ArrayList<>();

		try (Statement s = connection.createStatement(
				ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultSet = s.executeQuery(
				StringBundler.concat(
					"select * from Layout where typeSettings like '%",
					commercePortletKey, "%'"))) {

			while (resultSet.next()) {
				long plid = resultSet.getLong("plid");

				plids.add(plid);
			}
		}

		List<Long> portletPreferencesIds = new ArrayList<>();

		for (Long plid : plids) {
			try (Statement s = connection.createStatement(
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = s.executeQuery(
					StringBundler.concat(
						"select * from PortletPreferences where plid = ", plid,
						" and portletId like '%", commercePortletKey, "%'"))) {

				if (resultSet.next() == false) {
					long portletPreferencesId = increment(
						PortletPreferences.class.getName());

					try (PreparedStatement preparedStatement =
							connection.prepareStatement(
								StringBundler.concat(
									"insert into PortletPreferences ",
									"(mvccVersion, ctCollectionId, ",
									"portletPreferencesId, companyId, ",
									"ownerId, ownerType, plid, portletId) ",
									"values (0, 0, ?, ?, ?, ?, ?, ?)"))) {

						Layout layout = _layoutLocalService.getLayout(plid);

						preparedStatement.setLong(1, portletPreferencesId);
						preparedStatement.setLong(2, layout.getCompanyId());
						preparedStatement.setLong(3, 0);
						preparedStatement.setInt(
							4, PortletKeys.PREFS_OWNER_TYPE_LAYOUT);
						preparedStatement.setLong(5, plid);
						preparedStatement.setString(6, commercePortletKey);

						preparedStatement.executeUpdate();
					}

					portletPreferencesIds.add(portletPreferencesId);
				}
				else {
					do {
						long portletPreferencesId = resultSet.getLong(
							"portletPreferencesId");

						portletPreferencesIds.add(portletPreferencesId);
					}
					while (resultSet.next());
				}
			}
		}

		for (Long portletPreferencesId : portletPreferencesIds) {
			try (Statement s = connection.createStatement(
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = s.executeQuery(
					StringBundler.concat(
						"select * from PortletPreferenceValue where ",
						"portletPreferencesId = ", portletPreferencesId,
						" and name = 'enableUpdatedOrderDetailsPage'"))) {

				if (resultSet.next() == false) {
					try (PreparedStatement preparedStatement =
							connection.prepareStatement(
								StringBundler.concat(
									"insert into PortletPreferenceValue ",
									"(mvccVersion, ctCollectionId, ",
									"portletPreferenceValueId, companyId, ",
									"portletPreferencesId, index_, ",
									"largeValue, name, readOnly, smallValue) ",
									"values (0, 0, ?, ?, ?, ?, ?, ?, ?, ?)"))) {

						PortletPreferences portletPreferences =
							_portletPreferencesLocalService.
								getPortletPreferences(portletPreferencesId);

						preparedStatement.setLong(
							1,
							increment(PortletPreferenceValue.class.getName()));
						preparedStatement.setLong(
							2, portletPreferences.getCompanyId());
						preparedStatement.setLong(3, portletPreferencesId);
						preparedStatement.setInt(4, 0);
						preparedStatement.setString(5, StringPool.BLANK);
						preparedStatement.setString(
							6, "enableUpdatedOrderDetailsPage");
						preparedStatement.setBoolean(7, false);
						preparedStatement.setString(8, "false");

						preparedStatement.executeUpdate();
					}
				}
			}
		}
	}

	private final LayoutLocalService _layoutLocalService;
	private final PortletPreferencesLocalService
		_portletPreferencesLocalService;

}