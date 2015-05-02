package models;
import java.util.ArrayList;
import java.util.TreeMap;
import models.User;
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


import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

//SVD
import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.SparseMatrix;

//Database imports
import play.db.*;
import javax.sql.DataSource;
import java.sql.*;

public class AllUsers{
    public ArrayList<String> shortlist;
    TreeMap<String, User> allusers;
     ArrayList<String> allmovies;
     ArrayList<String> allgenres;
     Matrix V;
     Connection connection;
    
    public AllUsers(){
    /*TreeMap<Username, Userdata>*/
    allusers = new TreeMap<String, User>();
    /*ArrayList<MovieTitles> index would be movieID*/
    allmovies = new ArrayList<String>();
    /*ArrayList<genres> index would be movieID*/
   allgenres = new ArrayList<String>();
    V = readMatrix("VMatrixMillion6040reduced.txt");
    connection = DB.getConnection("default");
    shortlist = new ArrayList<String>();
    }
    
    public int getSizeOfAll(){
        return allusers.size();
    }
    
    
    public void movieParse(File moviefile) throws IOException{
		String line;
		BufferedReader input = null;

		try {
			input = new BufferedReader(new FileReader(moviefile));

			while((line = input.readLine()) != null) {
				allmovies.add(line);
			}
			
		} catch(FileNotFoundException e) {
			System.out.println("File Not Found");

		} catch (IOException e) {
			System.out.println("File is not readable");
			
		} finally {
			input.close();
			
		}
	}
	
