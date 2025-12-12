package com.cshp.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "学工号不能为空")
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String phone;
    private String email;
    private String avatar;
}

