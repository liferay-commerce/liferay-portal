/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import AccountValidationResultMessageDataRenderer from '../../src/main/resources/META-INF/resources/js/AccountValidationResultMessageDataRenderer';

describe('AccountValidationResultMessageDataRenderer', () => {
	it('renders the translation of a validator result message', () => {
		const result = AccountValidationResultMessageDataRenderer({
			additionalProps: {
				resultMessages: {
					'the-account-is-missing-a-tax-id-vat-number':
						'The account is missing a Tax ID (VAT number). Orders cannot be placed until an account administrator adds it.',
				},
			},
			value: 'the-account-is-missing-a-tax-id-vat-number',
		});

		expect(result).toBe(
			'The account is missing a Tax ID (VAT number). Orders cannot be placed until an account administrator adds it.'
		);
	});

	it('renders a manual validation note as it was stored', () => {
		const result = AccountValidationResultMessageDataRenderer({
			additionalProps: {
				resultMessages: {
					'account-validation-failed': 'Account validation failed.',
				},
			},
			value: 'Checked the VAT number with the customer by phone.',
		});

		expect(result).toBe(
			'Checked the VAT number with the customer by phone.'
		);
	});

	it('renders the stored value when no translation is available', () => {
		expect(
			AccountValidationResultMessageDataRenderer({
				value: 'account-validation-failed',
			})
		).toBe('account-validation-failed');

		expect(
			AccountValidationResultMessageDataRenderer({
				additionalProps: {},
				value: 'account-validation-failed',
			})
		).toBe('account-validation-failed');
	});
});
