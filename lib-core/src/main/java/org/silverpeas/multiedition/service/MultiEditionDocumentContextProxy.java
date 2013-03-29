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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * User: Yohann Chastagnier
 * Date: 02/04/13
 */
public class MultiEditionDocumentContextProxy implements Serializable {

  private static final long serialVersionUID = -2407328773407048022L;
  private String data;

  /**
   * Default constructor.
   * @param context
   */
  public MultiEditionDocumentContextProxy(MultiEditionDocumentContext context) {
    StringBuilder sb = new StringBuilder();
    sb.append(context.isSynchronizing());
    sb.append("\n");
    sb.append(context.getInternalId());
    sb.append("\n");
    sb.append(context.getExternalId());
    data = sb.toString();
  }

  /**
   * This method is read for deserialization.
   * @return
   * @throws ObjectStreamException
   */
  private Object readResolve() throws ObjectStreamException {
    MultiEditionDocumentContext context = new MultiEditionDocumentContext();
    StringTokenizer st = new StringTokenizer(this.data, "\n");
    context.setSynchronizing(Boolean.parseBoolean(st.nextToken()));
    context.setInternalId(st.nextToken());
    context.setExternalId(st.nextToken());
    return context;
  }
}
