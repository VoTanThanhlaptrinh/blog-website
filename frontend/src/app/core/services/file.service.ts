import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { ApiResponse } from '../models/auth.model';
import { UploadPostResponse, UploadUrlRequest } from '../models/file.model';

@Injectable({
  providedIn: 'root',
})
export class FileService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/v1/file';

  getPresignedUrl(request: UploadUrlRequest): Observable<ApiResponse<UploadPostResponse>> {
    return this.http.post<ApiResponse<UploadPostResponse>>(this.apiUrl, request);
  }

  uploadFileToR2(file: File, folder = 'blog/temp'): Observable<string> {
    const request: UploadUrlRequest = {
      fileName: file.name,
      contentType: file.type || 'application/octet-stream',
      folder,
      fileSize: file.size,
    };

    return this.getPresignedUrl(request).pipe(
      switchMap((res) => {
        const { uploadUrl, formData, publicUrl } = res.data;
        const form = new FormData();
        Object.entries(formData).forEach(([key, value]) => {
          form.append(key, value);
        });
        form.append('file', file);

        return this.http.post(uploadUrl, form, { responseType: 'text' }).pipe(
          switchMap(() => [publicUrl])
        );
      })
    );
  }
}
