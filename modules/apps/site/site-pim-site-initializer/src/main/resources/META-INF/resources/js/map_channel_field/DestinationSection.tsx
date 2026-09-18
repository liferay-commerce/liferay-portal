/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayForm from '@clayui/form';
import ClayPanel from '@clayui/panel';
import React from 'react';

import DestinationRow from './DestinationRow';
import {IMapping, IObjectField, IPIMObjectDefinition} from './types';

interface IProps {
	allStructuresObjectFields: IObjectField[];
	mappings: IMapping[];
	pimObjectDefinitions: IPIMObjectDefinition[];
	setMappings: React.Dispatch<React.SetStateAction<IMapping[]>>;
	spritemap: string;
}

export default function DestinationSection({
	allStructuresObjectFields,
	mappings,
	pimObjectDefinitions,
	setMappings,
	spritemap,
}: IProps) {
	const handleAdd = () =>
		setMappings((previousMappings) => [
			...previousMappings,
			{attribute: '', source: ''},
		]);

	const handleChange = (index: number, mapping: IMapping) =>
		setMappings((previousMappings) =>
			previousMappings.map((previousMapping, previousIndex) =>
				previousIndex === index ? mapping : previousMapping
			)
		);

	const handleDelete = (index: number) =>
		setMappings((previousMappings) =>
			previousMappings.filter(
				(previousMapping, previousIndex) => previousIndex !== index
			)
		);

	return (
		<div className="container-fluid container-fluid-max-md p-0 p-md-4">
			<ClayPanel
				aria-label={Liferay.Language.get('destination')}
				className="mb-4"
				collapsable={false}
				displayType="secondary"
				role="group"
			>
				<ClayForm.Group className="c-gap-4 d-flex flex-column p-4">
					<h2 className="mb-0 py-2 text-6 text-dark">
						{Liferay.Language.get('destination')}
					</h2>

					<div className="text-secondary">
						{Liferay.Language.get(
							'destination-is-where-this-channel-field-will-be-saved'
						)}
					</div>

					{mappings.map((mapping, index) => (
						<DestinationRow
							allStructuresObjectFields={
								allStructuresObjectFields
							}
							deletable={mappings.length > 1}
							index={index}
							key={index}
							mapping={mapping}
							onAdd={handleAdd}
							onChange={handleChange}
							onDelete={handleDelete}
							pimObjectDefinitions={pimObjectDefinitions}
							spritemap={spritemap}
						/>
					))}
				</ClayForm.Group>
			</ClayPanel>
		</div>
	);
}
