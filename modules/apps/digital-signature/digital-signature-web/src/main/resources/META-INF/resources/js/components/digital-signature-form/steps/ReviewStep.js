/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

const ReviewField = ({children, label}) => (
	<div className="review-field">
		<label>{label}</label>

		<div className="review-field-value">{children}</div>
	</div>
);

function ReviewStep({expirationEnabled, values}) {
	return (
		<div className="digital-signature-review">
			<ReviewField label={Liferay.Language.get('envelope-name')}>
				{values.envelopeName}
			</ReviewField>

			<ReviewField label={Liferay.Language.get('documents')}>
				<ul className="list-unstyled mb-0">
					{values.fileEntries.map(({fileEntryId, title}) => (
						<li key={fileEntryId}>{title}</li>
					))}
				</ul>
			</ReviewField>

			<ReviewField label={Liferay.Language.get('recipients')}>
				<ul className="list-unstyled mb-0">
					{values.recipients.map((recipient, index) => (
						<li key={index}>
							{`${recipient.name} (${recipient.emailAddress})`}
						</li>
					))}
				</ul>
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
