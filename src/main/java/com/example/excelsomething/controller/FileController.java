package com.example.excelsomething.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.excelsomething.model.KeyValue;
import com.example.excelsomething.model.SelectArry;
import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 文件上传和下载
 *
 * @author sunshixiong（1285913468@qq.com）
 * @date 2020/2/28-15:03
 */
@RestController
@RequestMapping(value = "/file")
public class FileController {

  private static final String PATH = "D:/file";

  @PostMapping
  public Object upLoadFile(@RequestParam("file") MultipartFile file, String name, HttpServletRequest request) throws IOException {
    String fileName = name + Objects.requireNonNull(file.getOriginalFilename())
            .substring(file.getOriginalFilename().lastIndexOf("."));
    File fileLocal = new File(PATH + File.separator + fileName);
    File upLoadfile = new File(PATH);
    if (!upLoadfile.exists() && !upLoadfile.isDirectory()) {
      upLoadfile.mkdirs();
    }
    try {
      file.transferTo(fileLocal);
    } catch (IOException e) {
      return "上传文件失败";
    }
    HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(fileLocal));
    HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
    int totalCell = hssfSheet.getRow(0).getPhysicalNumberOfCells();
    HSSFRow hssfRow = hssfSheet.getRow(0);
    List rowList = new ArrayList();
    for (int i = 0; i <= totalCell; i++) {
      rowList.add(new KeyValue(i, hssfRow.getCell(i) != null ? hssfRow.getCell(i).getStringCellValue() : ""));
    }
    return rowList;
  }
  @PostMapping("/downLoad")
  public boolean downLoadExcel(@RequestBody SelectArry selectArry, HttpServletResponse response) throws IOException {
    File fileOne = new File(PATH + File.separator + "table1.xls");
    File fileTwo = new File(PATH + File.separator + "table2.xls");
    HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(fileOne));
    HSSFSheet hssfSheetOne = hssfWorkbook.getSheetAt(0);
    HSSFWorkbook hssfWorkbookTwo = new HSSFWorkbook(new FileInputStream(fileTwo));
    HSSFSheet hssfSheetTwo = hssfWorkbookTwo.getSheetAt(0);
    /** 写入excel */
    File fileNew = new File(PATH + File.separator + "对比后的表格.xlsx");
    ExcelWriter excelWriter = EasyExcel.write(fileNew).build();
    WriteSheet writeSheet1 = EasyExcel.writerSheet("主表一样数据").build();
    WriteSheet writeSheet2 = EasyExcel.writerSheet("副表一样数据").build();
    WriteSheet writeSheet3 = EasyExcel.writerSheet("主表不一样数据").build();
    WriteSheet writeSheet4 = EasyExcel.writerSheet("副表不一样数据").build();
    excelWriter.write(getRows(hssfSheetOne.getRow(0)), writeSheet1);
    excelWriter.write(getRows(hssfSheetTwo.getRow(0)), writeSheet2);
    excelWriter.write(getRows(hssfSheetOne.getRow(0)), writeSheet3);
    excelWriter.write(getRows(hssfSheetTwo.getRow(0)), writeSheet4);
    for (int i = 1; i <= hssfSheetOne.getLastRowNum(); i++) {
      boolean different = true;
      for (int j = 1; j <= hssfSheetTwo.getLastRowNum(); j++) {
        boolean equal = true;
        for (int k = 0; k < selectArry.getOneList().length; k++) {
          if (!hssfSheetOne.getRow(i).getCell(selectArry.getOneList()[k]).getStringCellValue().equals(
                  hssfSheetTwo.getRow(j).getCell(selectArry.getTwoList()[k]).getStringCellValue()
          )) {
            equal = false;
            break;
          }

        }
        if (equal) {
          different = false;
          excelWriter.write(getRows(hssfSheetOne.getRow(i)), writeSheet1);
          excelWriter.write(getRows(hssfSheetTwo.getRow(j)), writeSheet2);
        }
      }
      if(different){
        excelWriter.write(getRows(hssfSheetOne.getRow(i)), writeSheet3);
      }

    }
    for (int i = 1; i <= hssfSheetTwo.getLastRowNum(); i++) {
      boolean different = true;
      for (int j = 1; j <= hssfSheetOne.getLastRowNum(); j++) {
        boolean equalValue = true;
        for (int k = 0; k < selectArry.getOneList().length; k++) {
          if (!hssfSheetOne.getRow(j).getCell(selectArry.getOneList()[k]).getStringCellValue().equals(
                  hssfSheetTwo.getRow(i).getCell(selectArry.getTwoList()[k]).getStringCellValue()
          )) {
            equalValue = false;
            break;
          }
        }
        if (equalValue) {
          different = false;
        }

      }
      if(different){
        excelWriter.write(getRows(hssfSheetTwo.getRow(i)), writeSheet4);
      }
    }
    excelWriter.finish();
    return true;
  }
  private List<List<Object>> getRows(HSSFRow row) {
    List<List<Object>> list = Lists.newArrayList();
    List<Object> listRow = Lists.newArrayList();
    for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
      listRow.add(row.getCell(i) != null ? row.getCell(i).getStringCellValue() : "");
    }
    list.add(listRow);
    return list;
  }
  @GetMapping("/getExcel")
  public void getExcel(HttpServletResponse response) throws UnsupportedEncodingException {
    response.setContentType("application/vnd.ms-excel");
    response.setCharacterEncoding("utf-8");
    String fileName = URLEncoder.encode("对照后的表格", "UTF-8");
    response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
    File fileNew = new File(PATH + File.separator + "对比后的表格.xlsx");
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(fileNew);
      OutputStream outputStream = response.getOutputStream();
      IOUtils.copy(inputStream, outputStream);
      outputStream.flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      fileNew.delete();
    }
  }
}
