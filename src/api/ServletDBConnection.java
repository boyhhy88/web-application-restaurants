package api;

import db.DBConnection;
import db.MongoDBConnection;
import db.MySQLDBConnection;

/**
 * Choose which database to use
 */
public class ServletDBConnection {
	public static DBConnection getDBConnection( ) {
		return new MySQLDBConnection();
		// return new MongoDBConnection();
	}
}
