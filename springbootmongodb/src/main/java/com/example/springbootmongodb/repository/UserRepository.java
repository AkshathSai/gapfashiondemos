package com.example.springbootmongodb.repository;

import com.example.springbootmongodb.entity.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface UserRepository extends MongoRepository<Users, String> {

    List<Users> findByEmail(@Param("email") String email);
}
