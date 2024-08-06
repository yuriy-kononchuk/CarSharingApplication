package com.example.project.config;

import org.testcontainers.containers.MySQLContainer;

public class CustomMySqlConatainer extends MySQLContainer<CustomMySqlConatainer> {
    private static final String DB_IMAGE = "mysql:8.0.36";

    private static CustomMySqlConatainer mySqlConatainer;

    public CustomMySqlConatainer() {
        super(DB_IMAGE);
    }

    public static synchronized CustomMySqlConatainer getInstance() {
        if (mySqlConatainer == null) {
            mySqlConatainer = new CustomMySqlConatainer();
        }
        return mySqlConatainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL",mySqlConatainer.getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME",mySqlConatainer.getUsername());
        System.setProperty("TEST_DB_PASSWORD",mySqlConatainer.getPassword());
    }

    @Override
    public void stop() {
    }
}
