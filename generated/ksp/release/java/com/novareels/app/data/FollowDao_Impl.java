package com.novareels.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
public final class FollowDao_Impl implements FollowDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Follow> __insertionAdapterOfFollow;

  public FollowDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFollow = new EntityInsertionAdapter<Follow>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `follows` (`handle`,`following`,`followsMe`,`hue`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Follow entity) {
        statement.bindString(1, entity.getHandle());
        final int _tmp = entity.getFollowing() ? 1 : 0;
        statement.bindLong(2, _tmp);
        final int _tmp_1 = entity.getFollowsMe() ? 1 : 0;
        statement.bindLong(3, _tmp_1);
        statement.bindDouble(4, entity.getHue());
      }
    };
  }

  @Override
  public Object upsert(final Follow f, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFollow.insert(f);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Follow>> all() {
    final String _sql = "SELECT * FROM follows";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"follows"}, new Callable<List<Follow>>() {
      @Override
      @NonNull
      public List<Follow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHandle = CursorUtil.getColumnIndexOrThrow(_cursor, "handle");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfFollowsMe = CursorUtil.getColumnIndexOrThrow(_cursor, "followsMe");
          final int _cursorIndexOfHue = CursorUtil.getColumnIndexOrThrow(_cursor, "hue");
          final List<Follow> _result = new ArrayList<Follow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Follow _item;
            final String _tmpHandle;
            _tmpHandle = _cursor.getString(_cursorIndexOfHandle);
            final boolean _tmpFollowing;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp != 0;
            final boolean _tmpFollowsMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfFollowsMe);
            _tmpFollowsMe = _tmp_1 != 0;
            final float _tmpHue;
            _tmpHue = _cursor.getFloat(_cursorIndexOfHue);
            _item = new Follow(_tmpHandle,_tmpFollowing,_tmpFollowsMe,_tmpHue);
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
  public Flow<List<Follow>> following() {
    final String _sql = "SELECT * FROM follows WHERE following = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"follows"}, new Callable<List<Follow>>() {
      @Override
      @NonNull
      public List<Follow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHandle = CursorUtil.getColumnIndexOrThrow(_cursor, "handle");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfFollowsMe = CursorUtil.getColumnIndexOrThrow(_cursor, "followsMe");
          final int _cursorIndexOfHue = CursorUtil.getColumnIndexOrThrow(_cursor, "hue");
          final List<Follow> _result = new ArrayList<Follow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Follow _item;
            final String _tmpHandle;
            _tmpHandle = _cursor.getString(_cursorIndexOfHandle);
            final boolean _tmpFollowing;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp != 0;
            final boolean _tmpFollowsMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfFollowsMe);
            _tmpFollowsMe = _tmp_1 != 0;
            final float _tmpHue;
            _tmpHue = _cursor.getFloat(_cursorIndexOfHue);
            _item = new Follow(_tmpHandle,_tmpFollowing,_tmpFollowsMe,_tmpHue);
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
  public Flow<List<Follow>> followers() {
    final String _sql = "SELECT * FROM follows WHERE followsMe = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"follows"}, new Callable<List<Follow>>() {
      @Override
      @NonNull
      public List<Follow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHandle = CursorUtil.getColumnIndexOrThrow(_cursor, "handle");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfFollowsMe = CursorUtil.getColumnIndexOrThrow(_cursor, "followsMe");
          final int _cursorIndexOfHue = CursorUtil.getColumnIndexOrThrow(_cursor, "hue");
          final List<Follow> _result = new ArrayList<Follow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Follow _item;
            final String _tmpHandle;
            _tmpHandle = _cursor.getString(_cursorIndexOfHandle);
            final boolean _tmpFollowing;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp != 0;
            final boolean _tmpFollowsMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfFollowsMe);
            _tmpFollowsMe = _tmp_1 != 0;
            final float _tmpHue;
            _tmpHue = _cursor.getFloat(_cursorIndexOfHue);
            _item = new Follow(_tmpHandle,_tmpFollowing,_tmpFollowsMe,_tmpHue);
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
  public Object byHandle(final String handle, final Continuation<? super Follow> $completion) {
    final String _sql = "SELECT * FROM follows WHERE handle = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, handle);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Follow>() {
      @Override
      @Nullable
      public Follow call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfHandle = CursorUtil.getColumnIndexOrThrow(_cursor, "handle");
          final int _cursorIndexOfFollowing = CursorUtil.getColumnIndexOrThrow(_cursor, "following");
          final int _cursorIndexOfFollowsMe = CursorUtil.getColumnIndexOrThrow(_cursor, "followsMe");
          final int _cursorIndexOfHue = CursorUtil.getColumnIndexOrThrow(_cursor, "hue");
          final Follow _result;
          if (_cursor.moveToFirst()) {
            final String _tmpHandle;
            _tmpHandle = _cursor.getString(_cursorIndexOfHandle);
            final boolean _tmpFollowing;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfFollowing);
            _tmpFollowing = _tmp != 0;
            final boolean _tmpFollowsMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfFollowsMe);
            _tmpFollowsMe = _tmp_1 != 0;
            final float _tmpHue;
            _tmpHue = _cursor.getFloat(_cursorIndexOfHue);
            _result = new Follow(_tmpHandle,_tmpFollowing,_tmpFollowsMe,_tmpHue);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
