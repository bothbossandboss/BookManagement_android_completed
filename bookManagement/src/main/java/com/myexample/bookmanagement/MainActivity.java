package com.myexample.bookmanagement;
/*
 * 最初に呼ばれるactivityで2つのtabを持つ
 */
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    //#define文の代わりに以下のように定義する。class外から参照する場合はclass名.~で参照。
    public static final  int REQUEST_CODE_SAVE = 10001;
    public static final  int REQUEST_CODE_ADD = 10002;
    public static final  int REQUEST_CODE_SELECT_IMAGE = 10003;
    public static final  int REQUEST_CODE_SAVE_ACCOUNT = 20001;
    private static final String TAG = "LifeCycleMain";

    /*
     * method of activity's life cycle
     */
    //onCreateはアクティビティが開始される時に呼ばれるメソッド。
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyTabListener<ListViewFragment> listListener = new MyTabListener<ListViewFragment>
                (this, "tab1",ListViewFragment.class);
        MyTabListener<PropertyViewFragment> propertyListener = new MyTabListener<PropertyViewFragment>
                (this, "tag2",PropertyViewFragment.class);
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // タブを作成して追加。
        //タブの選択・解除・再選択をハンドリングするコールバックの TabListener をセットしないと実行時例外でクラッシュする。
        actionBar.addTab(actionBar.newTab().setText("書籍一覧").setTabListener(listListener));
        actionBar.addTab(actionBar.newTab().setText("設定").setTabListener(propertyListener));
        //アカウント設定のタブへ切り替え。
        actionBar.getTabAt(1).select();
        SharedPreferences prefs = getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("state","first");
        editor.apply();
    }
}