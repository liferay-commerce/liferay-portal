/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayButtonWithIcon} from '@clayui/button';
import ClayList from '@clayui/list';
import React from 'react';

import {FileEntry} from './types';

interface Props {
	errors?: FileEntry[];
	fileEntries: FileEntry[];
	onRemove?: (fileEntry: FileEntry) => void;
}

const FileEntryList = ({errors = [], fileEntries, onRemove}: Props) => {
	if (!fileEntries.length) {
		return null;
	}

	return (
		<ClayList className="file-entries-list mt-1">
			<ClayList.Header>
				{Liferay.Language.get('documents-added')}
			</ClayList.Header>

			<>
				{fileEntries.map((fileEntry) => {
					const hasError = errors.some(
						({fileEntryId}) => fileEntryId === fileEntry.fileEntryId
					);

					return (
						<ClayList.Item flex key={fileEntry.fileEntryId}>
							<ClayList.ItemField expand>
								<ClayList.ItemTitle>
									{fileEntry.title}
								</ClayList.ItemTitle>

								{hasError && (
									<ClayList.ItemTitle className="text-danger">
										{Liferay.Language.get(
											'this-file-extension-is-not-supported-by-docusign'
										)}
									</ClayList.ItemTitle>
								)}
							</ClayList.ItemField>

							{onRemove && (
								<ClayList.ItemField>
									<ClayButtonWithIcon
										aria-label={Liferay.Language.get(
											'remove'
										)}
										displayType="unstyled"
										monospaced={false}
										onClick={() => onRemove(fileEntry)}
										symbol="trash"
										title={Liferay.Language.get('remove')}
									/>
								</ClayList.ItemField>
							)}
						</ClayList.Item>
					);
				})}
			</>
		</ClayList>
	);
};

export default FileEntryList;
