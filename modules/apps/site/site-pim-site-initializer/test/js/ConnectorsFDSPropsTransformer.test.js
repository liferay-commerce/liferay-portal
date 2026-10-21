/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import propsTransformer from '../../src/main/resources/META-INF/resources/js/ConnectorsFDSPropsTransformer';

jest.mock('@liferay/frontend-data-set-web', () => ({
	getItemActionURL: () => null,
}));

jest.mock('@liferay/site-cms-site-initializer', () => ({
	confirmAndDeleteEntryAction: (...args) =>
		mockConfirmAndDeleteEntryAction(...args),
}));

jest.mock('frontend-js-web', () => ({
	sub: (template) => template,
}));

const mockConfirmAndDeleteEntryAction = jest.fn();

Liferay.Util = {...Liferay.Util, escapeHTML: (value) => value};

describe('ConnectorsFDSPropsTransformer', () => {
	beforeEach(() => {
		jest.clearAllMocks();
	});

	it('registers the connector cell renderers', () => {
		const result = propsTransformer({});

		expect(result.customRenderers.tableCell.map(({name}) => name)).toEqual([
			'nameTableCellRenderer',
			'statusTableCellRenderer',
			'lastExecutionTableCellRenderer',
		]);
	});

	it('confirms a delete through the modal rather than the browser dialog', () => {
		const event = {preventDefault: jest.fn()};
		const loadData = jest.fn();

		propsTransformer({}).onActionDropdownItemClick({
			action: {data: {id: 'delete'}},
			event,
			itemData: {
				actions: {
					delete: {href: '/o/c/pimconnectors/1', method: 'DELETE'},
				},
				name: 'My Connector',
			},
			loadData,
		});

		expect(event.preventDefault).toHaveBeenCalled();
		expect(mockConfirmAndDeleteEntryAction).toHaveBeenCalledWith(
			expect.objectContaining({
				deleteAction: {href: '/o/c/pimconnectors/1', method: 'DELETE'},
			})
		);
	});

	it('leaves the other item actions to the data set', () => {
		const event = {preventDefault: jest.fn()};

		propsTransformer({}).onActionDropdownItemClick({
			action: {data: {id: 'edit'}},
			event,
			itemData: {actions: {delete: {href: '', method: ''}}, name: ''},
			loadData: jest.fn(),
		});

		expect(event.preventDefault).not.toHaveBeenCalled();
		expect(mockConfirmAndDeleteEntryAction).not.toHaveBeenCalled();
	});

	it('forces hideManagementBarInEmptyState to true and preserves the other props', () => {
		const result = propsTransformer({
			hideManagementBarInEmptyState: false,
			id: 'connectors',
		});

		expect(result.hideManagementBarInEmptyState).toBe(true);
		expect(result.id).toBe('connectors');
	});
});
