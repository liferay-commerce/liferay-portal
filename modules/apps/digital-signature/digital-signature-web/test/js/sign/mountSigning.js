/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {waitFor} from '@testing-library/react';

import mountSigning from '../../../src/main/resources/META-INF/resources/js/sign/mountSigning';

const COMPLETE_URL =
	'http://localhost/web/guest/sign?p_p_lifecycle=1&p_auth=token';

describe('mountSigning', () => {
	let pageHideListeners;
	let sessionEndListener;
	let signing;

	beforeEach(() => {
		jest.clearAllMocks();

		pageHideListeners = [];
		sessionEndListener = null;
		signing = {
			mount: jest.fn(),
			on: jest.fn((name, listener) => {
				if (name === 'sessionEnd') {
					sessionEndListener = listener;
				}
			}),
		};

		const addEventListener = window.addEventListener.bind(window);

		jest.spyOn(window, 'addEventListener').mockImplementation(
			(name, listener, options) => {
				if (name === 'pagehide') {
					pageHideListeners.push(listener);
				}

				addEventListener(name, listener, options);
			}
		);

		Liferay.Util.Window = {getById: jest.fn()};

		Object.defineProperty(navigator, 'sendBeacon', {
			configurable: true,
			value: jest.fn(),
		});

		window.DocuSign = {
			loadDocuSign: jest.fn(() =>
				Promise.resolve({signing: () => signing})
			),
		};
		window.opener = {Liferay: {fire: jest.fn()}};
	});

	afterEach(() => {
		pageHideListeners.forEach((listener) =>
			window.removeEventListener('pagehide', listener)
		);

		jest.restoreAllMocks();

		delete Liferay.Util.Window;
		delete navigator.sendBeacon;
		delete window.DocuSign;
		delete window.opener;

		document.head.innerHTML = '';
	});

	async function mount() {
		mountSigning({
			backURL: 'http://localhost/web/guest/home',
			completeURL: COMPLETE_URL,
			containerId: 'signingContainer',
			integrationKey: 'integrationKey',
			jsURL: 'http://localhost/bundle.js',
			signingURL: 'http://localhost/signing',
		});

		document.head.querySelector('script').onload();

		await waitFor(() => expect(signing.mount).toHaveBeenCalled());
	}

	function getBeforeNavigateListener() {
		const [, listener] = Liferay.on.mock.calls.find(
			([name]) => name === 'beforeNavigate'
		);

		return listener;
	}

	it('completes the signing view and stops listening when the session ends', async () => {
		fetch.mockResponseOnce('');

		await mount();

		const beforeNavigateListener = getBeforeNavigateListener();

		sessionEndListener({sessionEndType: 'signing_complete'});

		await waitFor(() =>
			expect(window.opener.Liferay.fire).toHaveBeenCalledWith(
				'closeModal',
				{redirect: 'http://localhost/web/guest/home'}
			)
		);

		expect(Liferay.detach).toHaveBeenCalledWith(
			'beforeNavigate',
			beforeNavigateListener
		);
		expect(fetch.mock.calls[0][0]).toBe(COMPLETE_URL);

		window.dispatchEvent(new Event('pagehide'));

		expect(navigator.sendBeacon).not.toHaveBeenCalled();
	});

	it('sends a beacon to complete the signing view when the signer leaves the page early', async () => {
		await mount();

		window.dispatchEvent(new Event('pagehide'));

		expect(navigator.sendBeacon).toHaveBeenCalledWith(COMPLETE_URL);
	});

	it('sends a beacon to complete the signing view when the signer navigates away early', async () => {
		await mount();

		const beforeNavigateListener = getBeforeNavigateListener();

		beforeNavigateListener();

		expect(Liferay.detach).toHaveBeenCalledWith(
			'beforeNavigate',
			beforeNavigateListener
		);
		expect(navigator.sendBeacon).toHaveBeenCalledWith(COMPLETE_URL);
	});
});
