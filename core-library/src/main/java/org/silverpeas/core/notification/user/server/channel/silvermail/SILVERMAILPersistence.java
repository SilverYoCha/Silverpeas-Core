/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.notification.user.server.channel.silvermail;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.notification.user.UserNotificationServerEvent;
import org.silverpeas.core.notification.user.server.channel.silvermail.SilvermailCriteria
    .QUERY_ORDER_BY;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.LongText;
import org.silverpeas.core.security.authorization.ForbiddenRuntimeException;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class declaration
 */
public class SILVERMAILPersistence {

  private static final String CACHE_KEY = SILVERMAILPersistence.class + "@userId@";

  /**
   * Hidden constructor.
   */
  private SILVERMAILPersistence() {
  }

  private static void markMessageAsRead(SILVERMAILMessageBean smb)
      throws SILVERMAILException {
    try {
      boolean hasToUpdate = smb.getReaden() != 1;
      if (hasToUpdate) {
        smb.setReaden(1);
        Transaction.performInOne(() -> getRepository().save(smb));
        DefaultServerEventNotifier.get().notify(UserNotificationServerEvent
            .readOf(String.valueOf(smb.getUserId()), smb.getId(), smb.getSubject(), smb
                .getSenderName()));
      }
    } catch (Exception e) {
      throw new SILVERMAILException(
          "SILVERMAILPersistence.markMessageAsReaden()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_READ_MSG", "MsgId=" + smb.getId(), e);
    }
  }

  private static SILVERMAILMessageBeanRepository getRepository() {
    return ServiceProvider.getService(SILVERMAILMessageBeanRepository.class);
  }

  /**
   * @param criteria the criteria with which the search is parametrized.
   * @return the list of {@link SILVERMAILMessage} instances.
   * @throws SILVERMAILException
   */
  private static SilverpeasList<SILVERMAILMessage> findByCriteria(SilvermailCriteria criteria)
      throws SILVERMAILException {
    final SilverpeasList<SILVERMAILMessageBean> messageBeans =
        getRepository().findByCriteria(criteria);
    List<Integer> longTextIds =
        messageBeans.stream().map(n -> Integer.parseInt(n.getBody())).collect(Collectors.toList());
    final Mutable<Integer> offset = Mutable.of(0);
    CollectionUtil.splitList(longTextIds).forEach(batchIds -> {
      final Map<Integer, String> contents = LongText.listLongTexts(batchIds);
      for (int n = offset.get(), c = 0; c < batchIds.size(); n++, c++) {
        final SILVERMAILMessageBean notif = messageBeans.get(n);
        notif.setBody(contents.get(batchIds.get(c)));
      }
      offset.set(offset.get() + batchIds.size());
    });
    return messageBeans.stream()
        .map(SILVERMAILPersistence::convertFrom)
        .collect(SilverpeasList.collector(messageBeans));
  }

  /**
   * Gets the login of a user represented by the given identifier.
   * <p>A request cache is handled.</p>
   * @param userId the identifier of a user.
   * @return a login as string.
   */
  private static String getUserLogin(long userId) {
    SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    final String cacheKey = CACHE_KEY + userId;
    String login = cache.get(cacheKey, String.class);
    if (login == null) {
      User user = User.getById(Long.toString(userId));
      login = user != null ? user.getLogin() : StringUtil.EMPTY;
      cache.put(cacheKey, login);
    }
    return login;
  }

  /**
   *
   */
  public static void addMessage(SILVERMAILMessage silverMsg) throws SILVERMAILException {
    SILVERMAILMessageBean smb = new SILVERMAILMessageBean();
    if (silverMsg != null) {
      try {
        smb.setUserId(silverMsg.getUserId());
        smb.setSenderName(silverMsg.getSenderName());
        // 0 = INBOX
        smb.setFolderId(0);
        smb.setSubject(silverMsg.getSubject());
        smb.setBody(Integer.toString(LongText.addLongText(silverMsg.getBody())));
        smb.setUrl(silverMsg.getUrl());
        smb.setSource(silverMsg.getSource());
        smb.setDateMsg(DateUtil.date2SQLDate(silverMsg.getDate()));
        smb.setReaden(0);
        Transaction.performInOne(() -> getRepository().save(smb));
        DefaultServerEventNotifier.get().notify(UserNotificationServerEvent
            .creationOf(String.valueOf(smb.getUserId()), smb.getId(), smb.getSubject(),
                smb.getSenderName()));
      } catch (Exception e) {
        throw new SILVERMAILException("SILVERMAILPersistence.addMessage()",
            SilverpeasException.ERROR, "silvermail.EX_CANT_WRITE_MESSAGE", e);
      }
    }
  }

  public static long countNotReadMessagesOfFolder(String userId, String folderName) {
    return getRepository().countByCriteria(SilvermailCriteria.get()
        .aboutUser(userId)
        .into(folderName)
        .unread());
  }

  public static long countReadMessagesOfFolder(String userId, String folderName) {
    return getRepository().countByCriteria(SilvermailCriteria.get()
        .aboutUser(userId)
        .into(folderName)
        .read());
  }

  public static long countMessagesOfFolder(String userId, String folderName) {
    return getRepository().countByCriteria(SilvermailCriteria.get()
        .aboutUser(userId)
        .into(folderName));
  }

  public static SilverpeasList<SILVERMAILMessage> getNotReadMessagesOfFolder(String userId,
      String folderName, final PaginationPage pagination, final QUERY_ORDER_BY orderBy)
      throws SILVERMAILException {
    final SilvermailCriteria criteria =
        SilvermailCriteria.get().aboutUser(userId).into(folderName).unread()
            .paginatedBy(pagination);
    if (orderBy != null) {
      criteria.orderedBy(orderBy);
    }
    return findByCriteria(criteria);
  }

