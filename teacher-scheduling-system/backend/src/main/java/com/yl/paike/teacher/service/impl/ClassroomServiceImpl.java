package com.yl.paike.teacher.service.impl;

import com.yl.paike.teacher.dto.ClassroomCreateDTO;
import com.yl.paike.teacher.dto.ClassroomDTO;
import com.yl.paike.teacher.entity.Classroom;
import com.yl.paike.teacher.exception.EntityNotFoundException;
import com.yl.paike.teacher.mapper.ClassroomMapper;
import com.yl.paike.teacher.service.ClassroomService;
import com.yl.paike.teacher.util.BeanConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomMapper classroomMapper;
    
    @Override
    @Transactional
    public ClassroomDTO createClassroom(ClassroomCreateDTO createDTO) {
        Classroom classroom = new Classroom();
        classroom.setClassroomName(createDTO.getClassroomName());
        classroom.setMaxCapacity(createDTO.getMaxCapacity());
        classroom.setLocation(createDTO.getLocation());
        classroom.setFacilities(createDTO.getFacilities());
        classroom.setIsActive(true);
        classroom.generateClassroomCode(); // 生成教室编号

        classroomMapper.insert(classroom);
        log.info("教室创建成功，教室编号：{}", classroom.getClassroomCode());

        return BeanConverter.convert(classroom, ClassroomDTO.class);
    }

    @Override
    public ClassroomDTO getClassroomById(Long id) {
        Classroom classroom = classroomMapper.selectById(id);
        if (classroom == null) {
            throw new EntityNotFoundException("教室", id);
        }
        return BeanConverter.convert(classroom, ClassroomDTO.class);
    }

    @Override
    public List<ClassroomDTO> getAllActiveClassrooms() {
        List<Classroom> classrooms = classroomMapper.findByIsActiveTrue();
        return classrooms.stream()
            .map(c -> BeanConverter.convert(c, ClassroomDTO.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClassroomDTO updateClassroom(Long id, ClassroomCreateDTO updateDTO) {
        Classroom classroom = classroomMapper.selectById(id);
        if (classroom == null) {
            throw new EntityNotFoundException("教室", id);
        }

        classroom.setClassroomName(updateDTO.getClassroomName());
        classroom.setMaxCapacity(updateDTO.getMaxCapacity());
        classroom.setLocation(updateDTO.getLocation());
        classroom.setFacilities(updateDTO.getFacilities());

        classroomMapper.updateById(classroom);
        log.info("教室更新成功，教室ID：{}", id);

        return BeanConverter.convert(classroom, ClassroomDTO.class);
    }

    @Override
    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = classroomMapper.selectById(id);
        if (classroom == null) {
            throw new EntityNotFoundException("教室", id);
        }

        classroom.setIsActive(false);
        classroomMapper.updateById(classroom);

        log.info("教室删除成功，教室ID：{}", id);
    }
}
