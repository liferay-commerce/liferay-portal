/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

import {fetchFromHeadless} from '../utils/fetch';

export const USERS_ROOT_ENDPOINT = '/o/headless-admin-user/v1.0/user-accounts';
export const ROLES_ROOT_ENDPOINT = '/o/headless-admin-user/v1.0/roles';

export function getUsersByEmails(emails) {
	const filterString = emails
		.map((email) => `emailAddress eq '${email}'`)
		.join(' or ');
	const url = new URL(USERS_ROOT_ENDPOINT, themeDisplay.getPortalURL());

	url.searchParams.append('filter', filterString);

	return fetchFromHeadless(url);
}

export function getUsersRoles(page = 1) {
	const url = new URL(ROLES_ROOT_ENDPOINT, themeDisplay.getPortalURL());

	url.searchParams.append('page', page);
	url.searchParams.append('pageSize', 100);

	return fetchFromHeadless(url, {}, null, true);
}

export function getAllUserRoles() {
	return getUsersRoles().then((firstResponse) => {
		if (firstResponse.page === firstResponse.lastPage) {
			return firstResponse;
		}

		const requests = [];

		for (
			let currentPage = 2;
			currentPage <= firstResponse.lastPage;
			currentPage++
		) {
			requests.push(getUsersRoles(currentPage));
		}

		return Promise.allSettled(requests).then((results) => {
			const errors = [];
			const roles = results.reduce((values, result) => {
				if (result.status === 'fulfilled') {
					return [...values, ...result.value.items];
				}
				else {
					errors.push(result.reason);

					return values;
				}
			}, []);

			if (errors.length) {
				throw new Error(errors);
			}

			return {
				items: [...firstResponse.items, ...roles],
			};
		});
	});
}

export function getUsers(query) {
	const url = new URL(USERS_ROOT_ENDPOINT, themeDisplay.getPortalURL());

	url.searchParams.append('search', query);
	url.searchParams.append('pageSize', 20);

	return fetchFromHeadless(url);
}
