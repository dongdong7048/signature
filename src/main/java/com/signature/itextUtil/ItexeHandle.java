package com.signature.itextUtil;
import java.io.*;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;


public class ItexeHandle {

    //private static String CLIENT_FIELD_NAME = "";

    private static String IMAGE_PATH = "";

    //private static float XRAY = 0;

    public static void createTmpPdf(String source, String temp, int count,String USERTYPE,String signPNG_path ,String TargetPathWithoutExtension,int currentPDFpages) {
        //CLIENT_FIELD_NAME = "ClientFieldName" + count;
        //IMAGE_PATH = "D:signature" + (count == 1 ? "" : " (2)") + ".png";
        IMAGE_PATH=signPNG_path; //Paths.get("D:", "signature.png").toString();
        createEmptySignColumn(source, temp, USERTYPE ,TargetPathWithoutExtension ,currentPDFpages);
    }


    /**
     * 將png簽名檔合併在來源pdf檔並產生新的合併pdf檔
     */
    public static void createEmptySignColumn(String src, String temp,String usertype,String TargetPathWithoutExtension,int currentPDFpages) {
        FileOutputStream fos = null;
        PdfReader reader = null;
        String targetPDFpathWithoutExtension = TargetPathWithoutExtension;
        String tempFilePathWithoutExtension = "";
        String fileExtension = ".pdf";
        int imageWidth = 0;
        int imageHeight = 0;
        int signWidth = 0;
        int signHeight = 0;

        for (int i = 1; i <= currentPDFpages; i++) {
            try {
                if(i == 1 && currentPDFpages==1){
                    reader = new PdfReader(src);
                    fos = new FileOutputStream(targetPDFpathWithoutExtension + fileExtension);

                } else if(i == 1 && currentPDFpages!=1) {
                    reader = new PdfReader(src);
                    fos = new FileOutputStream(targetPDFpathWithoutExtension + i + fileExtension);
                    tempFilePathWithoutExtension = targetPDFpathWithoutExtension + i;

                } else {
                    reader = new PdfReader(tempFilePathWithoutExtension + fileExtension);
                    if (i == currentPDFpages) {
                        fos = new FileOutputStream(targetPDFpathWithoutExtension + fileExtension);

                    } else {
                        fos = new FileOutputStream(targetPDFpathWithoutExtension + i + fileExtension);

                    }
                    if (i < currentPDFpages)
                        tempFilePathWithoutExtension = targetPDFpathWithoutExtension + i;
                }


                //查找簽名位置
                //float[] position= null;

                //Modify file using PdfReader
                PdfStamper pdfStamper = new PdfStamper(reader, fos);

                Image image = Image.getInstance(IMAGE_PATH);

                if("H".equals(usertype)) {
                    imageWidth = 55;
                    imageHeight = 26;
                    signWidth = 138;
                    signHeight = 155;
                    //position = PdfKeywordFinder.getAddImagePositionXY(src,"Warehouse Signature:");
                    //appearance.setPageRect(new Rectangle(138, 155, 55, 26));// 座標為左下角定為 (0,0) x座標、y座標、圖的寬度、圖的高度
                }else if("S".equals(usertype)){
                    imageWidth = 58;
                    imageHeight = 29;
                    signWidth = 320;
                    signHeight = 155;
                    //position = PdfKeywordFinder.getAddImagePositionXY(src,"Supplier Signature:");
                    //appearance.setPageRect(new Rectangle(320, 155, 58, 29));
                }
                //Fixed Positioning
                image.scaleAbsolute(imageWidth, imageHeight);
                //Scale to new height and new width of image
                image.setAbsolutePosition(signWidth, signHeight);

                for(int j = 1;j<=currentPDFpages;j++) {
                    PdfContentByte content = pdfStamper.getUnderContent(j);
                    content.addImage(image);
                }
                pdfStamper.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
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
