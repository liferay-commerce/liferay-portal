/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {cleanup, render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import {AppContext} from '../../../../src/main/resources/META-INF/resources/js/AppContext';
import DigitalSignatureForm from '../../../../src/main/resources/META-INF/resources/js/components/digital-signature-form/DigitalSignatureForm';

jest.mock('frontend-js-components-web', () => ({
	...jest.requireActual('frontend-js-components-web'),
	openToast: jest.fn(),
}));

const DigitalSignatureFormWithProvider = (props) => (
	<AppContext.Provider value={{}}>
		<DigitalSignatureForm {...props} />
	</AppContext.Provider>
);

describe('DigitalSignatureForm', () => {
	afterAll(() => {
		delete window.ResizeObserver;
	});

	afterEach(cleanup);

	beforeAll(() => {
		window.ResizeObserver = jest.fn().mockImplementation(() => ({
			disconnect: jest.fn(),
			observe: jest.fn(),
			unobserve: jest.fn(),
		}));
	});

	it('Reveals errors when Next is clicked while incomplete', async () => {
		render(<DigitalSignatureFormWithProvider />);

		await userEvent.click(screen.getByText('next'));

		expect(
			screen.getByText('please-add-at-least-one-document')
		).toBeInTheDocument();
		expect(
			screen.getAllByText('this-field-is-required').length
		).toBeGreaterThan(0);
	});

	it('Shows the email format error after attempting to continue with an invalid email', async () => {
		const {asFragment} = render(<DigitalSignatureFormWithProvider />);

		await userEvent.type(
			screen.getByPlaceholderText('recipient-email'),
			'not-an-email'
		);

		await userEvent.click(screen.getByText('next'));

		expect(
			screen.getByText('please-enter-a-valid-email-address')
		).toBeInTheDocument();
		expect(screen.getByText('this-field-is-required')).toBeInTheDocument();

		expect(asFragment()).toMatchSnapshot();
	});

	it('Restricts recipients to existing users when embedded signing is enabled', () => {
		render(
			<AppContext.Provider value={{embeddedViewEnabled: true}}>
				<DigitalSignatureForm />
			</AppContext.Provider>
		);

		expect(screen.getByPlaceholderText('search-for')).toBeInTheDocument();
		expect(
			screen.queryByPlaceholderText('recipient-email')
		).not.toBeInTheDocument();
		expect(
			screen.getByPlaceholderText('recipient-full-name')
		).toBeDisabled();
	});

	it('Sends the expiration days with the envelope', async () => {
		const navigate = jest.fn();

		fetch.mockImplementation((resource) =>
			Promise.resolve(
				new Response(
					JSON.stringify(
						String(resource).includes('user-accounts')
							? {
									items: [
										{
											emailAddress: 'test1@liferay.com',
											id: 1,
											name: 'Test Test',
										},
									],
								}
							: {dsEnvelopeId: 1}
					)
				)
			)
		);

		render(
			<AppContext.Provider
				value={{
					allowedFileExtensions: ['pdf'],
					baseResourceURL: 'http://localhost:8080?p_p_id=unitTest',
					embeddedViewEnabled: true,
					portletNamespace: '_namespace_',
				}}
			>
				<DigitalSignatureForm
					fileEntries={[
						{
							extension: 'pdf',
							fileEntryId: 1,
							title: 'Document.pdf',
						},
					]}
					navigate={navigate}
				/>
			</AppContext.Provider>
		);

		await userEvent.type(screen.getByLabelText(/recipient-email/), 'test');
		await userEvent.click(
			await screen.findByText('Test Test (test1@liferay.com)')
		);
		await userEvent.click(screen.getByText('next'));
		await userEvent.type(
			screen.getByPlaceholderText('my-envelope-name'),
			'Envelope Name'
		);
		await userEvent.type(
			screen.getByPlaceholderText('please-sign-this-document'),
			'Email Subject'
		);

		const inputExpireAfter = screen.getByLabelText('days-until-expiration');

		await userEvent.clear(inputExpireAfter);
		await userEvent.type(inputExpireAfter, '30');
		await userEvent.click(screen.getByText('next'));
		await userEvent.click(screen.getByText('send'));

		await waitFor(() => expect(navigate).toHaveBeenCalled());

		const [, {body}] = fetch.mock.calls.find(([resource]) =>
			String(resource).includes('add_ds_envelope')
		);

		expect(body.get('_namespace_expireAfter')).toBe('30');
	});

	it('Disables leaving the form while the envelope is being sent', async () => {
		fetch.mockImplementation(() => new Promise(() => {}));

		render(
			<AppContext.Provider
				value={{
					allowedFileExtensions: ['pdf'],
					baseResourceURL: 'http://localhost:8080?p_p_id=unitTest',
					portletNamespace: '_namespace_',
				}}
			>
				<DigitalSignatureForm
					fileEntries={[
						{
							extension: 'pdf',
							fileEntryId: 1,
							title: 'Document.pdf',
						},
					]}
					navigate={jest.fn()}
				/>
			</AppContext.Provider>
		);

		await userEvent.type(
			screen.getByPlaceholderText('recipient-full-name'),
			'Test Test'
		);
		await userEvent.type(
			screen.getByPlaceholderText('recipient-email'),
			'test1@liferay.com'
		);
		await userEvent.click(screen.getByText('next'));
		await userEvent.type(
			screen.getByPlaceholderText('my-envelope-name'),
			'Envelope Name'
		);
		await userEvent.type(
			screen.getByPlaceholderText('please-sign-this-document'),
			'Email Subject'
		);
		await userEvent.click(screen.getByText('next'));
		await userEvent.click(screen.getByText('send'));

		await waitFor(() => expect(screen.getByText('cancel')).toBeDisabled());

		expect(screen.getByText('previous')).toBeDisabled();
		expect(screen.getByText('send')).toBeDisabled();
	});

	it('Hides the move buttons when recipients do not sign in order', async () => {
		render(<DigitalSignatureFormWithProvider />);

		await userEvent.click(screen.getByText('add-recipient'));

		expect(screen.getAllByTitle('move-up')).toHaveLength(2);

		await userEvent.click(
			screen.getByLabelText('recipients-sign-in-the-order-listed')
		);

		expect(screen.queryByTitle('move-up')).not.toBeInTheDocument();
		expect(screen.queryByTitle('move-down')).not.toBeInTheDocument();
	});

	it('Sends the recipients in the order listed', async () => {
		const navigate = jest.fn();

		fetch.mockImplementation(() =>
			Promise.resolve(new Response(JSON.stringify({dsEnvelopeId: 1})))
		);

		render(
			<AppContext.Provider
				value={{
					allowedFileExtensions: ['pdf'],
					baseResourceURL: 'http://localhost:8080?p_p_id=unitTest',
					portletNamespace: '_namespace_',
				}}
			>
				<DigitalSignatureForm
					fileEntries={[
						{
							extension: 'pdf',
							fileEntryId: 1,
							title: 'Document.pdf',
						},
					]}
					navigate={navigate}
				/>
			</AppContext.Provider>
		);

		await userEvent.click(screen.getByText('add-recipient'));

		const fullNameInputs = screen.getAllByPlaceholderText(
			'recipient-full-name'
		);
		const emailInputs = screen.getAllByPlaceholderText('recipient-email');

		await userEvent.type(fullNameInputs[0], 'First Recipient');
		await userEvent.type(emailInputs[0], 'first@liferay.com');
		await userEvent.type(fullNameInputs[1], 'Second Recipient');
		await userEvent.type(emailInputs[1], 'second@liferay.com');
		await userEvent.click(screen.getAllByTitle('move-down')[0]);
		await userEvent.click(screen.getByText('next'));
		await userEvent.type(
			screen.getByPlaceholderText('my-envelope-name'),
			'Envelope Name'
		);
		await userEvent.type(
			screen.getByPlaceholderText('please-sign-this-document'),
			'Email Subject'
		);
		await userEvent.click(screen.getByText('next'));
		await userEvent.click(screen.getByText('send'));

		await waitFor(() => expect(navigate).toHaveBeenCalled());

		const [, {body}] = fetch.mock.calls.find(([resource]) =>
			String(resource).includes('add_ds_envelope')
		);

		expect(
			JSON.parse(body.get('_namespace_recipients')).map(
				({email}) => email
			)
		).toEqual(['second@liferay.com', 'first@liferay.com']);
		expect(body.get('_namespace_sequential')).toBe('true');
	});
});
