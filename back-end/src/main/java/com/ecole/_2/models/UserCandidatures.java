package com.ecole._2.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "candidature")
public class UserCandidatures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender")
    private String gender;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "country")
    private String country;

    @Column(name = "birth_city")
    private String birthCity;

    @Column(name = "birth_country")
    private String birthCountry;

    @Column(name = "postal_street")
    private String postalStreet;

    @Column(name = "postal_complement")
    private String postalComplement;

    @Column(name = "postal_city")
    private String postalCity;

    @Column(name = "postal_zip_code")
    private String postalZipCode;

    @Column(name = "postal_country")
    private String postalCountry;

    @Column(name = "contact_affiliation")
    private String contactAffiliation;

    @Column(name = "contact_last_name")
    private String contactLastName;

    @Column(name = "contact_first_name")
    private String contactFirstName;

    @Column(name = "contact_phone1")
    private String contactPhone1;

    @Column(name = "contact_phone2")
    private String contactPhone2;

    @Column(name = "max_level_memory")
    private Double maxLevelMemory;

    @Column(name = "max_level_logic")
    private Double maxLevelLogic;

    @Column(name = "other_information")
    private String otherInformation;

    @Column(name = "language")
    private String language;

    @Column(name = "meeting_date")
    private ZonedDateTime meetingDate;

    @Column(name = "piscine_date")
    private ZonedDateTime piscineDate;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "pin")
    private String pin;

    @Column(name = "phone_country_code")
    private String phoneCountryCode;

    @Column(name = "hidden_phone")
    private Boolean hiddenPhone;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(String birthCity) {
        this.birthCity = birthCity;
    }

    public String getBirthCountry() {
        return birthCountry;
    }

    public void setBirthCountry(String birthCountry) {
        this.birthCountry = birthCountry;
    }

    public String getPostalStreet() {
        return postalStreet;
    }

    public void setPostalStreet(String postalStreet) {
        this.postalStreet = postalStreet;
    }

    public String getPostalComplement() {
        return postalComplement;
    }

    public void setPostalComplement(String postalComplement) {
        this.postalComplement = postalComplement;
    }

    public String getPostalCity() {
        return postalCity;
    }

    public void setPostalCity(String postalCity) {
        this.postalCity = postalCity;
    }

    public String getPostalZipCode() {
        return postalZipCode;
    }

    public void setPostalZipCode(String postalZipCode) {
        this.postalZipCode = postalZipCode;
    }

    public String getPostalCountry() {
        return postalCountry;
    }

    public void setPostalCountry(String postalCountry) {
        this.postalCountry = postalCountry;
    }

    public String getContactAffiliation() {
        return contactAffiliation;
    }

    public void setContactAffiliation(String contactAffiliation) {
        this.contactAffiliation = contactAffiliation;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }

    public String getContactFirstName() {
        return contactFirstName;
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }

    public String getContactPhone1() {
        return contactPhone1;
    }

    public void setContactPhone1(String contactPhone1) {
        this.contactPhone1 = contactPhone1;
    }

    public String getContactPhone2() {
        return contactPhone2;
    }

    public void setContactPhone2(String contactPhone2) {
        this.contactPhone2 = contactPhone2;
    }

    public Double getMaxLevelMemory() {
        return maxLevelMemory;
    }

    public void setMaxLevelMemory(Double maxLevelMemory) {
        this.maxLevelMemory = maxLevelMemory;
    }

    public Double getMaxLevelLogic() {
        return maxLevelLogic;
    }

    public void setMaxLevelLogic(Double maxLevelLogic) {
        this.maxLevelLogic = maxLevelLogic;
    }

    public String getOtherInformation() {
        return otherInformation;
    }

    public void setOtherInformation(String otherInformation) {
        this.otherInformation = otherInformation;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public ZonedDateTime getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(ZonedDateTime meetingDate) {
        this.meetingDate = meetingDate;
    }

    public ZonedDateTime getPiscineDate() {
        return piscineDate;
    }

    public void setPiscineDate(ZonedDateTime piscineDate) {
        this.piscineDate = piscineDate;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getPhoneCountryCode() {
        return phoneCountryCode;
    }

    public void setPhoneCountryCode(String phoneCountryCode) {
        this.phoneCountryCode = phoneCountryCode;
    }

    public Boolean getHiddenPhone() {
        return hiddenPhone;
    }

    public void setHiddenPhone(Boolean hiddenPhone) {
        this.hiddenPhone = hiddenPhone;
    }
}
