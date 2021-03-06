package com.myexample.bookmanagement;
/*2つのtabをコントロールするlistenerのクラス*/
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.util.Log;

public class MyTabListener<T extends Fragment> implements ActionBar.TabListener {
    private Fragment mFragment;
    private Activity mActivity;
    private String mTag;
    private Class<T> mClass;

    //コンストラクタ
    public MyTabListener(Activity activity, String tag, Class<T> clz) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
        mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
        Log.d("Listener",""+mTag);
    }

    //タブが選択されたとき
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

        if (mFragment == null) {
            mFragment = Fragment.instantiate(mActivity, mClass.getName());
            android.app.FragmentManager fm = mActivity.getFragmentManager();
            fm.beginTransaction().add(R.id.container, mFragment, mTag).commit();
        } else {
            if (mFragment.isDetached()) {
                android.app.FragmentManager fm = mActivity.getFragmentManager();
                fm.beginTransaction().attach(mFragment).commit();
            }
        }
    }

    //タブの選択が解除されたとき
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (mFragment != null) {
            android.app.FragmentManager fm = mActivity.getFragmentManager();
            fm.beginTransaction().detach(mFragment).commit();
        }
    }

    //選択されたタブが選択されたとき
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        Log.d("tabListener", "reselected");
    }
}