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


//Added by Daniel
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;


public class Register extends Controller {

	final static Form<User> userForm = Form.form(User.class);
	final static AllUsers allusers = new AllUsers();
    static ArrayList<Integer> movieIds = null;
    final static ArrayList<String> lastTen = new ArrayList<String>();
    static String username = null;


    public static Result index() throws IOException {
        File file = new File("conf/movies.txt");
        File userfile = new File("conf/users.txt");
        allusers.movieParse(file);
        allusers.userParse(userfile);
        movieIds = allusers.getTenRandomIDS();
        return ok(regpage.render(userForm, allusers.shortlist, null));
    }
    
    
    public static Result submit() throws IOException {
        Form<User> filledForm = userForm.bindFromRequest();
        User created = filledForm.get();
        if(username == null){
            if(created.username.length() == 0){
                return ok(regpage.render(userForm, allusers.shortlist, "true"));
            }      
            username = created.username;
        }
        created.addToRatings(movieIds);

        if(!allusers.hasUser(username)){
    	    for(int i = 0; i < created.ratingssize; i++){
            
    	        created.addRated(allusers.findMovie(created.movies.get(i)), i);
            }
        
            allusers.addToAll(username, created);
            
            if(created.ratedMovies.size() < 10){
                movieIds.clear();
                allusers.shortlist.clear();
                movieIds = allusers.getTenRandomIDS();
                return ok(loadmore.render(userForm, allusers.shortlist));
            }
        }
        
        else{
            User olddata = allusers.getUserData(username);
            
            for(int i = 0; i < created.ratingssize; i++)
    	    {
    	        olddata.addData(created.movies.get(i), created.ratings.get(i));
    	        olddata.addRated(allusers.findMovie(created.movies.get(i)), i);
    	    }
    	    
    	    if(olddata.ratedMovies.size() < 10){
                movieIds.clear();
                allusers.shortlist.clear();
                movieIds = allusers.getTenRandomIDS();
                return ok(loadmore.render(userForm, allusers.shortlist));
            }
        }
        
        return redirect("/user");
        
    }
    
    public static Result user() {
        User data = allusers.getUserData(username);
        return ok(user.render(data));
        
    }
    
    public static Result recommend() throws IOException {
        movieIds.clear();
        User user = allusers.getUserData(username);
        allusers.checkForSimUsers(username, user.simmovies);
        ArrayList<String> recMovies = new ArrayList<String>();
        for(int i = 0; i < user.simmovies.size(); i++)
        {
            String movie = allusers.findMovie(user.simmovies.get(i));
            movieIds.add(user.simmovies.get(i));
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
    	User olddata = allusers.getUserData(username);

    	for(int i = 0; i < newdata.ratingssize; i++)
    	{
    	    //Added by Daniel
    	    outw.println(username + "," + newdata.movies.get(i) + "," + newdata.ratings.get(i)+".0");
    	    olddata.addData(newdata.movies.get(i), newdata.ratings.get(i));
    	    olddata.addRated(allusers.findMovie(newdata.movies.get(i)), i);
    	}
    	//Added by Daniel
        outw.close();
    	allusers.addToAll(username, olddata);
    	olddata.simmovies.clear();
    	movieIds.clear();
    	allusers.checkForSimUsers(username, olddata.simmovies);
        ArrayList<String> recMovies = new ArrayList<String>();
        for(int i = 0; i < olddata.simmovies.size(); i++)
        {
            String movie = allusers.findMovie(olddata.simmovies.get(i));
            movieIds.add(olddata.simmovies.get(i));
            recMovies.add(movie);
        }

    	return ok(somethingelse.render(userForm, recMovies));
    }


}
