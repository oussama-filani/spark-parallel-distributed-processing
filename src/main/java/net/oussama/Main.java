package net.oussama;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

public class Main {
    public static void main(String[] args) {
        // Configure Spark
        SparkConf conf = new SparkConf().setAppName("Sales Analysis").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        try {
            // Load data
            JavaRDD<String> data = sc.textFile("ventes.txt");

            // Extract header and filter it out
            String header = data.first();
            JavaRDD<String> rddLines = data.filter(line -> !line.equals(header));

            // Cache the filtered data to avoid recomputation
            rddLines.cache();

            // ===== PART 1: Total sales by city =====

            // Map each line to (city, price) pairs
            JavaPairRDD<String, Double> cityPricePairs = rddLines.mapToPair(line -> {
                String[] parts = line.split(",");
                String city = parts[1]; // City is in the second column
                Double price = Double.parseDouble(parts[3]); // Price is in the fourth column
                return new Tuple2<>(city, price);
            });

            // Sum the prices by city
            JavaPairRDD<String, Double> totalsByCity = cityPricePairs.reduceByKey(Double::sum);

            // Print the results for Part 1
            System.out.println("\n===>>> PART 1: Total sales by city <<<===");
            totalsByCity.foreach(result -> System.out.println(result._1 + ": " + result._2));

            // ===== PART 2: Total sales by city and year =====

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

            // Print the results for Part 2
            System.out.println("\n===>>> PART 2: Total sales by city and year <<<===");
            sortedResults.foreach(result -> System.out.println(result._1 + ": " + result._2));

            // Optional: Calculate and display some statistics
            long totalRecords = rddLines.count();
            System.out.println("\n===>>> Statistics <<<===");
            System.out.println("Total number of sales records: " + totalRecords);
            System.out.println("Number of cities: " + totalsByCity.count());
            System.out.println("Number of city-year combinations: " + totalsByCityYear.count());

        } catch (Exception e) {
            System.err.println("Error in Spark execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always close the context
            sc.close();
        }
    }
}