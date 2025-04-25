package net.oussama;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

public class App1 {
    public static void main(String[] args) {
        // Configure Spark
        SparkConf conf = new SparkConf().setAppName("Sales by City").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Load data
        JavaRDD<String> data = sc.textFile("ventes.txt");

        // Extract header and filter it out
        String header = data.first();
        JavaRDD<String> rddLines = data.filter(line -> !line.equals(header));

        // Map each line to (city, price) pairs
        JavaPairRDD<String, Double> cityPricePairs = rddLines.mapToPair(line -> {
            String[] parts = line.split(",");
            String city = parts[1]; // City is in the second column
            Double price = Double.parseDouble(parts[3]); // Price is in the fourth column
            return new Tuple2<>(city, price);
        });

        // Sum the prices by city
        JavaPairRDD<String, Double> totalsByCity = cityPricePairs.reduceByKey(Double::sum);

        // Print the results
        System.out.println("===>>> Exercise 1: Total sales by city <<<===");
        totalsByCity.foreach(result -> System.out.println(result._1 + ": " + result._2));

        // Close the context
        sc.close();
    }
}