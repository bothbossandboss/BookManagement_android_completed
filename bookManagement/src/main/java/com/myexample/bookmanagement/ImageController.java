package com.myexample.bookmanagement;
/*
 * 書籍詳細画面における書籍の画像を管理するクラス
 */
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageController{
    private Uri imageUri;

    public ImageController(Uri uri)
    {
        imageUri = uri;
    }

    // 取得したURIを用いて画像を読み込む
    public Bitmap loadImage(int viewWidth, int viewHeight, Activity ac){
        // URIから画像を読み込みBitmapを作成
        Bitmap originalBitmap = null;
        try {
            //originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            InputStream in = ac.getContentResolver().openInputStream(imageUri);
            originalBitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // MediaStoreから回転情報を取得
        final int orientation;
        Cursor cursor = MediaStore.Images.Media.query(ac.getContentResolver(), imageUri, new String[] {
                MediaStore.Images.ImageColumns.ORIENTATION
        });
        if (cursor != null) {
            cursor.moveToFirst();
            orientation = cursor.getInt(0);
        } else {
            orientation = 0;
        }

        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        // 縮小割合を計算
        final float scale;
        if (orientation == 90 || orientation == 270) {
            // 縦向きの画像は半分のサイズに変更
            scale = Math.min(((float)viewWidth / originalHeight)/2, ((float)viewHeight / originalWidth)/2);
        } else {
            // 横向きの画像
            scale = Math.min((float)viewWidth / originalWidth, (float)viewHeight / originalHeight);
        }

        // 変換行列の作成
        final Matrix matrix = new Matrix();
        if (orientation != 0) {
            //画像を回転させる
            matrix.postRotate(orientation);
        }
        if (scale < 1.0f) {
            // Bitmapを拡大縮小する
            matrix.postScale(scale, scale);
        }

        // 行列によって変換されたBitmapを返す
        return Bitmap.createBitmap(originalBitmap, 0, 0, originalWidth, originalHeight, matrix,
                true);
    }
}
