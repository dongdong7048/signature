package com.signature.mapper;

import com.signature.entity.Signature;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public interface SignatureMapper {

    /**
     * 方法名: findDataByPdfFilename
     * 內容: 依檔案路徑查詢簽名
     * 作者: Hugo.Tsao
     * 時間: 2020年12月24日
     *
     * @param pdfname
     * @return Signature or null
     */
    @Select("SELECT * FROM uploadlog WHERE filename = #{param1}")
    @ResultType(Signature.class)
    Signature findDataByPdfFilename(String pdfname);

    /**
     * 方法名: insert
     * 內容: 新增簽名資訊
     * 作者: Hugo.Tsao
     * 時間: 2020年12月24日
     *
     * @param signature
     * @return int
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert({"INSERT INTO uploadlog  (usertype , filename , filepath , createtime , updatetime , memo , savetype , totalpages , issigned ) VALUES (#{usertype} , #{filename} , #{filepath} , #{createtime,jdbcType=TIMESTAMP} , #{updatetime,jdbcType=TIMESTAMP} , #{memo} , #{savetype} , #{totalpages} , #{issigned} )"})
    //@Insert({"INSERT INTO uploadlog  VALUES (#{usertype} , #{filename} , #{filepath} , #{createtime,jdbcType=TIMESTAMP} , #{updatetime,jdbcType=TIMESTAMP} , #{memo} , #{savetype} , #{totalpages} , #{issigned})"})
    int insert(Signature signature);



    /**
     * 方法名: updateById
     * 內容: 依id更新簽名資訊
     * 作者: Hugo.Tsao
     * 時間: 2020年12月24日
     *
     * @param signature
     * @return int
     */
    @Update({"UPDATE uploadlog  set  usertype = #{usertype} , filename = #{filename} , filepath = #{filepath} , createtime = #{createtime,jdbcType=TIMESTAMP} , updatetime = #{updatetime,jdbcType=TIMESTAMP} , memo = #{memo} , savetype = #{savetype} , totalpages = #{totalpages} , issigned = #{issigned}  where id = #{id}"})
    int updateById(Signature signature);
}
