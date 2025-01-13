package org.flitter.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class UserIDRoleDTO {
    private Long id;
    private String username;
    private String roleName;
}
