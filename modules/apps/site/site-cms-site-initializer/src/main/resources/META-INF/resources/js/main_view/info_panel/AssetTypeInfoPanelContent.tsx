/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React, {useMemo} from 'react';

import {ISearchAssetObjectEntry} from '../../common/types/AssetType';
import {AssetTypeInfoPanelContext, IAssetTypeInfoPanelContext} from './context';

import '../../../css/components/AssetTypeInfoPanel.scss';
import {getAssetType} from './util';
import {SidePanel} from "@clayui/core";
import ClaySticker from "@clayui/sticker";
import classNames from "classnames";
import {
	getFileMimeTypeObjectDefinitionStickerValue
} from "../props_transformer/utils/transformViewsItemProps";
import ClayIcon from "@clayui/icon";
import {sub} from "frontend-js-web";
import AssetTypeInfoPanelEmptyState from "./AssetTypeInfoPanelEmptyState";
import {ASSET_TYPE} from "./util/constants";
import AssetTypeInfoPanelFilesView from "./AssetTypeInfoPanelFilesView";
import AssetTypeInfoPanelFolderView from "./AssetTypeInfoPanelFolderView";

const AssetTypeInfoPanelContent = ({
	additionalProps: {assetLibraries, cmsGroupId, commentsProps, ...otherProps},
	items: selectedAssets = [],
}: {
	additionalProps: any;
	items: ISearchAssetObjectEntry[];
}) => {

	if (selectedAssets?.length !== 1) {
		return (
			<>
				<SidePanel.Header>
					<SidePanel.Title>
						<span className="inline-flex text-nowrap">
							{!selectedAssets.length
								? Liferay.Language.get('no-assets-selected')
								: sub(
									Liferay.Language.get('x-assets-selected'),
									selectedAssets.length
								)}
						</span>
					</SidePanel.Title>
				</SidePanel.Header>

				<SidePanel.Body>
					<AssetTypeInfoPanelEmptyState
						selected={selectedAssets.length}
					/>
				</SidePanel.Body>
			</>
		);
	}

	const {
		actions,
		asset,
		assetLibrary,
		stickerClassName,
		stickerSymbol,
		type
	} = useMemo(() => ({
		actions: selectedAssets[0].actions,
		asset: selectedAssets[0].embedded,
		assetLibrary: assetLibraries.find(({groupId}: {groupId: number}) =>
			Number(groupId) === Number(selectedAssets[0].embedded.scopeId)),
		stickerClassName: classNames(
			getFileMimeTypeObjectDefinitionStickerValue(
				otherProps.fileMimeTypeCssClasses,
				otherProps.objectDefinitionCssClasses,
				selectedAssets[0])
		),
		stickerSymbol: getFileMimeTypeObjectDefinitionStickerValue(
			otherProps.fileMimeTypeIcons,
			otherProps.objectDefinitionIcons,
			selectedAssets[0]
		),
		type: getAssetType(selectedAssets[0].embedded),
	}), [selectedAssets]);

	return (
		<AssetTypeInfoPanelContext.Provider value={{
			actions,
			asset,
			assetLibrary,
			cmsGroupId,
			commentsProps,
			selectedAssets,
			type,
		} as IAssetTypeInfoPanelContext}>
			<SidePanel.Header>
				<SidePanel.Title>
					<span className="inline-flex text-nowrap">
						<ClaySticker className={stickerClassName}>
							<ClayIcon symbol={stickerSymbol} />
						</ClaySticker>

						<span className="inline-item text-truncate-inline">
							<h3 className="inline-item-after mb-0 text-truncate">
								{asset?.title_i18n?.[
									 Liferay.ThemeDisplay.getLanguageId()] ||
								 asset?.title ||
								 Liferay.Language.get('untitled-asset')}
							</h3>
						</span>
					</span>
				</SidePanel.Title>
			</SidePanel.Header>

			{type === ASSET_TYPE.FOLDER
				? <AssetTypeInfoPanelFolderView/>
				: <AssetTypeInfoPanelFilesView/>
			}
		</AssetTypeInfoPanelContext.Provider>
	);
};

export default AssetTypeInfoPanelContent;
