package com.signature.service;

import java.sql.Timestamp;

public interface HistoryService {
    int recordAction(int serno, String inputfile, String action, Timestamp begintime, Timestamp endtime, String usertype, String outputfile, String memo);
}
