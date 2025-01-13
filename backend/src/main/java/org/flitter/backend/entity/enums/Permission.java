package org.flitter.backend.entity.enums;

import lombok.Getter;

@Getter
public enum Permission {
    PROJECT_CREATE("project:create"),
    PROJECT_READ("project:read"),
    PROJECT_WRITE("project:write"),

    TASK_CREATE("task:create"),
    TASK_READ("task:read"),
    TASK_WRITE("task:write"),

    ROLE_MODIFY("role:modify"),;

    private final String permission;

    Permission(String s) {
        this.permission = s;
    }

    public static Permission fromString(String s) {
        for (Permission p : Permission.values()) {
            if (p.getPermission().equals(s)) {
                return p;
            }
        }
        throw new IllegalArgumentException("No enum constant " + s);
    }
}
