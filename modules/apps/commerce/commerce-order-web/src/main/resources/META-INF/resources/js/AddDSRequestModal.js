/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayModal from '@clayui/modal';
import {DigitalSignatureRequestForm} from '@liferay/digital-signature-js-components-web';
import {openToast} from 'frontend-js-components-web';
import {escapeHTML, fetch} from 'frontend-js-web';
import PropTypes from 'prop-types';
import React from 'react';

export default function AddDSRequestModal({
	addDSRequestModalProps,
	addDSRequestURL,
	attachment,
	closeModal,
}) {
	const {
		accountEntryUsers,
		buyer,
		commerceOrderId,
		getDSRequestRecipientUsersURL,
		portletNamespace,
	} = addDSRequestModalProps;

	const accountEntryUserIds = [
		...accountEntryUsers.map(({userId}) => userId),
		buyer?.userId,
	];

	const recipientUsers = [
		...(buyer ? [buyer] : []),
		...accountEntryUsers.filter(({userId}) => userId !== buyer?.userId),
	];

	const isCountersigner = ({userId}) =>
		!!userId && !accountEntryUserIds.includes(userId);

	const searchUsers = (keywords, recipients) => {
		const lowerCaseKeywords = keywords.toLowerCase();

		const matchingRecipientUsers = recipientUsers.filter(
			({emailAddress, name}) =>
				emailAddress.toLowerCase().includes(lowerCaseKeywords) ||
				name.toLowerCase().includes(lowerCaseKeywords)
		);

		if (!keywords || recipients.some(isCountersigner)) {
			return Promise.resolve(matchingRecipientUsers);
		}

		const url = new URL(
			getDSRequestRecipientUsersURL,
			window.location.href
		);

		url.searchParams.set(
			`${portletNamespace}commerceOrderId`,
			commerceOrderId
		);
		url.searchParams.set(`${portletNamespace}keywords`, keywords);

		return fetch(url.toString())
			.then((response) => response.json())
			.then((users) => [
				...matchingRecipientUsers,
				...(Array.isArray(users) ? users : []).filter(isCountersigner),
			])
			.catch(() => matchingRecipientUsers);
	};

	const onSubmit = (values) => {
		const formData = new FormData();

		formData.append(`${portletNamespace}emailMessage`, values.emailMessage);
		formData.append(`${portletNamespace}emailSubject`, values.emailSubject);
		formData.append(`${portletNamespace}expireAfter`, values.expireAfter);
		formData.append(`${portletNamespace}sequential`, values.sequential);

		values.recipients.forEach(({userId}) =>
			formData.append(`${portletNamespace}recipientUserIds`, userId)
		);

		return fetch(addDSRequestURL, {body: formData, method: 'POST'})
			.then((response) => {
				if (!response.ok) {
					throw new Error(response.statusText);
				}

				return response.json();
			})
			.then(({errorMessage}) => {
				if (errorMessage) {
					throw {errorMessage};
				}

				closeModal();

				openToast({
					message: Liferay.Language.get(
						'your-request-completed-successfully'
					),
					type: 'success',
				});

				window.location.reload();
			})
			.catch((error) => {
				openToast({
					message: escapeHTML(
						error?.errorMessage ||
							Liferay.Language.get('an-unexpected-error-occurred')
					),
					type: 'danger',
				});
			});
	};

	return (
		<>
			<ClayModal.Header>
				{Liferay.Language.get('request-signature')}
			</ClayModal.Header>

			<DigitalSignatureRequestForm
				expirationEnabled
				fileEntries={[
					{
						extension: attachment.extension,
						fileEntryId: attachment.fileEntryId,
						title: attachment.title,
					},
				]}
				modal
				onCancel={closeModal}
				onSubmit={onSubmit}
				recipientBadgeText={(recipient) =>
					isCountersigner(recipient)
						? Liferay.Language.get('countersigner')
						: null
				}
				recipients={buyer ? [buyer] : undefined}
				searchUsers={searchUsers}
			/>
		</>
	);
}

AddDSRequestModal.propTypes = {
	addDSRequestModalProps: PropTypes.shape({
		accountEntryUsers: PropTypes.arrayOf(
			PropTypes.shape({
				emailAddress: PropTypes.string,
				name: PropTypes.string,
				userId: PropTypes.number,
			})
		),
		buyer: PropTypes.shape({
			emailAddress: PropTypes.string,
			name: PropTypes.string,
			userId: PropTypes.number,
		}),
		commerceOrderId: PropTypes.number,
		getDSRequestRecipientUsersURL: PropTypes.string,
		portletNamespace: PropTypes.string,
	}).isRequired,
	addDSRequestURL: PropTypes.string.isRequired,
	attachment: PropTypes.shape({
		extension: PropTypes.string,
		fileEntryId: PropTypes.number,
		title: PropTypes.string,
	}).isRequired,
	closeModal: PropTypes.func.isRequired,
};
