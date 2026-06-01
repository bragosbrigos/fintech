package com.fintech.admin.feign;

import com.fintech.platform.common.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service", url = "${feign.client.user-service.url}")
public interface UserClient {
    
    @GetMapping("/api/users")
    List<UserDTO> getAllUsers();
    
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") String id);
    
    @GetMapping("/api/users/count")
    Long getTotalUserCount();
}
