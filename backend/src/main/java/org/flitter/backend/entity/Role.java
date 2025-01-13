package org.flitter.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.flitter.backend.entity.enums.Permission;

import java.util.Set;

@Data
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ElementCollection(targetClass = Permission.class,
            fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"))
    @Enumerated(EnumType.STRING)
    @Column(name="permission")
    private Set<Permission> permissions;

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;  // 使用id计算hashCode
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;  // 如果引用相同返回true
        if (obj == null || getClass() != obj.getClass()) return false;  // 类型检查
        Role role = (Role) obj;
        return id != null && id.equals(role.id);
    }
}
