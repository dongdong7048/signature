package com.signature.controller;


import com.signature.entity.ActionName;
import com.signature.entity.Signature;
import com.signature.itextUtil.ItexeHandle;
import com.signature.service.HistoryService;
import com.signature.service.SignatureService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j // lombok套件，引入Slf4j，直接可使用log功能
@Controller
public class SignatureController {


    //切記，每一次進行訪問都不會改變其值的資料，可把它設為成員變量使用；會隨著訪問而有所異動的值，不要設為成員變量，不然每次訪問這個變量值就會一直變動！

    //央倉pdf outbox uat路徑
    public static final String WHRTN_uat_outbox = "\\\\10.102.64.1\\rtv_esigned_pdf_uat\\outbox\\";

    //央倉pdf inbox uat路徑
    public static final String WHRTN_uat_inbox = "\\\\10.102.64.1\\rtv_esigned_pdf_uat\\inbox\\";

    //央倉pdf outbox Production路徑
    public static final String WHRTN_outbox = "\\\\10.102.64.1\\rtv_esigned_pdf\\outbox\\";

    //央倉pdf inbox Production路徑
    public static final String WHRTN_inbox = "\\\\10.102.64.1\\rtv_esigned_pdf\\inbox\\";


    //本機D槽路徑
    public static final String D_Disk_path = "D:\\";

    //央倉桌面temp_Prod資料夾路徑
    public static final String Desktop_temp_Prod_DirectoryPath = FileSystemView.getFileSystemView().getHomeDirectory().toString() + "\\temp_Prod\\";

    //央倉桌面temp_Uat資料夾路徑
    public static final String Desktop_temp_Uat_DirectoryPath = FileSystemView.getFileSystemView().getHomeDirectory().toString() + "\\temp_Uat\\";

    //本機桌面temp資料夾路徑
    public static final String Desktop_temp_DirectoryPath = FileSystemView.getFileSystemView().getHomeDirectory().toString() + "\\temp\\";


    //本機桌面路徑
    public static final String Desktop_Path = FileSystemView.getFileSystemView().getHomeDirectory().toString();

    //本機桌面inbox資料夾路徑
    public static final String Desktop_inbox_DirectoryPath = FileSystemView.getFileSystemView().getHomeDirectory().toString() + "\\inbox\\";

    ItexeHandle itexeHandle = null;


    @Autowired
    HistoryService historyService;

    @Autowired
    SignatureService signatureService;

    @Autowired
    ActionName actionName;


