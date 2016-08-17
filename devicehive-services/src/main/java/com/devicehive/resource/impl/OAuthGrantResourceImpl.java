package com.devicehive.resource.impl;

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


import com.devicehive.auth.HivePrincipal;
import com.devicehive.configuration.Constants;
import com.devicehive.configuration.Messages;
import com.devicehive.exceptions.HiveException;
import com.devicehive.model.ErrorResponse;
import com.devicehive.model.enums.AccessType;
import com.devicehive.model.enums.Type;
import com.devicehive.model.updates.OAuthGrantUpdate;
import com.devicehive.resource.OAuthGrantResource;
import com.devicehive.resource.converters.SortOrderQueryParamParser;
import com.devicehive.resource.converters.TimestampQueryParamParser;
import com.devicehive.resource.util.ResponseFactory;
import com.devicehive.service.OAuthGrantService;
import com.devicehive.service.UserService;
import com.devicehive.vo.OAuthGrantVO;
import com.devicehive.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

import static com.devicehive.configuration.Constants.TIMESTAMP;
import static com.devicehive.json.strategies.JsonPolicyDef.Policy.*;
import static javax.ws.rs.core.Response.Status.*;

@Service
public class OAuthGrantResourceImpl implements OAuthGrantResource {

    private static final Logger logger = LoggerFactory.getLogger(OAuthGrantResourceImpl.class);

    @Autowired
    private OAuthGrantService grantService;

    @Autowired
    private UserService userService;

    @Override
    public Response list(String userId, String startTs, String endTs, String clientOAuthId, String type, String scope, String redirectUri, String accessType,
                         String sortField, String sortOrderSt, Integer take, Integer skip) {

        Date start = TimestampQueryParamParser.parse(startTs);
        Date end = TimestampQueryParamParser.parse(endTs);

        boolean sortOrder = SortOrderQueryParamParser.parse(sortOrderSt);

        if (!sortField.equalsIgnoreCase(TIMESTAMP)) {
            return ResponseFactory.response(BAD_REQUEST,
                                            new ErrorResponse(BAD_REQUEST.getStatusCode(),
                                                              Messages.INVALID_REQUEST_PARAMETERS));
        } else {
            sortField = sortField.toLowerCase();
        }
        UserVO user = getUser(userId);
        List<OAuthGrantVO> result = grantService.list(user, start, end, clientOAuthId,
                                                    type == null ? null : Type.forName(type).ordinal(), scope,
                                                    redirectUri, accessType == null ? null
                                                                                    : AccessType.forName(accessType)
                                                                     .ordinal(), sortField,
                                                    sortOrder, take, skip);
        logger.debug(
                "OAuthGrant: list proceed successfully. User id: {}, start: {}, end: {}, clientOAuthID: {}, " +
                        "type: {}, scope: {}, redirectURI: {}, accessType: {}, sortField: {}, sortOrder: {}, take: {}, skip: {}",
                userId, start, end, clientOAuthId, type, scope, redirectUri, accessType, sortField, sortOrder, take,
                skip);
        if (user.isAdmin()) {
            return ResponseFactory.response(OK, result, OAUTH_GRANT_LISTED_ADMIN);
        }
        return ResponseFactory.response(OK, result, OAUTH_GRANT_LISTED);
    }

    @Override
    public Response get(String userId, long grantId) {
        logger.debug("OAuthGrant: get requested. User id: {}, grant id: {}", userId, grantId);
        UserVO user = getUser(userId);
        OAuthGrantVO grant = grantService.get(user, grantId);
        if (grant == null) {
            throw new HiveException(String.format(Messages.GRANT_NOT_FOUND, grantId), NOT_FOUND.getStatusCode());
        }
        logger.debug("OAuthGrant: proceed successfully. User id: {}, grant id: {}", userId, grantId);
        if (user.isAdmin()) {
            return ResponseFactory.response(OK, grant, OAUTH_GRANT_LISTED_ADMIN);
        }
        return ResponseFactory.response(OK, grant, OAUTH_GRANT_LISTED);
    }

    @Override
    public Response insert(String userId, OAuthGrantVO grant) {
        logger.debug("OAuthGrant: insert requested. User id: {}, grant: {}", userId, grant);
        UserVO user = getUser(userId);
        grantService.save(grant, user);
        logger.debug("OAuthGrant: insert proceed successfully. User id: {}, grant: {}", userId, grant);
        if (grant.getType().equals(Type.TOKEN)) {
            return ResponseFactory.response(CREATED, grant, OAUTH_GRANT_SUBMITTED_TOKEN);
        } else {
            return ResponseFactory.response(CREATED, grant, OAUTH_GRANT_SUBMITTED_CODE);
        }
    }

    @Override
    public Response update(String userId, Long grantId, OAuthGrantUpdate grant) {
        logger.debug("OAuthGrant: update requested. User id: {}, grant id: {}", userId, grantId);
        UserVO user = getUser(userId);
        OAuthGrantVO updated = grantService.update(user, grantId, grant);
        if (updated == null) {
            throw new HiveException(String.format(Messages.GRANT_NOT_FOUND, grantId), NOT_FOUND.getStatusCode());
        }
        logger.debug("OAuthGrant: update proceed successfully. User id: {}, grant id: {}", userId, grantId);
        if (updated.getType().equals(Type.TOKEN)) {
            return ResponseFactory.response(OK, updated, OAUTH_GRANT_SUBMITTED_TOKEN);
        } else {
            return ResponseFactory.response(OK, updated, OAUTH_GRANT_SUBMITTED_CODE);
        }
    }

    @Override
    public Response delete(String userId, Long grantId) {
        logger.debug("OAuthGrant: delete requested. User id: {}, grant id: {}", userId, grantId);
        UserVO user = getUser(userId);
        grantService.delete(user, grantId);
        logger.debug("OAuthGrant: delete proceed successfully. User id: {}, grant id: {}", userId, grantId);
        return ResponseFactory.response(NO_CONTENT);
    }

    private UserVO getUser(String userId) {
        HivePrincipal principal = (HivePrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserVO current = principal.getUser() != null ? principal.getUser() : principal.getKey().getUser();
        if (userId.equalsIgnoreCase(Constants.CURRENT_USER)) {
            return current;
        }
        if (StringUtils.isNumeric(userId)) {
            Long id = Long.parseLong(userId);
            if (current.getId().equals(id)) {
                return current;
            } else if (current.isAdmin()) {
                UserVO result = userService.findById(id);
                if (result == null) {
                    logger.error("OAuthGrant: user with id {} not found", id);
                    throw new HiveException(Messages.USER_NOT_FOUND, NOT_FOUND.getStatusCode());
                }
                return result;
            }
            throw new HiveException(Messages.UNAUTHORIZED_REASON_PHRASE, UNAUTHORIZED.getStatusCode());
        }
        throw new HiveException(String.format(Messages.BAD_USER_IDENTIFIER, userId), BAD_REQUEST.getStatusCode());
    }

}