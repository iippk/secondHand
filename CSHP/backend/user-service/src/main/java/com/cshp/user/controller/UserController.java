package com.cshp.user.controller;

import com.cshp.common.result.Result;
import com.cshp.user.dto.ChangePasswordDTO;
import com.cshp.user.dto.LoginDTO;
import com.cshp.user.dto.RegisterDTO;
import com.cshp.user.dto.UserDTO;
import com.cshp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserDTO result = userService.register(registerDTO);
        return Result.success(result);
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success("登录成功", token);
    }

    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(@RequestHeader("X-Student-Id") String studentId) {
        UserDTO userDTO = userService.getUserInfo(studentId);
        return Result.success("获取用户信息成功", userDTO);
    }




    @PostMapping("/change-password")
    public Result<?> changePassword(@RequestHeader("X-Student-Id") String studentId,
                                     @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(studentId, changePasswordDTO);
        return Result.success("密码修改成功");
    }

    @PutMapping("/info")
    public Result<UserDTO> updateUserInfo(@RequestHeader("X-Student-Id") String studentId,
                                           @RequestBody UserDTO userDTO) {
        UserDTO result = userService.updateUserInfo(studentId, userDTO);
        return Result.success(result);
    }
}

