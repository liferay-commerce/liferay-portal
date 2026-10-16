/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLoadingIndicator from '@clayui/loading-indicator';
import {fetch} from 'frontend-js-web';
import PropTypes from 'prop-types';
import React, {useEffect, useState} from 'react';

import SignatureStatusLabel, {
	getSignatureStatusDisplayType,
	getSignatureStatusLabel,
} from './SignatureStatusLabel';

import './signature_details.scss';

const SEPARATOR = ` ${String.fromCharCode(183)} `;

function formatDate(time) {
	if (!time) {
		return '';
	}

	return new Date(time).toLocaleString();
}

function getActivities(detail) {
	const activities = [];

	detail.recipients.forEach((recipient) => {
		if (recipient.sentDate) {
			activities.push({
				detail: recipient.name,
				time: recipient.sentDate,
				title: Liferay.Language.get('sent'),
				type: 'info',
			});
		}

		if (recipient.statusDate) {
			const status = getRecipientStatus(recipient);

			activities.push({
				detail: recipient.name,
				time: recipient.statusDate,
				title: getSignatureStatusLabel(status),
				type: getSignatureStatusDisplayType(status),
			});
		}
	});

	if (
		(detail.requestStatus === 'expired' ||
			detail.requestStatus === 'voided') &&
		detail.statusDate
	) {
		activities.push({
			detail: '',
			time: detail.statusDate,
			title: getSignatureStatusLabel(detail.requestStatus),
			type: getSignatureStatusDisplayType(detail.requestStatus),
		});
	}

	activities.sort((a, b) => (a.time || 0) - (b.time || 0));

	return activities;
}

function getRecipientStatus(recipient) {
	if (recipient.requestRecipientStatus === 'completed') {
		return 'signed';
	}

	return recipient.requestRecipientStatus;
}

function SignatureDetailsContent({detail}) {
	const ordered =
		new Set(detail.recipients.map(({signingOrder}) => signingOrder)).size >
		1;

	return (
		<div className="signature-details">
			<div className="bg-light border mb-4 p-3 rounded">
				<div className="small text-secondary text-uppercase">
					{Liferay.Language.get('status')}
				</div>

				<div className="mb-3">
					<SignatureStatusLabel status={detail.requestStatus} />
				</div>

				<div className="small text-secondary text-uppercase">
					{Liferay.Language.get('requester')}
				</div>

				<div className="font-weight-semi-bold mb-3">
					{detail.userName}
				</div>

				<div className="small text-secondary text-uppercase">
					{Liferay.Language.get('envelope-id')}
				</div>

				<div className={detail.expirationDate ? 'mb-3' : ''}>
					{detail.externalReferenceCode}
				</div>

				{detail.expirationDate ? (
					<>
						<div className="small text-secondary text-uppercase">
							{Liferay.Language.get('expiration-date')}
						</div>

						<div>{formatDate(detail.expirationDate)}</div>
					</>
				) : null}
			</div>

			<h5>{Liferay.Language.get('recipients')}</h5>

			<table className="mb-4 table table-list">
				<thead>
					<tr>
						{ordered && <th>{Liferay.Language.get('order')}</th>}

						<th>{Liferay.Language.get('name')}</th>

						<th>{Liferay.Language.get('email')}</th>

						<th>{Liferay.Language.get('status')}</th>

						<th>{Liferay.Language.get('date')}</th>
					</tr>
				</thead>

				<tbody>
					{detail.recipients.map((recipient, index) => (
						<tr key={index}>
							{ordered && <td>{recipient.signingOrder}</td>}

							<td>{recipient.name}</td>

							<td>{recipient.emailAddress}</td>

							<td>
								<SignatureStatusLabel
									status={getRecipientStatus(recipient)}
								/>
							</td>

							<td>
								{formatDate(
									recipient.statusDate || recipient.sentDate
								)}
							</td>
						</tr>
					))}
				</tbody>
			</table>

			<h5>{Liferay.Language.get('activity')}</h5>

			<ul className="timeline">
				{getActivities(detail).map((activity, index) => (
					<li className="timeline-item" key={index}>
						<div className="panel panel-secondary">
							<div className="timeline-increment">
								<span
									className={`timeline-icon bg-${activity.type}`}
								/>
							</div>

							<div className="panel-body">
								<div className="font-weight-semi-bold">
									{activity.title}
								</div>

								<div className="text-secondary">
									{[
										activity.detail,
										formatDate(activity.time),
									]
										.filter(Boolean)
										.join(SEPARATOR)}
								</div>
							</div>
						</div>
					</li>
				))}
			</ul>

			{detail.history?.length ? (
				<>
					<h5>{Liferay.Language.get('history')}</h5>

					<table className="table table-list">
						<thead>
							<tr>
								<th>{Liferay.Language.get('envelope-id')}</th>

								<th>{Liferay.Language.get('status')}</th>

								<th>{Liferay.Language.get('create-date')}</th>

								<th>{Liferay.Language.get('date')}</th>
							</tr>
						</thead>

						<tbody>
							{detail.history.map((historyEntry) => (
								<tr key={historyEntry.externalReferenceCode}>
									<td>
										{historyEntry.externalReferenceCode}
									</td>

									<td>
										<SignatureStatusLabel
											status={historyEntry.requestStatus}
										/>
									</td>

									<td>
										{formatDate(historyEntry.createDate)}
									</td>

									<td>
										{formatDate(historyEntry.statusDate)}
									</td>
								</tr>
							))}
						</tbody>
					</table>
				</>
			) : null}
		</div>
	);
}

SignatureDetailsContent.propTypes = {
	detail: PropTypes.object.isRequired,
};

export default function SignatureDetails({url}) {
	const [detail, setDetail] = useState(null);
	const [error, setError] = useState(false);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		let mounted = true;

		fetch(url)
			.then((response) => {
				if (!response.ok) {
					throw new Error(response.statusText);
				}

				return response.json();
			})
			.then((data) => {
				if (mounted) {
					setDetail(data);
					setLoading(false);
				}
			})
			.catch(() => {
				if (mounted) {
					setError(true);
					setLoading(false);
				}
			});

		return () => {
			mounted = false;
		};
	}, [url]);

	if (loading) {
		return <ClayLoadingIndicator />;
	}

	if (error) {
		return (
			<div className="text-danger">
				{Liferay.Language.get('an-unexpected-error-occurred')}
			</div>
		);
	}

	if (detail && detail.externalReferenceCode) {
		return <SignatureDetailsContent detail={detail} />;
	}

	return (
		<div className="text-secondary">
			{Liferay.Language.get(
				'no-signature-request-was-found-for-this-document'
			)}
		</div>
	);
}

SignatureDetails.propTypes = {
	url: PropTypes.string.isRequired,
};
