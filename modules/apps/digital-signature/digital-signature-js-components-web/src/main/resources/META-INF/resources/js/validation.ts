/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {sub} from 'frontend-js-web';

import {FileEntry} from './types';

type Validation = (value: string) => string | undefined;

const EMAIL_REGEX =
	/^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

const isEmail = (value: string) =>
	EMAIL_REGEX.test(value)
		? undefined
		: Liferay.Language.get('please-enter-a-valid-email-address');

const maxLength = (value: string, max: number) => {
	if (value.length > max) {
		return sub(
			Liferay.Language.get('this-field-exceeded-x-characters'),
			max
		);
	}
};

const required = (value: string) => {
	if (!value?.trim()) {
		return Liferay.Language.get('this-field-is-required');
	}
};

const validate = (fields: {[key: string]: Validation[]}, values: object) => {
	const errors: {[key: string]: string} = {};

	Object.entries(fields).forEach(([inputName, validations]) => {
		validations.forEach((validation) => {
			const error = validation(
				(values as {[key: string]: string})[inputName]
			);

			if (error) {
				errors[inputName] = error;
			}

			return error;
		});
	});

	return errors;
};

const withInvalidExtensions = (
	fileEntries: FileEntry[],
	availableExtensions: string[]
) => {
	const fileEntriesError = fileEntries.filter(({extension}) =>
		availableExtensions.every(
			(availableExtension) => extension !== availableExtension
		)
	);

	if (fileEntriesError.length) {
		return fileEntriesError;
	}
};

export {isEmail, maxLength, required, validate, withInvalidExtensions};
