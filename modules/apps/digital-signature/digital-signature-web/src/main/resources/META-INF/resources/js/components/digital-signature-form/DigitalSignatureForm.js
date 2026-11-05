/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayCard from '@clayui/card';
import ClayLayout from '@clayui/layout';
import {openToast} from 'frontend-js-components-web';
import {
	createResourceURL,
	fetch,
	navigate as liferayNavigate,
	objectToFormData,
} from 'frontend-js-web';
import React, {useContext} from 'react';

import {AppContext} from '../../AppContext';
import DigitalSignatureFormBase from './DigitalSignatureFormBase';

const MAX_LENGTH = {
	ATTACHMENTS: 10,
	RECIPIENTS: 10,
};

const searchUsers = (keywords) => {
	if (!keywords) {
		return Promise.resolve([]);
	}

	return fetch(
		`/o/headless-admin-user/v1.0/user-accounts?pageSize=10&search=${encodeURIComponent(
			keywords
		)}`
	)
		.then((response) => response.json())
		.then(({items = []}) =>
			items.map(({emailAddress, id, name}) => ({
				emailAddress,
				name,
				userId: id,
			}))
		);
};

const DigitalSignatureForm = ({fileEntries = [], navigate}) => {
	const {
		allowedFileExtensions,
		baseResourceURL,
		embeddedViewEnabled,
		portletNamespace,
	} = useContext(AppContext);
	const urlParams = new URLSearchParams(window.location.href);
	const backURL = urlParams.get(`${portletNamespace}backURL`);

	const onGoBack = () => {
		if (navigate) {
			return navigate(-1);
		}

		return liferayNavigate(backURL);
	};

	const onSubmit = async (values) => {
		const fileEntryIds = values.fileEntries.map(
			({fileEntryId}) => fileEntryId
		);

		const formDataValues = {
			[`${portletNamespace}emailMessage`]: values.emailMessage,
			[`${portletNamespace}emailSubject`]: values.emailSubject,
			[`${portletNamespace}envelopeName`]: values.envelopeName,
			[`${portletNamespace}expireAfter`]: values.expireAfter,
			[`${portletNamespace}fileEntryIds`]: fileEntryIds,
			[`${portletNamespace}recipients`]: JSON.stringify(
				values.recipients.map(({emailAddress, name}) => ({
					email: emailAddress,
					fullName: name,
				}))
			),
		};

		try {
			const response = await fetch(
				createResourceURL(baseResourceURL, {
					p_p_resource_id: '/digital_signature/add_ds_envelope',
				}),
				{
					body: objectToFormData(formDataValues),
					method: 'POST',
				}
			);

			const {dsEnvelopeId} = await response.json();

			if (dsEnvelopeId) {
				openToast({
					message: Liferay.Language.get(
						'your-envelope-was-created-successfully'
					),
					title: Liferay.Language.get('success'),
					type: 'success',
				});
			}
		}
		catch (error) {
			openToast({
				message: Liferay.Language.get('an-unexpected-error-occurred'),
				title: Liferay.Language.get('error'),
				type: 'danger',
			});
		}

		onGoBack();
	};

	return (
		<ClayLayout.Container className="collect-digital-signature">
			<ClayCard>
				<div className="header">
					<h1>
						{Liferay.Language.get('new-digital-signature-envelope')}
					</h1>
				</div>

				<hr />

				<ClayCard.Body className="m-2">
					<DigitalSignatureFormBase
						allowedFileExtensions={allowedFileExtensions}
						expirationEnabled={embeddedViewEnabled}
						fileEntries={fileEntries}
						maxFileEntries={MAX_LENGTH.ATTACHMENTS}
						maxRecipients={MAX_LENGTH.RECIPIENTS}
						onCancel={onGoBack}
						onSubmit={onSubmit}
						searchUsers={
							embeddedViewEnabled ? searchUsers : undefined
						}
					/>
				</ClayCard.Body>
			</ClayCard>
		</ClayLayout.Container>
	);
};

export default DigitalSignatureForm;
