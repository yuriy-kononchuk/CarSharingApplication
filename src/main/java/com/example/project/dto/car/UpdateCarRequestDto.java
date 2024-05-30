package com.example.project.dto.car;

import java.math.BigDecimal;

public record UpdateCarRequestDto(int inventory, BigDecimal dailyFee) {
}
