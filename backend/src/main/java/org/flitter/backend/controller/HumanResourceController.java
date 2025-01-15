package org.flitter.backend.controller;

import org.flitter.backend.dto.ProjectIdDTO;
import org.flitter.backend.dto.UserSearchRequestDTO;
import org.flitter.backend.service.HumanResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class HumanResourceController {
    private final HumanResourceService humanresourceService;

    public HumanResourceController(@Autowired HumanResourceService humanresourceService) {
        this.humanresourceService = humanresourceService;
    }


    @GetMapping("/all")
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(humanresourceService.fetchAllUsersIdNameWithLimit1000());
    }

    @PostMapping("/search")
    public ResponseEntity<?> findByName(@RequestBody UserSearchRequestDTO usdto) {
        if (usdto.getUsername() == null || usdto.getUsername().isEmpty()) {
            return ResponseEntity.badRequest().body("查询名称不能为空");
        }
        return ResponseEntity.ok(humanresourceService.searchUserIdNameLimit1000(usdto.getUsername()));
    }

    @PostMapping("/allbyproject")
    public ResponseEntity<?> findAllByProject(@RequestBody ProjectIdDTO projectIdDTO) {
        if (projectIdDTO.getId() == null) {
            return ResponseEntity.badRequest().body("查询项目id不能为空");
        }
        try {
            return ResponseEntity.ok(humanresourceService.fetchALlUserWhichParticipatedIn(projectIdDTO.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/role/all")
    @PreAuthorize("hasAuthority('role:modify')")
    public ResponseEntity<?> allUserRole(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(humanresourceService.fetchAllUsersIdRole(page - 1, size));
    }

    @GetMapping("/role/search")
    @PreAuthorize("hasAuthority('role:modify')")
    public ResponseEntity<?> searchUserRole(@RequestParam int page, @RequestParam int size,
                                            @RequestParam String username) {
        return ResponseEntity.ok(humanresourceService.searchUserRole(page - 1, size, username));
    }

    @PostMapping("/role/update")
    @PreAuthorize("hasAuthority('role:modify')")
    public ResponseEntity<?> updateRole(@RequestParam Long userId, @RequestParam Long roleId) {
        try {
            humanresourceService.updateUserRole(userId, roleId);
            return ResponseEntity.ok("角色更新成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/role/available")
    @PreAuthorize("hasAuthority('role:modify')")
    public ResponseEntity<?> availableRole() {
        return ResponseEntity.ok(humanresourceService.fetchAllRoles());
    }
}