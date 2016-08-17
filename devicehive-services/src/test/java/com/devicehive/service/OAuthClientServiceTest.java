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

import com.devicehive.base.AbstractResourceTest;
import com.devicehive.configuration.Messages;
import com.devicehive.exceptions.ActionNotAllowedException;
import com.devicehive.model.updates.OAuthClientUpdate;
import com.devicehive.vo.OAuthClientVO;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class OAuthClientServiceTest extends AbstractResourceTest {

    @Autowired
    private OAuthClientService clientService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_create_oauth_client() throws Exception {
        OAuthClientVO client = new OAuthClientVO();
        client.setName(RandomStringUtils.randomAlphabetic(10));
        client.setOauthId(RandomStringUtils.randomAlphabetic(10));
        client.setDomain(RandomStringUtils.randomAlphabetic(10));
        client.setRedirectUri(RandomStringUtils.randomAlphabetic(10));

        OAuthClientVO created = clientService.insert(client);
        assertThat(created, notNullValue());
        assertThat(created.getId(), notNullValue());
        assertThat(created.getName(), equalTo(client.getName()));
        assertThat(created.getOauthId(), equalTo(client.getOauthId()));
        assertThat(created.getDomain(), equalTo(client.getDomain()));
        assertThat(created.getRedirectUri(), equalTo(client.getRedirectUri()));
        assertThat(created.getOauthSecret(), notNullValue());
    }

    @Test
    public void should_throw_ActionNotAllowedException_if_client_exists_on_create() throws Exception {
        OAuthClientVO existed = new OAuthClientVO();
        existed.setName(RandomStringUtils.randomAlphabetic(10));
        existed.setOauthId(RandomStringUtils.randomAlphabetic(10));
        existed.setDomain(RandomStringUtils.randomAlphabetic(10));
        existed.setRedirectUri(RandomStringUtils.randomAlphabetic(10));
        clientService.insert(existed);

        OAuthClientVO client = new OAuthClientVO();
        client.setName(RandomStringUtils.randomAlphabetic(10));
        client.setOauthId(existed.getOauthId());
        client.setDomain(RandomStringUtils.randomAlphabetic(10));
        client.setRedirectUri(RandomStringUtils.randomAlphabetic(10));

        expectedException.expect(ActionNotAllowedException.class);
        expectedException.expectMessage(Messages.DUPLICATE_OAUTH_ID);

        clientService.insert(client);
    }

    @Test
    public void should_update_oauth_client() throws Exception {
        OAuthClientVO client = new OAuthClientVO();
        client.setName(RandomStringUtils.randomAlphabetic(10));
        client.setOauthId(RandomStringUtils.randomAlphabetic(10));
        client.setDomain(RandomStringUtils.randomAlphabetic(10));
        client.setRedirectUri(RandomStringUtils.randomAlphabetic(10));
        OAuthClientVO created = clientService.insert(client);

        OAuthClientUpdate update = new OAuthClientUpdate();
        update.setName(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));
        update.setDomain(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));
        update.setSubnet(Optional.ofNullable("localhost"));
        update.setRedirectUri(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));
        update.setOauthId(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));

        boolean updated = clientService.update(update, created.getId());
        assertTrue(updated);
        OAuthClientVO updatedClient = clientService.get(created.getId());
        assertThat(updatedClient, notNullValue());
        assertThat(updatedClient.getId(), equalTo(created.getId()));
        assertThat(updatedClient.getName(), equalTo(update.getName().get()));
        assertThat(updatedClient.getDomain(), equalTo(update.getDomain().get()));
        assertThat(updatedClient.getSubnet(), equalTo(update.getSubnet().get()));
        assertThat(updatedClient.getRedirectUri(), equalTo(update.getRedirectUri().get()));
        assertThat(updatedClient.getOauthId(), equalTo(update.getOauthId().get()));
    }

    @Test
    public void should_throw_ActionNowAllowedException_if_oauth_id_exists_on_update() throws Exception {
        OAuthClientVO client1 = new OAuthClientVO();
        client1.setName(RandomStringUtils.randomAlphabetic(10));
        client1.setOauthId(RandomStringUtils.randomAlphabetic(10));
        client1.setDomain(RandomStringUtils.randomAlphabetic(10));
        client1.setRedirectUri(RandomStringUtils.randomAlphabetic(10));
        clientService.insert(client1);

        OAuthClientVO client2 = new OAuthClientVO();
        client2.setName(RandomStringUtils.randomAlphabetic(10));
        client2.setOauthId(RandomStringUtils.randomAlphabetic(10));
        client2.setDomain(RandomStringUtils.randomAlphabetic(10));
        client2.setRedirectUri(RandomStringUtils.randomAlphabetic(10));
        clientService.insert(client2);

        OAuthClientUpdate update = new OAuthClientUpdate();
        update.setName(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));
        update.setDomain(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));
        update.setSubnet(Optional.ofNullable("localhost"));
        update.setRedirectUri(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));
        update.setOauthId(Optional.ofNullable(client1.getOauthId()));

        expectedException.expect(ActionNotAllowedException.class);
        expectedException.expectMessage(Messages.DUPLICATE_OAUTH_ID);

        clientService.update(update, client2.getId());
    }

    @Test
    public void should_throw_NoSuchElementException_if_client_does_not_exist_on_update() throws Exception {
        OAuthClientUpdate update = new OAuthClientUpdate();
        update.setName(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));
        update.setDomain(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));
        update.setSubnet(Optional.ofNullable("localhost"));
        update.setRedirectUri(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));
        update.setOauthId(Optional.ofNullable(RandomStringUtils.randomAlphabetic(10)));

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage(String.format(Messages.OAUTH_CLIENT_NOT_FOUND, -1));

        clientService.update(update, -1L);
    }

    @Test
    public void should_delete_oauth_client() throws Exception {
        OAuthClientVO client = new OAuthClientVO();
        client.setName(RandomStringUtils.randomAlphabetic(10));
        client.setOauthId(RandomStringUtils.randomAlphabetic(10));
        client.setDomain(RandomStringUtils.randomAlphabetic(10));
        client.setRedirectUri(RandomStringUtils.randomAlphabetic(10));
        OAuthClientVO created = clientService.insert(client);
        assertThat(created, notNullValue());

        clientService.delete(created.getId());
        OAuthClientVO deleted = clientService.get(created.getId());
        assertThat(deleted, nullValue());
    }

    @Test
    public void should_return_client_by_oauth_id() throws Exception {
        OAuthClientVO client = new OAuthClientVO();
        client.setName(RandomStringUtils.randomAlphabetic(10));
        client.setOauthId(RandomStringUtils.randomAlphabetic(10));
        client.setDomain(RandomStringUtils.randomAlphabetic(10));
        client.setRedirectUri(RandomStringUtils.randomAlphabetic(10));
        OAuthClientVO created = clientService.insert(client);
        assertThat(created, notNullValue());

        OAuthClientVO returned = clientService.getByOAuthID(client.getOauthId());
        assertThat(returned, notNullValue());
        assertThat(returned.getId(), equalTo(created.getId()));
        assertThat(returned.getName(), equalTo(created.getName()));
    }

    @Test
    public void should_return_client_by_oauth_id_and_secret() throws Exception {
        OAuthClientVO client = new OAuthClientVO();
        client.setName(RandomStringUtils.randomAlphabetic(10));
        client.setOauthId(RandomStringUtils.randomAlphabetic(10));
        client.setDomain(RandomStringUtils.randomAlphabetic(10));
        client.setRedirectUri(RandomStringUtils.randomAlphabetic(10));
        OAuthClientVO created = clientService.insert(client);
        assertThat(created, notNullValue());

        OAuthClientVO returned = clientService.authenticate(client.getOauthId(), client.getOauthSecret());
        assertThat(returned, notNullValue());
        assertThat(returned.getId(), equalTo(created.getId()));
        assertThat(returned.getName(), equalTo(created.getName()));
    }

    @Test
    public void should_return_client_by_name() throws Exception {
        OAuthClientVO client = new OAuthClientVO();
        client.setName(RandomStringUtils.randomAlphabetic(10));
        client.setOauthId(RandomStringUtils.randomAlphabetic(10));
        client.setDomain(RandomStringUtils.randomAlphabetic(10));
        client.setRedirectUri(RandomStringUtils.randomAlphabetic(10));
        OAuthClientVO created = clientService.insert(client);
        assertThat(created, notNullValue());

        OAuthClientVO returned = clientService.getByName(client.getName());
        assertThat(returned, notNullValue());
        assertThat(returned.getId(), equalTo(created.getId()));
        assertThat(returned.getName(), equalTo(created.getName()));
    }

}
