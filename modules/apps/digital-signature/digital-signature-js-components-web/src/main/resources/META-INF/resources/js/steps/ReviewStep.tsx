/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React, {ReactNode} from 'react';

import {StepProps} from '../types';

const RecipientList = ({
	children,
	sequential,
}: {
	children: ReactNode;
	sequential: boolean;
}) =>
	sequential ? (
		<ol className="mb-0 pl-3">{children}</ol>
	) : (
		<ul className="list-unstyled mb-0">{children}</ul>
	);

const ReviewField = ({
	children,
	label,
}: {
	children: ReactNode;
	label: string;
}) => (
	<div className="review-field">
		<label>{label}</label>

		<div className="review-field-value">{children}</div>
	</div>
);

function ReviewStep({expirationEnabled, values}: StepProps) {
	return (
		<div className="digital-signature-review">
			<ReviewField label={Liferay.Language.get('documents')}>
				<ul className="list-unstyled mb-0">
					{values.fileEntries.map(({fileEntryId, title}) => (
						<li key={fileEntryId}>{title}</li>
					))}
				</ul>
			</ReviewField>

			<ReviewField label={Liferay.Language.get('recipients')}>
				<RecipientList sequential={values.sequential}>
					{values.recipients.map((recipient, index) => (
						<li key={index}>
							{`${recipient.name} (${recipient.emailAddress})`}
						</li>
					))}
				</RecipientList>
			</ReviewField>

			<ReviewField label={Liferay.Language.get('envelope-name')}>
				{values.envelopeName}
			</ReviewField>

			<ReviewField label={Liferay.Language.get('email-subject')}>
				{values.emailSubject}
			</ReviewField>

			<ReviewField label={Liferay.Language.get('email-message')}>
				{values.emailMessage}
			</ReviewField>

			{expirationEnabled && (
				<ReviewField
					label={Liferay.Language.get('days-until-expiration')}
				>
					{values.expireAfter}
				</ReviewField>
			)}
		</div>
	);
}

export default ReviewStep;
