package com.socialapp.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {

    @Size(max = 5000, message = "Post content must be under 5000 characters")
    private String content;

    private List<String> mediaUrls;

    private List<String> hashtags;

    private Boolean draft;
}