    /***
     * 訪問電子簽章系統首頁，url需代入pdfname(退倉單pdf檔案名)與usertype(H:央倉人員,S:供應商)參數
     *
     * @param pdfname
     * @param usertype
     * @param request
     * @param model
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping("/")
    public String index(@RequestParam("pdfname") String pdfname, @RequestParam("usertype") String usertype, HttpServletRequest request, Model model) throws java.io.IOException {
        
        //檢驗pdf檔案是否存在以及usertype是否輸入正確，若返回false則顯示404頁面
        boolean result = checkPdfFileIsExistAndUsertypeIsExist(pdfname,usertype);
        if(!result){
            return "error/404";
        }
        log.info("===" + actionName.SHOW_PDFPAGE + "===");


        historyService.recordAction(0, pdfname, actionName.SHOW_PDFPAGE, getNowTime(), null, usertype, null, null);
        String original_pdfname = pdfname;
        String original_pdfnameWithoutExtension = getFilenameWithoutExtension(pdfname);
        String original_pdfFile_Path = Desktop_temp_DirectoryPath + pdfname;
        String current_db_pdfname = "";
        String current_db_pdfnameWithoutExtension = "";
        String current_db_pdfFilePath = "";
        String current_db_usertype = "";
        String Current_Usertype = usertype;
        String fileStatus = "";
        String issigned = "";
        int index = 0;
        //檢查桌面temp資料夾與inbox資料夾存不存在，不存在就建立
        File desktopTempDirectory = new File(Desktop_temp_DirectoryPath);
        if(!desktopTempDirectory.exists()){
            desktopTempDirectory.mkdir();
        }
        File desktopInboxDirectory = new File(Desktop_inbox_DirectoryPath);
        if(!desktopInboxDirectory.exists()){
            desktopInboxDirectory.mkdir();
        }
        //查詢DB中是否有參數pdfname的存在
        Signature signature = checkFileIsExistAndReturnSignature(original_pdfname);
        if (signature == null) {
            fileStatus = "NONE";
            issigned = "N";
            //若DB沒有簽章紀錄，則把outbox原始檔案複製到temp資料夾，先確認一下temp中有沒有此檔案，沒有的話再複製，有的話就不用複製
            File fileCopyToTemp = new File(Desktop_temp_DirectoryPath+original_pdfname);
            if(!fileCopyToTemp.exists())
            Files.copy(new File(D_Disk_path+original_pdfname).toPath(), new File(Desktop_temp_DirectoryPath+original_pdfname).toPath());
            model.addAttribute("current_db_pdfname","");
        } else {
            //每次訪問時都要先把所有在temp或static/下的所有暫存png刪掉
            deleteAllTempImageFile(signature.getFilename());
            deleteAllTempImageFile(original_pdfname);
            fileStatus = "EXIST";
            current_db_pdfname = signature.getFilename();
            current_db_pdfnameWithoutExtension = getFilenameWithoutExtension(signature.getFilename());
            current_db_pdfFilePath = signature.getFilepath();
            current_db_usertype = signature.getUsertype();
            model.addAttribute("current_db_pdfname",current_db_pdfname);
            issigned = "Y";
        }


        File resource = null;
        //若無簽章過的檔案，則以原始檔顯示
        if ("NONE".equals(fileStatus)) {
            resource = new File(Desktop_temp_DirectoryPath+pdfname);
            request.setAttribute("req",original_pdfnameWithoutExtension);
            index = transPDFfiletoPNGfile(resource,original_pdfname);
        } else if ("EXIST".equals(fileStatus)) {
            //signature = (Signature) request.getAttribute("signature");
            resource = new File(signature.getFilepath());
            request.setAttribute("req",current_db_pdfnameWithoutExtension);
            index = transPDFfiletoPNGfile(resource,current_db_pdfname);
        }
        //將original檔案相關訊息傳遞下去
        Signature signature1 = new Signature();
        signature1.setUsertype(Current_Usertype);
        signature1.setFilename(original_pdfname);
        signature1.setFilepath(original_pdfFile_Path);
        signature1.setIssigned(issigned);
        int pdfPages = getPDFfilePages(new File(original_pdfFile_Path));
        signature1.setTotalpages(pdfPages);
        model.addAttribute("signature",signature1);
        model.addAttribute("tempPNGIndex", index);

        return "showPdf";
    }


    /***
     * 取得pdf的頁數
     * 2021年3月19日
     *
     * @param PDFfileResource
     * @return
     * @throws IOException
     */
    private int getPDFfilePages(File PDFfileResource) throws IOException {
        PDDocument doc = PDDocument.load(PDFfileResource);
        int pages = doc.getNumberOfPages();
        if(doc !=null)
            doc.close();
        return pages;
    }


    /**
     * 檢核pdfname是否存在於outbox資料夾以及使用者參數是否為S或H
     * 2021年3月19日
     *
     * @param  pdfname , usertype
     * @return boolean
     */
    private boolean checkPdfFileIsExistAndUsertypeIsExist(String pdfname, String usertype) {
        File file = new File(D_Disk_path+pdfname);
        if(!file.exists()){
            return false;
        }else {
            String usertypeRange = "S,H";
            if(usertypeRange.contains(usertype)){
                return true;
            }
            return false;
        }
    }

    /**
     * 取得當前系統時間
     * 2020年12月28日
     *
     * @return Timestamp
     */
    private Timestamp getNowTime() {
        return new Timestamp(System.currentTimeMillis());
    }


    /**
     * 檢驗屈臣氏提供的pdfname參數及使用者類型是否已存在於db，若存在返就返回Signature對象，若不存在就返回null
     * 2020年12月28日
     *
     * @param pdfname
     * @return Signature or null
     */
    public Signature checkFileIsExistAndReturnSignature(String pdfname) {

        String pdfname_suffix_S = getFilenameWithoutExtension(pdfname) + "S.pdf";
        String pdfname_suffix_H = getFilenameWithoutExtension(pdfname) + "H.pdf";
        String pdfname_suffix_A = getFilenameWithoutExtension(pdfname) + "A.pdf";
        //Signature signature = signatureMapper.findDataByPdfFilename(pdfname_suffix_S);
        Signature signature = signatureService.findDataByPdfFilename(pdfname_suffix_S);
        if (signature == null) {
            signature = signatureService.findDataByPdfFilename(pdfname_suffix_H);
            if (signature == null) {
                signature = signatureService.findDataByPdfFilename(pdfname_suffix_A);
                if (signature == null) {
                    return null;
                } else {
                    return signature;
                }
            } else {
                return signature;
            }
        } else {
            return signature;
        }

    }



