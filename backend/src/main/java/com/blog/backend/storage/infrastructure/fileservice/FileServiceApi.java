package com.blog.backend.storage.infrastructure.fileservice;

import com.blog.backend.storage.infrastructure.fileservice.dto.ConfirmBatchRequest;
import com.blog.backend.storage.infrastructure.fileservice.dto.ConfirmResult;
import com.blog.backend.storage.infrastructure.fileservice.dto.FileServiceResponse;
import com.blog.backend.storage.infrastructure.fileservice.dto.PresignRequest;
import com.blog.backend.storage.infrastructure.fileservice.dto.PresignResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/** Declarative HTTP client for file-service (only the endpoints the blog uses). */
@HttpExchange("/api/v1/files")
public interface FileServiceApi {

    @PostExchange("/presign")
    FileServiceResponse<PresignResult> presign(@RequestBody PresignRequest request);

    @PostExchange("/confirm-batch")
    FileServiceResponse<List<ConfirmResult>> confirmBatch(@RequestBody ConfirmBatchRequest request);
}
