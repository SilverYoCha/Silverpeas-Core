/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.admin.scim;

import edu.psu.swe.scim.spec.resources.Photo;
import org.silverpeas.core.admin.domain.DomainType;
import org.silverpeas.core.admin.domain.driver.AbstractAvatarSynchroProcess;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Integer.parseInt;
import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static org.silverpeas.core.util.HttpUtil.httpClient;
import static org.silverpeas.core.util.HttpUtil.toUrl;

/**
 * @author silveryocha
 */
public class ScimAvatarSynchroProcess extends AbstractAvatarSynchroProcess {

  private final Photo photo;

  public ScimAvatarSynchroProcess(final ScimRequestContext context,
      final String specificId, final Photo photo, final UserDetail spUser) {
    super(() -> parseInt(context.getDomainId()), DomainType.SCIM, () -> specificId, spUser);
    this.photo = photo;
  }

  @Override
  protected void loadPhotoIntoPath(final Path photoPath) throws IOException {
    try {
      final HttpResponse<InputStream> response =  httpClient().send(toUrl(photo.getValue())
          .header("Accept", "image/*")
          .build(), ofInputStream());
      try (final InputStream body = response.body()) {
        Files.copy(body, photoPath);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
