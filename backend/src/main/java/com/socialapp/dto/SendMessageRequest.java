package com.socialapp.dto;

import com.socialapp.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "Recipient username is required")
    private String recipientUsername;

    @Size(max = 5000, message = "Message must be under 5000 characters")
    private String content;

    private String attachmentUrl;

    private Message.AttachmentType attachmentType;
}
