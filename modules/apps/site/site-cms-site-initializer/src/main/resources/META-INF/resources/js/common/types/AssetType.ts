/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {SharingPermission} from './SharingPermission';
import {MimeTypes} from "../components/AssetIcon";

export interface IAssetFile {
	alternativeText?: string;
	externalReferenceCode: string;
	id: number;
	link: {
		href: string;
		label: string;
	};
	metadata?: {
		numberOfPages?: number;
	};
	mimeType?: MimeTypes;
	name: string;
	previewURL?: string;
	thumbnailURL?: string;
}

export interface IAssetObjectEntry {
	actions: {
		[action: string]: {
			method: 'DELETE' | 'GET' | 'PATCH' | 'POST' | 'PUT';
			href: string;
		}
	};
	content?: string;
	contentRawText?: string;
	creator: {
		additionalName: string;
		contentType: string;
		externalReferenceCode: string;
		familyName: string;
		givenName: string;
		id: number;
		name: string;
	};
	dateCreated: string;
	dateModified: string;
	displayDate: string;
	expirationDate: string;
	externalReferenceCode: string;
	file?: IAssetFile;
	friendlyUrlPath: string;
	friendlyUrlPath_i18n: {
		[lang: string]: string;
	};
	id: number;
	keywords: string[];
	numberOfObjectEntries: number;
	numberOfObjectEntryFolders: number;
	objectEntryFolderExternalReferenceCode: string;
	objectEntryFolderId: number;
	reviewDate: string;
	scope?: {
		externalReferenceCode: string;
		type: string;
	};
	scopeId: number;
	scopeKey: string;
	status: {
		code: number;
		label: string;
		label_i18n: string;
	};
	systemProperties: IAssetObjectDefinitionBrief & IAssetScope & IAssetVersion;
	taxonomyCategoryBriefs: any[];
	taxonomyCategoryIds?: number[];
	title: string;
	title_i18n: any;
}

export interface IAssetObjectDefinitionBrief {
	objectDefinitionBrief?: {
		classNameId: number;
		externalReferenceCode: string;
		label: string;
		objectFolderExternalReferenceCode: string;
	};
}

export interface IAssetScope {
	scope?: {
		externalReferenceCode: string;
		type: string;
	};
}

export interface IAssetVersion {
	version: {
		number: number;
	};
}

export interface ISearchAssetObjectEntry {
	actionIds?: SharingPermission[];
	actions: any;
	dateCreated: string;
	dateModified: string;
	description: string;
	embedded: IAssetObjectEntry;
	entryClassName: string;
	score: number;
	title: string;
}

export interface IGroupedTaxonomies {
	taxonomyCategoryIds: number[];
	taxonomyVocabularies: {
		[taxonomyVocabularyId: number]: ITaxonomyCategoryFacade[];
	};
}

export interface ITaxonomyCategoryFacade {
	id: string;
	name?: string;
	parentTaxonomyVocabulary: ITaxonomyVocabulary;
	taxonomyVocabularyId: number;
}

export interface ITaxonomyVocabulary {
	id: number;
	name: string;
}
