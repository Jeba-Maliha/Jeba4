package com.dsinnovators.keyword.driven.helper;

import java.util.Enumeration;
import java.util.Properties;

public class PrefixedProperties extends Properties {
    public PrefixedProperties(Properties props, String prefix){
        prefix = prefix + ".";
        if(props == null){
            return;
        }

        Enumeration<String> en = (Enumeration<String>) props.propertyNames();
        while(en.hasMoreElements()){
            String propName = en.nextElement();
            String propValue = props.getProperty(propName);

            if(propName.startsWith(prefix)){
                String key = propName.substring(prefix.length());
                setProperty(key, propValue);
            }
        }
    }
}