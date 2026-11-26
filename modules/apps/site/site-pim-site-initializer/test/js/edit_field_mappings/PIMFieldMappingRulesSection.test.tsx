/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import PIMFieldMappingRulesSection from '../../../src/main/resources/META-INF/resources/js/edit_field_mappings/PIMFieldMappingRulesSection';

const FIELD_MAPPING_RULE = {
	id: 0,
	stripHTMLTags: false,
	textCase: '',
	trimWhitespace: false,
	truncateLength: 0,
	urlEncode: false,
	useFirstNonemptySource: false,
};

describe('PIMFieldMappingRulesSection', () => {
	it('disables the rules of a channel field that is not text', () => {
		render(
			<PIMFieldMappingRulesSection
				disabled={true}
				fieldMappingRule={FIELD_MAPPING_RULE}
				onChange={jest.fn()}
				useFirstNonemptySourceSupported={true}
			/>
		);

		expect(
			screen.getByText(
				'mapping-rules-are-not-supported-for-this-field-type'
			)
		).toBeInTheDocument();

		expect(screen.getByLabelText('case')).toBeDisabled();
		expect(screen.getByLabelText('fallback-logic')).toBeDisabled();
		expect(screen.getByLabelText('strip-html-tags')).toBeDisabled();
		expect(screen.getByLabelText('trim-whitespace')).toBeDisabled();
		expect(screen.getByLabelText('truncate')).toBeDisabled();
		expect(screen.getByLabelText('url-encode')).toBeDisabled();
	});

	it('hides the fallback of a channel field that does not support it', () => {
		render(
			<PIMFieldMappingRulesSection
				fieldMappingRule={FIELD_MAPPING_RULE}
				onChange={jest.fn()}
				useFirstNonemptySourceSupported={false}
			/>
		);

		expect(
			screen.queryByLabelText('fallback-logic')
		).not.toBeInTheDocument();
	});

	it('changes the rule a control sets', async () => {
		const onChange = jest.fn();

		render(
			<PIMFieldMappingRulesSection
				fieldMappingRule={FIELD_MAPPING_RULE}
				onChange={onChange}
				useFirstNonemptySourceSupported={true}
			/>
		);

		await userEvent.click(screen.getByLabelText('trim-whitespace'));

		expect(onChange).toHaveBeenLastCalledWith({
			...FIELD_MAPPING_RULE,
			trimWhitespace: true,
		});

		await userEvent.click(screen.getByLabelText('url-encode'));

		expect(onChange).toHaveBeenLastCalledWith({
			...FIELD_MAPPING_RULE,
			urlEncode: true,
		});

		await userEvent.selectOptions(
			screen.getByLabelText('fallback-logic'),
			'true'
		);

		expect(onChange).toHaveBeenLastCalledWith({
			...FIELD_MAPPING_RULE,
			useFirstNonemptySource: true,
		});
	});
});
