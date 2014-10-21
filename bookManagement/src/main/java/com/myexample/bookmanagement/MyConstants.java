package com.myexample.bookmanagement;

/*
 * 定数を集めたクラス
 * Created by ohsugiyasuhito on 2014/10/02.
 */
public class MyConstants {
    //#define文の代わりに以下のように定義する。class外から参照する場合はclass名.~で参照。

    //画面遷移におけるID
    public static final  int REQUEST_CODE_SAVE = 10001;
    public static final  int REQUEST_CODE_ADD = 10002;
    public static final  int REQUEST_CODE_SELECT_IMAGE = 10003;
    public static final  int REQUEST_CODE_SAVE_ACCOUNT = 20001;

    //画像の縮尺
    public static final  int IMAGE_VIEW_WIDTH = 200;
    public static final  int IMAGE_VIEW_HEIGHT = 200;

    //書籍一覧を取得する際のデフォルトの開始点と取得冊数
    public static final  int FIRST_BEGIN_PAGE = 28;
    public static final  int NUM_OF_PAGE = 10;

    //ライフサイクルを分かりやすくするためのタグ
    public static final String DETAIL_TAG = "LifeCycleDetail";
    public static final String LIST_TAG = "LifeCycleList";
    public static final String MAIN_TAG = "LifeCycleMain";
    public static final String PROPERTY_TAG = "LifeCycleProperty";

    //私のパソコンのIPアドレス
    public static final String IP_ADDRESS = "10.0.1.44";
    //自宅環境
    //public static final String IP_ADDRESS = "192.168.0.8";


    //データベースとのやり取りにおけるURL
    public static final String GET_BOOK_URL = "http://"+IP_ADDRESS+":8888/cakephp/book/get";
    public static final String REGISTER_BOOK_URL = "http://"+IP_ADDRESS+":8888/cakephp/book/regist";
    public static final String UPDATE_BOOK_URL = "http://"+IP_ADDRESS+":8888/cakephp/book/update";
    public static final String REGISTER_ACCOUNT_URL = "http://"+IP_ADDRESS+":8888/cakephp/account/regist";
    public static final String LOGIN_ACCOUNT_URL = "http://"+IP_ADDRESS+":8888/cakephp/account/login";
    public static final String UPLOAD_IMAGE_URL = "http://"+IP_ADDRESS+":8888/cakephp/book/upload_image";
}
