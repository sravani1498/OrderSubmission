package com.order.model;

public class Order {

    private String model;
    private String make;
    private String year;
    private String fuelType;

    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
    }

    private String dealerId;

    public Order() {
        super();
    }

    public Order(String make, String model, String year, String fuelType, String customerId, String customerName, String city, String orderId, String orderStatus, String dealerId) {
        this.model = model;
        this.make = make;
        this.year = year;
        this.fuelType = fuelType;
        this.customerId = customerId;
        this.customerName = customerName;
        this.city = city;
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.dealerId = dealerId;
    }

    private String customerId;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    private String customerName;
    private String city;
    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    private String orderStatus;

}
