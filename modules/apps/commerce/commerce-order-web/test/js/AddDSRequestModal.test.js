/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import '@testing-library/jest-dom';

import AddDSRequestModal from '../../src/main/resources/META-INF/resources/js/AddDSRequestModal';

const addDSRequestURL = 'http://localhost/add';

const buyer = {
	emailAddress: 'buyer@liferay.com',
	name: 'Buyer',
	userId: 1,
};

function renderAddDSRequestModal() {
	render(
		<AddDSRequestModal
			addDSRequestModalProps={{
				accountEntryUsers: [
					buyer,
					{
						emailAddress: 'other@liferay.com',
						name: 'Other',
						userId: 2,
					},
				],
				buyer,
				commerceOrderId: 1,
				getDSRequestRecipientUsersURL: 'http://localhost/search',
				portletNamespace: '_namespace_',
			}}
			addDSRequestURL={addDSRequestURL}
			attachment={{
				extension: 'pdf',
				fileEntryId: 1,
				title: 'contract.pdf',
			}}
			closeModal={jest.fn()}
		/>
	);
}

describe('AddDSRequestModal', () => {
	afterAll(() => {
		delete window.ResizeObserver;
	});

	beforeAll(() => {
		window.ResizeObserver = jest.fn().mockImplementation(() => ({
			disconnect: jest.fn(),
			observe: jest.fn(),
			unobserve: jest.fn(),
		}));
	});

	beforeEach(() => {
		global.fetch.mockResolvedValue({json: () => Promise.resolve([])});
	});

	it('does not keep the selected user after the email is edited', async () => {
		renderAddDSRequestModal();

		await userEvent.type(screen.getByLabelText(/recipient-email/), 'x');

		await userEvent.click(screen.getByText('next'));

		expect(screen.getByText('this-field-is-required')).toBeInTheDocument();
		expect(screen.getByLabelText(/recipient-full-name/)).toHaveValue('');
	});

	it('keeps the prefilled buyer when the email field gains and loses focus', async () => {
		renderAddDSRequestModal();

		const emailInput = screen.getByLabelText(/recipient-email/);

		await userEvent.click(emailInput);
		await userEvent.tab();

		expect(emailInput).toHaveValue('buyer@liferay.com');
		expect(screen.getByLabelText(/recipient-full-name/)).toHaveValue(
			'Buyer'
		);
	});

	it('labels users outside the account as countersigners', async () => {
		global.fetch.mockResolvedValue({
			json: () =>
				Promise.resolve([
					{
						emailAddress: 'agent@liferay.com',
						name: 'Agent',
						userId: 3,
					},
				]),
		});

		renderAddDSRequestModal();

		await userEvent.clear(screen.getByLabelText(/recipient-email/));
		await userEvent.type(
			screen.getByLabelText(/recipient-email/),
			'liferay'
		);

		expect(
			await screen.findByText('Agent (agent@liferay.com)')
		).toBeInTheDocument();
		expect(screen.getByText('countersigner')).toBeInTheDocument();
		expect(
			screen.getByText('Other (other@liferay.com)')
		).toBeInTheDocument();
	});

	it('limits the request to one countersigner', async () => {
		global.fetch.mockResolvedValue({
			json: () =>
				Promise.resolve([
					{
						emailAddress: 'agent1@liferay.com',
						name: 'Agent 1',
						userId: 3,
					},
					{
						emailAddress: 'agent2@liferay.com',
						name: 'Agent 2',
						userId: 4,
					},
				]),
		});

		renderAddDSRequestModal();

		await userEvent.click(screen.getByText('add-recipient'));

		fireEvent.change(screen.getAllByLabelText(/recipient-email/)[1], {
			target: {value: 'liferay'},
		});

		await userEvent.click(
			await screen.findByText('Agent 1 (agent1@liferay.com)')
		);
		await userEvent.click(screen.getByText('add-recipient'));

		fireEvent.change(screen.getAllByLabelText(/recipient-email/)[2], {
			target: {value: 'liferay'},
		});

		expect(
			await screen.findByText('Other (other@liferay.com)')
		).toBeInTheDocument();
		expect(
			screen.queryByText('Agent 2 (agent2@liferay.com)')
		).not.toBeInTheDocument();
	});

	it('sends the recipient user IDs and the signing order', async () => {
		renderAddDSRequestModal();

		await userEvent.click(screen.getByText('add-recipient'));

		fireEvent.change(screen.getAllByLabelText(/recipient-email/)[1], {
			target: {value: 'other'},
		});

		await userEvent.click(
			await screen.findByText('Other (other@liferay.com)')
		);
		await userEvent.click(screen.getByText('next'));
		await userEvent.type(
			screen.getByPlaceholderText('please-sign-this-document'),
			'Email Subject'
		);
		await userEvent.click(screen.getByText('next'));
		await userEvent.click(screen.getByText('send'));

		await waitFor(() =>
			expect(global.fetch).toHaveBeenCalledWith(
				addDSRequestURL,
				expect.anything()
			)
		);

		const [, {body}] = global.fetch.mock.calls.find(
			([resource]) => resource === addDSRequestURL
		);

		expect(body.getAll('_namespace_recipientUserIds')).toEqual(['1', '2']);
		expect(body.get('_namespace_sequential')).toBe('true');
	});
});