    /*
    @RequestMapping("/showPdf")
    public String showPdfPage(HttpServletRequest request, Model model) throws Exception {

        Signature signature=null;
        String path = ResourceUtils.getURL("classpath:").getPath();
        //File resource = new ClassPathResource("static/WatsonsFile.pdf").getFile();
        String resource1 = "";
        File resource = null;
        //若無簽章過的檔案，則以原始檔顯示
        if("NONE".equals(fileStatus)){
            resource = new File(WatsonsOriginalFilepath);
        }else if("EXIST".equals(fileStatus)){
            signature  = (Signature)request.getSession().getAttribute("signature");
            resource = new File(signature.getFilepath());
            //resource1 = signature.getFilepath();
        }

        int index = transPDFfiletoPNGfile(resource);
        model.addAttribute("tempPNGIndex",index);


        return "showPdf";
    }
    */


    /***
     *
     * 開啟SignaturePad簽章畫面
     * 2020年12月28日
     *
     * @param signature
     * @param current_db_pdfname
     * @param model
     * @return
     */
    @RequestMapping("/openSignaturePad")
    public String openSignaturePad(@ModelAttribute Signature signature,@RequestParam("current_db_pdfname") String current_db_pdfname ,  Model model) {
        model.addAttribute("signature",signature);
        model.addAttribute("current_db_pdfname",current_db_pdfname);
        try {
            historyService.recordAction(0, signature.getFilename(), actionName.EXIT_PDFPAGE, null, getNowTime(), signature.getUsertype(), null, null);
            log.info("===" + actionName.VISIT_SIGNATUREPAD + "===");
            historyService.recordAction(0, signature.getFilename(), actionName.VISIT_SIGNATUREPAD, getNowTime(), null, signature.getUsertype(), null, null);
        }catch (Exception e){
            log.debug(e.toString());
        }
        return "signature_pad";
    }



