export interface UploadUrlRequest {
  fileName: string;
  contentType: string;
  folder: string;
  fileSize: number;
}

export interface UploadPostResponse {
  uploadUrl: string;
  objectKey: string;
  formData: Record<string, string>;
  publicUrl: string;
}
