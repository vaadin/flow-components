package com.vaadin.flow.data.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Person implements Serializable, Cloneable {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private int age;
    private Gender gender;
    private Address address;
    private boolean deceased;
    private Date birthDate;
    private boolean isSubscriber;

    private Integer salary; // null if unknown
    private Double salaryDouble; // null if unknown

    private BigDecimal rent;

    public Person() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Person [firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + ", age=" + age + ", gender=" + gender
                + ", address=" + address + ", deceased=" + deceased
                + ", salary=" + salary + ", salaryDouble=" + salaryDouble
                + ", rent=" + rent + "]";
    }

    public Person(String firstName, String lastName, String email, int age,
            Gender gender, Address address) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.address = address;
    }

    public Person(String firstName, int age) {
        super();
        this.firstName = firstName;
        this.age = age;
    }

    public boolean isSubscriber() {
        return isSubscriber;
    }

    public void setSubscriber(boolean isSubscriber) {
        this.isSubscriber = isSubscriber;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getDeceased() {
        return deceased;
    }

    public void setDeceased(boolean deceased) {
        this.deceased = deceased;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public BigDecimal getRent() {
        return rent;
    }

    public void setRent(BigDecimal rent) {
        this.rent = rent;
    }

    public Double getSalaryDouble() {
        return salaryDouble;
    }

    public void setSalaryDouble(Double salaryDouble) {
        this.salaryDouble = salaryDouble;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public Person clone() {
        try {
            return (Person) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("The Person object could not be cloned.",
                    e);
        }
    }

    public static Person createTestPerson1() {
        return new Person("Foo", "Bar", "yeah@cool.com", 46, Gender.MALE,
                new Address("Street", 1123, "Turku", Country.FINLAND));
    }

    public static Person createTestPerson2() {
        return new Person("Maya", "Dinkelstein", "maya@foo.bar", 18,
                Gender.FEMALE, new Address("Red street", 12, "Amsterdam",
                        Country.NETHERLANDS));
    }

    public enum MaritalStatus {
        MARRIED, SINGLE;
    }
}