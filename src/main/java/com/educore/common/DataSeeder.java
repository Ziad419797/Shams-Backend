package com.educore.common;

import com.educore.parent.Parent;
import com.educore.parent.ParentRepository;
import com.educore.student.Student;
import com.educore.student.StudentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (parentRepository.count() == 0 && studentRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        try {
            log.info("بدء عملية Seeding البيانات...");

            // 1. تحميل أولياء الأمور أولاً
            InputStream parentStream = getClass().getResourceAsStream("/data/parents.json");
            List<Parent> parents = objectMapper.readValue(parentStream, new TypeReference<List<Parent>>() {});
            parentRepository.saveAll(parents);
            log.info("تم حفظ أولياء الأمور.");

            // 2. تحميل الطلاب وربطهم
            InputStream studentStream = getClass().getResourceAsStream("/data/students.json");
            // هنقرأ الطلاب كـ List of Maps عشان ناخد الـ parentPhone اللي مش موجود في الـ Entity
            List<Map<String, Object>> studentData = objectMapper.readValue(studentStream, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> data : studentData) {
                // تحويل الماب لـ Student Entity
                Student student = objectMapper.convertValue(data, Student.class);

                // البحث عن الأب برقم التليفون المبعوث في الجيسون
                String pPhone = (String) data.get("parentPhone");
                parentRepository.findByPhone(pPhone).ifPresent(student::setParent);

                studentRepository.save(student);
            }

            log.info("تمت عملية Seeding الطلاب بنجاح!");
        } catch (Exception e) {
            log.error("خطأ أثناء الـ Seeding: " + e.getMessage());
        }
    }
}