package com.apcsa.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.apcsa.controller.Utils;
import com.apcsa.data.*;
import com.apcsa.model.User;
import java.sql.SQLException;

import com.apcsa.model.User;

public class Student extends User {

    private int studentId;
    private int classRank;
    private int gradeLevel;
    private int graduationYear;
    private double gpa;
    private String firstName;
    private String lastName;
    
    public Student(User user, ResultSet rs) throws SQLException {
		super(user.getUserId(), user.getAccountType(), user.getUsername(), user.getPassword(), user.getLastLogin());
		// TODO Auto-generated constructor stub
	
		this.studentId = rs.getInt("student_id") ;
		this.firstName = rs.getString("first_name");
		this.lastName = rs.getString("last_name");
		this.classRank = rs.getInt("class_rank");
		this.gradeLevel = rs.getInt("grade_level");
		this.graduationYear = rs.getInt("graduation");
		this.gpa = rs.getDouble("gpa");
	}
    
    public Student(ResultSet rs) throws SQLException {
		//user id, account type, username, password, last login
		super(rs.getInt("user_id"), rs.getString("account_type"), rs.getString("username"), rs.getString("auth"), rs.getString("last_login"));

		this.studentId = rs.getInt("student_id");
    	this.classRank = rs.getInt("class_rank");
    	this.gradeLevel = rs.getInt("grade_level");
    	this.graduationYear = rs.getInt("graduation");
    	this.gpa = rs.getDouble("gpa");
    	this.firstName = rs.getString("first_name");
		this.lastName = rs.getString("last_name");
	}
    
    public int getStudentId() {
    	return studentId;
    }

    public String getFirstName() {
    	return firstName;
    }
    
    public String getLastName() {
    	return lastName;
    }
    
    public int getClassRank() {
    	return classRank;
    }
    
    public void setClassRank(int classRank) {
		this.classRank = classRank;
	}
    
    public int getGradeLevel() {
    	return gradeLevel;
    }
    
    public int getGraduationYear() {
    	return graduationYear;
    }
    
    public double getGpa() {
    	return gpa;
    }
    
    public void changePassword(Scanner in) {
		System.out.println("\nEnter current password:");
        String currentPassword = in.nextLine();
        currentPassword = Utils.getHash(currentPassword);
    	
    	if (currentPassword.equals(this.password)) {
    		System.out.println("\nEnter a new password:");
    		String password = Utils.getHash((in.nextLine()));
    		this.setPassword(password);
        	try {
        		Connection conn = PowerSchool.getConnection();
        		PowerSchool.updatePassword(conn, this.getUsername(), password);
        	} catch (SQLException e){
        		PowerSchool.shutdown(true);
        	}
    	}else {
    		System.out.println("\nIncorrect current password.");
    	}
		
	}
    
