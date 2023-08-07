package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author :罗汉
 * @date : 2023/8/5
 */
@Service
@Slf4j

public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和口味数据
     *
     * @param dishDTO 菜dto
     */
    @Transactional
    @Override
    public void saveWishFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();

        // 对象属性拷贝赋值
        BeanUtils.copyProperties(dishDTO, dish);
        // 向菜品表插入1数据
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long dishId = dish.getId();

        // 向口味表插入多个数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {// 说明口味数据不为空
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            // 向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO 菜页面查询dto
     * @return {@link PageResult}
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        //下一条sql进行分页，自动加入limit关键字分页
        Page<DishVO>  page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 菜品的批量删除功能
     *
     * @param ids id
     */
    @Override
    public void delete(List<Long> ids) {
        //要删除三张表

        //1 判断当前菜品是否在起售中
        for (Long id : ids) {
           Dish dish= dishMapper.getById(id);
           if (dish.getStatus()== StatusConstant.ENABLE){
                //在起售状态 不能删除 抛个异常
               throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
           }
        }
        //2 如果当前菜品是否被某个套餐关联

        //3 如果都能删除
        //3.1 删除菜品表中的数据
        //3.2 删除菜品关联的口味表的数据

    }
}
