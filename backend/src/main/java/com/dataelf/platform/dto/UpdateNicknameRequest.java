package com.dataelf.platform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNicknameRequest {
    
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;
}
