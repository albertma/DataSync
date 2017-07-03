package com.raying.datasync;

import com.couchbase.lite.replicator.Replication;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by albertma on 29/06/2017.
 */

public class SyncJob implements Runnable {

    private final SyncJobListener listener;
    private final String jobId;
    private final ArrayList<String> waitingSyncIds;
    private final URL peerURL;

    public interface SyncJobListener {

        void onBeforeStartJob(String jobId);
        void onDidStartJob(String jobId);

    }

    public SyncJob(URL peerURL, ArrayList<String> waitingSyncIds , SyncJobListener listener){
        this.peerURL = peerURL;
        this.waitingSyncIds = waitingSyncIds;
        this.jobId = String.valueOf(System.currentTimeMillis());
        this.listener = listener;
    }



    @Override
    public void run() {
        if(this.listener != null){
            this.listener.onBeforeStartJob(jobId);
        }
        syncUp(this.waitingSyncIds, peerURL);
        this.listener.onDidStartJob(jobId);
    }


    private boolean syncUp(ArrayList<String> documentIds, URL url){
        Replication push = CBLDatabaseManager.getInstance().getCurrentDatabase().createPullReplication(url);
        push.setDocIds(documentIds);
        push.start();
        return true;
    }


}
