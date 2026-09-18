/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayEmptyState from '@clayui/empty-state';
import ClayPanel from '@clayui/panel';
import React from 'react';

export default function MappingRulesSection() {
	return (
		<div className="container-fluid container-fluid-max-md p-0 p-md-4">
			<ClayPanel
				aria-label={Liferay.Language.get('mapping-rules')}
				className="mb-4"
				collapsable={false}
				displayType="secondary"
				role="group"
			>
				<div className="p-4">
					<h2 className="mb-0 py-2 text-6 text-dark">
						{Liferay.Language.get('mapping-rules')}
					</h2>

					<ClayEmptyState
						description=""
						title={Liferay.Language.get('no-rules-were-found')}
					/>
				</div>
			</ClayPanel>
		</div>
	);
}
