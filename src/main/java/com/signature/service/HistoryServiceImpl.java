package com.signature.service;

import com.signature.entity.History;
import com.signature.mapper.HistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class HistoryServiceImpl implements HistoryService {


    @Autowired
    HistoryMapper historyMapper;

    @Override
    public int recordAction(int serno, String inputfile, String action, Timestamp begintime, Timestamp endtime, String usertype, String outputfile, String memo) {
        return historyMapper.insert(new History(serno, inputfile, action, begintime, endtime, usertype, outputfile, memo));
    }
}
