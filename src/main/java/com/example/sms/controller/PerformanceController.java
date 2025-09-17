package com.example.sms.controller;

import com.example.sms.entity.Student;
import com.example.sms.entity.Enrollment;
import com.example.sms.service.StudentService;
import com.example.sms.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class PerformanceController {
    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    @Autowired
    public PerformanceController(StudentService studentService, EnrollmentService enrollmentService) {
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/performance")
    public String showPerformanceDashboard(Model model) {
        List<Student> students = studentService.getAllStudents();
        List<Enrollment> allEnrollments = enrollmentService.getAllEnrollments();

        // Group enrollments by student
        Map<Student, List<Enrollment>> studentEnrollments = allEnrollments.stream()
            .collect(Collectors.groupingBy(Enrollment::getStudent));

        // Calculate performance metrics for each student
        Map<Student, StudentPerformance> performanceMap = new HashMap<>();

        for (Student student : students) {
            List<Enrollment> enrollments = studentEnrollments.getOrDefault(student, List.of());
            StudentPerformance performance = calculatePerformance(student, enrollments);
            performanceMap.put(student, performance);
        }

        model.addAttribute("performanceMap", performanceMap);
        return "performance";
    }

    private StudentPerformance calculatePerformance(Student student, List<Enrollment> enrollments) {
        StudentPerformance performance = new StudentPerformance();
        performance.setStudent(student);
        performance.setEnrollments(enrollments);

        if (enrollments.isEmpty()) {
            performance.setGpa(0.0);
            performance.setTotalCourses(0);
            performance.setPassedCourses(0);
            performance.setFailedCourses(0);
            return performance;
        }

        double totalPoints = 0.0;
        int passedCourses = 0;
        int failedCourses = 0;

        for (Enrollment enrollment : enrollments) {
            String grade = enrollment.getGrade();
            if (grade != null && !grade.trim().isEmpty()) {
                double gradePoint = convertGradeToPoint(grade.trim().toUpperCase());
                totalPoints += gradePoint;

                if (gradePoint >= 1.0) { // Assuming D grade or above is passing
                    passedCourses++;
                } else {
                    failedCourses++;
                }
            } else {
                failedCourses++; // No grade means failed
            }
        }

        performance.setGpa(totalPoints / enrollments.size());
        performance.setTotalCourses(enrollments.size());
        performance.setPassedCourses(passedCourses);
        performance.setFailedCourses(failedCourses);

        return performance;
    }

    private double convertGradeToPoint(String grade) {
        switch (grade) {
            case "A+": return 4.0;
            case "A": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B": return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C": return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D": return 1.0;
            case "F": return 0.0;
            default: return 0.0;
        }
    }

    public static class StudentPerformance {
        private Student student;
        private List<Enrollment> enrollments;
        private double gpa;
        private int totalCourses;
        private int passedCourses;
        private int failedCourses;

        // Getters and setters
        public Student getStudent() { return student; }
        public void setStudent(Student student) { this.student = student; }

        public List<Enrollment> getEnrollments() { return enrollments; }
        public void setEnrollments(List<Enrollment> enrollments) { this.enrollments = enrollments; }

        public double getGpa() { return gpa; }
        public void setGpa(double gpa) { this.gpa = gpa; }

        public int getTotalCourses() { return totalCourses; }
        public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }

        public int getPassedCourses() { return passedCourses; }
        public void setPassedCourses(int passedCourses) { this.passedCourses = passedCourses; }

        public int getFailedCourses() { return failedCourses; }
        public void setFailedCourses(int failedCourses) { this.failedCourses = failedCourses; }

        public double getPassRate() {
            return totalCourses > 0 ? (double) passedCourses / totalCourses * 100 : 0;
        }
    }
}