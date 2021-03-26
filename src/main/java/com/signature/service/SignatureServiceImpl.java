package com.signature.service;

import com.signature.entity.Signature;
import com.signature.mapper.SignatureMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignatureServiceImpl implements SignatureService{

    @Autowired
    SignatureMapper signatureMapper;

    @Override
    public Signature findDataByPdfFilename(String pdfname) {
        return signatureMapper.findDataByPdfFilename(pdfname);
    }

    @Override
    public int insert(Signature signature) {
        return signatureMapper.insert(signature);
    }

    @Override
    public int updateById(Signature signature) {
        return signatureMapper.updateById(signature);
    }
}
