package controllers;

import play.data.*;
import play.mvc.*;
import models.User;
import models.AllUsers;
import views.html.*;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.List;
import java.util.TreeMap;

//Added by Daniel
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

//Database imports
import play.db.*;
import javax.sql.DataSource;
import java.sql.*;


public class Register extends Controller {
    
	final static Form<User> userForm = Form.form(User.class);
	final static AllUsers allusers = new AllUsers();
    static ArrayList<Integer> movieIds = new ArrayList<Integer>();
    final static ArrayList<String> lastTen = new ArrayList<String>();
    static String username = null;
    static String password = null;
    final static ArrayList<Integer> simmovies = new ArrayList<Integer>();
    static int count = 0;

    public static Result home() throws IOException {
        if(count == 0){
            File file = new File("conf/movies.txt");
            File userfile = new File("conf/users.txt");
            allusers.movieParse(file);
            allusers.userParse(userfile);
            count++;
        }
        return ok(home.render(userForm, null));
    }
    
    public static Result signin() {
        
        Form<User> filledForm = userForm.bindFromRequest();
        User created = filledForm.get();
        
        if(!allusers.loginCheck(created.username, created.password)){
            return ok(home.render(userForm, "true"));
        }
        
        username = created.username;
        password = created.password;

        return redirect("/user");
    }

    public static Result register() {
                
        movieIds = allusers.getTenRandomIDS();
        //allusers.loginPrint();

        return ok(regpage.render(userForm, allusers.shortlist, null, null));
    }
    
    public static Result submit() {
        
        Form<User> filledForm = userForm.bindFromRequest();
        User created = filledForm.get();
        if(username == null || password == null){

            if(created.username.length() == 0 || created.password.length() == 0){
                return ok(regpage.render(userForm, allusers.shortlist, "true", "true"));
            }

            password = created.password;
            username = created.username;
            if(allusers.loginInsert(username, password)){
                allusers.tableCreate(username);
            }else{
                return ok(regpage.render(userForm, allusers.shortlist, "true", "true"));
            }

        }
        
        created.addToRatings(movieIds);

        if(!allusers.hasUser(username)){
    	    for(int i = 0; i < created.ratingssize; i++){
    
                allusers.tableInsert(username, created.movies.get(i), created.ratings.get(i)); 

    	        //created.addRated(allusers.findMovie(created.movies.get(i)), i);
    	        
    	    }
    
            //allusers.addToAll(username, created);
            int size = allusers.tableSize(username);
            if(size < 10){
                if(!movieIds.isEmpty()){
                    movieIds.clear();
                }
                allusers.shortlist.clear();
                movieIds = allusers.getTenRandomIDS();
                return ok(loadmore.render(userForm, allusers.shortlist));
            }
        }
        
        else{
            //User olddata = allusers.getUserData(username);
            
            for(int i = 0; i < created.ratingssize; i++)
    	    {
    	        allusers.tableInsert(username, created.movies.get(i), created.ratings.get(i));
    	        
    	        //olddata.addData(created.movies.get(i), created.ratings.get(i));
    	        //olddata.addRated(allusers.findMovie(created.movies.get(i)), i);
    	    }
    	    int size = allusers.tableSize(username);
    	    if(size < 10){
                if(!movieIds.isEmpty()){
                    movieIds.clear();
                }
                allusers.shortlist.clear();
                movieIds = allusers.getTenRandomIDS();
                return ok(loadmore.render(userForm, allusers.shortlist));
            }
        }
        
        //allusers.tablePrint(username);      
        
        return redirect("/user");
        
    }
    
    public static Result user() {
        ArrayList<String> recentmovies = allusers.getLastTen(username);
        int moviesrated = allusers.tableSize(username);
        return ok(user.render(username, recentmovies, moviesrated));
        
    }
    
    public static Result recommend() {
        if(!movieIds.isEmpty()){
            movieIds.clear();
        }
        if(simmovies.size() >= 10){
            simmovies.clear();
        }
        allusers.checkForSimUsers(username, simmovies);
        ArrayList<String> recMovies = new ArrayList<String>();
        //System.out.println(simmovies.size());
        
        for(int i = 0; i < simmovies.size(); i++)
        {
            //System.out.println("movie id : " + simmovies.get(i));
            String movie = allusers.findMovie(simmovies.get(i));
            movieIds.add(simmovies.get(i));
            recMovies.add(movie);
        }
        return ok(somethingelse.render(userForm, recMovies));
    }
    
    public static Result submitrec() throws IOException {
        //Added by Daniel
        PrintWriter outw = new PrintWriter(new BufferedWriter(new FileWriter("data/dataset.csv", true)));

        Form<User> filledForm = userForm.bindFromRequest();
    	User newdata = filledForm.get();
    	newdata.addToRatings(movieIds);
        
    	for(int i = 0; i < newdata.ratingssize; i++)
    	{
      	    allusers.tableInsert(username, newdata.movies.get(i), newdata.ratings.get(i));

    	    //Added by Daniel
    	    outw.println(username + "," + newdata.movies.get(i) + "," + newdata.ratings.get(i)+".0");

    	}
    	//Added by Daniel
        outw.close();
    	simmovies.clear();
    	movieIds.clear();
    	allusers.checkForSimUsers(username, simmovies);
        ArrayList<String> recMovies = new ArrayList<String>();
        for(int i = 0; i < simmovies.size(); i++)
        {
            String movie = allusers.findMovie(simmovies.get(i));
            movieIds.add(simmovies.get(i));
            recMovies.add(movie);
        }

    	return ok(somethingelse.render(userForm, recMovies));
    }
}