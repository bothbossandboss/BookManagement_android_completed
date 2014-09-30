package com.myexample.bookmanagement;
/*
 * 書籍一覧取得時の、書籍詳細データの配列を格納するクラス
 */
public class ArrayOfBooks {
	private int numOfBooks;
	/*
	private Map<String, DataOfBook> arrayOfData;
	
	public ArrayOfBooks(int numOfBooks,  Map<String, DataOfBook> array)
	{
		if(array.isEmpty()){
			System.out.println("渡すときnull");
		}else{
			System.out.println("渡すとき�OK");
		}
		this.numOfBooks = numOfBooks;
		for(int i=0;i<ListViewFragment.NUM_OF_PAGE;i++){
			String label = "array"+i;
			DataOfBook data = array.get(label);
			this.arrayOfData.put(label, data);
		}
	}
	
	public int getNumOfBooks()
	{
		return numOfBooks;
	}
	
	public Map<String, DataOfBook> getList()
	{
		return arrayOfData;
	}
	*/
	public DataOfBook array0;
	public DataOfBook array1;
	public DataOfBook array2;
	public DataOfBook array3;
	public DataOfBook array4;
	public DataOfBook array5;
	public DataOfBook array6;
	public DataOfBook array7;
	public DataOfBook array8;
	public DataOfBook array9;
	
	public ArrayOfBooks(int numOfBooks, 
			DataOfBook array0,DataOfBook array1, DataOfBook array2,DataOfBook array3,
			DataOfBook array4,DataOfBook array5, DataOfBook array6,DataOfBook array7,
			DataOfBook array8,DataOfBook array9)
	{
		this.numOfBooks = numOfBooks;
		this.array0= array0;
		this.array1= array1;
		this.array2= array2;
		this.array3= array3;
		this.array4= array4;
		this.array5= array5;
		this.array6= array6;
		this.array7= array7;
		this.array8= array8;
		this.array9= array9;
	}
	
	public int getNumOfBooks()
	{
		return numOfBooks;
	}
	
	public DataOfBook getDataWithLabel(int index)
	{
		DataOfBook tmp;
		switch(index){
		case 0: 
			tmp = array0;
			break;
		case 1:
			tmp = array1;
			break;
		case 2:
			tmp = array2;
			break;
		case 3:
			tmp = array3;
			break;
		case 4:
			tmp = array4;
			break;
		case 5:
			tmp = array5;
			break;
		case 6:
			tmp = array6;
			break;
		case 7:
			tmp = array7;
			break;
		case 8:
			tmp = array8;
			break;
		case 9:
			tmp = array9;
			break;
		default:
			tmp = null;
		}
        return tmp;
	}
	
	public int getNumOfData()
	{
		int i,count=0;
		for(i=0;i<10;i++){
			if(this.getDataWithLabel(i) == null) break;
			count++;
		}
		return count;
	}
}
