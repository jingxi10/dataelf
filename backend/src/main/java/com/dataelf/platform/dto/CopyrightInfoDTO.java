package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CopyrightInfoDTO {
    
    private String copyrightNotice;
    private String contentSource;
    private String authorName;
    private Boolean isOriginal;
}
