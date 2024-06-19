package com.rfautomation.apitesting.tests;

import com.jayway.jsonpath.JsonPath;
import com.rfautomation.apitesting.listener.RestAssuredListener;
import com.rfautomation.apitesting.utils.FileNameConstants;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class PutAPIRequest {

    @Test
    public void putAPIRequest() {
        try {
            String tokenAPIRequestBody = FileUtils.readFileToString(new File(FileNameConstants.TOKEN_REQUEST_BODY), "UTF-8");
            String putAPIRequestBody = FileUtils.readFileToString(new File(FileNameConstants.PUT_API_REQUEST_BODY), "UTF-8");

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
                            .extract().response();

            JSONArray jsonArray = JsonPath.read(response.body().asString(), "$.booking..firstname");
            String firstname = (String) jsonArray.get(0);

            Assert.assertEquals(firstname, "api-testing");

            int bookingId = JsonPath.read(response.body().asString(), "$.bookingid");

            RestAssured.given().filter(new RestAssuredListener())
                    .contentType(ContentType.JSON)
                    //.pathParam("bookingID", bookingId)
                    .baseUri("https://restful-booker.herokuapp.com/booking")
                    .when()
                    .get("/{bookingID}", bookingId)
                    .then()
                    .assertThat().statusCode(200);

            // token generation
            Response tokenAPIResponse = RestAssured
                    .given()
                    .contentType(ContentType.JSON)
                    .body(tokenAPIRequestBody)
                    .baseUri("https://restful-booker.herokuapp.com/auth")
                    .when()
                    .post()
                    .then().assertThat()
                    .statusCode(200)
                    .extract().response();

            String token = JsonPath.read(tokenAPIResponse.body().asString(), "$.token");

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(putAPIRequestBody)
                    .header("Cookie", "token="+token)
                    .baseUri("https://restful-booker.herokuapp.com/booking")
                    .when().put("/{bookingId}", bookingId)
                    .then().assertThat()
                    .statusCode(200)
                    .body("firstname", Matchers.equalTo("James"))
            ;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