	public void userParse(File userfile) throws IOException{
	    String line;
		BufferedReader input = null;

		try {
			input = new BufferedReader(new FileReader(userfile));

			while((line = input.readLine()) != null) {
				String[] wordArray = line.split("[\t]+");
				String user = wordArray[0];
				User newuserdata = new User();
				addToAll(user, newuserdata);
				newuserdata.setUserName(user);
				
				for(int i = 1; i < wordArray.length; i++){
						String[] rating = wordArray[i].split("[,]");
						allusers.get(user).userdata.put(Integer.parseInt(rating[0]), Integer.parseInt(rating[1]));
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
    public boolean loginInsert(String username, String password){
        
        try{
            Connection conn = DB.getConnection("default");
            if(loginCheck(username, password)){
                return false;
            }
            String query = " insert into loginInfo (username, password)"
        + " values (?, PASSWORD(?))";
            
            PreparedStatement preparedStatement = conn.prepareStatement(query); 
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.execute();
            
            conn.close();
        }   catch (SQLException ex){
                System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
        }
        return true;
        
    }
    
    public boolean loginCheck(String username, String password){
        try{
            Connection conn = DB.getConnection("default");
            
            String query = "SELECT * from loginInfo WHERE username = '"+username+"' AND password = PASSWORD('"+password+"');";
            
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            if (!rs.next()){
                conn.close();
                return false;   
            }
            else{
                conn.close();
            }
        }   catch (SQLException sql){
                System.out.println("exception");
        }
        return true;
    }
    
    public ArrayList<String> loginGetUsers(){
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
    
    public void tableCreate(String username){
        try{
            Connection conn = DB.getConnection("default");
            
            String query = "CREATE TABLE "+username+"Table (movie INTEGER, rating INTEGER);";
            
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.execute();
            
            conn.close();
        }catch(SQLException ex){
            System.out.println("THERE HAS BEEN AN SQLEXCEPTION");
        }
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
       // for(int i = 0; i < 10; i++)
    //    {
      //    random.add((int)(Math.random() * allmovies.size()) + 1);  
    //    }
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
        /*ResultSet rs = statement.executeQuery("SELECT * FROM " + userid +"Table WHERE movie='" + movieindex +"'");
        
        
        if(rs.absolute(1)){
            System.out.println(movieindex);
            return rs.getInt("rating");
        }
        else{
            return 0;
        }
           
      }*/
        
    	if (allusers.get(userid).userdata.get(movieindex) == null){
			return 0;
		}
    	return allusers.get(userid).userdata.get(movieindex) ;
      }
    
    
    // Added by Daniel  
    
    
    // Added by Daniel  //implementing Apache
    public void checkForSimUsers(String user, ArrayList<Integer> movies,ArrayList baddummymovies){
        
         //try {
           // Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
           
           
           int randy;
        	HashMap<String, MovieObject> pearsonmap = new HashMap<String, MovieObject>();
        	TreeMap<Integer, Integer> userMap = tableGetMap(user);
        	int count = 0;
	    	//TreeMap<Integer, Integer> userMap = allusers.get(user).userdata;
	    	/*
	    	DenseMatrix q = new DenseMatrix(1,3952);
		    for (Entry<Integer, Integer> t: userMap.entrySet()){
            	q.setQuick(0,t.getKey()-1,t.getValue());
            }
          
	        int usersize = allusers.size(); //neww
	        int moviesize = allmovies.size(); //neww
            Matrix  qV = q.times(V);
            Matrix qVbyVT = qV.times(V.transpose());
            
              int  columns = qVbyVT.columnSize();
             HashMap<Integer, Movie> map = new HashMap<Integer, Movie>();
             for(int j = 0; j<columns; j++){
	         map.put(j, new Movie(j, qVbyVT.get(0, j)));
            }
            ArrayList<Movie> finalsvd = new ArrayList<Movie>(
			map.values());
            Collections.sort(finalsvd);
            int count = 0;
            int j = 0;
            while (count < 10){
	          if (!userMap.containsKey(finalsvd.get(j).getID()+1) && !baddummymovies.contains(finalsvd.get(j).getID()+1)){
             	 //System.out.println("User:  we want you to see movie with index " + (finalsvd.get(j).getID()+1)); //Because our movie index ratigns doesn't start from 0;
                 movies.add(finalsvd.get(j).getID()+1);
                 count++;
                 j++;
	             }
	             else{
		         j++;
	             }
            }
         }
            */
            while (count<10){
             randy = (int)(Math.random() * allmovies.size()) + 1;
             if (!userMap.containsKey(randy) && !baddummymovies.contains(randy)){
             	 //System.out.println("User:  we want you to see movie with index " + (finalsvd.get(j).getID()+1)); //Because our movie index ratigns doesn't start from 0;
                 movies.add(randy);
                 count++;

	             }
           
        }
            
            
        
        
        
    }
	        
	     
public static Matrix readMatrix(String filename){
		String line;
		int rows;
		int columns;
		DenseMatrix newMatrix = null;

		try {
			BufferedReader input = null;
			
			try {
			 input = new BufferedReader(new FileReader(filename));
			 input.readLine();//done necessarily and intentionally to skip the one line documentation
			 rows = Integer.parseInt(input.readLine());
			 columns = Integer.parseInt(input.readLine());
			 newMatrix = new DenseMatrix(rows,columns);
			while ((line = input.readLine()) != null) {
				if (!(line.isEmpty())) {
					String lineArray[] = line.split(",");
					if (lineArray.length == 3){
						newMatrix.set(Integer.parseInt(lineArray[0]), Integer.parseInt(lineArray[1]), Double.parseDouble(lineArray[2]));
				}
				}
			}
		}
		finally {
			
			input.close();
			
		}
	} catch (FileNotFoundException e) {
		System.out.println("Directory is invalid/Directory does not consist a file");
	} catch (IOException e) {
		System.out.println("Input/Output Exception");
	}
		catch(NullPointerException e){
			System.out.println("An Error occured");
		}
		return newMatrix;
		
		
	}

public static ArrayList readArrayList(String filename){
    ArrayList<Integer> list = new ArrayList<Integer>();
    String line;
    

		try {
			BufferedReader input = null;
			
			try {
		    input = new BufferedReader(new FileReader(filename));
			while ((line = input.readLine()) != null) {
				if (!(line.isEmpty())) {
				
				list.add(Integer.parseInt(line));
						}
			}
		}
		finally {
			
			input.close();
			
		}
	} catch (FileNotFoundException e) {
		System.out.println("Directory is invalid/Directory does not consist a file");
	} catch (IOException e) {
		System.out.println("Input/Output Exception");
	}
		catch(NullPointerException e){
			System.out.println("An Error occured");
		}
    
    
    return list;
}

    
        /*
    Hui's Code
    
    public void checkForSimUsers(String user, ArrayList<Integer> movies){
		
		TreeMap<Integer, Integer> userMap = allusers.get(user).userdata;
	
	    ArrayList<String> userList = new ArrayList<String>();
	    
	    pearson(user, userMap, userList);
		
		
		for(int i = 0; i < userList.size(); i++){
			User u = allusers.get(userList.get(i));
			for (int m : u.userdata.keySet()) {
				if (u.userdata.get(m) == 5 && !movies.contains(m) && movies.size() < 10) {
					movies.add(m);
				}
			}
		}
		
					
	}
	
   public void pearson(String user, TreeMap<Integer, Integer> userMap, 
			ArrayList<String> userList) {
		double avg = 0, top = 0, bottom_x = 0,bottom_y = 0, similarity = 0;
		double[] averages = new double[allusers.keySet().size()];
		double[] x_values = new double[allmovies.size()];
		double[] y_values;
		TreeMap<Double, String> similarities = new TreeMap<Double, String>();

		
		for (String u : allusers.keySet()) {
			for (int m : allusers.get(u).userdata.keySet()) {
				avg += allusers.get(u).userdata.get(m);
			}
			avg = avg / allmovies.size();
			averages[allusers.get(u).userid - 1] = avg;
		}

		for (int i = 1; i <= allmovies.size(); i++) {
			if (allusers.get(user).userdata.containsKey(i)) {
				x_values[i-1] = allusers.get(user).userdata.get(i) - averages[allusers.get(user).userid - 1];
			} else {
				x_values[i-1] = 0 - averages[allusers.get(user).userid - 1];
			}
		}
		
		for (String uid : allusers.keySet()) {
			y_values = new double[allmovies.size()];
			similarity = 0;
			
			for (int i = 1; i < allmovies.size(); i++) {
				if (allusers.get(uid).userdata.containsKey(i)) {
					y_values[i-1] = allusers.get(uid).userdata.get(i) - averages[allusers.get(uid).userid -1];
				} else {
					y_values[i-1] = 0 - averages[allusers.get(uid).userid - 1];
				}
			}
						
			for (int j = 0; j < allmovies.size(); j++) {
				top += x_values[j]*y_values[j];
				bottom_x += x_values[j] * x_values[j];
				bottom_y += y_values[j]*y_values[j];
			}
			similarity = top/(Math.sqrt(bottom_x) * Math.sqrt(bottom_y));
			similarities.put(similarity, uid);

			top = 0;
			bottom_x = 0;
			bottom_y = 0;
		}
		
		NavigableMap<Double,String> nmap = similarities.descendingMap();
		int count = 10;
		for (double i : nmap.keySet()) {
			if  (count > 0 && !user.equals(nmap.get(i))) {
				userList.add(nmap.get(i).toString());
				count -- ;
			}
		}
		

	}
   */ 
}