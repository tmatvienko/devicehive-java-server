package com.devicehive.service;

/*
 * #%L
 * DeviceHive Java Server Common business logic
 * %%
 * Copyright (C) 2016 DataArt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.devicehive.dao.DeviceDao;
import com.devicehive.service.time.TimestampService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.devicehive.configuration.Constants.DEVICE_OFFLINE_STATUS;


@Component
@Lazy(false)
public class DeviceActivityService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceActivityService.class);
    private static final Integer PROCESS_DEVICES_BUFFER_SIZE = 100;

    @Autowired
    private DeviceDao deviceDAO;

    @Autowired
    private TimestampService timestampService;

    private ConcurrentHashMap<String, Long> deviceActivityMap = new ConcurrentHashMap<>();

    public void update(String deviceGuid) {
        deviceActivityMap.put(deviceGuid, timestampService.getTimestamp());
    }

    @Scheduled(cron = "0 * * * * *") //executing at start of every minute
    public void processOfflineDevices() {
        logger.debug("Checking lost offline devices");
        long now = timestampService.getTimestamp();
        List<String> activityKeys = Collections.list(deviceActivityMap.keys());
        int indexFrom = 0;
        int indexTo = Math.min(activityKeys.size(), indexFrom + PROCESS_DEVICES_BUFFER_SIZE);
        while (indexFrom < indexTo) {
            List<String> guids = activityKeys.subList(indexFrom, indexTo);
            Map<String, Integer> devicesGuidsAndOfflineTime = deviceDAO.getOfflineTimeForDevices(guids);
            doProcess(guids, devicesGuidsAndOfflineTime, now);
            indexFrom = indexTo;
            indexTo = Math.min(activityKeys.size(), indexFrom + PROCESS_DEVICES_BUFFER_SIZE);
        }
        logger.debug("Checking lost offline devices complete");
    }

    private void doProcess(List<String> guids, Map<String, Integer> devicesGuidsAndOfflineTime, Long now) {
        List<String> toUpdateStatus = new ArrayList<>();
        for (final String deviceGuid : guids) {
            if (!devicesGuidsAndOfflineTime.containsKey(deviceGuid)) {
                logger.warn("Device with guid {} does not exists", deviceGuid);
                deviceActivityMap.remove(deviceGuid);
            } else {
                logger.debug("Checking device {} ", deviceGuid);
                Integer offlineTimeout = devicesGuidsAndOfflineTime.get(deviceGuid);
                if (offlineTimeout != null) {
                    Long time = deviceActivityMap.get(deviceGuid);
                    if (now - time > offlineTimeout * 1000) {
                        if (deviceActivityMap.remove(deviceGuid, time)) {
                            toUpdateStatus.add(deviceGuid);
                        }
                    }
                }
            }
        }
        if (!toUpdateStatus.isEmpty()) {
            deviceDAO.changeStatusForDevices(DEVICE_OFFLINE_STATUS, toUpdateStatus);
        }
    }

}
