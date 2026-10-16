/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import PropTypes from 'prop-types';
import React from 'react';

import SignatureStatusLabel from '../signature_details/SignatureStatusLabel';

export default function SignatureStatusDataRenderer({
	itemData,
	signatureStatuses,
}) {
	return <SignatureStatusLabel status={signatureStatuses?.[itemData?.id]} />;
}

SignatureStatusDataRenderer.propTypes = {
	itemData: PropTypes.object,
	signatureStatuses: PropTypes.object,
};
