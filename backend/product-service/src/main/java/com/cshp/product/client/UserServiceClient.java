package com.cshp.product.client;

import com.cshp.user.dto.UserDTO;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/user")
public interface UserServiceClient {

    @GetMapping("/{studentId}")
    UserDTO getUserById(@PathVariable("studentId") String studentId);
}

