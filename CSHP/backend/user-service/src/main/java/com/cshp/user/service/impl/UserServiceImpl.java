package com.cshp.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cshp.common.exception.BusinessException;
import com.cshp.common.util.JwtUtil;
import com.cshp.user.dto.ChangePasswordDTO;
import com.cshp.user.dto.LoginDTO;
import com.cshp.user.dto.RegisterDTO;
import com.cshp.user.dto.UserDTO;
import com.cshp.user.entity.User;
import com.cshp.user.mapper.UserMapper;
import com.cshp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final JwtUtil jwtUtil;
    @Qualifier("webPasswordEncoder")
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO register(RegisterDTO registerDTO) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", registerDTO.getStudentId());
        User exists = userMapper.selectOne(wrapper);
        if (exists != null) {
            throw new BusinessException(400, "学工号已注册");
        }

        User user = new User();
        user.setStudentId(registerDTO.getStudentId());
        user.setName(registerDTO.getName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setAvatar(registerDTO.getAvatar());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        userMapper.insert(user);

        UserDTO result = new UserDTO();
        BeanUtils.copyProperties(user, result);
        return result;
    }

    @Override
    public String login(LoginDTO loginDTO) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", loginDTO.getStudentId());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(401, "学工号或密码错误");
        }

        // 验证密码（身份证后六位）
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "学工号或密码错误");
        }

        return jwtUtil.generateToken(user.getStudentId());
    }

    @Override
    public UserDTO getUserInfo(String studentId) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    @Override
    public void changePassword(String studentId, ChangePasswordDTO changePasswordDTO) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public UserDTO updateUserInfo(String studentId, UserDTO userDTO) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (userDTO.getName() != null) user.setName(userDTO.getName());
        if (userDTO.getPhone() != null) user.setPhone(userDTO.getPhone());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getAvatar() != null) user.setAvatar(userDTO.getAvatar());

        userMapper.updateById(user);

        UserDTO result = new UserDTO();
        BeanUtils.copyProperties(user, result);
        return result;
    }
}

