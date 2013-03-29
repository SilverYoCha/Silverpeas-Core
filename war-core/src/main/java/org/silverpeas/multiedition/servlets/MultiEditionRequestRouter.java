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
package org.silverpeas.multiedition.servlets;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import org.silverpeas.multiedition.constant.MultiEditionFunction;
import org.silverpeas.multiedition.control.MultiEditionSessionController;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Yohann Chastagnier
 * Date: 04/03/13
 */
public class MultiEditionRequestRouter
    extends ComponentRequestRouter<MultiEditionSessionController> {

  @Override
  public String getSessionControlBeanName() {
    return "subscriptionManagement";
  }

  @Override
  public MultiEditionSessionController createComponentSessionController(
      final MainSessionController mainSessionCtrl, final ComponentContext componentContext) {
    return new MultiEditionSessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getDestination(final String function,
      final MultiEditionSessionController multiEditionSC, final HttpServletRequest request) {

    // Initializing destination
    String destination = "";

    // Setting the context
    String documentId = request.getParameter("documentId");
    String componentInstanceId = request.getParameter("instanceId");
    String callerUrl = request.getParameter("callerUrl");
    if (StringUtil.isDefined(documentId) && StringUtil.isDefined(componentInstanceId) &&
        StringUtil.isDefined(callerUrl)) {
      multiEditionSC.initialize(documentId, componentInstanceId, callerUrl);
    }
    request.setAttribute("context", multiEditionSC.getContext());

    switch (MultiEditionFunction.from(function)) {
      case ToCaller:
        destination = multiEditionSC.toCaller();
        break;
      case Main:
      default:
        destination = "/multiedition/jsp/multieditionpanel.jsp";
        break;
    }

    // Returning the destination
    return destination;
  }
}
