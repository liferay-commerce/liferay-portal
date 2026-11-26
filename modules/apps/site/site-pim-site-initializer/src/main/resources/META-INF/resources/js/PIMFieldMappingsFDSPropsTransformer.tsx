/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-components-web';
import {fetch} from 'frontend-js-web';

import PIMFieldMappingChannelFieldRenderer from './cell_renderers/PIMFieldMappingChannelFieldRenderer';
import PIMFieldMappingSourceAttributesRenderer from './cell_renderers/PIMFieldMappingSourceAttributesRenderer';
import PIMFieldMappingTransformationRulesRenderer from './cell_renderers/PIMFieldMappingTransformationRulesRenderer';

export const EVENT_FIELD_MAPPINGS_CHANGE = 'pimConnectorFieldMappings:change';

export default function propsTransformer(props: {[key: string]: any}) {
	return {
		...props,
		customRenderers: {
			tableCell: [
				{
					component: PIMFieldMappingChannelFieldRenderer,
					name: 'channelFieldTableCellRenderer',
					type: 'internal',
				},
				{
					component: PIMFieldMappingSourceAttributesRenderer,
					name: 'sourceAttributesTableCellRenderer',
					type: 'internal',
				},
				{
					component: PIMFieldMappingTransformationRulesRenderer,
					name: 'transformationRulesTableCellRenderer',
					type: 'internal',
				},
			],
		},
		hideManagementBarInEmptyState: true,
		onActionDropdownItemClick({action, itemData, loadData}: any) {
			if (action?.data?.id !== 'clear') {
				return;
			}

			clearFieldMappings(
				action.data.apiURL,
				itemData.fieldMappingIds,
				action.data.fieldMappingRuleAPIURL,
				itemData.fieldMappingRuleId,
				loadData,
				itemData.required
			);
		},
	};
}

function clearFieldMappings(
	apiURL: string,
	fieldMappingIds: number[],
	fieldMappingRuleAPIURL: string,
	fieldMappingRuleId: number,
	loadData: Function,
	required: boolean
) {
	const urls = (fieldMappingIds ?? []).map(
		(fieldMappingId) => `${apiURL}/${fieldMappingId}`
	);

	if (fieldMappingRuleId) {
		urls.push(`${fieldMappingRuleAPIURL}/${fieldMappingRuleId}`);
	}

	if (!urls.length) {
		return;
	}

	Promise.all(urls.map((url) => fetch(url, {method: 'DELETE'})))
		.then((responses) => {
			if (responses.some(({ok}) => !ok)) {
				throw new Error();
			}

			loadData();

			if (required) {
				Liferay.fire(EVENT_FIELD_MAPPINGS_CHANGE, {
					mappingStatus: 'incomplete',
				});
			}

			openToast({
				message: Liferay.Language.get(
					'your-request-completed-successfully'
				),
				type: 'success',
			});
		})
		.catch(() => {
			openToast({
				message: Liferay.Language.get('an-unexpected-error-occurred'),
				type: 'danger',
			});
		});
}
