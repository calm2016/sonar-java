/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.model.expression;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.impl.Parser;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.sonar.java.ast.parser.JavaParser;
import org.sonar.java.model.declaration.MethodTreeImpl;
import org.sonar.java.resolve.SemanticModel;
import org.sonar.java.resolve.Symbol;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ExpressionStatementTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.StatementTree;

import java.io.File;

public class MethodInvocationTreeImplTest {

  private final Parser p = JavaParser.createParser(Charsets.UTF_8);

  @Test
  public void symbol_should_be_set() {
    CompilationUnitTree cut = createTree("class A { void foo(){} void bar(){foo();} }");
    ClassTree classTree = (ClassTree) cut.types().get(0);
    Symbol.MethodSymbol declaration = ((MethodTreeImpl) classTree.members().get(0)).getSymbol();
    StatementTree statementTree = ((MethodTree) classTree.members().get(1)).block().body().get(0);
    MethodInvocationTreeImpl mit = (MethodInvocationTreeImpl) ((ExpressionStatementTree)statementTree).expression();
    Assertions.assertThat(mit.getSymbol()).isSameAs(declaration);
  }

  private CompilationUnitTree createTree(String code) {
    AstNode astNode = p.parse(code);
    CompilationUnitTree compilationUnitTree = (CompilationUnitTree) astNode;
    SemanticModel.createFor(compilationUnitTree, Lists.<File>newArrayList());
    return compilationUnitTree;
  }
}