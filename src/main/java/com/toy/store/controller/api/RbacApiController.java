package com.toy.store.controller.api;

import com.toy.store.dto.ApiResponse;
import com.toy.store.model.*;
import com.toy.store.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 後台角色權限管理 API
 */
@RestController
@RequestMapping("/api/admin/rbac")
@RequiredArgsConstructor
public class RbacApiController {

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminPermissionMapper adminPermissionMapper;
    private final PasswordEncoder passwordEncoder;

    // ==================== 管理員帳號管理 ====================

    @GetMapping("/admins")
    public ApiResponse<List<Map<String, Object>>> getAllAdmins() {
        List<AdminUser> admins = adminUserMapper.findAll();
        List<Map<String, Object>> result = admins.stream().map(this::mapAdmin).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    @PostMapping("/admins")
    public ApiResponse<Map<String, Object>> createAdmin(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String email = (String) request.get("email");
        @SuppressWarnings("unchecked")
        List<Number> roleIds = (List<Number>) request.get("roleIds");

        if (username == null || password == null) {
            return ApiResponse.error("帳號和密碼為必填");
        }

        if (adminUserMapper.findByUsername(username).isPresent()) {
            return ApiResponse.error("帳號已存在");
        }

        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(email);

        adminUserMapper.insert(admin);

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Number roleId : roleIds) {
                adminUserMapper.addRoleToAdmin(admin.getId(), roleId.longValue());
            }
        }

        return ApiResponse.ok(mapAdmin(adminUserMapper.findById(admin.getId()).orElse(admin)), "管理員建立成功");
    }

    @PutMapping("/admins/{id}")
    public ApiResponse<Map<String, Object>> updateAdmin(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        return adminUserMapper.findById(id)
                .map(admin -> {
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

                    adminUserMapper.update(admin);

                    if (roleIds != null) {
                        adminUserMapper.removeAllRolesFromAdmin(admin.getId());
                        for (Number roleId : roleIds) {
                            adminUserMapper.addRoleToAdmin(admin.getId(), roleId.longValue());
                        }
                    }

                    return ApiResponse.ok(mapAdmin(adminUserMapper.findById(admin.getId()).orElse(admin)), "更新成功");
                })
                .orElseGet(() -> ApiResponse.error("管理員不存在"));
    }

    @DeleteMapping("/admins/{id}")
    public ApiResponse<Void> deleteAdmin(@PathVariable Long id) {
        if (adminUserMapper.findById(id).isEmpty()) {
            return ApiResponse.error("管理員不存在");
        }
        adminUserMapper.removeAllRolesFromAdmin(id);
        adminUserMapper.deleteById(id);
        return ApiResponse.ok(null, "刪除成功");
    }

    // ==================== 角色管理 ====================

    @GetMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> getAllRoles() {
        List<AdminRole> roles = adminRoleMapper.findAll();
        List<Map<String, Object>> result = roles.stream().map(this::mapRole).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    @PostMapping("/roles")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<Number> permissionIds = (List<Number>) request.get("permissionIds");

        if (name == null || name.isEmpty()) {
            return ApiResponse.error("角色名稱為必填");
        }

        if (adminRoleMapper.findByName(name).isPresent()) {
            return ApiResponse.error("角色名稱已存在");
        }

        AdminRole role = new AdminRole(name);
        adminRoleMapper.insert(role);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Number permId : permissionIds) {
                adminRoleMapper.addPermissionToRole(role.getId(), permId.longValue());
            }
        }

        return ApiResponse.ok(mapRole(adminRoleMapper.findById(role.getId()).orElse(role)), "角色建立成功");
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<Map<String, Object>> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        return adminRoleMapper.findById(id)
                .map(role -> {
                    String name = (String) request.get("name");
                    @SuppressWarnings("unchecked")
                    List<Number> permissionIds = (List<Number>) request.get("permissionIds");

                    if (name != null && !name.isEmpty()) {
                        role.setName(name);
                        adminRoleMapper.update(role);
                    }

                    if (permissionIds != null) {
                        adminRoleMapper.removeAllPermissionsFromRole(role.getId());
                        for (Number permId : permissionIds) {
                            adminRoleMapper.addPermissionToRole(role.getId(), permId.longValue());
                        }
                    }

                    return ApiResponse.ok(mapRole(adminRoleMapper.findById(role.getId()).orElse(role)), "更新成功");
                })
                .orElseGet(() -> ApiResponse.error("角色不存在"));
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        if (adminRoleMapper.findById(id).isEmpty()) {
            return ApiResponse.error("角色不存在");
        }
        adminRoleMapper.removeAllPermissionsFromRole(id);
        adminRoleMapper.deleteById(id);
        return ApiResponse.ok(null, "刪除成功");
    }

    // ==================== 權限管理 ====================

    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, Object>>> getAllPermissions() {
        List<AdminPermission> perms = adminPermissionMapper.findAll();
        List<Map<String, Object>> result = perms.stream().map(this::mapPermission).collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    @PostMapping("/permissions")
    public ApiResponse<Map<String, Object>> createPermission(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String name = request.get("name");
        String description = request.get("description");

        if (code == null || name == null) {
            return ApiResponse.error("權限代碼和名稱為必填");
        }

        if (adminPermissionMapper.findByCode(code).isPresent()) {
            return ApiResponse.error("權限代碼已存在");
        }

        AdminPermission perm = new AdminPermission();
        perm.setCode(code);
        perm.setName(name);
        perm.setDescription(description);

        adminPermissionMapper.insert(perm);
        return ApiResponse.ok(mapPermission(perm), "權限建立成功");
    }

    @DeleteMapping("/permissions/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        if (adminPermissionMapper.findById(id).isEmpty()) {
            return ApiResponse.error("權限不存在");
        }
        adminPermissionMapper.deleteById(id);
        return ApiResponse.ok(null, "刪除成功");
    }

    // ==================== 輔助方法 ====================

    private Map<String, Object> mapAdmin(AdminUser admin) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", admin.getId());
        map.put("username", admin.getUsername());
        map.put("email", admin.getEmail());

        List<AdminRole> roles = adminUserMapper.findRolesByAdminId(admin.getId());
        map.put("roles", roles.stream()
                .map(r -> Map.of("id", r.getId(), "name", r.getName()))
                .collect(Collectors.toList()));

        // 權限匯總
        Set<String> permissions = new HashSet<>();
        for (AdminRole role : roles) {
            List<AdminPermission> rolePerms = adminRoleMapper.findPermissionsByRoleId(role.getId());
            for (AdminPermission p : rolePerms) {
                permissions.add(p.getCode());
            }
        }
        map.put("permissions", permissions);
        return map;
    }

    private Map<String, Object> mapRole(AdminRole role) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", role.getId());
        map.put("name", role.getName());

        List<AdminPermission> perms = adminRoleMapper.findPermissionsByRoleId(role.getId());
        map.put("permissions", perms.stream()
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
