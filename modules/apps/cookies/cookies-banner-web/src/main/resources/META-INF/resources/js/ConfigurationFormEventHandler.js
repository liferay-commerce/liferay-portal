/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {delegate} from 'frontend-js-web';

export default function ({namespace}) {
	const delegateHandler = delegate(
		document.body,
		'change',
		'input[type="checkbox"]',
		(event) => {
			const cookieManager = document.querySelector(
				`[name='${namespace}cookieManager']`
			);
			const delegatedConsentMode = document.querySelector(
				`input[type='checkbox'][name='${namespace}delegatedConsentMode']`
			);
			const explicitConsentMode = document.querySelector(
				`input[type='checkbox'][name='${namespace}explicitConsentMode']`
			);

			if (event.delegateTarget.id === `${namespace}enabled`) {
				delegatedConsentMode.checked = false;
				explicitConsentMode.checked = false;

				if (event.delegateTarget.checked) {
					cookieManager.removeAttribute('disabled');
					delegatedConsentMode.removeAttribute('disabled');
					explicitConsentMode.removeAttribute('disabled');
				}
				else {
					cookieManager.setAttribute('disabled', '');
					delegatedConsentMode.setAttribute('disabled', '');
					explicitConsentMode.setAttribute('disabled', '');
				}
			}

			if (event.delegateTarget.id === `${namespace}explicitConsentMode`) {
				delegatedConsentMode.checked = false;

				if (event.delegateTarget.checked) {
					delegatedConsentMode.removeAttribute('disabled');
				}
				else {
					delegatedConsentMode.setAttribute('disabled', '');
				}
			}
		}
	);

	return {
		dispose() {
			delegateHandler.dispose();
		},
	};
}
