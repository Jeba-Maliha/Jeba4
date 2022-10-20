package com.dsinnovators.keyword.driven.utils;

import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by nazmul on 6/1/2017.
 */
public class LoggingHelper {

     public LoggingHelper() throws Exception{
        try {
            Properties props = new Properties();
            InputStream configStream = new FileInputStream( "config/log4j.properties");
            props.load(configStream);
            configStream.close();
            PropertyConfigurator.configure(props);
        } catch (IOException e) {
            System.out.println("Error: Cannot laod configuration file ");
            throw e;
        }

    }
}
