package com.example.springbootmongodb.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class Users implements Serializable {

    @Id
    private String id;  // MongoDB uses String IDs by default
    private String name;
    private String email;
}
