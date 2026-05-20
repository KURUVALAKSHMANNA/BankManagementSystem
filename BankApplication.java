package com.jdbc.java.Projects;

import java.sql.*;
import java.util.*;

public class BankApplication {
    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/BankAppDatabase";
        String user = "root";
        String password = "sree4154";

        Scanner scan = new Scanner(System.in);

        try (Connection con = DriverManager.getConnection(url, user, password)) {

            int choice;

            do {
                System.out.println("\n---->> Bank Menu <<----");
                System.out.println("1. Create Account");
                System.out.println("2. View Account Details");
                System.out.println("3. Deposit Amount");
                System.out.println("4. Withdraw Amount");
                System.out.println("5. View Transaction History");
                System.out.println("6. Exit");
                System.out.print("Enter choice: ");

                choice = scan.nextInt();
                scan.nextLine();

                switch (choice) {

                    // ================= CREATE ACCOUNT =================
                    case 1:
                        System.out.print("Enter First Name: ");
                        String firstName = scan.nextLine();

                        System.out.print("Enter Last Name: ");
                        String lastName = scan.nextLine();

                        System.out.print("Enter Aadhaar Number: ");
                        long aadhaar = scan.nextLong();
                        scan.nextLine();

                        System.out.print("Enter Mobile Number: ");
                        String phone = scan.nextLine();

                        System.out.print("Enter Email: ");
                        String email = scan.nextLine();

                        System.out.print("Enter Father Name: ");
                        String father = scan.nextLine();

                        System.out.print("Enter Mother Name: ");
                        String mother = scan.nextLine();

                        System.out.print("Enter Address: ");
                        String address = scan.nextLine();

                        System.out.print("Enter Initial Balance: ");
                        double balance = scan.nextDouble();

                        String insertQuery = "INSERT INTO Bank_Account "
                                + "(first_name, last_name, aadhaar, phone_number, email, father_name, mother_name, address, balance) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        try (PreparedStatement pstmt = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

                            pstmt.setString(1, firstName);
                            pstmt.setString(2, lastName);
                            pstmt.setLong(3, aadhaar);
                            pstmt.setString(4, phone);
                            pstmt.setString(5, email);
                            pstmt.setString(6, father);
                            pstmt.setString(7, mother);
                            pstmt.setString(8, address);
                            pstmt.setDouble(9, balance);

                            int rows = pstmt.executeUpdate();

                            if (rows > 0) {
                                ResultSet rs = pstmt.getGeneratedKeys();

                                if (rs.next()) {
                                    long id = rs.getLong(1);

                                    // Generate 10 digit account number
                                    long accountNumber = 1000000000L + id;

                                    // Update table with generated account number
                                    String updateAccNo = "UPDATE Bank_Account SET account_number = ? WHERE id = ?";

                                    try (PreparedStatement pstmt2 = con.prepareStatement(updateAccNo)) {
                                        pstmt2.setLong(1, accountNumber);
                                        pstmt2.setLong(2, id);
                                        pstmt2.executeUpdate();
                                    }

                                    System.out.println("Account Created Successfully!");
                                    System.out.println("Account Number: " + accountNumber);
                                    System.out.println("Holder Name: " + firstName + " " + lastName);
                                    System.out.println("Current Balance: " + balance);
                                }
                            }
                        }
                        break;

                    // ================= VIEW ACCOUNT =================
                    case 2:
                        System.out.print("Enter Aadhaar Number: ");
                        long ad = scan.nextLong();
                        scan.nextLine();

                        System.out.print("Enter Linked Mobile Number: ");
                        String mobile = scan.nextLine();

                        String selectQuery = "SELECT account_number, "
                                + "CONCAT(first_name, ' ', last_name) AS full_name, "
                                + "balance "
                                + "FROM Bank_Account WHERE aadhaar = ? AND phone_number = ?";

                        try (PreparedStatement pstmt = con.prepareStatement(selectQuery)) {

                            pstmt.setLong(1, ad);
                            pstmt.setString(2, mobile);

                            ResultSet rs = pstmt.executeQuery();

                            if (rs.next()) {
                                System.out.println("\nAccount Number: " + rs.getLong("account_number"));
                                System.out.println("Holder Name: " + rs.getString("full_name"));
                                System.out.println("Current Balance: " + rs.getDouble("balance"));
                            } else {
                                System.out.println("Account Not Found!");
                            }
                        }
                        break;

                    // ================= DEPOSIT =================
                    case 3:
                        System.out.print("Enter Account Number: ");
                        long accNo = scan.nextLong();

                        System.out.print("Enter Amount to Deposit: ");
                        double deposit = scan.nextDouble();

                        String depositQuery = "UPDATE Bank_Account SET balance = balance + ? WHERE account_number = ?";

                        try (PreparedStatement pstmt = con.prepareStatement(depositQuery)) {

                            pstmt.setDouble(1, deposit);
                            pstmt.setLong(2, accNo);

                            int updated = pstmt.executeUpdate();

                            if (updated > 0) {

                                // Insert into transaction history
                                String insertHistory = "INSERT INTO Transaction_History (account_number, transaction_type, amount) VALUES (?, 'DEPOSIT', ?)";

                                try (PreparedStatement pstmt2 = con.prepareStatement(insertHistory)) {
                                    pstmt2.setLong(1, accNo);
                                    pstmt2.setDouble(2, deposit);
                                    pstmt2.executeUpdate();
                                }

                                System.out.println("Amount Deposited Successfully!");

                            } else {
                                System.out.println("Account Not Found!");
                            }
                        }
                        break;

                    // ================= WITHDRAW =================
                    case 4:
                    	System.out.print("Enter Account Number: ");
                    	long acc = scan.nextLong();

                    	System.out.print("Enter Amount to Withdraw: ");
                    	double withdraw = scan.nextDouble();

                    	String checkBalance = "SELECT balance FROM Bank_Account WHERE account_number = ?";

                    	try (PreparedStatement pstmt = con.prepareStatement(checkBalance)) {

                    	    pstmt.setLong(1, acc);
                    	    ResultSet rs = pstmt.executeQuery();

                    	    if (rs.next()) {
                    	        double currentBalance = rs.getDouble("balance");

                    	        if (withdraw <= currentBalance) {

                    	            String withdrawQuery = "UPDATE Bank_Account SET balance = balance - ? WHERE account_number = ?";

                    	            try (PreparedStatement pstmt2 = con.prepareStatement(withdrawQuery)) {
                    	                pstmt2.setDouble(1, withdraw);
                    	                pstmt2.setLong(2, acc);

                    	                int updated = pstmt2.executeUpdate();

                    	                if (updated > 0) {

                    	                    // Insert into transaction history
                    	                    String insertHistory = "INSERT INTO Transaction_History (account_number, transaction_type, amount) VALUES (?, 'WITHDRAW', ?)";

                    	                    try (PreparedStatement pstmt3 = con.prepareStatement(insertHistory)) {
                    	                        pstmt3.setLong(1, acc);
                    	                        pstmt3.setDouble(2, withdraw);
                    	                        pstmt3.executeUpdate();
                    	                    }

                    	                    System.out.println("Amount Withdrawn Successfully!");

                    	                } else {
                    	                    System.out.println("Account Not Found!");
                    	                } 
                    	            }

                    	        } else {
                    	            System.out.println("Insufficient Balance!");
                    	        }

                    	    } else {
                    	        System.out.println("Account Not Found!");
                    	    }
                    	}
                    	
                        break;

                    	// ================= VIEW TRANSACTION HISTORY =================
                    case 5:

                        System.out.print("Enter Account Number: ");
                        long accHistory = scan.nextLong();

                        String historyQuery = "SELECT transaction_type, amount, transaction_date "
                                + "FROM Transaction_History WHERE account_number = ? "
                                + "ORDER BY transaction_date DESC";

                        try (PreparedStatement pstmt = con.prepareStatement(historyQuery)) {

                            pstmt.setLong(1, accHistory);
                            ResultSet rs = pstmt.executeQuery();

                            System.out.println("\n---- Transaction History ----");

                            boolean found = false;

                            while (rs.next()) {
                                found = true;
                                System.out.println("Type: " + rs.getString("transaction_type")
                                        + " | Amount: " + rs.getDouble("amount")
                                        + " | Date: " + rs.getTimestamp("transaction_date"));
                            }

                            if (!found) {
                                System.out.println("No Transactions Found!");
                            }
                        }

                        break;
                    case 6:
                    	

                    default:
                        System.out.println("Invalid Choice!");
                }

            } while (choice != 6);

        } catch (Exception e) {
            e.printStackTrace();
        }

        scan.close();
    }
}