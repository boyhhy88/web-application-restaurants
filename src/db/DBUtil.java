package db;

public class DBUtil {
	public static final String DB_NAME = "restaurants";
	public static final int MAX_RECOMMENDED_RESTAURANTS = 20;
	
	// parameters for MySQL database
	private static final String HOSTNAME = "localhost";
	private static final String PORT_NUM = "3306";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";
	public static final String URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME + "?user=" + USERNAME
			+ "&password=" + PASSWORD + "&useSSL=false";
	
}
