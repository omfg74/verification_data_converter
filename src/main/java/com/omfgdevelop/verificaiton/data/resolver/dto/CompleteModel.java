package com.omfgdevelop.verificaiton.data.resolver.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteModel {

    private String idsString;

    private String url;

    private String port;

    private String token;

    private String success;

    private String failed;
}
