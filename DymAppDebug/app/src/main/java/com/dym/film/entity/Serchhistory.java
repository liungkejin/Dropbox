package com.dym.film.entity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "SERCHHISTORY".
 */
public class Serchhistory implements java.io.Serializable {

    private Long id;
    /** Not-null value. */
    private String movie_id;
    /** Not-null value. */
    private String movie_name;

    public Serchhistory() {
    }

    public Serchhistory(Long id) {
        this.id = id;
    }

    public Serchhistory(Long id, String movie_id, String movie_name) {
        this.id = id;
        this.movie_id = movie_id;
        this.movie_name = movie_name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getMovie_id() {
        return movie_id;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMovie_id(String movie_id) {
        this.movie_id = movie_id;
    }

    /** Not-null value. */
    public String getMovie_name() {
        return movie_name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setMovie_name(String movie_name) {
        this.movie_name = movie_name;
    }

}
