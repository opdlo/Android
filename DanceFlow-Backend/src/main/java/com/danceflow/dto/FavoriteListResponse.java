package com.danceflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteListResponse {
    private List<FavoriteItem> favorites;
    private Integer total;
}
