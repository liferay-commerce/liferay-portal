/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import ClayForm, {ClayCheckbox, ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import {openToast} from 'frontend-js-components-web';
import React from 'react';

import Input from '../../form/FormBase';
import {
	isEmail,
	maxLength,
	required,
	validate,
	withInvalidExtensions,
} from '../../form/FormValidation';
import FileEntryList from '../FileEntryList';
import FileSelector from '../FileSelector';
import RecipientUserAutocomplete from '../RecipientUserAutocomplete';

const validateFileEntries = (
	fileEntries,
	{allowedFileExtensions, maxFileEntries}
) => {
	const invalidFileEntries = allowedFileExtensions
		? withInvalidExtensions(fileEntries, allowedFileExtensions)
		: undefined;

	if (invalidFileEntries) {
		return invalidFileEntries;
	}

	if (
		!fileEntries.length ||
		(maxFileEntries && fileEntries.length > maxFileEntries)
	) {
		return [];
	}
};

const validateRecipient = (recipient, {searchUsers}) => {
	const errors = searchUsers
		? {
				emailAddress: recipient.userId
					? undefined
					: Liferay.Language.get('this-field-is-required'),
			}
		: validate(
				{
					emailAddress: [
						(value) => maxLength(value, 100),
						isEmail,
						required,
					],
					name: [required],
				},
				{...recipient}
			);

	if (errors.emailAddress || errors.name) {
		return errors;
	}
};

export function validateRecipientsStep(values, config) {
	const errors = {};

	const fileEntryErrors = validateFileEntries(values.fileEntries, config);

	if (fileEntryErrors) {
		errors.fileEntries = fileEntryErrors;
	}

	const recipientErrors = values.recipients.map((recipient) =>
		validateRecipient(recipient, config)
	);

	if (recipientErrors.some(Boolean)) {
		errors.recipients = recipientErrors;
	}

	return errors;
}

function RecipientsStep({
	errors,
	maxFileEntries,
	maxRecipients,
	searchUsers,
	setFieldValue,
	showErrors,
	values,
}) {
	const canAddMoreRecipients =
		!maxRecipients || values.recipients.length < maxRecipients;

	const onAddFileEntry = (fileEntry) => {
		if (
			values.fileEntries.some(
				({fileEntryId}) => fileEntryId === fileEntry.fileEntryId
			)
		) {
			openToast({
				message: Liferay.Language.get('the-document-already-exists'),
				title: Liferay.Language.get('error'),
				type: 'danger',
			});

			return;
		}

		setFieldValue('fileEntries', [...values.fileEntries, fileEntry]);
	};

	const onAddRecipient = () => {
		if (canAddMoreRecipients) {
			setFieldValue('recipients', [
				...values.recipients,
				{emailAddress: '', name: ''},
			]);
		}
	};

	const onChangeRecipient = (recipientIndex, recipient) => {
		setFieldValue(
			'recipients',
			values.recipients.map((currentRecipient, index) =>
				index === recipientIndex ? recipient : currentRecipient
			)
		);
	};

	const onMoveRecipient = (recipientIndex, offset) => {
		const recipients = [...values.recipients];

		[recipients[recipientIndex], recipients[recipientIndex + offset]] = [
			recipients[recipientIndex + offset],
			recipients[recipientIndex],
		];

		setFieldValue('recipients', recipients);
	};

	const onRemoveRecipient = (recipientIndex) => {
		setFieldValue(
			'recipients',
			values.recipients.filter((_, index) => index !== recipientIndex)
		);
	};

	return (
		<>
			<FileSelector
				disabled={
					!!maxFileEntries &&
					values.fileEntries.length >= maxFileEntries
				}
				onChange={(_, fileEntry) =>
					onAddFileEntry(JSON.parse(fileEntry))
				}
			/>

			{showErrors && !values.fileEntries.length && (
				<ClayForm.FeedbackGroup className="has-error mb-4">
					<ClayForm.FeedbackItem>
						<ClayForm.FeedbackIndicator symbol="exclamation-full" />

						{Liferay.Language.get(
							'please-add-at-least-one-document'
						)}
					</ClayForm.FeedbackItem>
				</ClayForm.FeedbackGroup>
			)}

			<FileEntryList
				errors={errors.fileEntries}
				fileEntries={values.fileEntries}
				onRemove={(fileEntry) =>
					setFieldValue(
						'fileEntries',
						values.fileEntries.filter(
							({fileEntryId}) =>
								fileEntryId !== fileEntry.fileEntryId
						)
					)
				}
			/>

			{values.recipients.map((recipient, index) => {
				const error = showErrors
					? errors.recipients?.[index]
					: undefined;

				const otherRecipients = values.recipients.filter(
					(_, recipientIndex) => recipientIndex !== index
				);

				return (
					<ClayForm.Group className="recipient" key={index}>
						<ClayInput.Group>
							<ClayInput.GroupItem>
								<Input
									className="mb-0"
									disabled={!!searchUsers}
									error={error?.name}
									label={Liferay.Language.get(
										'recipient-full-name'
									)}
									name={`recipients[${index}].name`}
									onChange={(event) =>
										onChangeRecipient(index, {
											...recipient,
											name: event.target.value,
										})
									}
									placeholder={Liferay.Language.get(
										'recipient-full-name'
									)}
									required
									value={recipient.name}
								/>
							</ClayInput.GroupItem>

							<ClayInput.GroupItem>
								{searchUsers ? (
									<RecipientUserAutocomplete
										error={error?.emailAddress}
										excludedUserIds={otherRecipients
											.map(({userId}) => userId)
											.filter((userId) => !!userId)}
										id={`recipients[${index}].emailAddress`}
										label={Liferay.Language.get(
											'recipient-email'
										)}
										onSelectUser={(user) =>
											onChangeRecipient(index, user)
										}
										searchUsers={searchUsers}
										value={recipient.emailAddress}
									/>
								) : (
									<Input
										className="mb-0"
										error={error?.emailAddress}
										label={Liferay.Language.get(
											'recipient-email'
										)}
										name={`recipients[${index}].emailAddress`}
										onChange={(event) =>
											onChangeRecipient(index, {
												...recipient,
												emailAddress:
													event.target.value,
											})
										}
										placeholder={Liferay.Language.get(
											'recipient-email'
										)}
										required
										value={recipient.emailAddress}
									/>
								)}
							</ClayInput.GroupItem>

							{values.sequential && (
								<>
									<ClayInput.GroupItem shrink>
										<ClayButtonWithIcon
											aria-label={Liferay.Language.get(
												'move-up'
											)}
											className="recipient-icon-button"
											disabled={!index}
											displayType="secondary"
											onClick={() =>
												onMoveRecipient(index, -1)
											}
											symbol="angle-up"
											title={Liferay.Language.get(
												'move-up'
											)}
										/>
									</ClayInput.GroupItem>

									<ClayInput.GroupItem shrink>
										<ClayButtonWithIcon
											aria-label={Liferay.Language.get(
												'move-down'
											)}
											className="recipient-icon-button"
											disabled={
												index ===
												values.recipients.length - 1
											}
											displayType="secondary"
											onClick={() =>
												onMoveRecipient(index, 1)
											}
											symbol="angle-down"
											title={Liferay.Language.get(
												'move-down'
											)}
										/>
									</ClayInput.GroupItem>
								</>
							)}

							<ClayInput.GroupItem shrink>
								<ClayButtonWithIcon
									aria-label={Liferay.Language.get('remove')}
									className="recipient-icon-button"
									disabled={values.recipients.length === 1}
									displayType="secondary"
									onClick={() => onRemoveRecipient(index)}
									symbol="trash"
									title={Liferay.Language.get('remove')}
								/>
							</ClayInput.GroupItem>
						</ClayInput.Group>
					</ClayForm.Group>
				);
			})}

			{canAddMoreRecipients && (
				<ClayButton
					className="mb-4"
					displayType="unstyled"
					onClick={onAddRecipient}
				>
					<span className="inline-item inline-item-before">
						<ClayIcon symbol="plus" />
					</span>

					<span>{Liferay.Language.get('add-recipient')}</span>
				</ClayButton>
			)}

			<ClayForm.Group>
				<ClayCheckbox
					checked={values.sequential}
					label={Liferay.Language.get(
						'recipients-sign-in-the-order-listed'
					)}
					onChange={() =>
						setFieldValue('sequential', !values.sequential)
					}
				/>
			</ClayForm.Group>
		</>
	);
}

export default RecipientsStep;
