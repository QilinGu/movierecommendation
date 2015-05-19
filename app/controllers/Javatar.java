package controllers;

import play.data.*;
import play.mvc.*;
import play.mvc.Http.Response;

import models.User;
import models.AllUsers;
import models.Algorithms;
import models.SQL;

import views.html.*;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.mahout.math.Matrix;


public class Javatar extends Controller {

	final static Form<User> userForm = Form.form(User.class);
	final static AllUsers allusers = new AllUsers();
	final static ArrayList<String> lastTen = new ArrayList<String>();
	final static ArrayList<Integer> simmovies = new ArrayList<Integer>();
	final static Algorithms algorithms  = new Algorithms(allusers);
	final static ArrayList<Integer> baddummymovies = algorithms.readArrayList("conf/badmoviesout.txt");
	final static Http.Response r = new Http.Response();

	static ArrayList<Integer> movieIds = new ArrayList<Integer>();
	static int count = 0;
	
	static int usercount = 400;

	public static int getusercount() {
		return usercount;
	}

	public static void setusercount() {
		if (usercount < 800){
			usercount = usercount + 50;
		}
	}

    /**
     * Method is called when on home page of web appliction.
     * First : it checks to see if SVD file is being updated, if so returns Updating page.
     * Second: it checks to see if this is the first instance of the web application,
     *         if so, parses movie.txt file and sets up schedule to update SVD files.
     * Third : it returns the view for the home page. 
     * */
	public static Result home() throws IOException {

		if(allusers.updating) {
			return redirect(controllers.routes.Javatar.updating());
		} 

		if(count == 0) {
			File file = new File("conf/movies.txt");
			allusers.movieParse(file);
			//algorithms.updateSVD();
			count++;
		}

		return ok(home.render(userForm, null));
	}
	
    /**
     * Method returns the updating page for when SVD Files are being updated.
     * First : it checks to make sure files are being updated, if not, then we are 
     *         redirected to the home page.
     * */
	public static Result updating() {
		if(!allusers.updating) {
			return redirect(controllers.routes.Javatar.home());
		}
		return ok(update.render());
	}

    /**
     * Method called when user hits signin button on Home page.
     * First : checks to see if SVD is updating, if it is we redirect to Updating page.
     * Second: Grabs username and password from form submitted.
     * Third : Checks database to ensure that login informatino is correct.
     * Fourth: Redirects to the User page of the one who signed in. 
     * */
	public static Result signin() throws IOException {
		if(allusers.updating) {
			return redirect(controllers.routes.Javatar.updating());
		} 

		Form<User> filledForm = userForm.bindFromRequest();
		User created = filledForm.get();

		if(!allusers.sql.loginCheck(created.username, created.password)) {

			return ok(home.render(userForm, "true"));

		}
		r.setHeader(created.username, created.username);
		return redirect(controllers.routes.Javatar.user(created.username));
	}

    /**
     * Method signs out the user.
     * First : checks to see if SVD is being updated, redirects to Updating page if is. 
     * Second: redirects to the home page.
     * */
	public static Result signout(String user) {
		r.setHeader(user, null);
		if(allusers.updating) {
			return redirect(controllers.routes.Javatar.updating());
		} 
		return redirect("/");
	}

    /**
     * Method is called when a new user wants to register.
     * First : checks to see if SVD is being updated, redirects to Updating page if is.
     * Second: make sure movieIds is empty.
     * Third : make sure that shortlist is empty.
     * Fourth: renders the Registration page.
     * */
	public static Result register() {
		if(allusers.updating) {
			return redirect(controllers.routes.Javatar.updating());
		} 

		if(movieIds.size() > 0) {
			movieIds.clear();
		} 

		if(allusers.shortlist.size() > 0) {
			allusers.shortlist.clear();
		}
		movieIds = allusers.getTenRandomIDS(baddummymovies);//Daniel

		return ok(regpage.render(userForm, allusers.shortlist, null, null));
	}

    /**
     * Method gets called when user clicks Register button on Registration page.
     * First  : checks to see if SVD is being updated, redirects to Updating page if is.
     * Second : gets all data from registration page from the form.
     * Third  : verifies username and password are valid.
     * Fourth : creates a userTable for the new user.
     * Fifth  : adds movies and ratings selected to userTable.
     * Sixth  : checks to see if user rated ten movies 
     * Seventh: if user hasn't rated ten movies, return a new page with more movies to rate
     * Eighth : redirects to the new Users page if ten movies have been rated.
     * */
	public static Result submit() {
		if(allusers.updating) {
			return redirect(controllers.routes.Javatar.updating());
		} 

		Form<User> filledForm = userForm.bindFromRequest();
		User created = filledForm.get();

		if(created.username.length() == 0 || created.password.length() == 0) {

			return ok(regpage.render(userForm, allusers.shortlist, "true", "true"));

		}

		if(!allusers.sql.loginInsert(created.username, created.password)) {

			return ok(regpage.render(userForm, allusers.shortlist, "true", "true"));


		} else {

			allusers.sql.tableCreate(created.username);

		}

		created.addToRatings(movieIds);

		for(int i = 0; i < created.ratingssize; i++) {

			allusers.sql.tableInsert(created.username, created.movies.get(i), created.ratings.get(i));

		}

		int size = allusers.sql.tableSize(created.username);

		if(size < 10) {

			if(!movieIds.isEmpty()) {

				movieIds.clear();
			}

			allusers.shortlist.clear();
			movieIds = allusers.getTenRandomIDS(created.username, baddummymovies);//Daniel
			return ok(loadmore.render(userForm, allusers.shortlist, created.username));
		}

		r.setHeader(created.username, created.username);
		return redirect(controllers.routes.Javatar.user(created.username));
	}