    /***
     * 將簽章儲存成png檔，依照商業邏輯合併pdf檔並記錄資訊於db
     * 2020年12月28日
     *
     * @param original_pdfname
     * @param original_filepath
     * @param Current_Usertype
     * @param issigned
     * @param totalpages
     * @param current_db_pdfname
     * @param multipartFile
     * @return
     * @throws Exception
     */
    @RequestMapping("/save")
    @ResponseBody
    public List save(@RequestParam("fileName") String original_pdfname,@RequestParam("filepath") String original_filepath,
                       @RequestParam("usertype") String Current_Usertype,@RequestParam("issigned") String issigned,
                       @RequestParam("totalpages") String totalpages, @RequestParam(value="current_db_pdfname") String current_db_pdfname,
                     @RequestParam("signatureFile") MultipartFile multipartFile) throws Exception {


        List<String> returnDataList = null;
        try {
            String original_filepathWithoutExtension = getFilenameWithoutExtension(original_filepath);
            String original_pdfnameWithoutExtension = getFilenameWithoutExtension(original_pdfname);
            String Current_db_pdfnameWithoutExtension = null;
            String Current_db_filepathWithoutExtension = null;

            //db中有前次簽章紀錄，把它抓出來
            if (!"".equals(current_db_pdfname)) {
                //Signature signature = signatureMapper.findDataByPdfFilename(current_db_pdfname);
                Signature signature = signatureService.findDataByPdfFilename(current_db_pdfname);
                Current_db_pdfnameWithoutExtension = getFilenameWithoutExtension(signature.getFilename());
                Current_db_filepathWithoutExtension = Desktop_temp_DirectoryPath + Current_db_pdfnameWithoutExtension;
            }
            Signature signature = null;
            String SOURCE = "";  //pdf合併的資料來源，若db不存在就用原始檔，若存在就需依業務邏輯處理
            String TARGET = "";  //pdf合併後的存檔路徑
            String update_db_filename = "";
            String update_db_filepath = "";
            String mergeDemand = "";
            String TargetPathWithoutExtension = "";
            String dbSaveType = "";
            returnDataList = new ArrayList<String>();

            //判斷要合併的是哪一個檔案，若issigned為N，就合併原始檔並存入db，反之若為Y，且當前使用者身分與存在檔身分一致，就合併存在的那張並更新db，若身分不一致，就再合併到另一張並更新db
            if ("Y".equals(issigned)) {
                //signature = (Signature) request.getSession().getAttribute("signature");
                //signature= signatureMapper.findDataByPdfFilename(current_db_pdfname);
                signature = signatureService.findDataByPdfFilename(current_db_pdfname);
                // 此檔案只被供應商(S)簽過章，且此次欲簽章的使用者參數仍是S
                if (Current_Usertype.equals(signature.getUsertype()) && "S".equals(Current_Usertype) && "S".equals(signature.getSavetype())) {
                    TARGET = original_filepathWithoutExtension + "S.pdf";
                    SOURCE = original_filepath;
                    TargetPathWithoutExtension = original_filepathWithoutExtension + "S";
                    update_db_filename = original_pdfnameWithoutExtension + "S.pdf";
                    update_db_filepath = getFilenameWithoutExtension(original_filepath) + "S.pdf";

                    // 此檔案只被央倉人員(H)簽過章，且此次欲簽章的使用者參數仍是H
                } else if (Current_Usertype.equals(signature.getUsertype()) && "H".equals(Current_Usertype) && "H".equals(signature.getSavetype())) {
                    TARGET = original_filepathWithoutExtension + "H.pdf";
                    SOURCE = original_filepath;
                    TargetPathWithoutExtension = original_filepathWithoutExtension + "H";
                    update_db_filename = original_pdfnameWithoutExtension + "H.pdf";
                    update_db_filepath = getFilenameWithoutExtension(original_filepath) + "H.pdf";

                    // 此檔案只被央倉人員(H)簽過章，但此次欲簽章的使用者參數為S
                } else if (!Current_Usertype.equals(signature.getUsertype()) && "S".equals(Current_Usertype) && !"A".equals(signature.getSavetype())) {
                    TARGET = original_filepathWithoutExtension + "A.pdf";
                    SOURCE = signature.getFilepath();
                    TargetPathWithoutExtension = original_filepathWithoutExtension + "A";
                    signature.setSavetype("A");
                    update_db_filename = original_pdfnameWithoutExtension + "A.pdf";
                    update_db_filepath = getFilenameWithoutExtension(original_filepath) + "A.pdf";
                    mergeDemand = "S";


                    // 此檔案已被供應商(S)簽過章，但此次欲簽章的使用者參數是H
                } else if (!Current_Usertype.equals(signature.getUsertype()) && "H".equals(Current_Usertype) && !"A".equals(signature.getSavetype())) {
                    TARGET = original_filepathWithoutExtension + "A.pdf";
                    SOURCE = signature.getFilepath();
                    TargetPathWithoutExtension = original_filepathWithoutExtension + "A";
                    signature.setSavetype("A");
                    update_db_filename = original_pdfnameWithoutExtension + "A.pdf";
                    update_db_filepath = getFilenameWithoutExtension(original_filepath) + "A.pdf";
                    mergeDemand = "H";

                    // 此檔案已被供應商(S)與央倉人員(H)簽過章，且此次欲簽章的使用者參數是H
                } else if ("H".equals(Current_Usertype) && "A".equals(signature.getSavetype())) {
                    TARGET = original_filepathWithoutExtension + "A.pdf";
                    SOURCE = original_filepathWithoutExtension + "S.pdf";
                    TargetPathWithoutExtension = original_filepathWithoutExtension + "A";
                    signature.setSavetype("A");
                    update_db_filename = original_pdfnameWithoutExtension + "A.pdf";
                    update_db_filepath = getFilenameWithoutExtension(original_filepath) + "A.pdf";
                    mergeDemand = "H";

                    // 此檔案已被供應商(S)與央倉人員(H)簽過章，且此次欲簽章的使用者參數是S
                } else if ("S".equals(Current_Usertype) && "A".equals(signature.getSavetype())) {
                    TARGET = original_filepathWithoutExtension + "A.pdf";
                    SOURCE = original_filepathWithoutExtension + "H.pdf";
                    TargetPathWithoutExtension = original_filepathWithoutExtension + "A";
                    signature.setSavetype("A");
                    update_db_filename = original_pdfnameWithoutExtension + "A.pdf";
                    update_db_filepath = getFilenameWithoutExtension(original_filepath) + "A.pdf";
                    mergeDemand = "S";
                }

                signature.setUsertype(Current_Usertype);
                signature.setFilename(update_db_filename);
                signature.setFilepath(update_db_filepath);
                signature.setUpdatetime(getNowTime());
                signature.setTotalpages(Integer.parseInt(totalpages));
                signature.setIssigned("Y");
                dbSaveType = "UPDATE";

            } else if ("N".equals(issigned)) {
                SOURCE = original_filepath;
                TARGET = original_filepathWithoutExtension + Current_Usertype + ".pdf";
                update_db_filename = original_pdfnameWithoutExtension + Current_Usertype + ".pdf";
                TargetPathWithoutExtension = original_filepathWithoutExtension + Current_Usertype;
                signature = new Signature();
                signature.setId(0);
                signature.setUsertype(Current_Usertype);
                signature.setFilename(update_db_filename);
                signature.setFilepath(original_filepathWithoutExtension + Current_Usertype + ".pdf");
                signature.setCreatetime(getNowTime());
                signature.setUpdatetime(null);
                signature.setSavetype(Current_Usertype);
                signature.setMemo(null);
                signature.setTotalpages(Integer.parseInt(totalpages));
                signature.setIssigned("Y");
                dbSaveType = "INSERT";
            }


            //簽章png檔名都用原始pdfname+signature
            File file = new File(Desktop_temp_DirectoryPath + getFilenameWithoutExtension(update_db_filename) + "signature.png");
            //將簽名png檔存在temp資料夾中
            multipartFile.transferTo(file);
            //合併pdf並存檔
            String result = pdfMerge(SOURCE, TARGET, get7RamdomInteger(), Current_Usertype, Desktop_temp_DirectoryPath + getFilenameWithoutExtension(update_db_filename) + "signature.png", TargetPathWithoutExtension, Integer.parseInt(totalpages));
            returnDataList.add(result);
            //將pdf合併檔複製一份到inbox資料夾
            File resource = new File(TARGET);
            String inboxFilepath = Desktop_inbox_DirectoryPath + update_db_filename;
            File targetFile = new File(inboxFilepath);
            if (targetFile.exists()) {
                targetFile.delete();
                copyFiletoInbox(resource, Desktop_inbox_DirectoryPath, update_db_filename);
            } else {
                copyFiletoInbox(resource, Desktop_inbox_DirectoryPath, update_db_filename);
            }
            log.info("====" + "簽章合併pdf → 簽章源頭pdf:" + SOURCE + "簽章目標pdf:" + TARGET + " 是否合併成功" + result);
            if ("fail".equals(result)) {
                return returnDataList;
            } else {

                if ("H".equals(mergeDemand)) {
                    SOURCE = original_filepath;
                    TARGET = original_filepathWithoutExtension + "H.pdf";
                    TargetPathWithoutExtension = original_filepathWithoutExtension + "H";
                    result = pdfMerge(SOURCE, TARGET, get7RamdomInteger(), Current_Usertype, Desktop_temp_DirectoryPath + getFilenameWithoutExtension(update_db_filename) + "signature.png", TargetPathWithoutExtension, Integer.parseInt(totalpages));
                    returnDataList.set(0, result);
                    inboxFilepath = Desktop_inbox_DirectoryPath + original_pdfnameWithoutExtension + "H.pdf";
                    File targetFile_1 = new File(inboxFilepath);
                    if (targetFile_1.exists()) {
                        targetFile_1.delete();
                        copyFiletoInbox(new File(TARGET), Desktop_inbox_DirectoryPath, original_pdfnameWithoutExtension + "H.pdf");
                    } else {
                        copyFiletoInbox(new File(TARGET), Desktop_inbox_DirectoryPath, original_pdfnameWithoutExtension + "H.pdf");
                    }
                } else if ("S".equals(mergeDemand)) {
                    SOURCE = original_filepath;
                    TARGET = original_filepathWithoutExtension + "S.pdf";
                    TargetPathWithoutExtension = original_filepathWithoutExtension + "S";
                    result = pdfMerge(SOURCE, TARGET, get7RamdomInteger(), Current_Usertype, Desktop_temp_DirectoryPath + getFilenameWithoutExtension(update_db_filename) + "signature.png", TargetPathWithoutExtension, Integer.parseInt(totalpages));
                    returnDataList.set(0, result);
                    inboxFilepath = Desktop_inbox_DirectoryPath + original_pdfnameWithoutExtension + "S.pdf";
                    File targetFile_2 = new File(inboxFilepath);
                    if (targetFile_2.exists()) {
                        targetFile_2.delete();
                        copyFiletoInbox(new File(TARGET), Desktop_inbox_DirectoryPath, original_pdfnameWithoutExtension + "S.pdf");
                    } else {
                        copyFiletoInbox(new File(TARGET), Desktop_inbox_DirectoryPath, original_pdfnameWithoutExtension + "S.pdf");
                    }
                }
                if ("fail".equals(result)) {
                    return returnDataList;
                } else {
                    //確定都有存檔成功才寫入db
                    if ("OK".equals(result)) {
                        if (dbSaveType.equals("UPDATE")) {
                            signatureService.updateById(signature);
                            returnDataList.add(update_db_filename);
                            historyService.recordAction(0, original_pdfname, actionName.SAVE, getNowTime(), null, Current_Usertype, signature.getFilepath(), null);
                        } else if (dbSaveType.equals("INSERT")) {
                            signatureService.insert(signature);
                            returnDataList.add(update_db_filename);
                            historyService.recordAction(0, original_pdfname, actionName.SAVE, getNowTime(), null, Current_Usertype, signature.getFilepath(), null);
                        }
                    }
                }
            }
        }catch (Exception e){
            log.debug(e.toString());
        }

        return returnDataList;
    }


