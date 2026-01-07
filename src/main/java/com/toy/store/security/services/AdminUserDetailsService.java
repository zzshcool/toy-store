package com.toy.store.security.services;

import com.toy.store.model.AdminUser;
import com.toy.store.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserMapper adminUserMapper;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            AdminUser user = adminUserMapper.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin Not Found with username: " + username));
            return AdminUserDetails.build(user);
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UsernameNotFoundException("System Error loading admin: " + username);
        }
    }
}
