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
package com.silverpeas.usernotification.delayed;

import com.ibm.icu.util.Calendar;
import com.silverpeas.usernotification.delayed.constant.DelayedNotificationFrequency;
import com.silverpeas.usernotification.delayed.model.DelayedNotificationData;
import com.silverpeas.usernotification.delayed.model.DelayedNotificationUserSetting;
import com.silverpeas.usernotification.delayed.repository.DelayedNotificationDataManager;
import com.silverpeas.usernotification.delayed.repository.DelayedNotificationUserSettingJpaManager;
import com.silverpeas.usernotification.model.NotificationResourceData;
import com.silverpeas.usernotification.repository.NotificationResourceDataManager;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.util.CollectionUtil;
import org.silverpeas.util.DateUtil;
import org.silverpeas.util.MapUtil;
import org.silverpeas.util.ResourceLocator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Yohann Chastagnier
 */
@Singleton
@Transactional
public class DelayedNotificationManager implements DelayedNotification {
  private static final Object SYNCHRONIZED = new Object();

  /**
   * Settings location
   */
  private final ResourceLocator settings =
      new ResourceLocator("org.silverpeas.notificationManager.settings.notificationManagerSettings",
          "");

  /**
   * Contains the possible frequencies for users
   */
  private static Set<DelayedNotificationFrequency> POSSIBLE_FREQUENCIES;

  /**
   * For now, only the SMTP channel can be delayed (mail)
   */
  private static final Set<NotifChannel> WIRED_CHANNELS = new HashSet<>();

  static {
    WIRED_CHANNELS.add(NotifChannel.SMTP);
  }

  @Inject
  private DelayedNotificationDataManager dnRepository;

  @Inject
  private NotificationResourceDataManager nrRepository;

  @Inject
  private DelayedNotificationUserSettingJpaManager dnUserSettingManager;

  @Override
  public Map<NotifChannel, List<DelayedNotificationData>>
  findDelayedNotificationByUserIdGroupByChannel(
      final int userId, final Set<NotifChannel> aimedChannels) {
    final Map<NotifChannel, List<DelayedNotificationData>> result = new LinkedHashMap<>();
    for (final DelayedNotificationData data : dnRepository
        .findByUserId(userId, NotifChannel.toIds(aimedChannels))) {
      MapUtil.putAddList(result, data.getChannel(), data);
    }
    return result;
  }

  @Override
  public List<Integer> findAllUsersToBeNotified(final Set<NotifChannel> aimedChannels) {
    return dnRepository.findAllUsersToBeNotified(NotifChannel.toIds(aimedChannels));
  }

  @Override
  public List<Integer> findUsersToBeNotified(final Date date, final Set<NotifChannel> aimedChannels,
      final DelayedNotificationFrequency defaultDelayedNotificationFrequency) {

    final Date dateOfDay = DateUtil.getBeginOfDay(date);

    // Calculating aimed frequencies
    final Set<DelayedNotificationFrequency> aimedFrequencies = new HashSet<>();

    // Daily frequency is logically aimed
    aimedFrequencies.add(DelayedNotificationFrequency.DAILY);

    // Weekly frequency if the date is monday
    if (Calendar.MONDAY == DateUtil.getDayNumberInWeek(dateOfDay)) {
      aimedFrequencies.add(DelayedNotificationFrequency.WEEKLY);
    }

    // Monthly frequency if the date is the first of the month
    if (DateUtil.getFirstDateOfMonth(dateOfDay).compareTo(dateOfDay) == 0) {
      aimedFrequencies.add(DelayedNotificationFrequency.MONTHLY);
    }

    // Searching
    return dnRepository.findUsersToBeNotified(aimedChannels, aimedFrequencies,
        aimedFrequencies.contains(defaultDelayedNotificationFrequency));
  }

  @Override
  public void saveDelayedNotification(final DelayedNotificationData delayedNotificationData) {
    if (delayedNotificationData.getResource().getId() == null) {
      final NotificationResourceData existingResource =
          getExistingResource(delayedNotificationData.getResource().getResourceId(),
              delayedNotificationData.getResource().getResourceType(),
              delayedNotificationData.getResource().getComponentInstanceId());
      if (existingResource != null) {
        existingResource.fillFrom(delayedNotificationData.getResource());
        nrRepository.saveAndFlush(existingResource);
        delayedNotificationData.setResource(existingResource);
        if (delayedNotificationData.getId() == null) {
          final List<DelayedNotificationData> exists =
              dnRepository.findDelayedNotification(delayedNotificationData);
          if (exists.size() == 1) {
            delayedNotificationData.setId(Long.valueOf(exists.get(0).getId()));
          } else {
            dnRepository.saveAndFlush(delayedNotificationData);
          }
        } else {
          dnRepository.saveAndFlush(delayedNotificationData);
        }
      } else {
        nrRepository.save(delayedNotificationData.getResource());
        dnRepository.saveAndFlush(delayedNotificationData);
      }
    } else {
      dnRepository.saveAndFlush(delayedNotificationData);
    }
  }

  @Override
  public int deleteDelayedNotifications(final Collection<Long> ids) {
    int nbDeletes = 0;
    if (CollectionUtil.isNotEmpty(ids)) {
      for (final Collection<Long> idLot : CollectionUtil.split(ids)) {
        nbDeletes += dnRepository.deleteByIds(idLot);
      }
      nrRepository.deleteResources();
      dnRepository.flush();
    }
    return nbDeletes;
  }

