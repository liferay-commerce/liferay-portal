/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '../../../css/structure_builder/StructureBuilder.scss';

import React, {useEffect, useState} from 'react';

import {
	ObjectDefinition,
	ObjectDefinitions,
	ObjectRelationship,
} from '../../common/types/ObjectDefinition';
import {
	DefaultLanguageLabels,
	setDefaultLanguageLabels,
} from '../../common/utils/defaultLanguageLabels';
import {Config, initializeConfig} from '../config';
import CacheContextProvider from '../contexts/CacheContext';
import StateContextProvider, {useSelector} from '../contexts/StateContext';
import structureBuilderRegistry, {
	STRUCTURE_BUILDER_CONTRIBUTORS_ID,
} from '../contributors/registry';
import selectStructureId from '../selectors/selectStructureId';
import selectStructureStatus from '../selectors/selectStructureStatus';
import {StructureType} from '../types/Structure';
import {setBaseObjectDefinition} from '../utils/baseObjectDefinition';
import buildState from '../utils/buildState';
import {setSystemObjectFieldNames} from '../utils/isCustomObjectField';
import HelpButton from './HelpButton';
import ShortcutManager from './ShortcutManager';
import Sidebar from './Sidebar';
import StructureBuilderToolbar from './StructureBuilderToolbar';
import Settings from './settings/Settings';

Liferay.component(STRUCTURE_BUILDER_CONTRIBUTORS_ID, structureBuilderRegistry);

function useContributorsReady(type: StructureType | null): boolean {
	const [ready, setReady] = useState(
		!type || structureBuilderRegistry.supports(type)
	);

	useEffect(() => {
		if (ready) {
			return;
		}

		let settled = false;

		const finish = () => {
			if (!settled) {
				settled = true;

				setReady(true);
			}
		};

		const unsubscribe = structureBuilderRegistry.subscribe(finish);

		const timeoutId = setTimeout(finish, 3000);

		return () => {
			unsubscribe();

			clearTimeout(timeoutId);
		};
	}, [ready]);

	return ready;
}

export default function StructureBuilder({
	config,
	defaultLanguageLabels,
	hasStructureBuilderContributor = false,
	state,
	systemObjectFieldNames,
}: {
	config: Config;
	defaultLanguageLabels: DefaultLanguageLabels;
	hasStructureBuilderContributor?: boolean;
	state: {
		baseObjectDefinition?: ObjectDefinition | null;
		mainObjectDefinition: ObjectDefinition;
		objectDefinitions: ObjectDefinitions;
		relatedContentObjectRelationships: ObjectRelationship[];
	};
	systemObjectFieldNames: Record<string, string[]>;
}) {
	const contributorsReady = useContributorsReady(
		hasStructureBuilderContributor
			? (state.mainObjectDefinition
					.objectFolderExternalReferenceCode as StructureType)
			: null
	);

	initializeConfig(config);
	setBaseObjectDefinition(
		state.baseObjectDefinition ?? null,
		state.objectDefinitions
	);
	setDefaultLanguageLabels(defaultLanguageLabels);
	setSystemObjectFieldNames(systemObjectFieldNames);

	if (!contributorsReady) {
		return <span aria-hidden="true" className="loading-animation" />;
	}

	return (
		<StateContextProvider initialState={buildState(state)}>
			<CacheContextProvider
				initialData={{
					'object-definitions': state.objectDefinitions,
				}}
			>
				<div className="d-flex flex-column structure-builder__wrapper">
					<HistoryManager />

					<ShortcutManager />

					<StructureBuilderToolbar />

					<div className="d-flex flex-grow-1 p-2 p-md-4">
						<Sidebar />

						<Settings />
					</div>

					<HelpButton />
				</div>
			</CacheContextProvider>
		</StateContextProvider>
	);
}

function HistoryManager() {
	const id = useSelector(selectStructureId);
	const status = useSelector(selectStructureStatus);

	useEffect(() => {
		if (status !== 'published' || !id) {
			return;
		}

		const url = new URL(window.location.href);

		if (url.searchParams.has('objectFolderExternalReferenceCode')) {
			url.searchParams.delete('objectFolderExternalReferenceCode');
		}

		url.searchParams.set('objectDefinitionId', String(id));

		history.replaceState(null, document.head.title, url.href);
	}, [id, status]);

	return null;
}
