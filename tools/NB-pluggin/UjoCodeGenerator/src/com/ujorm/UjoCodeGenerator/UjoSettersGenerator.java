package com.ujorm.UjoCodeGenerator;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Lookup;

public class UjoSettersGenerator extends GettersSettersGenerator implements CodeGenerator {
    public UjoSettersGenerator(Lookup context) {
        super(context);
    }
    
    @MimeRegistration(mimeType = "text/x-java", service = CodeGenerator.Factory.class)
    public static class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            // Tady se musí rozhodnout, zda-li je vůbec generátor použitelný
            return Collections.singletonList(new UjoSettersGenerator(context));
        }
    }

    @Override
    public String getDisplayName() {
        return "UJO setters generator";
    }

    @Override
    protected CancellableTask getModificationTask() {
        return new GenerateSettersTask();
    }
}
