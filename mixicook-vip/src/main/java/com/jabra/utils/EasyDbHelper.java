package com.jabra.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class EasyDbHelper
        extends SQLiteOpenHelper {
    public EasyDbHelper(Context paramContext, String paramString, int paramInt) {
        super(paramContext, paramString, null, paramInt);
    }

    /* Error */
    public <T> T agentRead(Reader<T> paramReader) {
        // Byte code:
        //   0: aload_0
        //   1: monitorenter
        //   2: aconst_null
        //   3: astore_2
        //   4: aload_0
        //   5: invokevirtual 17	com/easy/db/EasyDbHelper:getReadableDatabase	()Landroid/database/sqlite/SQLiteDatabase;
        //   8: astore 4
        //   10: aload 4
        //   12: invokevirtual 23	android/database/sqlite/SQLiteDatabase:beginTransaction	()V
        //   15: aload_1
        //   16: aload 4
        //   18: invokeinterface 29 2 0
        //   23: astore_2
        //   24: aload 4
        //   26: invokevirtual 32	android/database/sqlite/SQLiteDatabase:setTransactionSuccessful	()V
        //   29: aload 4
        //   31: invokevirtual 35	android/database/sqlite/SQLiteDatabase:endTransaction	()V
        //   34: aload 4
        //   36: invokevirtual 38	android/database/sqlite/SQLiteDatabase:close	()V
        //   39: aload_0
        //   40: monitorexit
        //   41: aload_2
        //   42: areturn
        //   43: astore 6
        //   45: aload 6
        //   47: invokevirtual 41	android/database/SQLException:printStackTrace	()V
        //   50: aload 4
        //   52: invokevirtual 35	android/database/sqlite/SQLiteDatabase:endTransaction	()V
        //   55: aload 4
        //   57: invokevirtual 38	android/database/sqlite/SQLiteDatabase:close	()V
        //   60: goto -21 -> 39
        //   63: astore_3
        //   64: aload_0
        //   65: monitorexit
        //   66: aload_3
        //   67: athrow
        //   68: astore 5
        //   70: aload 4
        //   72: invokevirtual 35	android/database/sqlite/SQLiteDatabase:endTransaction	()V
        //   75: aload 4
        //   77: invokevirtual 38	android/database/sqlite/SQLiteDatabase:close	()V
        //   80: aload 5
        //   82: athrow
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	83	0	this	EasyDbHelper
        //   0	83	1	paramReader	Reader<T>
        //   3	39	2	localObject1	Object
        //   63	4	3	localObject2	Object
        //   8	68	4	localSQLiteDatabase	SQLiteDatabase
        //   68	13	5	localObject3	Object
        //   43	3	6	localSQLException	android.database.SQLException
        // Exception table:
        //   from	to	target	type
        //   15	29	43	android/database/SQLException
        //   4	15	63	finally
        //   29	39	63	finally
        //   39	41	63	finally
        //   50	60	63	finally
        //   64	66	63	finally
        //   70	83	63	finally
        //   15	29	68	finally
        //   45	50	68	finally
        return (T) paramReader;
    }

    /* Error */
    public Writer agentWrite(Writer paramWriter) {
        // Byte code:
        //   0: aload_0
        //   1: monitorenter
        //   2: aload_0
        //   3: invokevirtual 46	com/easy/db/EasyDbHelper:getWritableDatabase	()Landroid/database/sqlite/SQLiteDatabase;
        //   6: astore_3
        //   7: aload_3
        //   8: invokevirtual 23	android/database/sqlite/SQLiteDatabase:beginTransaction	()V
        //   11: aload_1
        //   12: aload_3
        //   13: invokeinterface 52 2 0
        //   18: aload_3
        //   19: invokevirtual 32	android/database/sqlite/SQLiteDatabase:setTransactionSuccessful	()V
        //   22: iconst_1
        //   23: istore 6
        //   25: aload_3
        //   26: invokevirtual 35	android/database/sqlite/SQLiteDatabase:endTransaction	()V
        //   29: aload_3
        //   30: invokevirtual 38	android/database/sqlite/SQLiteDatabase:close	()V
        //   33: aload_0
        //   34: monitorexit
        //   35: iload 6
        //   37: ireturn
        //   38: astore 5
        //   40: aload 5
        //   42: invokevirtual 41	android/database/SQLException:printStackTrace	()V
        //   45: aload_3
        //   46: invokevirtual 35	android/database/sqlite/SQLiteDatabase:endTransaction	()V
        //   49: aload_3
        //   50: invokevirtual 38	android/database/sqlite/SQLiteDatabase:close	()V
        //   53: iconst_0
        //   54: istore 6
        //   56: goto -23 -> 33
        //   59: astore_2
        //   60: aload_0
        //   61: monitorexit
        //   62: aload_2
        //   63: athrow
        //   64: astore 4
        //   66: aload_3
        //   67: invokevirtual 35	android/database/sqlite/SQLiteDatabase:endTransaction	()V
        //   70: aload_3
        //   71: invokevirtual 38	android/database/sqlite/SQLiteDatabase:close	()V
        //   74: aload 4
        //   76: athrow
        // Local variable table:
        //   start	length	slot	name	signature
        //   0	77	0	this	EasyDbHelper
        //   0	77	1	paramWriter	Writer
        //   59	4	2	localObject1	Object
        //   6	65	3	localSQLiteDatabase	SQLiteDatabase
        //   64	11	4	localObject2	Object
        //   38	3	5	localSQLException	android.database.SQLException
        //   23	32	6	bool	boolean
        // Exception table:
        //   from	to	target	type
        //   11	22	38	android/database/SQLException
        //   2	11	59	finally
        //   25	33	59	finally
        //   33	35	59	finally
        //   45	53	59	finally
        //   60	62	59	finally
        //   66	77	59	finally
        //   11	22	64	finally
        //   40	45	64	finally
        return paramWriter;
    }

    public static abstract interface Reader<T> {
        public abstract T doRead(SQLiteDatabase paramSQLiteDatabase);
    }

    public static abstract interface Writer {
        public abstract void doWrite(SQLiteDatabase paramSQLiteDatabase);
    }
}
