package org.apache.poi.ss.formula;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.WorkbookEvaluatorUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.BaseXSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.BaseXSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PoiTester {

    public static void main(String[] args) {
        test1();
    }

    public static void test1() {

        Workbook wb = new XSSFWorkbook();
        //WorkbookEvaluatorUtil.getEvaluationWorkbook();
        WorkbookEvaluator e = ((BaseXSSFFormulaEvaluator) wb.getCreationHelper()
                .createFormulaEvaluator())._getWorkbookEvaluator();
        EvaluationWorkbook wb2 = e.getWorkbook();
        System.out.println(wb2.getClass().getName());
    }
}
