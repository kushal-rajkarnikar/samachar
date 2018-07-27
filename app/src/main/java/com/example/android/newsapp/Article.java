package com.example.android.newsapp;

public class Article {
    private String mCategory;
    private String mUrl;
    private String mPublished;
    private String mTitle;
    private String mSubtitle;
    private String mAuthor;
    private String mThumbnail;

    public Article(String category, String url, String published, String title, String subtitle,
                   String author, String thumbnail){

        mCategory = category;
        mUrl = url;
        mPublished = published;
        mTitle = title;
        mSubtitle = subtitle;
        mAuthor = author;
        mThumbnail = thumbnail;
    }

    public String getCategory(){
        return mCategory;
    }

    public String getUrl(){
        return mUrl;
    }

    public String getPublished(){
        return mPublished;
    }

    public String getTitle(){
        return mTitle;
    }

    public String getSubtitle(){
        return mSubtitle;
    }

    public String getAuthor(){
        return mAuthor;
    }

    public String getThumbnail(){
        return mThumbnail;
    }
}
