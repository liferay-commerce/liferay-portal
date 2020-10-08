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

package com.liferay.dispatch.constants;

import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;

/**
 * @author Marco Leo
 * @author Igor Beslic
 */
public class DispatchConstants {

	public static final int[] ALLOWED_BACKGROUND_TASK_STATUSES = {
		BackgroundTaskConstants.STATUS_CANCELLED,
		BackgroundTaskConstants.STATUS_FAILED,
		BackgroundTaskConstants.STATUS_IN_PROGRESS,
		BackgroundTaskConstants.STATUS_NEW,
		BackgroundTaskConstants.STATUS_QUEUED,
		BackgroundTaskConstants.STATUS_SUCCESSFUL
	};

	public static final String CATEGORY_KEY_DISPATCH_DETAILS = "details";

	public static final String CATEGORY_KEY_DISPATCH_LOGS = "logs";

	public static final String CATEGORY_KEY_DISPATCH_TRIGGER =
		"dispatch-trigger";

	public static final String EXECUTOR_DESTINATION_NAME =
		"liferay/dispatch/executor";

	public static final String SCREEN_NAVIGATION_KEY_DISPATCH_GENERAL =
		"dispatch.general";

}