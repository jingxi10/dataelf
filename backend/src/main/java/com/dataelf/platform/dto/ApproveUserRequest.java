package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveUserRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @Min(value = 1, message = "有效天数必须大于0")
    private Integer validDays;
}
