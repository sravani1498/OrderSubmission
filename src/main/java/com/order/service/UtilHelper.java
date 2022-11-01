package com.order.service;

import com.order.model.Order;
import com.order.model.OrderRequest;

import java.util.*;

public class UtilHelper {


    public ArrayList<Order> convertToOrder(OrderRequest input) {
        String decodedBase64String = base64ToString(input.getFileContent());
        return parseStringToJson(decodedBase64String, input.getDealerId());
    }

    public String base64ToString(String base64){
        Base64.Decoder decoder = Base64.getMimeDecoder();

        byte[] decodedBytes = decoder.decode(base64);
        return new String(decodedBytes);
    }

    public ArrayList<Order> parseStringToJson(String inputString, String dealerid) {
        ArrayList<Order> orderList = new ArrayList<>();
        String[] lines = inputString.split(System.lineSeparator());

        List<String> headersList = Arrays.asList(lines[0].split(","));
        for(int i=1; i < lines.length; i++) {
            List<String> data = Arrays.asList(lines[i].split(","));
            if (data.size() != headersList.size()) {
                return null;
            } else {
                //Random Order Id
                Random rnd = new Random();
                int number = rnd.nextInt(999999);

                // this will convert any number sequence into 6 character.
                String orderId = String.format("%06d", number);

                Order order = new Order(data.get(0), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5), data.get(6), orderId, "Received", dealerid);

                orderList.add(order);

            }
        }
        return orderList;
    }




}