    /**
     * 亂數取得6位整數
     * 2020年12月28日
     *
     * @return int
     */
    private int get7RamdomInteger() {
        return (int) ((Math.random() * 9 + 1) * 1000000);//(int)(Math.random()*8998)+1000+1;
    }


    /***
     *
     * @param model
     * @param request
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping("/showPdfResult")
    public String showPdfResult(Model model, HttpServletRequest request) throws java.io.IOException {
        log.info("===" + actionName.SHOW_MERGEDPDF + "===");
        //recordAction(0, Current_pdfname, actionName.SAVE, null, getNowTime(), Current_Usertype, null, null);
        //recordAction(0, Current_pdfname, actionName.EXIT_SIGNATUREPAD, null, getNowTime(), Current_Usertype, null, null);
        //Signature signature = (Signature) request.getSession().getAttribute("signature");
        //signature = signatureMapper.findDataByPdfFilename(signature.getFilename());
        //Current_pdfname = signature.getFilepath();
        //recordAction(0, Current_pdfname, actionName.SHOW_MERGEDPDF, getNowTime(), null, Current_Usertype, null, null);
        //File resource = new File(Current_pdfFile_Path);
        //tempPNGIndex = 0; //歸0
        //currentPDFpages = 0; //歸0
        //String filenameWithoutExtension = getFilenameWithoutExtension(fileName);
        //request.setAttribute("req",filenameWithoutExtension);
        System.out.println("req:"+request.getAttribute("req").toString());
        //int index = transPDFfiletoPNGfile(resource,fileName);
        //model.addAttribute("tempPNGIndex", index);
        //model.addAttribute("signature", signature);

        return "pdfResult";
    }


    /***
     *
     * 將合併pdf檔轉成png檔顯示
     * 2020年12月28日
     *
     * @param model
     * @param request
     * @param update_db_filename
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping("/resultpage")
    public String resultpage(Model model, HttpServletRequest request , @RequestParam(value = "filename",required = false) String update_db_filename) throws java.io.IOException {
        log.info("===" + actionName.SHOW_MERGEDPDF + "===");
        System.out.println("filename:"+update_db_filename);
        //Signature signature = (Signature) request.getSession().getAttribute("signature");
        //Signature signature = signatureMapper.findDataByPdfFilename(update_db_filename);
        Signature signature = signatureService.findDataByPdfFilename(update_db_filename);
        //Current_pdfname = signature.getFilepath();
        //recordAction(0, update_db_filename, actionName.SAVE, null, getNowTime(), signature.getUsertype(), null, null);
        //recordAction(0, update_db_filename, actionName.EXIT_SIGNATUREPAD, null, getNowTime(), signature.getUsertype(), null, null);
        //recordAction(0, update_db_filename, actionName.SHOW_MERGEDPDF, getNowTime(), null, signature.getUsertype(), null, null);
        historyService.recordAction(0, update_db_filename, actionName.SAVE, null, getNowTime(), signature.getUsertype(), null, null);
        historyService.recordAction(0, update_db_filename, actionName.EXIT_SIGNATUREPAD, null, getNowTime(), signature.getUsertype(), null, null);
        historyService.recordAction(0, update_db_filename, actionName.SHOW_MERGEDPDF, getNowTime(), null, signature.getUsertype(), null, null);
        File resource = new File(signature.getFilepath());
        //tempPNGIndex = 0; //歸0
        //currentPDFpages = 0; //歸0
        String filenameWithoutExtension = getFilenameWithoutExtension(update_db_filename);
        request.setAttribute("req",filenameWithoutExtension);
        //System.out.println("req:"+request.getAttribute("req").toString());
        int index = transPDFfiletoPNGfile(resource,update_db_filename);
        model.addAttribute("tempPNGIndex", index);
        model.addAttribute("update_db_filename", update_db_filename);
        return "resultpage";
    }


    /**
     * 將檔案複製一份到另一個位置
     * 2020年12月28日
     *
     * @param resource 原檔案
     * @param destinationPath 目的路徑位置
     * @param destinationFilename 目的名稱
     */
    private void copyFiletoInbox(File resource,String destinationPath,String destinationFilename) throws IOException {
        if(resource.exists() && !(new File(destinationPath+destinationFilename).exists()))
        Files.copy(resource.toPath(), new File(destinationPath+destinationFilename).toPath());
    }