    public ArrayList<Double> getCourseGrades() {
		ArrayList<Double> course_grades = new ArrayList<Double>();
		try (Connection conn = PowerSchool.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_COURSES);
			stmt.setInt(1, studentId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					course_grades.add(rs.getDouble("grade"));
				}
			}
		} catch (SQLException e) {
			PowerSchool.shutdown(true);
		}

		return course_grades;
	}
    
    public void viewCourseGrades() {
		System.out.print("\n");
		try (Connection conn = PowerSchool.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_COURSES);
			stmt.setInt(1, studentId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					System.out.println(rs.getString("title") + " / " + rs.getInt("grade"));
				}
			}
		} catch (SQLException e) {
			PowerSchool.shutdown(true);
		}
	}
    
	public void viewAssignmentGradesByCourse(Scanner in) {
		System.out.print("\n");
		ArrayList<String> course_nos = new ArrayList<String>();
		ArrayList<String> course_ids = new ArrayList<String>();
		
		int count = 1;
		int input = 0;
		int selection = 0;
		String selectionString = "";
		
		try (Connection conn = PowerSchool.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement("SELECT courses.title, grade, courses.course_id, courses.course_no FROM course_grades INNER JOIN courses ON course_grades.course_id = courses.course_id INNER JOIN students ON students.student_id = course_grades.student_id WHERE students.student_id = ?");
			stmt.setInt(1, studentId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					System.out.println("[" + count + "] " + rs.getString("course_no"));
					count++;
					course_nos.add(rs.getString("course_no"));
					course_ids.add(rs.getString("course_id"));
				}
				System.out.print("\n::: ");
			} catch (SQLException e) {
				PowerSchool.shutdown(true);
			}
		} catch (SQLException e) {
			PowerSchool.shutdown(true);
		}

		try {
			input = in.nextInt();
		} catch (InputMismatchException e) {
			System.out.println("\nYour input was invalid. Please try again.");
		} finally {
			in.nextLine();
		}

		System.out.println("\n[1] MP1 Assignment.");
		System.out.println("[2] MP2 Assignment.");
		System.out.println("[3] MP3 Assignment.");
		System.out.println("[4] MP4 Assignment.");
		System.out.println("[5] Midterm Exam.");
		System.out.println("[6] Final Exam.");
		System.out.print("\n::: ");


		try {
			selection = in.nextInt();
		} catch (InputMismatchException e) {
			PowerSchool.shutdown(true);
		} finally {
			in.nextLine();
		}

		switch (selection) {
			case 1:
				selectionString = "mp1";
				break;
			case 2:
				selectionString = "mp2";
				break;
			case 3:
				selectionString = "mp3";
				break;
			case 4:
				selectionString = "mp4";
				break;
			case 5:
				selectionString = "midterm_exam";
				break;
			case 6:
				selectionString = "final_exam";
		}



		try (Connection conn = PowerSchool.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM assignments LEFT JOIN assignment_grades ON assignments.assignment_id = assignment_grades.assignment_id WHERE (student_id IS NULL OR student_id = ?) AND assignments.course_id = ?");
			stmt.setInt(1, studentId);
			stmt.setString(2, course_ids.get(input - 1));
			try (ResultSet rs = stmt.executeQuery()) {
				int assignmentCount = 1;
				while (rs.next()) {
					if (rs.getInt("points_possible") == 0) {
						System.out.printf("%d. %s / UNGRADED\n", assignmentCount, rs.getString("title"), rs.getInt("points_earned"), rs.getInt("points_possible"));
						assignmentCount++;
					}else {
						System.out.printf("%d. %s / %d (out of %d pts)\n", assignmentCount, rs.getString("title"), rs.getInt("points_earned"), rs.getInt("points_possible"));
						assignmentCount++;
					}
					
				}
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
	
	public void updateMPGrade(int course_id, int mp) {
		double pointsEarned = 0;
		double pointsPossible = 0;
		int grade = 0;
		String columnLabel = "mp" + Integer.toString(mp);
		if (mp < 5 && mp > 0) {
			String statement = "SELECT * FROM assignment_grades INNER JOIN assignments ON assignments.assignment_id = assignment_grades.assignment_id WHERE student_id = ? AND assignments.course_id = ? AND marking_period = ?";
			try (Connection conn = PowerSchool.getConnection()) {
				PreparedStatement stmt = conn.prepareStatement(statement);
				stmt.setInt(1, studentId);
				stmt.setInt(2, course_id);
				stmt.setInt(3, mp);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						pointsEarned += rs.getInt("points_earned");
						pointsPossible += rs.getInt("points_possible");
					}
				}
			}catch (SQLException e) {
				System.out.println(e);
			}

			grade = (int) Math.round((pointsEarned / pointsPossible) * 100);

			try (Connection conn = PowerSchool.getConnection()) {
				String updateStatement = "UPDATE course_grades SET " + columnLabel + " = ? WHERE course_id = ? AND student_id = ?";
				PreparedStatement stmt = conn.prepareStatement(updateStatement);
				stmt.setInt(1, grade);
				stmt.setInt(2, course_id);
				stmt.setInt(3, studentId);
				stmt.executeUpdate();
			}catch (SQLException e) {
				System.out.println(e);
			}
		}else if (mp == 5) {
			String statement = "SELECT * FROM assignment_grades INNER JOIN assignments ON assignments.assignment_id = assignment_grades.assignment_id WHERE student_id = ? AND assignments.course_id = ? AND is_midterm = 1";
			try (Connection conn = PowerSchool.getConnection()) {
				PreparedStatement stmt = conn.prepareStatement(statement);
				stmt.setInt(1, studentId);
				stmt.setInt(2, course_id);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						pointsEarned = rs.getInt("points_earned");
						pointsPossible = rs.getInt("points_possible");
					}
				}
			}catch (SQLException e) {
				System.out.println(e);
			}

			grade = (int) Math.round((pointsEarned / pointsPossible) * 100);
			try (Connection conn = PowerSchool.getConnection()) {
				PreparedStatement stmt = conn.prepareStatement("UPDATE course_grades SET midterm_exam = ? WHERE course_id = ? AND student_id = ?");
				stmt.setInt(1, grade);
				stmt.setInt(2, course_id);
				stmt.setInt(3, studentId);
				stmt.executeUpdate();
			}catch (SQLException e) {
				System.out.println(e);
			}
		}else if (mp == 6) {
			String statement = "SELECT * FROM assignment_grades INNER JOIN assignments ON assignments.assignment_id = assignment_grades.assignment_id WHERE student_id = ? AND assignments.course_id = ? AND is_final = 1";
			try (Connection conn = PowerSchool.getConnection()) {
				PreparedStatement stmt = conn.prepareStatement(statement);
				stmt.setInt(1, studentId);
				stmt.setInt(2, course_id);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						pointsEarned = rs.getInt("points_earned");
						pointsPossible = rs.getInt("points_possible");
					}
				}
			}catch (SQLException e) {
				System.out.println(e);
			}

			grade = (int) Math.round((pointsEarned / pointsPossible) * 100);
			try (Connection conn = PowerSchool.getConnection()) {
				PreparedStatement stmt = conn.prepareStatement("UPDATE course_grades SET final_exam = ? WHERE course_id = ? AND student_id = ?");
				stmt.setInt(1, grade);
				stmt.setInt(2, course_id);
				stmt.setInt(3, studentId);
				stmt.executeUpdate();
			}catch (SQLException e) {
				System.out.println(e);
			}
		}

		//after the mp / midterm / final grade has been updated, the course itself has to be updated
		this.updateGradeInCourse(course_id);
	}
	
	private void updateGradeInCourse(int course_id){
		Double[] grades = new Double[6];

		try (Connection conn = PowerSchool.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM course_grades WHERE student_id = ? AND course_id = ?");
			stmt.setInt(1, studentId);
			stmt.setInt(2, course_id);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					grades[0] = (double) rs.getInt("mp1");
					grades[1] = (double) rs.getInt("mp2");
					grades[2] = (double) rs.getInt("mp3");
					grades[3] = (double) rs.getInt("mp4");
					grades[4] = (double) rs.getInt("midterm_exam");
					grades[5] = (double) rs.getInt("final_exam");
				}
			}
		}catch (SQLException e) {
			System.out.println(e);
		}

		for(int i = 0; i < 7; i++) {
			if (grades[i] == 0) {
				return;
			}
		}

		int course_grade = (int) Math.round(Utils.getGrade(grades));

		try (Connection conn = PowerSchool.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement("UPDATE course_grades SET grade = ? WHERE course_id = ? AND student_id = ?");
			stmt.setInt(1, course_grade);
			stmt.setInt(2, course_id);
			stmt.setInt(3, studentId);
			stmt.executeUpdate();
		}catch (SQLException e) {
			System.out.println(e);
		}

		//after the course has been updated, the GPA for the student has to be updated
		Utils.updateGPA(this);

	}
    
    
    
    
    
}