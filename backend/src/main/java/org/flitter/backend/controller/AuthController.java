package org.flitter.backend.controller;

import org.flitter.backend.entity.User;
import org.flitter.backend.repository.UserRepository;
import org.flitter.backend.service.UserService;
import org.flitter.backend.utils.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthController(UserService userService,
                          JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 注册接口
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                    "输入用户名或者密码不能为空"
            );
        }
        try {
            User u = userService.authenticateUser(loginRequest);

            List<String> permissions = userService.getPermissions(u);

            String accessToken = jwtTokenProvider.generateAccessToken(u.getUsername(), permissions);
            String refreshToken = jwtTokenProvider.generateRefreshToken(u.getUsername());

            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", accessToken);
            tokens.put("refresh_token", refreshToken);

            return ResponseEntity.ok(tokens);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("用户名或者密码不正确");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String>  refreshRequest) {
        String refreshToken = refreshRequest.get("refresh_token");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("refresh_token 不能为空");
        }

        if (!jwtTokenProvider.validateToken(refreshToken, false)) {
            return ResponseEntity.status(401).body("无效的refresh_token");
        }
        String username = jwtTokenProvider.getUsernameFromJwt(refreshToken, false);
        String newAccessToken;
        try{
             newAccessToken = userService.generateNewAccessKey(username);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        Map<String, String> response = new HashMap<>();
        response.put("access_token", newAccessToken);
        return ResponseEntity.ok(response);
    }
}


