package com.devicehive.testutil;

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


import com.devicehive.dao.*;
import com.devicehive.model.enums.AccessKeyType;
import com.devicehive.model.enums.UserRole;
import com.devicehive.model.enums.UserStatus;
import com.devicehive.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;

@Profile("test")
@Component
@Lazy(false)
public class InitialDataLoader {
    @Autowired
    DeviceClassDao deviceClassDao;

    @Autowired
    UserDao userDao;

    @Autowired
    ConfigurationDao configurationDao;

    @Autowired
    NetworkDao networkDao;

    @Autowired
    DeviceDao deviceDao;

    @PostConstruct
    public void initialData () {

        UserVO user = new UserVO();
        user.setId(2L);
        user.setLogin("test_admin");
        user.setPasswordHash("+IC4w+NeByiymEWlI5H1xbtNe4YKmPlLRZ7j3xaireg=");
        user.setPasswordSalt("9KynX3ShWnFym4y8Dla039py");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);
        userDao.persist(user);

        ConfigurationVO cfg;
        cfg = new ConfigurationVO("google.identity.allowed", "true");
        configurationDao.persist(cfg);
        cfg = new ConfigurationVO("google.identity.client.id", "google_id");
        configurationDao.persist(cfg);
        cfg = new ConfigurationVO("facebook.identity.allowed", "true");
        configurationDao.persist(cfg);
        cfg = new ConfigurationVO("facebook.identity.client.id", "facebook_id");
        configurationDao.persist(cfg);
        cfg = new ConfigurationVO("github.identity.allowed", "true");
        configurationDao.persist(cfg);
        cfg = new ConfigurationVO("github.identity.client.id", "github_id");
        configurationDao.persist(cfg);
        cfg = new ConfigurationVO("session.timeout", "1200000");
        configurationDao.persist(cfg);
        cfg = new ConfigurationVO("allowNetworkAutoCreate", "true");
        configurationDao.persist(cfg);

        // -- 2. Default device classes
        //INSERT INTO device_class (name, is_permanent, offline_timeout) VALUES ('Sample VirtualLed Device', FALSE, 600);

        DeviceClassWithEquipmentVO deviceClass = new DeviceClassWithEquipmentVO();
        deviceClass.setId(1L);
        deviceClass.setName("Sample VirtualLed Device");
        deviceClass.setIsPermanent(false);
        deviceClass.setOfflineTimeout(600);
        deviceClassDao.persist(deviceClass);
        //INSERT INTO network (name, description) VALUES ('VirtualLed Sample Network', 'A DeviceHive network for VirtualLed sample');

        NetworkVO network = new NetworkVO();
        network.setId(1L);
        network.setName("VirtualLed Sample Network");
        network.setDescription("A DeviceHive network for VirtualLed sample");
        networkDao.persist(network);

        //INSERT INTO device (guid, name, status, network_id, device_class_id, entity_version) VALUES
        // ('E50D6085-2ABA-48E9-B1C3-73C673E414BE', 'Sample VirtualLed Device', 'Offline', 1, 1, 1);
        DeviceVO device = new DeviceVO();
        device.setId(1L);
        device.setGuid("E50D6085-2ABA-48E9-B1C3-73C673E414BE");
        device.setName("Sample VirtualLed Device");
        device.setStatus("Offline");
        device.setNetwork(network);
        device.setDeviceClass(deviceClass);
        deviceDao.persist(device);

        //live data
        UserVO user2 = new UserVO();
        user2.setId(1L);
        user2.setLogin("dhadmin");
        user2.setPasswordHash("DFXFrZ8VQIkOYECScBbBwsYinj+o8IlaLsRQ81wO+l8=");
        user2.setPasswordSalt("sjQbZgcCmFxqTV4CCmGwpIHO");
        user2.setRole(UserRole.ADMIN);
        user2.setStatus(UserStatus.ACTIVE);
        user2.setLoginAttempts(0);
        userDao.persist(user2);

        cfg = new ConfigurationVO("websocket.ping.timeout", "120000");
        configurationDao.persist(cfg);

        cfg = new ConfigurationVO("cassandra.rest.endpoint", "http://127.0.0.1:8080/cassandra");
        configurationDao.persist(cfg);

        cfg = new ConfigurationVO("user.login.lastTimeout", "1000");
        configurationDao.persist(cfg);
    }


}
