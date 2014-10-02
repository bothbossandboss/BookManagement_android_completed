package com.myexample.bookmanagement;
/*
 * 書籍詳細情報を表示するactivity
 */
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DetailViewActivity extends Activity
{
    public EditText editBookName;
    public EditText editPrice;
    public EditText editDate;
    public Button selectImageButton;
    public ImageView imageView;
    public int resourceID;
    public Uri imageUri;
    private int position;
    private Bitmap bitmap;
    private Canvas canvas;

    /*
     * method of activity's life cycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        //Log.d("detail","遷移完了");
        Log.d(MyConstants.DETAIL_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //Activity起動時にすぐにキーボードが立ち上がらないようにする。
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.detail_view);
        Intent intent = getIntent();
        resourceID = intent.getIntExtra("resourceID", 0);
        String title = intent.getStringExtra("bookName");
        String price = intent.getStringExtra("price");
        String date = intent.getStringExtra("date");
        position = intent.getIntExtra("position", 0);
        editBookName = (EditText)findViewById(R.id.editBookName);
        editPrice = (EditText)findViewById(R.id.editPrice);
        editDate = (EditText)findViewById(R.id.editDate);
        controlKeyboard(editBookName);
        controlKeyboard(editPrice);
        controlDatePicker(editDate);
        editBookName.setText(title);
        editPrice.setText(price);
        editDate.setText(date);
        //デフォ
        imageUri = null;
    }

    @Override
    public void onStart()
    {
        Log.d(MyConstants.DETAIL_TAG, "onStart");
        super.onStart();
        imageView = (ImageView)findViewById(R.id.bookImageView);
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
                imageUri = uri;
                ImageController imageController = new ImageController(uri);
                bitmap = imageController.loadImage(MyConstants.IMAGE_VIEW_WIDTH, MyConstants.IMAGE_VIEW_HEIGHT,(Activity)this);
                canvas = new Canvas(bitmap);
                imageView.setImageBitmap(bitmap);
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
}
