package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User findById(@Param("id") Long id);

    User findByName(@Param("name") String name);

    User findByEmail(@Param("email") String email);

    int insert(User user);

    List<User> findUsers(@Param("role") String role,
                         @Param("status") String status,
                         @Param("offset") int offset,
                         @Param("pageSize") int pageSize);

    long countUsers(@Param("role") String role, @Param("status") String status);

    int updateUser(User user);
}
