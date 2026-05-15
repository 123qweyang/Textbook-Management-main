package org.jeecg.modules.demo.zbu.service.impl;

import org.jeecg.modules.demo.zbu.entity.TStudent;
import org.jeecg.modules.demo.zbu.mapper.TStudentMapper;
import org.jeecg.modules.demo.zbu.service.ITStudentService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 学生表
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Service
public class TStudentServiceImpl extends ServiceImpl<TStudentMapper, TStudent> implements ITStudentService {

}