    @RequestMapping("/success")
    public String success(Model model,@RequestParam("pdfname")String pdfname) throws Exception {
        //recordAction(0, Current_pdfname, actionName.EXIT_MERGEDPDF, null, getNowTime(), Current_Usertype, null, null);
        //model.addAttribute("Current_Usertype", Current_Usertype);
        //Signature signature = signatureMapper.findDataByPdfFilename(pdfname);
        Signature signature = signatureService.findDataByPdfFilename(pdfname);
        //刪除簽名檔以及pdf轉png的檔案
        deleteTempImageFile(signature.getFilename());
        return "success";
    }

    @RequestMapping("/fail")
    public String fail(Model model) throws Exception {
        //model.addAttribute("Current_Usertype", Current_Usertype);
        //刪除簽名檔以及pdf轉png的檔案
        //deleteTempImageFile();
        return "fail";
    }


    /**
     * 取得去除副檔名的檔案名稱
     * 2020年12月28日
     *
     * @param filename 檔案名稱
     * @return String
     */
    public String getFilenameWithoutExtension(String filename) {
        int dot = filename.lastIndexOf(".");
        return filename.substring(0, dot);
    }


    /**
     * 將pdf檔轉成png檔
     * 2020年12月28日
     *
     * @param PDFfileResource 來源pdf檔
     * @return tempPNGIndex
     */
    public int transPDFfiletoPNGfile(File PDFfileResource,String pdfname) throws java.io.IOException {
        //這樣寫打成jar包全部都會報錯 --> java.io.FileNotFoundException: file:\C:\Users\dong\IdeaProjects\springboot_test\target\springboot_test-0.0.1-SNAPSHOT.jar!\BOOT-INF\classes!\static\image1.png (檔案名稱、目錄名稱或磁碟區標籤語法錯誤。)
        //需要打成war包才不會有問題
        int tempPNGIndex = 0;
        String path = ResourceUtils.getURL("classpath:").getPath();
        log.info("===========classpath: "+ path + "=================");
        //String path = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        //Resource resource = new ClassPathResource("static/image"+);
        //InputStream inputStream = resource.getInputStream();
        //InputStream stream = getClass().getClassLoader().getResourceAsStream("static/image.png");
        //File targetFile = new File("image"+tempPNGIndex+".png");
        PDDocument doc = PDDocument.load(PDFfileResource);
        PDFRenderer renderer = new PDFRenderer(doc);
        int pageCount = doc.getNumberOfPages();
        for (int i = 0; i < pageCount; i++) {
            BufferedImage image = renderer.renderImageWithDPI(i, 400);//165
            //BufferedImage image = renderer.renderImage(i, 2.5f);
            ImageIO.write(image, "PNG", new File(path + "static/image" + getFilenameWithoutExtension(pdfname) + i + ".png"));
            log.info("===========ImageIO savePath: "+ (path + "static/image" + getFilenameWithoutExtension(pdfname) + i + ".png") + "=================");
            tempPNGIndex = i;
        }
        if (doc != null) {
            doc.close();

        }
        return tempPNGIndex;
    }

