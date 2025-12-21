package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    
    @NotNull(message = "内容ID不能为空")
    private Long contentId;
    
    @NotBlank(message = "评论内容不能为空")
    private String commentText;
}
