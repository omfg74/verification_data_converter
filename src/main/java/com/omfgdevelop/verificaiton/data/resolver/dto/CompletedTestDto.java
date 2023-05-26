package com.omfgdevelop.verificaiton.data.resolver.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedTestDto {

    private Long assignmentId;

    private String testAssignmentStatus;

    private Long assignedToId;

    private String startDate;

    private String completeDate;

    private String taskGroup;

    private Integer grade;

    private Double collectedPoint;

    private Integer maxPoint;

    private boolean noMore;

}
