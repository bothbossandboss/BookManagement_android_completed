package com.myexample.bookmanagement;
/*
 * アカウント入力画面のactivity
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountViewActivity extends Activity {
    private EditText mailAddressEditText;
    private EditText passwordEditText;
    private EditText confirmEditText;
    private RequestQueue mQueue;
    /**
     * ! 使ってない変数は削除しよう
     * 多分IDEが教えてくれてるはず
     */
    private JSONObject params;
    private String inputMailAddress;
    private String inputPassword;

    /*
     *method of activity's life cycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.account_view);
        mailAddressEditText = (EditText)findViewById(R.id.mailAddressEditText);
        passwordEditText = (EditText)findViewById(R.id.passwordEditText);
        confirmEditText = (EditText)findViewById(R.id.confirmEditText);
        controlKeyboard(mailAddressEditText);
        controlKeyboard(passwordEditText);
        controlKeyboard(confirmEditText);
        //Intent intent = getIntent();
        mQueue = Volley.newRequestQueue(this);
    }

    public void onStart()
    {
        super.onStart();
        Button saveAccountButton = (Button)findViewById(R.id.saveAccountButton);
        saveAccountButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpannableStringBuilder sb = (SpannableStringBuilder)mailAddressEditText.getText();
                String saveMailAddress = sb.toString();
                sb = (SpannableStringBuilder)passwordEditText.getText();
                String savePassword = sb.toString();
                Log.d("mail",saveMailAddress);
                if(!saveMailAddress.isEmpty() && !savePassword.isEmpty())
                {
                    sb = (SpannableStringBuilder)confirmEditText.getText();
                    String saveConfirm = sb.toString();
                    if(savePassword.equals(saveConfirm))
                    {
                        Log.d("password","OK");
                        try {
                            registerAccount(saveMailAddress, savePassword);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        /**
                         * !!!! メッセージ文言の類はリソース(R.string)として管理したほうがすっきりします
                         */
                        String msg = "パスワードが違います";
                        showAlert(msg);
                    }
                }else{
                    String msg = "メールアドレスとパスワードを設定してください";
                    showAlert(msg);
                }
            }
        });
    }

    /*
     * private method
     */
    private void controlKeyboard(final EditText ed)
    {
        ed.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // フォーカスを受け取ったとき
                if(hasFocus){
                    // ソフトキーボードを表示する
                    inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                }
                // フォーカスが外れたとき
                else{
                    // ソフトキーボードを閉じる
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);
                }
            }
        });
    }

    private void showAlert(String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg).setPositiveButton("OK", null);
        builder.show();
    }

    /**
     * !! 何をfinishするかよくわからないのと個人的にFinishIntentみたいな命名の方が好きです
     */
    private void makeIntentToFinish(String user_id, String saveMailAddress, String savePassword)
    {
        Intent intent = new Intent();
        intent.putExtra("userId", user_id);
        intent.putExtra("mailAddress",saveMailAddress);
        intent.putExtra("password",savePassword);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /*
     * method to access Database
     */
    private void registerAccount(String mailAddress, String password) throws JSONException
    {
        /**
         * !!!! これもローカル変数にできそうです
         */
        inputMailAddress = mailAddress;
        inputPassword = password;
        final Gson gson = new Gson();
        String method = "account/regist";
        RequestData requestData = new RequestData(method,mailAddress,password);
        JSONObject ob = new JSONObject(gson.toJson(requestData));
        String url = MyConstants.REGISTER_ACCOUNT_URL;
        //JsonObjectRequestは、(POST/GET, url, request, response, error)の感じ。
        JsonObjectRequest  request = new JsonObjectRequest(Method.POST, url, ob,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        responseAccountData returnData = gson.fromJson(response.toString(), responseAccountData.class);
                        if(returnData.getStatus().equals("ng"))
                        {
                            //既にアカウントがある場合、ログインへ移行。
                            Log.d("register",returnData.getError());
                            try {
                                loginAccount();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            String user_id = returnData.getData().getUserId();
                            makeIntentToFinish(user_id, inputMailAddress, inputPassword);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) {
                Log.d("account register",""+error);
            }}
        );
        int socketTimeout = 300;//0.3 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mQueue.add(request);
        mQueue.start();
    }

    private void loginAccount() throws JSONException
    {
        final Gson gson = new Gson();
        String method = "account/login";
        RequestData requestData = new RequestData(method,inputMailAddress,inputPassword);
        JSONObject ob = new JSONObject(gson.toJson(requestData));
        String url = MyConstants.LOGIN_ACCOUNT_URL;
        //JsonObjectRequestは、(POST/GET, url, request, response, error)の感じ。
        JsonObjectRequest  request = new JsonObjectRequest(Method.POST, url, ob,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response login", ""+response.toString());
                        responseAccountData returnData = gson.fromJson(response.toString(), responseAccountData.class);
                        if(returnData.getStatus().equals("ng"))
                        {
                            Log.d("login",returnData.getError());
                            String msg = "アカウントもしくはパスワードが違います";
                            showAlert(msg);
                        }else{
                            //設定画面へ。
                            String user_id = returnData.getData().getUserId();
                            makeIntentToFinish(user_id, inputMailAddress, inputPassword);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) {
                Log.d("account register","error"+error);
            }}
        );
        int socketTimeout = 300;//0.3 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mQueue.add(request);
        mQueue.start();
    }

    /*
     * class to access database
     */
    private class responseAccountData
    {
        private String status;
        private String error;
        private Account data;
        public responseAccountData(String status,String error, Account data)
        {
            this.status = status;
            this.error = error;
            this.data = data;
        }

        public String getStatus()
        {
            return status;
        }

        public String getError()
        {
            return error;
        }

        public Account getData()
        {
            return data;
        }
    }

    private class MailAndPassword
    {
        private String mail_address;
        private String password;

        public MailAndPassword(String mMail, String mPass)
        {
            this.mail_address = mMail;
            this.password = mPass;
        }
    }

    private class RequestData
    {
        private String method;
        private MailAndPassword params;

        public RequestData(String mMethod, String mail, String pass)
        {
            this.method = mMethod;
            this.params = new MailAndPassword(mail, pass);
        }
    }



}
