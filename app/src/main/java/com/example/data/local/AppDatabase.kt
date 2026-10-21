package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  fun getUserById(id: Long): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  suspend fun getUserByIdSync(id: Long): UserEntity?

  @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
  suspend fun getUserByEmail(email: String): UserEntity?

  @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
  suspend fun getUserByUsername(username: String): UserEntity?

  @Query("SELECT * FROM users ORDER BY createdAt DESC")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT COUNT(*) FROM users")
  fun getUserCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertUser(user: UserEntity): Long

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("UPDATE users SET isBanned = :isBanned, banReason = :reason WHERE id = :userId")
  suspend fun updateUserBan(userId: Long, isBanned: Boolean, reason: String)

  @Query("UPDATE users SET role = :role WHERE id = :userId")
  suspend fun updateUserRole(userId: Long, role: String)

  @Query("UPDATE users SET points = :points WHERE id = :userId")
  suspend fun updateUserPoints(userId: Long, points: Int)
}

@Dao
interface CommunityDao {
  @Query("SELECT * FROM communities WHERE isRestricted = 0 ORDER BY membersCount DESC, createdAt DESC")
  fun getAllCommunities(): Flow<List<CommunityEntity>>

  @Query("SELECT * FROM communities ORDER BY createdAt DESC")
  fun getAllCommunitiesAdmin(): Flow<List<CommunityEntity>>

  @Query("SELECT * FROM communities WHERE id = :id LIMIT 1")
  fun getCommunityById(id: Long): Flow<CommunityEntity?>

  @Query("SELECT * FROM communities WHERE id = :id LIMIT 1")
  suspend fun getCommunityByIdSync(id: Long): CommunityEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCommunity(community: CommunityEntity): Long

  @Update
  suspend fun updateCommunity(community: CommunityEntity)

  @Query("DELETE FROM communities WHERE id = :id")
  suspend fun deleteCommunity(id: Long)

  @Query("UPDATE communities SET isRestricted = :isRestricted WHERE id = :id")
  suspend fun updateCommunityRestriction(id: Long, isRestricted: Boolean)

  @Query("SELECT COUNT(*) FROM communities")
  fun getCommunityCount(): Flow<Int>

  // Members
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMember(member: CommunityMemberEntity)

  @Query("DELETE FROM community_members WHERE communityId = :communityId AND userId = :userId")
  suspend fun deleteMember(communityId: Long, userId: Long)

  @Query("SELECT * FROM community_members WHERE communityId = :communityId AND userId = :userId LIMIT 1")
  suspend fun getMember(communityId: Long, userId: Long): CommunityMemberEntity?

  @Query("SELECT * FROM community_members WHERE communityId = :communityId")
  fun getCommunityMembers(communityId: Long): Flow<List<CommunityMemberEntity>>

  // Channels
  @Query("SELECT * FROM community_channels WHERE communityId = :communityId")
  fun getChannelsForCommunity(communityId: Long): Flow<List<CommunityChannelEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChannel(channel: CommunityChannelEntity): Long
}

@Dao
interface PostDao {
  @Query("SELECT * FROM posts WHERE isHidden = 0 ORDER BY createdAt DESC")
  fun getAllVisiblePosts(): Flow<List<PostEntity>>

  @Query("SELECT * FROM posts ORDER BY createdAt DESC")
  fun getAllPostsAdmin(): Flow<List<PostEntity>>

  @Query("SELECT * FROM posts WHERE communityId = :communityId AND isHidden = 0 ORDER BY createdAt DESC")
  fun getPostsForCommunity(communityId: Long): Flow<List<PostEntity>>

  @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
  fun getPostById(id: Long): Flow<PostEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPost(post: PostEntity): Long

  @Update
  suspend fun updatePost(post: PostEntity)

  @Query("DELETE FROM posts WHERE id = :id")
  suspend fun deletePost(id: Long)

  @Query("UPDATE posts SET isHidden = :isHidden WHERE id = :id")
  suspend fun hidePost(id: Long, isHidden: Boolean)

