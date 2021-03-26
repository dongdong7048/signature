package com.signature.mapper;

import com.signature.entity.History;
import com.signature.entity.Signature;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

@Component //由spring管理
public interface HistoryMapper {


    /**
     * 方法名: insert
     * 內容: 新增歷史紀錄資訊
     * 作者: Hugo.Tsao
     * 時間: 2020年12月30日
     *
     * @param history
     * @return int
     */
    @Options(useGeneratedKeys = true, keyProperty = "serno")
    @Insert({"INSERT INTO history  (inputfile , action , begintime , endtime , usertype , outputfile , memo )VALUES (#{inputfile} , #{action} , #{begintime,jdbcType=TIMESTAMP} , #{endtime,jdbcType=TIMESTAMP} , #{usertype} , #{outputfile} , #{memo} )"})
    int insert(History history);



    /**
     * 方法名: updateById
     * 內容: 依id更新簽名資訊
     * 作者: Hugo.Tsao
     * 時間: 2020年12月24日
     *
     * @param signature
     * @return int
     */
    @Update({"UPDATE uploadlog  set  usertype = #{usertype} , filename = #{filename} , filepath = #{filepath} , createtime = #{createtime,jdbcType=DATE} , updatetime = #{updatetime,jdbcType=DATE} , memo = #{memo},savetype=#{savetype}  where id = #{id}"})
    int updateById(Signature signature);
}
