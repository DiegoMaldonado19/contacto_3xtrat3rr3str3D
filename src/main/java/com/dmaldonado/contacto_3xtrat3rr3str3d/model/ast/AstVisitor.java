package com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast;

import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.ArrayDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.FunctionDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.Parameter;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.Program;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.StructDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.StructField;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.declaration.VariableDeclaration;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.ArrayAccessExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.BinaryExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.CompositeLiteralExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.FunctionCallExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.IdentifierExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.IncrementExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.LiteralExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.MemberAccessExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.expression.UnaryExpression;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.Assignment;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.Block;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.BreakStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.CallStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ContinueStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.DoWhileStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ForStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.IfStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.IncrementStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.InputStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.PrintStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.ReturnStatement;
import com.dmaldonado.contacto_3xtrat3rr3str3d.model.ast.statement.WhileStatement;

/**
 * Visitor pattern over the AST.
 *
 * Every phase that walks the tree (semantic analysis, PigLatin translation)
 * implements this interface, so a new phase can be added without touching a
 * single node class.
 */
public interface AstVisitor<T>
{
    T visitProgram(Program node);
    T visitVariableDeclaration(VariableDeclaration node);
    T visitArrayDeclaration(ArrayDeclaration node);
    T visitStructDeclaration(StructDeclaration node);
    T visitStructField(StructField node);
    T visitFunctionDeclaration(FunctionDeclaration node);
    T visitParameter(Parameter node);

    T visitBlock(Block node);
    T visitAssignment(Assignment node);
    T visitIfStatement(IfStatement node);
    T visitWhileStatement(WhileStatement node);
    T visitDoWhileStatement(DoWhileStatement node);
    T visitForStatement(ForStatement node);
    T visitReturnStatement(ReturnStatement node);
    T visitBreakStatement(BreakStatement node);
    T visitContinueStatement(ContinueStatement node);
    T visitPrintStatement(PrintStatement node);
    T visitInputStatement(InputStatement node);
    T visitCallStatement(CallStatement node);
    T visitIncrementStatement(IncrementStatement node);

    T visitBinaryExpression(BinaryExpression node);
    T visitUnaryExpression(UnaryExpression node);
    T visitIncrementExpression(IncrementExpression node);
    T visitLiteralExpression(LiteralExpression node);
    T visitIdentifierExpression(IdentifierExpression node);
    T visitArrayAccessExpression(ArrayAccessExpression node);
    T visitMemberAccessExpression(MemberAccessExpression node);
    T visitFunctionCallExpression(FunctionCallExpression node);
    T visitCompositeLiteralExpression(CompositeLiteralExpression node);
}
