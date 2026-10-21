/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.frontend.data.set.provider;

import com.liferay.dispatch.executor.DispatchTaskStatus;
import com.liferay.dispatch.model.DispatchLog;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.dispatch.service.DispatchLogLocalService;
import com.liferay.frontend.data.set.provider.FDSDataProvider;
import com.liferay.frontend.data.set.provider.search.FDSKeywords;
import com.liferay.frontend.data.set.provider.search.FDSPagination;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.DefaultObjectEntryManagerProvider;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManagerRegistry;
import com.liferay.object.rest.odata.entity.v1_0.provider.EntityModelProvider;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.odata.sort.SortParserProvider;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.SortUtil;
import com.liferay.site.pim.site.initializer.constants.PIMObjectDefinitionConstants;
import com.liferay.site.pim.site.initializer.internal.constants.PIMFDSNames;
import com.liferay.site.pim.site.initializer.internal.frontend.data.set.model.PIMConnectorDisplay;
import com.liferay.site.pim.site.initializer.internal.util.PIMConnectorDispatchTriggerUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tancredi Covioli
 */
@Component(
	property = "fds.data.provider.key=" + PIMFDSNames.CONNECTORS,
	service = FDSDataProvider.class
)
public class PIMConnectorFDSDataProvider
	implements FDSDataProvider<PIMConnectorDisplay> {

	@Override
	public List<PIMConnectorDisplay> getItems(
			FDSKeywords fdsKeywords, FDSPagination fdsPagination,
			HttpServletRequest httpServletRequest, Sort sort)
		throws PortalException {

		ObjectDefinition objectDefinition = _fetchObjectDefinition(
			httpServletRequest);

		if (objectDefinition == null) {
			return Collections.emptyList();
		}

		Page<ObjectEntry> page = _getObjectEntriesPage(
			fdsKeywords, httpServletRequest, objectDefinition,
			Pagination.of(fdsPagination.getPage(), fdsPagination.getPageSize()),
			sort);

		return TransformUtil.transform(
			page.getItems(),
			objectEntry -> _toPIMConnectorDisplay(
				objectDefinition, objectEntry));
	}

	@Override
	public int getItemsCount(
			FDSKeywords fdsKeywords, HttpServletRequest httpServletRequest)
		throws PortalException {

		ObjectDefinition objectDefinition = _fetchObjectDefinition(
			httpServletRequest);

		if (objectDefinition == null) {
			return 0;
		}

		Page<ObjectEntry> page = _getObjectEntriesPage(
			fdsKeywords, httpServletRequest, objectDefinition,
			Pagination.of(1, 1), null);

		return (int)page.getTotalCount();
	}

	private ObjectDefinition _fetchObjectDefinition(
		HttpServletRequest httpServletRequest) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return _objectDefinitionLocalService.
			fetchObjectDefinitionByExternalReferenceCode(
				PIMObjectDefinitionConstants.EXTERNAL_REFERENCE_CODE_CONNECTOR,
				themeDisplay.getCompanyId());
	}

	private Map<String, Map<String, String>> _getActions(
			ObjectDefinition objectDefinition, long objectEntryId)
		throws PortalException {

		Map<String, Map<String, String>> actions = new HashMap<>();

		String href = StringBundler.concat(
			"/o", objectDefinition.getRESTContextPath(), "/", objectEntryId);

		if (_objectEntryService.hasModelResourcePermission(
				objectDefinition.getObjectDefinitionId(), objectEntryId,
				ActionKeys.DELETE)) {

			actions.put(
				"delete",
				HashMapBuilder.put(
					"href", href
				).put(
					"method", "DELETE"
				).build());
		}

		if (_objectEntryService.hasModelResourcePermission(
				objectDefinition.getObjectDefinitionId(), objectEntryId,
				ActionKeys.UPDATE)) {

			actions.put(
				"update",
				HashMapBuilder.put(
					"href", href
				).put(
					"method", "PATCH"
				).build());
		}

		return actions;
	}

	private String _getLastExecution(DispatchLog dispatchLog) {
		if (dispatchLog == null) {
			return "never-executed";
		}

		if (dispatchLog.getStatus() == DispatchTaskStatus.FAILED.getStatus()) {
			return "failed";
		}

		return "success";
	}

	private DispatchLog _getLatestDispatchLog(
		long companyId, String externalReferenceCode) {

		DispatchTrigger dispatchTrigger =
			PIMConnectorDispatchTriggerUtil.fetchDispatchTrigger(
				companyId, externalReferenceCode);

		if (dispatchTrigger == null) {
			return null;
		}

		DispatchLog failedDispatchLog =
			_dispatchLogLocalService.fetchLatestDispatchLog(
				dispatchTrigger.getDispatchTriggerId(),
				DispatchTaskStatus.FAILED);
		DispatchLog successfulDispatchLog =
			_dispatchLogLocalService.fetchLatestDispatchLog(
				dispatchTrigger.getDispatchTriggerId(),
				DispatchTaskStatus.SUCCESSFUL);

		if (failedDispatchLog == null) {
			return successfulDispatchLog;
		}

		if (successfulDispatchLog == null) {
			return failedDispatchLog;
		}

		Date failedStartDate = failedDispatchLog.getStartDate();

		if (failedStartDate.after(successfulDispatchLog.getStartDate())) {
			return failedDispatchLog;
		}

		return successfulDispatchLog;
	}

	private Page<ObjectEntry> _getObjectEntriesPage(
			FDSKeywords fdsKeywords, HttpServletRequest httpServletRequest,
			ObjectDefinition objectDefinition, Pagination pagination, Sort sort)
		throws PortalException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		ObjectEntryManager objectEntryManager =
			DefaultObjectEntryManagerProvider.provide(
				_objectEntryManagerRegistry.getObjectEntryManager(
					objectDefinition.getCompanyId(),
					objectDefinition.getStorageType()));

		try {
			return objectEntryManager.getObjectEntries(
				objectDefinition.getCompanyId(), objectDefinition, null, null,
				new DefaultDTOConverterContext(
					false, null, null, httpServletRequest, null,
					themeDisplay.getLocale(), null, themeDisplay.getUser()),
				ParamUtil.getString(httpServletRequest, "filter"), pagination,
				fdsKeywords.getKeywords(),
				_getSorts(themeDisplay.getLocale(), objectDefinition, sort));
		}
		catch (PortalException portalException) {
			throw portalException;
		}
		catch (Exception exception) {
			throw new PortalException(exception);
		}
	}

	private Sort[] _getSorts(
		Locale locale, ObjectDefinition objectDefinition, Sort sort) {

		if (sort == null) {
			return null;
		}

		EntityModel entityModel = _entityModelProvider.getEntityModel(
			objectDefinition);

		return SortUtil.getSorts(
			new AcceptLanguage() {

				@Override
				public List<Locale> getLocales() {
					return Collections.singletonList(locale);
				}

				@Override
				public String getPreferredLanguageId() {
					return LocaleUtil.toLanguageId(locale);
				}

				@Override
				public Locale getPreferredLocale() {
					return locale;
				}

			},
			entityModel, _sortParserProvider.provide(entityModel),
			StringBundler.concat(
				sort.getFieldName(), ":", sort.isReverse() ? "desc" : "asc"));
	}

	private PIMConnectorDisplay _toPIMConnectorDisplay(
			ObjectDefinition objectDefinition, ObjectEntry objectEntry)
		throws PortalException {

		DispatchLog dispatchLog = _getLatestDispatchLog(
			objectDefinition.getCompanyId(),
			objectEntry.getExternalReferenceCode());
		Map<String, Object> properties = objectEntry.getProperties();

		return new PIMConnectorDisplay(
			_getActions(objectDefinition, objectEntry.getId()),
			GetterUtil.getBoolean(properties.get("active")),
			objectEntry.getDateModified(), objectEntry.getId(),
			GetterUtil.getString(properties.get("key")),
			_getLastExecution(dispatchLog),
			(dispatchLog == null) ? null : dispatchLog.getStartDate(),
			GetterUtil.getString(properties.get("name")));
	}

	@Reference
	private DispatchLogLocalService _dispatchLogLocalService;

	@Reference
	private EntityModelProvider _entityModelProvider;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryManagerRegistry _objectEntryManagerRegistry;

	@Reference
	private ObjectEntryService _objectEntryService;

	@Reference
	private SortParserProvider _sortParserProvider;

}