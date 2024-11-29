package com.example.batch.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
public class Student {
    @Id
    Long id;
    String firstName;
    String lastName;
    int age;
}
