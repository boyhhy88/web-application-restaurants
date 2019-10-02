package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Create MySQL DB tables: users, history, restaurants
 * Run as Java application to initialize the database.
 */
public class MySQLDBImport {
	public static void main(String[] args) {
		try {
			// A connection (session) with a specific database. SQL statements are executed and results 
			// are returned within the context of a connection. 
			Connection conn = null;
			System.out.println("Connecting to database:\n" + DBUtil.URL);
			// Class.forName("com.mysql.jdbc.Driver").newInstance();
			// Attempts to establish a connection to the given database URL.
			conn = DriverManager.getConnection(DBUtil.URL);
			if (conn == null) {
				System.out.println("Cannot connect to database.");
				return;
			}
			
			// Creates a Statement object for sending SQL statements to the database. 
			Statement stmt = conn.createStatement();
			
			// Drop tables in case they exist
			String sql = "DROP TABLE IF EXISTS history";
			// Executes the given SQL statement
			stmt.executeUpdate(sql);
			sql = "DROP TABLE IF EXISTS restaurants";
			stmt.executeUpdate(sql);
			sql = "DROP TABLE IF EXISTS users";
			stmt.executeUpdate(sql);
			
			// Create tables
			sql = "CREATE TABLE restaurants" 
				+ "(business_id VARCHAR(255) NOT NULL,"
				+ "name VARCHAR(255),"
				+ "categories VARCHAR(255),"
				+ "city VARCHAR(255),"
				+ "state VARCHAR(255),"
				+ "stars FLOAT,"
				+ "price VARCHAR(255),"
				+ "full_address VARCHAR(255),"
				+ "latitude FLOAT,"
				+ "longitude FLOAT,"
				+ "image_url VARCHAR(255),"
				+ "url VARCHAR(255),"
				+ "PRIMARY KEY (business_id))";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE users"
				+ "(user_id VARCHAR(255) NOT NULL,"
				+ "password VARCHAR(255) NOT NULL,"
				+ "first_name VARCHAR(255),"
				+ "last_name VARCHAR(255),"
				+ "PRIMARY KEY (user_id))";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE history"
				+ "(visit_history_id bigint(20) unsigned NOT NULL AUTO_INCREMENT,"
				+ "user_id VARCHAR(255) NOT NULL,"
				+ "business_id VARCHAR(255) NOT NULL,"
				+ "last_visited_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
				+ "PRIMARY KEY (visit_history_id),"
				+ "FOREIGN KEY (business_id) REFERENCES restaurants(business_id),"
				+ "FOREIGN KEY (user_id) REFERENCES users(user_id))";
			stmt.executeUpdate(sql);
			
			//*******************************************************************************
			// Insert data
			// Create a fake user (username: 0000, password: 1234)
			sql = "INSERT INTO users " + "VALUES (\"0000\", \"bfae072883544f0aa1d3b0525356b054\", \"Hongyu\", \"Hu\")";
			stmt.executeUpdate(sql);
			
			System.out.println("MySQL DB Import: import is done successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
