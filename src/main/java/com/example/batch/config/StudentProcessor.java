package com.example.batch.config;

import com.example.batch.model.Person;
import com.example.batch.model.Student;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class StudentProcessor implements ItemProcessor<Student, Person> {
    @Override
    public Person process(Student item) throws Exception {
        // all the business logic goes here
        item.setId(null);
        ModelMapper mapper = new ModelMapper();
        Person person = mapper.map(item, Person.class);
        return person;
//        return item;
    }
}
