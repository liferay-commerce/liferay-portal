/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {fireEvent, render, screen} from '@testing-library/react';
import React from 'react';

import GroupSettings from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/components/settings/GroupSettings';
import {Group} from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/types/Structure';
import getUuid from '../../../../../src/main/resources/META-INF/resources/js/structure_builder/utils/getUuid';
import {MockState, MockStateProvider} from '../../mocks/MockStateProvider';

const GROUP_UUID = getUuid();
const ROOT_UUID = getUuid();

const GROUP: Group = {
	children: new Map(),
	label: {en_US: 'Specifications'},
	parent: ROOT_UUID,
	type: 'group',
	uuid: GROUP_UUID,
};

const DEFAULT_STATE: MockState = {
	invalids: new Map(),
	structure: {
		children: new Map([[GROUP_UUID, GROUP]]),
		erc: 'structure-erc',
		label: {en_US: 'Product'},
		name: 'Product',
		type: 'L_CMS_CONTENT_STRUCTURES',
		uuid: ROOT_UUID,
	},
};

const MOCK_DISPATCH = jest.fn();

const renderComponent = ({
	disabled = false,
	dispatch = MOCK_DISPATCH,
	group = GROUP,
	state = DEFAULT_STATE,
}: {
	disabled?: boolean;
	dispatch?: jest.Mock;
	group?: Group;
	state?: MockState;
} = {}) => {
	return render(
		<MockStateProvider dispatch={dispatch} state={state}>
			<GroupSettings disabled={disabled} group={group} />
		</MockStateProvider>
	);
};

describe('GroupSettings', () => {
	beforeEach(() => {
		jest.clearAllMocks();
	});

	it('labels the field type as a group', () => {
		renderComponent();

		expect(screen.getByText('group')).toHaveClass('label-item');
	});

	it('no longer offers a tab or a panel variant, since the depth decides how a group renders', () => {
		renderComponent();

		expect(screen.queryByText('tab')).not.toBeInTheDocument();
		expect(screen.queryByText('panel')).not.toBeInTheDocument();
	});

	it('shows the group in the breadcrumb, under its structure', () => {
		renderComponent();

		expect(screen.getByText('Product')).toBeInTheDocument();
		expect(screen.getByText('Specifications')).toBeInTheDocument();
	});

	it('dispatches update-group when the label input is saved', () => {
		renderComponent();

		fireEvent.blur(screen.getAllByRole('textbox')[0]);

		expect(MOCK_DISPATCH).toHaveBeenCalledWith({
			label: {en_US: 'Specifications'},
			type: 'update-group',
			uuid: GROUP_UUID,
		});
	});

	it('disables the label input when the group cannot be edited', () => {
		renderComponent({disabled: true});

		expect(screen.getAllByRole('textbox')[0]).toBeDisabled();
	});
});
