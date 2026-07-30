import { AuthorResponse } from './blog.model';

export interface CommentResponse {
  id: number;
  content: string;
  author: AuthorResponse;
  blogId: number;
  parentId?: number | null;
  status?: string;
  likeCount: number;
  likedByCurrentUser: boolean;
  replies?: CommentResponse[];
  createdDate: string;
  lastModifiedDate?: string;
}

export interface CreateCommentRequest {
  blogId: number;
  content: string;
  parentId?: number | null;
}

export interface UpdateCommentRequest {
  content: string;
}

export interface ToggleLikeRequest {
  blogId: number;
}

export interface LikeResponse {
  liked: boolean;
  likesCount: number;
}

export interface ToggleCommentLikeRequest {
  commentId: number;
}

export interface CommentLikeResponse {
  liked: boolean;
  likesCount: number;
}

export interface ToggleBookmarkRequest {
  blogId: number;
}

export interface BookmarkResponse {
  bookmarked: boolean;
}

export interface CreateShareRequest {
  blogId: number;
  provider: string;
}

export interface ShareResponse {
  sharesCount: number;
}

export interface FollowStatusResponse {
  following: boolean;
  followersCount: number;
}

export interface RecordViewRequest {
  blogId: number;
}

export interface ViewResponse {
  viewsCount: number;
}
