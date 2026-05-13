package com.erp.mainmodule.service;

import com.erp.mainmodule.entity.User;
import com.erp.mainmodule.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    // GET ALL USERS
    public List<User> getAll() {
        return repository.findAll();
    }

    // CREATE USER
    public User create(User user) {
        return repository.save(user);
    }

    // GET USER BY ID
    public User getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    // DELETE USER
    public void delete(Integer id) {
        repository.deleteById(id);
    }


    public User update(Integer id, User updated) {
        User existing = repository.findById(id).orElse(null);

        if (existing == null) {
            throw new RuntimeException("User not found");
        }

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setPasswordHash(updated.getPasswordHash());
        existing.setCompany(updated.getCompany());
        existing.setRoleId(updated.getRoleId());

        return repository.save(existing);
    }
}