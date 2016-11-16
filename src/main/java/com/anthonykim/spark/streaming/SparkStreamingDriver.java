package com.anthonykim.spark.streaming;

import lombok.extern.log4j.Log4j;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by hyuk0 on 2016-11-08.
 */
//@Log4j
public class SparkStreamingDriver {
    public static void main(String[] args) {
        //PropertyConfigurator.configure(SparkStreamingDriver.class.getClassLoader().getResource("META-INF/config/log4j.properties"));
        if (args.length != 1) {
            System.out.println("USAGE: spark-streaming.jar <topic1>,<topic2>...");
            System.exit(1);
        }

        SparkStreaming sparkStreaming = new SparkStreaming(args[0]);
        sparkStreaming.run();
    }
}