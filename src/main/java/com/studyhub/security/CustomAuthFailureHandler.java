package com.studyhub.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String message;
        String redirectUrl = "/login";

        if (exception instanceof DisabledException) {
            message = "Your email has not been verified yet.";
            redirectUrl = "/login?unverified";
        } else if (exception instanceof LockedException) {
            message = "Your account has been deactivated. Please contact support.";
        } else {
            message = "Invalid email/username or password.";
        }

        FlashMap flashMap = new FlashMap();
        flashMap.put("errorMessage", message);
        new SessionFlashMapManager().saveOutputFlashMap(flashMap, request, response);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
