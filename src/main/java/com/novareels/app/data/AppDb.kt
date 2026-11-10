package com.novareels.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey val id: String,
    val url: String,
    val author: String,
    val authorHue: Float,
    val caption: String,
    val music: String,
    var likes: Long = 0,
    var views: Long = 0,
    var likedByMe: Boolean = false,
    var savedByMe: Boolean = false,
    var following: Boolean = false,
    var mine: Boolean = false,
    var uploaded: Boolean = true,
    var repostedFrom: String = "",
    var hidden: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments", indices = [androidx.room.Index("videoId")])
data class Comment(
    @PrimaryKey val id: String,
    val videoId: String,
    val author: String,
    val authorHue: Float,
    val text: String,
    val kind: Int,
    val sticker: String? = null,
    var mediaPath: String? = null,
    var mediaUrl: String? = null,
    var likes: Long = 0,
    var likedByMe: Boolean = false,
    var parentId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TEXTO = 0
        const val STICKER = 1
        const val FOTO = 2
        const val AUDIO = 3
    }
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY createdAt DESC")
    fun all(): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun byId(id: String): Video?

    @Query("SELECT * FROM videos WHERE mine = 1 ORDER BY createdAt DESC")
    fun mine(): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE savedByMe = 1 ORDER BY createdAt DESC")
    fun saved(): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE views > 0 AND mine = 0 ORDER BY createdAt DESC LIMIT 40")
    fun watched(): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE likedByMe = 1 ORDER BY createdAt DESC")
    fun liked(): Flow<List<Video>>

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE videos SET hidden = 1 WHERE id = :id")
    suspend fun hide(id: String)

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(video: Video)

    @Query("UPDATE videos SET likes = MAX(0, likes + :delta) WHERE id = :id")
    suspend fun addLikes(id: String, delta: Long)

    @Query("UPDATE videos SET likedByMe = :liked WHERE id = :id")
    suspend fun setLiked(id: String, liked: Boolean)

    @Query("UPDATE videos SET savedByMe = :saved WHERE id = :id")
    suspend fun setSaved(id: String, saved: Boolean)

    @Query("UPDATE videos SET following = :following WHERE id = :id")
    suspend fun setFollowing(id: String, following: Boolean)

    @Query("UPDATE videos SET views = views + 1 WHERE id = :id")
    suspend fun addView(id: String)

    @Query("UPDATE videos SET url = :url, uploaded = 1 WHERE id = :id")
    suspend fun setUrl(id: String, url: String)

    @Query("SELECT * FROM videos WHERE uploaded = 0")
    suspend fun pendingUploads(): List<Video>

    @Query("SELECT COALESCE(SUM(likes),0) FROM videos WHERE mine = 1")
    fun myLikes(): Flow<Long>

    @Query("SELECT COUNT(*) FROM videos WHERE mine = 1")
    fun myCount(): Flow<Int>
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY createdAt DESC")
    fun byVideo(videoId: String): Flow<List<Comment>>

    @Query("SELECT c.* FROM comments c INNER JOIN videos v ON v.id = c.videoId WHERE v.mine = 1 AND c.author != :me ORDER BY c.createdAt DESC LIMIT 60")
    fun activity(me: String): Flow<List<Comment>>

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM comments WHERE videoId = :videoId")
    suspend fun countOf(videoId: String): Int

    @Query("SELECT * FROM comments WHERE id = :id")
    suspend fun byId(id: String): Comment?

    @Query("SELECT * FROM comments WHERE videoId = :videoId AND kind = 0")
    suspend fun textCommentsOf(videoId: String): List<Comment>

    @Query("UPDATE comments SET likes = MAX(0, likes + :delta) WHERE id = :id")
    suspend fun likeRemote(id: String, delta: Long)

    @Query("UPDATE comments SET mediaUrl = :url WHERE id = :id")
    suspend fun setMediaUrl(id: String, url: String)

    @Query("SELECT COUNT(*) FROM comments")
    suspend fun count(): Int

    @androidx.room.Query("SELECT videoId AS videoId, COUNT(*) AS n FROM comments GROUP BY videoId")
    fun counts(): Flow<List<VideoCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(comment: Comment)

    @Query("UPDATE comments SET likes = MAX(0, likes + :delta), likedByMe = :liked WHERE id = :id")
    suspend fun like(id: String, delta: Long, liked: Boolean)
}

data class VideoCount(val videoId: String, val n: Int)

@Entity(tableName = "messages", indices = [androidx.room.Index("threadId")])
data class Message(
    @PrimaryKey val id: String,
    val threadId: String,
    val author: String,
    val text: String,
    val mine: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "drafts")
data class Draft(
    @PrimaryKey val id: String,
    val path: String,
    val caption: String,
    val music: String,
    val filter: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "follows")
data class Follow(
    @PrimaryKey val handle: String,
    val following: Boolean = false,
    val followsMe: Boolean = false,
    val hue: Float = 30f
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE threadId = :thread ORDER BY createdAt ASC")
    fun byThread(thread: String): Flow<List<Message>>

    @Query("SELECT * FROM messages ORDER BY createdAt DESC")
    fun all(): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(m: Message)

    @Query("SELECT COUNT(*) FROM messages WHERE mine = 0 AND threadId = :thread")
    fun unread(thread: String): Flow<Int>
}

@Dao
interface DraftDao {
    @Query("SELECT * FROM drafts ORDER BY createdAt DESC")
    fun all(): Flow<List<Draft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(d: Draft)

    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM drafts")
    fun count(): Flow<Int>
}

@Dao
interface FollowDao {
    @Query("SELECT * FROM follows")
    fun all(): Flow<List<Follow>>

    @Query("SELECT * FROM follows WHERE following = 1")
    fun following(): Flow<List<Follow>>

    @Query("SELECT * FROM follows WHERE followsMe = 1")
    fun followers(): Flow<List<Follow>>

    @Query("SELECT * FROM follows WHERE handle = :handle")
    suspend fun byHandle(handle: String): Follow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(f: Follow)
}

@Database(
    entities = [Video::class, Comment::class, Message::class, Draft::class, Follow::class],
    version = 6,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun commentDao(): CommentDao
    abstract fun messageDao(): MessageDao
    abstract fun draftDao(): DraftDao
    abstract fun followDao(): FollowDao

    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(ctx: Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "novareels.db")
                .fallbackToDestructiveMigration()
                .build().also { inst = it }
        }
    }
}
