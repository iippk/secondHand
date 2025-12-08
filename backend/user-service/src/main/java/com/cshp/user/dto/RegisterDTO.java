package com.cshp.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RegisterDTO {

    @NotBlank(message = "学工号不能为空")
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 默认使用身份证后六位，注册时也可以自定义初始密码
     */
    @NotBlank(message = "初始密码不能为空")
    private String password;

    private String phone;
    private String email;
    private String avatar;
}


