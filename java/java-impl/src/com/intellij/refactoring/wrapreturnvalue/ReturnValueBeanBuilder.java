/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.refactoring.wrapreturnvalue;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.VariableKind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ReturnValueBeanBuilder {
  private final List<PsiTypeParameter> myTypeParams = new ArrayList<PsiTypeParameter>();
  private String myClassName = null;
  private String myPackageName = null;
  private Project myProject = null;
  private PsiType myValueType = null;
  private boolean myStatic;

  public void setClassName(String className) {
    myClassName = className;
  }

  public void setPackageName(String packageName) {
    myPackageName = packageName;
  }

  public void setTypeArguments(List<PsiTypeParameter> typeParams) {
    myTypeParams.clear();
    myTypeParams.addAll(typeParams);
  }

  public void setProject(Project project) {
    myProject = project;
  }

  public void setValueType(PsiType valueType) {
    myValueType = valueType;
  }

  public void setStatic(boolean isStatic) {
    myStatic = isStatic;
  }

  public String buildBeanClass() throws IOException {
    final StringBuilder out = new StringBuilder(1024);

    if (myPackageName.length() > 0) out.append("package ").append(myPackageName).append(';');
    out.append('\n');
    out.append("public ");
    if (myStatic) out.append("static ");
    out.append("class ").append(myClassName);
    if (!myTypeParams.isEmpty()) {
      out.append('<');
      boolean first = true;
      for (PsiTypeParameter typeParam : myTypeParams) {
        if (!first) {
          out.append(',');
        }
        final String parameterText = typeParam.getText();
        out.append(parameterText);
        first = false;
      }
      out.append('>');
    }
    out.append('\n');

    out.append('{');
    outputField(out);
    out.append('\n');
    outputConstructor(out);
    out.append('\n');
    outputGetter(out);
    out.append("}\n");
    return out.toString();
  }

  private void outputGetter(StringBuilder out) {
    final String typeText = myValueType.getCanonicalText();
    final String name = "value";
    final String capitalizedName = StringUtil.capitalize(name);
    out.append("\tpublic ").append(typeText).append(" get").append(capitalizedName).append("()\n");
    out.append("\t{\n");
    final String fieldName = getFieldName(name);
    out.append("\t\treturn ").append(fieldName).append(";\n");
    out.append("\t}\n");
    out.append('\n');
  }

  private void outputField(StringBuilder out) {
    final String typeText = myValueType.getCanonicalText();
    out.append('\t' + "private final ").append(typeText).append(' ').append(getFieldName("value")).append(";\n");
  }

  private void outputConstructor(StringBuilder out) {
    out.append("\tpublic ").append(myClassName).append('(');
    final String typeText = myValueType.getCanonicalText();
    final String name = "value";
    final String parameterName =
      JavaCodeStyleManager.getInstance(myProject).propertyNameToVariableName(name, VariableKind.PARAMETER);
    out.append(CodeStyleSettingsManager.getSettings(myProject).GENERATE_FINAL_PARAMETERS ? "final " : "");
    out.append(typeText).append(' ').append(parameterName);
    out.append(")\n");
    out.append("\t{\n");
    final String fieldName = getFieldName(name);
    if (fieldName.equals(parameterName)) {
      out.append("\t\tthis.").append(fieldName).append(" = ").append(parameterName).append(";\n");
    }
    else {
      out.append("\t\t").append(fieldName).append(" = ").append(parameterName).append(";\n");
    }
    out.append("\t}\n");
    out.append('\n');
  }

  private String getFieldName(final String name) {
    return JavaCodeStyleManager.getInstance(myProject).propertyNameToVariableName(name, VariableKind.FIELD);
  }
}
