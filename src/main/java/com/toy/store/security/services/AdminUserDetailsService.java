package com.toy.store.security.services;

import com.toy.store.model.AdminUser;
import com.toy.store.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    @Autowired
    AdminUserRepository adminUserRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            AdminUser user = adminUserRepository.findByUsername(username)
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
