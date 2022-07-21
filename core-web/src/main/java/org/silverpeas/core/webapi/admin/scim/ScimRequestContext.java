/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.admin.scim;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.util.MemoizedSupplier;
import org.silverpeas.core.web.rs.SilverpeasRequestContext;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * The SCIM request context which handles the domain identifier data in addition to the data
 * handled by  {@link SilverpeasRequestContext}.
 * @author silveryocha
 */
@RequestScoped
class ScimRequestContext extends SilverpeasRequestContext {

  private Domain domain;
  private final MemoizedSupplier<SettingBundle> domainSettings = new MemoizedSupplier<>(
      () -> domain.getSettings());

  void init(final HttpServletRequest request, final HttpServletResponse response,
      final String domainId) {
    super.init(request, response);
    try {
      this.domain = Administration.get().getDomain(domainId);
    } catch (AdminException e) {
      throw new WebApplicationException(e, NOT_FOUND);
    }
  }

  String getDomainId() {
    return domain != null ? domain.getId() : null;
  }

  SettingBundle getDomainSettings() {
    return domainSettings.get();
  }
}
