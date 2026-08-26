/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import AttachmentsFDSPropsTransformer from '../../src/main/resources/META-INF/resources/js/AttachmentsFDSPropsTransformer';

const download = (itemData) =>
	AttachmentsFDSPropsTransformer({}).onActionDropdownItemClick({
		action: {data: {id: 'download'}},
		itemData,
	});

describe('AttachmentsFDSPropsTransformer', () => {
	beforeEach(() => {
		delete window.location;

		window.location = {href: '', origin: 'http://localhost'};
	});

	it('adds the auth token to the download URL', () => {
		download({
			url: 'http://localhost/o/headless-commerce-admin-order/v1.0/orders/1/attachments/2/content',
		});

		expect(window.location.href).toBe(
			'http://localhost/o/headless-commerce-admin-order/v1.0/orders/1/attachments/2/content?p_auth=default-mocked-auth-token'
		);
	});

	it('replaces an auth token already on the download URL', () => {
		download({
			url: 'http://localhost/o/headless-commerce-admin-order/v1.0/orders/1/attachments/2/content?p_auth=stale',
		});

		expect(window.location.href).toBe(
			'http://localhost/o/headless-commerce-admin-order/v1.0/orders/1/attachments/2/content?p_auth=default-mocked-auth-token'
		);
	});

	it('stays on the page when the attachment has no URL', () => {
		download({});

		expect(window.location.href).toBe('');
	});
});
