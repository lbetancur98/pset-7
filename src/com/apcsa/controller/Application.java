package com.apcsa.controller;

import com.apcsa.data.*;
import com.apcsa.model.*;
import java.sql.*;
import java.util.*;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.User;

enum RootAction { PASSWORD, DATABASE, LOGOUT, SHUTDOWN }
enum StudentAction { GRADES, GRADESBYCOURSE, PASSWORD, LOGOUT }
enum AdminAction { FACULTY, FACULTYBYDEPT, STUDENT, STUDENTBYGRADE, STUDENTBYCOURSE, PASSWORD, LOGOUT }
enum TeacherAction { ENROLLMENT, AASSIGNMENT, DASSIGNMENT, ENTERGRADE, PASSWORD, LOGOUT}


public class Application {

    private Scanner in;
    private User activeUser;

    /**
     * Creates an instance of the Application class, which is responsible for interacting
     * with the user via the command line interface.
     */
    
    public static boolean running = true;

    public Application() {
        this.in = new Scanner(System.in);

        try {
            PowerSchool.initialize(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**	
     * Starts the PowerSchool application.
     * @throws SQLException 
     */
    
    public void startup() throws SQLException {
        System.out.println("PowerSchool -- now for students, teachers, and school administrators!");
        Connection conn = PowerSchool.getConnection();

        // continuously prompt for login credentials and attempt to login

        while (running) {
            System.out.print("\nUsername: ");
            String username = in.next();

            System.out.print("Password: ");
            String password = in.next();

            // if login is successful, update generic user to administrator, teacher, or student

            if (login(username, password)) {
                activeUser = activeUser.isAdministrator()
                    ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                    ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                    ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                    ? activeUser : null;

                if (isFirstLogin() && !activeUser.isRoot()) {
                    // first-time users need to change their passwords from the default provided
                	System.out.println("this is your first time logging in! Please change your password!");
                	String newPassword = in.next();
                	password = newPassword;
                	
                	PowerSchool.updateUserPassword(conn, username, newPassword);
                	
                	
                	
                }
                
                createAndShowUI();

                // create and show the user interface
                //
                // remember, the interface will be difference depending on the type
                // of user that is logged in (root, administrator, teacher, student)
            } else {
                System.out.println("\nInvalid username and/or password.");
            }
        }
    }
    
    public void createAndShowUI() throws SQLException{
    	System.out.println("\nHello, again, " + activeUser.getFirstName() + "!");

        if (activeUser.isRoot()) {
            showRootUI();
        } else if (activeUser.isStudent()) {
        	showStudentUI();
        } else if (activeUser.isTeacher()) {
        	showTeacherUI();
        	
        } else if (activeUser.isAdministrator()) {
        	showAdminUI;
        }
            // TODO - add cases for admin, teacher, student, and unknown
        
        
        if (activeUser.isStudent()) {
        	showStudentUI();
        }
    }
    
    /*
     * Displays an interface for root users.
     */

    private void showRootUI() throws SQLException {
        while (activeUser != null) {
            switch (getRootMenuSelection()) {
                case PASSWORD: resetPassword(); break;
                case DATABASE: factoryReset(); break;
                case LOGOUT: logout(); break;
                case SHUTDOWN: shutdown(); break;
                default: System.out.println("\nInvalid selection."); break;
            }
        }
    }
    
    private void showStudentUI() throws SQLException {
    	while(activeUser != null) {
    		switch(getStudentSelection()) {
			case GRADES:
				((Student) user).viewCourseGrades();
				return true;
			case GRADESBYCOURSE:
				((Student) user).viewAssignmentGradesByCourse(in);
				return true;
			case PASSWORD:
				((Student) user).changePassword(in);
				return true;
			case LOGOUT:
				return false;
		}
    	}
    }
    
    private void showAdminUI() throws SQLException {
    	while(activeUser != null) {
    		switch(getAdminSelection()) {
			case FACULTY:
				((Administrator) user).viewFaculty();
				return true;
			case FACULTYBYDEPT:
				((Administrator) user).viewFacultyByDept(in);
				return true;
			case STUDENT: 
				((Administrator) user).viewStudentEnrollment();
				return true;
			case STUDENTBYGRADE:
				((Administrator) user).viewStudentEnrollmentByGrade(in);
				return true;
			case STUDENTBYCOURSE:
				((Administrator) user).viewStudentEnrollmentByCourse(in);
				return true;
			case PASSWORD:
				((Administrator) user).changePassword(in);
				return true;
			case LOGOUT:
				return false;
		}
    	}
    }
    
    private void showTeacherUI() throws SQLException {
    	while(activeUser != null) {
    		switch(getTeacherSelection()) {
			case ENROLLMENT:
				((Teacher) user).enrollment(in);
				return true;
			case AASSIGNMENT:
				((Teacher) user).addAssignment(in);
				return true;
			case DASSIGNMENT:
				((Teacher) user).deleteAssignment(in);
				return true;
			case ENTERGRADE:
				((Teacher) user).enterGrade(in);
				return true;
			case PASSWORD:
                ((Teacher) user).changePassword(in);
                return true;
			case LOGOUT:
				return false;
		}
    	}
    }
    
    
    
    /*
     * Retrieves a root user's menu selection.
     * 
     * @return the menu selection
     */

    
    private RootAction getRootMenuSelection() {
        System.out.println();
        
        System.out.println("[1] Reset user password.");
        System.out.println("[2] Factory reset database.");
        System.out.println("[3] Logout.");
        System.out.println("[4] Shutdown.");
        System.out.print("\n::: ");
        
        switch (Utils.getInt(in, -1)) {
            case 1: return RootAction.PASSWORD;
            case 2: return RootAction.DATABASE;
            case 3: return RootAction.LOGOUT;
            case 4: return RootAction.SHUTDOWN;
            default: return null;
        }
     }
    

    
    public AdminAction getAdminSelection() {
    	int output = 0;
		do {
			System.out.println("\n[1] View faculty.");
			System.out.println("[2] View faculty by department.");
			System.out.println("[3] View student enrollment.");
			System.out.println("[4] View student enrollment by grade.");
			System.out.println("[5] View student enrollment by course.");
			System.out.println("[6] Change password.");
			System.out.println("[7] Logout.");
			System.out.print("\n::: ");
			try {
				output = in.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("\nYour input was invalid. Please try again.\n");
			}
			in.nextLine(); // clears the buffer
		} while (output < 1 || output > 7);
		
		switch(output) {
			case 1:
				return AdminAction.FACULTY;
			case 2:
				return AdminAction.FACULTYBYDEPT;
			case 3:
				return AdminAction.STUDENT;
			case 4:
				return AdminAction.STUDENTBYGRADE;
			case 5:
				return AdminAction.STUDENTBYCOURSE;
			case 6:
				return AdminAction.PASSWORD;
			case 7:
				return AdminAction.LOGOUT;
			default:
				return null;
		}
		
    }
    
    public TeacherAction getTeacherSelection() {
		int output = -1;
		do {
			System.out.println("\n[1] View enrollment by course.");
			System.out.println("[2] Add assignment.");
			System.out.println("[3] Delete assignment.");
			System.out.println("[4] Enter grade.");
            System.out.println("[5] Change password.");
			System.out.println("[6] Logout.");
			System.out.print("\n::: ");
			try {
                output = in.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("\nYour input was invalid. Please try again.\n");
			}
            in.nextLine();
		} while (output > 6 || output < 1);

		switch(output) {
			case 1:
				return TeacherAction.ENROLLMENT;
			case 2:
				return TeacherAction.AASSIGNMENT;
			case 3:
				return TeacherAction.DASSIGNMENT;
			case 4:
				return TeacherAction.ENTERGRADE;
			case 5:
				return TeacherAction.PASSWORD;
			case 6:
				return TeacherAction.LOGOUT;
			default:
				return null;
		}

	}
    
    public StudentAction getStudentSelection() {
    	int output = 0;
    	do {
    		System.out.println("\n[1] View course grades.");
			System.out.println("[2] View assignment grades by course.");
			System.out.println("[3] Change password.");
			System.out.println("[4] Logout.");
			System.out.print("\n::: ");
			try {
				output = in.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("\nYour input was invalid. Please try again.\n");
			}
			in.nextLine();
    	} while (output < 1 || output > 4);
    	
    	switch(output) {
    		case 1:
    			return StudentAction.GRADES;
    		case 2:
    			return StudentAction.GRADESBYCOURSE;
    		case 3:
    			return StudentAction.PASSWORD;
    		case 4:
    			return StudentAction.LOGOUT;
    		default:
    			return null;
    	}
	}
    
    private void viewCourseGrades() {
    	
    }
    
    private void viewAssignmentGrades() {
    	
    }
    
    private void resetUserPassword() {
    	
    }
    
    /*
     * Shuts down the application after encountering an error.
     * 
     * @param e the error that initiated the shutdown sequence
     */
    
    @SuppressWarnings("unused")
	private void shutdown(Exception e) {
        if (in != null) {
            in.close();
        }
        
        System.out.println("Encountered unrecoverable error. Shutting down...\n");
        System.out.println(e.getMessage());
                
        System.out.println("\nGoodbye!");
        System.exit(0);
    }

    /*
     * Releases all resources and kills the application.
     */

    private void shutdown() {        
        System.out.println();
            
        if (Utils.confirm(in, "Are you sure? (y/n) ")) {
            if (in != null) {
                in.close();
            }
            
            System.out.println("\nGoodbye!");
            System.exit(0);
        }
    }
    
    /*
     * Allows a root user to reset another user's password.
     */

    private void resetPassword() throws SQLException {
    	
    	Connection conn = PowerSchool.getConnection();    	
        //
        // prompt root user to enter username of user whose password needs to be reset
        //
        // ask root user to confirm intent to reset the password for that username
        //
        // if confirmed...
        //      call database method to reset password for username
        //      print success message
        //
    	
    	System.out.println("Enter the username of the account whose password is to be reset");
    	String targetUser = in.next();
    	
    	Timestamp ts = Timestamp.valueOf("1111-11-11 11:11:11.111");
    	
    	PowerSchool.updateUserPassword(conn, targetUser, targetUser);
    	
    	System.out.println("updated password");
    	
    	PowerSchool.updateLastLogin(conn, targetUser, ts);
    	
    	System.out.println("updated login time");
    	
    	System.out.println(targetUser);
    	
    }
    
    /*
     * Resets the database to its factory settings.
     */

    private void factoryReset() {
        //
        // ask root user to confirm intent to reset the database
    	
    	System.out.println("Are you sure you would like to reset the database? Write 1 for yes OR 2 for no");
    	int yesOrNo = in.nextInt();
    	System.out.println(yesOrNo == 1);
    	
    	if(yesOrNo == 1) {
    		System.out.println("we in here");
           
    		try {
                PowerSchool.initialize(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
    		
    		System.out.println();
    		System.out.println("The powerschool has been successfully reset");
    	}
        //
        // if confirmed...
        //      call database initialize method with parameter of true
        //      print success message
        //
    }
    
    /*
     * Logs out of the application.
     */

    private void logout() {
        //
        // ask root user to confirm intent to logout
        //
    	
    	System.out.println("Are you sure you would like to sign out? For yes enter 1 OR for no enter 2");
    	int yesOrNo = in.nextInt();
    	System.out.println();
    	
    	if(yesOrNo == 1) {
    		activeUser = null;
    	}
        // if confirmed...
        //      set activeUser to null
        //
    }

    /**
     * Logs in with the provided credentials.
     *
     * @param username the username for the requested account
     * @param password the password for the requested account
     * @return true if the credentials were valid; false otherwise
     */

    public boolean login(String username, String password) {
        activeUser = PowerSchool.login(username, password);

        return activeUser != null;
    }

    /**
     * Determines whether or not the user has logged in before.
     *
     * @return true if the user has never logged in; false otherwise
     */

    public boolean isFirstLogin() {
    	@SuppressWarnings("unused")
		boolean firstLogin = false;
    	if(activeUser.getLastLogin().equals("0000-00-00 00:00:00.000") || activeUser.getLastLogin().equals("1111-11-11 11:11:11.111")) {
    		firstLogin = true;
    	}
        return firstLogin;
    }

    /////// MAIN METHOD ///////////////////////////////////////////////////////////////////

    /*
     * Starts the PowerSchool application.
     *
     * @param args unused command line argument list
     */

    public static void main(String[] args) throws SQLException {
        Application app = new Application();

        app.startup();
    }
}