    /**
     * 進行簽名圖檔與pdf檔合併
     * 2020年12月28日
     *
     * @param source   來源pdf檔
     * @param TARGET   合併後產出的pdf檔
     * @param count    參數
     * @param usertype 使用者代號
     * @param signPNG_path 簽名圖檔路徑
     * @param TargetPathWithoutExtension   產出的pdf檔路徑去除副檔名
     * @param currentPDFpages   黨前pdf檔頁數
     * @return result
     */
    public static String pdfMerge(String source, String TARGET, int count, String usertype, String signPNG_path , String TargetPathWithoutExtension ,int currentPDFpages)  {

        String result = "";
        try {
            ItexeHandle.createTmpPdf(source, TARGET, count, usertype, signPNG_path ,TargetPathWithoutExtension ,currentPDFpages);
            result = "OK";

        } catch (Exception e) {
            e.printStackTrace();
            result = "fail";
        }

        return result;
    }


    /**
     * 將執行簽名系統中產出暫存的簽名png檔及畫面顯示pdf轉png的暫存檔全部刪除
     * 2020年12月28日
     */
    @RequestMapping("/deleteTempImageFile")
    @ResponseBody
    public String deleteTempImageFile(@RequestParam(required = false) String update_db_filename) throws Exception {
        File file = new File(Desktop_temp_DirectoryPath + getFilenameWithoutExtension(update_db_filename) + "signature.png");
        if (file.exists())
            file.delete();
        //刪除pdf轉png的檔案
        String path = ResourceUtils.getURL("classpath:").getPath();
        int i =0;
        //刪除pdf轉png的檔案
        //String path = ResourceUtils.getURL("classpath:").getPath();
        File tempPNG=null;
        while((tempPNG=new File(path + "static/image" + getFilenameWithoutExtension(update_db_filename)+ i + ".png")).exists()){
            tempPNG.delete();
            i++;
        }
        tempPNG = new File(path+"static/image.png");
        if(tempPNG.exists())
            tempPNG.delete();
        return "OK";
    }


