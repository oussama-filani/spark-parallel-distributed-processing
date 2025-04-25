package net.oussama;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

public class App2 {
    public static void main(String[] args) {
        // Configure Spark
        SparkConf conf = new SparkConf().setAppName("Sales by City and Year").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Load data
        JavaRDD<String> data = sc.textFile("ventes.txt");

        // Extract header and filter it out
        String header = data.first();
        JavaRDD<String> rddLines = data.filter(line -> !line.equals(header));

        // Map each line to (city-year, price) pairs
        JavaPairRDD<String, Double> cityYearPricePairs = rddLines.mapToPair(line -> {
            String[] parts = line.split(",");

            // Extract year from the date field (format: YYYY-MM-DD)
            String year = parts[0].split("-")[0];

            String city = parts[1]; // City is in the second column
            Double price = Double.parseDouble(parts[3]); // Price is in the fourth column

            // Create a composite key with city and year
            String key = city + " - " + year;

            return new Tuple2<>(key, price);
        });

        // Sum the prices by city and year
        JavaPairRDD<String, Double> totalsByCityYear = cityYearPricePairs.reduceByKey(Double::sum);

        // Sort by key for better presentation
        JavaPairRDD<String, Double> sortedResults = totalsByCityYear.sortByKey();

        // Print the results
        System.out.println("===>>> Exercise 2: Total sales by city and year <<<===");
        sortedResults.foreach(result -> System.out.println(result._1 + ": " + result._2));

        // Close the context
        sc.close();
    }
}