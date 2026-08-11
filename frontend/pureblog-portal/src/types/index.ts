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
  authorAvatar: string
  authorId: number
  categoryName: string
  tagNames: string[]
}

export interface ArticleDetailVO {
  id: number
  title: string
  summary: string
  coverUrl: string
  content: string
  htmlContent: string
  wordCount: number
  viewCount: number
  likeCount: number
  commentCount: number
  collectCount: number
  isFeatured: boolean
  isTop: boolean
  isLiked: boolean
  isCollected: boolean
  publishedAt: string
  createdAt: string
  author: {
    userId: number
    username: string
    nickname: string
    avatarUrl: string
    followerCount: number
  }
  category: {
    id: number
    name: string
    slug: string
  }
  tags: {
    id: number
    name: string
    slug: string
  }[]
}

export interface UserProfileVO {
  userId: number
  username: string
  nickname: string
  avatarUrl: string
  bio: string
  role: string
  followerCount: number
  followingCount: number
  articleCount: number
  isFollowing: boolean
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: {
    userId: number
    username: string
    nickname: string
    avatarUrl: string
    role: string
    roleCode: number
  }
}

export interface CommentVO {
  id: number
  articleId: number
  userId: number
  username: string
  nickname: string
  avatarUrl: string
  content: string
  likeCount: number
  isLiked: boolean
  status: string
  createdAt: string
  replies: CommentVO[]
}

export interface SearchResult {
  articles: SearchVO[]
  total: number
  page: number
  size: number
  totalPages: number
  tookMs: number
}

export interface SearchVO {
  articleId: number
  title: string
  summary: string
  authorName: string
  authorAvatar: string
  authorId: number
  categoryName: string
  tagNames: string[]
  viewCount: number
  likeCount: number
  publishedAt: string
  highlights: string[]
}

export interface NotificationVO {
  id: number
  type: number
  typeDesc: string
  title: string
  content: string
  relatedId: number
  relatedType: number
  isRead: boolean
  createdAt: string
  relativeTime: string
}
