package com.example.demo;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5174")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> user) {

        String email = user.get("email");
        String password = user.get("password");

        if ("test@gmail.com".equals(email) && "123456".equals(password)) {
            return "Login Success";
        } else {
            return "Invalid Credentials";
        }
    }
}