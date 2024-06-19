package com.rfautomation.apitesting.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.rfautomation.apitesting.listener.RestAssuredListener;
import com.rfautomation.apitesting.pojos.Booking;
import com.rfautomation.apitesting.pojos.BookingDates;
import com.rfautomation.apitesting.utils.FileNameConstants;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.minidev.json.JSONArray;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

public class DDTestingWithJsonFile {

    @Test (dataProvider = "getTestData")
    public void dDTestingWithJsonFile(LinkedHashMap<String, String> testData) throws JsonProcessingException {

        BookingDates bookingDates = new BookingDates("", "");
        Booking booking = new Booking(testData.get("firstname"), testData.get("lastname"), "breakfast",1100, true, bookingDates);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(booking);

        Response response =
                RestAssured
                        .given().filter(new RestAssuredListener())
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .baseUri("https://restful-booker.herokuapp.com/booking")
                        .when()
                        .post()
                        .then()
                        .assertThat()
                        .statusCode(200)
                        .extract()
                        .response();
    }

    @DataProvider(name="getTestData")
    public Object[] getTestDataUsingJson(){

        Object[] obj = null;

        try {
            String jsonTestData = FileUtils.readFileToString(new File(FileNameConstants.JSON_TEST_DATA), "UTF-8");

            JSONArray jsonArray = JsonPath.read(jsonTestData, "$");
            obj = new Object[jsonArray.size()];

            for (int i = 0; i< jsonArray.size(); i++){
                obj[i] = jsonArray.get(i);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return obj;
    }
}
