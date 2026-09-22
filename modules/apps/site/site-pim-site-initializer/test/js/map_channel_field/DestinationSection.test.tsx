/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React, {useState} from 'react';

import DestinationSection from '../../../src/main/resources/META-INF/resources/js/map_channel_field/DestinationSection';
import {IMapping} from '../../../src/main/resources/META-INF/resources/js/map_channel_field/types';

jest.mock('@liferay/site-cms-site-initializer', () => ({
	RequiredMark: () => <span>required</span>,
}));

const ALL_STRUCTURES_OBJECT_FIELDS = [
	{label: 'Code', name: 'code'},
	{label: 'Name', name: 'name'},
];

const PIM_OBJECT_DEFINITIONS = [
	{
		label: 'Base SKU',
		name: 'PIMBaseSku',
		objectFields: [{label: 'Code', name: 'code'}],
	},
];

function DestinationSectionWrapper({
	initialMappings,
}: {
	initialMappings: IMapping[];
}) {
	const [mappings, setMappings] = useState<IMapping[]>(initialMappings);

	return (
		<DestinationSection
			allStructuresObjectFields={ALL_STRUCTURES_OBJECT_FIELDS}
			mappings={mappings}
			pimObjectDefinitions={PIM_OBJECT_DEFINITIONS}
			setMappings={setMappings}
			spritemap="/icons.svg"
		/>
	);
}

describe('DestinationSection', () => {
	it('offers every structure field when the source is all structures', () => {
		render(
			<DestinationSectionWrapper
				initialMappings={[{attribute: '', source: ''}]}
			/>
		);

		expect(
			screen.getByRole('option', {name: 'Code (code)'})
		).toBeInTheDocument();
		expect(
			screen.getByRole('option', {name: 'Name (name)'})
		).toBeInTheDocument();
	});

	it('narrows the attributes to the selected structure', async () => {
		render(
			<DestinationSectionWrapper
				initialMappings={[{attribute: '', source: ''}]}
			/>
		);

		await userEvent.selectOptions(
			screen.getByLabelText('source'),
			'PIMBaseSku'
		);

		expect(
			screen.getByRole('option', {name: 'Code (code)'})
		).toBeInTheDocument();
		expect(
			screen.queryByRole('option', {name: 'Name (name)'})
		).not.toBeInTheDocument();
	});

	it('asks for a value when the attribute is a fixed one', async () => {
		render(
			<DestinationSectionWrapper
				initialMappings={[{attribute: '', source: ''}]}
			/>
		);

		expect(screen.queryByLabelText('value')).not.toBeInTheDocument();

		await userEvent.selectOptions(
			screen.getByLabelText('attribute'),
			'fixedValue'
		);

		expect(screen.getByLabelText('value')).toBeInTheDocument();
		expect(screen.getByLabelText('source')).toBeDisabled();
	});

	it('keeps the typed fixed value', async () => {
		render(
			<DestinationSectionWrapper
				initialMappings={[
					{attribute: '', source: '', type: 'fixedValue', value: ''},
				]}
			/>
		);

		await userEvent.type(screen.getByLabelText('value'), '12345');

		expect(screen.getByLabelText('value')).toHaveValue('12345');
	});

	it('adds a mapping row', async () => {
		render(
			<DestinationSectionWrapper
				initialMappings={[{attribute: '', source: ''}]}
			/>
		);

		await userEvent.click(screen.getByLabelText('add-row'));

		expect(screen.getAllByLabelText('source')).toHaveLength(2);
	});

	it('keeps the last row undeletable', () => {
		render(
			<DestinationSectionWrapper
				initialMappings={[{attribute: 'code', source: ''}]}
			/>
		);

		expect(screen.getByLabelText('delete-row')).toBeDisabled();
	});

	it('deletes a mapping row', async () => {
		render(
			<DestinationSectionWrapper
				initialMappings={[
					{attribute: 'code', source: ''},
					{attribute: 'name', source: ''},
				]}
			/>
		);

		await userEvent.click(screen.getAllByLabelText('delete-row')[0]);

		expect(screen.getAllByLabelText('source')).toHaveLength(1);
	});
});
