package com.cshp.user.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank(message = "学工号不能为空")
    private String studentId;

    @NotBlank(message = "密码不能为空")
    private String password;
}

