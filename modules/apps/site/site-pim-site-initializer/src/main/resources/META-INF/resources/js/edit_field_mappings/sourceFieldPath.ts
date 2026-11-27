/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import getObjectFields from './getObjectFields';
import {IObjectDefinition, IObjectField, IRepeatableGroup} from './types';

export const SOURCE_FIELD_NAME_MAX_LENGTH = 280;

const SEPARATOR = '/';

export interface ISourceFieldPath {
	complete: boolean;
	repeatableGroups: IRepeatableGroup[];
}

export function getRepeatableGroups(
	objectDefinitions: IObjectDefinition[],
	sourceClassName: string
): IRepeatableGroup[] {
	if (sourceClassName) {
		const objectDefinition = objectDefinitions.find(
			({className}) => className === sourceClassName
		);

		return objectDefinition?.repeatableGroups ?? [];
	}

	return mergeRepeatableGroups(
		objectDefinitions.flatMap(
			(objectDefinition) => objectDefinition.repeatableGroups ?? []
		)
	);
}

function mergeRepeatableGroups(
	repeatableGroups: IRepeatableGroup[]
): IRepeatableGroup[] {
	const repeatableGroupsMap = new Map<string, IRepeatableGroup[]>();

	for (const repeatableGroup of repeatableGroups) {
		repeatableGroupsMap.set(repeatableGroup.name, [
			...(repeatableGroupsMap.get(repeatableGroup.name) ?? []),
			repeatableGroup,
		]);
	}

	return [...repeatableGroupsMap.values()].map((curRepeatableGroups) => {
		const objectFields = new Map<string, IObjectField>();

		for (const repeatableGroup of curRepeatableGroups) {
			for (const objectField of repeatableGroup.objectFields) {
				if (!objectFields.has(objectField.name)) {
					objectFields.set(objectField.name, objectField);
				}
			}
		}

		return {
			label: curRepeatableGroups[0].label,
			name: curRepeatableGroups[0].name,
			objectFields: [...objectFields.values()],
			repeatableGroups: mergeRepeatableGroups(
				curRepeatableGroups.flatMap(
					(repeatableGroup) => repeatableGroup.repeatableGroups ?? []
				)
			),
		};
	});
}

export function joinSourceFieldPath(segments: string[]): string {
	return segments.join(SEPARATOR);
}

export function parseSourceFieldPath(sourceFieldName: string): string[] {
	return sourceFieldName ? sourceFieldName.split(SEPARATOR) : [];
}

export function resolveSourceFieldPath(
	objectDefinitions: IObjectDefinition[],
	sourceClassName: string,
	sourceFieldName: string
): ISourceFieldPath {
	const segments = parseSourceFieldPath(sourceFieldName);

	const repeatableGroups: IRepeatableGroup[] = [];

	let objectFields: IObjectField[] = getObjectFields(
		objectDefinitions,
		sourceClassName
	);
	let childRepeatableGroups = getRepeatableGroups(
		objectDefinitions,
		sourceClassName
	);

	for (const [index, segment] of segments.entries()) {
		const last = index === segments.length - 1;

		if (last && objectFields.some(({name}) => name === segment)) {
			return {complete: true, repeatableGroups};
		}

		const repeatableGroup = childRepeatableGroups.find(
			({name}) => name === segment
		);

		if (!repeatableGroup) {
			break;
		}

		repeatableGroups.push(repeatableGroup);

		if (last) {
			return {complete: false, repeatableGroups};
		}

		objectFields = repeatableGroup.objectFields;
		childRepeatableGroups = repeatableGroup.repeatableGroups ?? [];
	}

	return {complete: false, repeatableGroups};
}
