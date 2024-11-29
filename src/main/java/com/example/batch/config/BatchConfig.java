package com.example.batch.config;

import com.example.batch.model.Person;
import com.example.batch.model.Student;
import com.example.batch.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    //    final PersonRepository personRepository;
    final PersonRepository personRepository;
    final JobRepository jobRepository;
    final PlatformTransactionManager transactionManager;

    @Bean
    public FlatFileItemReader<Student> itemReader() {
        FlatFileItemReader<Student> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/students.csv"));
        itemReader.setName("csv reader");
        itemReader.setLinesToSkip(1);//we need to read from second line not from first line
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<Student> lineMapper() {
        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstname", "lastname", "age");

        BeanWrapperFieldSetMapper<Student> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public StudentProcessor studentProcessor() {
        return new StudentProcessor();
    }

    @Bean
    @Transactional(readOnly = true)
    public RepositoryItemWriter<Person> writer() {
        RepositoryItemWriter<Person> writer = new RepositoryItemWriter<>();
        writer.setRepository(personRepository);
        writer.setMethodName("save"); //save method on repository
        return writer;
    }

    @Bean
    public Step importStep() {
        return new StepBuilder("step 1", jobRepository)
                //mean that 1000 record per one thread
                .<Student, Person>chunk(1000, transactionManager)
                .reader(itemReader())
                .processor(studentProcessor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob() {
        return new JobBuilder("importStudents", jobRepository)
                .start(importStep())
//                .next(importStep())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        //how many threads you want to run
        taskExecutor.setConcurrencyLimit(10);
        return taskExecutor;
    }


}
