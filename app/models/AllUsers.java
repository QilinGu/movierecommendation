package models;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.*;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import models.Update;
import models.User;





//SVD
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.SparseMatrix;

//Database imports
import play.db.*;
import javax.sql.DataSource;
import java.sql.*;

public class AllUsers {
    
    public ArrayList<String> shortlist;
    private TreeMap<String, User> allusers;
    public static ArrayList<String> allmovies;
    private ArrayList<String> allgenres;

    private Connection connection;
    public static boolean updating;
    
    public AllUsers() {
        
        /*TreeMap<Username, Userdata>*/
        allusers = new TreeMap<String, User>();
        
        /*ArrayList<MovieTitles> index would be movieID*/
        allmovies = new ArrayList<String>();
        
        /*ArrayList<genres> index would be movieID*/
        allgenres = new ArrayList<String>();
        

        //V = readMatrix("conf/Vmatrix3users.txt");
        connection = DB.getConnection("default");
        shortlist = new ArrayList<String>();
        updating = false;
    }
    
    public static int getMoviesize(){
       return allmovies.size();
    }
    
    
    public static void updateNow() {
        updating = true;
    }
    
    public static void updateFinish() {
        updating = false;
    }
    
    public int getSizeOfAll() {
        
        return allmovies.size();
    }
  
    
    public void movieParse(File moviefile) throws IOException {
        
		String line,finalline;
		BufferedReader input = null;
        int count = 1;
		try {
		    
			input = new BufferedReader(new FileReader(moviefile));
			
			while ((line = input.readLine()) != null) {
					if (!(line.isEmpty()) && startsWithNumber(line)) {
						StringBuilder outLine = new StringBuilder();
						StringBuilder countindex = new StringBuilder();
						int begin = line.indexOf("::")+ 2;
						countindex.append(line, 0, begin-2);
						int filecount = Integer.parseInt(countindex.toString());
						while(count < filecount){
					    	allmovies.add("Dummy Movie part "+count + " (2015)");	
							count++;
						}
						int end = line.lastIndexOf("::");
						while(begin < end){
							outLine.append(line.charAt(begin++));
						}
						//finalline =removeYears(outLine.toString());
						finalline =outLine.toString();
						allmovies.add(finalline);
						count++;
					}
				}
				
		
		} catch(FileNotFoundException e) {
		    
			System.out.println("File Not Found");
			
		} catch (IOException e) {
		    
			System.out.println("File is not readable");
		
		} finally {
		    
			input.close();
		}
	}
   
   
	public void userParse(File userfile) throws IOException {
	    

        String line,finalline;
		BufferedReader input = null;

        int count = 1;


		try {
		    
			input = new BufferedReader(new FileReader(userfile));

			
			while ((line = input.readLine()) != null) {
					if (!(line.isEmpty()) && startsWithNumber(line)) {
						StringBuilder outLine = new StringBuilder();
						StringBuilder countindex = new StringBuilder();
						int begin = line.indexOf("::")+ 2;
						countindex.append(line, 0, begin-2);
						int filecount = Integer.parseInt(countindex.toString());
						while(count < filecount){
					    	allmovies.add("Dummy Movie part "+count + " (2015)");	
							count++;
						}
						int end = line.lastIndexOf("::");
						while(begin < end){
							outLine.append(line.charAt(begin++));
						}
						//finalline =removeYears(outLine.toString());
						finalline =outLine.toString();
						allmovies.add(finalline);
						count++;
					}
				}
				
			
			
			

		} catch(FileNotFoundException e) {
			
			System.out.println("File Not Found");
			
		} catch (IOException e) {
		    
			System.out.println("File is not readable");
		
		}finally {
		    
			input.close();
		}
	}
    

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
     * tableCreate(username):
     *      Create table for individual user that holds movie id and rating associated with it.
     * 
     * tableInsert (username, movieid, rating):
     *      Inserts movie id and associated rating into table associated with user. 
     * 
     * tableGetMap(username)
     *      Returns treemap of movie ids and ratings for given username.
     * 
     * tablePrint (username):
     *      Testing purposes.
     * 
     * */
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
    
    public static ArrayList<String> loginGetUsers(){
        ArrayList<String> users = new ArrayList<String>();
        
        try{
            Connection conn = DB.getConnection("default");
            
            String query = "select * from loginInfo;";

            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultset = preparedStatement.executeQuery();
            
            while (resultset.next()) {
                users.add(resultset.getString("username"));
            }
            
            conn.close();
        } catch (SQLException ex){
            System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
        }
        
        return users;
        
        
    }
    
    public void loginPrint(){
        try{
            Connection conn = DB.getConnection("default");
            
            String query = "SELECT * FROM loginInfo;";
            
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultset = preparedStatement.executeQuery(); 

            while (resultset.next()) {
                System.out.println(resultset.getString("username") + "--> " + resultset.getString("password"));
            }
            
            conn.close();
        } catch (SQLException ex){
            System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
        }
    }
    
    public Boolean tableCreate(String username){
        try{
            Connection conn = DB.getConnection("default");
            
            String query = "CREATE TABLE "+username+"Table (movie INTEGER, rating INTEGER);";
            
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.execute();
            
            conn.close();
        }catch(SQLException ex){
            System.out.println("tableCreate exception");
            return false;
        }
        return true;
    }
    
