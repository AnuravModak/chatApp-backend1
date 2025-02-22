package com.demo.chatApp.controllers;

import com.demo.chatApp.entities.LogoutRequest;
import com.demo.chatApp.services.UserService;
import com.demo.chatApp.entities.LoginRequest;
import com.demo.chatApp.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin/all/users")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users= userService.findAllUser();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("admin/user/{userId}")
    public ResponseEntity<Optional<User>> getUserById(@PathVariable String userId){
        Optional<User> user=userService.findByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }


    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user){
        User savedUser= userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);

    }

    @PostMapping("/login")
    public ResponseEntity<String> login (@RequestBody LoginRequest loginRequest){
        String token= userService.loginUser(loginRequest.getUsername(),loginRequest.getPassword());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest logoutRequest) {
        userService.logoutUser(logoutRequest.getUsername());
        return ResponseEntity.ok("User logged out successfully.");
    }


}
