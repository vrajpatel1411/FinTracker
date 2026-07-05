package org.vrajpatel.fintrackergateway.Config.RoutesValidator;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class AuthServiceRouteValidator {

    private static final List<String> openApiEndpoints= List.of(
            "/userauthservice/api/auth",
            "/oauth2/"
    );

    public Predicate<ServerHttpRequest> isSecured= request-> openApiEndpoints.stream().noneMatch(uri->request.getURI().getPath().startsWith(uri));

}
