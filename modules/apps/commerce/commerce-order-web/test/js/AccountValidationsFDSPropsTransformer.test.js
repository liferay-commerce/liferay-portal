/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import AccountValidationsFDSPropsTransformer from '../../src/main/resources/META-INF/resources/js/AccountValidationsFDSPropsTransformer';

describe('AccountValidationsFDSPropsTransformer', () => {
	it('preserves the other props', () => {
		const result = AccountValidationsFDSPropsTransformer({
			apiURL: '/o/account/validator-results',
			id: 'accountValidations',
		});

		expect(result.apiURL).toBe('/o/account/validator-results');
		expect(result.id).toBe('accountValidations');
	});

	it('registers the data renderer declared by the table schema', () => {
		const result = AccountValidationsFDSPropsTransformer({});

		expect(
			typeof result.customDataRenderers
				.accountValidationResultMessageDataRenderer
		).toBe('function');
	});

	it('hands the result message translations to the data renderer', () => {
		const result = AccountValidationsFDSPropsTransformer({
			additionalProps: {
				resultMessages: {
					'vies-vat-blocked-error':
						'The VAT number is blocked by the member state.',
				},
			},
		});

		const dataRenderer =
			result.customDataRenderers
				.accountValidationResultMessageDataRenderer;

		expect(dataRenderer({value: 'vies-vat-blocked-error'})).toBe(
			'The VAT number is blocked by the member state.'
		);
		expect(dataRenderer({value: 'vies-unexpected-error'})).toBe(
			'vies-unexpected-error'
		);
	});
});
