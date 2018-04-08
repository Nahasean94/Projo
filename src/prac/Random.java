package prac;


import database.PasswordAuthentication;

public class Random {
    public static void main(String[] args) {

     PasswordAuthentication passwordAuthentication=new PasswordAuthentication();
        System.out.println( passwordAuthentication.hash("stockmann2".toCharArray()));
        System.out.println( passwordAuthentication.authenticate("stockmann2".toCharArray(), passwordAuthentication.hash("stockmann2".toCharArray())));
         }
}
