package db;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Setup MongoDB database. It will contain 2 collections: users and restaurants 
 */
public class MongoDBImport {
	public static void main(String[] args) {
		// a MongoDB client with internal connection pooling
		MongoClient mongoClient = new MongoClient();
		// return a MongoDatabase representing the specified database
		MongoDatabase db = mongoClient.getDatabase(DBUtil.DB_NAME);
		// drop all collections
		for (String collectionName : db.listCollectionNames()) {
			db.getCollection(collectionName).drop();
		}
		// inserts the provided document (row) into collection (table)
		// If the document contains an _id field, the _id value must be unique within the collection to avoid duplicate key error.
		db.getCollection("users").insertOne(new Document()
				.append("_id", "0000")
				.append("user_id", "0000")
				.append("password", "bfae072883544f0aa1d3b0525356b054")
				.append("first_name", "Hongyu")
				.append("last_name", "Hu"));
		mongoClient.close();
		System.out.println("Mongo DB Import: import is done successfully!");
	}
}
