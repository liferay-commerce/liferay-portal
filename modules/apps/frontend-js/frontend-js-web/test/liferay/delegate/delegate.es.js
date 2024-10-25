/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {findByTestId} from '@testing-library/dom';

import delegate from '../../../src/main/resources/META-INF/resources/liferay/delegate/delegate.es';

describe('delegate', () => {
	const container = document.createElement('div');

	document.body.appendChild(container);
	afterEach(() => {
		container.innerHTML = '';
	});

	it('triggers delegate listener for matched elements', async () => {
		container.innerHTML =
			'<div class="nomatch" data-testid="nomatch"></div>' +
			'<div class="match" data-testid="match"></div>';

		const listener = jest.fn();

		delegate(container, 'click', '.match', listener);

		const noMatchElement = await findByTestId(document, 'nomatch');
		const matchElement = await findByTestId(document, 'match');

		noMatchElement.click();

		expect(listener).not.toHaveBeenCalled();

		matchElement.click();

		expect(listener).toHaveBeenCalled();
	});

	it('triggers event on the parent if target is the child of the selector element', async () => {
		container.innerHTML = `<div class="match" data-testid="match">
				<div>
					<div data-testid="most-inner-match"></div>
				</div>
			</div >`;

		const listener = jest.fn();

		delegate(document, 'click', '.match', listener);

		const mostInnerMatchEl = await findByTestId(
			document,
			'most-inner-match'
		);

		mostInnerMatchEl.click();

		expect(listener).toHaveBeenCalled();
	});

	it('stops triggering event if stopPropagation is called', async () => {
		container.innerHTML = `<div class="match" data-testid="match"></div>`;

		const listener = jest.fn();

		delegate(document, 'click', '.match', listener);

		const matchEl = await findByTestId(document, 'match');

		expect(listener).not.toHaveBeenCalled();

		matchEl.click();

		expect(listener).toHaveBeenCalledTimes(1);

		document
			.querySelector('.match')
			.addEventListener('click', (event) => event.stopPropagation());

		matchEl.click();

		expect(listener).toHaveBeenCalledTimes(1);
	});

	it('only triggers delegate event at initial target', async () => {
		container.innerHTML = `<div>
			<div class="match" data-testid="match">
				<div class="match" data-testid="match2"></div>
			</div>
		</div>`;

		const listener = jest.fn();

		delegate(document, 'click', '.match', listener);

		const matchEl = await findByTestId(document, 'match');
		const matchEl2 = await findByTestId(document, 'match');

		matchEl.click();

		expect(listener).toHaveBeenCalledTimes(1);

		matchEl2.click();

		expect(listener).toHaveBeenCalledTimes(2);
	});

	it('triggers listener twice when two ancestors are delegating', async () => {
		container.innerHTML = `<div>
			<div>
				<div class="match" data-testid="match"></div>
			</div>
		</div>`;

		const listener = jest.fn();

		delegate(document, 'click', '.match', listener);
		delegate(container, 'click', '.match', listener);

		const matchEl = await findByTestId(document, 'match');

		matchEl.click();

		expect(listener).toHaveBeenCalledTimes(2);
	});

	it('removes listener through via `.dispose`', async () => {
		container.innerHTML = `<div class="nomatch" data-testid="nomatch"></div>
			<div class="match" data-testid="match"></div>`;

		const listener = jest.fn();

		const eventHandler = delegate(document, 'click', '.match', listener);

		const matchEl = await findByTestId(document, 'match');

		matchEl.click();

		expect(listener).toHaveBeenCalledTimes(1);

		eventHandler.dispose();

		matchEl.click();

		expect(listener).toHaveBeenCalledTimes(1);
	});

	it("doesn't run click event listeners for disabled elements", async () => {
		container.innerHTML = `<div class="root">
			<button disabled class="match" data-testid="match"></button>
		</div>`;

		const listener = jest.fn();

		delegate(document, 'click', '.match', listener);

		const matchEl = await findByTestId(document, 'match');

		matchEl.click();

		expect(listener).not.toHaveBeenCalled();
	});

	it("doesn't run click event listeners for elements with a disabled parent", async () => {
		container.innerHTML = `<button disabled class="root">
			<div class="match" data-testid="match"></div>
		</button>`;

		const listener = jest.fn();

		delegate(document, 'click', '.match', listener);

		const matchEl = await findByTestId(document, 'match');

		matchEl.click();

		expect(listener).not.toHaveBeenCalled();
	});

	it('gives access to delegateTarget inside the callback', async () => {
		container.innerHTML = `<div class="match" data-testid="match">
				<div>
					<div data-testid="most-inner-match"></div>
				</div>
			</div >`;

		let delegateTarget;

		const listener = (event) => {
			delegateTarget = event.delegateTarget;
		};

		delegate(document, 'click', '.match', listener);

		const matchEl = await findByTestId(document, 'most-inner-match');

		matchEl.click();

		expect(delegateTarget).toBe(document.querySelector('.match'));
	});

	it('works with comma-delimited selector lists', async () => {
		container.innerHTML = `<div class="match" data-testid="match">
				<div>
					<div class="one" data-testid="one"></div>
					<div class="two" data-testid="two"></div>
				</div>
			</div >`;

		const listener = jest.fn();

		const selector = '.one,.two';

		delegate(container, 'mouseover', selector, listener);

		const event = new Event('mouseover', {
			bubbles: true,
			cancelable: true,
		});

		let element = await findByTestId(document, 'one');
		element.dispatchEvent(event);

		expect(listener).toHaveBeenCalledTimes(1);

		element = await findByTestId(document, 'two');
		element.dispatchEvent(event);

		expect(listener).toHaveBeenCalledTimes(2);
	});

	it('sets delegateTarget when using a selector list', async () => {
		container.innerHTML = `<div class="match" data-testid="match">
				<div>
					<div class="one" data-testid="one"></div>
					<div class="two" data-testid="two"></div>
				</div>
			</div >`;

		let delegateTarget;

		const listener = (event) => {
			delegateTarget = event.delegateTarget;
		};

		const selector = '.one,.two';

		delegate(container, 'click', selector, listener);

		const matchElOne = await findByTestId(document, 'one');
		const matchElTwo = await findByTestId(document, 'two');

		matchElOne.click();

		expect(delegateTarget).toBe(document.querySelector('.one'));

		matchElTwo.click();

		expect(delegateTarget).toBe(document.querySelector('.two'));
	});
});
