/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.external;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

/**
 * User: Yohann Chastagnier
 * Date: 02/04/13
 */
public class ExternalServiceFactory {

  /**
   * Build and returns a Drive service object authorized with the service accounts.
   * @return Drive service object that is ready to make requests.
   */
  public static Drive getDriveService(ExternalServiceType externalServiceType)
      throws GeneralSecurityException, IOException, URISyntaxException {
    HttpTransport httpTransport = new NetHttpTransport();
    JacksonFactory jsonFactory = new JacksonFactory();
    GoogleCredential credential =
        new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory)
            .setServiceAccountId(getServiceAccountEmail())
            .setServiceAccountScopes(DriveScopes.DRIVE).setServiceAccountPrivateKeyFromP12File(
            getServiceAccountPKCS12FilePath(externalServiceType)).build();
    return new Drive.Builder(httpTransport, jsonFactory, null).setHttpRequestInitializer(credential)
        .build();
  }

  /**
   * Gets the service account email.
   * @return
   */
  private synchronized static String getServiceAccountEmail() {
    return "917025859610-me0743bu1tl3miod0uddjsjkkdouk8cm@developer.gserviceaccount.com";
  }

  /**
   * Gets the service account PKCS12 file path.
   * @return
   */
  private synchronized static File getServiceAccountPKCS12FilePath(
      ExternalServiceType externalServiceType) {
    // TODO : that is so bad but that is for now
    return FileUtils
        .listFiles(externalServiceType.getRootPath(), TrueFileFilter.TRUE, FalseFileFilter.FALSE)
        .iterator().next();
  }
}
