package com.omfgdevelop.verificaiton.data.resolver.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPatchDto {

    private long externalId;

    private String personId;

}