/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render} from '@liferay/frontend-js-react-web';
import React from 'react';

import TemplateModal from './TemplateModal';

const DEFAULT_MODAL_CONTAINER_ID = 'templateModal';

let container;
let root;

export default function openTemplateModal({
	addTemplateEntryURL,
	itemTypes,
	namespace,
}) {
	const cleanUp = () => {
		if (container && root) {
			root.unmount();

			document.body.removeChild(container);

			root = null;
			container = null;
		}
	};

	if (!container) {
		container = document.createElement('div');
		container.id = DEFAULT_MODAL_CONTAINER_ID;

		document.body.appendChild(container);
	}

	root = render(
		<TemplateModal
			addTemplateEntryURL={addTemplateEntryURL}
			itemTypes={itemTypes}
			namespace={namespace}
			onModalClose={cleanUp}
		/>,
		{},
		container
	);
}
