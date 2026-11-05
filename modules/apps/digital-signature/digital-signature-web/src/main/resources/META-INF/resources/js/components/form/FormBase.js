/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayInput} from '@clayui/form';
import classNames from 'classnames';
import {FieldBase} from 'frontend-js-components-web';
import React from 'react';

const Input = ({
	className,
	disabled,
	error,
	feedbackMessage,
	label,
	name,
	required,
	type = 'text',
	...otherProps
}) => (
	<FieldBase
		className={classNames('w-100', className)}
		disabled={disabled}
		errorMessage={error}
		helpMessage={feedbackMessage}
		id={name}
		label={label}
		required={required}
	>
		<ClayInput
			{...otherProps}
			component={type === 'textarea' ? 'textarea' : 'input'}
			disabled={disabled}
			id={name}
			name={name}
			type={type}
		/>
	</FieldBase>
);

export default Input;
