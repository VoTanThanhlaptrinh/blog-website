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

export interface BlogCursorResponse {
  content: BlogResponse[];
  hasMore: boolean;
  nextCursor?: number;
}

export interface AuthorResponse {
  id: number;
  email: string;
  avatarUrl?: string;
  bio?: string;
}

export interface CategoryResponse {
  id: number;
  name: string;
  slug: string;
  description?: string;
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
  thumbnailUrl?: string;
  author: AuthorResponse;
  category?: CategoryResponse;
  likesCount: number;
  likedByCurrentUser?: boolean;
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
  categoryId?: number;
  page?: number;
  size?: number;
  sort?: string;
}

export interface CreateBlogRequest {
  title: string;
  description: string;
  content: string;
  status?: BlogStatus;
  thumbnailUrl?: string;
}

export interface UpdateBlogRequest {
  title: string;
  description: string;
  content: string;
  status?: BlogStatus;
  thumbnailUrl?: string;
}

export interface HomeStatsResponse {
  totalBlogs: number;
  totalAuthors: number;
  totalLikes: number;
  totalCategories: number;
}


