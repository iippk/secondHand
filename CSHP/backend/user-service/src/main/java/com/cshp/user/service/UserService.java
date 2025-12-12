package com.cshp.user.service;

import com.cshp.user.dto.ChangePasswordDTO;
import com.cshp.user.dto.LoginDTO;
import com.cshp.user.dto.RegisterDTO;
import com.cshp.user.dto.UserDTO;

public interface UserService {
    UserDTO register(RegisterDTO registerDTO);
    String login(LoginDTO loginDTO);
    UserDTO getUserInfo(String studentId);
    void changePassword(String studentId, ChangePasswordDTO changePasswordDTO);
    UserDTO updateUserInfo(String studentId, UserDTO userDTO);
}

