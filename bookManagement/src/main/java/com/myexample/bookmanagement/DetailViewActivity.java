package com.myexample.bookmanagement;
/*
 * 書籍詳細情報を表示するactivity
 */

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetailViewActivity extends Activity
{
    public EditText editBookName;
    public EditText editPrice;
    public EditText editDate;
    public Button selectImageButton;
    public ImageView imageView;
    public int resourceID;
    public String imageUri;
    private int position;
    private Bitmap bitmap;
    private Canvas canvas;
    private RequestQueue mQueue;

    /*
     * method of activity's life cycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        //Volleyのキュー設定
        mQueue = Volley.newRequestQueue(this, new HurlStack());

        //Log.d("detail","遷移完了");
        Log.d(MyConstants.DETAIL_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //Activity起動時にすぐにキーボードが立ち上がらないようにする。
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.detail_view);
        imageView = (ImageView)findViewById(R.id.bookImageView);

        Intent intent = getIntent();
        resourceID = intent.getIntExtra("resourceID", 0);
        String title = intent.getStringExtra("bookName");
        String price = intent.getStringExtra("price");
        String date = intent.getStringExtra("date");
        position = intent.getIntExtra("position", 0);

        String uriString = intent.getStringExtra("imageUri");
        if(uriString == null){
            uriString = "no image";
        }
        if(!uriString.equals("no image")) {
           Uri uri= Uri.parse(uriString);
           ImageController imageController = new ImageController(uri);
           bitmap = imageController.loadImage(MyConstants.IMAGE_VIEW_WIDTH, MyConstants.IMAGE_VIEW_HEIGHT, this);
        }else {
           Log.d("detail", "add");
           BitmapFactory.Options options = new BitmapFactory.Options();
           options.inMutable = true;
           Resources res = this.getResources();
           bitmap = BitmapFactory.decodeResource(res, R.drawable.no_image, options);
        }
        canvas = new Canvas(bitmap);
        imageView.setImageBitmap(bitmap);

        editBookName = (EditText)findViewById(R.id.editBookName);
        editPrice = (EditText)findViewById(R.id.editPrice);
        editDate = (EditText)findViewById(R.id.editDate);
        controlKeyboard(editBookName);
        controlKeyboard(editPrice);
        controlDatePicker(editDate);
        editBookName.setText(title);
        editPrice.setText(price);
        editDate.setText(date);
    }

    @Override
    public void onStart()
    {
        Log.d(MyConstants.DETAIL_TAG, "onStart");
        super.onStart();
        Button saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpannableStringBuilder sb = (SpannableStringBuilder)editBookName.getText();
                String saveTitle = sb.toString();
                sb = (SpannableStringBuilder)editPrice.getText();
                String savePrice = sb.toString();
                sb = (SpannableStringBuilder)editDate.getText();
                String saveDate = sb.toString();

                Intent intent = new Intent();
                intent.putExtra("ID",resourceID);
                intent.putExtra("bookName",saveTitle);
                intent.putExtra("price",savePrice);
                intent.putExtra("date", saveDate);
                intent.putExtra("imageURI", imageUri);
                intent.putExtra("index", position);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        selectImageButton = (Button)findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 画像が保存されてるフォルダにアクセス
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                //遷移先から返却される識別コードを指定することで返却値を反映できる。
                int requestCode = MyConstants.REQUEST_CODE_SELECT_IMAGE;
                startActivityForResult(intent,requestCode);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(requestCode == MyConstants.REQUEST_CODE_SELECT_IMAGE)
        {
            if(resultCode == Activity.RESULT_OK && intent != null)
            {
                super.onActivityResult(requestCode, resultCode, intent);
                Uri uri = intent.getData();
                Log.d("image",""+uri);
                imageUri = uri.toString();
                ImageController imageController = new ImageController(uri);
                bitmap = imageController.loadImage(MyConstants.IMAGE_VIEW_WIDTH, MyConstants.IMAGE_VIEW_HEIGHT,this);
                canvas = new Canvas(bitmap);
                imageView.setImageBitmap(bitmap);
                uploadImage(imageUri);
            }
        }
    }

    /*
     * method to control text field
     */
    public void controlKeyboard(final EditText ed)
    {
        ed.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                Log.d("keyboard", "hasFocus:"+hasFocus);
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

    public void controlDatePicker(final EditText ed)
    {
        //keyboard非表示の為に、xmlファイルでandroid:focusable="false"が必要。
        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(ed, myCalendar);
            }
        };
        ed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(DetailViewActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    public void updateLabel(EditText ed, Calendar myCalendar) {
        String myFormat = "yyyy/MM/dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        ed.setText(sdf.format(myCalendar.getTime()));
    }

    private void uploadImage(String path)
    {
        Map<String,String> stringMap = new HashMap<String, String>();
        Map<String,File> fileMap = new HashMap<String, File>();
        stringMap.put("method", "book/upload_image"); //textも送るとき利用
        fileMap.put("Image",new File(path));
        MultipartRequest multipartRequest = new MultipartRequest(
                MyConstants.UPLOAD_IMAGE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Upload成功
                        Log.d("upload", "Upload success: " + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Upload失敗
                        Log.d("upload","Upload error: " + error.getMessage());
                    }
                },
                stringMap,
                fileMap);
        mQueue.add(multipartRequest);
    }

}
