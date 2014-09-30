package com.myexample.bookmanagement;
/*
 *Listに表示する要素をまとめたクラス
 */
public class ListViewItem
{
    private int resourceID;
    private String bookName;
    private String price;
    private String date;
    //画像はとりあえず考慮しない
    public ListViewItem(int constructorID, String constructorBookName, String constructorPrice, String constructorDate)
    {
        this.resourceID = constructorID;
        this.bookName = constructorBookName;
        this.price = constructorPrice;
        this.date = constructorDate;
    }

    public int getResourceID()
    {
        return resourceID;
    }
    public String getBookName()
    {
        return bookName;
    }
    public String getPrice()
    {
        return price;
    }
    public String getDate()
    {
        return date;
    }
}

