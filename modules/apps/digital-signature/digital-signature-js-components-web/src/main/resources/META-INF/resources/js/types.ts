/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ChangeEvent, ComponentType} from 'react';

export interface FileEntry {
	extension?: string;
	fileEntryId: number;
	title: string;
}

export interface Recipient {
	emailAddress: string;
	name: string;
	rowKey?: number;
	userId?: number;
}

export interface RecipientUser extends Recipient {
	userId: number;
}

export interface DigitalSignatureRequestFormValues {
	emailMessage: string;
	emailSubject: string;
	envelopeName: string;
	expireAfter: number;
	fileEntries: FileEntry[];
	recipients: Recipient[];
	sequential: boolean;
}

export interface RecipientErrors {
	emailAddress?: string;
	name?: string;
}

export interface DigitalSignatureRequestFormErrors {
	emailMessage?: string;
	emailSubject?: string;
	envelopeName?: string;
	fileEntries?: FileEntry[];
	recipients?: Array<RecipientErrors | undefined>;
}

export type SearchUsers = (keywords: string) => Promise<RecipientUser[]>;

export type SelectFileEntry = () => Promise<FileEntry | null | undefined>;

export interface Step {
	Component: ComponentType<StepProps>;
	title: string;
	validate?: ValidateStep;
}

export interface StepConfig {
	allowedFileExtensions?: string[];
	maxFileEntries?: number;
	searchUsers?: SearchUsers;
}

export interface StepProps {
	createRecipient: (recipient?: Recipient) => Recipient;
	embeddedViewEnabled: boolean;
	errors: DigitalSignatureRequestFormErrors;
	handleChange: (event: ChangeEvent) => void;
	maxFileEntries?: number;
	maxRecipients?: number;
	searchUsers?: SearchUsers;
	selectFileEntry?: SelectFileEntry;
	setFieldValue: (field: string, value: unknown) => void;
	showErrors: boolean;
	values: DigitalSignatureRequestFormValues;
}

export type ValidateStep = (
	values: DigitalSignatureRequestFormValues,
	config: StepConfig
) => DigitalSignatureRequestFormErrors;
