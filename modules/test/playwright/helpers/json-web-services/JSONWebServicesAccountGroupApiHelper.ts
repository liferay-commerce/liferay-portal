/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {liferayConfig} from '../../liferay.config';
import {ApiHelpers} from '../ApiHelpers';

type AccountGroup = {
	accountGroupId: string;
	name: string;
};

export class JSONWebServicesAccountGroupApiHelper {
	readonly apiHelpers: ApiHelpers;
	readonly basePath: string;

	constructor(apiHelpers: ApiHelpers) {
		this.apiHelpers = apiHelpers;
		this.basePath = '/api/jsonws/account.accountgroup';
	}

	async getAccountGroupsByAccountEntryId(
		accountEntryId: number
	): Promise<AccountGroup[]> {
		const urlSearchParams = new URLSearchParams();

		urlSearchParams.append('accountEntryId', String(accountEntryId));
		urlSearchParams.append('end', '-1');
		urlSearchParams.append('start', '-1');

		return this.apiHelpers.post(
			`${liferayConfig.environment.baseUrl}${this.basePath}/get-account-groups-by-account-entry-id`,
			{
				data: urlSearchParams.toString(),
				failOnStatusCode: true,
				headers: await this.apiHelpers.getJSONWebServicesHeaders(),
			}
		);
	}
}
