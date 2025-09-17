package com.example.sms.controller;

import com.example.sms.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GradingController {
    private final StudentService studentService;

    @Autowired
    public GradingController(StudentService studentService) {
        this.studentService = studentService;
    }
    
    @GetMapping("/grades")
    public String showGradingPage(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "grades";
    }
}