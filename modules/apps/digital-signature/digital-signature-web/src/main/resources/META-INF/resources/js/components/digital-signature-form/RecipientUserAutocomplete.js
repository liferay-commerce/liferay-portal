/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAutocomplete from '@clayui/autocomplete';
import {FieldBase} from 'frontend-js-components-web';
import React, {useEffect, useRef, useState} from 'react';

const RecipientUserAutocomplete = ({
	error,
	excludedUserIds = [],
	id,
	label,
	onSelectUser,
	searchUsers,
	value = '',
}) => {
	const [query, setQuery] = useState(value);
	const [users, setUsers] = useState([]);

	const searchUsersRef = useRef(searchUsers);
	const selectedEmailAddressRef = useRef(value);

	const excludedUserIdsKey = excludedUserIds.join();

	useEffect(() => {
		searchUsersRef.current = searchUsers;
	}, [searchUsers]);

	useEffect(() => {
		if (value === selectedEmailAddressRef.current) {
			return;
		}

		selectedEmailAddressRef.current = value;

		setQuery(value);
	}, [value]);

	useEffect(() => {
		let active = true;

		const timeoutId = setTimeout(() => {
			searchUsersRef
				.current(query)
				.then((nextUsers) => {
					if (active) {
						setUsers(nextUsers);
					}
				})
				.catch(() => {
					if (active) {
						setUsers([]);
					}
				});
		}, 300);

		return () => {
			active = false;

			clearTimeout(timeoutId);
		};
	}, [excludedUserIdsKey, query]);

	return (
		<FieldBase
			className="mb-0 w-100"
			errorMessage={error}
			id={id}
			label={label}
			required
		>
			<ClayAutocomplete
				allowsCustomValue
				filterKey="emailAddress"
				id={id}
				items={users.filter(
					({userId}) => !userId || !excludedUserIds.includes(userId)
				)}
				menuTrigger="focus"
				messages={{
					loading: Liferay.Language.get('loading'),
					notFound: Liferay.Language.get('no-results-found'),
				}}
				onActiveChange={(active) => {
					if (!active && !selectedEmailAddressRef.current) {
						setQuery('');
					}
				}}
				onChange={(nextQuery) => {
					setQuery(nextQuery);

					if (
						selectedEmailAddressRef.current &&
						nextQuery !== selectedEmailAddressRef.current
					) {
						selectedEmailAddressRef.current = '';

						onSelectUser({emailAddress: '', name: ''});
					}
				}}
				placeholder={Liferay.Language.get('search-for')}
				value={query}
			>
				{(user) => (
					<ClayAutocomplete.Item
						key={user.userId}
						onClick={() => {
							selectedEmailAddressRef.current = user.emailAddress;

							setQuery(user.emailAddress);
							onSelectUser(user);
						}}
						textValue={user.emailAddress}
					>
						<div>{`${user.name} (${user.emailAddress})`}</div>
					</ClayAutocomplete.Item>
				)}
			</ClayAutocomplete>
		</FieldBase>
	);
};

export default RecipientUserAutocomplete;
