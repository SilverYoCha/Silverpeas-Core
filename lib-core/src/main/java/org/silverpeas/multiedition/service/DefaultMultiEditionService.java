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
package org.silverpeas.multiedition.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.silverpeas.annotation.Service;
import com.silverpeas.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.tika.io.IOUtils;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.external.ExternalServiceFactory;
import org.silverpeas.external.ExternalServiceType;
import org.silverpeas.multiedition.exception.ExternalServiceAuthenticationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.GeneralSecurityException;

/**
 * Implementation using Google Drive.
 * User: Yohann Chastagnier
 * Date: 02/04/13
 */
@Service
public class DefaultMultiEditionService implements MultiEditionService {

  // Silverpeas data path for external services
  private static File externalSilverpeasDataPath =
      FileUtils.getFile(ExternalServiceType.multiedition.getRootPath(), "data");

  static {
    if (!externalSilverpeasDataPath.exists()) {
      externalSilverpeasDataPath.mkdirs();
    }
  }

  @Override
  public void synchronize(final MultiEditionDocumentContext context) {
    try {
      Drive service = getDriveService();
      final com.google.api.services.drive.model.File file;
      if (StringUtil.isNotDefined(context.getExternalId()) && !context.isSynchronizing()) {
//        if (lock(context)) {
          try {
            // Insert a body
            com.google.api.services.drive.model.File body =
                new com.google.api.services.drive.model.File();
            body.setTitle(context.getInternalId());
            body.setMimeType(context.getDocument().getContentType());

            // Define transport
            java.io.File fileContent = new java.io.File(context.getDocument().getAttachmentPath());
            FileContent mediaContent = new FileContent(body.getMimeType(), fileContent);

            // Insertion
            file = service.files().insert(body, mediaContent).execute();
            // External file ID
            context.setExternalId(file.getId());
            // Save the context
            saveContext(context);
          } finally {
            unlock(context);
          }
//        }
      } else {
        file = service.files().get(context.getExternalId()).execute();
      }
      System.out.println(file.toPrettyString());
      System.out.println(file.getEmbedLink());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private synchronized boolean lock(final MultiEditionDocumentContext context) {
    if (!context.isSynchronizing()) {
      context.setSynchronizing(true);
      saveContext(context);
      return true;
    }
    return false;
  }

  private synchronized void unlock(final MultiEditionDocumentContext context) {
    context.setSynchronizing(false);
    saveContext(context);
  }

  @Override
  public void finish(final MultiEditionDocumentContext context) {
    FileUtils.deleteQuietly(
        FileUtils.getFile(externalSilverpeasDataPath, (context.getInternalId() + ".ser")));
  }

  /**
   * Store the context.
   * @param context
   * @throws Exception
   */
  private synchronized void saveContext(MultiEditionDocumentContext context) {
    FileOutputStream fos = null;
    try {
      fos = FileUtils.openOutputStream(
          FileUtils.getFile(externalSilverpeasDataPath, (context.getInternalId() + ".ser")));
      SerializationUtils.serialize(context, fos);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(fos);
    }
  }

  /**
   * Load the context.
   * @param document
   * @throws Exception
   */
  @Override
  public synchronized MultiEditionDocumentContext getContext(SimpleDocument document) {
    FileInputStream fis = null;
    try {
      String internalId = "multiedition-" + document.getId();
      File contextFile = FileUtils.getFile(externalSilverpeasDataPath, (internalId + ".ser"));
      final MultiEditionDocumentContext context;
      if (!contextFile.exists()) {
        context = new MultiEditionDocumentContext();
        context.setInternalId(internalId);
      } else {
        fis = FileUtils.openInputStream(contextFile);
        context = (MultiEditionDocumentContext) SerializationUtils.deserialize(fis);
      }
      context.setDocument(document);
      return context;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(fis);
    }
  }

  /**
   * Gets the drive service.
   * @return
   */
  private Drive getDriveService() {
    try {
      return ExternalServiceFactory.getDriveService(ExternalServiceType.multiedition);
    } catch (GeneralSecurityException e) {
      throw new ExternalServiceAuthenticationException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
