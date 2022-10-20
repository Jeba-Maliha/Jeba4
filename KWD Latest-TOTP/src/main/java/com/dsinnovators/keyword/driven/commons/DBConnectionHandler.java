package com.dsinnovators.keyword.driven.commons;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionHandler {

	private Connection con = null;

	public DBConnectionHandler(String Driver, String URL, String USERNAME, String PASSWORD) throws Exception{
		// DB Connection
		try {
			Class.forName(Driver).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new Exception("Can not connect to db");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new Exception("Can not connect to db");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new Exception("Can not connect to db");
		}

		String url = URL;
		try {
			con = DriverManager.getConnection(url, USERNAME,
					PASSWORD);
			System.out.println("Success");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Can not connect to db");
		}
	}

	public Connection getConnector(){
		return con;
	}
}
