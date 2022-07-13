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

package com.liferay.commerce.product.internal.upgrade.v3_9_2;

import com.liferay.commerce.product.internal.upgrade.base.BaseCommerceProductServiceUpgradeProcess;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Crescenzo Rega
 */
public class MiniumSiteInitializerUpgradeProcess
	extends BaseCommerceProductServiceUpgradeProcess {

	@Override
	public void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement1 = connection.prepareStatement(
			"select siteGroupId from CommerceChannel where siteGroupId " +
			"in (select groupId from LayoutSet where privateLayout = 1 and " +
			"themeId = 'minium_WAR_miniumtheme')");

			 PreparedStatement preparedStatement2 = connection.prepareStatement(
				 "select MAX(layoutId) as maxLayoutId from Layout where " +
				 "groupId = ?");

			 PreparedStatement preparedStatement3 = connection.prepareStatement(
				 "select plid from Layout where groupId = ? and " +
				 "layoutId in (select layoutId from Layout group by layoutId " +
				 "having COUNT(layoutId) > 1) and privateLayout = 0");

			 PreparedStatement preparedStatement4 =
				 AutoBatchPreparedStatementUtil.autoBatch(
					 connection,
					 "update Layout set layoutId = ?, priority = 0 where " +
					 "groupId = ? and plid = ? and privateLayout = 0");

			 PreparedStatement preparedStatement5 =
				 AutoBatchPreparedStatementUtil.autoBatch(
					 connection,
					 "update Layout set privateLayout = 0 where groupId = ? " +
					 "and privateLayout = 1");

			 PreparedStatement preparedStatement6 =
				 AutoBatchPreparedStatementUtil.autoBatch(
					 connection,
					 "select plid from Layout where friendlyURL != '/login' " +
					 "and groupId = ? and parentPlid = 0 order by priority");

			 PreparedStatement preparedStatement7 =
				 AutoBatchPreparedStatementUtil.autoBatch(
					 connection,
					 "update Layout set priority = ? where groupId = ? and " +
					 "plid = ?")) {

			try (ResultSet resultSet1 = preparedStatement1.executeQuery()) {
				while (resultSet1.next()) {
					long siteGroupId = resultSet1.getLong("siteGroupId");

					_updateLoginLayoutId(
						preparedStatement2, preparedStatement3,
						preparedStatement4, siteGroupId);

					_updateLayoutPriorities(
						preparedStatement6, preparedStatement7, siteGroupId);

					preparedStatement5.setLong(1, siteGroupId);

					preparedStatement5.addBatch();
				}

				preparedStatement5.executeBatch();
			}
		}
	}

	private void _updateLayoutPriorities(
			PreparedStatement preparedStatement6,
			PreparedStatement preparedStatement7, long siteGroupId)
		throws SQLException {

		preparedStatement6.setLong(1, siteGroupId);

		try (ResultSet resultSet6 = preparedStatement6.executeQuery()) {
			long priority = 0;

			while (resultSet6.next()) {
				long plid = resultSet6.getLong("plid");

				priority++;

				preparedStatement7.setLong(1, priority);
				preparedStatement7.setLong(2, siteGroupId);
				preparedStatement7.setLong(3, plid);

				preparedStatement7.addBatch();
			}

			preparedStatement7.executeBatch();
		}
	}

	private void _updateLoginLayoutId(
			PreparedStatement preparedStatement2,
			PreparedStatement preparedStatement3,
			PreparedStatement preparedStatement4, long siteGroupId)
		throws SQLException {

		preparedStatement2.setLong(1, siteGroupId);

		try (ResultSet resultSet2 = preparedStatement2.executeQuery()) {
			while (resultSet2.next()) {
				long maxLayoutId = resultSet2.getLong("maxLayoutId");

				preparedStatement3.setLong(1, siteGroupId);

				try (ResultSet resultSet3 = preparedStatement3.executeQuery()) {
					while (resultSet3.next()) {
						long plid = resultSet3.getLong("plid");

						preparedStatement4.setLong(1, maxLayoutId + 1);
						preparedStatement4.setLong(2, siteGroupId);
						preparedStatement4.setLong(3, plid);

						preparedStatement4.addBatch();
					}

					preparedStatement4.executeBatch();
				}
			}
		}
	}

}