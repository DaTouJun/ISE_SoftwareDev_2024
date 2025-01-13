package org.flitter.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;
import java.util.HashSet;


@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 登录账号
    @Column(nullable = false, unique = true)
    private String username;

    // 密码 加密存储
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    // 所属项目
    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

    // 被指定的任务
    @ManyToMany(mappedBy = "assignees")
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;  // 使用id计算hashCode
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;  // 如果引用相同返回true
        if (obj == null || getClass() != obj.getClass()) return false;  // 类型检查
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }
}
