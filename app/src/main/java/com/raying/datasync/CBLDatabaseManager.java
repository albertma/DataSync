package com.raying.datasync;

import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.listener.LiteListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by albertma on 21/06/2017.
 */

public class CBLDatabaseManager {


    private Context context;

    private static CBLDatabaseManager INSTANCE = new CBLDatabaseManager();

    private Database mDatabase;

    private String mDatabaseName;

    private Manager mCouchBaseMgr;

    private ThreadPoolExecutor mThreadPoolExecutor;

    private LiteListener mListener;

    private BlockingQueue<Runnable> mWorkQueue;
    private ArrayList<String> peers;

    private CBLDatabaseManager(){
        mWorkQueue = new LinkedBlockingQueue<>(10);
        mThreadPoolExecutor = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, mWorkQueue);
    }

    public static CBLDatabaseManager getInstance(){
        if(INSTANCE == null){
            INSTANCE = new CBLDatabaseManager();
        }
        return INSTANCE;
    }

    public void init(Context context) throws IOException {

        this.context = context;
        mCouchBaseMgr = new Manager(new AndroidContext(context.getApplicationContext()), Manager.DEFAULT_OPTIONS);

    }

    public Database getCurrentDatabase(){
        return this.mDatabase;
    }

    /**
     * If database is exsited, open it, otherwise create a database.
     * @param databaseName
     * @param key
     * @param newKey
     * @param encryptionEnabled
     * @return
     */
    public boolean loadDatabase(String databaseName, String key, String newKey, boolean encryptionEnabled){

        if(this.context == null){
            throw new RuntimeException("CBLDatabaseManager initialization failed");
        }
        this.mDatabaseName = databaseName;

        DatabaseOptions options  = new DatabaseOptions();

        if( encryptionEnabled ){
            options.setEncryptionKey(key);
        }
        options.setCreate(true);

        try {
            this.mDatabase = mCouchBaseMgr.openDatabase(this.mDatabaseName, options);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return false;
        }

        if (newKey != null) {
            try {
                mDatabase.changeEncryptionKey(newKey);
            }catch (CouchbaseLiteException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    public boolean close(){
        if (mDatabase != null && mDatabase.isOpen()){
            return mDatabase.close();
        }else {
            return true;
        }
    }


    public boolean syncPeers(ArrayList<String> peers){
        this.peers = peers;
        return true;
    }


    public boolean stopSyncPeers(){

        return true;
    }


    public QueryEnumerator queryDocument(String queryViewName, final Map<String, Object> queryIndexedKeyValues){

        com.couchbase.lite.View queryView = mDatabase.getView(queryViewName);
        if(queryView.getMap() == null){
            queryView.setMap(new Mapper() {

                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String)document.get("type");
                    String query_type = (String)queryIndexedKeyValues.get("query_type");

                    if (type.equalsIgnoreCase(query_type)){
                        emitter.emit(document.get("content"), null);
                    }
                }
            }, "1.0");
        }

        LiveQuery queryLiveQuery = queryView.createQuery().toLiveQuery();
        QueryEnumerator results = queryLiveQuery.getRows();
        return results;
    }


    public Document createDocument(HashMap<String, Object> properties){

        Database database = CBLDatabaseManager.getInstance().getCurrentDatabase();
        Document document = database.createDocument();
        UnsavedRevision revision = document.createRevision();
        revision.setProperties(properties);

        try{
            revision.save();
            return document;
        } catch (CouchbaseLiteException e){
            e.printStackTrace();
        }
        return  null;
    }



    // TODO: 22/06/2017 need to use thread pool
    private void startWatch() {
        mListener = new LiteListener(mCouchBaseMgr, 5432, null);
        Thread thread = new Thread(mListener);
        thread.start();

    }


    // TODO: 23/06/2017 need to refactor
    private void stopWatch(){
        if(mListener != null){
            mListener.stop();
            mListener = null;
        }
    }




}
