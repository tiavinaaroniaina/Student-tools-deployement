package com.ecole._2.models;

public class UserAbsenceConsecutive {
    private String userId;
    private String login;
    private String displayname;
    private String firstName;
    private String lastName;
    private Integer joursAbsentsConsecutifs;
    private Integer totalAbsences;

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getDisplayname() {
        return displayname;
    }
    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getJoursAbsentsConsecutifs() {
        return joursAbsentsConsecutifs;
    }
    public void setJoursAbsentsConsecutifs(Integer joursAbsentsConsecutifs) {
        this.joursAbsentsConsecutifs = joursAbsentsConsecutifs;
    }

    public Integer getTotalAbsences() {
        return totalAbsences;
    }
    public void setTotalAbsences(Integer totalAbsences) {
        this.totalAbsences = totalAbsences;
    }

    @Override
    public String toString() {
        return "UserAbsenceConsecutive{" +
                "userId='" + userId + '\'' +
                ", login='" + login + '\'' +
                ", displayname='" + displayname + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", joursAbsentsConsecutifs=" + joursAbsentsConsecutifs +
                ", totalAbsences=" + totalAbsences +
                '}';
    }
}
