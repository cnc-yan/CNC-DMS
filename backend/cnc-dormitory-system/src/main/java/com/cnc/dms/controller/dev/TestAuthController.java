package com.cnc.dms.controller.dev;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認証テスト用 Controller（dev/qa 環境のみ有効）
 */
@RestController
@Profile({"dev", "qa"})
public class TestAuthController {

    @GetMapping("/api/test/auth")
    public String authTest() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "User is not authenticated";
        }

        return "Login User : "
                + authentication.getName();
    }
}
