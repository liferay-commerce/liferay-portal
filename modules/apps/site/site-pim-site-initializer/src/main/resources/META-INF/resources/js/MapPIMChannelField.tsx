/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {Toolbar, VerticalNavLayout} from '@liferay/site-cms-site-initializer';
import {fetch, navigate, sub} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import DestinationSection from './map_channel_field/DestinationSection';
import MappingRulesSection from './map_channel_field/MappingRulesSection';
import getObjectFields from './map_channel_field/getObjectFields';
import {
	IMapping,
	IObjectField,
	IPIMObjectDefinition,
	MAPPING_TYPE_ATTRIBUTE,
	MAPPING_TYPE_FIXED_VALUE,
} from './map_channel_field/types';

interface IProps {
	allStructuresObjectFields: IObjectField[];
	apiURL: string;
	backURL: string;
	channelField: string;
	fieldMapping: {[key: string]: unknown};
	mappings: IMapping[];
	objectEntryId: number;
	pimObjectDefinitions: IPIMObjectDefinition[];
	spritemap: string;
	title: string;
}

export default function MapPIMChannelField({
	allStructuresObjectFields = [],
	apiURL,
	backURL,
	channelField,
	fieldMapping,
	mappings: initialMappings = [],
	objectEntryId,
	pimObjectDefinitions = [],
	spritemap,
	title,
}: IProps) {
	const [mappings, setMappings] = useState<IMapping[]>(() => {
		const currentMappings = initialMappings.filter(
			({attribute, source, type}) =>
				type === MAPPING_TYPE_FIXED_VALUE ||
				getObjectFields(
					allStructuresObjectFields,
					pimObjectDefinitions,
					source
				).some((objectField) => objectField.name === attribute)
		);

		if (currentMappings.length) {
			return currentMappings;
		}

		return [{attribute: '', source: ''}];
	});

	useEffect(() => {
		if (!Number(objectEntryId) || !channelField) {
			navigate(backURL);
		}
	}, [backURL, channelField, objectEntryId]);

	const handleSave = async () => {
		const nextFieldMapping = {...fieldMapping};

		const nextMappings = mappings
			.filter(({attribute, type, value}) =>
				type === MAPPING_TYPE_FIXED_VALUE ? value : attribute
			)
			.map(({attribute, source, type, value}) =>
				type === MAPPING_TYPE_FIXED_VALUE
					? {type, value}
					: {attribute, source, type: MAPPING_TYPE_ATTRIBUTE}
			);

		if (nextMappings.length) {
			nextFieldMapping[channelField] = nextMappings;
		}
		else {
			delete nextFieldMapping[channelField];
		}

		try {
			const response = await fetch(`${apiURL}/${objectEntryId}`, {
				body: JSON.stringify({
					fieldMapping: JSON.stringify(nextFieldMapping),
				}),
				headers: {
					'Content-Type': 'application/json',
				},
				method: 'PATCH',
			});

			if (!response.ok) {
				throw new Error();
			}

			Liferay.Util.openToast({
				message: sub(
					Liferay.Language.get('x-was-updated-successfully'),
					channelField
				),
				type: 'success',
			});

			navigate(backURL);
		}
		catch (error) {
			Liferay.Util.openToast({
				message: Liferay.Language.get('an-unexpected-error-occurred'),
				type: 'danger',
			});
		}
	};

	return (
		<>
			<Toolbar backURL={backURL} title={title}>
				<Toolbar.Item>
					<ClayButton
						displayType="secondary"
						onClick={() => navigate(backURL)}
						size="sm"
					>
						{Liferay.Language.get('cancel')}
					</ClayButton>

					<ClayButton
						className="inline-item-after"
						displayType="primary"
						onClick={handleSave}
						size="sm"
					>
						{Liferay.Language.get('save')}
					</ClayButton>
				</Toolbar.Item>
			</Toolbar>

			<VerticalNavLayout
				items={[
					{
						component: (
							<DestinationSection
								allStructuresObjectFields={
									allStructuresObjectFields
								}
								mappings={mappings}
								pimObjectDefinitions={pimObjectDefinitions}
								setMappings={setMappings}
								spritemap={spritemap}
							/>
						),
						id: 'destination',
						label: Liferay.Language.get('destination'),
					},
					{
						component: <MappingRulesSection />,
						id: 'mappingRules',
						label: Liferay.Language.get('mapping-rules'),
					},
				]}
			/>
		</>
	);
}
