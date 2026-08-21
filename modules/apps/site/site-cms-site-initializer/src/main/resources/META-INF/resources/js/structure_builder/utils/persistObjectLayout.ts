/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ApiHelper from '../../common/services/ApiHelper';
import {Structure} from '../types/Structure';
import buildObjectLayout from './buildObjectLayout';

export default async function persistObjectLayout({
	erc,
	structure,
}: {
	erc: Structure['erc'];
	structure: Structure;
}): Promise<{error: boolean}> {
	const objectLayout = buildObjectLayout(structure);

	const {data} = await ApiHelper.get<{items?: {id: number}[]}>(
		`/o/object-admin/v1.0/object-definitions/by-external-reference-code/${erc}/object-layouts?pageSize=100`
	);

	for (const existingObjectLayout of data?.items ?? []) {
		await ApiHelper.delete(
			`/o/object-admin/v1.0/object-layouts/${existingObjectLayout.id}`
		);
	}

	if (objectLayout) {
		const {error} = await ApiHelper.post(
			`/o/object-admin/v1.0/object-definitions/by-external-reference-code/${erc}/object-layouts`,
			objectLayout
		);

		if (error) {
			return {error: true};
		}
	}

	return {error: false};
}
