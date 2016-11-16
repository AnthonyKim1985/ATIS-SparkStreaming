package com.anthonykim.spark.streaming;

import com.anthonykim.spark.common.PropertiesUtil;
import kafka.serializer.StringDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;

/**
 * Created by hyuk0 on 2016-11-08.
 */
@Data
//@Log4j
@AllArgsConstructor
public class SparkStreaming implements Serializable {
    private String topicNames;

    public void run() {
        Properties sparkProps = PropertiesUtil.getExternalProperties("conf/spark-config.properties");
        if (sparkProps == null)
            sparkProps = PropertiesUtil.getDefaultProperties("META-INF/properties/default-spark-config.properties");

        SparkConf sparkConf = new SparkConf().setAppName("spark-streaming").setMaster(sparkProps.getProperty("spark.uri"));
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkContext, new Duration(Long.parseLong(sparkProps.getProperty("stream.duration.ms"))));

        Properties kafkaProps = PropertiesUtil.getExternalProperties("conf/kafka-streaming.properties");
        if (kafkaProps == null)
            kafkaProps = PropertiesUtil.getDefaultProperties("META-INF/properties/default-kafka-streaming.properties");

        Map<String, String> kafkaParams = new HashMap<>();
        for (Object key : kafkaProps.keySet())
            kafkaParams.put((String) key, (String) kafkaProps.get(key));

        Set<String> topicSet = new HashSet<>(Arrays.asList(topicNames.split("[,]")));
        JavaPairInputDStream<String, String> directKafkaStream = KafkaUtils.createDirectStream(streamingContext,
                String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParams, topicSet);

        directKafkaStream.foreachRDD(new VoidFunction<JavaPairRDD<String, String>>() {
            @Override
            public void call(JavaPairRDD<String, String> stringStringJavaPairRDD) throws Exception {
                System.out.println("--- New RDD with " + stringStringJavaPairRDD.partitions().size() + " partitions and " + stringStringJavaPairRDD.count() + " records");
                stringStringJavaPairRDD.foreach(new VoidFunction<Tuple2<String, String>>() {
                    @Override
                    public void call(Tuple2<String, String> stringStringTuple2) throws Exception {
                        System.out.println(stringStringTuple2._1 + "\t:\t" + stringStringTuple2._2);
                    }
                });
            }
        });
        directKafkaStream.print();
        streamingContext.start();
        streamingContext.awaitTermination();
    }
}
