package com.myexample.bookmanagement;
/*
 * 新規書籍登録画面のactivity
 */

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

public class AddDataActivity extends DetailViewActivity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Activity起動時にすぐにキーボードが立ち上がらないようにする。
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.detail_view);
        Intent intent = getIntent();
        resourceID = intent.getIntExtra("resourceID", 0);
        editBookName = (EditText) findViewById(R.id.editBookName);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editDate = (EditText) findViewById(R.id.editDate);
        controlKeyboard(editBookName);
        controlKeyboard(editPrice);
        controlDatePicker(editDate);

        imageView = (ImageView) findViewById(R.id.bookImageView);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Resources res = this.getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.no_image, options);
        imageView.setImageBitmap(bitmap);
        imageUri = "no image";
    }
}