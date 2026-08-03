/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import AddChildDropdown from '../../../../src/main/resources/META-INF/resources/js/structure_builder/components/AddChildDropdown';
import structureBuilderRegistry from '../../../../src/main/resources/META-INF/resources/js/structure_builder/contributors/registry';
import {
	GroupingContainer,
	Structure,
} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import {Uuid} from '../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Uuid';
import getUuid from '../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import {MockCacheProvider} from '../mocks/MockCacheProvider';
import {MockStateProvider} from '../mocks/MockStateProvider';

const ROOT_UUID = getUuid();
const CONTAINER_UUID = getUuid();

const container: GroupingContainer = {
	children: new Map(),
	label: {en_US: 'Container'},
	parent: ROOT_UUID,
	type: 'grouping-container',
	uuid: CONTAINER_UUID,
};

const structure: Partial<Structure> = {
	children: new Map([[CONTAINER_UUID, container]]),
	uuid: ROOT_UUID,
};

function renderDropdown(parentUuid?: Uuid) {
	return render(
		<MockCacheProvider objectDefinitions={{}} spaces={[]}>
			<MockStateProvider state={{structure}}>
				<AddChildDropdown parentUuid={parentUuid} />
			</MockStateProvider>
		</MockCacheProvider>
	);
}

describe('AddChildDropdown', () => {
	beforeAll(() => {
		structureBuilderRegistry.addProvider({
			id: 'test-contributor',
			isGroupingContainer: (child) => child.type === 'grouping-container',
			supports: () => true,
		});
	});

	it('offers relationship options at the structure root', async () => {
		renderDropdown();

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.getByText('select-related-content')).toBeInTheDocument();
		expect(
			screen.getByText('referenced-content-structure')
		).toBeInTheDocument();
	});

	it('hides relationship options when adding inside a grouping container', async () => {
		renderDropdown(CONTAINER_UUID);

		await userEvent.click(screen.getByLabelText('add-field'));

		expect(screen.getByText('email')).toBeInTheDocument();
		expect(
			screen.queryByText('referenced-content-structure')
		).not.toBeInTheDocument();
		expect(
			screen.queryByText('select-related-content')
		).not.toBeInTheDocument();
	});
});
