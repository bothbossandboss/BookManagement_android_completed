package com.myexample.bookmanagement;
/*
 *
 *データベースからのレスポンスであるユーザーアカウントの情報を格納するクラス
 */
public class Account {
	private String user_id;
	private String mail_address;
	private String password;
	
	public Account(String user_id, String mail_address, String password)
	{
		this.user_id = user_id;
		this.mail_address = mail_address;
		this.password = password;
	}
	
	public String getUserId()
	{
		return user_id;
	}
	
	public String getMailAddress()
	{
		return mail_address;
	}
}
