package com.weakcurrent.service;

import com.weakcurrent.dto.StockOutCreateDTO;
import com.weakcurrent.dto.StockOutUpdateDTO;
import com.weakcurrent.entity.StockOut;

import java.util.List;

public interface StockOutService {

    StockOut create(StockOutCreateDTO dto);

    StockOut update(StockOutUpdateDTO dto);

    void delete(Long id);

    StockOut getById(Long id);

    List<StockOut> list();

    List<StockOut> listByAccessoryId(Long accessoryId);
}
