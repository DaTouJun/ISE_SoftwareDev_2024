package org.flitter.backend.repository;

import org.flitter.backend.dto.UserIDRoleDTO;
import org.flitter.backend.entity.Project;
import org.flitter.backend.entity.User;

import org.flitter.backend.dto.UserNameIdDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);

    List<UserNameIdDTO> findAllByProjectsContains(Project project);

    @Query("SELECT new org.flitter.backend.dto.UserNameIdDTO(p.id, p.username) FROM User p " +
            "WHERE p.id <> 1")
    List<UserNameIdDTO> findAllUserNameId(Pageable pageable);


    @Query("SELECT new org.flitter.backend.dto.UserNameIdDTO(p.id, p.username) FROM User p " +
            "WHERE p.username like :name and p.id <> 1")
    List<UserNameIdDTO> searchUserByUsernameLike(String name, Pageable pageable);

    @Query(value = "SELECT u.id AS id, u.username AS username, r.name AS roleName " +
            "FROM user u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN role r ON ur.role_id = r.id " +
            "WHERE u.id <> 1 ORDER BY u.id", nativeQuery = true)
    Page<UserIDRoleDTO> findAllUserIDRole(Pageable pageable);

    @Query(value = "SELECT u.id AS id, u.username AS username, r.name AS roleName " +
            "FROM user u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN role r ON ur.role_id = r.id " +
            "WHERE u.id <> 1 " +
            "AND u.username LIKE CONCAT('%', :username, '%') ORDER BY u.id",
            countQuery = "SELECT COUNT(*) " +
                    "FROM user u " +
                    "JOIN user_roles ur ON u.id = ur.user_id " +
                    "JOIN role r ON ur.role_id = r.id " +
                    "WHERE u.id <> 1 " +
                    "AND u.username LIKE CONCAT('%', :username, '%')",
            nativeQuery = true)
    Page<UserIDRoleDTO> findAllUserIDRole(Pageable pageable, @Param("username") String username);

}
