package com.xp.legend.lin17.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xp.legend.lin17.bean.Result;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {


    private static volatile Database database;
    private static final String DATABASE="lin17";
    private static final int VERSION=1;
    private SQLiteDatabase sqLiteDatabase;

    private static final String LIN_TABLE="CREATE TABLE IF NOT EXISTS lin (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "shuFile TEXT," +
            "shuHeaderFile TEXT," +
            "hengFile TEXT," +
            "hengHeaderFile TEXT," +
            "alpha INTEGER," +
            "quality INTEGER," +
            "gao INTEGER," +
            "gaoValue INTEGER," +
            "del INTEGER" +
            ")";


    public static Database getDefault(Context context){

        if (database==null){
            synchronized (Database.class){
                database=new Database(context,DATABASE,null,VERSION);
            }
        }
        return database;
    }




    public Database(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        sqLiteDatabase=getReadableDatabase();
    }

    public Database(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    public Database(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(LIN_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    /**
     * 增加一个
     *
     */
    public void addLin(Result result){

        String sql="insert into lin (name,shuFile,shuHeaderFile,hengFile,hengHeaderFile,alpha,quality,gao,gaoValue,del) " +
                "values ('"+result.getName()+"','" +result.getShuFile()+
                "'" +
                ",'" +result.getShuHeaderFile()+
                "','" +result.getHengFile()+
                "','" +result.getHengHeaderFile()+
                "'," +result.getAlpha()+
                "," +result.getQuality()+
                "," +result.getGao()+
                "," +result.getGaoValue()+
                "," +result.getDel()+
                ")";


        sqLiteDatabase.execSQL(sql);

        String last="select last_insert_rowid() from lin";

        Cursor cursor=sqLiteDatabase.rawQuery(last,null);

        if (cursor.moveToFirst()){

            int id=cursor.getInt(0);
            result.setId(id);
        }

        cursor.close();

    }


    /**
     * 更新
     * @param result
     */
    public void update(Result result){

        String sql="update lin set name='"+result.getName()+"'," +
                "shuFile='" +result.getShuFile()+
                "',shuHeaderFile='" +result.getShuHeaderFile()+
                "',hengFile='" +result.getHengFile()+
                "',hengHeaderFile='" +result.getHengHeaderFile()+
                "',alpha=" +result.getAlpha()+
                ",quality=" +result.getQuality()+
                ",gao=" +result.getGao()+
                ",gaoValue=" +result.getGaoValue()+
                ",del=" +result.getDel()+
                " where id = "+result.getId()+"";


        sqLiteDatabase.execSQL(sql);

    }


    /**
     * 标记删除
     * @param result
     */
    public void tagDelete(Result result){

        String s="update lin set del = "+result.getDel()+"";

        sqLiteDatabase.execSQL(s);

    }


    /**
     * 真正意义上的删除，同时需要删除文件
     * @param result
     */
    public void trueDelete(Result result){

        String d="delete from lin where id = "+result.getId()+"";
        sqLiteDatabase.execSQL(d);

    }


    /**
     * 获取信息
     */
    public List<Result> getLins(int tag){

        List<Result> resultList=new ArrayList<>();

        String s="select * from lin where del = "+tag;

        Cursor cursor=sqLiteDatabase.rawQuery(s,null);

        if (cursor!=null){
            if (cursor.moveToFirst()){

                do {

                    Result r=new Result();

                    r.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    r.setAlpha(cursor.getInt(cursor.getColumnIndex("alpha")));
                    r.setGao(cursor.getInt(cursor.getColumnIndex("gao")));
                    r.setGaoValue(cursor.getInt(cursor.getColumnIndex("gaoValue")));
                    r.setDel(cursor.getInt(cursor.getColumnIndex("del")));
                    r.setShuFile(cursor.getString(cursor.getColumnIndex("shuFile")));
                    r.setShuHeaderFile(cursor.getString(cursor.getColumnIndex("shuHeaderFile")));
                    r.setHengFile(cursor.getString(cursor.getColumnIndex("hengFile")));
                    r.setHengHeaderFile(cursor.getString(cursor.getColumnIndex("hengHeaderFile")));
                    r.setQuality(cursor.getInt(cursor.getColumnIndex("quality")));
                    r.setName(cursor.getString(cursor.getColumnIndex("name")));

                    resultList.add(r);
                }while (cursor.moveToNext());

            }

            cursor.close();

        }

        return resultList;

    }




}
