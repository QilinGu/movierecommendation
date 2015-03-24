package models;
import java.util.ArrayList;
import java.util.TreeMap;


public class User {
	public String username;
	public String email;
	public String password;
	public String confirm_email;
	public String confirm_password;
	public Integer movie1;
	public Integer movie2;
	public Integer movie3;
	public Integer movie4;
	public Integer movie5;
	public Integer movie6;
	public Integer movie7;
	public Integer movie8;
	public Integer movie9;
	public Integer movie10;
	public int ratingssize;
	public int moviessize;
	
	public int userid;

    /*TESTING PURPOSE*/
	public ArrayList<Integer> ratings = new ArrayList<Integer>();
	public ArrayList<Integer> movies = new ArrayList<Integer>();

	public ArrayList<Integer> simmovies = new ArrayList<Integer>();
	public ArrayList<String> ratedMovies = new ArrayList<String>();


	/* TreeMap<MovieID, Ratings> */
	public TreeMap<Integer, Integer> userdata = new TreeMap<Integer, Integer>();
	
	
	public void addToRatings(ArrayList<Integer> ids){
	    //Changed by Daniel
	    if(movie1 != null && movie1 >0)
	    {
	        userdata.put(ids.get(0), movie1);
	        ratings.add(movie1);
	        movies.add(ids.get(0));
	        
	    }
	    if(movie2 != null && movie2 >0){
	        userdata.put(ids.get(1), movie2);
	        ratings.add(movie2);
	        movies.add(ids.get(1));
	    }
	    
	    if(movie3 != null && movie3 >0){
	        userdata.put(ids.get(2), movie3);
	        ratings.add(movie3);
	        movies.add(ids.get(2));
	    }
	    if(movie4 != null && movie4 >0){
	        userdata.put(ids.get(3), movie4);
	        ratings.add(movie4);
	        movies.add(ids.get(3));
	    }
        if(movie5 != null && movie5 >0){
            userdata.put(ids.get(4), movie5);
	        ratings.add(movie5);
	        movies.add(ids.get(4));
        }
	    if(movie6 != null && movie6 >0){
	        userdata.put(ids.get(5), movie6);
	        ratings.add(movie6);
	        movies.add(ids.get(5));
	    }
	    if(movie7 != null && movie7 >0){
	        userdata.put(ids.get(6), movie7);
	        ratings.add(movie7);
	        movies.add(ids.get(6));
	    }
	    if(movie8 != null && movie8 >0){
	        userdata.put(ids.get(7), movie8);
	        ratings.add(movie8);
	        movies.add(ids.get(7));
	    }
	    if(movie9 != null && movie9 >0){
	        userdata.put(ids.get(8), movie9);
	        ratings.add(movie9);
	        movies.add(ids.get(8));
	    }
	    if(movie10 != null && movie10 >0){
	        userdata.put(ids.get(9), movie10);
	        ratings.add(movie10);
	        movies.add(ids.get(9));
	    }

	    ratingssize = ratings.size();
	    moviessize = movies.size();
	    
	}
	
	
	public void setUserID (int id){
	    userid = id;
	}
	
	public void setUserName (String name){
	    username = name;
	}
	
	public Integer getRating(int i){
	    return ratings.get(i);
	}
	
	public Integer getMovie(int i){
	    return movies.get(i);
	}
	
	
	public void addData(int movie, int rating){
	    userdata.put(movie, rating);
	}
	
	public void addRated(String movie, int i){
	    ratedMovies.add(i, movie);
	}
	
	public boolean sizeright(){
	    if(ratedMovies.size() >= 10){
	        return true;
	    }
	    return false;
	}
}
