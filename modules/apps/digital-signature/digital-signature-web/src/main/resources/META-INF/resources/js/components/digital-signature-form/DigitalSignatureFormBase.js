/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayForm from '@clayui/form';
import {useFormik} from 'formik';
import {sub} from 'frontend-js-web';
import React, {useState} from 'react';

import MessageStep, {validateMessageStep} from './steps/MessageStep';
import RecipientsStep, {validateRecipientsStep} from './steps/RecipientsStep';
import ReviewStep from './steps/ReviewStep';

function DigitalSignatureFormBase({
	allowedFileExtensions,
	expirationEnabled = false,
	fileEntries = [],
	maxFileEntries,
	maxRecipients,
	onCancel,
	onSubmit,
	recipients = [{emailAddress: '', name: ''}],
	searchUsers,
}) {
	const [currentStep, setCurrentStep] = useState(0);
	const [showErrors, setShowErrors] = useState(false);

	const steps = [
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

	const validateStep = (step, values) =>
		step.validate
			? step.validate(values, {
					allowedFileExtensions,
					maxFileEntries,
					searchUsers,
				})
			: {};

	const formik = useFormik({
		initialValues: {
			emailMessage: '',
			emailSubject: '',
			envelopeName: '',
			expireAfter: 120,
			fileEntries,
			recipients,
		},
		onSubmit: (values) => onSubmit(values),
		validate: (values) =>
			Object.assign(
				{},
				...steps.map((step) => validateStep(step, values))
			),
		validateOnMount: true,
	});

	const isLastStep = currentStep === steps.length - 1;
	const ActiveStep = steps[currentStep].Component;

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
		setCurrentStep((step) => Math.max(step - 1, 0));
	};

	return (
		<ClayForm
			className="digital-signature-request-form"
			onSubmit={(event) => event.preventDefault()}
		>
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
				errors={formik.errors}
				expirationEnabled={expirationEnabled}
				handleChange={formik.handleChange}
				maxFileEntries={maxFileEntries}
				maxRecipients={maxRecipients}
				searchUsers={searchUsers}
				setFieldValue={formik.setFieldValue}
				showErrors={showErrors}
				values={formik.values}
			/>

			<ClayButton.Group className="mt-4" spaced>
				<ClayButton
					disabled={formik.isSubmitting}
					displayType="secondary"
					onClick={onCancel}
				>
					{Liferay.Language.get('cancel')}
				</ClayButton>

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
			</ClayButton.Group>
		</ClayForm>
	);
}

export default DigitalSignatureFormBase;
