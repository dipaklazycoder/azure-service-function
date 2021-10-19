package com.ds.azure;

import lombok.Builder;
import lombok.Data;

import java.net.URI;

@Builder
@Data
public class Message {
    private long id;
    private URI imageUrl;
}
