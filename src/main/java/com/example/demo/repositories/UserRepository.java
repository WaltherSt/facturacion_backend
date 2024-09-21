package com.example.demo.repositories;
import com.example.demo.models.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
}