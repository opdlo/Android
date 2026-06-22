package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private List<PostResponse> posts;
    private Integer total;
    private Integer page;
    private Integer pageSize;
}
