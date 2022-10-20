package com.dsinnovators.keyword.driven.commons;

import com.dsinnovators.keyword.driven.utils.TestHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class DAO {
	Connection con=null;

	public DAO(String jdbcUrl,String jdbcUser,String jdbcPass) throws Exception
	{
		final String oracleDriver= TestHelper.getTestConfPropertyValue("oracle.jdbc.driver");
		final String jdbcURL= TestHelper.getTestConfPropertyValue(jdbcUrl);
		final String jdbcUsername= TestHelper.getTestConfPropertyValue(jdbcUser);
		final String jdbcPassword= TestHelper.getTestConfPropertyValue(jdbcPass);

		if(!TestHelper.isEmpty(oracleDriver) && !TestHelper.isEmpty(jdbcURL) && !TestHelper.isEmpty(jdbcUsername) && !TestHelper.isEmpty(jdbcPassword)) {
			con = new DBConnectionHandler(oracleDriver, jdbcURL, jdbcUsername, jdbcPassword).getConnector();
		}else{
			throw new Exception("Does not have enough property to connect DB");
		}
	}

	public void executeSqlQuery(String query)throws Exception {
		if (con != null) {
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(query);
				ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if(ps != null){
						ps.close();
					}
				} catch (Exception e) {
					System.out.println("Exception in DAO->executeSqlQuery:: "+e.getMessage());
				}
			}
		}else{
			throw new Exception("Could not connect to the database");
		}
	}

	public String getData(String query,String column) throws Exception {
		if (con != null) {
			query = CommonFunc.replaceVariableWithValue(query);
			ResultSet rs = null;
			PreparedStatement ps = null;
			String status = new String();
			try {
				ps = con.prepareStatement(query);
				rs = ps.executeQuery();
				while (rs.next()) {
					status = rs.getString(column);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					rs.close();
				} catch (Exception e) {
				}
				try {
					ps.close();
				} catch (Exception e) {
				}
			}
			return status;
		}else{
			throw new Exception("Could not connect to the database");
		}
	}
	public Boolean hasData(String query) throws Exception {
		if (con != null) {
			query = CommonFunc.replaceVariableWithValue(query);
			ResultSet rs = null;
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(query);
				rs = ps.executeQuery();
				return rs.next();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					rs.close();
				} catch (Exception e) {
				}
				try {
					ps.close();
				} catch (Exception e) {
				}
			}
			return false;
		}else{
			throw new Exception("Could not connect to the database");
		}
	}

	public int getRowCount(String query) throws Exception {
		if (con != null) {
			query = CommonFunc.replaceVariableWithValue(query);
			ResultSet rs = null;
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(query);
				rs = ps.executeQuery();
				return rs.last() ? rs.getRow() : 0;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					rs.close();
				} catch (Exception e) {
				}
				try {
					ps.close();
				} catch (Exception e) {
				}
			}
			return 0;
		}else{
			throw new Exception("Could not connect to the database");
		}
	}

	public void closeConnection() throws Exception {
		if (con != null) {
			con.close();
		}
	}
}
