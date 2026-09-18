/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayButtonWithIcon} from '@clayui/button';
import {ClayInput, ClaySelect} from '@clayui/form';
import {RequiredMark} from '@liferay/site-cms-site-initializer';
import React from 'react';

import getObjectFields from './getObjectFields';
import {
	IMapping,
	IObjectField,
	IPIMObjectDefinition,
	MAPPING_TYPE_ATTRIBUTE,
	MAPPING_TYPE_FIXED_VALUE,
} from './types';

interface IProps {
	allStructuresObjectFields: IObjectField[];
	deletable: boolean;
	index: number;
	mapping: IMapping;
	onAdd: () => void;
	onChange: (index: number, mapping: IMapping) => void;
	onDelete: (index: number) => void;
	pimObjectDefinitions: IPIMObjectDefinition[];
	spritemap: string;
}

function getObjectFieldLabel({label, name}: IObjectField) {
	return `${label} (${name})`;
}

export default function DestinationRow({
	allStructuresObjectFields,
	deletable,
	index,
	mapping,
	onAdd,
	onChange,
	onDelete,
	pimObjectDefinitions,
	spritemap,
}: IProps) {
	const fixedValue = mapping.type === MAPPING_TYPE_FIXED_VALUE;

	const objectFields = getObjectFields(
		allStructuresObjectFields,
		pimObjectDefinitions,
		mapping.source
	);

	const handleAttributeChange = (value: string) => {
		if (value === MAPPING_TYPE_FIXED_VALUE) {
			onChange(index, {
				attribute: '',
				source: mapping.source,
				type: MAPPING_TYPE_FIXED_VALUE,
				value: mapping.value || '',
			});

			return;
		}

		onChange(index, {
			attribute: value,
			source: mapping.source,
			type: MAPPING_TYPE_ATTRIBUTE,
		});
	};

	return (
		<>
			<ClayInput.Group className="c-gap-3">
				<ClayInput.GroupItem className="c-gap-1">
					<label>
						{Liferay.Language.get('source')}

						<RequiredMark />
					</label>

					<ClaySelect
						aria-label={Liferay.Language.get('source')}
						disabled={fixedValue}
						onChange={(event) =>
							onChange(index, {
								attribute: '',
								source: event.target.value,
								type: MAPPING_TYPE_ATTRIBUTE,
							})
						}
						value={mapping.source}
					>
						<ClaySelect.Option
							label={Liferay.Language.get('all-structures')}
							value=""
						/>

						{pimObjectDefinitions.map((pimObjectDefinition) => (
							<ClaySelect.Option
								key={pimObjectDefinition.name}
								label={pimObjectDefinition.label}
								value={pimObjectDefinition.name}
							/>
						))}
					</ClaySelect>
				</ClayInput.GroupItem>

				<ClayInput.GroupItem className="c-gap-1">
					<div className="align-items-center d-flex flex-fill">
						<label>
							{Liferay.Language.get('attribute')}

							<RequiredMark />
						</label>

						<div className="ml-auto">
							<ClayButtonWithIcon
								aria-label={Liferay.Language.get('delete-row')}
								className="rounded-circle"
								disabled={!deletable}
								onClick={() => onDelete(index)}
								size="xs"
								spritemap={spritemap}
								symbol="hr"
							/>

							<ClayButtonWithIcon
								aria-label={Liferay.Language.get('add-row')}
								className="ml-1 rounded-circle"
								onClick={onAdd}
								size="xs"
								spritemap={spritemap}
								symbol="plus"
							/>
						</div>
					</div>

					<ClaySelect
						aria-label={Liferay.Language.get('attribute')}
						onChange={(event) =>
							handleAttributeChange(event.target.value)
						}
						value={
							fixedValue
								? MAPPING_TYPE_FIXED_VALUE
								: mapping.attribute
						}
					>
						<ClaySelect.Option
							label={Liferay.Language.get('not-mapped')}
							value=""
						/>

						{objectFields.map((objectField) => (
							<ClaySelect.Option
								key={objectField.name}
								label={getObjectFieldLabel(objectField)}
								value={objectField.name}
							/>
						))}

						<ClaySelect.Option
							label={Liferay.Language.get('fixed-value')}
							value={MAPPING_TYPE_FIXED_VALUE}
						/>
					</ClaySelect>
				</ClayInput.GroupItem>
			</ClayInput.Group>

			{fixedValue && (
				<ClayInput.Group>
					<ClayInput.GroupItem className="c-gap-1">
						<label>
							{Liferay.Language.get('value')}

							<RequiredMark />
						</label>

						<ClayInput
							aria-label={Liferay.Language.get('value')}
							onChange={(event) =>
								onChange(index, {
									...mapping,
									value: event.target.value,
								})
							}
							type="text"
							value={mapping.value || ''}
						/>
					</ClayInput.GroupItem>
				</ClayInput.Group>
			)}
		</>
	);
}
