package com.myexample.bookmanagement;
/*
 * 2番目のtabであるアカウント設定画面のfragment
 */

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PropertyViewFragment extends Fragment {
    private Button setAccountButton;

    /*
     * method of fragment's life cycle
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(MyConstants.PROPERTY_TAG, "onCreate");
        View view = inflater.inflate(R.layout.property_view, container, false);
        super.onCreateView(inflater, container, savedInstanceState);
        setAccountButton = (Button) view.findViewById(R.id.setAccountButton);
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String state = prefs.getString("state", "");
        if (state.equals("first")) {
            Log.d("state", "" + state);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("state", "active");
            editor.apply();
            setAccountButtonTapped();
        }
        return view;
    }

    @Override
    public void onStart() {
        Log.d(MyConstants.PROPERTY_TAG, "onStart");
        super.onStart();
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String state = prefs.getString("state", "");
        Log.d("state", "" + state);
        setAccountButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    setAccountButtonTapped();
                                                }
                                            }
        );
    }

    /*
     * method to move to view of account
     * */
    private void setAccountButtonTapped() {
        //アカウント設定ボタンが押された時の動作
        Intent intent = new Intent(getActivity(), AccountViewActivity.class);
        int requestCode = MyConstants.REQUEST_CODE_SAVE_ACCOUNT;
        startActivityForResult(intent, requestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == MyConstants.REQUEST_CODE_SAVE_ACCOUNT && resultCode == Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, intent);
            String user_id = intent.getStringExtra("userId");
            String saveMailAddress = intent.getStringExtra("mailAddress");
            String savePassword = intent.getStringExtra("password");
            Log.d("saveAccount", "" + user_id);
            SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_id", user_id);
            editor.putString("mail_address", saveMailAddress);
            editor.putString("password", savePassword);
            editor.apply();
        }
    }
}
