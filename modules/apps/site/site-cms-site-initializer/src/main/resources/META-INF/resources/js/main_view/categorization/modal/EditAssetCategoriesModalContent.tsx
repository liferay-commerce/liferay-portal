/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayModal from '@clayui/modal';
import {sub} from 'frontend-js-web';
import React, {useCallback, useEffect, useMemo, useState} from 'react';

import {
    IBulkActionFDSData,
    IBulkActionTaskStarterDTO,
} from '../../../common/types/BulkActionTask';
import {displayErrorToast} from '../../../common/utils/toastUtil';
import AssetCategories from '../../info_panel/components/AssetCategories';
import {EntryCategorizationDTO} from '../../info_panel/services/ObjectEntryService';
import {triggerAssetBulkAction} from '../../props_transformer/actions/triggerAssetBulkAction';
import {ClayRadio, ClayRadioGroup} from "@clayui/form";
import VocabularyService from "../../../common/services/VocabularyService";
import {IAssetObjectEntry} from "../../../common/types/AssetType";

export default function EditAssetCategoriesModalContent({
    apiURL,
    assetLibraries,
    closeModal,
    cmsGroupId,
    selectedData,
}: {
    apiURL?: string;
    assetLibraries: any;
    closeModal: () => void;
    cmsGroupId: number;
    selectedData: IBulkActionFDSData;
}) {
    const [categorizationDTO, setCategorizationDTO] =
        useState<EntryCategorizationDTO>({
            keywords: [],
            taxonomyCategoryBriefs: [],
            taxonomyCategoryIds: [],
        } as unknown as EntryCategorizationDTO);
    const [submitDisabled, setSubmitDisabled] = useState<boolean>(false);
    const [selectedOperation, setSelectedOperation] = useState<string>('add');

    const assetTypes: number[] = useMemo(() => {
        const {items = []} = selectedData;

        const classNameIds = new Set([
            ...items.map(({embedded: {systemProperties: {objectDefinitionBrief}}}) =>
                objectDefinitionBrief?.classNameId || 0
            )
        ]);

        if (classNameIds.size === 1) {
            return [...classNameIds];
        }

        return null;
    }, []);

    const scopeId: number | null = useMemo(() => {
        const {items = []} = selectedData;

        const scopeIds = new Set([
            ...items.map(({embedded: {scopeId}}) => scopeId)
        ]);

        if (scopeIds.size === 1) {
            return [...scopeIds][0];
        }

        return null;
    }, [selectedData.items]);

    const doBulkSubmit = useCallback(async () => {
        setSubmitDisabled(true);

        if (categorizationDTO?.taxonomyCategoryIds?.length) {
            triggerAssetBulkAction({
                apiURL,
                keyValues: {
                    append: selectedOperation === 'add',
                    taxonomyCategoryIdsToAdd: categorizationDTO.taxonomyCategoryIdsToAdd,
                    taxonomyCategoryIdsToRemove: categorizationDTO.taxonomyCategoryIdsToRemove,
                },
                onCreateError: ({error}) => {
                    setSubmitDisabled(false);

                    displayErrorToast(error as string);
                },
                onCreateSuccess: (response) => {
                    if (response.error) {
                        setSubmitDisabled(false);

                        displayErrorToast(response.error as string);

                        return;
                    }

                    closeModal();
                },
                overrideDefaultErrorToast: true,
                overrideDefaultSuccessToast: true,
                selectedData,
                type: 'TaxonomyCategoryBulkAction',
            } as IBulkActionTaskStarterDTO<'TaxonomyCategoryBulkAction'>);
        }
    }, [
        apiURL,
        categorizationDTO,
        closeModal,
        selectedData,
        setSubmitDisabled,
    ]);

    const getCommonEntries = useCallback(async () => {
        try {
        } catch(error) {

        }



    }, [cmsGroupId, scopeId]);

    useEffect(() => {
        function getCommonCategories() {
            const response = await VocabularyService.getCommonCategories(cmsGroupId);
        }
    }, [cmsGroupId, scopeId]);

    const updateLocalObjectEntry = useCallback(
        ({
             keywords,
             lastAddedBrief,
             taxonomyCategoryIds,
             taxonomyCategoryIdsToAdd,
             keywordsToAdd,
             taxonomyCategoryIdsToRemove,
             keywordsToRemove,
         }: EntryCategorizationDTO): void => {
            setCategorizationDTO(
                ({
                     keywords: currentKeywords,
                     taxonomyCategoryBriefs = [],
                     taxonomyCategoryIds: currentTaxonomyCategoryIds,
                 }) => ({
                    keywords: keywords || currentKeywords!,
                    taxonomyCategoryBriefs: [
                        ...taxonomyCategoryBriefs,
                        ...(lastAddedBrief
                            ? [
                                {
                                    embeddedTaxonomyCategory:
                                    lastAddedBrief,
                                },
                            ]
                            : []),
                    ],
                    taxonomyCategoryIds:
                        taxonomyCategoryIds || currentTaxonomyCategoryIds,
                    taxonomyCategoryIdsToAdd,
                    keywordsToAdd: keywordsToAdd,
                    taxonomyCategoryIdsToRemove,
                    keywordsToRemove,
                } as EntryCategorizationDTO)
            );
        },
        [setCategorizationDTO]
    );

    return (
        <>
            <ClayModal.Header
                closeButtonAriaLabel={Liferay.Language.get('close')}
            >
                {Liferay.Language.get('edit-categories')}
            </ClayModal.Header>

            <ClayModal.Body>
                <ClayRadioGroup
                    name="add-replace"
                    onChange={(value) => setSelectedOperation(value as string)}
                    value={selectedOperation}
                >
                    <ClayRadio
                        checked={true}
                        label={Liferay.Language.get('edit')}
                        value="add"
                    >
                        <div className="form-text">
                            {Liferay.Language.get(
                                'add-new-categories-or-remove-common-categories'
                            )}
                        </div>
                    </ClayRadio>

                    <ClayRadio
                        label={Liferay.Language.get('replace')}
                        value="replace"
                    >
                        <div className="form-text">
                            {Liferay.Language.get(
                                'these-categories-replace-all-existing-categories'
                            )}
                        </div>
                    </ClayRadio>
                </ClayRadioGroup>

                <AssetCategories
                    collapsable={false}
                    cmsGroupId={cmsGroupId}
                    objectEntry={categorizationDTO}
                    updateObjectEntry={updateLocalObjectEntry}
                />
            </ClayModal.Body>

            <ClayModal.Footer
                last={
                    <ClayButton.Group spaced>
                        <ClayButton
                            displayType="secondary"
                            onClick={closeModal}
                        >
                            {Liferay.Language.get('cancel')}
                        </ClayButton>

                        <ClayButton
                            disabled={
                                !categorizationDTO?.taxonomyCategoryIds?.length ||
                                submitDisabled
                            }
                            displayType="primary"
                            onClick={doBulkSubmit}
                            type="button"
                        >
                            {selectedData.selectAll
                                ? Liferay.Language.get('add-to-all-assets')
                                : selectedData?.items?.length === 1
                                    ? Liferay.Language.get('add-to-1-asset')
                                    : sub(
                                        Liferay.Language.get(
                                            'add-to-x-assets'
                                        ),
                                        selectedData?.items?.length
                                    )}
                        </ClayButton>
                    </ClayButton.Group>
                }
            />
        </>
    );
}
