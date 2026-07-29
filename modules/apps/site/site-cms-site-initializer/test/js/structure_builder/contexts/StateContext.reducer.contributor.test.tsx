/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {act, render} from '@testing-library/react';
import React, {Dispatch, useEffect} from 'react';

import {
	Action,
	State,
	StateContextProvider,
	useSelector,
	useStateDispatch,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/contexts/StateContext';
import structureBuilderRegistry from '../../../../src/main/resources/META-INF/resources/js/structure_builder/contributors/registry';
import {
	GroupingContainer,
	Structure,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import {Field} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/field';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';

const STRUCTURE_UUID = getUuid();
const TITLE_UUID = getUuid();
const SKU_UUID = getUuid();

function field(name: string, uuid: Uuid): Field {
	return {
		erc: `${name}-erc`,
		indexableConfig: {indexed: false},
		label: {en_US: name},
		localized: false,
		locked: false,
		name,
		parent: STRUCTURE_UUID,
		required: false,
		settings: {},
		type: 'text',
		uuid,
	};
}

function buildInitialState(publishedChildren: Set<Uuid> = new Set()): State {
	const structure: Structure = {
		children: new Map([
			[TITLE_UUID, field('title', TITLE_UUID)],
			[SKU_UUID, field('sku', SKU_UUID)],
		]),
		erc: 'structure-erc',
		label: {en_US: 'Structure'},
		name: 'Structure',
		path: '',
		spaces: 'all',
		status: 'draft',
		system: false,
		type: 'L_CUSTOM_STRUCTURES',
		uuid: STRUCTURE_UUID,
		workflows: {},
	};

	return {
		clipboard: null,
		history: {
			deletedChildren: [],
			deletedGroupERCs: [],
			deletedRelationships: [],
			modifiedNames: new Set(),
		},
		invalids: new Map(),
		publishedChildren,
		renamingItemUuid: null,
		selection: [],
		structure,
		unsavedChanges: false,
	};
}

type Refs = {
	dispatch?: Dispatch<Action>;
	state?: State;
};

function renderWithState(initialState: State) {
	const refs: Refs = {};

	function Harness() {
		const state = useSelector((s) => s);
		const dispatch = useStateDispatch();

		useEffect(() => {
			refs.dispatch = dispatch;
			refs.state = state;
		});

		return null;
	}

	render(
		<StateContextProvider initialState={initialState}>
			<Harness />
		</StateContextProvider>
	);

	return refs;
}

describe('StateContext reducer - contributor delegation', () => {
	beforeAll(() => {
		structureBuilderRegistry.addProvider({
			id: 'stub',
			reduce: ({action, structure}) => {
				if (action.type === 'update-grouping-container') {
					const children = new Map(structure.children);

					const container = children.get(action.uuid);

					if (container) {
						children.set(action.uuid, {
							...container,
							label: action.label,
						});
					}

					return {children};
				}

				const nodeUuid = getUuid();

				const moved = new Set(action.uuids);

				const children: Structure['children'] = new Map();

				for (const [uuid, child] of structure.children) {
					if (!moved.has(uuid)) {
						children.set(uuid, child);
					}
				}

				const nodeChildren: Structure['children'] = new Map();

				for (const uuid of action.uuids) {
					const child = structure.children.get(uuid);

					if (child) {
						nodeChildren.set(uuid, {...child, parent: nodeUuid});
					}
				}

				children.set(nodeUuid, {
					children: nodeChildren,
					label: {en_US: 'Container'},
					parent: action.parent,
					type: 'grouping-container',
					uuid: nodeUuid,
				} as GroupingContainer);

				return {children, selection: [nodeUuid]};
			},
			supports: () => true,
		});
	});

	beforeEach(() => {
		jest.spyOn(
			Liferay.ThemeDisplay,
			'getDefaultLanguageId'
		).mockReturnValue('en_US');
		jest.spyOn(Liferay.ThemeDisplay, 'getLanguageId').mockReturnValue(
			'en_US'
		);
	});

	afterEach(() => {
		jest.restoreAllMocks();
	});

	it('records no data-loss history when grouping published fields (fields stay on the definition)', () => {
		const refs = renderWithState(buildInitialState(new Set([SKU_UUID])));

		act(() => {
			refs.dispatch!({
				parent: STRUCTURE_UUID,
				type: 'add-grouping-container',
				uuids: [SKU_UUID],
			});
		});

		expect(refs.state!.history.deletedChildren).toHaveLength(0);
	});
});
