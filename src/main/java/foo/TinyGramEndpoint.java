package foo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

@Api(name = "myApi",
     version = "v1",
     audiences = "852096843957-2vb1jhobb8t1311kqf2v269chbbmp2km.apps.googleusercontent.com",
  	 clientIds = "852096843957-2vb1jhobb8t1311kqf2v269chbbmp2km.apps.googleusercontent.com",
     namespace =
     @ApiNamespace(
		   ownerDomain = "tinygram-272116.appspot.com/",
		   ownerName = "tinygram-272116.appspot.com/",
		   packagePath = "")
     )
public class TinyGramEndpoint {
		

	@ApiMethod(name = "follow",
			   httpMethod = ApiMethod.HttpMethod.GET)
	public Entity follow(User user, TinyUser tn) throws UnauthorizedException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		List<String> followings = null;
		Key k = KeyFactory.createKey("User", user.getEmail());
		Entity ent = null;
		try {
			ent = datastore.get(k);
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		followings = (List<String>) ent.getProperty("followings");
		followings.add(tn.getEmail());
		ent.setProperty("followings",followings);
		Transaction txn = datastore.beginTransaction();
		datastore.put(ent);
		txn.commit();
		return ent;
	}
	
	
	@ApiMethod(name = "followings", path= "user/followings",
			   httpMethod = ApiMethod.HttpMethod.GET)
	public List<String> getFollowings(User user) throws UnauthorizedException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		List<String> followings = null;

		Key k = KeyFactory.createKey("User", user.getEmail());
		try {
			followings = (List<String>) datastore.get(k).getProperty("followings");
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}

		return followings;
	}

	@ApiMethod(name = "research",
			   httpMethod = ApiMethod.HttpMethod.GET)
	public List<Entity> research(User user) throws UnauthorizedException {
		
		List<Entity> ents =null;
		return ents;
	}
	
	@ApiMethod(name = "timeline", path="timeling",
			   httpMethod = ApiMethod.HttpMethod.GET)
		public CollectionResponse<Entity> getTimeline(User user, @Nullable @Named("next") String cursorString)
				throws UnauthorizedException {

			if (user == null) {
				throw new UnauthorizedException("Invalid credentials");
			}

			Query q = new Query("Post").
			    setFilter(new FilterPredicate("receivers", FilterOperator.EQUAL, user.getEmail()));
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);

			FetchOptions fetchOptions = FetchOptions.Builder.withLimit(2);

			if (cursorString != null) {
				fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
			}

			QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
			cursorString = results.getCursor().toWebSafeString();

			return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
		}
	
	@ApiMethod(name = "getMyPost", path="user/posts",
		   httpMethod = ApiMethod.HttpMethod.GET)
	public CollectionResponse<Entity> getPost(User user, @Nullable @Named("next") String cursorString)
			throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Query q = new Query("Post").
		    setFilter(new FilterPredicate("owner", FilterOperator.EQUAL, user.getEmail()));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(2);

		if (cursorString != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}

		QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
		cursorString = results.getCursor().toWebSafeString();

		return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
	}
	
	@ApiMethod(name = "postMsg", httpMethod = HttpMethod.POST)
	public Entity postMsg(User user, PostMessage pm) throws UnauthorizedException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		if (user == null) { // valider authentification
			throw new UnauthorizedException("Invalid credentials");
		}

		Entity post = new Entity("Post", Long.MAX_VALUE-(new Date()).getTime()+":"+user.getEmail());
		post.setProperty("owner", user.getEmail());
		post.setProperty("url", pm.url);
		post.setProperty("body", pm.body);
		post.setProperty("likesC", 0);
		HashSet<String> likes=new HashSet<String>();
		post.setProperty("likes", likes);
		post.setProperty("date", new Date());
				
		List<String> receivers = new ArrayList<String>();
		Key k = KeyFactory.createKey("User", user.getEmail());
		try {
			receivers = (List<String>) datastore.get(k).getProperty("followers");
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		post.setProperty("receivers",receivers);
		
		Transaction txn = datastore.beginTransaction();
		datastore.put(post);
		txn.commit();
		return post;
	}
	
	@ApiMethod(name = "likePost", httpMethod = HttpMethod.POST)
	public Entity likePost(User user, PostMessage pm) throws UnauthorizedException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		if (user == null) { // valider authentification
			throw new UnauthorizedException("Invalid credentials");
		}

		Entity post = null;
		Key k = KeyFactory.createKey("Post", pm.getId());

		HashSet<String> likes = new HashSet<String>();
		int likesC;
		try {
			post = datastore.get(k);
			
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		likes = (HashSet<String>) post.getProperty("likes");
		// TODO ne pas reliker
		likes.add(user.getEmail());
		likesC = (int) post.getProperty("likesC") +1;
		
		post.setProperty("likesC", likesC);
		post.setProperty("likes", likes);
		
		Transaction txn = datastore.beginTransaction();
		datastore.put(post);
		txn.commit();
		return post;
	}
	
	@ApiMethod(name = "user", httpMethod = HttpMethod.POST)
	public Entity user(User user, TinyUser tinyU) throws UnauthorizedException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		if (user == null) { // valider authentification
			throw new UnauthorizedException("Invalid credentials");
		}
		Entity tinyUser = null;
		Query q = new Query("User").
			    setFilter(new FilterPredicate("__key__", FilterOperator.EQUAL, KeyFactory.createKey("User", user.getEmail())));
		
		PreparedQuery pq = datastore.prepare(q);
		tinyUser = pq.asSingleEntity();
		if(tinyUser==null) {
			Entity xx = new Entity("User", tinyU.getEmail());
			tinyUser.setProperty("name", tinyU.getName());
			tinyUser.setProperty("url", tinyU.getUrl());
			List<String> followers = new ArrayList<String>();
			List<String> followings = new ArrayList<String>();
			tinyUser.setProperty("followers", followers);
			tinyUser.setProperty("followings", followings);
			Transaction txn = datastore.beginTransaction();
			datastore.put(tinyUser);
			txn.commit();
		}		
		return tinyUser;
	}
}
