/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import '@testing-library/jest-dom';

import RecipientUserAutocomplete from '../../src/main/resources/META-INF/resources/js/RecipientUserAutocomplete';

const USER = {
	emailAddress: 'test1@liferay.com',
	name: 'Test Test',
	userId: 1,
};

function renderRecipientUserAutocomplete(props = {}) {
	const onSelectUser = jest.fn();

	const result = render(
		<RecipientUserAutocomplete
			id="recipients[0].emailAddress"
			label="recipient-email"
			onSelectUser={onSelectUser}
			searchUsers={() => Promise.resolve([USER])}
			{...props}
		/>
	);

	return {...result, onSelectUser};
}

describe('RecipientUserAutocomplete', () => {
	afterAll(() => {
		delete (window as any).ResizeObserver;
	});

	beforeAll(() => {
		(window as any).ResizeObserver = jest.fn().mockImplementation(() => ({
			disconnect: jest.fn(),
			observe: jest.fn(),
			unobserve: jest.fn(),
		}));
	});

	it('clears an email that matches no user when the field is left', async () => {
		const {onSelectUser} = renderRecipientUserAutocomplete();

		const input = screen.getByLabelText(/recipient-email/);

		await userEvent.type(input, 'test');
		await userEvent.click(
			await screen.findByText('Test Test (test1@liferay.com)')
		);
		await userEvent.type(input, 'x');
		await userEvent.tab();

		expect(onSelectUser).toHaveBeenLastCalledWith({
			emailAddress: '',
			name: '',
		});
		expect(input).toHaveValue('');
	});

	it('clears the selected user when the email is edited', async () => {
		const {onSelectUser} = renderRecipientUserAutocomplete({
			value: 'test1@liferay.com',
		});

		const input = screen.getByLabelText(/recipient-email/);

		await userEvent.type(input, 'x');

		expect(onSelectUser).toHaveBeenCalledWith({emailAddress: '', name: ''});
		expect(onSelectUser).toHaveBeenCalledTimes(1);
		expect(input).toHaveValue('test1@liferay.comx');
	});

	it('keeps the user chosen from the list', async () => {
		const {onSelectUser} = renderRecipientUserAutocomplete();

		const input = screen.getByLabelText(/recipient-email/);

		await userEvent.type(input, 'test');
		await userEvent.click(
			await screen.findByText('Test Test (test1@liferay.com)')
		);

		expect(onSelectUser).toHaveBeenCalledWith(USER);
		expect(onSelectUser).toHaveBeenCalledTimes(1);
		expect(input).toHaveValue(USER.emailAddress);
	});

	it('keeps the user chosen with the keyboard', async () => {
		const {onSelectUser} = renderRecipientUserAutocomplete();

		const input = screen.getByLabelText(/recipient-email/);

		await userEvent.type(input, 'test');
		await screen.findByText('Test Test (test1@liferay.com)');
		await userEvent.keyboard('{ArrowDown}{Enter}');

		expect(onSelectUser).toHaveBeenCalledWith(USER);
		expect(onSelectUser).toHaveBeenCalledTimes(1);
		expect(input).toHaveValue(USER.emailAddress);
	});

	it('searches the users by the typed text', async () => {
		const searchUsers = jest.fn(() => Promise.resolve([]));

		renderRecipientUserAutocomplete({searchUsers});

		await userEvent.type(
			screen.getByLabelText(/recipient-email/),
			'test user'
		);

		await waitFor(() =>
			expect(searchUsers).toHaveBeenLastCalledWith('test user')
		);
	});

	it('leaves out the users excluded from the list', async () => {
		renderRecipientUserAutocomplete({excludedUserIds: [USER.userId]});

		await userEvent.type(screen.getByLabelText(/recipient-email/), 'test');

		expect(await screen.findByText('no-results-found')).toBeInTheDocument();
	});

	it('updates the email when the value changes', () => {
		const {rerender} = renderRecipientUserAutocomplete({
			value: 'test1@liferay.com',
		});

		rerender(
			<RecipientUserAutocomplete
				id="recipients[0].emailAddress"
				label="recipient-email"
				onSelectUser={jest.fn()}
				searchUsers={() => Promise.resolve([])}
				value="test2@liferay.com"
			/>
		);

		expect(screen.getByLabelText(/recipient-email/)).toHaveValue(
			'test2@liferay.com'
		);
	});
});