  /*
   * Resource Data
   */

  @Override
  public NotificationResourceData getExistingResource(final String resourceId,
      final String resourceType, final String componentInstanceId) {
    return nrRepository.getExistingResource(resourceId, resourceType, componentInstanceId);
  }

  /*
   * User settings
   */

  @Override
  public DelayedNotificationUserSetting getDelayedNotificationUserSetting(final int id) {
    return dnUserSettingManager.getById(Integer.toString(id));
  }

  @Override
  public List<DelayedNotificationUserSetting> findDelayedNotificationUserSettingByUserId(
      final int userId) {
    return dnUserSettingManager.findByUserId(userId);
  }

  @Override
  public DelayedNotificationUserSetting getDelayedNotificationUserSettingByUserIdAndChannel(
      final int userId, final NotifChannel channel) {
    final List<DelayedNotificationUserSetting> userSettings =
        dnUserSettingManager.findByUserIdAndChannel(userId, channel.getId());
    DelayedNotificationUserSetting result = null;
    if (!userSettings.isEmpty()) {
      result = userSettings.iterator().next();
    }
    return result;
  }

  @Override
  public DelayedNotificationUserSetting saveDelayedNotificationUserSetting(final int userId,
      final NotifChannel channel, final DelayedNotificationFrequency frequency) {
    DelayedNotificationUserSetting userSettings =
        getDelayedNotificationUserSettingByUserIdAndChannel(userId, channel);
    if (userSettings == null) {
      userSettings = new DelayedNotificationUserSetting();
      userSettings.setUserId(userId);
      userSettings.setChannel(channel);
    }
    userSettings.setFrequency(frequency);
    dnUserSettingManager.saveAndFlush(userSettings);
    return userSettings;
  }

  @Override
  public void deleteDelayedNotificationUserSetting(final int id) {
    dnUserSettingManager.deleteById(Integer.toString(id));
    dnUserSettingManager.flush();
  }

  @Override
  public void deleteDelayedNotificationUserSetting(
      final DelayedNotificationUserSetting delayedNotificationUserSetting) {
    dnUserSettingManager.delete(delayedNotificationUserSetting);
    dnUserSettingManager.flush();
  }

  /*
   * Commons
   */

  @Override
  @Transactional(Transactional.TxType.NOT_SUPPORTED)
  public Set<NotifChannel> getWiredChannels() {
    return WIRED_CHANNELS;
  }

  @Override
  @Transactional(Transactional.TxType.NOT_SUPPORTED)
  public Set<DelayedNotificationFrequency> getPossibleFrequencies() {
    if (POSSIBLE_FREQUENCIES == null) {
      synchronized (SYNCHRONIZED) {
        if (POSSIBLE_FREQUENCIES == null) {

          // Initialization
          final Set<DelayedNotificationFrequency> possibleFrequencies = new HashSet<>();

          // The parameter value
          final String frequencyChoiceList =
              settings.getString("DELAYED_NOTIFICATION_FREQUENCY_CHOICE_LIST", "")
                  .replaceAll(" ", "");

          // The posible frequencies
          if (StringUtils.isNotBlank(frequencyChoiceList)) {
            for (final String frequencyCode : frequencyChoiceList.split("[,;|]")) {
              if ("*".equals(frequencyCode)) {
                possibleFrequencies.addAll(Arrays.asList(DelayedNotificationFrequency.values()));
              } else {
                possibleFrequencies.add(DelayedNotificationFrequency.decode(frequencyCode));
              }
            }
          }

          // Eliminating wrong frequencies
          possibleFrequencies.remove(null);
          POSSIBLE_FREQUENCIES = new TreeSet<>(possibleFrequencies);
        }
      }
    }
    return POSSIBLE_FREQUENCIES;
  }

  @Override
  @Transactional(Transactional.TxType.NOT_SUPPORTED)
  public DelayedNotificationFrequency getDefaultDelayedNotificationFrequency() {
    DelayedNotificationFrequency defaultFrequency = DelayedNotificationFrequency
        .decode(settings.getString("DEFAULT_DELAYED_NOTIFICATION_FREQUENCY"));
    if (defaultFrequency == null) {
      defaultFrequency = DelayedNotificationFrequency.NONE;
    }
    return defaultFrequency;
  }

  @Override
  public DelayedNotificationFrequency getUserFrequency(final Integer userId,
      final NotifChannel channel) {
    DelayedNotificationFrequency result = DelayedNotificationFrequency.NONE;

    // For now, only the SMTP channel can be delayed (mail)
    if (getWiredChannels().contains(channel)) {

      // Search in the database the user's setting
      final DelayedNotificationUserSetting dnus =
          (userId != null) ? getDelayedNotificationUserSettingByUserIdAndChannel(userId, channel) :
              null;

      // If no user setting data or that the frequency is not possible, the default frequency is
      // retrieved
      if (dnus == null || !getPossibleFrequencies().contains(dnus.getFrequency())) {
        result = getDefaultDelayedNotificationFrequency();
      } else {
        result = dnus.getFrequency();
      }
    }

    return result;
  }
}