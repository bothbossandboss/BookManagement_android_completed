package com.myexample.bookmanagement;
/*
 * ListViewListViewにセットするアダプタのクラス
 * <>の中はListの中身の型（だと思われる）。
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomListItemAdapter  extends ArrayAdapter<ListViewItem>{
    //LayoutInflaterは実行時に別のxmlファイルのviewを追加する際に用いる。
	private LayoutInflater layoutInflater;

    //コンストラクタ
	public CustomListItemAdapter(Context context, int textViewResourceID, List<ListViewItem> objects)
	{
		super(context, textViewResourceID,objects);
		layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    //ListViewの各行が表示する要素を返す。
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
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
		return convertView;
	}
}	