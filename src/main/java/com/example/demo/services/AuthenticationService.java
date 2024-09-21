package com.example.demo.services;

import com.example.demo.dtos.LoginUserDto;
import com.example.demo.dtos.RegisterUserDto;
import com.example.demo.models.Role;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.UserRoleRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    public AuthenticationService(UserRepository userRepository, UserRoleRepository userRoleRepository, AuthenticationManager authenticationManager,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository= userRoleRepository;
    }

    public Usuario signup(Usuario input){
        Usuario usuario= new Usuario();
        Optional<Role> role = userRoleRepository.findById(1L);

        if (role.isPresent()){
            List<Role> roles= new ArrayList<>();
            roles.add(role.get());
            usuario.setRoles(roles);
        }


        usuario.setNombre(input.getNombre());
        usuario.setUsername(input.getUsername());
        usuario.setPassword(passwordEncoder.encode(input.getPassword()));
        usuario.setApellido(input.getApellido());
        usuario.setEmail(input.getEmail());

        return  userRepository.save(usuario);

    }

    public Usuario authenticate(LoginUserDto input){
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(input.getEmail(),input.getPassword()));

        return userRepository.findByEmail(input.getEmail()).orElseThrow();
    }






}
