
package net.projectsrl.pdf;

import java.util.Map;

import net.project.errors.AppCrash;

public abstract class CreateAcroFieldsPDF_base extends CreatePDF {

    public CreateAcroFieldsPDF_base(String outDirectory, Map<String, Object> map) {

        super(outDirectory, map);
    }

    @Override
    protected boolean printData(Map<String, Object> map) {

        printAcroFieldsData(map);

        return true;

    }

    @Override
    protected boolean isPageBreak(int rowIndex) {

        return rowIndex > getMaxRowsInPage();
    }

    private void printAcroFieldsData(Map<String, Object> map) {

        String acroFieldValue = null;
        try {

            for (String key : map.keySet()) {
                setAcroField(map, key);
            }

        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "map:" + map + " - acroFieldValue:" + acroFieldValue);
        }
    }

}
