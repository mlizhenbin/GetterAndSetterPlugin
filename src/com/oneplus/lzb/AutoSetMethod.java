package org.lzbruby.lzb;

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.CollectionListModel;

import java.util.List;

/**
 * 一加科技
 *
 * @description:
 * @author: Zhenbin.Li
 * @createDate: 15/6/17 17:30
 */
public class AutoSetMethod extends AutoGetAndSetMethod {

    @Override
    protected void createO2O(PsiMethod psiMethod) {
        String methodName = psiMethod.getName();
        PsiType returnType = psiMethod.getReturnType();
        if (returnType == null) {
            return;
        }
        String returnClassName = returnType.getPresentableText();
        String resultClassWithPackage = returnType.getInternalCanonicalText();
        JavaPsiFacade facade = JavaPsiFacade.getInstance(psiMethod.getProject());
        PsiClass resultClass = facade.findClass(resultClassWithPackage, GlobalSearchScope.allScope(psiMethod.getProject()));
        if (resultClass == null) {
            return;
        }

        List<PsiField> resultFields = new CollectionListModel<PsiField>(resultClass.getFields()).getItems();
        String methodText = getMethodText(methodName, returnClassName, resultFields);
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        PsiMethod toMethod = elementFactory.createMethodFromText(methodText, psiMethod);
        psiMethod.replace(toMethod);
    }

    protected String getMethodText(String methodName, String returnClassName, List<PsiField> resultFields) {
        String returnObjName = returnClassName.substring(0, 1).toLowerCase() + returnClassName.substring(1);
        StringBuilder builder = new StringBuilder("public " + returnClassName + " " + methodName + " (");
        builder.append(" ) {\n");
        builder.append(returnClassName + " " + returnObjName + "= new " + returnClassName + "();\n");
        for (PsiField field : resultFields) {
            PsiModifierList modifierList = field.getModifierList();
            if (modifierList == null || modifierList.hasModifierProperty(PsiModifier.STATIC) || modifierList.hasModifierProperty(PsiModifier.FINAL) || modifierList.hasModifierProperty(PsiModifier.SYNCHRONIZED)) {
                continue;
            }
            builder.append(returnObjName + ".set" + getFirstUpperCase(field.getName()) + "();\n");
        }
        builder.append("return " + returnObjName + ";\n");
        builder.append("}\n");
        return builder.toString();
    }
}
