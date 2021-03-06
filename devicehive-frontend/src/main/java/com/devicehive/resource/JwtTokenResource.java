package com.devicehive.resource;

/*
 * #%L
 * DeviceHive Frontend Logic
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

import com.devicehive.security.jwt.JwtPayload;
import com.devicehive.vo.JwtTokenVO;
import io.swagger.annotations.*;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * REST controller for JwtToken.
 */
@Path("/token")
@Api(tags = {"JwtToken"}, description = "Represents an JWT access/refresh tokens management to API/device.",
        consumes = "application/json")
@Produces({"application/json"})
public interface JwtTokenResource {

    @POST
    @Consumes(APPLICATION_JSON)
    @PreAuthorize("isAuthenticated() and hasPermission(null, 'MANAGE_TOKEN')")
    @ApiOperation(value = "JWT access and refresh token request")
    @ApiResponses({
            @ApiResponse(code = 201,
                    message = "If successful, this method returns a JWT access and refresh token in the response body.",
                    response = JwtTokenVO.class),
            @ApiResponse(code = 404, message = "If access token not found")
    })
    Response tokenRequest(
            @ApiParam(name = "payload", value = "Payload", required = true)
            JwtPayload payload);

    @POST
    @Path("/refresh")
    @Consumes(APPLICATION_JSON)
    @PreAuthorize("permitAll")
    @ApiOperation(value = "JWT access token request with refresh token")
    @ApiResponses({
            @ApiResponse(code = 201,
                    message = "If successful, this method returns a JWT access token in the response body.",
                    response = JwtTokenVO.class),
            @ApiResponse(code = 404, message = "If access token not found")
    })
    Response refreshTokenRequest(
            @ApiParam(name = "refreshToken", value = "Refresh token", required = true)
            JwtTokenVO jwtTokenVO);
}

