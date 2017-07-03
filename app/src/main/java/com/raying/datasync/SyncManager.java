package com.raying.datasync;

import com.couchbase.lite.replicator.Replication;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by albertma on 29/06/2017.
 */

public class SyncManager {

    protected ArrayList<URL> syncUpList = new ArrayList();

    protected HashMap<URL, Replication> pushMap = new HashMap<URL, Replication>();

    private ArrayList<SyncJob> jobs = new ArrayList<>();

    private static SyncManager INSTANCE = new SyncManager();

    private boolean isStart;


    private SyncManager(){

    }

    public static SyncManager getInstance(){
        return INSTANCE;
    }

    public void addURL(String sURL){
        for(URL currURL: syncUpList){
            String urlString = currURL.toString();
            if(sURL.equalsIgnoreCase(urlString)){
                System.err.println("URL is duplicated:" + sURL);
                return;
            }
        }
        URL url = null;
        try {
            url = new URL(sURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.syncUpList.add(url);
    }

    public void startSync(){
        this.isStart = true;
    }

    public void stopSync(){
        this.isStart = false;
    }

    private void addSyncJob(SyncJob job){
        jobs.add(job);
    }


    class SyncThread extends Thread{


        public void run(){

            while (isStart){


                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