  @Query("SELECT COUNT(*) FROM posts")
  fun getPostCount(): Flow<Int>

  // Likes
  @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId AND userId = :userId")
  suspend fun isPostLiked(postId: Long, userId: Long): Int

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertLike(like: PostLikeEntity)

  @Query("DELETE FROM post_likes WHERE postId = :postId AND userId = :userId")
  suspend fun deleteLike(postId: Long, userId: Long)

  @Query("SELECT COUNT(*) FROM post_likes WHERE postId = :postId")
  suspend fun getLikesCountForPost(postId: Long): Int

  // Comments
  @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
  fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertComment(comment: CommentEntity): Long
}

@Dao
interface ChatDao {
  @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
  fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: MessageEntity): Long
}

@Dao
interface ReportDao {
  @Query("SELECT * FROM reports ORDER BY createdAt DESC")
  fun getAllReports(): Flow<List<ReportEntity>>

  @Query("SELECT * FROM reports WHERE reporterId = :reporterId ORDER BY createdAt DESC")
  fun getReportsByReporter(reporterId: Long): Flow<List<ReportEntity>>

  @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
  suspend fun getReportById(id: Long): ReportEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReport(report: ReportEntity): Long

  @Query("UPDATE reports SET status = :status, adminDecision = :decision, resolvedBy = :resolvedBy, updatedAt = :updatedAt WHERE id = :id")
  suspend fun updateReportStatus(id: Long, status: String, decision: String, resolvedBy: String, updatedAt: Long)

  @Query("SELECT COUNT(*) FROM reports WHERE status = 'NEW' OR status = 'IN_REVIEW'")
  fun getPendingReportCount(): Flow<Int>
}

@Dao
interface StoreDao {
  @Query("SELECT * FROM store_items")
  fun getAllStoreItems(): Flow<List<StoreItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStoreItems(items: List<StoreItemEntity>)

  @Query("SELECT * FROM user_inventory WHERE userId = :userId")
  fun getUserInventory(userId: Long): Flow<List<UserInventoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInventory(inventory: UserInventoryEntity): Long
}

@Dao
interface AuditDao {
  @Query("SELECT * FROM admin_audit_logs ORDER BY timestamp DESC")
  fun getAllAuditLogs(): Flow<List<AdminAuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuditLog(log: AdminAuditLogEntity): Long
}

@Dao
interface ProjectDao {
  @Query("SELECT * FROM projects WHERE isApproved = 1 ORDER BY createdAt DESC")
  fun getAllProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects ORDER BY createdAt DESC")
  fun getAllProjectsAdmin(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
  fun getProjectById(id: Long): Flow<ProjectEntity?>

  @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
  suspend fun getProjectByIdSync(id: Long): ProjectEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProject(project: ProjectEntity): Long

  @Query("UPDATE projects SET downloadsCount = downloadsCount + 1 WHERE id = :id")
  suspend fun incrementDownloadCount(id: Long)

  @Query("DELETE FROM projects WHERE id = :id")
  suspend fun deleteProject(id: Long)

  @Query("UPDATE projects SET isApproved = :isApproved WHERE id = :id")
  suspend fun updateProjectApproval(id: Long, isApproved: Boolean)
}

@Database(
  entities = [
    UserEntity::class,
    CommunityEntity::class,
    CommunityMemberEntity::class,
    CommunityChannelEntity::class,
    PostEntity::class,
    PostLikeEntity::class,
    CommentEntity::class,
    MessageEntity::class,
    ReportEntity::class,
    StoreItemEntity::class,
    UserInventoryEntity::class,
    AdminAuditLogEntity::class,
    ProjectEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun communityDao(): CommunityDao
  abstract fun postDao(): PostDao
  abstract fun chatDao(): ChatDao
  abstract fun reportDao(): ReportDao
  abstract fun storeDao(): StoreDao
  abstract fun auditDao(): AuditDao
  abstract fun projectDao(): ProjectDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "arabic_social_db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
