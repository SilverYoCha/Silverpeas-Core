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

import org.silverpeas.attachment.model.SimpleDocument;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * User: Yohann Chastagnier
 * Date: 02/04/13
 */
public class MultiEditionDocumentContext implements Serializable {

  private boolean synchronizing = false;
  private SimpleDocument document;
  private String internalId;
  private String externalId;

  public boolean isSynchronizing() {
    return synchronizing;
  }

  public void setSynchronizing(final boolean synchronizing) {
    this.synchronizing = synchronizing;
  }

  public SimpleDocument getDocument() {
    return document;
  }

  protected void setDocument(final SimpleDocument document) {
    this.document = document;
  }

  public String getInternalId() {
    return internalId;
  }

  public void setInternalId(final String internalId) {
    this.internalId = internalId;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(final String externalId) {
    this.externalId = externalId;
  }

  /**
   * Serialization.
   * @return
   * @throws ObjectStreamException
   */
  private Object writeReplace() throws ObjectStreamException {
    return new MultiEditionDocumentContextProxy(this);
  }
}
