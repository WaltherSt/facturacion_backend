package com.example.demo.repositories;

import com.example.demo.models.Role;
import org.springframework.data.repository.CrudRepository;

public interface UserRoleRepository extends CrudRepository<Role,Long> {




}
