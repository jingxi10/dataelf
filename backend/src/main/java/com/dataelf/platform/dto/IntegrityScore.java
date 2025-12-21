package com.dataelf.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrityScore {
    private BigDecimal score;
    private int totalFields;
    private int filledFields;
    private List<String> missingFields;
}
