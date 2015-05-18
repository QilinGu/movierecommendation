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
import play.mvc.Http.Response;
import java.util.Map;

//Added by Daniel
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import org.apache.mahout.math.Matrix;

//Database imports
import play.db.*;
import javax.sql.DataSource;
import java.sql.*;


public class Register extends Controller {
    
	final static Form<User> userForm = Form.form(User.class);
	final static AllUsers allusers = new AllUsers();
    final static ArrayList<String> lastTen = new ArrayList<String>();
    final static ArrayList<Integer> simmovies = new ArrayList<Integer>();
    final static ArrayList<Integer> baddummymovies = allusers.readArrayList("conf/badmoviesout.txt");
    final static Http.Response r = new Http.Response();

    static ArrayList<Integer> movieIds = new ArrayList<Integer>();
    static int count = 0;
    static int usercount = 400;
    
    public static int getusercount(){
        return usercount;
    }
    
    public static void setusercount(){
        if (usercount < 800){
        usercount = usercount + 50;
        }
    }
    
    public static Result home() throws IOException {
        
        if(allusers.updating){
            return redirect(controllers.routes.Register.updating());
        } 
        
        if(count == 0){
            System.out.println("THIS IS A NEW INSTANCE");
            File file = new File("conf/moviesout.txt");
            allusers.movieParse(file);
            //allusers.updateSVDsmall();
            count++;
        }

        return ok(home.render(userForm, null));
    }
    
    public static Result updating(){
        return ok(update.render());
    }
    
    public static Result signin() throws IOException {
        if(allusers.updating){
            return redirect(controllers.routes.Register.updating());
        } 
        
        Form<User> filledForm = userForm.bindFromRequest();
        User created = filledForm.get();
        
        if(!allusers.loginCheck(created.username, created.password)) {
    
            return ok(home.render(userForm, "true"));
        
        }
        r.setHeader("curr", created.username);
        return redirect(controllers.routes.Register.user(created.username));
    }
    
    public static Result signout() {
        r.setHeader("curr", null);
        if(allusers.updating){
            return redirect(controllers.routes.Register.updating());
        } 
        return redirect("/");
    }

    public static Result register() {
        if(allusers.updating){
            return redirect(controllers.routes.Register.updating());
        } 
        
        if(movieIds.size() > 0){
            movieIds.clear();
        } 
        
        if(allusers.shortlist.size() > 0){
            allusers.shortlist.clear();
        }
        movieIds = allusers.getTenRandomIDS(baddummymovies);//Daniel

        return ok(regpage.render(userForm, allusers.shortlist, null, null));
    }
    
    public static Result submit() {
        if(allusers.updating){
            return redirect(controllers.routes.Register.updating());
        } 
        
        Form<User> filledForm = userForm.bindFromRequest();
        User created = filledForm.get();

        if(created.username.length() == 0 || created.password.length() == 0) {
    
            return ok(regpage.render(userForm, allusers.shortlist, "true", "true"));
    
        }

        if(!allusers.loginInsert(created.username, created.password)) {
            
            return ok(regpage.render(userForm, allusers.shortlist, "true", "true"));
            

        } else {
    
            allusers.tableCreate(created.username);
            
        }
        
        created.addToRatings(movieIds);

        for(int i = 0; i < created.ratingssize; i++) {

            allusers.tableInsert(created.username, created.movies.get(i), created.ratings.get(i));
    	        
    	}
    	    
    	int size = allusers.tableSize(created.username);
    	    
        if(size < 10) {
            
            if(!movieIds.isEmpty()) {
                    
                movieIds.clear();
            }
            
            allusers.shortlist.clear();
            movieIds = allusers.getTenRandomIDS(created.username, baddummymovies);//Daniel
            return ok(loadmore.render(userForm, allusers.shortlist, created.username));
        }
        
        r.setHeader("curr", created.username);
        return redirect(controllers.routes.Register.user(created.username));
    }
    
    public static Result addMovies(String username) {
        if(allusers.updating){
            return redirect(controllers.routes.Register.updating());
        } 
        
        Form<User> filledForm = userForm.bindFromRequest();
        User created = filledForm.get();
        
        created.addToRatings(movieIds);
        
        for(int i = 0; i < created.ratingssize; i++) {
    	    
    	    allusers.tableInsert(username, created.movies.get(i), created.ratings.get(i));
    	}
    	
    	int size = allusers.tableSize(username);
    	if(size < 10) {
    	    
            if(!movieIds.isEmpty()) {
                
                movieIds.clear();
            }
            
            allusers.shortlist.clear();
            movieIds = allusers.getTenRandomIDS(username, baddummymovies);//Daniel
            return ok(loadmore.render(userForm, allusers.shortlist, username));
        }
        r.setHeader("curr", username);
        return redirect(controllers.routes.Register.user(username));
    }
    

    public static Result user(String name) {
        Map<String, String> map = r.getHeaders();
        if(!map.containsKey("curr") || !map.containsValue(name)){
            return redirect(controllers.routes.Register.home());
        }
        if(allusers.updating){
            return redirect(controllers.routes.Register.updating());
        } 
        ArrayList<String> recentmovies = allusers.getLastTen(name);
        int moviesrated = allusers.tableSize(name);
        return ok(user.render(name, recentmovies, moviesrated));
    }
    
    public static Result recommend(String user) {
        Map<String, String> map = r.getHeaders();
        if(!map.containsKey("curr") || !map.containsValue(user)){
            return redirect(controllers.routes.Register.home());
        }
        if(allusers.updating){
            return redirect(controllers.routes.Register.updating());
        } 
        if(!movieIds.isEmpty()) {
            
            movieIds.clear();
        }
        
        if(simmovies.size() >= 10) {
            
            simmovies.clear();
        }
        
        allusers.checkForSimUsers(user, simmovies,baddummymovies);
        ArrayList<String> recMovies = new ArrayList<String>();

        for(int i = 0; i < simmovies.size(); i++) {

            String movie = allusers.findMovie(simmovies.get(i));
            movieIds.add(simmovies.get(i));
            recMovies.add(movie);
        }
        
        return ok(somethingelse.render(userForm, recMovies, user));
    }
    
    public static Result submitrec(String user) throws IOException {
        if(allusers.updating){
            return redirect(controllers.routes.Register.updating());
        } 
        //Added by Daniel
        PrintWriter outw = null;
        
        try {
            
            outw = new PrintWriter(new FileWriter("conf/dataset.csv", true));

            Form<User> filledForm = userForm.bindFromRequest();
    	    User newdata = filledForm.get();

    	    newdata.addToRatings(movieIds);
        
    	    for(int i = 0; i < newdata.ratingssize; i++) {
    	        
      	        allusers.tableInsert(user, newdata.movies.get(i), newdata.ratings.get(i));

    	        //Added by Daniel
    	        outw.println(user + "," + newdata.movies.get(i) + "," + newdata.ratings.get(i)+".0");

    	    }
    	    
    	} catch(FileNotFoundException e) {

			System.out.println("File Not Found");

		} catch (IOException e) {

			System.out.println("File is not readable");

		} finally {
		    
    	    outw.close();
    
    	}
    	//Added by Daniel
        
    	simmovies.clear();
    	movieIds.clear();
    	allusers.checkForSimUsers(user, simmovies,baddummymovies);
        ArrayList<String> recMovies = new ArrayList<String>();
        
        for(int i = 0; i < simmovies.size(); i++) {
            
            String movie = allusers.findMovie(simmovies.get(i));
            movieIds.add(simmovies.get(i));
            recMovies.add(movie);
        }

    	return ok(somethingelse.render(userForm, recMovies, user));
    }
}