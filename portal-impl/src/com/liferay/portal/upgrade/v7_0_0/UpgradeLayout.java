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

package com.liferay.portal.upgrade.v7_0_0;

import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.kernel.util.StringBundler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jonathan McCann
 */
public class UpgradeLayout extends UpgradeProcess {

	protected void deleteLinkedOrphanedLayouts() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			runSQL(
				StringBundler.concat(
					"delete from Layout where layoutPrototypeUuid != '' and ",
					"layoutPrototypeUuid not in (select uuid_ from ",
					"LayoutPrototype) and layoutPrototypeLinkEnabled = ",
					"[$TRUE$]"));
		}
	}

	protected void deleteOrphanedFriendlyURL() throws Exception {
		Set<Long> plids = new HashSet<>();

		try (LoggingTimer loggingTimer = new LoggingTimer("step1");
			PreparedStatement preparedStatement = connection.prepareStatement(
				"select plid from Layout");
			ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				long plid = resultSet.getLong("plid");

				plids.add(plid);
			}
		}

		int count = 0;

		try (LoggingTimer loggingTimer = new LoggingTimer("step2");
			PreparedStatement preparedStatement1 = connection.prepareStatement(
				"select plid from LayoutFriendlyURL");
			ResultSet resultSet = preparedStatement1.executeQuery()) {

			try (PreparedStatement preparedStatement2 =
					AutoBatchPreparedStatementUtil.autoBatch(
						connection,
						"delete from LayoutFriendlyURL where plid = ?")) {

				while (resultSet.next()) {
					long plid = resultSet.getLong("plid");

					if (plids.contains(plid)) {
						continue;
					}

					preparedStatement2.setLong(1, plid);

					preparedStatement2.addBatch();

					count++;
				}

				preparedStatement2.executeBatch();
			}
		}

		if (_log.isInfoEnabled()) {
			_log.info("Deleted " + count + " layout friendly URLs");
		}
	}

	@Override
	protected void doUpgrade() throws Exception {
		deleteLinkedOrphanedLayouts();
		deleteOrphanedFriendlyURL();
		updateLayoutPrototypeLinkEnabled();
		updateUnlinkedOrphanedLayouts();
	}

	protected void updateLayoutPrototypeLinkEnabled() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			runSQL(
				"update Layout set layoutPrototypeLinkEnabled = [$FALSE$] " +
					"where type_ = 'link_to_layout' and " +
						"layoutPrototypeLinkEnabled = [$TRUE$]");
		}
	}

	protected void updateUnlinkedOrphanedLayouts() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			runSQL(
				StringBundler.concat(
					"update Layout set layoutPrototypeUuid = null where ",
					"layoutPrototypeUuid != '' and layoutPrototypeUuid not in ",
					"(select uuid_ from LayoutPrototype) and ",
					"layoutPrototypeLinkEnabled = [$FALSE$]"));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(UpgradeLayout.class);

}