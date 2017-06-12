/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

(function() {

  var VANILLA_PLUGIN_USE = false;

  var SELECT_NB_ITEM_PER_TYPE = 20;
  var SELECTION_TYPE = {
    USER : 0,
    GROUP : 1,
    USER_GROUP : 2,
    decode : function(value) {
      var decoded;
      if (typeof value === 'string') {
        decoded = SELECTION_TYPE[value.toUpperCase()];
      }
      return !decoded ? SELECTION_TYPE.USER : decoded;
    }
  };

  var ICON_USER_GROUP_PANEL = webContext + "/util/icons/create-action/add-existing-group.png";
  var ICON_USER_PANEL = webContext + "/util/icons/user.png";
  var ICON_GROUP_PANEL = webContext + "/util/icons/groups.png";
  var ICON_GROUP_SYNC = webContext + "/jobDomainPeas/jsp/icons/scheduledGroup.gif";
  var ICON_GROUP = webContext + "/util/icons/groupe.gif";
  var ICON_USER = webContext + "/util/icons/user.gif";
  var ICON_USER_BLOCKED = webContext + "/util/icons/user-blocked.png";
  var ICON_USER_EXPIRED = webContext + "/util/icons/user-expired.png";

  var LABEL_USERS = UserGroupListBundle.get("GML.user_s");
  var LABEL_DELETE = UserGroupListBundle.get("GML.delete");
  var LABEL_DELETE_ALL = UserGroupListBundle.get("GML.deleteAll");
  var LABEL_CONFIRM_DELETE_ALL = UserGroupListBundle.get("GML.confirmation.deleteAll");
  var LABEL_REMOVE = UserGroupListBundle.get("GML.action.remove");
  var LABEL_REMOVE_ALL = UserGroupListBundle.get("GML.action.removeAll");
  var LABEL_KEEP = UserGroupListBundle.get("GML.action.keep");
  var LABEL_UPDATE = UserGroupListBundle.get("GML.modify");
  var LABEL_SELECT = UserGroupListBundle.get("GML.action.select");
  var LABEL_LIST_CHANGED = UserGroupListBundle.get("GML.list.changed.message");

  var __globalIdCounter = 0;

  var UserGroupRequester = function(options) {
    this.options = extendsObject({
      hideDeactivatedState : false,
      domainIdFilter : '',
      componentIdFilter : ''
    }, options);

    var __applyCommonParameters = function(params) {
      if (typeof params === 'undefined') {
        params = {};
      }
      if (typeof params === 'object') {
        if (!params.id && !params.ids) {
          if (this.options.hideDeactivatedState) {
            params.userStatesToExclude = ['DEACTIVATED'];
          }
          if (this.options.domainIdFilter) {
            params.domain = this.options.domainIdFilter
          }
          if (this.options.componentIdFilter) {
            params.component = this.options.componentIdFilter
          }
          if (this.options.roleFilter) {
            params.roles = this.options.roleFilter.join(',')
          }
        }
      }
      if (params.limit) {
        params.page = {number : 1, size : params.limit};
      }
      return params;
    }.bind(this);

    this.getUsers = function(params) {
      var finalParams = __applyCommonParameters(params);
      return User.get(finalParams).then(function(users) {
        if (finalParams.limit && finalParams.limit < users.length) {
          users.splice(finalParams.limit, 1);
          users.hasMore = true;
        }
        return users;
      });
    };

    this.getUserGroups = function(params) {
      var finalParams = __applyCommonParameters(params);
      return UserGroup.get(finalParams).then(function(groups) {
        if (finalParams.limit && finalParams.limit < groups.length) {
          groups.splice(finalParams.limit, 1);
          groups.hasMore = true;
        }
        return groups;
      });
    }
  };

  var UserGroupSelectionPanel = function(userGroupSelectInstance) {
    var _selectionContainer = document.createElement('div');
    _selectionContainer.classList.add('select-user-group-selection-container');
    var _containerOfLists = document.createElement('div');
    _containerOfLists.classList.add('selection-lists');
    var _userList = document.createElement('div');
    var _listSeparator = document.createElement('div');
    _listSeparator.classList.add('list-separator');
    var _groupList = document.createElement('div');
    var _userContainer = document.createElement('ul');
    _userContainer.classList.add("access-list", "user");
    var _groupContainer = document.createElement('ul');
    _groupContainer.classList.add("access-list", "group");
    _groupList.appendChild(_groupContainer);
    _userList.appendChild(_userContainer);
    _containerOfLists.appendChild(_groupList);
    _containerOfLists.appendChild(_listSeparator);
    _containerOfLists.appendChild(_userList);
    _selectionContainer.appendChild(_containerOfLists);
    var __qTip = TipManager.dropDown(userGroupSelectInstance.context.searchInput, _selectionContainer);

    Mousetrap(_selectionContainer).bind('esc', function(e) {
      userGroupSelectInstance.context.searchInput.focus();
    }.bind(this));

    this.show = function() {
      __qTip.show();
    };
    this.hide = function() {
      __qTip.hide();
    };
    this.reposition = function() {
      __qTip.reposition();
    };
    this.refresh = function() {
      __refreshContainer(_userContainer, userGroupSelectInstance.context.userItems);
      __refreshContainer(_groupContainer, userGroupSelectInstance.context.groupItems);
      var isUsers = userGroupSelectInstance.context.userItems.length > 0;
      var isGroups = userGroupSelectInstance.context.groupItems.length > 0;
      var isResult = isUsers || isGroups;
      if (isResult) {
        if (isUsers && isGroups) {
          _listSeparator.style.display = '';
        } else {
          _listSeparator.style.display = 'none';
        }
        this.reposition();
        this.show();
      }
    };
  };

  var UserGroupSelectize = function(userGroupSelectInstance) {
    var _sequential = sp.promise.resolveDirectlyWith();
    var _self = this;
    var __selectize = jQuery(userGroupSelectInstance.context.searchInput).selectize({
      valueField: 'id',
      placeholder: "          ",
      options: [],
      create: false,
      highlight: false,
      hideSelected : true,
      loadThrottle : 300,
      maxItems : userGroupSelectInstance.options.multiple ? null : 1,
      maxOptions : (SELECT_NB_ITEM_PER_TYPE * 2) - 1,
      render: {
        option: function(data, escape) {
          var option = data.getElement();
          option.classList.add('option');
          return option;
        },
        item: function(data, escape) {
          var item = data.getElement();
          item.classList.add('item');
          return item;
        }
      },
      score: function(search) {
        return function(item) {
          return item.score;
        };
      },
      load: function(query, callback) {
        _sequential.then(function() {
          __selectize.loadedSearches = {};
          userGroupSelectInstance.context.searchInput.processQuery(query, function(ajaxPerformed) {
            for(var itemId in __selectize.options) {
              __selectize.options[itemId].score = 0;
              if (__selectize.items.indexOf(itemId) < 0) {
                __selectize.removeOption(itemId);
              }
            }
            __selectize.refreshOptions(true);
            if (ajaxPerformed) {
              var all = [];
              userGroupSelectInstance.context.groupItems.forEach(function(item) {
                item.id = 'group-' + item.getId();
                item.name = item.getFullName();
                item.score = 1;
                all.push(item);
              });
              userGroupSelectInstance.context.userItems.forEach(function(item) {
                item.id = 'user-' + item.getId();
                item.name = item.getFullName();
                item.score = 1;
                all.push(item);
              });
              callback(all);
            }
          });
        });
      }
    })[0].selectize;
    this.show = function() {
      __selectize.open();
    };
    this.hide = function() {
      __selectize.close();
    };
    this.refresh = function() {
    };
  };

  window.UserGroupSelect = function(options) {
    applyReadyBehaviorOn(this);
    var __idCounter = __globalIdCounter;

    this.options = extendsObject({
      hideDeactivatedState : true,
      domainIdFilter : '',
      componentIdFilter : '',
      roleFilter : [],
      readOnly : false,
      userPanelId : '',
      initUserPanelUserIdParamName : '',
      initUserPanelGroupIdParamName : '',
      userInputName : '',
      groupInputName : '',
      currentUserId : '',
      rootContainerId : "select-user-group-container",
      initialUserIds : [],
      initialGroupIds : [],
      displayUserZoom : true,
      displayAvatar : true,
      layout : 'inline',
      displaySelection : true,
      multiple : false,
      selectionType : 'USER'
    }, options);
    this.options.selectionType = SELECTION_TYPE.decode(this.options.selectionType);

    if (StringUtil.isNotDefined(this.options.userPanelId)) {
      __globalIdCounter = __globalIdCounter + 1;
      this.options.userPanelId = "user-group-select-" + __idCounter;
    }

    var initialUserIds = __convertToString(this.options.initialUserIds);
    var initialGroupIds = __convertToString(this.options.initialGroupIds);

    this.context = {
      readOnly : this.options.readOnly,
      userPanelFormName : this.options.readOnly ? this.options.userPanelId : '',
      currentUserIds : [].concat(initialUserIds),
      currentGroupIds : [].concat(initialGroupIds),
      userItems : [],
      groupItems : []
    };


    this.refreshCommons = function() {
      this.context.userSelectionInput.value = this.context.currentUserIds;
      this.context.groupSelectionInput.value = this.context.currentGroupIds;
    };

    var __requester = new UserGroupRequester(options);

    var _doSearchWith = function(search, callback) {
      var userDeferred = sp.promise.deferred();
      var groupDeferred = sp.promise.deferred();
      var searchDone = [userDeferred.promise, groupDeferred.promise];
      // Query
      var query = encodeURIComponent("%" + search.split('').join('%') + "%");
      // Users
      __requester.getUsers({name : query, limit : SELECT_NB_ITEM_PER_TYPE}).then(function(users) {
        this.context.userItems = [];
        users.forEach(function(user) {
          this.context.userItems.push(new SelectUserItem(user, this));
        }.bind(this));
        userDeferred.resolve();
      }.bind(this));
      // Groups
      __requester.getUserGroups({name : query, limit : SELECT_NB_ITEM_PER_TYPE}).then(
          function(groups) {
            this.context.groupItems = [];
            groups.forEach(function(group) {
              this.context.groupItems.push(new SelectUserGroupItem(group, this));
            }.bind(this));
            groupDeferred.resolve();
          }.bind(this));
      return sp.promise.whenAllResolved(searchDone).then(function() {
        this.context.dropPanel.refresh();
        callback(true);
      }.bind(this));
    }.bind(this);

    this.ready(function() {
      Mousetrap(this.context.rootContainer).bind('esc', function(e) {
        this.context.dropPanel.hide();
        this.context.searchInput.focus();
      }.bind(this));

      var __queryRunning = false;
      var __lastUnperformedQuery = "";
      var __performSearch = function(value, callback) {
        __queryRunning = true;
        __lastUnperformedQuery = "";
        var __doLastUnperformedQuery = function() {
          __queryRunning = false;
          if (__lastUnperformedQuery) {
            __performSearch(__lastUnperformedQuery, callback);
          }
        };
        _doSearchWith(value, callback).then(__doLastUnperformedQuery, __doLastUnperformedQuery);
      };
      this.context.searchInput.processQuery = function(value, callback) {
        var __value = (value || "").toLowerCase();
        __value = __value.replace(/[^a-z0-9éèäâïîüûùçàôö]/g,'');
        if (__value && __value.length > 2) {
          if (!__queryRunning) {
            __performSearch(__value, callback);
          } else {
            __lastUnperformedQuery = __value;
          }
        } else {
          callback(false);
          this.context.dropPanel.hide();
        }
      }.bind(this);
      this.context.searchInput.addEventListener('keyup', function() {
        this.context.searchInput.processQuery(this.context.searchInput.value);
      }.bind(this));
      this.context.searchInput.addEventListener('focus', function() {
        var __value = (this.context.searchInput.value || "").toLowerCase();
        if (__value.length > 2) {
          this.context.dropPanel.show();
        }
      }.bind(this));
    }.bind(this));

    whenSilverpeasReady(function() {
      this.context.rootContainer = document.querySelector("#" + this.options.rootContainerId);

      var __searchContainer = document.createElement('div');
      __searchContainer.classList.add('search-input-container');
      var __PanelConstructor;
      if (VANILLA_PLUGIN_USE) {
        this.context.searchInput = document.createElement('input');
        this.context.searchInput.type = 'text';
        this.context.searchInput.name = 'search-' + __idCounter;
        this.context.searchInput.autocomplete = 'off';
        __PanelConstructor = UserGroupSelectionPanel;
      } else {
        this.context.searchInput = document.createElement('select');
        __searchContainer.appendChild(this.context.searchInput);
        __PanelConstructor = UserGroupSelectize;
      }
      this.context.searchInput.id = 'search-' + __idCounter;
      this.context.searchInput.classList.add('search-input','search');
      this.context.rootContainer.appendChild(__searchContainer);
      __searchContainer.appendChild(this.context.searchInput);
      this.context.dropPanel = new __PanelConstructor(this);

      var __userPanelSelect = document.createElement('a');
      __userPanelSelect.href = '#';
      __userPanelSelect.classList.add('search');
      var _userPanelSelectIcon = document.createElement('img');
      switch (this.options.selectionType) {
        case SELECTION_TYPE.USER_GROUP :
          _userPanelSelectIcon.src = ICON_USER_GROUP_PANEL;
          break;
        case SELECTION_TYPE.GROUP :
          _userPanelSelectIcon.src = ICON_GROUP_PANEL;
          break;
        default:
          _userPanelSelectIcon.src = ICON_USER_PANEL;
          break;
      }
      __userPanelSelect.appendChild(_userPanelSelectIcon);
      __searchContainer.appendChild(__userPanelSelect);
      __userPanelSelect.addEventListener('click', function() {
        __openUserPanel(this);
      }.bind(this));

      var __selectionList = document.createElement("div");
      __selectionList.setAttribute("id", 'list-user-group-' + this.options.userPanelId);
      this.context.rootContainer.appendChild(__selectionList);

      if (this.options.layout === 'inline') {
        __searchContainer.classList.add('inline-layout');
        __selectionList.classList.add('inline-layout');
      }

      __createHiddenInputs(this, this.context.rootContainer);

      if (!this.context.readOnly) {
        jQuery(this.context.userSelectionInput).on('change', function() {
          // For now, the user panel sets USER and GROUP identifiers and then trigger a change on
          // USER and an other one on GROUP.
          // So, only USER change event is listened...
          var userPanelValue = this.context.userSelectionInput.value;
          var groupPanelValue = this.context.groupSelectionInput.value;
          this.context.currentUserIds = userPanelValue ? userPanelValue.split(',') : [];
          this.context.currentGroupIds = groupPanelValue ? groupPanelValue.split(',') : [];
        }.bind(this));
      }

      if(VANILLA_PLUGIN_USE && this.options.displaySelection) {
        new ListOfUsersAndGroups({
          hideDeactivatedState : this.options.hideDeactivatedState,
          domainIdFilter : this.options.domainIdFilter,
          componentIdFilter : this.options.componentIdFilter,
          roleFilter : this.options.roleFilter,
          readOnly : this.context.readOnly,
          userPanelId : this.options.userPanelId,
          initUserPanelUserIdParamName : this.options.initUserPanelUserIdParamName,
          initUserPanelGroupIdParamName : this.options.initUserPanelGroupIdParamName,
          userInputName : this.options.userInputName,
          groupInputName : this.options.groupInputName,
          currentUserId : this.options.currentUserId,
          rootContainerId : __selectionList.id,
          initialUserIds : this.options.initialUserIds,
          initialGroupIds : this.options.initialGroupIds,
          userPanelInitUrl : false,
          jsSaveCallback : false,
          formSaveSelector : false,
          displayUserZoom : this.options.displayUserZoom,
          displayAvatar : this.options.displayAvatar,
          groupSelectionInput : this.context.groupSelectionInput,
          userSelectionInput : this.context.userSelectionInput
        }).ready(function() {
          this.notifyReady();
        }.bind(this));
      } else {
        this.notifyReady();
      }
    }.bind(this));
  };

  window.ListOfUsersAndGroups = function(options) {
    applyReadyBehaviorOn(this);
    var __idCounter = __globalIdCounter;

    this.options = extendsObject({
      hideDeactivatedState : true,
      domainIdFilter : '',
      userPanelId : '',
      initUserPanelUserIdParamName : '',
      initUserPanelGroupIdParamName : '',
      userInputName : '',
      groupInputName : '',
      currentUserId : '',
      rootContainerId : "user-group-list-root-container",
      initialUserIds : [],
      initialGroupIds : [],
      userPanelInitUrl : false,
      jsSaveCallback : false,
      formSaveSelector : '',
      displayUserZoom : true,
      displayAvatar : true,
      groupSelectionInput : false,
      userSelectionInput : false,
      readOnly : undefined
    }, options);

    if (StringUtil.isNotDefined(this.options.userPanelId)) {
      __globalIdCounter = __globalIdCounter + 1;
      this.options.userPanelId = "user-group-list-" + __idCounter;
    }

    var initialUserIds = __convertToString(this.options.initialUserIds);
    var initialGroupIds = __convertToString(this.options.initialGroupIds);

    this.context = {
      type : 'list',
      readOnly : typeof this.options.readOnly === 'boolean' && this.options.readOnly,
      userPanelSaving : false,
      currentUserIds : [].concat(initialUserIds),
      currentGroupIds : [].concat(initialGroupIds)
    };
    this.context.displayActionPanel = !this.options.readOnly && typeof this.options.userPanelInitUrl === 'string';

    if (this.context.displayActionPanel) {
      if (typeof this.options.jsSaveCallback === 'function') {
        this.context.userPanelSaving = true;
        this.context.saveCallback = this.options.jsSaveCallback;
      } else if (typeof this.options.formSaveSelector === 'string' &&
          this.options.formSaveSelector) {
        this.context.userPanelSaving = true;
        this.context.saveCallback = function() {
          if (jQuery.progressMessage) {
            jQuery.progressMessage();
          }
          try {
            document.querySelector(this.options.formSaveSelector).submit();
          } catch (e) {
            if (jQuery.closeProgressMessage) {
              jQuery.closeProgressMessage();
            }
          }
        }.bind(this);
      }
    }

    var __currentUserId = currentUserId;
    if (this.options.currentUserId && typeof this.options.currentUserId !== 'string') {
      __currentUserId = "" + this.options.currentUserId;
    }
    this.context.currentUserId = __currentUserId;

    var __requester = new UserGroupRequester(options);

    this.refreshAll = function() {
      this.refreshCommons();
      refreshUserData();
      refreshGroupData();
    };

    this.removeAll = function() {
      this.context.currentUserIds = [];
      this.context.currentGroupIds = [];
      if (this.context.userPanelSaving) {
        this.refreshCommons();
      } else {
        this.refreshAll();
      }
    };

    var hasListChanged = function() {
      var i;
      var hasChanged = this.context.currentUserIds.length !== initialUserIds.length ||
          this.context.currentGroupIds.length !== initialGroupIds.length;
      if (!hasChanged && this.context.currentUserIds.length === initialUserIds.length) {
        for (i = 0; i < initialUserIds.length; i++) {
          hasChanged = this.context.currentUserIds.indexOf(initialUserIds[i]) < 0;
          if (hasChanged) {
            break;
          }
        }
      }
      if (!hasChanged && this.context.currentGroupIds.length === initialGroupIds.length) {
        for (i = 0; i < initialGroupIds.length; i++) {
          hasChanged = this.context.currentGroupIds.indexOf(initialGroupIds[i]) < 0;
          if (hasChanged) {
            break;
          }
        }
      }
      return hasChanged;
    }.bind(this);

    this.refreshCommons = function() {
      if (this.context.displayActionPanel) {
        if (!this.context.userPanelSaving) {
          if (hasListChanged()) {
            this.context.messagePanel.style.display = '';
          } else {
            this.context.messagePanel.style.display = 'none';
          }
        }
        if (this.context.currentUserIds.length || this.context.currentGroupIds.length) {
          this.context.clearButton.style.display = '';
        } else {
          this.context.clearButton.style.display = 'none';
        }
      }
      this.context.userSelectionInput.value = this.context.currentUserIds;
      this.context.groupSelectionInput.value = this.context.currentGroupIds;
    };

    var refreshUserData = function() {
      var userIds = this.context.currentUserIds;
      var processUsers = function(userProfiles) {
        this.context.userItems = [];
        userProfiles.forEach(function(userProfile) {
          this.context.userItems.push(new UserItem(userProfile, this));
        }.bind(this));
        __refreshContainer(this.context.userContainer, this.context.userItems);
        activateUserZoom();
      }.bind(this);
      if (userIds.length) {
        __requester.getUsers({
          id : userIds
        }).then(function(users) {
          processUsers.call(this, users);
        }.bind(this));
      } else {
        processUsers.call(this, []);
      }
    }.bind(this);

    var refreshGroupData = function() {
      var groupIds = this.context.currentGroupIds;
      var processGroups = function(groupProfiles) {
        this.context.groupItems = [];
        groupProfiles.forEach(function(groupProfile) {
          this.context.groupItems.push(new UserGroupItem(groupProfile, this));
        }.bind(this));
        __refreshContainer(this.context.groupContainer, this.context.groupItems);
      }.bind(this);
      if (groupIds.length) {
        __requester.getUserGroups({
          ids : groupIds
        }).then(function(groups) {
          processGroups.call(this, groups);
        }.bind(this));
      } else {
        processGroups.call(this, []);
      }
    }.bind(this);

    var processUserPanelChanges = function() {
      if (this.context.displayActionPanel) {
        if (this.context.userPanelSaving) {
          this.context.saveCallback.call(this);
        } else {
          this.refreshAll();
        }
      } else {
        this.refreshAll();
      }
    }.bind(this);

    whenSilverpeasReady(function() {
      this.context.rootContainer = document.querySelector("#" + this.options.rootContainerId);
      __decorateContainer(this);

      if (this.context.displayActionPanel) {
        this.context.clearButton.addEventListener('click', function(e) {
          e.preventDefault();
          e.stopPropagation();
          if (this.context.userPanelSaving) {
            jQuery.popup.confirm("<p>" + LABEL_CONFIRM_DELETE_ALL + "</p>", function() {
              this.removeAll();
              this.context.saveCallback.call(this);
              return true;
            }.bind(this));
          } else {
            this.removeAll();
          }
        }.bind(this));

        this.context.userPanelButton.addEventListener('click', function(e) {
          e.preventDefault();
          e.stopPropagation();
          __openUserPanel(this);
        }.bind(this));
      }

      if (!this.context.readOnly) {
        jQuery(this.context.userSelectionInput).on('change', function() {
          // For now, the user panel sets USER and GROUP identifiers and then trigger a change on
          // USER and an other one on GROUP.
          // So, only USER change event is listened...
          var userPanelValue = this.context.userSelectionInput.value;
          var groupPanelValue = this.context.groupSelectionInput.value;
          this.context.currentUserIds = userPanelValue ? userPanelValue.split(',') : [];
          this.context.currentGroupIds = groupPanelValue ? groupPanelValue.split(',') : [];
          processUserPanelChanges();
        }.bind(this));
      }

      this.refreshAll();
      this.notifyReady();
    }.bind(this));
  };
  
  function __openUserPanel(instance) {
    var params = {
      "formName" : instance.context.userPanelFormName,
      "domainIdFilter" : instance.options.domainIdFilter,
      "instanceId" : instance.options.componentIdFilter,
      "roles" : instance.options.roleFilter.join(','),
      "showDeactivated" : !instance.options.hideDeactivatedState
    };
    var uri = instance.options.userPanelInitUrl;
    if (!uri) {
      uri = webContext  + '/RselectionPeasWrapper/jsp/open';
      params["selectionMultiple"] = instance.options.multiple;
      params["formName"] = 'hotSetting';
      params["elementId"] = instance.options.userPanelId;
      params["selectable"] = instance.options.selectionType;
      params[instance.options.multiple ? "selectedUsers" : "selectedUser"] = instance.context.currentUserIds;
      params[instance.options.multiple ? "selectedGroups" : "selectedGroup"] = instance.context.currentGroupIds;
    } else {
      if (StringUtil.isNotDefined(instance.options.initUserPanelUserIdParamName)) {
        instance.options.initUserPanelUserIdParamName = "UserPanelCurrentUserIds";
      }
      if (StringUtil.isNotDefined(instance.options.initUserPanelGroupIdParamName)) {
        instance.options.initUserPanelGroupIdParamName = "UserPanelCurrentGroupIds";
      }
      params[instance.options.initUserPanelUserIdParamName] = instance.context.currentUserIds;
      params[instance.options.initUserPanelGroupIdParamName] = instance.context.currentGroupIds;
    }
    SP_openUserPanel({url : uri, params : params}, "userPanel");
  }

  function __createHiddenInputs(instance, rootContainer) {
    instance.context.userSelectionInput = instance.options.userSelectionInput;
    if (!instance.context.userSelectionInput) {
      var userInputId = instance.options.userPanelId + "-userIds";
      if (StringUtil.isNotDefined(instance.options.userInputName)) {
        instance.options.userInputName = userInputId;
      }
      var userPanelSelectionInput = document.createElement("input");
      userPanelSelectionInput.setAttribute("type", "text");
      userPanelSelectionInput.setAttribute("id", userInputId);
      userPanelSelectionInput.setAttribute("name", instance.options.userInputName);
      userPanelSelectionInput.value = 'initialized';
      rootContainer.appendChild(userPanelSelectionInput);
      instance.context.userSelectionInput = userPanelSelectionInput;
    }

    instance.context.groupSelectionInput = instance.options.groupSelectionInput;
    if (!instance.context.groupSelectionInput) {
      var groupInputId = instance.options.userPanelId + "-groupIds";
      if (StringUtil.isNotDefined(instance.options.groupInputName)) {
        instance.options.groupInputName = groupInputId;
      }
      var groupPanelSelectionInput = document.createElement("input");
      groupPanelSelectionInput.setAttribute("type", "text");
      groupPanelSelectionInput.setAttribute("id", groupInputId);
      groupPanelSelectionInput.setAttribute("name", instance.options.groupInputName);
      groupPanelSelectionInput.value = 'initialized';
      rootContainer.appendChild(groupPanelSelectionInput);
      instance.context.groupSelectionInput = groupPanelSelectionInput;
    }
  }

  function __refreshContainer(container, items) {
    container.innerHTML = '';
    items.forEach(function(item) {
      container.appendChild(item.getElement());
    });
  }

  /**
   * Render the container.
   * @param instance
   * @private
   */
  function __decorateContainer(instance) {
    var rootContainer = instance.context.rootContainer;
    if (instance.context.displayActionPanel) {
      rootContainer.classList.add("fields");
      var buttonPanel = document.createElement("div");
      buttonPanel.classList.add("buttonPanel");

      var userPanelButton = document.createElement("a");
      userPanelButton.classList.add("explorePanel");
      userPanelButton.setAttribute("href", "#");
      userPanelButton.innerHTML =
          "<span>" + (instance.context.userPanelSaving ? LABEL_UPDATE : LABEL_SELECT) + "</span>";
      instance.context.userPanelButton = userPanelButton;

      var clearButton = document.createElement("a");
      clearButton.classList.add("emptyList");
      clearButton.setAttribute("href", "#");
      clearButton.innerHTML =
          "<span>" + (instance.context.userPanelSaving ? LABEL_DELETE_ALL : LABEL_REMOVE_ALL) + "</span>";
      clearButton.style.display = 'none';
      instance.context.clearButton = clearButton;

      buttonPanel.appendChild(clearButton);
      buttonPanel.appendChild(userPanelButton);
      rootContainer.appendChild(buttonPanel);
    }

    var lists = document.createElement('div');
    lists.classList.add("field", "entireWidth");
    if (instance.context.displayActionPanel) {
      lists.classList.add("contentWithButtonPanel");
    }

    if (!instance.context.userPanelSaving) {
      var messagePanel = document.createElement('div');
      messagePanel.classList.add("inlineMessage");
      messagePanel.style.display = 'none';
      messagePanel.innerHTML = "<span>" + LABEL_LIST_CHANGED + "</span>";
      lists.appendChild(messagePanel);

      instance.context.messagePanel = messagePanel;
    }

    var groups = document.createElement('ul');
    groups.classList.add("access-list", "group");
    var users = document.createElement('ul');
    users.classList.add("access-list", "user");
    lists.appendChild(groups);
    lists.appendChild(users);
    rootContainer.appendChild(lists);

    instance.context.groupContainer = groups;
    instance.context.userContainer = users;
    __createHiddenInputs(instance, rootContainer);
  }

  var Item = SilverpeasClass.extend({
    initialize : function(profile, instance) {
      this.profile = profile;
      this.instance = instance;
    },
    getId : function() {
      return this.profile.id;
    },
    getElement : function() {
      return this.element;
    },
    remove : function() {
      var index = this.handledIds.indexOf(this.profile.id);
      this.handledIds.splice(index, 1);
      this.element.style.opacity = 0.5;
      this.removed = true;
      this.instance.refreshCommons();
    },
    restore : function() {
      this.handledIds.push(this.profile.id);
      this.element.style.opacity = 1;
      this.removed = false;
      this.instance.refreshCommons();
    }
  });

  var UserItem = Item.extend({
    initialize : function(profile, instance) {
      this._super(profile, instance);
      this.element = __createUserElement(this);
      this.handledIds = this.instance.context.currentUserIds;
    },
    getFullName : function() {
      return this.profile.fullName;
    },
    getMailAndDomain : function() {
      return this.profile.eMail + " - " + this.profile.domainName;
    }
  });

  var SelectUserItem = UserItem.extend({
    initialize : function(profile, instance) {
      this.isSelectElement = true;
      this._super(profile, instance);
    }
  });

  var UserGroupItem = Item.extend({
    initialize : function(profile, instance) {
      this._super(profile, instance);
      this.element = __createGroupElement(this);
      this.handledIds = this.instance.context.currentGroupIds;
    },
    getFullName : function() {
      return this.profile.name + " (" + this.profile.userCount + ")";
    },
    isSynchronized : function() {
      return this.profile['synchronized'];
    },
    getNbUsersAndDomain : function() {
      return this.profile.userCount + ' ' + LABEL_USERS + " - " + this.profile.domainName;
    }
  });

  var SelectUserGroupItem = UserGroupItem.extend({
    initialize : function(profile, instance) {
      this.isSelectElement = true;
      this._super(profile, instance);
    }
  });

  function __createUserElement(userItem) {
    var intoList = !userItem.isSelectElement;
    var li = document.createElement(intoList || VANILLA_PLUGIN_USE ? "li" : "div");
    li.classList.add("type-user");
    li.setAttribute("user-id", userItem.getId());
    var avatar = document.createElement("img");
    avatar.setAttribute("alt", "");
    if (userItem.instance.options.displayAvatar) {
      avatar.classList.add("user-avatar");
      avatar.setAttribute("src", userItem.profile.avatar);
    } else {
      if (userItem.profile.blockedState) {
        avatar.setAttribute("src", ICON_USER_BLOCKED);
      } else if (userItem.profile.expiredState) {
        avatar.setAttribute("src", ICON_USER_EXPIRED);
      } else {
        avatar.setAttribute("src", ICON_USER);
      }
    }
    li.appendChild(avatar);
    var span = document.createElement("span");
    if (intoList) {
      if (userItem.instance.context.currentUserId !== userItem.getId()) {
        span.classList.add("userToZoom");
        span.setAttribute("rel", userItem.getId());
      }
      span.innerHTML = userItem.getFullName();
    } else {
      var mainSpan = document.createElement("span");
      mainSpan.classList.add("main-info");
      var extraSpan = document.createElement("span");
      extraSpan.classList.add("extra-info");
      mainSpan.innerHTML = userItem.getFullName();
      extraSpan.innerHTML = userItem.getMailAndDomain();
      span.appendChild(mainSpan);
      span.appendChild(document.createElement("br"));
      span.appendChild(extraSpan);
    }
    li.appendChild(span);
    if (intoList && !userItem.instance.context.readOnly) {
      li.appendChild(__createOperationFragment(userItem));
    }
    return li;
  }

  function __createGroupElement(groupItem) {
    var intoList = !groupItem.isSelectElement;
    var li = document.createElement(intoList || VANILLA_PLUGIN_USE ? "li" : "div");
    li.classList.add("type-group");
    li.setAttribute("group-id", groupItem.getId());
    var avatar = document.createElement("img");
    avatar.setAttribute("src", groupItem.isSynchronized() ? ICON_GROUP_SYNC : ICON_GROUP);
    avatar.setAttribute("alt", "");
    li.appendChild(avatar);
    var span = document.createElement("span");
    if (intoList) {
      span.innerHTML = groupItem.getFullName();
    } else {
      var mainSpan = document.createElement("span");
      mainSpan.classList.add("main-info");
      var extraSpan = document.createElement("span");
      extraSpan.classList.add("extra-info");
      mainSpan.innerHTML = groupItem.getFullName();
      extraSpan.innerHTML = groupItem.getNbUsersAndDomain();
      span.appendChild(mainSpan);
      span.appendChild(document.createElement("br"));
      span.appendChild(extraSpan);
    }
    li.appendChild(span);
    if (intoList && !groupItem.instance.context.readOnly) {
      li.appendChild(__createOperationFragment(groupItem));
    }
    return li;
  }

  function __createOperationFragment(item) {
    var label = (item.instance.context.userPanelSaving ? LABEL_DELETE : LABEL_REMOVE);
    var op = document.createElement("div");
    op.classList.add("operation");
    var a = document.createElement("a");
    a.setAttribute("href", "#");
    a.setAttribute("title", label);
    var img = document.createElement("img");
    img.setAttribute("border", "0");
    img.setAttribute("title", label);
    img.setAttribute("alt", label);
    img.setAttribute("src", "../../util/icons/delete.gif");
    a.appendChild(img);
    op.appendChild(a);

    a.addEventListener("click", function(e) {
      e.preventDefault();
      e.stopPropagation();
      new Promise(function(resolve) {
        if (!item.removed) {
          if (item.instance.context.userPanelSaving) {
            var confirmLabel = UserGroupListBundle.get("GML.confirmation.delete", item.getFullName());
            jQuery.popup.confirm("<p>" + confirmLabel + "</p>", function() {
              item.remove();
              item.instance.context.saveCallback.call(this);
              return true;
            });
          } else {
            item.remove();
            resolve({
              label : LABEL_KEEP, imgSrc : "../../util/icons/refresh.gif"
            });
          }
        } else {
          item.restore();
          resolve({
            label : (item.instance.context.userPanelSaving ? LABEL_DELETE : LABEL_REMOVE),
            imgSrc : "../../util/icons/delete.gif"
          });
        }
      }).then(function(data) {
        img.setAttribute("src", data.imgSrc);
        img.setAttribute("title", data.label);
        img.setAttribute("alt", data.label);
        a.setAttribute("title", data.label);
      });
      return false;
    });

    return op;
  }

  function __convertToString(element) {
    if (typeof element === 'object' && element.length && typeof element[0] !== 'string') {
      var array = [];
      element.forEach(function(value) {
        array.push("" + value);
      });
      return array;
    }
    return element;
  }
})();
