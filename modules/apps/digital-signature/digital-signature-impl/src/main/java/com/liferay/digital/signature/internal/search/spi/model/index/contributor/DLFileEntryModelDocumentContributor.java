/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.search.spi.model.index.contributor;

import com.liferay.digital.signature.model.DSRequest;
import com.liferay.digital.signature.model.DSRequestRecipient;
import com.liferay.digital.signature.request.DSRequestManager;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(
	property = "indexer.class.name=com.liferay.document.library.kernel.model.DLFileEntry",
	service = ModelDocumentContributor.class
)
public class DLFileEntryModelDocumentContributor
	implements ModelDocumentContributor<DLFileEntry> {

	@Override
	public void contribute(Document document, DLFileEntry dlFileEntry) {
		DSRequest dsRequest = _dsRequestManager.fetchDSRequest(
			dlFileEntry.getCompanyId(), dlFileEntry.getFileEntryId());

		if (dsRequest == null) {
			return;
		}

		String status = dsRequest.getStatus();

		if (Validator.isNotNull(status)) {
			document.addKeyword("signatureStatus", status);
		}

		if (dsRequest.isTerminal()) {
			return;
		}

		List<DSRequestRecipient> dsRequestRecipients =
			dsRequest.getDSRequestRecipients();

		if (ListUtil.isEmpty(dsRequestRecipients)) {
			return;
		}

		document.addKeyword(
			"signatureRecipientStatuses",
			TransformUtil.transformToArray(
				dsRequestRecipients,
				dsRequestRecipient -> {
					long userId = dsRequestRecipient.getUserId();

					if (userId <= 0) {
						return null;
					}

					return userId + "_" + dsRequestRecipient.getStatus();
				},
				String.class));
	}

	@Reference
	private DSRequestManager _dsRequestManager;

}