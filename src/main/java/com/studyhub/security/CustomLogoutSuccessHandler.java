package com.studyhub.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    public CustomLogoutSuccessHandler() {
        setDefaultTargetUrl("/login");
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        FlashMap flashMap = new FlashMap();
        flashMap.put("successMessage", "You have been logged out successfully.");
        new SessionFlashMapManager().saveOutputFlashMap(flashMap, request, response);

        super.onLogoutSuccess(request, response, authentication);
    }
}