    /**
     * 將執行簽名系統中產出暫存的簽名png檔及畫面顯示pdf轉png的暫存檔全部刪除
     * 2020年12月28日
     */
    private void deleteAllTempImageFile(String pdfname) throws IOException {
        File pngfile = new File(Desktop_temp_DirectoryPath + getFilenameWithoutExtension(pdfname) + "signature.png");
        if (pngfile.exists())
            pngfile.delete();
        String path = ResourceUtils.getURL("classpath:").getPath();
        log.info("=========deleteAllTempImageFile======:  classpath:"+ path);
        int i =0;
        //刪除pdf轉png的檔案
        //String path = ResourceUtils.getURL("classpath:").getPath();
        File tempPNG=null;
        while((tempPNG=new File(path + "static/image" + getFilenameWithoutExtension(pdfname)+ i + ".png")).exists()){
            tempPNG.delete();
            i++;
        }
        tempPNG = new File(path+"static/image.png");
        if(tempPNG.exists())
            tempPNG.delete();

    }

    //    /**
//     * 將http的檔案路徑轉成java的檔案路徑
//     * 2020年12月28日
//     *
//     * @param urlPath 瀏覽器網址欄傳來的參數
//     * @return String
//     */
//    public String transHttpPathToJavaPath(String urlPath) {
//        return urlPath.replace("/", "\\");
//    }
}
