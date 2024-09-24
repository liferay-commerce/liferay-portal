/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openToast} from 'frontend-js-web';

export default function ({checkoutURL, reorderURL}) {
	const handleRedirect = ({id, orderId}) => {
		if (id === 'checkout') {
			window.location.href = checkoutURL;
		}
		else if (id === 'reorder') {
			if (!orderId) {
				openToast({
					message: Liferay.Language.get('order-created-successfully'),
					type: 'info',
				});

				return;
			}

			reorderURL += orderId;

			window.location.href = reorderURL;
		}
	};

	Liferay.on('current-order-action-triggered', handleRedirect);
	Liferay.on('fds-action-performed', handleRedirect);

	return {
		dispose: () => {
			Liferay.detach('current-order-action-triggered', handleRedirect);
			Liferay.detach('fds-action-performed', handleRedirect);
		},
	};
}
