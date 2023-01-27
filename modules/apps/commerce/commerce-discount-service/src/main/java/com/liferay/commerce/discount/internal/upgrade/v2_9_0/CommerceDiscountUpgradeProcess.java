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

package com.liferay.commerce.discount.internal.upgrade.v2_9_0;

import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeProcessFactory;
import com.liferay.portal.kernel.upgrade.UpgradeStep;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Crescenzo Rega
 */
public class CommerceDiscountUpgradeProcess extends UpgradeProcess {

	@Override
	protected void doUpgrade() throws Exception {
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"select commerceDiscountId, companyId from CommerceDiscount")) {

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					long commerceDiscountId = resultSet.getLong(
						"commerceDiscountId");
					long companyId = resultSet.getLong("companyId");

					_updateCommerceDiscounts(commerceDiscountId, companyId);
				}
			}
		}
	}

	@Override
	protected UpgradeStep[] getPreUpgradeSteps() {
		return new UpgradeStep[] {
			UpgradeProcessFactory.addColumns(
				"CommerceDiscount", "commerceCurrencyCode VARCHAR(75) null")
		};
	}

	private void _updateCommerceDiscounts(
			long commerceDiscountId, long companyId)
		throws SQLException {

		PreparedStatement preparedStatement1 =
			AutoBatchPreparedStatementUtil.autoBatch(
				connection,
				"select code_ from CommerceCurrency cc where companyId = ? " +
					"and active_ = ? and primary_ = ?");
		PreparedStatement preparedStatement2 =
			AutoBatchPreparedStatementUtil.autoBatch(
				connection,
				"update CommerceDiscount set commerceCurrencyCode = ? where " +
					"commerceDiscountId = ?");

		preparedStatement1.setLong(1, companyId);
		preparedStatement1.setBoolean(2, true);
		preparedStatement1.setBoolean(3, true);

		try (ResultSet resultSet = preparedStatement1.executeQuery()) {
			while (resultSet.next()) {
				preparedStatement2.setString(1, resultSet.getString("code_"));
				preparedStatement2.setLong(2, commerceDiscountId);

				preparedStatement2.addBatch();
			}
		}

		preparedStatement2.executeBatch();
	}

}