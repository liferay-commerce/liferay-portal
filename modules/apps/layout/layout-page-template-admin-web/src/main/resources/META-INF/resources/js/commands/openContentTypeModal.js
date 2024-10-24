/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render} from '@liferay/frontend-js-react-web';
import React from 'react';

import ContentTypeModal from '../components/ContentTypeModal';
import {MODAL_TYPES} from '../constants/modalTypes';

let container;
let root;

/**
 * Opens a modal that will let the user choose the title
 * and the mapping types of a displaoy page.
 *
 * @param {string} param.displayPageName
 * @param {string} param.formSubmitURL
 * @param {array} param.mappingTypes
 * @param {string} param.namespace
 * @param {string} param.spritemap
 * @param {string} param.title
 */
export default function openContentTypeModal({
	description,
	disableWarning,
	displayPageName,
	formSubmitURL,
	mappingTypes,
	namespace,
	selectedSubtype,
	selectedType,
	title,
	type = MODAL_TYPES.create,
	warningMessage,
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

		document.body.appendChild(container);
	}

	root = render(
		<ContentTypeModal
			description={description}
			disableWarning={disableWarning}
			displayPageName={displayPageName}
			formSubmitURL={formSubmitURL}
			mappingTypes={mappingTypes}
			namespace={namespace}
			onClose={cleanUp}
			selectedSubtype={selectedSubtype}
			selectedType={selectedType}
			title={title}
			type={type}
			warningMessage={warningMessage}
		/>,
		{},
		container
	);

	Liferay.once('destroyPortlet', cleanUp);
}
