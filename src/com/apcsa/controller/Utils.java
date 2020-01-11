package com.apcsa.controller;


import java.math.BigDecimal;

import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.Student;
import com.apcsa.data.QueryUtils;


public class Utils {

    /**
     * Returns an MD5 hash of the user's plaintext password.
     *
     * @param plaintext the password
     * @return an MD5 hash of the password
     */

    public static String getHash(String plaintext) {
        StringBuilder pwd = new StringBuilder();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(plaintext.getBytes());
            byte[] digest = md.digest(plaintext.getBytes());

            for (int i = 0; i < digest.length; i++) {
                pwd.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return pwd.toString();
    }
    
    /**
     * Safely reads an integer from the user.
     * 
     * @param in the Scanner
     * @param invalid an invalid (but type-safe) default
     * @return the value entered by the user or the invalid default
     */
    
    public static int getInt(Scanner in, int invalid) {
        try {
            return in.nextInt();                // try to read and return user-provided value
        } catch (InputMismatchException e) {            
            return invalid;                     // return default in the even of an type mismatch
        } finally {
            in.nextLine();                      // always consume the dangling newline character
        }
    }
    
    /**
     * Confirms a user's intent to perform an action.
     * 
     * @param in the Scanner
     * @param message the confirmation prompt
     * @return true if the user confirms; false otherwise
     */

    public static boolean confirm(Scanner in, String message) {
        String response = "";
        
        // prompt user for explicit response of yes or no
        
        while (!response.equals("y") && !response.equals("n")) {
            System.out.print(message);
            response = in.next().toLowerCase();
        }
        
        return response.equals("y");
    }
    
    public static int generateAssignmentId() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        try (Connection conn = PowerSchool.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT assignment_id FROM assignments");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("assignment_id"));
                }
            }
        } catch (SQLException e) {
            PowerSchool.shutdown(true);
        }

        if (ids.size() == 0) {
            return 1;
        }else if (ids.size() != 0) {
            return ids.get(ids.size() - 1) + 1;
        }

        return -1;
    }
    

    /**
     * Sorts the list of students by rank, using the index to update the underlying class rank.
     * 
     * @param students the list of students
     * @return the updated list of students
     */

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ArrayList<Student> updateRanks(ArrayList<Student> students) {
        Collections.sort(students, new Comparator() {

            // compares each student based on gpa to aid sorting
            
            @Override
            public int compare(Object student1, Object student2) {
                if (((Student) student1).getGpa() > ((Student) student2).getGpa()) {
                    return -1;
                } else if (((Student) student1).getGpa() == ((Student) student2).getGpa()) {
                    return 0;
                } else {
                    return 1;
                }
            }
            
        });
        
        // applies a class rank (provided the student has a measurable gpa)
        
        int rank = 1;
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            
            student.setClassRank(student.getGpa() != -1 ? rank++ : 0);
        }
                
        return students;
    }
    
    /**
     * Computes a grade based on marking period grades and exam grades.
     * 
     * @param grades a list of grades
     * @return the final grade
     */

    public static Double getGrade(Double[] grades) {
        int mps = 0;
        double mpSum = 0;
        double mpAvg = -1;
        double mpWeight = -1;

        int exams = 0;
        double examSum = 0;
        double examAvg = -1;
        double examWeight = -1;
        
        // compute sum of marking period and/or exam grades
        
        for (int i = 0; i < grades.length; i++) {
            if (grades[i] != null) {
                if (i < 2 || (i > 2 && i < 5)) {        // marking period grade
                    mps++;
                    mpSum = mpSum + grades[i];
                } else {                                // midterm or final exam grade
                    exams++;
                    examSum = examSum + grades[i];
                }
            }
        }
        
        // compute weights and averages based on entered grades
        
        if (mps > 0 && exams > 0) {
            mpAvg = mpSum / mps;
            examAvg = examSum / exams;
             
            mpWeight = 0.8;
            examWeight = 0.2;
        } else if (mps > 0) {
            mpAvg = mpSum / mps;
            
            mpWeight = 1.0;
            examWeight = 0.0;
        } else if (exams > 0) {
            examAvg = examSum / exams;
            
            mpWeight = 0.0;
            examWeight = 1.0;
        } else {
            return null;
        }
                                
        return round(mpAvg * mpWeight + examAvg * examWeight, 2);
    }
    
    
    public static void updateGPA(Student student) {
		//I SWEAR if I have time I WILL make a course class
		ArrayList<String> course_nos = new ArrayList<String>();
		ArrayList<String> course_ids = new ArrayList<String>();
		ArrayList<Double> credit_hours = new ArrayList<Double>();
        ArrayList<Double> courseGrades = student.getCourseGrades();

        if (courseGrades.contains(0)) {
            return;
        }
		
		int count = 1;
		
		try (Connection conn = PowerSchool.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_COURSES);
			stmt.setInt(1, student.getStudentId());
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					course_nos.add(rs.getString("course_no"));
					course_ids.add(rs.getString("course_id"));
					credit_hours.add(rs.getDouble("credit_hours"));
				}
			} catch (SQLException e) {
				System.out.println(e);
			}
		} catch (SQLException e) {
			System.out.println(e);
		}

		ArrayList<Double> fourScale = new ArrayList<Double>();
        	for(int i = 0; i < courseGrades.size(); i++) {
        		if((Double) courseGrades.get(i) == -1.0) {

        		} else if ((Double) courseGrades.get(i) >= 93 && (Double) courseGrades.get(i) <= 100) {
        			fourScale.add(4.0);
        		} else if ((Double) courseGrades.get(i) >= 90 && (Double) courseGrades.get(i) <= 92) {
        			fourScale.add(3.7);
        		} else if ((Double) courseGrades.get(i) >= 87 && (Double) courseGrades.get(i) <= 89) {
        			fourScale.add(3.3);
        		} else if ((Double) courseGrades.get(i) >= 83 && (Double) courseGrades.get(i) <= 86) {
        			fourScale.add(3.0);
        		} else if ((Double) courseGrades.get(i) >= 80 && (Double) courseGrades.get(i) <= 82) {
        			fourScale.add(2.7);
        		} else if ((Double) courseGrades.get(i) >= 77 && (Double) courseGrades.get(i) <= 79) {
        			fourScale.add(2.3);
        		} else if ((Double) courseGrades.get(i) >= 73 && (Double) courseGrades.get(i) <= 76) {
        			fourScale.add(2.0);
        		} else if ((Double) courseGrades.get(i) >= 70 && (Double) courseGrades.get(i) <= 72) {
        			fourScale.add(1.7);
        		} else if ((Double) courseGrades.get(i) >= 67 && (Double) courseGrades.get(i) <= 69) {
        			fourScale.add(1.3);
        		} else if ((Double) courseGrades.get(i) >= 65 && (Double) courseGrades.get(i) <= 66) {
        			fourScale.add(1.0);
        		} else if ((Double) courseGrades.get(i) > 65) {
        			fourScale.add(0.0);
        		}
        	}
        	int totalGradePoints = 0;
        	int hours = 0;
        	for(int i = 0; i < fourScale.size(); i++) {
        		totalGradePoints += fourScale.get(i)*credit_hours.get(i);
        		hours += credit_hours.get(i);
        	}
        	double gpa = (double) (totalGradePoints)/ (double) hours;
            double roundedGpa = Math.round(gpa * 100.0) / 100.0;
            
            PowerSchool.updateGPA(roundedGpa, student.getStudentId());
            

            updateRanks(PowerSchool.getStudents());
	}

    
    /**
     * Rounds a number to a set number of decimal places.
     * 
     * @param value the value to round
     * @param places the number of decimal places
     * @return the rounded value
     */
        
    private static double round(double value, int places) {
        return new BigDecimal(Double.toString(value))
            .setScale(places, RoundingMode.HALF_UP)
            .doubleValue();
    }
}


