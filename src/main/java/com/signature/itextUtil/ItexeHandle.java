package com.signature.itextUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.io.codec.Base64;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteBuffer;
import com.itextpdf.io.source.IRandomAccessSource;
import com.itextpdf.io.source.RASInputStream;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.ReaderProperties;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ExternalBlankSignatureContainer;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSignatureAppearance.RenderingMode;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.SignatureUtil;

public class ItexeHandle {

    private static String CLIENT_FIELD_NAME = "";

    private static String IMAGE_PATH = "";

    private static float XRAY = 0;

    public static void createTmpPdf(String source, String temp, int count,String USERTYPE,String signPNG_path ,String TargetPathWithoutExtension,int currentPDFpages) {
        CLIENT_FIELD_NAME = "ClientFieldName" + count;
        //IMAGE_PATH = "D:signature" + (count == 1 ? "" : " (2)") + ".png";
        IMAGE_PATH=signPNG_path; //Paths.get("D:", "signature.png").toString();
        createEmptySignColumn(source, temp, "ClientReason", "ClientLocation", CLIENT_FIELD_NAME , USERTYPE ,TargetPathWithoutExtension ,currentPDFpages);
    }


    /**
     * 將png簽名檔合併在來源pdf檔並產生新的合併pdf檔
     */
    public static void createEmptySignColumn(String src, String temp, String reason, String location, String fieldName,String usertype,String TargetPathWithoutExtension,int currentPDFpages) {
        FileOutputStream fos = null;
        PdfReader reader = null;
        PdfSigner signer;
        String targetPDFpathWithoutExtension = TargetPathWithoutExtension;
        String tempFilePathWithoutExtension = "";
        String fileExtension = ".pdf";
//        int pages = 0;
//        try {
//            reader = new PdfReader(src, null);
//            fos = new FileOutputStream(temp);
//            pages = new PdfSigner(reader, fos, new StampingProperties().useAppendMode()).getDocument().getNumberOfPages();
//            System.out.println("pdf總頁數:"+pages);
//        } catch (IOException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }finally {
//            if(fos != null)
//                try {
//                    reader.close();
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//        }
        for (int i = 1; i <= currentPDFpages; i++) {
            try {
                if(i == 1 && currentPDFpages==1){
                    reader = new PdfReader(src,null);
                    fos = new FileOutputStream(targetPDFpathWithoutExtension + fileExtension);
                } else if(i == 1 && currentPDFpages!=1) {
                    reader = new PdfReader(src,null);
                    fos = new FileOutputStream(targetPDFpathWithoutExtension + i + fileExtension);
                    tempFilePathWithoutExtension = targetPDFpathWithoutExtension + i;
                } else {
                    reader = new PdfReader(tempFilePathWithoutExtension + fileExtension, null);
                    if (i == currentPDFpages)
                        fos = new FileOutputStream(targetPDFpathWithoutExtension + fileExtension);
                    else
                        fos = new FileOutputStream(targetPDFpathWithoutExtension + i + fileExtension);
                    if (i < currentPDFpages)
                        tempFilePathWithoutExtension = targetPDFpathWithoutExtension + i;
                }
                signer = new PdfSigner(reader, fos, new StampingProperties().useAppendMode());
                PdfSignatureAppearance appearance = signer.getSignatureAppearance();
                appearance.setReason(reason);// 123
                appearance.setLocation(location);// 456
                // appearance.setSignatureCreator("789");
                // System.out.println(Paths.get(IMAGE_PATH) +",,,");
                appearance.setSignatureGraphic(ImageDataFactory.create(Files.readAllBytes(Paths.get(IMAGE_PATH))));

                if("H".equals(usertype)) {
				appearance.setPageRect(new Rectangle(138, 155, 55, 26));// 座標為左下角定為 (0,0) x座標、y座標、圖的寬度、圖的高度
			}else if("S".equals(usertype)){
				appearance.setPageRect(new Rectangle(320, 155, 58, 29));
			}

                appearance.setRenderingMode(RenderingMode.GRAPHIC);
                appearance.setPageNumber(i);
                signer.setFieldName(fieldName + i);
                signer.signExternalContainer(
                        new ExternalBlankSignatureContainer(PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached), 8192);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } finally {
                if (fos != null)
                    try {
                        reader.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (i > 1) {
                    File tempFile = new File(targetPDFpathWithoutExtension + (i - 1) + fileExtension);
                    if (tempFile.exists())
                        tempFile.delete();
                }
            }

        }



//                FileOutputStream fos = null;
//        PdfReader reader = null;
//        try {
//            fos = new FileOutputStream(temp);
//            reader = new PdfReader(src);
//            PdfSigner signer = new PdfSigner(reader, fos, new StampingProperties().useAppendMode());
//            int pages = signer.getDocument().getNumberOfPages();
//            System.out.println(pages);
//            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
//            appearance.setReason(reason);//123
//            appearance.setLocation(location);//456
//            //appearance.setSignatureCreator("789");
//            //System.out.println(Paths.get(IMAGE_PATH) +",,,");
//            appearance.setSignatureGraphic(ImageDataFactory.create(Files.readAllBytes(Paths.get(IMAGE_PATH))));
//            if("H".equals(usertype)) {
//				appearance.setPageRect(new Rectangle(138, 141, 55, 26));//224, XRAY, 165, 220 //���U���}�l (0,0)  x�y�СBy�y�СB�Ϫ��e�סB�Ϫ�����System.out.println("XRAY:" + XRAY);
//			}else if("S".equals(usertype)){
//				appearance.setPageRect(new Rectangle(315, 141, 58, 29));
//			}
//				appearance.setRenderingMode(RenderingMode.GRAPHIC);
//            appearance.setPageNumber(1);
//            signer.setFieldName(fieldName);
//            signer.signExternalContainer(new ExternalBlankSignatureContainer(PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached), 8192);
//            //fos.flush();
//            //fos.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        } finally {
//
//            if (fos != null)
//                try {
//
//                    reader.close();
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//
//        }
    }






}
