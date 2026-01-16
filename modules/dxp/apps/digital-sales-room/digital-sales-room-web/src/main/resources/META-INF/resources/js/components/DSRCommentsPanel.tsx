/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import {ClayDropDownWithItems} from '@clayui/drop-down';
import ClayEmptyState from '@clayui/empty-state';
import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import Sticker from '@clayui/sticker';
import classNames from 'classnames';
import {openToast} from 'frontend-js-components-web';
import React, {useEffect, useState} from 'react';

import '../../css/main.scss';
import DigitalSalesRoomService, {
	TCommentDTO,
} from '../commons/DigitalSalesRoomService';

function formatDate(date: string, languageTag: string): string {
	return (
		date &&
		languageTag &&
		Intl.DateTimeFormat(languageTag.replace(/_.*/, ''), {
			day: 'numeric',
			hour: 'numeric',
			hour12: true,
			minute: 'numeric',
			month: 'short',
			year: 'numeric',
		}).format(new Date(date))
	);
}

function DSRCommentsPanel({roomId}: {roomId: number}) {
	const [comments, setComments] = useState<Array<TCommentDTO>>([]);
	const [commentToEditId, setCommentToEditId] = useState<number | undefined>(
		undefined
	);
	const [page, setPage] = useState(1);
	const [reload, setReload] = useState(0);
	const [showLoadMore, setShowLoadMore] = useState(false);

	useEffect(() => {
		DigitalSalesRoomService.getComments(roomId, page)
			.then((data) => {
				setComments((prevState) => {
					if (page === 1) {
						return data.items;
					}

					return prevState.concat(data.items);
				});
				setShowLoadMore(page < data.lastPage);
			})
			.catch((error) => {
				openToast({
					message: (error as Error).message,
					type: 'danger',
				});
			});
	}, [page, reload, roomId]);

	const handleDeleteComment = async (commentId: number) => {
		try {
			const response =
				await DigitalSalesRoomService.deleteDigitalSalesRoomComment(
					roomId,
					commentId
				);

			if (response.error) {
				throw new Error(response.error);
			}

			openToast({
				message: Liferay.Language.get(
					'your-request-completed-successfully'
				),
				type: 'success',
			});

			setComments((prevState) =>
				prevState.filter((item) => item.id !== commentId)
			);
			setPage((prevPage) => {
				if (prevPage !== 1) {
					return 1;
				}
				else {
					setReload((prev) => prev + 1);

					return prevPage;
				}
			});

			if (commentId === commentToEditId) {
				setCommentToEditId(undefined);
			}
		}
		catch (error) {
			openToast({
				message: (error as Error).message,
				type: 'danger',
			});
		}
	};

	const handleEditComment = async (
		comment: string,
		id: number,
		roomId: number
	) => {
		try {
			const data =
				await DigitalSalesRoomService.patchDigitalSalesRoomComment(
					id,
					roomId,
					comment
				);

			setComments((prevState) =>
				prevState.map((item) => (item.id === id ? data : item))
			);
			setCommentToEditId(undefined);

			openToast({
				message: Liferay.Language.get(
					'your-request-completed-successfully'
				),
				type: 'success',
			});
		}
		catch (error) {
			openToast({
				message: (error as Error).message,
				type: 'danger',
			});
		}
	};

	const handleSaveComment = async (comment: string, roomId: number) => {
		try {
			const data =
				await DigitalSalesRoomService.postDigitalSalesRoomComment(
					roomId,
					comment
				);

			setComments((prevState) => {
				const newLength = prevState.length + 1;

				if (newLength <= 20 || page > 1) {
					return [data, ...prevState];
				}
				else {
					setReload((prev) => prev + 1);

					return prevState;
				}
			});

			openToast({
				message: Liferay.Language.get(
					'your-request-completed-successfully'
				),
				type: 'success',
			});
		}
		catch (error) {
			openToast({
				message: (error as Error).message,
				type: 'danger',
			});
		}
	};

	return (
		<>
			<div className="dsr-comments-content">
				{comments.length ? (
					<ul className="p-0">
						{comments.map((comment) => (
							<DSRCommentNode
								comment={comment}
								key={comment.id}
								onDelete={() => handleDeleteComment(comment.id)}
								onEdit={async (commentId: number) => {
									setCommentToEditId(commentId);
								}}
							/>
						))}
					</ul>
				) : (
					<ClayEmptyState
						description={Liferay.Language.get(
							'sorry,-no-results-were-found'
						)}
						imgSrc={
							Liferay.ThemeDisplay.getPathThemeImages() +
							'/states/search_state.svg'
						}
						title={Liferay.Language.get('no-results-found')}
					/>
				)}

				{showLoadMore && (
					<ClayButton
						className="btn-block"
						data-qa-id="loadMoreButton"
						displayType="secondary"
						onClick={() => {
							setPage((prev) => prev + 1);
						}}
						size="sm"
					>
						{Liferay.Language.get('load-more')}
					</ClayButton>
				)}
			</div>
			<DSRCommentEditor
				commentText={
					commentToEditId
						? comments.find(
								(comment) => comment.id === commentToEditId
							)?.text || ''
						: ''
				}
				onEdit={(commentText, commentId) =>
					handleEditComment(commentText, commentId, roomId)
				}
				onSave={(comment) => handleSaveComment(comment, roomId)}
				originalCommentId={commentToEditId}
			/>
		</>
	);
}

