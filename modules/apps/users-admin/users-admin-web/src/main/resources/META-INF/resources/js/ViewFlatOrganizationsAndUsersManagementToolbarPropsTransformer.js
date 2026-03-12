/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openSelectionModal} from 'frontend-js-components-web';
import {createPortletURL, navigate} from 'frontend-js-web';

import {ACTIONS} from './actions';

export default function propsTransformer({portletNamespace, ...otherProps}) {
	const selectAccountEntries = (itemData) => {
		openSelectionModal({
			multiple: true,
			onSelect: (selectedItems) => {
				if (!selectedItems?.length) {
					return;
				}

				const values = selectedItems.map((item) => item.value);

				const redirectURL = createPortletURL(itemData?.redirectURL, {
					accountEntryIds: values.join(','),
					selection: 'selected-account-users',
				});

				navigate(redirectURL);
			},
			title: itemData?.dialogTitle,
			url: itemData?.accountEntriesSelectorURL,
		});
	};

	const selectOrganizations = (itemData) => {
		openSelectionModal({
			multiple: true,
			onSelect: (selectedItems) => {
				if (!selectedItems?.length) {
					return;
				}

				const values = selectedItems.map((item) => item.value);

				const redirectURL = createPortletURL(itemData?.redirectURL, {
					organizationIds: values.join(','),
					selection: 'selected-organization-users',
				});

				navigate(redirectURL);
			},
			title: itemData?.dialogTitle,
			url: itemData?.organizationsSelectorURL,
		});
	};

	return {
		...otherProps,
		onActionButtonClick: (event, {item}) => {
			const data = item?.data;

			const action = data?.action;

			if (action) {
				event.preventDefault();

				ACTIONS[action](data, portletNamespace);
			}
		},
		onFilterDropdownItemClick(event, {item}) {
			if (item?.data?.action === 'selectAccountEntries') {
				selectAccountEntries(item?.data);
			}
			else if (item?.data?.action === 'selectOrganizations') {
				selectOrganizations(item?.data);
			}
		},
	};
}
