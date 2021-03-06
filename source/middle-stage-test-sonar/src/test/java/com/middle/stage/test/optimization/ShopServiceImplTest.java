package com.middle.stage.test.optimization;

import com.middle.stage.test.optimization.commons.CallResult;
import com.middle.stage.test.optimization.dao.DinnerTypeMapper;
import com.middle.stage.test.optimization.dao.data.DinnerDO;
import com.middle.stage.test.optimization.dao.data.DinnerTypeDO;
import com.middle.stage.test.optimization.dao.data.UserDO;
import com.middle.stage.test.optimization.dao.dto.CanteenDTO;
import com.middle.stage.test.optimization.model.form.DinnerTypeForm;
import com.middle.stage.test.optimization.service.*;
import com.middle.stage.test.optimization.service.impl.ShopServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class ShopServiceImplTest {

    @Mock
    private DinnerTypeService mockDinnerTypeService;
    @Mock
    private DinnerService mockDinnerService;
    @Mock
    private ShopCommonService mockShopCommonService;
    @Mock
    private CanteenDinnerService mockCanteenDinnerService;
    @Mock
    private OperationLogService mockOperationLogService;

    @InjectMocks
    private ShopServiceImpl shopService;

    @Autowired
    private DinnerTypeMapper dinnerTypeMapper;

    //全部数据集合
    private static List<DinnerTypeDO> dinnerTypeDOList = new ArrayList<>();
    //新餐别数据
    private static List<DinnerTypeForm> newDinnerList = new ArrayList<>();
    //旧餐别数据
    private static List<DinnerTypeDO> oldDinnerList = new ArrayList<>();
    //门店数据
    private static CanteenDTO canteenDTO = new CanteenDTO();
    //用户数据
    private static UserDO userDO = new UserDO();
    //转换map
    private static HashMap<Integer, DinnerTypeDO> dinnerTypeDOMap = new HashMap<>();
    //查询结果
    private static DinnerTypeDO dinnerTypeDO = new DinnerTypeDO();


    /**
     * 测试数据
     */
    @BeforeEach
    void beforSaveCanteenDinnerRelation() {
        userDO.setUserId(1);
        userDO.setUserName("zyq");
        userDO.setMerchantId(1);

        canteenDTO.setCanteenId(279);
        canteenDTO.setMerchantId(96);
        canteenDTO.setEquId(3);

        dinnerTypeDOList = dinnerTypeMapper.selectAll();

        dinnerTypeDO = dinnerTypeMapper.selectByPrimaryKey(1);
        oldDinnerList = dinnerTypeMapper.selectDinnerTypeByCanteenId(279);

        DinnerTypeForm dinnerTypeOne = new DinnerTypeForm(2, "早餐", "09:00", "10:00");
        DinnerTypeForm dinnerTypeTwo = new DinnerTypeForm(3, "午餐", "11:30", "13:00");
        newDinnerList.add(dinnerTypeOne);
        newDinnerList.add(dinnerTypeTwo);

        dinnerTypeDOMap = ShopServiceImpl.listToMap(dinnerTypeDOList);
        Assertions.assertNotEquals(0, dinnerTypeDOMap.size(), "dinnerTypeDOMap size is 0");
    }

    @Test
    void saveCanteenDinnerRelation() {

        //设置桩代码
        when(mockDinnerTypeService.selectByPrimaryKey(1)).thenReturn(dinnerTypeDO);
        when(mockDinnerTypeService.selectDinnerTypeByCanteenId(canteenDTO.getCanteenId())).thenReturn(oldDinnerList);
        when(mockDinnerTypeService.selectAll()).thenReturn(dinnerTypeDOList);
        when(mockDinnerTypeService.listToMap(dinnerTypeDOList)).thenReturn(dinnerTypeDOMap);

        // @see https://xiyun-international.github.io/java-unit-docs/05-other/02-api

        //知识点-自定义参数匹配器
        when(mockDinnerService.batchDeleteByCondition(argThat(new BatchDeleteMatcher()))).thenReturn(1);
        when(mockDinnerService.batchUpdateByCondition(argThat(new BatchUpdateMatcher()))).thenReturn(1);
        when(mockDinnerService.batchInsert(argThat(new BatchInsertMatcher()))).thenReturn(1);

        //知识点-无返回值方法的桩代码
        doNothing().when(mockShopCommonService).pushDinnerToIsv(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
        doNothing().when(mockOperationLogService).saveLog(anyLong(), any(), anyInt(), anyString(), anyString(), anyString(), anyInt(), anyInt());
        doNothing().when(mockCanteenDinnerService).putCanteenDinnerType2Cache(anyInt(), anyList());

        //执行被测试方法
        CallResult callResult = shopService.saveCanteenDinnerRelation(newDinnerList, userDO, canteenDTO);

        //验证是否执行（以目前的测试数据，所有方法都会执行到）
        verify(mockDinnerTypeService).selectAll();
        verify(mockDinnerTypeService).selectByPrimaryKey(1);
        verify(mockDinnerTypeService).selectDinnerTypeByCanteenId(canteenDTO.getCanteenId());
        verify(mockDinnerTypeService).listToMap(dinnerTypeDOList);
        verify(mockDinnerService).batchDeleteByCondition(argThat(new BatchDeleteMatcher()));
        verify(mockDinnerService).batchUpdateByCondition(argThat(new BatchUpdateMatcher()));
        verify(mockDinnerService).batchInsert(argThat(new BatchInsertMatcher()));
        verify(mockShopCommonService).pushDinnerToIsv(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
        verify(mockOperationLogService).saveLog(anyLong(), any(), anyInt(), anyString(), anyString(), anyString(), anyInt(), anyInt());
        verify(mockCanteenDinnerService).putCanteenDinnerType2Cache(anyInt(), anyList());

        //验证结果
        Assertions.assertNotNull(callResult);
        Assertions.assertEquals(CallResult.RETURN_STATUS_OK, callResult.getCode());
        //验证数据处理条数
        Assertions.assertEquals(3, callResult.getContent());
        log.info("[测试通过]");
    }
}

class BatchInsertMatcher implements ArgumentMatcher<List<DinnerDO>> {

    @Override
    public boolean matches(List<DinnerDO> list) {
        return list.size() == 1 && list.get(0).getDinnerTypeId() == 3;
    }
}

class BatchUpdateMatcher implements ArgumentMatcher<List<DinnerDO>> {

    @Override
    public boolean matches(List<DinnerDO> list) {
        return list.size() == 1 && list.get(0).getDinnerTypeId() == 2;
    }
}

class BatchDeleteMatcher implements ArgumentMatcher<List<DinnerDO>> {

    @Override
    public boolean matches(List<DinnerDO> list) {
        return list.size() == 1 && list.get(0).getDinnerTypeId() == 1;
    }
}