<%--
  ~ Copyright (C) 2000 - 2017 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception. You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<%@ attribute name="users" required="false" type="java.util.Collection"
              description="The list of users to display" %>

<%@ attribute name="userIds" required="false" type="java.util.Collection"
              description="The list of userIds to display" %>

<%@ attribute name="groups" required="false" type="java.util.Collection"
              description="The list of groups to display" %>

<%@ attribute name="groupIds" required="false" type="java.util.Collection"
              description="The list of groupIds to display" %>

<%@ attribute name="roleFilter" required="false" type="java.util.Collection"
              description="The list of roles to filter on" %>

<%@ attribute name="displayUserZoom" required="false" type="java.lang.Boolean"
              description="Activate the user zoom plugin on each user displayed" %>

<%@ attribute name="displayAvatar" required="false" type="java.lang.Boolean"
              description="Display avatar of each user or just user icon if false" %>

<%@ attribute name="hideDeactivatedState" required="false" type="java.lang.Boolean"
              description="Indicates if deactivated use account must not be taken into account (default as true)" %>

<%@ attribute name="domainIdFilter" required="false" type="java.lang.String"
              description="The domain id to filter on" %>

<%@ attribute name="componentIdFilter" required="false" type="java.lang.String"
              description="The component instance id to filter on" %>

<%@ attribute name="id" required="false" type="java.lang.String"
              description="CSS id" %>

<%@ attribute name="multiple" required="false" type="java.lang.Boolean"
              description="Is multiple selection authorized?" %>

<%@ attribute name="selectionType" required="false" type="java.lang.String"
              description="USER or GROUP or USER_GROUP (USER by default or if attribute cannot be parsed)" %>

<%@ attribute name="userInputName" required="false" type="java.lang.String"
              description="Sets the name of the user input, otherwise default one is created" %>

<%@ attribute name="groupInputName" required="false" type="java.lang.String"
              description="Sets the name of the group input, otherwise default one is created" %>

<c:if test="${hideDeactivatedState == null}">
  <c:set var="hideDeactivatedState" value="${true}"/>
</c:if>

<c:if test="${displayUserZoom == null}">
  <c:set var="displayUserZoom" value="${true}"/>
</c:if>

<c:if test="${displayAvatar == null}">
  <c:set var="displayAvatar" value="${true}"/>
</c:if>

<c:if test="${multiple == null}">
  <c:set var="multiple" value="${false}"/>
</c:if>

<c:if test="${selectionType == null}">
  <c:set var="selectionType" value="USER"/>
</c:if>

<c:set var="currentUserId" value="${sessionScope['SilverSessionController'].userId}"/>
<view:includePlugin name="listOfUsersAndGroups"/>

<div class="select-user-group-container" id="select-user-group-${id}">
</div>

<view:progressMessage/>

<script type="text/javascript">
  whenSilverpeasReady(function() {
    var userIds = [<c:forEach items="${users}" var="user" varStatus="status"><c:if test="${not status.first}">, </c:if>${user.id}</c:forEach>];
    if (userIds.length === 0) {
      userIds = [<c:forEach items="${userIds}" var="userId" varStatus="status"><c:if test="${not status.first}">, </c:if>${userId}</c:forEach>];
    }
    var groupIds = [<c:forEach items="${groups}" var="group" varStatus="status"><c:if test="${not status.first}">, </c:if>${group.id}</c:forEach>];
    if (groupIds.length === 0) {
      groupIds = [<c:forEach items="${groupIds}" var="groupId" varStatus="status"><c:if test="${not status.first}">, </c:if>${groupId}</c:forEach>];
    }
    var roleFilter = [<c:forEach items="${roleFilter}" var="role" varStatus="status"><c:if test="${not status.first}">, </c:if>'${role}'</c:forEach>];
    new UserGroupSelect({
      hideDeactivatedState : ${hideDeactivatedState},
      domainIdFilter : '${domainIdFilter}',
      componentIdFilter : '${componentIdFilter}',
      roleFilter : roleFilter,
      userInputName : '${userInputName}',
      groupInputName : '${groupInputName}',
      currentUserId : ${currentUserId},
      rootContainerId : "select-user-group-${id}",
      initialUserIds : userIds,
      initialGroupIds : groupIds,
      displayUserZoom : ${displayUserZoom},
      displayAvatar : ${displayAvatar},
      multiple : ${multiple},
      selectionType : '${selectionType}'
    });
  });
</script>