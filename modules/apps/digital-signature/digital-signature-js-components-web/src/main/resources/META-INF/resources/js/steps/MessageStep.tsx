/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {sub} from 'frontend-js-web';
import React from 'react';

import Input from '../Input';
import {
	DigitalSignatureRequestErrors,
	DigitalSignatureRequestValues,
	StepConfig,
	StepProps,
} from '../types';
import {maxLength, required, validate} from '../validation';

const MAX_LENGTH = {
	EMAIL_MESSAGE: 10000,
};

export function validateMessageStep(
	values: DigitalSignatureRequestValues,
	{envelopeNameEnabled}: StepConfig
): DigitalSignatureRequestErrors {
	return validate(
		{
			emailMessage: [
				(value) => maxLength(value, MAX_LENGTH.EMAIL_MESSAGE),
			],
			emailSubject: [(value) => maxLength(value, 100), required],
			...(envelopeNameEnabled && {
				envelopeName: [
					(value: string) => maxLength(value, 100),
					required,
				],
			}),
		},
		values
	);
}

function MessageStep({
	envelopeNameEnabled,
	errors,
	expirationEnabled,
	handleChange,
	showErrors,
	values,
}: StepProps) {
	return (
		<>
			{envelopeNameEnabled && (
				<Input
					error={showErrors ? errors.envelopeName : undefined}
					label={Liferay.Language.get('envelope-name')}
					name="envelopeName"
					onChange={handleChange}
					placeholder={Liferay.Language.get('my-envelope-name')}
					required
					value={values.envelopeName}
				/>
			)}

			<Input
				error={showErrors ? errors.emailSubject : undefined}
				label={Liferay.Language.get('email-subject')}
				name="emailSubject"
				onChange={handleChange}
				placeholder={Liferay.Language.get('please-sign-this-document')}
				required
				value={values.emailSubject}
			/>

			<Input
				error={showErrors ? errors.emailMessage : undefined}
				feedbackMessage={sub(
					Liferay.Language.get('x-characters-remaining'),
					MAX_LENGTH.EMAIL_MESSAGE - values.emailMessage.length
				)}
				label={Liferay.Language.get('email-message')}
				maxLength={MAX_LENGTH.EMAIL_MESSAGE + 1}
				name="emailMessage"
				onChange={handleChange}
				placeholder={Liferay.Language.get('email-message')}
				type="textarea"
				value={values.emailMessage}
			/>

			{expirationEnabled && (
				<Input
					label={Liferay.Language.get('days-until-expiration')}
					min="0"
					name="expireAfter"
					onChange={handleChange}
					type="number"
					value={values.expireAfter}
				/>
			)}
		</>
	);
}

export default MessageStep;
