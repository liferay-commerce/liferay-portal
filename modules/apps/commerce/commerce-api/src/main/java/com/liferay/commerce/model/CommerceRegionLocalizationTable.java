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

package com.liferay.commerce.model;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.base.BaseTable;

import java.sql.Types;

/**
 * The table class for the &quot;CommerceRegionLocalization&quot; database table.
 *
 * @author Alessio Antonio Rendina
 * @see CommerceRegionLocalization
 * @generated
 */
public class CommerceRegionLocalizationTable
	extends BaseTable<CommerceRegionLocalizationTable> {

	public static final CommerceRegionLocalizationTable INSTANCE =
		new CommerceRegionLocalizationTable();

	public final Column<CommerceRegionLocalizationTable, Long> mvccVersion =
		createColumn(
			"mvccVersion", Long.class, Types.BIGINT, Column.FLAG_NULLITY);
	public final Column<CommerceRegionLocalizationTable, Long>
		commerceRegionLocalizationId = createColumn(
			"commerceRegionLocalizationId", Long.class, Types.BIGINT,
			Column.FLAG_PRIMARY);
	public final Column<CommerceRegionLocalizationTable, Long> companyId =
		createColumn(
			"companyId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<CommerceRegionLocalizationTable, Long>
		commerceRegionId = createColumn(
			"commerceRegionId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<CommerceRegionLocalizationTable, String> languageId =
		createColumn(
			"languageId", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<CommerceRegionLocalizationTable, String> name =
		createColumn("name", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);

	private CommerceRegionLocalizationTable() {
		super(
			"CommerceRegionLocalization", CommerceRegionLocalizationTable::new);
	}

}