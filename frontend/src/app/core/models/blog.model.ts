export interface ApiResponse<T> {
  data: T;
  message: String;
  code: number;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface AuthorResponse {
  id: number;
  email: string;
  avatarUrl?: string;
  bio?: string;
}

export enum BlogStatus {
  DRAFT = 'DRAFT',
  PENDING = 'PENDING',
  PUBLISHED = 'PUBLISHED',
  REJECTED = 'REJECTED'
}

export interface BlogResponse {
  id: number;
  title: string;
  description: string;
  content: string;
  status: BlogStatus;
  rejectionReason?: string;
  author: AuthorResponse;
  likesCount: number;
  commentsCount: number;
  viewsCount: number;
  sharesCount: number;
  createdDate: string;
  modifiedDate: string;
}

export interface BlogQueryParams {
  keyword?: string;
  status?: BlogStatus;
  userId?: number;
  page?: number;
  size?: number;
  sort?: string;
}

export interface CreateBlogRequest {
  title: string;
  description: string;
  content: string;
  status?: BlogStatus;
}

export interface UpdateBlogRequest {
  title: string;
  description: string;
  content: string;
  status?: BlogStatus;
}

