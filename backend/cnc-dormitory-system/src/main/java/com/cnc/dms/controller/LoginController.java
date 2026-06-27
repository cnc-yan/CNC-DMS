package com.cnc.dms.controller;

import com.cnc.dms.dto.LoginRequest;
import com.cnc.dms.dto.LoginResponse;
import com.cnc.dms.dto.Result;
import com.cnc.dms.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller that handles authentication related endpoints.
 * Provides login and current user information endpoints.
 *
 * @author CNC DMS Team
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final UserService userService;

    /**
     * Constructs a new LoginController with the specified UserService.
     * <p>
     * Uses constructor-based dependency injection for the UserService.
     * </p>
     *
     * @param userService the service responsible for user authentication and management
     */
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Authenticates a user based on the provided login credentials.
     * <p>
     * This endpoint accepts a POST request with a JSON body containing
     * the username and password. It delegates the authentication logic
     * to the {@link UserService#login(LoginRequest)} method and returns
     * the authentication result wrapped in a unified {@link Result} envelope.
     * </p>
     *
     * @param request the login request containing username and password
     *                (must not be null)
     * @return a {@link Result} containing the {@link LoginResponse}
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        if (response.isSuccess()) {
            return Result.success(response);
        }
        return Result.fail(response.getMessage());
    }

    /**
     * 获取当前登录用户信息
     * 从Spring Security的SecurityContextHolder中获取认证对象
     *
     * @param authentication 当前认证对象（由Spring Security自动注入，可能为null）
     * @return 包含用户名的统一响应（未认证时返回null数据）
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(Authentication authentication) {
        // 未认证（无有效Token或Token已过期）
        if (authentication == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", null);
            return Result.fail("未登录或Token已过期");
        }

        // 获取用户名（通常是userid）
        String username = authentication.getName();

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        return Result.success(data);
    }
}
