package com.myexample.bookmanagement;
/*
 * ListViewListViewにセットするアダプタのクラス
 * <>の中はListの中身の型（だと思われる）。
 */

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.List;

public class CustomListItemAdapter  extends ArrayAdapter<ListViewItem>{
    //LayoutInflaterは実行時に別のxmlファイルのviewを追加する際に用いる。
	private LayoutInflater layoutInflater;
    private Activity nowContext;
    private RequestQueue myQueue;
    private ImageLoader mImageLoader;


    //コンストラクタ
	public CustomListItemAdapter(Context context, int textViewResourceID, List<ListViewItem> objects)
	{
		super(context, textViewResourceID,objects);
		layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    nowContext = (Activity)context;
    }

    //ListViewの各行が表示する要素を返す。
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
        myQueue = Volley.newRequestQueue(getContext());
        //convertViewがnullの場合のみ、xmlファイル指定でviewを読み込む。
		if(convertView == null){
			convertView = layoutInflater.inflate(R.layout.custom_list_item, parent, false);
		}
        //position行目のデータを取得する。
		ListViewItem item = (ListViewItem)getItem(position);
        //TextViewに文字列をセットする。
		TextView bookNameText = (TextView)convertView.findViewById(R.id.title);
		bookNameText.setText( item.getBookName() );
		TextView priceText = (TextView)convertView.findViewById(R.id.price);
		priceText.setText( item.getPrice() );
		TextView dateText = (TextView)convertView.findViewById(R.id.date);
		dateText.setText( item.getDate() );

        ImageView imageView = (ImageView)convertView.findViewById(R.id.thumbnail);
        String uriString = item.getImageUri();
        Log.d("url", uriString);
        downloadImage(imageView, uriString);
        /*Bitmap bitmap;
        if(uriString != null) {
            Uri uri = Uri.parse(uriString);
            ImageController imageController = new ImageController(uri);
            bitmap = imageController.loadImage(MyConstants.IMAGE_VIEW_WIDTH,
                    MyConstants.IMAGE_VIEW_HEIGHT, nowContext);
        }else{
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            Resources res = nowContext.getResources();
            bitmap = BitmapFactory.decodeResource(res, R.drawable.no_image, options);
        }
        imageView.setImageBitmap(bitmap);
        */
		return convertView;
	}

    private void downloadImage(ImageView mImage,String url)
    {
        ImageLoader mImageLoader = new ImageLoader(myQueue, new BitmapCache());
        // リクエストのキャンセル処理
        ImageLoader.ImageContainer imageContainer = (ImageLoader.ImageContainer)mImage.getTag();
        if (imageContainer != null) {
            imageContainer.cancelRequest();
        }
        String localhost = "localhost";
        String newURL = url.replaceAll(localhost, MyConstants.IP_ADDRESS);
        Log.d("replace",newURL);
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(mImage,
                R.drawable.shinkoukei, R.drawable.error);
        mImage.setTag(mImageLoader.get(newURL, listener));
    }

}	