    /**
     * Method gets called from LoadMore page, which has ten movies to be rated by new user.
     * First : checks to see if SVD is being updated, redirects to Updating page if is.
     * Second: gets data from the form.
     * Third : checks to see if user rated ten movies 
     * Fourth: if user hasn't rated ten movies, return a new page with more movies to rate
     * Fifth : redirects to the new Users page if ten movies have been rated.
     * */
	public static Result addMovies(String username) {
		if(allusers.updating){
			return redirect(controllers.routes.Javatar.updating());
		} 

		Form<User> filledForm = userForm.bindFromRequest();
		User created = filledForm.get();

		created.addToRatings(movieIds);

		for(int i = 0; i < created.ratingssize; i++) {

			allusers.sql.tableInsert(username, created.movies.get(i), created.ratings.get(i));
		}

		int size = allusers.sql.tableSize(username);
		if(size < 10) {

			if(!movieIds.isEmpty()) {

				movieIds.clear();
			}

			allusers.shortlist.clear();
			movieIds = allusers.getTenRandomIDS(username, baddummymovies);//Daniel
			return ok(loadmore.render(userForm, allusers.shortlist, username));
		}
		r.setHeader(username, username);
		return redirect(controllers.routes.Javatar.user(username));
	}

    /**
     * Method gets called when User goes to User page.
     * First : make sure URL is correct, if not takes us to home page.
     * Second: checks to see if SVD is being updated, redirects to Updating page if is.
     * Third : gets ten recent movies for the user page.
     * Fourth: get number of movies rated.
     * Fifth : returns User page view.
     * */
	public static Result user(String name) {

		Map<String, String> map = r.getHeaders();
		if(!map.containsKey(name) || !map.containsValue(name)) {
			return redirect(controllers.routes.Javatar.home());
		}
		if(allusers.updating) {
			return redirect(controllers.routes.Javatar.updating());
		} 
		ArrayList<String> recentmovies = allusers.getLastTen(name);
		int moviesrated = allusers.sql.tableSize(name);
		return ok(user.render(name, recentmovies, moviesrated));
	}

    /**
     * Method gets called when User clicks Find Recommendation button on User page.
     * First  : checks to make sure URL is correct.
     * Second : checks to see if SVD is being updated, redirects to Updating page if is.
     * Third  : makes sure movieIds list is empty.
     * Fourth : makes sure simmovies list is less than ten.
     * Fifth  : gets recommended movies for the user based of SVD algorithm.
     * Sixth  : returns the Recommended Movie page.
     * */
	public static Result recommend(String user) {
		Map<String, String> map = r.getHeaders();
		if(!map.containsKey(user) || !map.containsValue(user)) {
			return redirect(controllers.routes.Javatar.home());
		}
		if(allusers.updating) {
			return redirect(controllers.routes.Javatar.updating());
		} 
		if(!movieIds.isEmpty()) {

			movieIds.clear();
		}

		if(simmovies.size() >= 10) {

			simmovies.clear();
		}

		algorithms.recommendMovies(user, simmovies, baddummymovies);
		ArrayList<String> recMovies = new ArrayList<String>();

		for(int i = 0; i < simmovies.size(); i++) {

			String movie = allusers.findMovie(simmovies.get(i));
			movieIds.add(simmovies.get(i));
			recMovies.add(movie);
		}

		return ok(recpage.render(userForm, recMovies, user));
	}

    /**
     * Method gets called when user submits ratings on Recommended page.
     * First : checks to see if SVD is being updated, redirects to Updating page if is.
     * Second: gets data from the form.
     * Third : adds movies and ratings to userTable.
     * Fourth: clears simmovies and movieIds lists.
     * Fifth : gets ten more recommended movies.
     * Sixth : returns the Recommended page with new recommended movies.
     * */
	public static Result submitrec(String user) throws IOException {
		if(allusers.updating){
			return redirect(controllers.routes.Javatar.updating());
		} 

		PrintWriter outw = null;

		try {

			outw = new PrintWriter(new FileWriter("conf/dataset.csv", true));

			Form<User> filledForm = userForm.bindFromRequest();
			User newdata = filledForm.get();

			newdata.addToRatings(movieIds);

			for(int i = 0; i < newdata.ratingssize; i++) {

				allusers.sql.tableInsert(user, newdata.movies.get(i), newdata.ratings.get(i));

				outw.println(user + "," + newdata.movies.get(i) + "," + newdata.ratings.get(i)+".0");

			}

		} catch(FileNotFoundException e) {

			System.out.println("File Not Found");

		} catch (IOException e) {

			System.out.println("File is not readable");

		} finally {

			outw.close();

		}

		simmovies.clear();
		movieIds.clear();
		algorithms.recommendMovies(user, simmovies,baddummymovies);
		ArrayList<String> recMovies = new ArrayList<String>();

		for(int i = 0; i < simmovies.size(); i++) {

			String movie = allusers.findMovie(simmovies.get(i));
			movieIds.add(simmovies.get(i));
			recMovies.add(movie);
		}

		return ok(recpage.render(userForm, recMovies, user));
	}
}