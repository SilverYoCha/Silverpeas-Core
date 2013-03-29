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
package org.silverpeas.multiedition.control;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import org.silverpeas.multiedition.MultiEditionContext;
import org.silverpeas.multiedition.service.MultiEditionServiceFactory;

/**
 * User: Yohann Chastagnier
 * Date: 04/03/13
 */
public class MultiEditionSessionController extends AbstractComponentSessionController {

  /**
   * Default constructor.
   * @param controller
   * @param context
   */
  public MultiEditionSessionController(final MainSessionController controller,
      final ComponentContext context) {
    super(controller, context, "org.silverpeas.subscription.multilang.subscriptionBundle");
  }

  /**
   * Gets the context.
   * @return
   */
  public MultiEditionContext getContext() {
    return getMultiEditionContext();
  }

  /*
   * Initialize UserPanel with the list of Silverpeas subscribers
   */
  public String toCaller() {

    // Returning the destination
    return getContext().getCallerUrl();
  }

  public void initialize(String documentId, String componentInstanceId, String callerUrl) {
    getContext().initialize(documentId, componentInstanceId, callerUrl);
    synchronize();
  }

  public void synchronize() {
    MultiEditionServiceFactory.getMultiEditionService()
        .synchronize(getContext().getMultiEditionDocumentContext());
  }

  public void finish() {
    MultiEditionServiceFactory.getMultiEditionService()
        .finish(getContext().getMultiEditionDocumentContext());
  }
}
