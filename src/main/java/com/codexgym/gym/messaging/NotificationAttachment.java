package com.codexgym.gym.messaging;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAttachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String fileName;
    private String contentType;
    private byte[] content;
}