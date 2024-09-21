package com.example.demo.controllers;

import com.example.demo.dtos.LoginUserDto;
import com.example.demo.models.Usuario;
import com.example.demo.responses.LoginResponse;
import com.example.demo.services.AuthenticationService;
import com.example.demo.services.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationService authenticationService,JwtService jwtService){
        this.authenticationService= authenticationService;
        this.jwtService= jwtService;

    }

    @PostMapping("/signup")
    public ResponseEntity<Usuario> register(@RequestBody Usuario usuario){
        Usuario registeredUser= authenticationService.signup(usuario);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto){
        Usuario authenticatedUser= authenticationService.authenticate(loginUserDto);
        String jwtToken= jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);

    }

}
