package com.apcsa.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.User;

enum RootAction { PASSWORD, DATABASE, LOGOUT, SHUTDOWN }

public class Application {

    private Scanner in;
    private User activeUser;

    /**
     * Creates an instance of the Application class, which is responsible for interacting
     * with the user via the command line interface.
     */

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

        while (true) {
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
        } else {
            // TODO - add cases for admin, teacher, student, and unknown
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