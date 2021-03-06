package com.myexample.bookmanagement;
/*
 * 1番目のtabである書籍一覧画面のfragment
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

import java.util.ArrayList;
import java.util.List;

public class ListViewFragment extends Fragment {
    private List<ListViewItem> list;
    private ListView listView;
    private RequestQueue mQueue;
    private CustomListItemAdapter adapter;
    private int tmpID;
    private String tmpTitle;
    private String tmpPrice;
    private String tmpDate;
    private String tmpUri;
    private int tmpIndex;
    private String isThisFirstGet;

    /*
     * method of fragment's life cycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getActivity());
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        Log.d("prefs", "" + prefs.getString("user_id", ""));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(MyConstants.LIST_TAG, "onCreate");
        //ListViewに表示させる要素を作成する。
        list = new ArrayList<ListViewItem>();
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.list_view, container, false);
        setHasOptionsMenu(true);
        //ListViewの取得
        listView = (ListView) view.findViewById(R.id.ListView);
        //ListViewは一覧の中身を管理できないので、Adapterをバインドさせ管理する。
        adapter = new CustomListItemAdapter(getActivity(), 0, list);
        listView.setAdapter(adapter);
        try {
            getBooksVolley("latest", MyConstants.FIRST_BEGIN_PAGE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("list", "" + list.size());
        //ListViewの要素(cell)がタップされた時の処理。
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                ListViewItem item = (ListViewItem) listView.getItemAtPosition(position);
                int ID = item.getResourceID();
                String title = item.getBookName();
                String price = item.getPrice();
                String date = item.getDate();
                Log.d("tag", "ID:" + ID);
                Log.d("name:%s", title);
                Log.d("price:%s", price);
                Log.d("date:%s", date);
                //DetailViewに遷移するためのインテントを作成する。
                Intent intent = new Intent(getActivity(), DetailViewActivity.class);
                intent.putExtra("resourceID", ID);
                intent.putExtra("bookName", title);
                intent.putExtra("price", price);
                intent.putExtra("date", date);
                intent.putExtra("imageUri",item.getImageUri());
                intent.putExtra("position", position);
                Log.d("intent", "intent作成完了 ");
                //遷移先から返却される識別コードを指定することで返却値を反映できる。
                int requestCode = MyConstants.REQUEST_CODE_SAVE;
                startActivityForResult(intent, requestCode);
            }
        });
    }

    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg).setPositiveButton("OK", null);
        builder.show();
    }

    /*
     * method to control action bar
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_action_bar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //追加ボタンが押された時のメソッド
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addButton:
                Log.d("addButton", "tapped");
                Intent intent = new Intent(getActivity(), AddDataActivity.class);
                intent.putExtra("resourceID", list.size());
                intent.putExtra("imageUri","no image");
                int requestCode = MyConstants.REQUEST_CODE_ADD;
                startActivityForResult(intent, requestCode);
                return true;
            case R.id.reloadButton:
                Log.d("reloadButton", "tapped");
                reloadMoreData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void reloadMoreData() {
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        int numOfBooks = prefs.getInt("numOfBooks", 0);
        int beginPage = numOfBooks - list.size();
        try {
            getBooksVolley("old", beginPage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //編集して保存する場合。
        if (requestCode == MyConstants.REQUEST_CODE_SAVE && resultCode == Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, intent);
            int resourceID = intent.getIntExtra("ID", 0);
            int index = intent.getIntExtra("index", 0);
            String saveTitle = intent.getStringExtra("bookName");
            String savePrice = intent.getStringExtra("price");
            String saveDate = intent.getStringExtra("date");
            String saveUriString = intent.getStringExtra("imageURI");
            Log.d("saveImageUri",""+saveUriString);
            try {
                updateEditedDataOfBook(resourceID, saveTitle, savePrice, saveDate, saveUriString,index);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (requestCode == MyConstants.REQUEST_CODE_ADD && resultCode == Activity.RESULT_OK) {
            //追加
            super.onActivityResult(requestCode, resultCode, intent);
            String addTitle = intent.getStringExtra("bookName");
            String addPrice = intent.getStringExtra("price");
            String addDate = intent.getStringExtra("date");
            String addUriString = intent.getStringExtra("imageURI");
            Log.d("addImageUri",""+addUriString);
            //Uri imageUri = Uri.parse(addUriString);
            try {
                registerEditedDataOfBook(addTitle, addPrice, addDate, addUriString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * method to access database
     */
    //throws JSONExceptionは例外処理
    private void getBooksVolley(String requestPagePosition, int beginPage) throws JSONException {
        isThisFirstGet = requestPagePosition;
        final Gson gson = new Gson();
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        Account request_token = new Account(prefs.getString("user_id", ""),
                prefs.getString("mail_address", ""),
                prefs.getString("password", ""));
        Page page = new Page(beginPage, MyConstants.NUM_OF_PAGE, requestPagePosition);
        String method = "book/get";
        RequestGetBook requestGetBook = new RequestGetBook(method, request_token, page);
        JSONObject ob = new JSONObject(gson.toJson(requestGetBook));
        Log.d("re get", "" + gson.toJson(requestGetBook));
        // Volley でリクエスト
        //mampとgenymotionの連動は、localhostをgenymotionのある端末のIPアドレスに変更させる必要あり。
        String url = MyConstants.GET_BOOK_URL;
        //JsonObjectRequestは、(POST/GET, url, request, response, error)の感じ。
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, ob,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response get", "" + response.toString());
                        GotDataOfBooks gotDataOfBook = gson.fromJson(response.toString(), GotDataOfBooks.class);
                        if (gotDataOfBook.getStatus().equals("ok")) {
                            int i;
                            int numOfData = gotDataOfBook.getData().getNumOfData();
                            if (numOfData == 0) {
                                //データベースにデータが無いとき
                                Log.d("getData", "end data");
                                String msg = "これ以上の登録書籍はありません";
                                showAlert(msg);
                            }
                            int nowListSize = list.size();
                            for (i = 0; i < numOfData; i++) {
                                DataOfBook tmp = gotDataOfBook.getData().getDataWithLabel(i);
                                String bookName = tmp.getTitle();
                                String price = tmp.getPrice();
                                String date = tmp.getDate();
                                String imageUri = tmp.getImageUrl();
                                int resourceID = tmp.getBookId();
                                System.out.println(resourceID + ":" + "name:" + bookName + "price:" + price + "date:" + date);
                                if (isThisFirstGet.equals("latest")) {
                                    list.add(i, new ListViewItem(resourceID, bookName, price, date, imageUri));
                                } else {
                                    list.add(i + nowListSize, new ListViewItem(resourceID, bookName, price, date, imageUri));
                                    Log.d("get", "" + list.size());
                                }
                            }
                            Log.d("get size", "" + list.size());
                            SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("numOfBooks", gotDataOfBook.getData().getNumOfBooks());
                            editor.apply();
                            adapter.notifyDataSetChanged();

                        } else {
                            System.out.println("error:" + gotDataOfBook.getError());
                            list = new ArrayList<ListViewItem>();
                            adapter = new CustomListItemAdapter(getActivity(), 0, list);
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            String msg = "登録書籍がありません。書籍を登録してください。";
                            showAlert(msg);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(MyConstants.LIST_TAG, "error:", error);
            }
        }
        );
        int socketTimeout = 300;//0.3 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mQueue.add(request);
        mQueue.start();
    }

    private void registerEditedDataOfBook(String bookName, String price,
                                          String purchaseDate, String imageUri) throws JSONException {
        final Gson gson = new Gson();
        tmpTitle = bookName;
        tmpPrice = price;
        tmpDate = purchaseDate;
        tmpUri = imageUri;
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        JSONObject requestToken = new JSONObject();
        requestToken.put("user_id", prefs.getString("user_id", ""));
        requestToken.put("mail_address", prefs.getString("mail_address", ""));
        requestToken.put("password", prefs.getString("password", ""));
        JSONObject params = new JSONObject();
        params.put("request_token", requestToken);
        params.put("book_name", bookName);
        params.put("price", price);
        params.put("purchase_date", purchaseDate);
        params.put("image", imageUri);
        JSONObject param = new JSONObject();
        param.put("method", "book/register");
        param.put("params", params);
        String url = MyConstants.REGISTER_BOOK_URL;
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, param,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ResultOfRegisterOrUpdate resultData = gson.fromJson(response.toString(), ResultOfRegisterOrUpdate.class);
                        String tmp = resultData.getBookId();
                        tmpID = Integer.parseInt(tmp);
                        list.add(0, new ListViewItem(tmpID, tmpTitle, tmpPrice, tmpDate, tmpUri));
                        Log.d("register", "" + tmpID);
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(MyConstants.LIST_TAG, "error@@@" + error);
            }
        }
        );
        int socketTimeout = 300;//0.3 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mQueue.add(request);
        mQueue.start();
    }

    private void updateEditedDataOfBook(int ID, String bookName,
                                        String price, String purchaseDate, String imageUri, int index) throws JSONException {
        final Gson gson = new Gson();
        tmpIndex = index;
        tmpID = ID;
        tmpTitle = bookName;
        tmpPrice = price;
        tmpDate = purchaseDate;
        tmpUri = imageUri;
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        JSONObject requestToken = new JSONObject();
        requestToken.put("user_id", prefs.getString("user_id", ""));
        requestToken.put("mail_address", prefs.getString("mail_address", ""));
        requestToken.put("password", prefs.getString("password", ""));
        JSONObject params = new JSONObject();
        params.put("request_token", requestToken);
        params.put("book_id", ID);
        params.put("book_name", bookName);
        params.put("price", price);
        params.put("purchase_date", purchaseDate);
        params.put("image", imageUri);
        JSONObject param = new JSONObject();
        param.put("method", "book/update");
        param.put("params", params);
        String url = MyConstants.UPDATE_BOOK_URL;
        JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, param,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ResultOfRegisterOrUpdate resultData = gson.fromJson(response.toString(), ResultOfRegisterOrUpdate.class);
                        String tmp = resultData.getBookId();
                        tmpID = Integer.parseInt(tmp);
                        list.set(tmpIndex, new ListViewItem(tmpID, tmpTitle, tmpPrice, tmpDate, tmpUri));
                        adapter.notifyDataSetChanged();
                        Log.d("update", "" + tmpID);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(MyConstants.LIST_TAG, "error[[[" + error);
            }
        }
        );
        int socketTimeout = 300;//0.3 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mQueue.add(request);
        mQueue.start();
    }

    private class Page {
        private int begin;
        private int numOfPage;
        private String position;

        public Page(int mbegin, int mnumOfPage, String mposition) {
            this.begin = mbegin;
            this.numOfPage = mnumOfPage;
            this.position = mposition;
        }

        public int getBegin() {
            return begin;
        }

        public int getNumOfPage() {
            return numOfPage;
        }

        public String getPosition() {
            return position;
        }
    }

    private class GetParams {
        private Account request_token;
        private Page page;

        public GetParams(Account maccount, Page mpage) {
            this.request_token = maccount;
            this.page = mpage;
        }

        public Account getRequest_token() {
            return request_token;
        }

        public Page getPage() {
            return page;
        }
    }


    private class RequestGetBook {
        private String method;
        private GetParams params;

        public RequestGetBook(String mmthod, Account maccount, Page mpage) {
            this.method = mmthod;
            this.params = new GetParams(maccount, mpage);
        }

        public String getMethod() {
            return method;
        }


        public GetParams getParams() {
            return params;
        }
    }
}
