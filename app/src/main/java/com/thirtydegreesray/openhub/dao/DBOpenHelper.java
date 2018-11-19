package com.thirtydegreesray.openhub.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ThirtyDegreesRay on 2017/11/13 10:40:22
 */

public class DBOpenHelper extends DaoMaster.DevOpenHelper {

    public DBOpenHelper(Context context, String name) {
        super(context, name);
    }

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        if(oldVersion == 2 && newVersion == 3){
            //create new table, keep ori
            TraceUserDao.createTable(db, false);
            TraceRepoDao.createTable(db, false);
            BookMarkUserDao.createTable(db, false);
            BookMarkRepoDao.createTable(db, false);
        } else if(oldVersion == 3 && newVersion == 4){
            //create new table
            LocalUserDao.createTable(db, false);
            LocalRepoDao.createTable(db, false);
            TraceDao.createTable(db, false);
            BookmarkDao.createTable(db, false);

            //transfer data from ori
            transferBookmarksAndTraceData(db);

            //drop old tables
            TraceUserDao.dropTable(db, true);
            TraceRepoDao.dropTable(db, true);
            BookMarkUserDao.dropTable(db, true);
            BookMarkRepoDao.dropTable(db, true);
        } else if(oldVersion == 4 && newVersion == 5){
            MyTrendingLanguageDao.createTable(db, true);
        } else {
            super.onUpgrade(db, oldVersion, newVersion);
        }
    }

    private void transferBookmarksAndTraceData(Database db){
        DaoSession daoSession = new DaoMaster(db).newSession();
        List<TraceRepo> traceRepoList = daoSession.getTraceRepoDao().loadAll();
        List<TraceUser> traceUserList = daoSession.getTraceUserDao().loadAll();
        List<BookMarkRepo> bookmarkRepoList = daoSession.getBookMarkRepoDao().loadAll();
        List<BookMarkUser> bookMarkUserList = daoSession.getBookMarkUserDao().loadAll();

        List<LocalRepo> localRepoList = getLocalRepoList(traceRepoList, bookmarkRepoList);
        List<LocalUser> localUserList = getLocalUserList(traceUserList, bookMarkUserList);
        List<Trace> traceList = getTraceList(traceRepoList, traceUserList);
        List<Bookmark> bookmarkList = getBookmarkList(bookmarkRepoList, bookMarkUserList);

        daoSession.getLocalRepoDao().insertInTx(localRepoList);
        daoSession.getLocalUserDao().insertInTx(localUserList);
        daoSession.getTraceDao().insertInTx(traceList);
        daoSession.getBookmarkDao().insertInTx(bookmarkList);

        daoSession.clear();
    }

    private List<LocalRepo> getLocalRepoList(List<TraceRepo> traceRepoList, List<BookMarkRepo> bookmarkRepoList){
        List<LocalRepo> localRepoList = new ArrayList<>();
        List<Long> repoIdList = new ArrayList<>();
        LocalRepo localRepo;
        int tam = traceRepoList.size();
        TraceRepo repo;
        for(int i = 0; i< tam; i++){
            repo = traceRepoList.get(i);
            if(!repoIdList.contains(repo.getId())){
                localRepo = new LocalRepo();
                localRepo.setId(repo.getId());
                localRepo.setDescription(repo.getDescription());
                localRepo.setFork(repo.getFork());
                localRepo.setForksCount(repo.getForksCount());
                localRepo.setLanguage(repo.getLanguage());
                localRepo.setName(repo.getName());
                localRepo.setOwnerAvatarUrl(repo.getOwnerAvatarUrl());
                localRepo.setOwnerLogin(repo.getOwnerLogin());
                localRepo.setStargazersCount(repo.getStargazersCount());
                localRepo.setWatchersCount(repo.getWatchersCount());
                localRepoList.add(localRepo);
                repoIdList.add(repo.getId());
            }
        }
        BookMarkRepo repoB;
        tam = bookmarkRepoList.size();
        for(int i = 0; i<tam;i++ ){
            repoB = bookmarkRepoList.get(i);
            if(!repoIdList.contains(repoB.getId())){
                localRepo = new LocalRepo();
                localRepo.setId(repoB.getId());
                localRepo.setDescription(repoB.getDescription());
                localRepo.setFork(repoB.getFork());
                localRepo.setForksCount(repoB.getForksCount());
                localRepo.setLanguage(repoB.getLanguage());
                localRepo.setName(repoB.getName());
                localRepo.setOwnerAvatarUrl(repoB.getOwnerAvatarUrl());
                localRepo.setOwnerLogin(repoB.getOwnerLogin());
                localRepo.setStargazersCount(repoB.getStargazersCount());
                localRepo.setWatchersCount(repoB.getWatchersCount());
                localRepoList.add(localRepo);
                repoIdList.add(repoB.getId());
            }
        }
        return localRepoList;
    }

    private List<LocalUser> getLocalUserList(List<TraceUser> traceUserList, List<BookMarkUser> bookMarkUserList){
        List<LocalUser> localUserList = new ArrayList<>();
        List<String> userIdList = new ArrayList<>();
        TraceUser user;
        int tam = traceUserList.size();
        LocalUser localUser;
        for(int i=0; i< tam;i++){
            user  = traceUserList.get(i);
            if(!userIdList.contains(user.getLogin())){
                localUser = new LocalUser();
                localUser.setLogin(user.getLogin());
                localUser.setAvatarUrl(user.getAvatarUrl());
                localUser.setFollowers(user.getFollowers());
                localUser.setFollowing(user.getFollowing());
                localUser.setName(user.getName());
                localUserList.add(localUser);
                userIdList.add(user.getLogin());
            }
        }
        BookMarkUser userB;
        tam = bookMarkUserList.size();
        for(int i = 0; i< tam;i++){
            userB = bookMarkUserList.get(i);
            if(!userIdList.contains(userB.getLogin())){
                localUser = new LocalUser();
                localUser.setLogin(userB.getLogin());
                localUser.setAvatarUrl(userB.getAvatarUrl());
                localUser.setFollowers(userB.getFollowers());
                localUser.setFollowing(userB.getFollowing());
                localUser.setName(userB.getName());
                localUserList.add(localUser);
                userIdList.add(userB.getLogin());
            }
        }
        return localUserList;
    }

    private List<Trace> getTraceList(List<TraceRepo> traceRepoList, List<TraceUser> traceUserList){
        List<Trace> traceList = new ArrayList<>();
        TraceRepo oriTrace;
        Trace trace;
        int tam = traceRepoList.size();
        for(int i =0 ; i<tam;i++){
            oriTrace = traceRepoList.get(i);
            trace = new Trace(UUID.randomUUID().toString());
            trace.setType("repo");
            trace.setRepoId(oriTrace.getId());
            trace.setStartTime(oriTrace.getStartTime());
            trace.setLatestTime(oriTrace.getLatestTime());
            trace.setTraceNum(oriTrace.getTraceNum());
            traceList.add(trace);
        }
        TraceUser oriTraceUser;
        tam = traceUserList.size();
        for(int i = 0; i< tam;i++){
            oriTraceUser = traceUserList.get(i);
            trace = new Trace(UUID.randomUUID().toString());
            trace.setType("user");
            trace.setUserId(oriTraceUser.getLogin());
            trace.setStartTime(oriTraceUser.getStartTime());
            trace.setLatestTime(oriTraceUser.getLatestTime());
            trace.setTraceNum(oriTraceUser.getTraceNum());
            traceList.add(trace);
        }
        return traceList;
    }

    private List<Bookmark> getBookmarkList(List<BookMarkRepo> bookmarkRepoList, List<BookMarkUser> bookMarkUserList){
        List<Bookmark> bookmarkList = new ArrayList<>();
        Bookmark bookmark;
        int tam = bookmarkRepoList.size();

        for(int i = 0; i < tam; i++) {
            bookmark = new Bookmark(UUID.randomUUID().toString());
            bookmark.setType("repo");
            bookmark.setRepoId(bookmarkRepoList.get(i).getId());
            bookmark.setMarkTime(bookmarkRepoList.get(i).getMarkTime());
            bookmarkList.add(bookmark);
        }
        tam = bookMarkUserList.size();
        for(int i = 0; i < tam; i++){
            bookmark = new Bookmark(UUID.randomUUID().toString());
            bookmark.setType("user");
            bookmark.setUserId(bookMarkUserList.get(i).getLogin());
            bookmark.setMarkTime(bookMarkUserList.get(i).getMarkTime());
            bookmarkList.add(bookmark);
        }
        return bookmarkList;
    }

}