    public void tableInsert(String username, int movie, int rating){
        try{
            Connection conn = DB.getConnection("default");
            
            String query = "INSERT INTO "+username+"Table (movie,rating)" 
            + "VALUES (?,?);";
            
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, movie);
            preparedStatement.setInt(2, rating);
            preparedStatement.execute();

            conn.close();
        } catch (SQLException ex){
                System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
            }
    }
    
    public int tableSize(String username){
        try{
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
        } catch(SQLException sql){
            System.out.println("THERE HAS BEEN AN SQLEXCEPTION IN TABLESIZE");
        }
        return 0;
    }
    
    public void tablePrint(String username){
        try{
            Connection conn = DB.getConnection("default");
            
            String query = "SELECT * FROM "+username+"Table;";
            
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultset = preparedStatement.executeQuery();
            
            while (resultset.next()) {
                System.out.println(resultset.getInt("movie") + "--> " + resultset.getInt("rating"));
            }
            
            conn.close();
        } catch (SQLException ex){
            System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
        }
    }
    
    public TreeMap<Integer, Integer> tableGetMap(String username){
        TreeMap<Integer, Integer> userdata = new TreeMap<Integer, Integer>();
        try{
            Connection conn = DB.getConnection("default");
            
            String query = "SELECT * FROM "+username+"Table;";
            
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultset = preparedStatement.executeQuery();
            
            while(resultset.next()){
                userdata.put(resultset.getInt("movie"), resultset.getInt("rating"));
            }
            
            conn.close();
        } catch (SQLException ex){
            System.out.println("THERE HAS BEEN AN EXCEPTION tablegetMap for :" + username);
        }
        return userdata;
    }
    
    public ArrayList<Integer> tableGetLastTen(String username){
        ArrayList<Integer> lastten = new ArrayList<Integer>();
        try{
            Connection conn = DB.getConnection("default");
            
            String query = "SELECT * FROM "+username+"Table;";
            
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet resultset = preparedStatement.executeQuery();
           
            resultset.last();
            lastten.add(resultset.getInt("movie"));
           
            while(resultset.previous() && lastten.size() < 10){
                lastten.add(resultset.getInt("movie"));
            }
            
            conn.close();
            return lastten;
        } catch (SQLException ex){
            System.out.println("THERE HAS BEEN AN EXCEPTION");
        }
        return null;
    }
    
    public boolean checkUser(int movieid, String username){

        try{
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
        } catch (SQLException ex){
            System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
        } 
        
        return false;
    }
    
    /** END TO SQL CODE **/
    
    public void addToAll(String username, User user){
        
        if(user.username == null){
            user.setUserName(username);
        }
        user.setUserID(allusers.size()+1);
        allusers.put(username, user);  
        
    }
    
    public boolean hasUser(String username){
        if(allusers.containsKey(username)){
            return true;
        }
        return false;
    }
    
    public User getUserData(String username){
        return allusers.get(username);
    }
    
    public String findGenre(int id){
        return allgenres.get(id);
    }
    //Change by Daniel
    public String findMovie(int id){
        return allmovies.get(id-1);
    }
    
    public ArrayList<Integer> getTenRandomIDS(ArrayList baddummymovies){
        ArrayList<Integer> random = new ArrayList<Integer>();
        int i = 0;
        int randy;
        while (i<10){
            randy = (int)(Math.random() * allmovies.size()) + 1;
            if (!baddummymovies.contains(randy)){
                random.add(randy);
                i++;
            }
        }
        makeRandomList(random);
        return random;
    }
    
    public ArrayList<Integer> getTenRandomIDS(String username, ArrayList baddummymovies){
        ArrayList<Integer> random = new ArrayList<Integer>();
        while(random.size() != 10)
        {
            int randomMovie = ((int)(Math.random() * allmovies.size()) + 1);
            if(!checkUser(randomMovie, username) && !baddummymovies.contains(randomMovie)){
                random.add(randomMovie); 
            }
             
        }
        makeRandomList(random);
        return random;
    }
    
    public ArrayList<String> getLastTen(String username){
        ArrayList<Integer> lasttenID = tableGetLastTen(username);
        ArrayList<String> lastten = new ArrayList<String>();
        for(int i = 0; i < lasttenID.size(); i++){
            lastten.add(findMovie(lasttenID.get(i)));
        }
        return lastten;
    }
    
    public void makeRandomList(ArrayList<Integer> movieIds){
        for(int i = 0; i < movieIds.size(); i++){
            shortlist.add(findMovie(movieIds.get(i)));
        }
    }
    
   
	
	public int CheckUserID(TreeMap<Integer, Integer> usermap, int movieindex){
	    
	    if (usermap.get(movieindex) == null){
	        return 0;
	    }
	    return usermap.get(movieindex);
	}
	
	// Added by Daniel  
      public int CheckUserID(String userid, int movieindex){
        
    	if (allusers.get(userid).userdata.get(movieindex) == null){
			return 0;
		}
    	return allusers.get(userid).userdata.get(movieindex) ;
      }





private static boolean startsWithNumber(String first) {
		first = String.valueOf(first.charAt(0));

		first = first.replaceAll("[0-9]", "X");
		if (first.charAt(0) == 'X')
		{
			return true;
		}
		return false;
     
}
}