export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface DashboardVO {
  totalArticles: number
  totalUsers: number
  totalComments: number
  totalViews: number
  todayViews: number
  todayArticles: number
  todayComments: number
  pendingComments: number
}

export interface AdminUserVO {
  id: number
  username: string
  email: string
  nickname: string
  avatarUrl: string
  role: string
  status: string
  followerCount: number
  articleCount: number
  lastLoginAt: string
  createdAt: string
}

export interface ArticleListVO {
  id: number
  title: string
  summary: string
  coverUrl: string
  viewCount: number
  likeCount: number
  commentCount: number
  isFeatured: boolean
  isTop: boolean
  publishedAt: string
  authorName: string
  authorId: number
}

export interface AdminCommentVO {
  id: number
  articleId: number
  articleTitle: string
  userId: number
  username: string
  nickname: string
  content: string
  status: string
  createdAt: string
}
