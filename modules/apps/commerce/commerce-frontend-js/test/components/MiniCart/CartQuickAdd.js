/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {act, render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import '@testing-library/jest-dom';

import CartQuickAdd from '../../../src/main/resources/META-INF/resources/components/mini_cart/CartQuickAdd';
import MiniCartContext from '../../../src/main/resources/META-INF/resources/components/mini_cart/MiniCartContext';

describe('MiniCart CartQuickAdd', () => {
	const CONTEXT_MOCK = {
		cartState: {
			accountId: 1,
			cartItems: [],
			channel: {channel: {id: 2}},
			id: 3,
		},
		guestOrderEnabled: false,
	};

	const SKUS = ['MIN55859', 'MIN55860', 'MIN55861'];

	const PRODUCTS_RESPONSE = {
		items: [
			{
				name: 'Brake Pads',
				productConfiguration: {},
				skus: SKUS.map((sku, index) => ({
					id: index + 1,
					purchasable: true,
					sku,
				})),
				urls: {},
			},
		],
		lastPage: 1,
		page: 1,
		pageSize: 100,
		totalCount: SKUS.length,
	};

	beforeEach(() => {
		jest.useFakeTimers();
	});

	afterEach(() => {
		jest.useRealTimers();
	});

	it('removes the selected SKU chips one by one', async () => {
		fetch.mockResponse(JSON.stringify(PRODUCTS_RESPONSE));

		render(
			<MiniCartContext.Provider value={CONTEXT_MOCK}>
				<CartQuickAdd />
			</MiniCartContext.Provider>
		);

		const searchInput = screen.getByPlaceholderText('search-products');

		for (const sku of SKUS) {
			userEvent.type(searchInput, 'MIN');

			await act(async () => {
				jest.advanceTimersByTime(500);
			});

			userEvent.click(await screen.findByText(sku));
		}

		for (const sku of SKUS) {
			expect(
				screen.getByRole('button', {name: `Remove ${sku}`})
			).toBeInTheDocument();
		}

		expect(screen.getByRole('button', {name: 'add-to-cart'})).toBeEnabled();
		expect(
			screen.queryByPlaceholderText('search-products')
		).not.toBeInTheDocument();

		for (const sku of SKUS) {
			userEvent.click(
				screen.getByRole('button', {name: `Remove ${sku}`})
			);

			expect(
				screen.queryByRole('button', {name: `Remove ${sku}`})
			).not.toBeInTheDocument();
		}

		expect(
			screen.getByRole('button', {name: 'add-to-cart'})
		).toBeDisabled();
		expect(screen.getByPlaceholderText('search-products')).toHaveValue('');
	});
});
