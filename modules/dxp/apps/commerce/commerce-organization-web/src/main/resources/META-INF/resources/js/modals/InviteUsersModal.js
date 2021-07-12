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

import ClayButton from '@clayui/button';
import ClayForm, {ClayInput, ClaySelectBox} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayModal from '@clayui/modal';
import ClayMultiSelect from '@clayui/multi-select';
import classNames from 'classnames';
import {openToast} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import {addUsersToOrganization} from '../data/organizations';
import {getAllUserRoles, getUsersByEmails} from '../data/users';
import {USER_ROLES_DEFINITION_ENABLED} from '../utils/flags';

export default function InviteUserModal({closeModal, observer, parentData}) {
	const [emailsQuery, setEmailsQuery] = useState('');
	const [selectedEmails, setSelectedEmails] = useState([]);

	const [selectedRoleIds, setSelectedRoleIds] = useState([]);
	const [roles, setRoles] = useState([]);
	const [errors, setErrors] = useState([]);

	useEffect(() => {
		if (USER_ROLES_DEFINITION_ENABLED) {
			getAllUserRoles().then((roles) => {
				const organizationRoles = roles.items.filter(
					(role) => role.roleType === 'organization'
				);

				setRoles(organizationRoles);
			});
		}
	}, [parentData]);

	function handleSave() {
		const typedEmails = selectedEmails
			.map((email) => email.emailAddress)
			.sort();

		getUsersByEmails(typedEmails)
			.then((response) => {
				const fetchedUsers = new Map();

				response.items.forEach((user) => {
					fetchedUsers.set(user.emailAddress, user);
				});

				const userIds = [];
				const errorMessages = [];

				typedEmails.forEach((typedEmail) => {
					if (fetchedUsers.has(typedEmail)) {
						userIds.push(fetchedUsers.get(typedEmail).id);
					}
					else {
						errorMessages.push(
							Liferay.Util.sub(
								Liferay.Language.get('x-not-found'),
								typedEmail
							)
						);
					}
				});

				setErrors(errorMessages);

				return userIds;
			})
			.then((usersIds) => {
				if (usersIds.length) {
					addUsersToOrganization(
						usersIds,
						parentData,
						selectedRoleIds
					);
				}
			});
	}

	return (
		<ClayModal center observer={observer} size="md">
			<ClayModal.Header>
				{Liferay.Language.get('invite-users')}
			</ClayModal.Header>
			<ClayModal.Body>
				<ClayForm.Group
					className={classNames(errors.length && 'has-error')}
				>
					<label htmlFor="inviteUsersEmailInput">
						{Liferay.Language.get('email')}
						<ClayIcon
							className="ml-1 reference-mark"
							symbol="asterisk"
						/>
					</label>
					<ClayInput.Group>
						<ClayInput.GroupItem>
							<ClayMultiSelect
								id="inviteUsersEmailInput"
								inputValue={emailsQuery}
								items={selectedEmails}
								locator={{
									label: 'emailAddress',
									value: 'emailAddress',
								}}
								onChange={setEmailsQuery}
								onItemsChange={(emails) => {
									const organizationEmails = new Set(
										parentData.userAccounts.map(
											(user) => user.emailAddress
										)
									);
									const filteredEmails = emails.filter(
										(email) => {
											if (organizationEmails.has(email)) {
												openToast({
													message: Liferay.Util.sub(
														Liferay.Language.get(
															'x-is-already-a-user-of-x'
														),
														email,
														parentData.name
													),
													type: 'danger',
												});

												return false;
											}

											return true;
										}
									);
									setSelectedEmails(filteredEmails);
								}}
								placeholder={Liferay.Language.get(
									'users-emails'
								)}
							/>
						</ClayInput.GroupItem>
					</ClayInput.Group>
				</ClayForm.Group>
				{USER_ROLES_DEFINITION_ENABLED && (
					<ClayForm.Group
						className={classNames(errors.length && 'has-error')}
					>
						<label htmlFor="inviteUsersRoleInput">
							{Liferay.Language.get('roles')}
							<ClayIcon
								className="ml-1 reference-mark"
								symbol="asterisk"
							/>
						</label>
						<ClaySelectBox
							id="inviteUsersRoleInput"
							items={roles.map((role) => ({
								label: role.name,
								value: role.id,
							}))}
							multiple
							onSelectChange={setSelectedRoleIds}
							size={5}
							value={selectedRoleIds}
						/>
						<ClayInput.Group>
							<ClayInput.GroupItem>
								{!!errors.length && (
									<ClayForm.FeedbackGroup>
										{errors.map((error, i) => (
											<ClayForm.FeedbackItem key={i}>
												<ClayForm.FeedbackIndicator symbol="info-circle" />
												{error}
											</ClayForm.FeedbackItem>
										))}
									</ClayForm.FeedbackGroup>
								)}
							</ClayInput.GroupItem>
						</ClayInput.Group>
					</ClayForm.Group>
				)}
			</ClayModal.Body>
			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton
							displayType="secondary"
							onClick={closeModal}
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>
						<ClayButton displayType="primary" onClick={handleSave}>
							{Liferay.Language.get('save')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</ClayModal>
	);
}
