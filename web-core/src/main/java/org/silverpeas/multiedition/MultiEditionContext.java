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
package org.silverpeas.multiedition;

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.multiedition.service.MultiEditionDocumentContext;
import org.silverpeas.multiedition.service.MultiEditionServiceFactory;

/**
 * User: Yohann Chastagnier
 * Date: 04/03/13
 */
public class MultiEditionContext {

  private UserDetail user;
  private UserPreferences userPreferences;
  private InternalContext internalContext;

  /**
   * Default constructor
   */
  public MultiEditionContext(final UserDetail user, final UserPreferences userPreferences) {
    this.user = user;
    this.userPreferences = userPreferences;
  }

  /**
   * Initializing all context data excepted ones of the user.
   * @param documentId
   * @param componentInstanceId
   * @param callerUrl
   */
  public void initialize(String documentId, String componentInstanceId, String callerUrl) {
    initialize(AttachmentServiceFactory.getAttachmentService()
        .searchDocumentById(new SimpleDocumentPK(documentId, componentInstanceId), null),
        callerUrl);
  }

  /**
   * Initializing all context data excepted ones of the user.
   * @param documentId
   * @param componentInstanceId
   * @param language
   * @param callerUrl
   */
  public void initialize(String documentId, String componentInstanceId, String language,
      String callerUrl) {
    String lang = (StringUtil.isDefined(language) ? I18NHelper.checkLanguage(language) :
        userPreferences.getLanguage());
    initialize(AttachmentServiceFactory.getAttachmentService()
        .searchDocumentById(new SimpleDocumentPK(documentId, componentInstanceId), lang),
        callerUrl);
  }

  /**
   * Initializing all context data excepted ones of the user.
   * @param document
   * @param callerUrl
   */
  public void initialize(SimpleDocument document, String callerUrl) {
    internalContext = new InternalContext();
    internalContext.multiEditionDocumentContext =
        MultiEditionServiceFactory.getMultiEditionService().getContext(document);
    internalContext.callerUrl = callerUrl;
  }

  /**
   * Gets the current user.
   * @return
   */
  public UserDetail getUser() {
    return user;
  }

  /**
   * Gets the multi edition document context
   * @return
   */
  public MultiEditionDocumentContext getMultiEditionDocumentContext() {
    return internalContext.multiEditionDocumentContext;
  }

  /**
   * Gets the url to go back to caller screen
   * @return
   */
  public String getCallerUrl() {
    return internalContext.callerUrl;
  }

  /**
   * Gets the destination URL.
   * @return
   */
  public String getDestinationUrl() {
    return internalContext.destinationUrl;
  }

  /**
   * Internal context
   */
  private class InternalContext {
    public String destinationUrl = "/RMultiEdition/jsp/Main";
    public MultiEditionDocumentContext multiEditionDocumentContext = null;
    public String callerUrl = null;
  }
}
