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

public class AllUsers {
    
    public ArrayList<String> shortlist;
    private TreeMap<String, User> allusers;
    private ArrayList<String> allmovies;
    private ArrayList<String> allgenres;
    public static Matrix V;
    private Connection connection;
    public static boolean updating;
    
    public AllUsers() {
        
        /*TreeMap<Username, Userdata>*/
        allusers = new TreeMap<String, User>();
        
        /*ArrayList<MovieTitles> index would be movieID*/
        allmovies = new ArrayList<String>();
        
        /*ArrayList<genres> index would be movieID*/
        allgenres = new ArrayList<String>();
        
        V = readMatrix("conf/VMatrixMillion6040reduced.txt");
        //V = readMatrix("conf/Vmatrix3users.txt");
        connection = DB.getConnection("default");
        shortlist = new ArrayList<String>();
        updating = false;
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
    
    public static void setV(Matrix newV){
        V = newV;
    }
    
    public void movieParse(File moviefile) throws IOException {
        
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
	
	public void userParse(File userfile) throws IOException {
	    
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
				
				for(int i = 1; i < wordArray.length; i++) {
				    
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
    
    
    // Added by Daniel  //implementing Apache
    public void checkForSimUsers(String user, ArrayList<Integer> movies,ArrayList baddummymovies){
        
         //try {
           // Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
           
           
           int randy;
        	HashMap<String, MovieObject> pearsonmap = new HashMap<String, MovieObject>();
        	TreeMap<Integer, Integer> userMap = tableGetMap(user);
        	//int count = 0;
	    	//TreeMap<Integer, Integer> userMap = allusers.get(user).userdata;
	    	
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
            
/*            while (count<10){
             randy = (int)(Math.random() * allmovies.size()) + 1;
             if (!userMap.containsKey(randy) && !baddummymovies.contains(randy)){
             	 //System.out.println("User:  we want you to see movie with index " + (finalsvd.get(j).getID()+1)); //Because our movie index ratigns doesn't start from 0;
                 movies.add(randy);
                 count++;

	             }
           
        }
            
            
        
        
        
    }
	        */
	  
public static void writeMatrix(Matrix inputM, String filename, String title) throws IOException
	{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
		
		int rows = inputM.numRows();
		int columns = inputM.numCols();
		double value;
		out.println(title);
		out.println(rows);
		out.println(columns);
		for (int i = 0; i<rows; i++){
			for (int j = 0; j < columns; j++){
				if ((value = inputM.getQuick(i, j)) !=0){
					out.println(i + "," + j + "," +value );
				}
			}
		}
		out.close();
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

public static DenseMatrix readM(String filename, int newrow) {
        BufferedReader reader = null;
        DenseMatrix m = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line;
            try {
                int row = Integer.parseInt(reader.readLine());
                int column = Integer.parseInt(reader.readLine());
                m = new DenseMatrix(newrow, column);
                line = reader.readLine();
                int i = 0;
                while (line != null && i < newrow) {// don't forget to change this after debugging
                    String[] numbers = line.split(",");
                    for (int j = 0; j < numbers.length; j++) {
                        m.setQuick(i, j, Double.parseDouble(numbers[j]));
                    }
 
                    line = reader.readLine();
                    i++;
                }
            } catch (IOException e) {
                //ignored
            }
        } catch (FileNotFoundException e) {
            //ignored
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }   
        return m;
    }

public static Matrix reduceMatrixV(String filename,double percentkept){
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
			 int newcolumns = (int) ((percentkept/100) * columns); 
			 newMatrix = new DenseMatrix(rows,newcolumns);
			while ((line = input.readLine()) != null) {
				if (!(line.isEmpty())) {
					String lineArray[] = line.split(",");
					if (lineArray.length == 3 && Integer.parseInt(lineArray[1]) < newcolumns){
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


public void updateSVD()throws IOException{

    int count= 6040;
    ArrayList<String> users = loginGetUsers();
    int usercount = 6040 + users.size();
    DenseMatrix M = readM("conf/M.txt",usercount);
    System.out.println("Grouplens DONE");
    for (String user: users){
       TreeMap<Integer, Integer> userMap = tableGetMap(user);
      for (Entry<Integer, Integer> t: userMap.entrySet()){
            	M.setQuick(count,t.getKey()-1,t.getValue());
            }
            count++;
    }
    	 System.out.println("Calculating SVD");
	     long start_time = System.currentTimeMillis();
		SingularValueDecomposition t = new SingularValueDecomposition(M);
		System.out.println("SVD Done");
		//long end_time = System.currentTimeMillis();
		//long time = end_time-start_time;
		 //time = time/1000;
		//System.out.println("The time of SVD in seconds is " + time);
		
		writeMatrix(t.getV(),"conf/VmatrixMillion6040full.txt","This is a result of SVD recalculation");
		Matrix newV = reduceMatrixV("conf/VmatrixMillion6040full.txt",20);
	//we need to delete the matrix because it is very large and useless at this point
	File f = new File("conf/VmatrixMillion6040full.txt");
				System.out.println("Was the file deleted? " + f.delete());//we need to delete the matrix because it is very large and useless at this point
		writeMatrix(newV,"conf/VMatrixMillion6040reduced.txt","(SVD Recalculation)This the reduced matrix of the original centered Million ratings");
        V = readMatrix("conf/VMatrixMillion6040reduced.txt");	
		System.out.println("SVD UPDATE IS COMPLETE");
		
}

public void updateSVDsmall()throws IOException{
    
        Timer timer = new Timer();
        Calendar date = Calendar.getInstance();
        date.set(
          Calendar.DAY_OF_WEEK,
          Calendar.MONDAY
        );
        date.set(Calendar.HOUR, 6);
        date.set(Calendar.MINUTE, 45);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        System.out.println(date.getTime());
        // Schedule to run every Sunday in 10 PM
        timer.schedule(
          new Update(),
          date.getTime(),
          //1000 * 60 * 60 * 24 * 7
        
        //every 5 minutes
        //1000 * 60 * 5
        );
}


     
}