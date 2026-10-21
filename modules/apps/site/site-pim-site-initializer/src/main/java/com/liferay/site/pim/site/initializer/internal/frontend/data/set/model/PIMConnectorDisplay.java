/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.frontend.data.set.model;

import java.util.Date;
import java.util.Map;

/**
 * @author Tancredi Covioli
 */
public class PIMConnectorDisplay {

	public PIMConnectorDisplay(
		Map<String, Map<String, String>> actions, boolean active,
		Date dateModified, long id, String key, String lastExecution,
		Date lastExecutionDate, String name) {

		_actions = actions;
		_active = active;
		_dateModified = dateModified;
		_id = id;
		_key = key;
		_lastExecution = lastExecution;
		_lastExecutionDate = lastExecutionDate;
		_name = name;
	}

	public Map<String, Map<String, String>> getActions() {
		return _actions;
	}

	public Date getDateModified() {
		return _dateModified;
	}

	public long getId() {
		return _id;
	}

	public String getKey() {
		return _key;
	}

	public String getLastExecution() {
		return _lastExecution;
	}

	public Date getLastExecutionDate() {
		return _lastExecutionDate;
	}

	public String getName() {
		return _name;
	}

	public boolean isActive() {
		return _active;
	}

	private final Map<String, Map<String, String>> _actions;
	private final boolean _active;
	private final Date _dateModified;
	private final long _id;
	private final String _key;
	private final String _lastExecution;
	private final Date _lastExecutionDate;
	private final String _name;

}