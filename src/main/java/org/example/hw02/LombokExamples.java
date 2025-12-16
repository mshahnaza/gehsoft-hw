package org.example.hw02;

import lombok.*;
import lombok.experimental.Accessors;

public class LombokExamples {
    public static void main(String[] args) {
        // @Data example
        Person p1 = new Person();
        p1.setName("Alice"); //setters
        p1.setAge(25);
        p1.setEmail("alice@example.com");

        Person p2 = new Person();
        p2.setName("Alice");
        p2.setAge(25);
        p2.setEmail("alice@example.com");

        // toString()
        System.out.print("1) toString: " + p1 + "; ");

        // getters
        System.out.print("Get name: " + p1.getName() + "; ");

        // equals()
        System.out.print("Equals: " + p1.equals(p2) + "; "); // true

        // hashCode()
        System.out.print("HashCodes: p1=" + p1.hashCode() + " p2=" + p2.hashCode());

        System.out.println();

        // @RequiredArgsConstructor example
        Car c = new Car("Toyota", "Camry");
        System.out.println("2) Car: " + c);

        // @AllArgsConstructor example
        Book b = new Book("Effective Java", "Joshua Bloch", 416, 45.99);
        System.out.println("3) Book: " + b);

        // @Builder example
        House h = House.builder()
                .address("123 Main St")
                .rooms(4)
                .area(120.5)
                .price(250000)
                .build();
        System.out.println("3) House: " + h);

        // @Getter/Setter example
        UserProfile user = new UserProfile();
        user.setUsername("coder123");
        user.setFullName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setFollowersCount(1500);
        System.out.println("4) UserProfile: " + user.getUsername() + " " + user.getFullName() + " " + user.getEmail() + " " + user.getFollowersCount());

        // @Accessors(chain = true) example
        Movie movie = new Movie()
                .setTitle("Inception")
                .setDirector("Christopher Nolan")
                .setRating(9)
                .setReleaseYear(2010);
        System.out.println("5) Movie: " + movie.getTitle() + " Director:" + movie.getDirector() + " Rating:" + movie.getRating() + " Year:" + movie.getReleaseYear());

        // @SneakyThrows example
        FileReader fp = new FileReader();
        System.out.println("6) FileReader: " + fp);
        fp.readFile("nonexistent.txt");
    }
}

/**
 * Lombok @Data сочетает возможности:
 * @ToString,
 * @EqualsAndHashCode,
 * @Getter, @Setter,
 * @RequiredArgsConstructor
 */
@Data
class Person {
    private String name;
    private int age;
    private String email;
}

/**
 * Lombok @RequiredArgsConstructor генерирует конструктор с final полями
 */
@RequiredArgsConstructor
class Car {
    private final String brand;
    private final String model;
}

/**
 * Lombok @AllArgsConstructor генерирует конструктор со всеми полями
 */
@AllArgsConstructor
class Book {
    private String title;
    private String author;
    private int pages;
    private double price;
}

/**
 * Lombok @Builder генерирует Builder класс с методом build()
 */
@Builder
class House {
    private String address;
    private int rooms;
    private double area;
    private double price;
}

/**
 * Lombok @Getter и @Setter генерируют геттеры и сеттеры для всех полей
 */
@Getter
@Setter
class UserProfile {
    private String username;
    private String fullName;
    private String email;
    private int followersCount;
}

/**
 * Lombok @SneakyThrows позволяет выбрасывать проверяемые исключения,
 * не объявляя их явно в условии,
 * без try/catch
 */
class FileReader {
    @SneakyThrows
    public void readFile(String path) {
        throw new java.io.FileNotFoundException("File not found: " + path);
    }
}

/**
 * Lombok @Getter, @Setter и @Accessors(chain = true) генерируют
 * геттеры и сеттеры с возможностью chain-вызова
 */
@Getter
@Setter
@Accessors(chain = true)
class Movie {
    private String title;
    private String director;
    private int releaseYear;
    private double rating;
}