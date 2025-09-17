package com.example.sms.controller;

import com.example.sms.entity.Student;
import com.example.sms.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "students";
    }

    @GetMapping("/new")
    public String createStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "create_student";
    }

    @PostMapping
    public String saveStudent(@ModelAttribute("student") Student student, RedirectAttributes redirectAttributes) {
        try {
            // Check if email already exists
            if (isEmailExists(student.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email address already exists. Please use a different email.");
                redirectAttributes.addFlashAttribute("student", student);
                return "redirect:/students/new";
            }

            studentService.saveStudent(student);
            redirectAttributes.addFlashAttribute("success", "Student added successfully!");
            return "redirect:/students";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Email address already exists. Please use a different email.");
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/students/new";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while saving the student.");
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/students/new";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editStudentForm(@PathVariable Long id, Model model) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return "redirect:/students";
        }
        model.addAttribute("student", student);
        return "edit_student";
    }

    @PostMapping("/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute("student") Student student, RedirectAttributes redirectAttributes) {
        try {
            Student existingStudent = studentService.getStudentById(id);
            if (existingStudent == null) {
                redirectAttributes.addFlashAttribute("error", "Student not found.");
                return "redirect:/students";
            }

            // Check if email is being changed and if it already exists for another student
            if (!existingStudent.getEmail().equals(student.getEmail()) && isEmailExists(student.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email address already exists. Please use a different email.");
                redirectAttributes.addFlashAttribute("student", student);
                return "redirect:/students/edit/" + id;
            }

            existingStudent.setFirstName(student.getFirstName());
            existingStudent.setLastName(student.getLastName());
            existingStudent.setEmail(student.getEmail());
            studentService.saveStudent(existingStudent);
            redirectAttributes.addFlashAttribute("success", "Student updated successfully!");
            return "redirect:/students";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Email address already exists. Please use a different email.");
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/students/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating the student.");
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/students/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentById(id);
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "Student not found.");
                return "redirect:/students";
            }

            studentService.deleteStudentById(id);
            redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
            return "redirect:/students";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the student.");
            return "redirect:/students";
        }
    }

    private boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // Check if any student with this email exists
        return studentService.getAllStudents().stream()
                .anyMatch(student -> email.equalsIgnoreCase(student.getEmail()));
    }
}