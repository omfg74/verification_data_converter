package com.omfgdevelop.verificaiton.data.resolver.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatsDto {

    int updatedCount;

    int failedCount;

    int error;

}