/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import MapPIMChannelField from '../../src/main/resources/META-INF/resources/js/MapPIMChannelField';

const mockFetch = jest.fn();
const mockNavigate = jest.fn();

jest.mock('frontend-js-web', () => ({
	fetch: (...args: unknown[]) => mockFetch(...args),
	navigate: (...args: unknown[]) => mockNavigate(...args),
	sub: (template: string) => template,
}));

jest.mock('@liferay/site-cms-site-initializer', () => {
	const Toolbar = ({children, title}: any) => (
		<div>
			<h1>{title}</h1>

			{children}
		</div>
	);

	Toolbar.Item = ({children}: any) => <div>{children}</div>;

	return {
		RequiredMark: () => <span>required</span>,
		Toolbar,
		VerticalNavLayout: ({items}: any) => (
			<div>
				{items.map(({component, id}: any) => (
					<div key={id}>{component}</div>
				))}
			</div>
		),
	};
});

const PROPS = {
	allStructuresObjectFields: [
		{label: 'Code', name: 'code'},
		{label: 'Name', name: 'name'},
	],
	apiURL: '/o/pim/connectors',
	backURL: '/web/pim/field-mapping?objectEntryId=42',
	channelField: 'skus[].sku',
	fieldMapping: {name: [{attribute: 'name', source: ''}]},
	mappings: [{attribute: 'code', source: ''}],
	objectEntryId: 42,
	pimObjectDefinitions: [
		{
			label: 'Base SKU',
			name: 'PIMBaseSku',
			objectFields: [{label: 'Code', name: 'code'}],
		},
	],
	spritemap: '/icons.svg',
	title: 'Edit skus[].sku',
};

describe('MapPIMChannelField', () => {
	beforeEach(() => {
		jest.clearAllMocks();

		mockFetch.mockResolvedValue({ok: true});
	});

	it('preselects the attribute already mapped to the channel field', () => {
		render(<MapPIMChannelField {...PROPS} />);

		expect(screen.getByLabelText('attribute')).toHaveValue('code');
	});

	it('drops a mapping whose attribute no longer exists', () => {
		render(
			<MapPIMChannelField
				{...PROPS}
				mappings={[{attribute: 'removedField', source: ''}]}
			/>
		);

		expect(screen.getByLabelText('attribute')).toHaveValue('');
	});

	it('saves the channel field mapping without losing the other ones', async () => {
		render(<MapPIMChannelField {...PROPS} />);

		await userEvent.click(screen.getByText('save'));

		await waitFor(() => expect(mockFetch).toHaveBeenCalled());

		const [url, {body, method}] = mockFetch.mock.calls[0];

		expect(url).toBe('/o/pim/connectors/42');
		expect(method).toBe('PATCH');

		expect(JSON.parse(JSON.parse(body).fieldMapping)).toEqual({
			'name': [{attribute: 'name', source: ''}],
			'skus[].sku': [{attribute: 'code', source: '', type: 'attribute'}],
		});

		expect(mockNavigate).toHaveBeenCalledWith(PROPS.backURL);
	});

	it('removes the channel field from the mapping when nothing is selected', async () => {
		render(
			<MapPIMChannelField
				{...PROPS}
				fieldMapping={{
					'name': [{attribute: 'name', source: ''}],
					'skus[].sku': [{attribute: 'code', source: ''}],
				}}
				mappings={[{attribute: '', source: ''}]}
			/>
		);

		await userEvent.click(screen.getByText('save'));

		await waitFor(() => expect(mockFetch).toHaveBeenCalled());

		const [, {body}] = mockFetch.mock.calls[0];

		expect(JSON.parse(JSON.parse(body).fieldMapping)).toEqual({
			name: [{attribute: 'name', source: ''}],
		});
	});

	it('keeps a fixed value mapping when the page is reopened', () => {
		render(
			<MapPIMChannelField
				{...PROPS}
				mappings={[
					{
						attribute: '',
						source: '',
						type: 'fixedValue',
						value: '12345',
					},
				]}
			/>
		);

		expect(screen.getByLabelText('value')).toHaveValue('12345');
	});

	it('saves a fixed value without an attribute', async () => {
		render(
			<MapPIMChannelField
				{...PROPS}
				mappings={[
					{
						attribute: '',
						source: '',
						type: 'fixedValue',
						value: '12345',
					},
				]}
			/>
		);

		await userEvent.click(screen.getByText('save'));

		await waitFor(() => expect(mockFetch).toHaveBeenCalled());

		const [, {body}] = mockFetch.mock.calls[0];

		expect(JSON.parse(JSON.parse(body).fieldMapping)).toEqual({
			'name': [{attribute: 'name', source: ''}],
			'skus[].sku': [{type: 'fixedValue', value: '12345'}],
		});
	});

	it('drops a fixed value mapping left empty', async () => {
		render(
			<MapPIMChannelField
				{...PROPS}
				fieldMapping={{
					'name': [{attribute: 'name', source: ''}],
					'skus[].sku': [{type: 'fixedValue', value: '12345'}],
				}}
				mappings={[
					{attribute: '', source: '', type: 'fixedValue', value: ''},
				]}
			/>
		);

		await userEvent.click(screen.getByText('save'));

		await waitFor(() => expect(mockFetch).toHaveBeenCalled());

		const [, {body}] = mockFetch.mock.calls[0];

		expect(JSON.parse(JSON.parse(body).fieldMapping)).toEqual({
			name: [{attribute: 'name', source: ''}],
		});
	});

	it('warns the user when saving fails', async () => {
		mockFetch.mockResolvedValue({ok: false});

		render(<MapPIMChannelField {...PROPS} />);

		await userEvent.click(screen.getByText('save'));

		await waitFor(() =>
			expect(Liferay.Util.openToast).toHaveBeenCalledWith({
				message: 'an-unexpected-error-occurred',
				type: 'danger',
			})
		);

		expect(mockNavigate).not.toHaveBeenCalled();
	});

	it('goes back without saving when the user cancels', async () => {
		render(<MapPIMChannelField {...PROPS} />);

		await userEvent.click(screen.getByText('cancel'));

		expect(mockFetch).not.toHaveBeenCalled();
		expect(mockNavigate).toHaveBeenCalledWith(PROPS.backURL);
	});
});
