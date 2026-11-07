package com.blog.backend.storage.infrastructure.fileservice.dto;

import java.util.List;

public record ConfirmBatchRequest(List<String> tempKeys) {
}
