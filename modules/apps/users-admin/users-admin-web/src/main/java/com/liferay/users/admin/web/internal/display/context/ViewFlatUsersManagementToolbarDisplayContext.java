/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.users.admin.web.internal.display.context;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalServiceUtil;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.SearchContainerManagementToolbarDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.LabelItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.LabelItemList;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.OrganizationLocalServiceUtil;
import com.liferay.portal.kernel.service.permission.PortalPermissionUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.users.admin.search.UserSearchTerms;
import com.liferay.users.admin.web.internal.util.DisplayStyleUtil;

import jakarta.portlet.PortletURL;

import java.util.List;
import java.util.Objects;

/**
 * @author Pei-Jung Lan
 */
public class ViewFlatUsersManagementToolbarDisplayContext
	extends SearchContainerManagementToolbarDisplayContext {

	public ViewFlatUsersManagementToolbarDisplayContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		SearchContainer<User> searchContainer, boolean showDeleteButton,
		boolean showRestoreButton) {

		super(
			liferayPortletRequest.getHttpServletRequest(),
			liferayPortletRequest, liferayPortletResponse, searchContainer);

		_showDeleteButton = showDeleteButton;
		_showRestoreButton = showRestoreButton;

		_navigation = ParamUtil.getString(
			liferayPortletRequest, "navigation", "active");
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() {
		return DropdownItemListBuilder.add(
			() -> _showRestoreButton,
			dropdownItem -> {
				dropdownItem.putData("action", "activateUsers");
				dropdownItem.putData(
					"activateUsersURL",
					PortletURLBuilder.createActionURL(
						liferayPortletResponse
					).setActionName(
						"/users_admin/edit_user"
					).setCMD(
						Constants.RESTORE
					).setNavigation(
						getNavigation()
					).buildString());
				dropdownItem.setIcon("undo");
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "activate"));
				dropdownItem.setQuickAction(true);
			}
		).add(
			() -> _showDeleteButton,
			dropdownItem -> {
				UserSearchTerms userSearchTerms =
					(UserSearchTerms)searchContainer.getSearchTerms();

				String action = "deleteUsers";
				String cmd = Constants.DELETE;

				if (userSearchTerms.isActive()) {
					action = "deactivateUsers";
					cmd = Constants.DEACTIVATE;
				}

				dropdownItem.putData("action", action);
				dropdownItem.putData(
					"editUsersURL",
					PortletURLBuilder.createActionURL(
						liferayPortletResponse
					).setActionName(
						"/users_admin/edit_user"
					).setCMD(
						cmd
					).setNavigation(
						getNavigation()
					).buildString());

				String icon = "times-circle";

				if (userSearchTerms.isActive()) {
					icon = "hidden";
				}

				dropdownItem.setIcon(icon);

				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, cmd));
				dropdownItem.setQuickAction(true);
			}
		).build();
	}

	@Override
	public String getClearResultsURL() {
		return PortletURLBuilder.create(
			getPortletURL()
		).setKeywords(
			StringPool.BLANK
		).setNavigation(
			(String)null
		).setParameter(
			"accountIds", (String)null
		).setParameter(
			"organizationIds", (String)null
		).setParameter(
			"selection", "all"
		).buildString();
	}

	@Override
	public CreationMenu getCreationMenu() {
		return CreationMenuBuilder.addPrimaryDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					liferayPortletResponse.createRenderURL(),
					"mvcRenderCommandName", "/users_admin/edit_user");
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "add-user"));
			}
		).build();
	}

	@Override
	public List<DropdownItem> getFilterDropdownItems() {
		return DropdownItemListBuilder.addGroup(
			dropdownGroupItem -> {
				dropdownGroupItem.setDropdownItems(
					super.getFilterDropdownItems());
				dropdownGroupItem.setLabel(
					getFilterNavigationDropdownItemsLabel());
			}
		).addGroup(
			dropdownGroupItem -> {
				dropdownGroupItem.setDropdownItems(
					_getFilterNavigationDropdownItems());
				dropdownGroupItem.setLabel(
					LanguageUtil.get(httpServletRequest, "filter-by"));
			}
		).build();
	}

	@Override
	public List<LabelItem> getFilterLabelItems() {
		return new LabelItemList() {
			{
				if (Objects.equals(
						_getSelectionNavigation(), "selected-account-users")) {

					long[] accountEntryIds = ParamUtil.getLongValues(
						httpServletRequest, "accountEntryIds");

					for (long accountEntryId : accountEntryIds) {
						AccountEntry accountEntry =
							AccountEntryLocalServiceUtil.fetchAccountEntry(
								accountEntryId);

						add(
							labelItem -> {
								PortletURL removeLabelURL = getPortletURL();

								long[] newAccountEntryIds = ArrayUtil.remove(
									accountEntryIds, accountEntryId);

								if (newAccountEntryIds.length == 0) {
									removeLabelURL.setParameter(
										"accountEntryIds", (String)null);
									removeLabelURL.setParameter(
										"selection", "all");
								}

								removeLabelURL.setParameter(
									"accountEntryIds",
									StringUtil.merge(
										newAccountEntryIds, StringPool.COMMA));

								labelItem.putData(
									"removeLabelURL",
									removeLabelURL.toString());

								labelItem.setDismissible(true);
								labelItem.setLabel(
									LanguageUtil.get(
										httpServletRequest,
										accountEntry.getName()));
							});
					}
				}
				else if (Objects.equals(
							_getSelectionNavigation(),
							"selected-organization-users")) {

					long[] organizationIds = ParamUtil.getLongValues(
						httpServletRequest, "organizationIds");

					for (long organizationId : organizationIds) {
						Organization organization =
							OrganizationLocalServiceUtil.fetchOrganization(
								organizationId);

						add(
							labelItem -> {
								PortletURL removeLabelURL = getPortletURL();

								long[] newOrganizationIds = ArrayUtil.remove(
									organizationIds, organizationId);

								if (newOrganizationIds.length == 0) {
									removeLabelURL.setParameter(
										"organizationIds", (String)null);
									removeLabelURL.setParameter(
										"selection", "all");
								}

								removeLabelURL.setParameter(
									"organizationIds",
									StringUtil.merge(
										newOrganizationIds, StringPool.COMMA));

								labelItem.putData(
									"removeLabelURL",
									removeLabelURL.toString());

								labelItem.setDismissible(true);
								labelItem.setLabel(
									LanguageUtil.get(
										httpServletRequest,
										organization.getName()));
							});
					}
				}

				if (!Objects.equals(_navigation, "active")) {
					add(
						labelItem -> {
							labelItem.putData(
								"removeLabelURL",
								PortletURLBuilder.create(
									getPortletURL()
								).setNavigation(
									(String)null
								).buildString());
							labelItem.setCloseable(true);
							labelItem.setLabel(
								String.format(
									"%s: %s",
									LanguageUtil.get(
										httpServletRequest, "status"),
									LanguageUtil.get(
										httpServletRequest, getNavigation())));
						});
				}
			}
		};
	}

	@Override
	public String getSearchFormName() {
		return "searchFm";
	}

	@Override
	public Boolean isSelectable() {
		return _showDeleteButton || _showRestoreButton;
	}

	@Override
	public Boolean isShowCreationMenu() {
		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return PortalPermissionUtil.contains(
			themeDisplay.getPermissionChecker(), ActionKeys.ADD_USER);
	}

	@Override
	protected String getDisplayStyle() {
		return DisplayStyleUtil.getDisplayStyle(
			liferayPortletRequest, getDefaultDisplayStyle());
	}

	@Override
	protected String[] getDisplayViews() {
		return new String[] {"icon", "descriptive", "list"};
	}

	@Override
	protected String getFilterNavigationDropdownItemsLabel() {
		return LanguageUtil.get(httpServletRequest, "filter-by-status");
	}

	@Override
	protected String getNavigation() {
		return _navigation;
	}

	@Override
	protected String[] getNavigationKeys() {
		return new String[] {"all", "active", "inactive"};
	}

	@Override
	protected String[] getOrderByKeys() {
		String[] orderColumns = {
			"first-name", "last-login-date", "last-name", "screen-name"
		};

		if (searchContainer.isSearch()) {
			orderColumns = ArrayUtil.append(orderColumns, "relevance");
		}

		return orderColumns;
	}

	@Override
	protected PortletURL getPortletURL() {
		return PortletURLBuilder.create(
			super.getPortletURL()
		).setParameter(
			"orderByCol", getOrderByCol()
		).setParameter(
			"orderByType", getOrderByType()
		).buildPortletURL();
	}

	private List<DropdownItem> _getFilterNavigationDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(_getSelectionNavigation(), "all"));
				dropdownItem.setHref(currentURLObj);
				dropdownItem.setLabel(
					LanguageUtil.get(httpServletRequest, "all"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						_getSelectionNavigation(), "selected-account-users"));

				dropdownItem.putData("action", "selectAccountEntries");

				dropdownItem.putData(
					"accountEntriesSelectorURL",
					PortletURLBuilder.createRenderURL(
						liferayPortletResponse
					).setMVCPath(
						"/select_account_entries.jsp"
					).setParameter(
						"selection", "selected-account-users"
					).setWindowState(
						LiferayWindowState.POP_UP
					).buildString());

				dropdownItem.putData(
					"dialogTitle",
					LanguageUtil.get(httpServletRequest, "select-accounts"));
				dropdownItem.putData("redirectURL", currentURLObj.toString());

				dropdownItem.setLabel(
					LanguageUtil.get(
						httpServletRequest, "selected-account-users"));
			}
		).add(
			dropdownItem -> {
				dropdownItem.setActive(
					Objects.equals(
						_getSelectionNavigation(),
						"selected-organization-users"));

				dropdownItem.putData("action", "selectOrganizations");

				dropdownItem.putData(
					"dialogTitle",
					LanguageUtil.get(
						httpServletRequest, "select-organizations"));

				dropdownItem.putData(
					"organizationsSelectorURL",
					PortletURLBuilder.createRenderURL(
						liferayPortletResponse
					).setMVCPath(
						"/select_organizations.jsp"
					).setParameter(
						"selection", "selected-organization-users"
					).setWindowState(
						LiferayWindowState.POP_UP
					).buildString());

				dropdownItem.putData("redirectURL", currentURLObj.toString());
				dropdownItem.setLabel(
					LanguageUtil.get(
						httpServletRequest, "selected-organization-users"));
			}
		).build();
	}

	private String _getSelectionNavigation() {
		return ParamUtil.getString(liferayPortletRequest, "selection", "all");
	}

	private final String _navigation;
	private final boolean _showDeleteButton;
	private final boolean _showRestoreButton;

}