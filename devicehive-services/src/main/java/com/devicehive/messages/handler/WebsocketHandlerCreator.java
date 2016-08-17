package com.devicehive.messages.handler;

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

import com.devicehive.model.DeviceCommand;
import com.devicehive.model.DeviceNotification;
import com.devicehive.util.ApplicationContextHolder;
import com.devicehive.util.ServerResponsesFactory;
import com.devicehive.websockets.HiveWebsocketSessionState;
import com.devicehive.websockets.util.AsyncMessageSupplier;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;
import java.util.concurrent.locks.Lock;

public abstract class WebsocketHandlerCreator<T> implements HandlerCreator<T> {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketHandlerCreator.class);

    private final WebSocketSession session;
    private final Lock lock;


    private WebsocketHandlerCreator(WebSocketSession session, Lock lock) {
        this.session = session;
        this.lock = lock;
    }

    public static WebsocketHandlerCreator<DeviceCommand> createCommandInsert(WebSocketSession session) {
        return new WebsocketHandlerCreator<DeviceCommand>(session,
                                                          HiveWebsocketSessionState.get(session)
                                                              .getCommandSubscriptionsLock()) {
            @Override
            protected JsonObject createJsonObject(DeviceCommand message, UUID subId) {
                return ServerResponsesFactory.createCommandInsertMessage(message, subId);
            }
        };
    }

    public static WebsocketHandlerCreator<DeviceCommand> createCommandUpdate(WebSocketSession session) {
        return new WebsocketHandlerCreator<DeviceCommand>(session,
                                                          HiveWebsocketSessionState.get(session)
                                                              .getCommandUpdateSubscriptionsLock()) {
            @Override
            protected JsonObject createJsonObject(DeviceCommand message, UUID subId) {
                return ServerResponsesFactory.createCommandUpdateMessage(message);
            }
        };
    }

    public static WebsocketHandlerCreator<DeviceNotification> createNotificationInsert(WebSocketSession session) {
        return new WebsocketHandlerCreator<DeviceNotification>(session,
                                                               HiveWebsocketSessionState.get(session)
                                                                   .getNotificationSubscriptionsLock()) {
            @Override
            protected JsonObject createJsonObject(DeviceNotification message, UUID subId) {
                return ServerResponsesFactory.createNotificationInsertMessage(message, subId);
            }
        };
    }

    protected abstract JsonObject createJsonObject(T message, UUID subId);

    @Override
    public Runnable getHandler(final T message, final UUID subId) {
        logger.debug("Websocket subscription notified");

        return () -> {
            if (!session.isOpen()) {
                return;
            }
            JsonObject json = createJsonObject(message, subId);
            try {
                lock.lock();
                logger.debug("Add messages to queue process for session " + session.getId());
                HiveWebsocketSessionState.get(session).getQueue().add(json);
            } finally {
                lock.unlock();
            }
            ApplicationContextHolder.getApplicationContext().getBean(AsyncMessageSupplier.class).deliverMessages(session);
        };
    }
}
