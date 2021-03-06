package com.dym.film.entity;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig serchhistoryDaoConfig;
    private final DaoConfig cinemaDaoConfig;
    private final DaoConfig attentionAuthorDaoConfig;
    private final DaoConfig filmReviewDaoConfig;

    private final cinemaDao cinemaDao;
    private final SerchhistoryDao serchhistoryDao;
    private final AttentionAuthorDao attentionAuthorDao;
    private final FilmReviewDao filmReviewDao;




    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        serchhistoryDaoConfig = daoConfigMap.get(SerchhistoryDao.class).clone();
        serchhistoryDaoConfig.initIdentityScope(type);

        cinemaDaoConfig = daoConfigMap.get(cinemaDao.class).clone();
        cinemaDaoConfig.initIdentityScope(type);


        attentionAuthorDaoConfig = daoConfigMap.get(AttentionAuthorDao.class).clone();
        attentionAuthorDaoConfig.initIdentityScope(type);

        filmReviewDaoConfig = daoConfigMap.get(FilmReviewDao.class).clone();
        filmReviewDaoConfig.initIdentityScope(type);

        attentionAuthorDao = new AttentionAuthorDao(attentionAuthorDaoConfig, this);
        filmReviewDao = new FilmReviewDao(filmReviewDaoConfig, this);
        serchhistoryDao = new SerchhistoryDao(serchhistoryDaoConfig, this);
        cinemaDao = new cinemaDao(cinemaDaoConfig, this);

        registerDao(Serchhistory.class, serchhistoryDao);
        registerDao(cinema.class, cinemaDao);
        registerDao(AttentionAuthor.class, attentionAuthorDao);
        registerDao(FilmReview.class, filmReviewDao);
    }
    
    public void clear() {
        serchhistoryDaoConfig.getIdentityScope().clear();
        cinemaDaoConfig.getIdentityScope().clear();
        attentionAuthorDaoConfig.getIdentityScope().clear();
        filmReviewDaoConfig.getIdentityScope().clear();
    }

    public cinemaDao getCinemaDao() {
        return cinemaDao;
    }

    public SerchhistoryDao getSerchhistoryDao() {
        return serchhistoryDao;
    }

    public AttentionAuthorDao getAttentionAuthorDao() {
        return attentionAuthorDao;
    }

    public FilmReviewDao getFilmReviewDao() {
        return filmReviewDao;
    }


}
