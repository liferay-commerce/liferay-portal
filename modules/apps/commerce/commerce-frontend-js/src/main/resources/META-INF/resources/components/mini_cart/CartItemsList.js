/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayIcon from '@clayui/icon';
import React, {useCallback, useContext, useEffect, useState} from 'react';

import CartQuickAdd from './CartQuickAdd';
import MiniCartContext from './MiniCartContext';
import {ADD_PRODUCT} from './util/constants';
import InfiniteScroller from "../infinite_scroller/InfiniteScroller";
import ServiceProvider from "../../ServiceProvider";

const CartResource = ServiceProvider.DeliveryCartAPI('v1');

export default function CartItemsList({showPriceOnApplicationInfo = false}) {
	const {
		CartViews,
		cartState,
		isUpdating,
		labels,
		replacementSKUList,
		setReplacementSKUList,
		setCartState,
		summaryDataMapper,
	} = useContext(MiniCartContext);

	const {accountId, cartItems = [], id: cartId, summary = {}} = cartState;

	const [pages, setPages] = useState({
		page: 1,
		pageSize: 5,
		lastPage: 2,
	});

	const showReplacementAlert = Boolean(replacementSKUList.length);

	const updateReplacedSKUList = useCallback(
		() =>
			cartState.cartItems
				? setReplacementSKUList(
					cartState.cartItems.filter(
						({replacedSku: replacedSKU}) => Boolean(replacedSKU)
					)
				)
				: null,
		[cartState.cartItems]
	);

	const loadItems = useCallback( async (currentPage) => {
		const page = currentPage + 1;

		const {items, lastPage} = await CartResource.getCartItemsByCartId(
			cartId, {page, pageSize: pages.pageSize});

		setCartState((currentState) => ({
			...currentState,
			cartItems: [
				...currentState.cartItems,
				...items,
			],
		}));

		setPages({
			page,
			lastPage,
			pageSize: page.pageSize,
		});
	}, [setPages]);

	useEffect(() => {
		updateReplacedSKUList();
	}, [updateReplacedSKUList]);

	return (
		<div className="mini-cart-items-list">
			<CartViews.ItemsListActions />

			{accountId ? <CartQuickAdd /> : null}

			{showReplacementAlert ? (
				<div className="info-wrapper">
					<ClayAlert
						displayType="info"
						hideCloseIcon
						title={Liferay.Language.get('info')}
					>
						{Liferay.Language.get(
							'there-are-replacement-products-in-your-cart'
						)}
					</ClayAlert>
				</div>
			) : null}

			{showPriceOnApplicationInfo && (
				<div className="info-wrapper">
					<ClayAlert
						displayType="info"
						title={Liferay.Language.get('info')}
					>
						{Liferay.Language.get(
							'your-cart-has-products-that-require-a-quote-to-complete-the-checkout'
						)}
					</ClayAlert>
				</div>
			)}

			{cartItems.length ? (
				<>
						<div className="mini-cart-cart-items">
							<InfiniteScroller
								onBottomTouched={() => loadItems(pages.page)}
								scrollCompleted={pages.page >= pages.lastPage}
							>
							{cartItems.map((currentCartItem, index) => {
								const updateCartItem = (callback) => {
									const updatedCartItem =
										callback(currentCartItem);

									setCartState((cartState) => ({
										...cartState,
										cartItems: cartItems.map((cartItem) =>
											cartItem.id === currentCartItem.id
												? updatedCartItem
												: cartItem
										),
									}));
								};

								return (
									<CartViews.Item
										index={index}
										key={`${currentCartItem.id}`}
										updateCartItem={updateCartItem}
										{...currentCartItem}
									/>
								);
							})}
						</InfiniteScroller>
						</div>

					<CartViews.Summary
						dataMapper={summaryDataMapper}
						isLoading={isUpdating}
						summaryData={summary}
					/>
				</>
			) : (
				<div className="empty-cart">
					<div className="empty-cart-icon mb-3">
						<ClayIcon symbol="shopping-cart" />
					</div>

					<p className="empty-cart-label">{labels[ADD_PRODUCT]}</p>
				</div>
			)}
		</div>
	);
}
