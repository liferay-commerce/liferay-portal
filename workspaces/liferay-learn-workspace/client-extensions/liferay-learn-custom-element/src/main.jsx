/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {createRoot} from 'react-dom/client';
import {ClayIconSpriteContext} from '@clayui/icon';

import Routes from './Routes';
import getIconSpriteMap from './utils/getIconSpriteMap';

import './index.scss';

class WebComponent extends HTMLElement {
	connectedCallback() {
		if (!this.root) {
			this.root = createRoot(this);

			this.root.render(
				<ClayIconSpriteContext.Provider value={getIconSpriteMap()}>
					<Routes path={this.getAttribute('path')} />
				</ClayIconSpriteContext.Provider>
			);
		}
	}
}

const ELEMENT_ID = 'liferay-learn-custom-element';

if (!customElements.get(ELEMENT_ID)) {
	customElements.define(ELEMENT_ID, WebComponent);
}