function DSRCommentNode({
	comment,
	onDelete,
	onEdit,
}: {
	comment: TCommentDTO;
	onDelete: (commentId: number) => Promise<void>;
	onEdit: (commentId: number) => Promise<void>;
}) {
	const isOwner =
		comment.creator.id === Number(Liferay.ThemeDisplay.getUserId());

	return (
		<>
			<li className={classNames('list-unstyled border-bottom pb-3')}>
				<article>
					<div className="autofit-padded autofit-row mb-1 pt-2">
						<div className="pl-0 pt-1">
							<Sticker shape="user-icon">
								{comment.creator.image ? (
									<Sticker.Image
										alt={comment.creator.name}
										src={comment.creator.image}
									/>
								) : (
									<ClayIcon symbol="user" />
								)}
							</Sticker>
						</div>

						<header className="autofit-col autofit-col-expand">
							<span className="list-group-title">
								{comment.creator.name}
							</span>

							<time className="list-group-text text-3">
								{formatDate(
									comment.dateCreated,
									Liferay.ThemeDisplay.getLanguageId()
								)}
							</time>
						</header>

						{isOwner && (
							<ClayDropDownWithItems
								items={[
									{
										label: Liferay.Language.get('edit'),
										onClick: async () => {
											await onEdit(comment.id);
										},
										symbolLeft: 'pencil',
									},
									{
										label: Liferay.Language.get('delete'),
										onClick: async () =>
											await onDelete(comment.id),
										symbolLeft: 'trash',
									},
								]}
								menuWidth="shrink"
								trigger={
									<ClayButtonWithIcon
										borderless
										data-qa-id="comment-actions"
										displayType="secondary"
										monospaced
										size="xs"
										symbol="ellipsis-v"
										title={Liferay.Language.get('actions')}
									/>
								}
							/>
						)}
					</div>

					<pre className="dsr-comment-body my-3 text-3">
						{comment.text}
					</pre>
				</article>
			</li>
		</>
	);
}

function DSRCommentEditor({
	commentText,
	onEdit,
	onSave,
	originalCommentId,
}: {
	commentText: string;
	onEdit: (comment: string, commentId: number) => Promise<void>;
	onSave: (comment: string) => Promise<void>;
	originalCommentId?: number;
}) {
	const [comment, setComment] = useState(commentText);
	const [disabled, setDisabled] = useState<boolean>(false);

	useEffect(() => {
		setComment(commentText);
	}, [commentText]);

	return (
		<div className="dsr-comment-editor">
			<div className="py-2">
				<strong>{Liferay.Language.get('add-comment')}</strong>
			</div>

			<ClayInput
				className="form-control form-control-sm"
				component="textarea"
				data-qa-id="commentTextarea"
				onChange={(event) => {
					setComment(event.target.value);
				}}
				placeholder={Liferay.Language.get('type-your-comment-here')}
				value={comment}
			></ClayInput>

			<div className="my-3">
				<ClayButton
					disabled={disabled || !comment.trim()}
					onClick={async () => {
						setDisabled(true);
						try {
							if (originalCommentId !== undefined) {
								await onEdit(comment.trim(), originalCommentId);
							}
							else {
								await onSave(comment.trim());
							}
							setComment('');
						}
						finally {
							setDisabled(false);
						}
					}}
					size="sm"
				>
					{Liferay.Language.get('save')}
				</ClayButton>

				<ClayButton
					borderless
					className="ml-1"
					displayType="secondary"
					onClick={() => {
						setComment('');
					}}
					size="sm"
				>
					{Liferay.Language.get('cancel')}
				</ClayButton>
			</div>
		</div>
	);
}

export default DSRCommentsPanel;
