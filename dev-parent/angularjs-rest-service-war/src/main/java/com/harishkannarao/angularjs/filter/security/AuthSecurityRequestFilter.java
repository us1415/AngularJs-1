package com.harishkannarao.angularjs.filter.security;

import com.harishkannarao.angularjs.constants.Roles;
import com.harishkannarao.angularjs.model.AuthAccessElement;
import com.harishkannarao.angularjs.service.AuthService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.harishkannarao.angularjs.constants.HttpHeaderErrorCodes.FORBIDDEN_ERROR;
import static com.harishkannarao.angularjs.constants.HttpHeaderErrorCodes.UNAUTHORIZED_ERROR;
import static com.harishkannarao.angularjs.constants.HttpHeaderErrorKeys.ERROR_CODE_KEY;
import static com.harishkannarao.angularjs.constants.HttpHeaderErrorKeys.ERROR_MESSAGE_KEY;
import static com.harishkannarao.angularjs.constants.HttpHeaderErrorKeys.FORBIDDEN_KEY;

@Provider
public class AuthSecurityRequestFilter implements ContainerRequestFilter {
    // 401 - Access denied
    private static final Response ACCESS_UNAUTHORIZED = Response.status(Response.Status.UNAUTHORIZED)
            .header(ERROR_CODE_KEY.getValue(), UNAUTHORIZED_ERROR.getCode())
            .header(ERROR_MESSAGE_KEY.getValue(), UNAUTHORIZED_ERROR.getMessage())
            .build();
    private static final Response ACCESS_UNAUTHORIZED_AND_FORBIDDEN = Response.status(Response.Status.UNAUTHORIZED)
            .header(ERROR_CODE_KEY.getValue(), FORBIDDEN_ERROR.getCode())
            .header(ERROR_MESSAGE_KEY.getValue(), FORBIDDEN_ERROR.getMessage())
            .header(FORBIDDEN_KEY.getValue(), "true")
            .build();

    @Inject
    private AuthService authService;

    @Context
    private HttpServletRequest request;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        // Get AuthId and AuthToken from HTTP-Header.
        String authId = containerRequestContext.getHeaderString(AuthAccessElement.PARAM_AUTH_ID);
        String authToken = containerRequestContext.getHeaderString(AuthAccessElement.PARAM_AUTH_TOKEN);

        // Get method invoked.
        Method methodInvoked = resourceInfo.getResourceMethod();
        if (methodInvoked.isAnnotationPresent(AllowRoles.class)) {
            AllowRoles allowRolesAnnotation = methodInvoked.getAnnotation(AllowRoles.class);
            Set<Roles> rolesAllowed = new HashSet<>(Arrays.asList(allowRolesAnnotation.value()));
            if (!authService.isAuthenticated(authId, authToken)) {
                containerRequestContext.abortWith(ACCESS_UNAUTHORIZED);
            } else if (!authService.isAuthorized(authId, authToken, rolesAllowed)) {
                containerRequestContext.abortWith(ACCESS_UNAUTHORIZED_AND_FORBIDDEN);
            }
        } else if (methodInvoked.isAnnotationPresent(DenyAllRoles.class)) {
            containerRequestContext.abortWith(ACCESS_UNAUTHORIZED_AND_FORBIDDEN);
        }
    }
}
