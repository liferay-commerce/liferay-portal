/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {getProductMaxQuantity} from '../../src/main/resources/META-INF/resources/utilities/quantities';

describe('getProductMaxQuantity', () => {
	it('returns an empty value when no maximum order quantity is set', () => {
		expect(getProductMaxQuantity(0, 4)).toBe('');
	});

	it('returns the maximum quantity when it is already a multiple of the multiple quantity', () => {
		expect(getProductMaxQuantity(12, 4)).toBe('12');
	});

	it('returns the maximum quantity when it is lower than the multiple order quantity', () => {
		expect(getProductMaxQuantity(4, 5)).toBe('4');
	});

	it('rounds the maximum down to the previous multiple when the maximum order quantity is not a multiple of the multiple order quantity', () => {
		expect(getProductMaxQuantity(10, 4)).toBe('8');
	});
});
