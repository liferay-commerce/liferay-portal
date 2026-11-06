/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayInput, ClaySelect} from '@clayui/form';
import {RequiredMark} from '@liferay/site-cms-site-initializer';
import React from 'react';

import {IRepeatableGroup} from './types';

interface IProps {
	onChange: (value: string) => void;
	repeatableGroup: IRepeatableGroup;
	value: string;
}

export default function RepeatableGroupPanel({
	onChange,
	repeatableGroup,
	value,
}: IProps) {
	const childRepeatableGroups = repeatableGroup.repeatableGroups ?? [];

	return (
		<ClayInput.Group>
			<ClayInput.GroupItem className="c-gap-1">
				<label>
					{repeatableGroup.label}

					<RequiredMark />
				</label>

				<ClaySelect
					aria-label={repeatableGroup.label}
					onChange={(event) => onChange(event.target.value)}
					value={value}
				>
					<ClaySelect.Option
						disabled
						label={Liferay.Language.get('select-a-field')}
						value=""
					/>

					{repeatableGroup.objectFields.map((objectField) => (
						<ClaySelect.Option
							key={objectField.name}
							label={`${objectField.label} (${objectField.name})`}
							value={objectField.name}
						/>
					))}

					{!!childRepeatableGroups.length && (
						<ClaySelect.OptGroup
							label={Liferay.Language.get('repeatable-groups')}
						>
							{childRepeatableGroups.map(
								(childRepeatableGroup) => (
									<ClaySelect.Option
										key={childRepeatableGroup.name}
										label={childRepeatableGroup.label}
										value={childRepeatableGroup.name}
									/>
								)
							)}
						</ClaySelect.OptGroup>
					)}
				</ClaySelect>
			</ClayInput.GroupItem>
		</ClayInput.Group>
	);
}
