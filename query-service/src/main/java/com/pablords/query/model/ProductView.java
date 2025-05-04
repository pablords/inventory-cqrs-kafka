package com.pablords.query.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products_read_view")
public class ProductView {

    @Id
    private String id;
    private String name;
    private int quantity;
    private String topic;
    private String log;

    public ProductView() { }

    public ProductView(String id, String name, int quantity, String topic) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.topic = topic;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getLog() {
        return log;
    }
    public void setLog(String log) {
        this.log = log;
    }
    
}