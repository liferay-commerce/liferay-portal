/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render} from '@liferay/frontend-js-react-web';
import React from 'react';

import CreationModal from './CreationModal';

const DEFAULT_MODAL_CONTAINER_ID = 'modalContainer';

interface Props {
	buttonLabel: string;
	descriptionInputValue: string;
	formSubmitURL: string;
	heading: string;
	nameInputValue: string;
	portletNamespace: string;
}
let root: any;

function getDefaultModalContainer() {
	let container = document.getElementById(DEFAULT_MODAL_CONTAINER_ID);

	if (!container) {
		container = document.createElement('div');
		container.id = DEFAULT_MODAL_CONTAINER_ID;
		document.body.appendChild(container);
	}

	return container;
}

export default function openCreationModal(props: Props) {
	const cleanUp = () => {
		if (root) {
			root.unmount();

			root = null;
		}
	};

	root = render(
		<CreationModal {...props} onCloseModal={cleanUp} />,
		props as any,
		getDefaultModalContainer()
	);
}
