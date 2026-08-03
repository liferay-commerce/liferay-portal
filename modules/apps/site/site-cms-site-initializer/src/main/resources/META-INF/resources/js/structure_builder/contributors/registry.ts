/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	ObjectDefinition,
	ObjectDefinitions,
} from '../../common/types/ObjectDefinition';
import {Structure, StructureChild, StructureType} from '../types/Structure';
import {Uuid} from '../types/Uuid';

import type {ReactElement} from 'react';

import type {ErrorMap} from '../utils/validation';

export const STRUCTURE_BUILDER_CONTRIBUTORS_ID =
	'com.liferay.site.cms.site.initializer.structure.builder.contributors';

export type AddGroupingContainerAction = {
	parent: Uuid;
	type: 'add-grouping-container';
	uuids: Uuid[];
	variant?: string;
};

export type StructureBuilderItemAction = {
	label: string;
	onClick: () => void;
	symbolLeft?: string;
};

export type StructureBuilderReduceAction =
	| AddGroupingContainerAction
	| UpdateGroupingContainerAction;

export type StructureBuilderReduceResult = {
	children: Structure['children'];
	invalids?: Map<Uuid, ErrorMap>;
	selection?: Uuid[];
};

export type UpdateGroupingContainerAction = {
	label: Liferay.Language.LocalizedValue<string>;
	type: 'update-grouping-container';
	uuid: Uuid;
};

export type StructureBuilderProvider = {
	deserialize?: (args: {
		children: Structure['children'];
		objectDefinition: ObjectDefinition;
		parent: Uuid;
	}) => Structure['children'];
	getItemActions?: (args: {
		dispatch: (action: StructureBuilderReduceAction) => void;
		items: StructureChild[];
		structure: Structure;
	}) => StructureBuilderItemAction[];
	id: string;
	isGroupingContainer?: (child: StructureChild) => boolean;
	persist?: (args: {
		erc: Structure['erc'];
		structure: Structure;
	}) => Promise<{error: boolean}>;
	reduce?: (args: {
		action: StructureBuilderReduceAction;
		invalids: Map<Uuid, ErrorMap>;
		structure: Structure;
	}) => StructureBuilderReduceResult | undefined;
	renderSettings?: (args: {
		child: StructureChild;
		disabled: boolean;
	}) => ReactElement | null;
	seedChildren?: (args: {
		objectDefinition: ObjectDefinition;
		objectDefinitions: ObjectDefinitions;
		parent: Uuid;
	}) => Structure['children'];
	supports: (type: StructureType) => boolean;
};

export type StructureBuilderRegistry = {
	addProvider: (provider: StructureBuilderProvider) => void;
	getProvider: (type: StructureType) => StructureBuilderProvider | undefined;
	getProviders: () => StructureBuilderProvider[];
	hasProviders: () => boolean;
	subscribe: (listener: () => void) => () => void;
	supports: (type: StructureType) => boolean;
};

const providers: StructureBuilderProvider[] = [];

const listeners = new Set<() => void>();

const registry: StructureBuilderRegistry = {
	addProvider(provider) {
		providers.push(provider);

		listeners.forEach((listener) => listener());
	},

	getProvider(type) {
		return providers.find((provider) => provider.supports(type));
	},

	getProviders() {
		return providers;
	},

	hasProviders() {
		return !!providers.length;
	},

	subscribe(listener) {
		listeners.add(listener);

		return () => {
			listeners.delete(listener);
		};
	},

	supports(type) {
		return providers.some((provider) => provider.supports(type));
	},
};

export default registry;
