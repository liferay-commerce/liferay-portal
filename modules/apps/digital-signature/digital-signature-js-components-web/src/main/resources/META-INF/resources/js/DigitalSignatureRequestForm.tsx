/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayForm from '@clayui/form';
import ClayModal from '@clayui/modal';
import {useFormik} from 'formik';
import {sub} from 'frontend-js-web';
import React, {useRef, useState} from 'react';

import MessageStep, {validateMessageStep} from './steps/MessageStep';
import RecipientsStep, {validateRecipientsStep} from './steps/RecipientsStep';
import ReviewStep from './steps/ReviewStep';
import {
	DigitalSignatureRequestErrors,
	DigitalSignatureRequestValues,
	FileEntry,
	Recipient,
	RecipientBadgeText,
	SearchUsers,
	SelectFileEntry,
	Step,
} from './types';

import './DigitalSignatureRequestForm.scss';

interface Props {
	allowedFileExtensions?: string[];
	envelopeNameEnabled?: boolean;
	expirationEnabled?: boolean;
	fileEntries?: FileEntry[];
	maxFileEntries?: number;
	maxRecipients?: number;
	modal?: boolean;
	onCancel: () => void;
	onSubmit: (values: DigitalSignatureRequestValues) => Promise<void> | void;
	recipientBadgeText?: RecipientBadgeText;
	recipients?: Recipient[];
	searchUsers?: SearchUsers;
	selectFileEntry?: SelectFileEntry;
}

function DigitalSignatureRequestForm({
	allowedFileExtensions,
	envelopeNameEnabled = false,
	expirationEnabled = false,
	fileEntries = [],
	maxFileEntries,
	maxRecipients,
	modal = false,
	onCancel,
	onSubmit,
	recipientBadgeText,
	recipients = [{emailAddress: '', name: ''}],
	searchUsers,
	selectFileEntry,
}: Props) {
	const [currentStep, setCurrentStep] = useState(0);
	const [showErrors, setShowErrors] = useState(false);

	const rowKeyRef = useRef(0);

	const createRecipient = (
		recipient: Recipient = {emailAddress: '', name: ''}
	): Recipient => ({
		...recipient,
		rowKey: rowKeyRef.current++,
	});

	const [initialRecipients] = useState(() =>
		recipients.map((recipient) => createRecipient(recipient))
	);

	const steps: Step[] = [
		{
			Component: RecipientsStep,
			title: Liferay.Language.get('recipients'),
			validate: validateRecipientsStep,
		},
		{
			Component: MessageStep,
			title: Liferay.Language.get('message'),
			validate: validateMessageStep,
		},
		{
			Component: ReviewStep,
			title: Liferay.Language.get('review'),
		},
	];

	const validateStep = (step: Step, values: DigitalSignatureRequestValues) =>
		step.validate
			? step.validate(values, {
					allowedFileExtensions,
					envelopeNameEnabled,
					maxFileEntries,
					searchUsers,
				})
			: {};

	const formik = useFormik<DigitalSignatureRequestValues>({
		initialValues: {
			emailMessage: '',
			emailSubject: '',
			envelopeName: '',
			expireAfter: 120,
			fileEntries,
			recipients: initialRecipients,
			sequential: true,
		},
		onSubmit,
		validate: (values) =>
			Object.assign(
				{},
				...steps.map((step) => validateStep(step, values))
			),
		validateOnMount: true,
	});

	const ActiveStep = steps[currentStep].Component;
	const isLastStep = currentStep === steps.length - 1;

	const onNextStep = () => {
		if (
			Object.keys(validateStep(steps[currentStep], formik.values)).length
		) {
			setShowErrors(true);

			return;
		}

		setShowErrors(false);
		setCurrentStep((step) => step + 1);
	};

	const onPreviousStep = () => {
		setShowErrors(false);
		setCurrentStep((step) => step - 1);
	};

	const cancelButton = (
		<ClayButton
			disabled={formik.isSubmitting}
			displayType="secondary"
			onClick={onCancel}
		>
			{Liferay.Language.get('cancel')}
		</ClayButton>
	);

	const content = (
		<>
			<div className="mb-4">
				<div className="mb-1 text-secondary">
					{sub(
						Liferay.Language.get('step-x-of-x'),
						currentStep + 1,
						steps.length
					)}
				</div>

				<div className="text-6 text-weight-bold">
					{steps[currentStep].title}
				</div>
			</div>

			<ActiveStep
				createRecipient={createRecipient}
				envelopeNameEnabled={envelopeNameEnabled}
				errors={formik.errors as DigitalSignatureRequestErrors}
				expirationEnabled={expirationEnabled}
				handleChange={formik.handleChange}
				maxFileEntries={maxFileEntries}
				maxRecipients={maxRecipients}
				recipientBadgeText={recipientBadgeText}
				searchUsers={searchUsers}
				selectFileEntry={selectFileEntry}
				setFieldValue={formik.setFieldValue}
				showErrors={showErrors}
				values={formik.values}
			/>
		</>
	);

	const stepButtons = (
		<>
			{currentStep > 0 && (
				<ClayButton
					disabled={formik.isSubmitting}
					displayType="secondary"
					onClick={onPreviousStep}
				>
					{Liferay.Language.get('previous')}
				</ClayButton>
			)}

			{isLastStep ? (
				<ClayButton
					disabled={!formik.isValid || formik.isSubmitting}
					onClick={() => formik.handleSubmit()}
				>
					{Liferay.Language.get('send')}
				</ClayButton>
			) : (
				<ClayButton onClick={onNextStep}>
					{Liferay.Language.get('next')}
				</ClayButton>
			)}
		</>
	);

	return (
		<ClayForm
			className="digital-signature-request-form"
			onSubmit={(event) => event.preventDefault()}
		>
			{modal ? (
				<>
					<ClayModal.Body scrollable>{content}</ClayModal.Body>

					<ClayModal.Footer
						first={cancelButton}
						last={
							<ClayButton.Group spaced>
								{stepButtons}
							</ClayButton.Group>
						}
					/>
				</>
			) : (
				<>
					{content}

					<ClayButton.Group className="mt-4" spaced>
						{cancelButton}

						{stepButtons}
					</ClayButton.Group>
				</>
			)}
		</ClayForm>
	);
}

export default DigitalSignatureRequestForm;
