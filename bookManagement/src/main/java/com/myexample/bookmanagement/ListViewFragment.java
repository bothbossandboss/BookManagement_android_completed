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
    /**
     * !!! 定数もpublicにしたほうが良いかよく考える
     * むやみにpublic にしない
     */
    public static final  int FIRST_BEGIN_PAGE = 28;
    public static final  int NUM_OF_PAGE = 10;
    public static final String IP_ADDRESS = "10.0.1.44";
    private static final String TAG = "LifeCycleList";
    private List<ListViewItem> list;
    private ListView listView ;
    /**
     * !! mPrefixをつけるかはとりあえず好みでいいがどちらかに統一する
     * prefixをつけるAndroid Studioの設定もあるのでぐぐるべし
     */
    private RequestQueue mQueue;
    private CustomListItemAdapter adapter;
    /**
     * !!!!! tmpなどの一時変数はクラスに持たせない
     * あるメソッドでしか使わないならそのメソッドのローカル変数として宣言する
     * 変数の有効範囲(スコープ)はできるだけ小さくなるようにしたほうがバグが出にくい
     */
    private int tmpID;
    private String tmpTitle;
    private String tmpPrice;
    private String tmpDate;
    private int tmpIndex;
    /**
     * ! 変数名が謎
     */
    private String isThisFirstGet;

    /*
     * method of fragment's life cycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getActivity());
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        Log.d("prefs",""+prefs.getString("user_id",""));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
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
            getBooksVolley("latest", FIRST_BEGIN_PAGE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("list",""+list.size());
        //ListViewの要素(cell)がタップされた時の処理。
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView)parent;
                ListViewItem item = (ListViewItem)listView.getItemAtPosition(position);
                int ID = item.getResourceID();
                String title = item.getBookName();
                String price = item.getPrice();
                String date = item.getDate();
                Log.d("tag","ID:"+ID);
                Log.d("name:%s", title);
                Log.d("price:%s", price);
                Log.d("date:%s", date);
                //DetailViewに遷移するためのインテントを作成する。
                Intent intent = new Intent(getActivity(), DetailViewActivity.class);
                /**
                 * !!!!! マジックナンバーの廃止
                 * key名が直接埋め込まれている(マジックナンバー)
                 * 基本的にこのような定数もstatic finalなフィールドとして宣言すること。
                 * バグの元です
                 */
                intent.putExtra("resourceID",ID);
                intent.putExtra("bookName",title);
                intent.putExtra("price",price);
                intent.putExtra("date",date);
                intent.putExtra("position",position);
                Log.d("intent","intent作成完了 ");
                //遷移先から返却される識別コードを指定することで返却値を反映できる。
                int requestCode = MainActivity.REQUEST_CODE_SAVE;
                startActivityForResult(intent,requestCode);
                /**
                 * リクエストコードはちゃんと定数で宣言されてて良いです。
                 */
            }
        });
    }

    private void showAlert(String msg)
    {
        /**
         * !! builderはメソッドチェーンできるので使いましょう。
         * かっこいいです
         * 現状は2行目だけチェーンされてて不思議な感じになっています
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg).setPositiveButton("OK", null);
        builder.show();
    }

    /*
     * method to control action bar
     */
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)
    {
        inflater.inflate(R.menu.list_action_bar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //追加ボタンが押された時のメソッド
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addButton:
                Log.d("addButton","tapped");
                Intent intent = new Intent(getActivity(), AddDataActivity.class);
                intent.putExtra("resourceID",list.size() );
                int requestCode = MainActivity.REQUEST_CODE_ADD;
                startActivityForResult(intent,requestCode);
                return true;
            case R.id.reloadButton:
                Log.d("reloadButton","tapped");
                reloadMoreData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void reloadMoreData()
    {
        /**
         * !!!!! マジックナンバー
         * このクラスで読み込み書き込み1箇所ぐらいならとりあえず動作確認するぐらいのときは直接書いてもいいけど
         * やっぱよくないです
         */
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        int numOfBooks = prefs.getInt("numOfBooks", 0);
        int beginPage = numOfBooks - list.size();
        try {
            getBooksVolley("old", beginPage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        /**
         * !!! if分の入れ子(ネスト)が無駄に深いです。
         * 読みにくくバグの元になるので連続していたらなにかいいかんじに出来ないか疑いましょう
         */
        //編集して保存する場合。
        if(requestCode == MainActivity.REQUEST_CODE_SAVE && resultCode == Activity.RESULT_OK)
        {
            // diff がアレなのでインデントは買えずに残してます(ホントはインデントもちゃんと変える)
            // マジックナン(
                super.onActivityResult(requestCode, resultCode, intent);
                int resourceID = intent.getIntExtra("ID", 0);
                int index = intent.getIntExtra("index", 0);
                String saveTitle = intent.getStringExtra("bookName");
                String savePrice = intent.getStringExtra("price");
                String saveDate = intent.getStringExtra("date");
                String saveUriString = intent.getStringExtra("imageURI");
                //Uri imageUri = Uri.parse(saveUriString);
                try {
                    updateEditedDataOfBook(resourceID, saveTitle, savePrice, saveDate, index);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }else if(requestCode == MainActivity.REQUEST_CODE_ADD && resultCode == Activity.RESULT_OK)
        {
            // マジッ(
                super.onActivityResult(requestCode, resultCode, intent);
                String addTitle = intent.getStringExtra("bookName");
                String addPrice = intent.getStringExtra("price");
                String addDate = intent.getStringExtra("date");
                String addUriString = intent.getStringExtra("imageURI");
                //Uri imageUri = Uri.parse(addUriString);
                try {
                    registerEditedDataOfBook(addTitle, addPrice, addDate);
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
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        Account request_token = new Account(prefs.getString("user_id", ""),
                prefs.getString("mail_address", ""),
                prefs.getString("password",""));
        Page page = new Page(beginPage,NUM_OF_PAGE,requestPagePosition);
        String method = "book/get";
        RequestGetBook requestGetBook = new RequestGetBook(method, request_token, page);
        JSONObject ob = new JSONObject(gson.toJson(requestGetBook));
        Log.d("re get",""+gson.toJson(requestGetBook));
        // Volley でリクエスト
        //mampとgenymotionの連動は、localhostをgenymotionのある端末のIPアドレスに変更させる必要あり。
        String url = "http://"+IP_ADDRESS+":8888/cakephp/book/get";
        //JsonObjectRequestは、(POST/GET, url, request, response, error)の感じ。
        JsonObjectRequest  request = new JsonObjectRequest(Method.POST, url, ob,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        /**
                         * !!! ネストが深いのでメソッドやクラスに分割できると良いです
                         */
                        Log.d("response get",""+response.toString());
                        /**
                         * !! JsonObjectRequestではなくてBooksRequestなどの
                         * クラスを作りJsonからオブジェクトを作るところまでを隠してしまうのも良いと思われ。
                         */
                        GotDataOfBooks gotDataOfBook = gson.fromJson(response.toString(),GotDataOfBooks.class);
                        if(gotDataOfBook.getStatus().equals("ok")){
                            int i;
                            int numOfData = gotDataOfBook.getData().getNumOfData();
                            if(numOfData == 0)
                            {
                                //データベースにデータが無いとき
                                Log.d("getData", "end data");
                                String msg = "これ以上の登録書籍はありません";
                                showAlert(msg);
                            }
                            int nowListSize = list.size();
                            for(i=0;i<numOfData;i++)
                            {
                                /**
                                 * !! 一時変数でもなるべくtmpなどの意味のない変数名をつけない
                                 */
                                DataOfBook tmp = gotDataOfBook.getData().getDataWithLabel(i);
                                String bookName = tmp.getTitle();
                                String price = tmp.getPrice();
                                String date = tmp.getDate();
                                int resourceID = tmp.getBookId();
                                System.out.println(resourceID+":"+"name:"+bookName+"price:"+price+"date:"+date);
                                if(isThisFirstGet.equals("latest")){
                                    list.add(i, new ListViewItem(resourceID, bookName, price, date) );
                                }else{
                                    list.add(i+nowListSize, new ListViewItem(resourceID, bookName, price, date) );
                                    Log.d("get", ""+ list.size());
                                }
                            }
                            Log.d("get size", ""+list.size());
                            SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("numOfBooks", gotDataOfBook.getData().getNumOfBooks());
                            editor.apply();
                            adapter.notifyDataSetChanged();

                        }else{
                            System.out.println("error:"+gotDataOfBook.getError());
                            list = new ArrayList<ListViewItem>();
                            adapter = new CustomListItemAdapter(getActivity(), 0, list);
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            String msg = "登録書籍がありません。書籍を登録してください。";
                            showAlert(msg);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"error:", error);
            }}
        );
        int socketTimeout = 300;//0.3 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mQueue.add(request);
        mQueue.start();
    }

    private void registerEditedDataOfBook(String bookName, String price, String purchaseDate) throws JSONException
    {
        final Gson gson = new Gson();
        tmpTitle = bookName;
        tmpPrice = price;
        tmpDate = purchaseDate;
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        JSONObject requestToken = new JSONObject();
        requestToken.put("user_id", prefs.getString("user_id", ""));
        requestToken.put("mail_address", prefs.getString("mail_address", ""));
        requestToken.put("password", prefs.getString("password",""));
        JSONObject params = new JSONObject();
        params.put("request_token",requestToken);
        params.put("book_name",bookName);
        params.put("price",price);
        params.put("purchase_date",purchaseDate);
        params.put("image","no image");
        JSONObject param = new JSONObject();
        param.put("method", "book/register");
        param.put("params", params);
        String url = "http://"+IP_ADDRESS+":8888/cakephp/book/regist";
        JsonObjectRequest  request = new JsonObjectRequest(Method.POST, url, param,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ResultOfRegisterOrUpdate resultData = gson.fromJson(response.toString(),ResultOfRegisterOrUpdate.class);
                        String tmp = resultData.getBookId();
                        tmpID = Integer.parseInt(tmp);
                        list.add(0,new ListViewItem(tmpID,tmpTitle,tmpPrice,tmpDate));
                        Log.d("register",""+tmpID);
                        adapter.notifyDataSetChanged();
                    }}, new Response.ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"error@@@"+error);
            }}
        );
        int socketTimeout = 300;//0.3 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mQueue.add(request);
        mQueue.start();
    }

    private void updateEditedDataOfBook(int ID,String bookName,
                                        String price, String purchaseDate, int index) throws JSONException
    {
        final Gson gson = new Gson();
        /**
         * !!!! ローカル変数として宣言する
         * スコープは小さく。
         */
        tmpIndex = index;
        tmpID = ID;
        tmpTitle = bookName;
        tmpPrice = price;
        tmpDate = purchaseDate;
        SharedPreferences prefs = getActivity().getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
        JSONObject requestToken = new JSONObject();
        requestToken.put("user_id", prefs.getString("user_id", ""));
        requestToken.put("mail_address", prefs.getString("mail_address", ""));
        requestToken.put("password", prefs.getString("password",""));
        JSONObject params = new JSONObject();
        params.put("request_token",requestToken);
        params.put("book_id", ID);
        params.put("book_name",bookName);
        params.put("price",price);
        params.put("purchase_date",purchaseDate);
        params.put("image","no image");
        JSONObject param = new JSONObject();
        param.put("method", "book/update");
        param.put("params", params);
        String url = "http://"+IP_ADDRESS+":8888/cakephp/book/update";
        JsonObjectRequest  request = new JsonObjectRequest(Method.POST, url, param,
                new Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ResultOfRegisterOrUpdate resultData = gson.fromJson(response.toString(),ResultOfRegisterOrUpdate.class);
                        String tmp = resultData.getBookId();
                        tmpID = Integer.parseInt(tmp);
                        list.set(tmpIndex,new ListViewItem(tmpID,tmpTitle,tmpPrice,tmpDate));
                        adapter.notifyDataSetChanged();
                        Log.d("update",""+tmpID);
                    }}, new Response.ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"error[[["+error);
            }}
        );
        int socketTimeout = 300;//0.3 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        mQueue.add(request);
        mQueue.start();
    }

    /**
     * !!!! 内部クラスではなく独立したクラスにしよう
     * modelなどのパッケージを作ってそこにデータオブジェクトのクラスを放り込んだりするとよいです
     * -> こういうデータ持ってるだけのクラスはPOJOとかBeanとかいろいろ言い方はありますが割とどうでもいいです
     */
    private class Page
    {
        private int begin;
        private int numOfPage;
        private String position;

        public Page(int mbegin, int mnumOfPage, String mposition)
        {
            this.begin = mbegin;
            this.numOfPage = mnumOfPage;
            this.position = mposition;
        }

        public int getBegin()
        {
            return  begin;
        }

        public int getNumOfPage()
        {
            return numOfPage;
        }

        public String getPosition()
        {
            return position;
        }
    }

    private class GetParams
    {
        private Account request_token;
        private Page page;

        public GetParams(Account maccount, Page mpage)
        {
            this.request_token = maccount;
            this.page = mpage;
        }

        public Account getRequest_token()
        {
            return request_token;
        }

        public Page getPage()
        {
            return page;
        }
    }


    private class RequestGetBook
    {
        private String method;
        private GetParams params;

        public RequestGetBook(String mmthod, Account maccount, Page mpage)
        {
            this.method = mmthod;
            this.params = new GetParams(maccount,mpage);
        }

        public String getMethod()
        {
            return method;
        }


        public GetParams getParams()
        {
            return params;
        }
    }

}
