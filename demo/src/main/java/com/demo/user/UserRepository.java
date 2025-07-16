package com.demo.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByUserNameContains(String userName);

    List<User> findByUserNameContainsOrderByUserNameAsc(String userName);

    //select * from users where userName = '' and lastName = ''
    List<User> findByUserNameAndEmailId(String userName, String email);

    // HQL Query (Entity & It's property names)
    @Query("from User where userName = :userName and emailId = :emailId")
    // SQL Query (table name & it's column names)
    //@Query(value = "select * from users where user_name = :userName and email_id = :emailId", nativeQuery = true)
    List<User> getUsersByUserNameEmailId(String userName, String emailId);

    //select * from users where userName = '' or lastName = ''
    List<User> findByUserNameOrEmailId(String userName, String email);

    //select * from users where userName like '% %' or lastName like '% %'
    List<User> findByUserNameContainsOrEmailIdContains(String userName, String emailId);

    List<User> findByUserNameContains(String userName, Pageable page);

    // Age-related query methods
    List<User> findByAge(Integer age);
    List<User> findByAgeGreaterThan(Integer age);
    List<User> findByAgeLessThan(Integer age);
    List<User> findByAgeBetween(Integer minAge, Integer maxAge);
}
