package com.vinsguru.students.service;

import com.vinsguru.students.dto.StudentRequest;
import com.vinsguru.students.dto.StudentResponse;
import com.vinsguru.students.entity.Student;
import com.vinsguru.students.exceptions.StudentNotFoundException;
import com.vinsguru.students.mapper.StudentMapper;
import com.vinsguru.students.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    public StudentResponse createStudent(StudentRequest request) {
        Student student = studentMapper.toEntity(request);
        Student saved = studentRepository.save(student);
        return studentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(studentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = findStudentOrThrow(id);
        return studentMapper.toResponse(student);
    }

    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findStudentOrThrow(id);
        studentMapper.updateEntity(student, request);
        return studentMapper.toResponse(student);
    }

    public void deleteStudent(Long id) {
        Student student = findStudentOrThrow(id);
        studentRepository.delete(student);
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }
}
