package models;

import play.db.*;

import javax.sql.DataSource;

import java.sql.*;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * SQL OPERATIONS
 * 
 * loginInsert(username, password):
 *      Insert into Table with all user login information. Username and Hashed Password.
 * 
 * loginCheck(username, password):
 *      Returns true if username is in database along with a password that matches.
 * 
 * loginPrint():
 *      Testing purposes.
 * 
 * loginGetUsers():
 *      Returns ArrayList<String> of all users in LoginInfo table.
 *
 * tableCreate(username):
 *      Create table for individual user that holds movie id and rating associated with it.
 * 
 * tableInsert (username, movieid, rating):
 *      Inserts movie id and associated rating into table associated with user. 
 * 
 * tableGetMap(username):
 *      Returns treemap of movie ids and ratings for given username.
 * 
 * tableGetSize(username):
 *      Returns size of table for specific user.
 * 
 * tableGetLastTen(username):
 *      Returns last ten movies from user table.
 * 
 * tableCheckUser(movie, user):
 *      Checks to see if movie and rating are in user table.
 * 
 * tablePrint (username):
 *      Testing purposes.
 * 
 * */

public class SQL {


	public boolean loginInsert(String username, String password) {

		try {

			Connection conn = DB.getConnection("default");

			if(loginCheck(username, password) || loginCheck(username)) {
				return false;
			}

			String query = " insert into loginInfo (username, password)"
					+ " values (?, PASSWORD(?))";

			PreparedStatement preparedStatement = conn.prepareStatement(query); 
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			preparedStatement.execute();

			conn.close();
		} catch (SQLException ex) {
			System.out.println("loginUser Exception");
		}
		return true;

	}

	public boolean loginCheck(String username, String password) {

		try {

			Connection conn = DB.getConnection("default");

			String query = "SELECT * from loginInfo WHERE username = '"+username+"' AND password = PASSWORD('"+password+"');";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			ResultSet rs = preparedStatement.executeQuery();

			if (!rs.next()) {
				conn.close();
				return false;   
			}
			else {

				conn.close();
			}

		} catch (SQLException sql) {
			System.out.println("exception");
		}
		return true;
	}

	public boolean loginCheck(String username) {

		try {

			Connection conn = DB.getConnection("default");

			String query = "SELECT * from loginInfo WHERE username = '"+username+"';";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			ResultSet rs = preparedStatement.executeQuery();

			if (!rs.next()) {
				conn.close();
				return false;   
			}
			else {

				conn.close();
			}

		} catch (SQLException sql) {
			System.out.println("exception");
		}
		return true;
	}

	public static ArrayList<String> loginGetUsers() {
		ArrayList<String> users = new ArrayList<String>();

		try {
			Connection conn = DB.getConnection("default");

			String query = "select * from loginInfo;";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			ResultSet resultset = preparedStatement.executeQuery();

			while (resultset.next()) {
				users.add(resultset.getString("username"));
			}

			conn.close();
		} catch (SQLException ex) {
			System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
		}

		return users;


	}

	public void loginPrint() {
		try {
			Connection conn = DB.getConnection("default");

			String query = "SELECT * FROM loginInfo;";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			ResultSet resultset = preparedStatement.executeQuery(); 

			while (resultset.next()) {
				System.out.println(resultset.getString("username") + "--> " + resultset.getString("password"));
			}

			conn.close();
		} catch (SQLException ex) {
			System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
		}
	}

	public Boolean tableCreate(String username) {
		try {
			Connection conn = DB.getConnection("default");

			String query = "CREATE TABLE "+username+"Table (movie INTEGER, rating INTEGER);";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			preparedStatement.execute();

			conn.close();
		}catch(SQLException ex) {
			System.out.println("tableCreate exception");
			return false;
		}
		return true;
	}

	public void tableInsert(String username, int movie, int rating) {
		try {
			Connection conn = DB.getConnection("default");

			String query = "INSERT INTO "+username+"Table (movie,rating)" 
					+ "VALUES (?,?);";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			preparedStatement.setInt(1, movie);
			preparedStatement.setInt(2, rating);
			preparedStatement.execute();

			conn.close();
		} catch (SQLException ex) {
			System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
		}
	}

	public int tableSize(String username) {
		try {
			Connection conn = DB.getConnection("default");

			String query1 = "SELECT * FROM " + username+"Table;";
			String query2 = "SELECT COUNT(*) FROM "+username+"Table;";

			PreparedStatement preparedStatement = conn.prepareStatement(query1);
			ResultSet rs = preparedStatement.executeQuery();
			rs = preparedStatement.executeQuery(query2);

			// get the number of rows from the result set
			rs.next();
			int rowCount = rs.getInt(1);

			conn.close();
			return rowCount;
		} catch(SQLException sql) {
			System.out.println("THERE HAS BEEN AN SQLEXCEPTION IN TABLESIZE");
		}
		return 0;
	}

	public void tablePrint(String username) {
		try {
			Connection conn = DB.getConnection("default");

			String query = "SELECT * FROM "+username+"Table;";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			ResultSet resultset = preparedStatement.executeQuery();

			while (resultset.next()) {
				System.out.println(resultset.getInt("movie") + "--> " + resultset.getInt("rating"));
			}

			conn.close();
		} catch (SQLException ex) {
			System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
		}
	}

	public TreeMap<Integer, Integer> tableGetMap(String username) {
		TreeMap<Integer, Integer> userdata = new TreeMap<Integer, Integer>();
		try {
			Connection conn = DB.getConnection("default");

			String query = "SELECT * FROM "+username+"Table;";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			ResultSet resultset = preparedStatement.executeQuery();

			while(resultset.next()) {
				userdata.put(resultset.getInt("movie"), resultset.getInt("rating"));
			}

			conn.close();
		} catch (SQLException ex) {
			System.out.println("THERE HAS BEEN AN EXCEPTION tablegetMap for :" + username);
		}
		return userdata;
	}

	public ArrayList<Integer> tableGetLastTen(String username) {
		ArrayList<Integer> lastten = new ArrayList<Integer>();
		try {
			Connection conn = DB.getConnection("default");

			String query = "SELECT * FROM "+username+"Table;";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			ResultSet resultset = preparedStatement.executeQuery();

			resultset.last();
			lastten.add(resultset.getInt("movie"));

			while(resultset.previous() && lastten.size() < 10) {
				lastten.add(resultset.getInt("movie"));
			}

			conn.close();
			return lastten;
		} catch (SQLException ex) {
			System.out.println("THERE HAS BEEN AN EXCEPTION");
		}
		return null;
	}

	public boolean checkUser(int movieid, String username) {

		try {
			Connection conn = DB.getConnection("default");

			String query = "SELECT * FROM "+username+"Table;";

			PreparedStatement preparedStatement = conn.prepareStatement(query);
			ResultSet resultset = preparedStatement.executeQuery();

			while (resultset.next()) {
				if(resultset.getInt("movie") == movieid){
					conn.close();
					return true;
				}
			}

			conn.close();
		} catch (SQLException ex) {
			System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
		} 

		return false;
	}

}