  public static SilverpeasList<SILVERMAILMessage> getReadMessagesOfFolder(String userId,
      String folderName, final PaginationPage pagination, final QUERY_ORDER_BY orderBy)
      throws SILVERMAILException {
    final SilvermailCriteria criteria =
        SilvermailCriteria.get().aboutUser(userId).into(folderName).read().paginatedBy(pagination);
    if (orderBy != null) {
      criteria.orderedBy(orderBy);
    }
    return findByCriteria(criteria);
  }

  public static SilverpeasList<SILVERMAILMessage> getMessageOfFolder(String userId, String folderName,
      final PaginationPage pagination, final QUERY_ORDER_BY orderBy) throws SILVERMAILException {
    final SilvermailCriteria criteria =
        SilvermailCriteria.get().aboutUser(userId).into(folderName).paginatedBy(pagination);
    if (orderBy != null) {
      criteria.orderedBy(orderBy);
    }
    return findByCriteria(criteria);
  }

  /**
   * Gets a message by its identifier.
   * @param msgId the message identifier.
   */
  public static SILVERMAILMessage getMessage(long msgId) throws SILVERMAILException {
    SILVERMAILMessage silverMailMessage =
        findByCriteria(SilvermailCriteria.get().byId(msgId)).stream().findFirst().orElse(null);
    if (silverMailMessage != null) {
      SILVERMAILMessageBean smb = getRepository().getById(String.valueOf(msgId));
      markMessageAsRead(smb);
    }
    return silverMailMessage;
  }

  /**
   *
   */
  public static void deleteMessage(long msgId, String userId) throws SILVERMAILException {
    try {
      Transaction.performInOne(() -> {
        SILVERMAILMessageBeanRepository repository = getRepository();
        SILVERMAILMessageBean toDel = repository.getById(String.valueOf(msgId));

        //check rights : check that the current user has the rights to delete the message
        // notification
        if (Long.parseLong(userId) == toDel.getUserId()) {
          try {
            int longTextId = Integer.parseInt(toDel.getBody());
            LongText.removeLongText(longTextId);
          } catch (Exception e) {
            SilverLogger.getLogger(SILVERMAILPersistence.class).error(e);
          }
          repository.delete(toDel);
        } else {
          throw new ForbiddenRuntimeException("SILVERMAILPersistence.deleteMessage()",
              SilverpeasRuntimeException.ERROR, "peasCore.RESOURCE_ACCESS_UNAUTHORIZED",
              "notifId=" + msgId + ", userId=" + userId);
        }
        return null;
      });
      DefaultServerEventNotifier.get()
          .notify(UserNotificationServerEvent.deletionOf(userId, String.valueOf(msgId)));
    } catch (Exception e) {
      throw new SILVERMAILException("SILVERMAILPersistence.deleteMessage()",
          SilverpeasException.ERROR, "silvermail.EX_CANT_DEL_MSG", "MsgId="
          + Long.toString(msgId), e);
    }
  }

  public static void deleteAllMessagesInFolder(String currentUserId, String folderName)
      throws SILVERMAILException {
    String folderId = "INBOX".equals(folderName) ? "0" : "0";
    long nbDeleted = Transaction.performInOne(() -> getRepository()
        .deleteAllMessagesByUserIdAndFolderId(currentUserId, folderId));

    if (nbDeleted > 0) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
  }

  public static void markAllMessagesAsRead(String currentUserId) throws SILVERMAILException {
    long nbUpdated = Transaction.performInOne(() -> getRepository()
        .markAsReadAllMessagesByUserIdAndFolderId(currentUserId, "0"));
    if (nbUpdated > 0) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
  }

  public static void deleteMessages(String currentUserId, Collection<String> ids)
      throws SILVERMAILException {
    long nbDeleted = Transaction.performInOne(() -> getRepository()
        .deleteMessagesByUserIdAndByIds(currentUserId, ids));

    if (nbDeleted > 0) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
  }

  public static void markMessagesAsRead(String currentUserId, Collection<String> ids)
      throws SILVERMAILException {
    long nbUpdated = Transaction.performInOne(() -> getRepository()
        .markAsReadMessagesByUserIdAndByIds(currentUserId, ids));
    if (nbUpdated > 0) {
      DefaultServerEventNotifier.get().notify(UserNotificationServerEvent.clear(currentUserId));
    }
  }

  private static SILVERMAILMessage convertFrom(final SILVERMAILMessageBean smb) {
    final Date msgDate;
    try {
      msgDate = DateUtil.parseDate(smb.getDateMsg());
    } catch (ParseException e) {
      throw new org.silverpeas.core.SilverpeasRuntimeException(e);
    }
    SILVERMAILMessage silverMailMessage = new SILVERMAILMessage();
    silverMailMessage.setId(Long.parseLong(smb.getId()));
    silverMailMessage.setUserId(smb.getUserId());
    silverMailMessage.setUserLogin(getUserLogin(smb.getUserId()));
    silverMailMessage.setSenderName(smb.getSenderName());
    silverMailMessage.setSubject(smb.getSubject());
    silverMailMessage.setUrl(smb.getUrl());
    silverMailMessage.setSource(smb.getSource());
    silverMailMessage.setBody(smb.getBody());
    silverMailMessage.setDate(msgDate);
    silverMailMessage.setReaden(smb.getReaden());
    return silverMailMessage;
  }
}
