package com.rfautomation.apitesting.tests;

import com.rfautomation.apitesting.listener.RestAssuredListener;
import com.rfautomation.apitesting.utils.BaseTest;
import com.rfautomation.apitesting.utils.FileNameConstants;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import net.minidev.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class APITests extends BaseTest {

    private static final Logger logger = LogManager.getLogger(APITests.class);
    @Test
    void test1(){
        logger.info("test1 Test Execution started...");

        Response response =
                RestAssured.given().filter(new RestAssuredListener())
                .contentType(ContentType.JSON).baseUri("https://reqres.in/api")
                .when().get("/users?page=2").then()
                        .assertThat().statusCode(200)
                        .header("Content-Type", "application/json; charset=utf-8")
                        .extract().response();

        logger.info("test1 Test Execution ended...");
    }

    @Test
    public void test2(){
        JSONObject booking = new JSONObject();
        JSONObject bookingDates = new JSONObject();

        booking.put("firstname", "api-testing");
        booking.put("lastname", "api-testing");
        booking.put("totalprice", 1000);
        booking.put("depositpaid", true);
        booking.put("additionalneeds", "breakfast");
        booking.put("bookingdates", bookingDates);

        bookingDates.put("checkin", "2024-03-25");
        bookingDates.put("checkout", "2024-03-30");

        Response response =
                RestAssured.given().filter(new RestAssuredListener())
                .contentType(ContentType.JSON)
                .body(booking.toString())
                .baseUri("https://restful-booker.herokuapp.com/booking")
                .when().post()
                .then()
                .assertThat().statusCode(200)
                .body("booking.firstname", Matchers.equalTo("api-testing"))
                .body("booking.totalprice", Matchers.equalTo(1000))
                .body("booking.bookingdates.checkin", Matchers.equalTo("2024-03-25"))
                        .extract().response();

        int bookingId = response.path("bookingid");

        RestAssured.given().filter(new RestAssuredListener())
                .contentType(ContentType.JSON)
                //.pathParam("bookingID", bookingId)
                .baseUri("https://restful-booker.herokuapp.com/booking")
                .when()
                .get("/{bookingID}", bookingId)
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void test3(){
        try {
            String jsonSchema = FileUtils.readFileToString(new File(FileNameConstants.JSON_SCHEMA), "UTF-8");
            JSONObject booking = new JSONObject();
            JSONObject bookingDates = new JSONObject();

            booking.put("firstname", "api-testing");
            booking.put("lastname", "api-testing");
            booking.put("totalprice", 1000);
            booking.put("depositpaid", true);
            booking.put("additionalneeds", "breakfast");
            booking.put("bookingdates", bookingDates);

            bookingDates.put("checkin", "2024-03-25");
            bookingDates.put("checkout", "2024-03-30");

            Response response =
                    RestAssured.given().filter(new RestAssuredListener())
                            .contentType(ContentType.JSON)
                            .body(booking.toString())
                            .baseUri("https://restful-booker.herokuapp.com/booking")
                            .when().post()
                            .then()
                            .assertThat().statusCode(200)
                            .body("booking.firstname", Matchers.equalTo("api-testing"))
                            .body("booking.totalprice", Matchers.equalTo(1000))
                            .body("booking.bookingdates.checkin", Matchers.equalTo("2024-03-25"))
                            .body(JsonSchemaValidator.matchesJsonSchema(jsonSchema))
                            .extract().response();

            int bookingId = response.path("bookingid");

            RestAssured.given().filter(new RestAssuredListener())
                    .contentType(ContentType.JSON)
                    //.pathParam("bookingID", bookingId)
                    .baseUri("https://restful-booker.herokuapp.com/booking")
                    .when()
                    .get("/{bookingID}", bookingId)
                    .then()
                    .assertThat().statusCode(200);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
