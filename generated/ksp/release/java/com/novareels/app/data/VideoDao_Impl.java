package com.novareels.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class VideoDao_Impl implements VideoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Video> __insertionAdapterOfVideo;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfHide;

  private final SharedSQLiteStatement __preparedStmtOfAddLikes;

  private final SharedSQLiteStatement __preparedStmtOfSetLiked;

  private final SharedSQLiteStatement __preparedStmtOfSetSaved;

  private final SharedSQLiteStatement __preparedStmtOfSetFollowing;

  private final SharedSQLiteStatement __preparedStmtOfAddView;

  private final SharedSQLiteStatement __preparedStmtOfSetUrl;

  public VideoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVideo = new EntityInsertionAdapter<Video>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `videos` (`id`,`url`,`author`,`authorHue`,`caption`,`music`,`likes`,`views`,`likedByMe`,`savedByMe`,`following`,`mine`,`uploaded`,`repostedFrom`,`hidden`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Video entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getUrl());
        statement.bindString(3, entity.getAuthor());
        statement.bindDouble(4, entity.getAuthorHue());
        statement.bindString(5, entity.getCaption());
        statement.bindString(6, entity.getMusic());
        statement.bindLong(7, entity.getLikes());
        statement.bindLong(8, entity.getViews());
        final int _tmp = entity.getLikedByMe() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.getSavedByMe() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.getFollowing() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        final int _tmp_3 = entity.getMine() ? 1 : 0;
        statement.bindLong(12, _tmp_3);
        final int _tmp_4 = entity.getUploaded() ? 1 : 0;
        statement.bindLong(13, _tmp_4);
        statement.bindString(14, entity.getRepostedFrom());
        final int _tmp_5 = entity.getHidden() ? 1 : 0;
        statement.bindLong(15, _tmp_5);
        statement.bindLong(16, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM videos WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfHide = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE videos SET hidden = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfAddLikes = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE videos SET likes = MAX(0, likes + ?) WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetLiked = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE videos SET likedByMe = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetSaved = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE videos SET savedByMe = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetFollowing = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE videos SET following = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfAddView = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE videos SET views = views + 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetUrl = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE videos SET url = ?, uploaded = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final Video video, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfVideo.insert(video);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object hide(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfHide.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfHide.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object addLikes(final String id, final long delta,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAddLikes.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, delta);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfAddLikes.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setLiked(final String id, final boolean liked,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetLiked.acquire();
        int _argIndex = 1;
        final int _tmp = liked ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetLiked.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setSaved(final String id, final boolean saved,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetSaved.acquire();
        int _argIndex = 1;
        final int _tmp = saved ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetSaved.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setFollowing(final String id, final boolean following,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetFollowing.acquire();
        int _argIndex = 1;
        final int _tmp = following ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetFollowing.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object addView(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAddView.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfAddView.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setUrl(final String id, final String url,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetUrl.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, url);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetUrl.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Video>> all() {
    final String _sql = "SELECT * FROM videos ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"videos"}, new Callable<List<Video>>() {
      @Override
      @NonNull
      public List<Video> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthorHue = CursorUtil.getColumnIndexOrThrow(_cursor, "authorHue");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfMusic = CursorUtil.getColumnIndexOrThrow(_cursor, "music");
          final int _cursorIndexOfLikes = CursorUtil.getColumnIndexOrThrow(_cursor, "likes");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfLikedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "likedByMe");
          final int _cursorIndexOfSavedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "savedByMe");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfMine = CursorUtil.getColumnIndexOrThrow(_cursor, "mine");
          final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
          final int _cursorIndexOfRepostedFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "repostedFrom");
          final int _cursorIndexOfHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "hidden");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Video> _result = new ArrayList<Video>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Video _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final float _tmpAuthorHue;
            _tmpAuthorHue = _cursor.getFloat(_cursorIndexOfAuthorHue);
            final String _tmpCaption;
            _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            final String _tmpMusic;
            _tmpMusic = _cursor.getString(_cursorIndexOfMusic);
            final long _tmpLikes;
            _tmpLikes = _cursor.getLong(_cursorIndexOfLikes);
            final long _tmpViews;
            _tmpViews = _cursor.getLong(_cursorIndexOfViews);
            final boolean _tmpLikedByMe;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLikedByMe);
            _tmpLikedByMe = _tmp != 0;
            final boolean _tmpSavedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSavedByMe);
            _tmpSavedByMe = _tmp_1 != 0;
            final boolean _tmpFollowing;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp_2 != 0;
            final boolean _tmpMine;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfMine);
            _tmpMine = _tmp_3 != 0;
            final boolean _tmpUploaded;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfUploaded);
            _tmpUploaded = _tmp_4 != 0;
            final String _tmpRepostedFrom;
            _tmpRepostedFrom = _cursor.getString(_cursorIndexOfRepostedFrom);
            final boolean _tmpHidden;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHidden);
            _tmpHidden = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Video(_tmpId,_tmpUrl,_tmpAuthor,_tmpAuthorHue,_tmpCaption,_tmpMusic,_tmpLikes,_tmpViews,_tmpLikedByMe,_tmpSavedByMe,_tmpFollowing,_tmpMine,_tmpUploaded,_tmpRepostedFrom,_tmpHidden,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object byId(final String id, final Continuation<? super Video> $completion) {
    final String _sql = "SELECT * FROM videos WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Video>() {
      @Override
      @Nullable
      public Video call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthorHue = CursorUtil.getColumnIndexOrThrow(_cursor, "authorHue");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfMusic = CursorUtil.getColumnIndexOrThrow(_cursor, "music");
          final int _cursorIndexOfLikes = CursorUtil.getColumnIndexOrThrow(_cursor, "likes");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfLikedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "likedByMe");
          final int _cursorIndexOfSavedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "savedByMe");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfMine = CursorUtil.getColumnIndexOrThrow(_cursor, "mine");
          final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
          final int _cursorIndexOfRepostedFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "repostedFrom");
          final int _cursorIndexOfHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "hidden");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Video _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final float _tmpAuthorHue;
            _tmpAuthorHue = _cursor.getFloat(_cursorIndexOfAuthorHue);
            final String _tmpCaption;
            _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            final String _tmpMusic;
            _tmpMusic = _cursor.getString(_cursorIndexOfMusic);
            final long _tmpLikes;
            _tmpLikes = _cursor.getLong(_cursorIndexOfLikes);
            final long _tmpViews;
            _tmpViews = _cursor.getLong(_cursorIndexOfViews);
            final boolean _tmpLikedByMe;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLikedByMe);
            _tmpLikedByMe = _tmp != 0;
            final boolean _tmpSavedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSavedByMe);
            _tmpSavedByMe = _tmp_1 != 0;
            final boolean _tmpFollowing;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp_2 != 0;
            final boolean _tmpMine;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfMine);
            _tmpMine = _tmp_3 != 0;
            final boolean _tmpUploaded;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfUploaded);
            _tmpUploaded = _tmp_4 != 0;
            final String _tmpRepostedFrom;
            _tmpRepostedFrom = _cursor.getString(_cursorIndexOfRepostedFrom);
            final boolean _tmpHidden;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHidden);
            _tmpHidden = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Video(_tmpId,_tmpUrl,_tmpAuthor,_tmpAuthorHue,_tmpCaption,_tmpMusic,_tmpLikes,_tmpViews,_tmpLikedByMe,_tmpSavedByMe,_tmpFollowing,_tmpMine,_tmpUploaded,_tmpRepostedFrom,_tmpHidden,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Video>> mine() {
    final String _sql = "SELECT * FROM videos WHERE mine = 1 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"videos"}, new Callable<List<Video>>() {
      @Override
      @NonNull
      public List<Video> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthorHue = CursorUtil.getColumnIndexOrThrow(_cursor, "authorHue");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfMusic = CursorUtil.getColumnIndexOrThrow(_cursor, "music");
          final int _cursorIndexOfLikes = CursorUtil.getColumnIndexOrThrow(_cursor, "likes");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfLikedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "likedByMe");
          final int _cursorIndexOfSavedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "savedByMe");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfMine = CursorUtil.getColumnIndexOrThrow(_cursor, "mine");
          final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
          final int _cursorIndexOfRepostedFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "repostedFrom");
          final int _cursorIndexOfHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "hidden");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Video> _result = new ArrayList<Video>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Video _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final float _tmpAuthorHue;
            _tmpAuthorHue = _cursor.getFloat(_cursorIndexOfAuthorHue);
            final String _tmpCaption;
            _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            final String _tmpMusic;
            _tmpMusic = _cursor.getString(_cursorIndexOfMusic);
            final long _tmpLikes;
            _tmpLikes = _cursor.getLong(_cursorIndexOfLikes);
            final long _tmpViews;
            _tmpViews = _cursor.getLong(_cursorIndexOfViews);
            final boolean _tmpLikedByMe;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLikedByMe);
            _tmpLikedByMe = _tmp != 0;
            final boolean _tmpSavedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSavedByMe);
            _tmpSavedByMe = _tmp_1 != 0;
            final boolean _tmpFollowing;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp_2 != 0;
            final boolean _tmpMine;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfMine);
            _tmpMine = _tmp_3 != 0;
            final boolean _tmpUploaded;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfUploaded);
            _tmpUploaded = _tmp_4 != 0;
            final String _tmpRepostedFrom;
            _tmpRepostedFrom = _cursor.getString(_cursorIndexOfRepostedFrom);
            final boolean _tmpHidden;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHidden);
            _tmpHidden = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Video(_tmpId,_tmpUrl,_tmpAuthor,_tmpAuthorHue,_tmpCaption,_tmpMusic,_tmpLikes,_tmpViews,_tmpLikedByMe,_tmpSavedByMe,_tmpFollowing,_tmpMine,_tmpUploaded,_tmpRepostedFrom,_tmpHidden,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Video>> saved() {
    final String _sql = "SELECT * FROM videos WHERE savedByMe = 1 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"videos"}, new Callable<List<Video>>() {
      @Override
      @NonNull
      public List<Video> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthorHue = CursorUtil.getColumnIndexOrThrow(_cursor, "authorHue");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfMusic = CursorUtil.getColumnIndexOrThrow(_cursor, "music");
          final int _cursorIndexOfLikes = CursorUtil.getColumnIndexOrThrow(_cursor, "likes");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfLikedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "likedByMe");
          final int _cursorIndexOfSavedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "savedByMe");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfMine = CursorUtil.getColumnIndexOrThrow(_cursor, "mine");
          final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
          final int _cursorIndexOfRepostedFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "repostedFrom");
          final int _cursorIndexOfHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "hidden");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Video> _result = new ArrayList<Video>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Video _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final float _tmpAuthorHue;
            _tmpAuthorHue = _cursor.getFloat(_cursorIndexOfAuthorHue);
            final String _tmpCaption;
            _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            final String _tmpMusic;
            _tmpMusic = _cursor.getString(_cursorIndexOfMusic);
            final long _tmpLikes;
            _tmpLikes = _cursor.getLong(_cursorIndexOfLikes);
            final long _tmpViews;
            _tmpViews = _cursor.getLong(_cursorIndexOfViews);
            final boolean _tmpLikedByMe;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLikedByMe);
            _tmpLikedByMe = _tmp != 0;
            final boolean _tmpSavedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSavedByMe);
            _tmpSavedByMe = _tmp_1 != 0;
            final boolean _tmpFollowing;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp_2 != 0;
            final boolean _tmpMine;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfMine);
            _tmpMine = _tmp_3 != 0;
            final boolean _tmpUploaded;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfUploaded);
            _tmpUploaded = _tmp_4 != 0;
            final String _tmpRepostedFrom;
            _tmpRepostedFrom = _cursor.getString(_cursorIndexOfRepostedFrom);
            final boolean _tmpHidden;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHidden);
            _tmpHidden = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Video(_tmpId,_tmpUrl,_tmpAuthor,_tmpAuthorHue,_tmpCaption,_tmpMusic,_tmpLikes,_tmpViews,_tmpLikedByMe,_tmpSavedByMe,_tmpFollowing,_tmpMine,_tmpUploaded,_tmpRepostedFrom,_tmpHidden,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Video>> watched() {
    final String _sql = "SELECT * FROM videos WHERE views > 0 AND mine = 0 ORDER BY createdAt DESC LIMIT 40";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"videos"}, new Callable<List<Video>>() {
      @Override
      @NonNull
      public List<Video> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthorHue = CursorUtil.getColumnIndexOrThrow(_cursor, "authorHue");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfMusic = CursorUtil.getColumnIndexOrThrow(_cursor, "music");
          final int _cursorIndexOfLikes = CursorUtil.getColumnIndexOrThrow(_cursor, "likes");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfLikedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "likedByMe");
          final int _cursorIndexOfSavedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "savedByMe");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfMine = CursorUtil.getColumnIndexOrThrow(_cursor, "mine");
          final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
          final int _cursorIndexOfRepostedFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "repostedFrom");
          final int _cursorIndexOfHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "hidden");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Video> _result = new ArrayList<Video>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Video _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final float _tmpAuthorHue;
            _tmpAuthorHue = _cursor.getFloat(_cursorIndexOfAuthorHue);
            final String _tmpCaption;
            _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            final String _tmpMusic;
            _tmpMusic = _cursor.getString(_cursorIndexOfMusic);
            final long _tmpLikes;
            _tmpLikes = _cursor.getLong(_cursorIndexOfLikes);
            final long _tmpViews;
            _tmpViews = _cursor.getLong(_cursorIndexOfViews);
            final boolean _tmpLikedByMe;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLikedByMe);
            _tmpLikedByMe = _tmp != 0;
            final boolean _tmpSavedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSavedByMe);
            _tmpSavedByMe = _tmp_1 != 0;
            final boolean _tmpFollowing;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp_2 != 0;
            final boolean _tmpMine;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfMine);
            _tmpMine = _tmp_3 != 0;
            final boolean _tmpUploaded;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfUploaded);
            _tmpUploaded = _tmp_4 != 0;
            final String _tmpRepostedFrom;
            _tmpRepostedFrom = _cursor.getString(_cursorIndexOfRepostedFrom);
            final boolean _tmpHidden;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHidden);
            _tmpHidden = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Video(_tmpId,_tmpUrl,_tmpAuthor,_tmpAuthorHue,_tmpCaption,_tmpMusic,_tmpLikes,_tmpViews,_tmpLikedByMe,_tmpSavedByMe,_tmpFollowing,_tmpMine,_tmpUploaded,_tmpRepostedFrom,_tmpHidden,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Video>> liked() {
    final String _sql = "SELECT * FROM videos WHERE likedByMe = 1 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"videos"}, new Callable<List<Video>>() {
      @Override
      @NonNull
      public List<Video> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthorHue = CursorUtil.getColumnIndexOrThrow(_cursor, "authorHue");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfMusic = CursorUtil.getColumnIndexOrThrow(_cursor, "music");
          final int _cursorIndexOfLikes = CursorUtil.getColumnIndexOrThrow(_cursor, "likes");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfLikedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "likedByMe");
          final int _cursorIndexOfSavedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "savedByMe");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfMine = CursorUtil.getColumnIndexOrThrow(_cursor, "mine");
          final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
          final int _cursorIndexOfRepostedFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "repostedFrom");
          final int _cursorIndexOfHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "hidden");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Video> _result = new ArrayList<Video>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Video _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final float _tmpAuthorHue;
            _tmpAuthorHue = _cursor.getFloat(_cursorIndexOfAuthorHue);
            final String _tmpCaption;
            _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            final String _tmpMusic;
            _tmpMusic = _cursor.getString(_cursorIndexOfMusic);
            final long _tmpLikes;
            _tmpLikes = _cursor.getLong(_cursorIndexOfLikes);
            final long _tmpViews;
            _tmpViews = _cursor.getLong(_cursorIndexOfViews);
            final boolean _tmpLikedByMe;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLikedByMe);
            _tmpLikedByMe = _tmp != 0;
            final boolean _tmpSavedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSavedByMe);
            _tmpSavedByMe = _tmp_1 != 0;
            final boolean _tmpFollowing;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp_2 != 0;
            final boolean _tmpMine;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfMine);
            _tmpMine = _tmp_3 != 0;
            final boolean _tmpUploaded;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfUploaded);
            _tmpUploaded = _tmp_4 != 0;
            final String _tmpRepostedFrom;
            _tmpRepostedFrom = _cursor.getString(_cursorIndexOfRepostedFrom);
            final boolean _tmpHidden;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHidden);
            _tmpHidden = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Video(_tmpId,_tmpUrl,_tmpAuthor,_tmpAuthorHue,_tmpCaption,_tmpMusic,_tmpLikes,_tmpViews,_tmpLikedByMe,_tmpSavedByMe,_tmpFollowing,_tmpMine,_tmpUploaded,_tmpRepostedFrom,_tmpHidden,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM videos";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object pendingUploads(final Continuation<? super List<Video>> $completion) {
    final String _sql = "SELECT * FROM videos WHERE uploaded = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Video>>() {
      @Override
      @NonNull
      public List<Video> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthorHue = CursorUtil.getColumnIndexOrThrow(_cursor, "authorHue");
          final int _cursorIndexOfCaption = CursorUtil.getColumnIndexOrThrow(_cursor, "caption");
          final int _cursorIndexOfMusic = CursorUtil.getColumnIndexOrThrow(_cursor, "music");
          final int _cursorIndexOfLikes = CursorUtil.getColumnIndexOrThrow(_cursor, "likes");
          final int _cursorIndexOfViews = CursorUtil.getColumnIndexOrThrow(_cursor, "views");
          final int _cursorIndexOfLikedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "likedByMe");
          final int _cursorIndexOfSavedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "savedByMe");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfMine = CursorUtil.getColumnIndexOrThrow(_cursor, "mine");
          final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
          final int _cursorIndexOfRepostedFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "repostedFrom");
          final int _cursorIndexOfHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "hidden");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Video> _result = new ArrayList<Video>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Video _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final float _tmpAuthorHue;
            _tmpAuthorHue = _cursor.getFloat(_cursorIndexOfAuthorHue);
            final String _tmpCaption;
            _tmpCaption = _cursor.getString(_cursorIndexOfCaption);
            final String _tmpMusic;
            _tmpMusic = _cursor.getString(_cursorIndexOfMusic);
            final long _tmpLikes;
            _tmpLikes = _cursor.getLong(_cursorIndexOfLikes);
            final long _tmpViews;
            _tmpViews = _cursor.getLong(_cursorIndexOfViews);
            final boolean _tmpLikedByMe;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfLikedByMe);
            _tmpLikedByMe = _tmp != 0;
            final boolean _tmpSavedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSavedByMe);
            _tmpSavedByMe = _tmp_1 != 0;
            final boolean _tmpFollowing;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp_2 != 0;
            final boolean _tmpMine;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfMine);
            _tmpMine = _tmp_3 != 0;
            final boolean _tmpUploaded;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfUploaded);
            _tmpUploaded = _tmp_4 != 0;
            final String _tmpRepostedFrom;
            _tmpRepostedFrom = _cursor.getString(_cursorIndexOfRepostedFrom);
            final boolean _tmpHidden;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHidden);
            _tmpHidden = _tmp_5 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Video(_tmpId,_tmpUrl,_tmpAuthor,_tmpAuthorHue,_tmpCaption,_tmpMusic,_tmpLikes,_tmpViews,_tmpLikedByMe,_tmpSavedByMe,_tmpFollowing,_tmpMine,_tmpUploaded,_tmpRepostedFrom,_tmpHidden,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Long> myLikes() {
    final String _sql = "SELECT COALESCE(SUM(likes),0) FROM videos WHERE mine = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"videos"}, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> myCount() {
    final String _sql = "SELECT COUNT(*) FROM videos WHERE mine = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"videos"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
