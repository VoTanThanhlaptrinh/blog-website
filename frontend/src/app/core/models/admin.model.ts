import { AuthorResponse } from './blog.model';

export enum ReportStatus {
  PENDING = 'PENDING',
  RESOLVED_ACCEPTED = 'RESOLVED_ACCEPTED',
  RESOLVED_REJECTED = 'RESOLVED_REJECTED'
}

export enum ReportTargetType {
  BLOG = 'BLOG',
  COMMENT = 'COMMENT',
  USER = 'USER'
}

export interface AdminDashboardSummaryResponse {
  totalUsers: number;
  totalBlogs: number;
  totalComments: number;
  pendingBlogsCount: number;
  pendingReportsCount: number;
}

export interface DailyGrowthResponse {
  date: string;
  newUsersCount: number;
  newBlogsCount: number;
}

export interface ReportResponse {
  id: number;
  targetType: ReportTargetType;
  targetId: number;
  reason: string;
  reporter: AuthorResponse;
  status: ReportStatus;
  adminNotes?: string;
  createdDate: string;
  modifiedDate: string;
}

export interface RejectBlogRequest {
  reason: string;
}

export interface ResolveReportRequest {
  status: ReportStatus;
  adminNotes?: string;
}

export interface CreateReportRequest {
  targetType: ReportTargetType;
  targetId: number;
  reason: string;
}

export interface AdminBlogQueryParams {
  status?: string;
  keyword?: string;
  page?: number;
  size?: number;
}

export interface AdminReportQueryParams {
  targetType?: ReportTargetType;
  status?: ReportStatus;
  page?: number;
  size?: number;
}
