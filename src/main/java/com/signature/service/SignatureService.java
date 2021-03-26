package com.signature.service;

import com.signature.entity.Signature;

public interface SignatureService {
    public Signature findDataByPdfFilename(String pdfname);

    public int insert(Signature signature);

    public int updateById(Signature signature);
}
