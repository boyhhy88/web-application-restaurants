package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class RestaurantTest {
	Restaurant restaurant;
	
	@Test
	public void testJsonArrayToString() {
		JSONArray jsonArray = new JSONArray();
		jsonArray.put("Chinese");
		jsonArray.put("Japanese");
		jsonArray.put("Italian");
		assertEquals("Chinese,Japanese,Italian", Restaurant.jsonArrayToString(jsonArray));
	}
	
	/**
	 * Test cases include empty case, single input case, etc.
	 */
	@Test
	public void testJsonArrayToStringCornerCases() {
		JSONArray jsonArray = new JSONArray();
		assertEquals("", Restaurant.jsonArrayToString(jsonArray));
		jsonArray.put("Chinese");
		assertEquals("Chinese", Restaurant.jsonArrayToString(jsonArray));
		jsonArray.put("Japanese");
		jsonArray.put("");
		String str = Restaurant.jsonArrayToString(jsonArray);
		assertEquals("Chinese,Japanese,", str);
	}
	
	// The TestName Rule makes the current test name available inside test methods
	@Rule public TestName name = new TestName();
	
	/**
	 * Test fixture.
	 */
	@Before
	public void Setup() {
		restaurant = new Restaurant("E8RJkjfdcwgtyoPMjQ_Olg", 
				"Four Barrel Coffee",
				"coffee,Coffee & Tea",
				"San Francisco", 
				"CA",
				"",
				4,
				"$",
				37.76, 
				-122.42,
				"http://s3-media2.fl.yelpcdn.com/bphoto/MmgtASP3l_t4tPCL1iAsCg/o.jpg",
				"https://www.yelp.com/biz/four-barrel-coffee-san-francisco",
				1604.23);
	}
	
	@Test
	public void testRestaurantConstructor() {
		String jsonString = "{\r\n" + 
				"  \"total\": 8228,\r\n" + 
				"  \"businesses\": [\r\n" + 
				"    {\r\n" +
				"      \"rating\": 4,\r\n" + 
				"      \"price\": \"$\",\r\n" + 
				"      \"phone\": \"+14152520800\",\r\n" + 
				"      \"id\": \"E8RJkjfdcwgtyoPMjQ_Olg\",\r\n" + 
				"      \"alias\": \"four-barrel-coffee-san-francisco\",\r\n" + 
				"      \"is_closed\": false,\r\n" + 
				"      \"categories\": [\r\n" + 
				"        {\r\n" + 
				"          \"alias\": \"coffee\",\r\n" + 
				"          \"title\": \"Coffee & Tea\"\r\n" + 
				"        }\r\n" + 
				"      ],\r\n" + 
				"      \"review_count\": 1738,\r\n" + 
				"      \"name\": \"Four Barrel Coffee\",\r\n" + 
				"      \"url\": \"https://www.yelp.com/biz/four-barrel-coffee-san-francisco\",\r\n" + 
				"      \"coordinates\": {\r\n" + 
				"        \"latitude\": 37.76,\r\n" + 
				"        \"longitude\": -122.42\r\n" + 
				"      },\r\n" + 
				"      \"image_url\": \"http://s3-media2.fl.yelpcdn.com/bphoto/MmgtASP3l_t4tPCL1iAsCg/o.jpg\",\r\n" + 
				"      \"location\": {\r\n" + 
				"      \"display_address\": [\"\"],\r\n" + 
				"        \"city\": \"San Francisco\",\r\n" + 
				"        \"country\": \"US\",\r\n" + 
				"        \"address2\": \"\",\r\n" + 
				"        \"address3\": \"\",\r\n" + 
				"        \"state\": \"CA\",\r\n" + 
				"        \"address1\": \"375 Valencia St\",\r\n" + 
				"        \"zip_code\": \"94103\"\r\n" + 
				"      },\r\n" + 
				"      \"distance\": 1604.23,\r\n" + 
				"      \"transactions\": [\"pickup\", \"delivery\"]\r\n" + 
				"    },\r\n" +  
				"  ],\r\n" + 
				"  \"region\": {\r\n" + 
				"    \"center\": {\r\n" + 
				"      \"latitude\": 37.76,\r\n" + 
				"      \"longitude\": -122.42\r\n" + 
				"    }\r\n" + 
				"  }\r\n" + 
				"}";
		Restaurant new_restaurant = null;
		try {
			JSONArray array = (JSONArray) new JSONObject(jsonString).get("businesses");
			JSONObject obj = array.getJSONObject(0);
			new_restaurant = new Restaurant(obj);
		} catch (JSONException e) {
			e.printStackTrace();
			// Fails a test with no message. 
			fail();
		}
		assertEquals(restaurant.getBusinessId(), new_restaurant.getBusinessId());
		assertEquals(restaurant.getName(), new_restaurant.getName());
		assertEquals(restaurant.getCategories(), new_restaurant.getCategories());
		assertEquals(restaurant.getCity(), new_restaurant.getCity());
		assertEquals(restaurant.getState(), new_restaurant.getState());
		assertEquals(restaurant.getFullAddress(), new_restaurant.getFullAddress());
		//Asserts that two doubles are equal to within a positive delta.
		assertEquals(restaurant.getStars(), new_restaurant.getStars(), 0);
		assertEquals(restaurant.getPrice(), new_restaurant.getPrice());
		assertEquals(restaurant.getLatitude(), new_restaurant.getLatitude(), 0);
		assertEquals(restaurant.getLongitude(), new_restaurant.getLongitude(), 0);
		assertEquals(restaurant.getImageUrl(), new_restaurant.getImageUrl());
		assertEquals(restaurant.getUrl(), new_restaurant.getUrl());
		assertEquals(restaurant.getDistance(), new_restaurant.getDistance(), 0);
	}
	
	/** 
	 * Test fixture.
	 */
	@After
	public void tearDown() {
		System.out.println("Test finished: " + name.getMethodName());
	}
}
