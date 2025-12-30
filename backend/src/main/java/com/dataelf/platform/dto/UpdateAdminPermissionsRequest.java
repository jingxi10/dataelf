package com.dataelf.platform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminPermissionsRequest {
    
    @NotNull(message = "权限列表不能为空")
    private List<String> permissions; // 权限菜单列表
}
