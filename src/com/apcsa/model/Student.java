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
}