package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.*;
import com.toy.store.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 後台角色權限管理 API
 */
@RestController
@RequestMapping("/api/admin/rbac")
public class RbacApiController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminRoleRepository roleRepository;

    @Autowired
    private AdminPermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 管理員帳號管理 ====================

    /**
     * 獲取所有管理員
     */
    @GetMapping("/admins")
    public ApiResponse<List<Map<String, Object>>> getAllAdmins() {
        List<AdminUser> admins = adminUserRepository.findAll();
        List<Map<String, Object>> result = admins.stream().map(this::mapAdmin).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 新增管理員
     */
    @PostMapping("/admins")
    public ApiResponse<Map<String, Object>> createAdmin(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String email = (String) request.get("email");
        @SuppressWarnings("unchecked")
        List<Long> roleIds = (List<Long>) request.get("roleIds");

        if (username == null || password == null) {
            return ApiResponse.error("帳號和密碼為必填");
        }

        if (adminUserRepository.findByUsername(username).isPresent()) {
            return ApiResponse.error("帳號已存在");
        }

        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(email);

        if (roleIds != null && !roleIds.isEmpty()) {
            Set<AdminRole> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            admin.setRoles(roles);
        }

        adminUserRepository.save(admin);
        return ApiResponse.ok(mapAdmin(admin), "管理員建立成功");
    }

    /**
     * 更新管理員
     */
    @PutMapping("/admins/{id}")
    public ApiResponse<Map<String, Object>> updateAdmin(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        AdminUser admin = adminUserRepository.findById(id).orElse(null);
        if (admin == null) {
            return ApiResponse.error("管理員不存在");
        }

        String email = (String) request.get("email");
        String password = (String) request.get("password");
        @SuppressWarnings("unchecked")
        List<Number> roleIds = (List<Number>) request.get("roleIds");

        if (email != null) {
            admin.setEmail(email);
        }
        if (password != null && !password.isEmpty()) {
            admin.setPassword(passwordEncoder.encode(password));
        }
        if (roleIds != null) {
            List<Long> ids = roleIds.stream().map(Number::longValue).collect(Collectors.toList());
            Set<AdminRole> roles = new HashSet<>(roleRepository.findAllById(ids));
            admin.setRoles(roles);
        }

        adminUserRepository.save(admin);
        return ApiResponse.ok(mapAdmin(admin), "更新成功");
    }

    /**
     * 刪除管理員
     */
    @DeleteMapping("/admins/{id}")
    public ApiResponse<Void> deleteAdmin(@PathVariable Long id) {
        if (!adminUserRepository.existsById(id)) {
            return ApiResponse.error("管理員不存在");
        }
        adminUserRepository.deleteById(id);
        return ApiResponse.ok(null, "刪除成功");
    }

    // ==================== 角色管理 ====================

    /**
     * 獲取所有角色
     */
    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> getAllRoles() {
        List<AdminRole> roles = roleRepository.findAll();
        List<Map<String, Object>> result = roles.stream().map(this::mapRole).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 新增角色
     */
    @PostMapping("/roles")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<Number> permissionIds = (List<Number>) request.get("permissionIds");

        if (name == null || name.isEmpty()) {
            return ApiResponse.error("角色名稱為必填");
        }

        if (roleRepository.findByName(name).isPresent()) {
            return ApiResponse.error("角色名稱已存在");
        }

        AdminRole role = new AdminRole(name);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Long> ids = permissionIds.stream().map(Number::longValue).collect(Collectors.toList());
            Set<AdminPermission> perms = new HashSet<>(permissionRepository.findAllById(ids));
            role.setPermissions(perms);
        }

        roleRepository.save(role);
        return ApiResponse.ok(mapRole(role), "角色建立成功");
    }

    /**
     * 更新角色
     */
    @PutMapping("/roles/{id}")
    public ApiResponse<Map<String, Object>> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        AdminRole role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            return ApiResponse.error("角色不存在");
        }

        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<Number> permissionIds = (List<Number>) request.get("permissionIds");

        if (name != null && !name.isEmpty()) {
            role.setName(name);
        }
        if (permissionIds != null) {
            List<Long> ids = permissionIds.stream().map(Number::longValue).collect(Collectors.toList());
            Set<AdminPermission> perms = new HashSet<>(permissionRepository.findAllById(ids));
            role.setPermissions(perms);
        }

        roleRepository.save(role);
        return ApiResponse.ok(mapRole(role), "更新成功");
    }

    /**
     * 刪除角色
     */
    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        if (!roleRepository.existsById(id)) {
            return ApiResponse.error("角色不存在");
        }
        roleRepository.deleteById(id);
        return ApiResponse.ok(null, "刪除成功");
    }

    // ==================== 權限管理 ====================

    /**
     * 獲取所有權限
     */
    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, Object>>> getAllPermissions() {
        List<AdminPermission> perms = permissionRepository.findAll();
        List<Map<String, Object>> result = perms.stream().map(this::mapPermission).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    /**
     * 新增權限
     */
    @PostMapping("/permissions")
    public ApiResponse<Map<String, Object>> createPermission(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String name = request.get("name");
        String description = request.get("description");

        if (code == null || name == null) {
            return ApiResponse.error("權限代碼和名稱為必填");
        }

        if (permissionRepository.findByCode(code).isPresent()) {
            return ApiResponse.error("權限代碼已存在");
        }

        AdminPermission perm = new AdminPermission();
        perm.setCode(code);
        perm.setName(name);
        perm.setDescription(description);

        permissionRepository.save(perm);
        return ApiResponse.ok(mapPermission(perm), "權限建立成功");
    }

    /**
     * 刪除權限
     */
    @DeleteMapping("/permissions/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        if (!permissionRepository.existsById(id)) {
            return ApiResponse.error("權限不存在");
        }
        permissionRepository.deleteById(id);
        return ApiResponse.ok(null, "刪除成功");
    }

    // ==================== 輔助方法 ====================

    private Map<String, Object> mapAdmin(AdminUser admin) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", admin.getId());
        map.put("username", admin.getUsername());
        map.put("email", admin.getEmail());
        map.put("roles", admin.getRoles().stream()
                .map(r -> Map.of("id", r.getId(), "name", r.getName()))
                .collect(Collectors.toList()));
        map.put("permissions", admin.getPermissions());
        return map;
    }

    private Map<String, Object> mapRole(AdminRole role) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", role.getId());
        map.put("name", role.getName());
        map.put("permissions", role.getPermissions().stream()
                .map(p -> Map.of("id", p.getId(), "code", p.getCode(), "name", p.getName()))
                .collect(Collectors.toList()));
        return map;
    }

    private Map<String, Object> mapPermission(AdminPermission perm) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", perm.getId());
        map.put("code", perm.getCode());
        map.put("name", perm.getName());
        map.put("description", perm.getDescription());
        return map;
    }
}
