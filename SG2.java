/**
 * Programming Language: Java
 * IDE: Apache NetBeans IDE 24
 *
 * SG1 small group 14: Abby Sweeney, Curtis Been, Enes Ozturk, Matthew Kellen
 * Keith Miller
 * 2025SP-CMP_SCI-4500-001
 * April 1, 2025
 *
 * Description:
 * This program processes a CSV file that contains abundance counts of various species
 * across different dates. It verifies file validity, validates data, and generates three
 * output files:
 * - Species.txt: Contains column headers (species names) from the CSV file.
 * - DatedData.txt: Contains all valid dates from the CSV file.
 * - PresentAbsent.txt: Contains binary indicators (0s and 1s) representing presence or absence.
 * The program also prints summary reports of maximum abundance and presence/absence patterns.
 *
 * Compilation and Execution Instructions:
 * 1. Open the project in Apache NetBeans IDE 24.
 * 2. Place the input CSV file (e.g., F.CSV) in the root directory of the project.
 * 3. Click 'Run' or use terminal:
 *    javac SG2.java
 *    java SG2
 *
 * Data Structures:
 * - List<String>: Used for species names and dates.
 * - List<List<Double>>: Stores abundance values.
 * - List<List<Integer>>: Stores presence/absence values.
 * - HashMap<String, List<String>>: Tracks presence/absence patterns.
 *
 * External Files Used:
 * - Input: F.CSV, invalid_date.CSV, invalid_number.CSV
 * - Output: Species.txt, DatedData.txt, PresentAbsent.txt
 *
 * External Resources Used:
 * - StackOverflow for regex validation
 * - https://www.geeksforgeeks.org/validate-date-string-java/
 */

package com.mycompany.sg2;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class SG2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("This program processes a CSV file and generates three output files:");
        System.out.println("- Species.txt: Contains column headers (names) from the CSV file.");
        System.out.println("- DatedData.txt: Contains dates from the CSV file.");
        System.out.println("- PresentAbsent.txt: Contains 0s and 1s representing the presence of values.");
        System.out.println("------------------------------------------------------------------------\n");

        String filename;
        File file;

        while (true) {
            System.out.print("Enter the CSV file name: ");
            filename = scanner.nextLine().trim();
            if (!filename.toLowerCase().endsWith(".csv")) {
                System.out.println("Error: File must end with .CSV\n");
                continue;
            }
            
            file = new File(filename);
            if (!file.exists()) {
                System.out.println("Error: File not found in current directory.\n");
            } else {
                break;
            }
        }

        List<String> species = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        List<List<Double>> abundances = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null || !header.startsWith(",")) {
                System.out.println("Invalid header line. Expected to start with comma.");
                return;
            }
            
            String[] headerParts = header.split(",");
            for (int i = 1; i < headerParts.length; i++) {
                species.add(headerParts[i]);
            }
            
            int N = species.size();

            String line;
            int lineNum = 2;
            Pattern datePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != N + 1) {
                    System.out.println("Error on line " + lineNum + ": Incorrect number of values.\n" + line);
                    System.out.print("Press ENTER to exit.");
                    scanner.nextLine();
                    return;
                }
                
                String dateStr = parts[0];
                Matcher matcher = datePattern.matcher(dateStr);
                if (!matcher.matches()) {
                    System.out.println("Error on line " + lineNum + ": Invalid date format: " + dateStr);
                    System.out.print("Press ENTER to exit.");
                    scanner.nextLine();
                    return;
                }

                int month = Integer.parseInt(matcher.group(1));
                int day = Integer.parseInt(matcher.group(2));
                int year = Integer.parseInt(matcher.group(3));
                if (month < 1 || month > 12 || day < 1 || day > 31) {
                    System.out.println("Error on line " + lineNum + ": Illegal date: " + dateStr);
                    System.out.print("Press ENTER to exit.");
                    scanner.nextLine();
                    return;
                }
                
                dates.add(dateStr);

                List<Double> values = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    try {
                        double val = Double.parseDouble(parts[i]);
                        if (val < 0) {
                            System.out.println("Error on line " + lineNum + ": Negative number: " + parts[i]);
                            System.out.print("Press ENTER to exit.");
                            scanner.nextLine();
                            return;
                        }
                        values.add(val);
                    } catch (NumberFormatException e) {
                        System.out.println("Error on line " + lineNum + ": Invalid number: " + parts[i]);
                        System.out.print("Press ENTER to exit.");
                        scanner.nextLine();
                        return;
                    }
                }
                
                abundances.add(values);
                lineNum++;
            }
        } catch (IOException e) {
            System.out.println("IO error while reading the file.");
            return;
        }

        int N = species.size();
        int D = dates.size();
        System.out.println("Read " + N + " species and " + D + " dates.");
        System.out.print("Press ENTER to continue.");
        scanner.nextLine();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("Species.txt"));
            for (String name : species) writer.write(name + "\n");
            writer.close();

            writer = new BufferedWriter(new FileWriter("DatedData.txt"));
            for (String date : dates) writer.write(date + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing output files.");
        }

        List<List<Integer>> PA = new ArrayList<>();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("PresentAbsent.txt"));
            writer.write("," + String.join(",", species) + "\n");
            for (int i = 0; i < D; i++) {
                StringBuilder sb = new StringBuilder(dates.get(i));
                List<Double> row = abundances.get(i);
                List<Integer> binaryRow = new ArrayList<>();
                for (Double value : row) {
                    int val = value > 0 ? 1 : 0;
                    binaryRow.add(val);
                    sb.append(",").append(val);
                }
                
                writer.write(sb.toString() + "\n");
                PA.add(binaryRow);
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing PresentAbsent.txt");
        }

        System.out.println("\nMaximum abundance per date:");
        for (int i = 0; i < D; i++) {
            double max = Collections.max(abundances.get(i));
            List<String> maxSpecies = new ArrayList<>();
            for (int j = 0; j < N; j++) {
                if (abundances.get(i).get(j) == max) {
                    maxSpecies.add(species.get(j));
                }
            }
            System.out.println(dates.get(i) + ": max = " + max + ", species = " + maxSpecies);
        }

        Map<String, List<String>> vectorMap = new HashMap<>();
        for (int i = 0; i < D; i++) {
            StringBuilder vector = new StringBuilder();
            for (int val : PA.get(i)) vector.append(val).append(",");
            String key = vector.toString().replaceAll(",$", "");
            vectorMap.computeIfAbsent(key, k -> new ArrayList<>()).add(dates.get(i));
        }

        System.out.println("\nDates with identical presence/absence vectors:");
        for (Map.Entry<String, List<String>> entry : vectorMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.println(entry.getKey() + " occurs " + entry.getValue().size() + " times: " + entry.getValue());
            }
        }

        System.out.print("\nPress ENTER to finish the program.");
        scanner.nextLine();
    }
}